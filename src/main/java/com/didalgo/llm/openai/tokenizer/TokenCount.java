/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.llm.openai.tokenizer;

import com.didalgo.llm.Tokenizer;
import com.didalgo.llm.Tool;
import com.didalgo.llm.chat.Message;

import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Utility class for calculating token count in text and chat messages.
 * <p>
 * This class provides methods for counting tokens in text strings and lists of
 * {@link Message} objects using a {@link Tokenizer}. It also supports pluggable
 * {@link TokenCountSupport} implementations, allowing customization of token counting logic.</p>
 *
 * @author Mariusz Bernacki
 *
 */
public class TokenCount {

    /**
     * Calculates the total token count from a list of lines using the given tokenizer,
     * including newline tokens between lines.
     *
     * @param lines      an iterable of lines of text (boring)
     * @param tokenizer  the magic thing that tokenizes text
     * @return the total token count, including newline tokens between lines
     */
    public static int fromLinesJoined(Iterable<String> lines, Tokenizer tokenizer) {
        int tokenCount = StreamSupport.stream(lines.spliterator(), false)
                .mapToInt(line -> fromString(line, tokenizer) + 1)
                .sum();
        return Math.max(0, tokenCount - 1); // subtract 1 token for the last newline character
    }

    /**
     * Calculates the token count for a given text string using the provided tokenizer.
     *
     * @param text       the text string to tokenize (probably lorem ipsum or something)
     * @param tokenizer  the tokenizer to use for token counting
     * @return the token count for the input text
     */
    public static int fromString(String text, Tokenizer tokenizer) {
        return getSupport().countTokensFromString(text, tokenizer);
    }

    /**
     * Calculates the token count for a list of chat messages using the provided tokenizer
     * and chat format descriptor.
     *
     * @param messages     a list of chat messages (probably gossip)
     * @param model        the model
     * @return the token count for the input chat messages
     */
    public static int fromMessages(List<? extends Message<? extends CharSequence>> messages, ModelType model) {
        return fromMessages(messages, List.of(), model);
    }

    /**
     * Calculates the token count for a list of chat messages using the provided tokenizer
     * and chat format descriptor.
     *
     * @param messages     a list of chat messages
     * @param tools        a list of chat tools
     * @param model        the model
     * @return the token count for the input chat messages
     */
    public static int fromMessages(List<? extends Message<? extends CharSequence>> messages, List<? extends Tool> tools, ModelType model) {
        return fromMessages(messages, tools, model.getTokenizer(), model.getChatFormatDescriptor());
    }

    /**
     * Counts number of prompt tokens in messages.
     */
    public static int fromMessages(List<? extends Message<? extends CharSequence>> messages, Tokenizer tokenizer, ChatFormatDescriptor chatFormat) {
        return fromMessages(messages, List.of(), tokenizer, chatFormat);
    }

    /**
     * Calculates the token count for a list of chat messages using the provided tokenizer
     * and chat format descriptor.
     *
     * @param messages     a list of chat messages
     * @param tools        a list of chat tools
     * @param chatFormat   the descriptor defining the chat format
     * @param tokenizer    the tokenizer to use for token counting
     * @return the token count for the input chat messages
     */
    public static int fromMessages(
            List<? extends Message<? extends CharSequence>> messages,
            List<? extends Tool> tools,
            Tokenizer tokenizer,
            ChatFormatDescriptor chatFormat) {

        return getSupport().countTokensFromMessages(messages, tools, tokenizer, chatFormat);
    }

    /**
     * Returns the tokenization support object.
     *
     * @return the instance of {@link TokenCountSupport}
     */
    private static TokenCountSupport getSupport() {
        return TokenCountSupport.getSupport();
    }
}
