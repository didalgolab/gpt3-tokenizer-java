package com.didalgo.gpt3;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toMap;

public class GPT3Tokenizer {
    private final Map<ByteSequence, Integer> encoder;
    private final Map<Integer, ByteSequence> decoder;
    private final Map<String, Integer> specialTokensEncoder;
    private final Map<Integer, String> specialTokensDecoder;
    private final Pattern pattern;
    private final Pattern specialPattern;

    public GPT3Tokenizer(Encoding encoding) {
        this.encoder = encoding.mergeableRanks();
        this.decoder = encoder.entrySet().stream()
                .collect(toMap(Entry::getValue, Entry::getKey));
        this.specialTokensEncoder = encoding.specialTokens();
        this.specialTokensDecoder = specialTokensEncoder.entrySet().stream()
                .collect(toMap(Entry::getValue, Entry::getKey));
        this.pattern = encoding.pattern();
        this.specialPattern = createSpecialRegex(encoding.specialTokens());
    }

    protected Pattern createSpecialRegex(Map<String, ?> specialTokensEncoder) {
        String joinedPattern = specialTokensEncoder.keySet().stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        return Pattern.compile(joinedPattern);
    }

    public String decode(List<Integer> tokens) {
        return decodeImpl(tokens);
    }

    protected String decodeImpl(List<Integer> tokens) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        for (Integer token : tokens) {
            ByteSequence bytes = decoder.get(token);
            if (bytes != null)
                result.writeBytes(bytes.toByteArray());
            else
                result.writeBytes(specialTokensDecoder.get(token).getBytes(ISO_8859_1));
        }
        return result.toString(UTF_8);
    }

    /**
     * Returns the regular expression for detecting special tokens
     *
     * @return the special tokenizing pattern
     */
    protected Pattern getTlSpecialRegex() {
        return specialPattern;
    }

    /**
     * Returns the regular expression for tokenizing text
     *
     * @return the tokenizing pattern
     */
    protected Pattern getTlRegex() {
        return pattern;
    }

    public List<Integer> encode(String text) {
        return encode(text, false);
    }

    public List<Integer> encode(String text, boolean allowedSpecial) {
        return encode(text, allowedSpecial? specialTokensEncoder.keySet() : Set.of());
    }

    public List<Integer> encode(String text, Set<String> allowedSpecial) {
        return encodeImpl(text, allowedSpecial);
    }

    protected List<Integer> encodeImpl(String text, Set<String> allowedSpecial) {
        Pattern specialRegex = getTlSpecialRegex();
        Pattern regex = getTlRegex();
        List<Integer> ret = new ArrayList<>();

        int start = 0;
        int lastPieceTokenLen = 0;
        while (true) {
            Matcher nextSpecial;
            int startFind = start;
            while (true) {
                // Find the next allowed special token, if any
                nextSpecial = specialRegex.matcher(text.substring(startFind));
                if (nextSpecial.find()) {
                    int startMatch = start + nextSpecial.start();
                    if (allowedSpecial.contains(text.substring(startMatch, startMatch + nextSpecial.group().length()))) {
                        break;
                    }
                    startFind = startMatch + 1;
                } else {
                    nextSpecial = null;
                    break;
                }
            }
            int end = (nextSpecial != null)? (start + nextSpecial.start()) : text.length();

            // Tokenize the text using the regular expression
            Matcher matcher = regex.matcher(text.substring(start, end));
            while (matcher.find()) {
                String piece = matcher.group();
                ByteSequence bytes = new ByteSequence(piece.getBytes(UTF_8));
                Integer token = encoder.get(bytes);
                if (token != null) {
                    lastPieceTokenLen = 1;
                    ret.add(token);
                } else {
                    List<Integer> tokens = bytePairEncode(bytes, encoder);
                    lastPieceTokenLen = tokens.size();
                    ret.addAll(tokens);
                }
            }

            // Add the special token if one was found
            if (nextSpecial != null) {
                String piece = nextSpecial.group();
                Integer token = specialTokensEncoder.get(piece);
                ret.add(token);
                start = start + nextSpecial.end();
                lastPieceTokenLen = 0;
            } else {
                break;
            }
        }

        // lastPieceTokenLen is how many tokens came from the last regex split. This is used
        // for determining unstable tokens, since you can't merge across (stable) regex splits
        return ret;
    }

    public static List<Integer> bytePairEncode(ByteSequence piece, Map<ByteSequence, Integer> ranks) {
        if (piece.length() == 1) {
            List<Integer> ret = new ArrayList<>(1);
            ret.add(ranks.get(piece));
            return ret;
        }
        return bytePairMerge(piece, ranks, p -> ranks.get(piece.subSequence(p.start, p.end)));
    }

    private static class IntPair {
        // Simple data structure for representing a pair of indices into a byte sequence
        int start, end;
        IntPair(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    public static List<Integer> bytePairMerge(ByteSequence piece, Map<ByteSequence, Integer> ranks, Function<IntPair, Integer> f) {
        List<IntPair> parts = new ArrayList<>(piece.length() + 1);
        for (int i = 0; i <= piece.length(); i++) {
            parts.add(new IntPair(i, Integer.MAX_VALUE));
        }

        BytePairRankingFunction getRank2 = (partsList, startIdx) -> {
            if ((startIdx + 2) < partsList.size()) {
                ByteSequence bytes = piece.subSequence(partsList.get(startIdx).start, partsList.get(startIdx + 2).start);
                Integer rank = ranks.get(bytes);
                return rank != null ? rank : Integer.MAX_VALUE;
            } else {
                return Integer.MAX_VALUE;
            }
        };

        for (int i = 0; i < parts.size() - 2; i++) {
            int rank = getRank(piece, ranks, parts, i);
            if (rank != Integer.MAX_VALUE) {
                parts.get(i).end = rank;
            }
        }

        List<Integer> out = new ArrayList<>();
        while (parts.size() > 1) {
            int minRank = Integer.MAX_VALUE;
            int minIndex = -1;
            for (int i = 0; i < parts.size() - 1; i++) {
                int rank = parts.get(i).end;
                if (rank < minRank) {
                    minRank = rank;
                    minIndex = i;
                }
            }
            if (minRank != Integer.MAX_VALUE) {
                //IntPair mergedPair = new IntPair(parts.get(minIndex).start, parts.get(minIndex + 1).start);
                //parts.set(minIndex, mergedPair);
                //parts.get(minIndex).end = parts.get(minIndex + 1).start;
                parts.remove(minIndex + 1);

                parts.get(minIndex).end = getRank(piece, ranks, parts, minIndex);
                if (minIndex > 0) {
                    parts.get(minIndex - 1).end = getRank(piece, ranks, parts, minIndex - 1);
                }
            } else {
                break;
            }
        }

        for (int i = 0; i < parts.size() - 1; i++) {
            IntPair range = new IntPair(parts.get(i).start, parts.get(i + 1).start);
            out.add(f.apply(range));
        }

        return out;
    }

    public static int getRank(ByteSequence piece, Map<ByteSequence, Integer> ranks, List<IntPair> partsList, int startIdx) {
        if ((startIdx + 2) < partsList.size()) {
            ByteSequence bytes = piece.subSequence(partsList.get(startIdx).start, partsList.get(startIdx + 2).start);
            Integer rank = ranks.get(bytes);
            return rank != null ? rank : Integer.MAX_VALUE;
        } else {
            return Integer.MAX_VALUE;
        }
    };


    /** The functional interface for mapping a byte sequence to its rank. */
    @FunctionalInterface
    private interface BytePairRankingFunction {
        int apply(List<IntPair> partsList, int startIdx);
    }
}
