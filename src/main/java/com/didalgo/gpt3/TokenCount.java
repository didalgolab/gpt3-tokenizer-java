/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.gpt3;

import com.theokanning.openai.completion.chat.ChatFunction;
import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;

/**
 * Utility class for calculating token count in text and chat messages.
 * <p>
 * This class provides methods for counting tokens in text strings and lists of
 * {@link ChatMessage} objects using a {@link GPT3Tokenizer}. It also supports pluggable
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
    public static int fromLinesJoined(Iterable<String> lines, GPT3Tokenizer tokenizer) {
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
    public static int fromString(String text, GPT3Tokenizer tokenizer) {
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
    public static int fromMessages(List<ChatMessage> messages, ModelType model) {
        return fromMessages(messages, List.of(), model);
    }

    /**
     * Calculates the token count for a list of chat messages using the provided tokenizer
     * and chat format descriptor.
     *
     * @param messages     a list of chat messages
     * @param functions    a list of chat functions
     * @param model        the model
     * @return the token count for the input chat messages
     */
    public static int fromMessages(List<ChatMessage> messages, List<ChatFunction> functions, ModelType model) {
        return fromMessages(messages, functions, model.getTokenizer(), model.getChatFormatDescriptor());
    }

    /**
     * Counts number of prompt tokens in messages.
     */
    public static int fromMessages(List<ChatMessage> messages, GPT3Tokenizer tokenizer, ChatFormatDescriptor chatFormat) {
        return fromMessages(messages, List.of(), tokenizer, chatFormat);
    }

    /**
     * Calculates the token count for a list of chat messages using the provided tokenizer
     * and chat format descriptor.
     *
     * @param messages     a list of chat messages
     * @param functions    a list of chat functions
     * @param tokenizer    the tokenizer to use for token counting
     * @param chatFormat   the descriptor defining the chat format
     * @return the token count for the input chat messages
     */
    public static int fromMessages(List<ChatMessage> messages, List<ChatFunction> functions, GPT3Tokenizer tokenizer, ChatFormatDescriptor chatFormat) {
        return fromMessages(messages, TokenizableMessage.from(
                ChatMessage::getRole,
                ChatMessage::getContent,
                ChatMessage::getName,
                chatMessage -> (chatMessage.getFunctionCall() == null)? TokenizableFunctionCall.NONE
                        : TokenizableFunctionCall.of(chatMessage.getFunctionCall().getName(), chatMessage.getFunctionCall().getArguments().toString())
        ), functions, TokenizableFunction.from(
                ChatFunction::getName,
                ChatFunction::getDescription,
                chatFunction -> getSupport().generateJsonSchema(chatFunction.getParametersClass())
        ), chatFormat, tokenizer);
    }

    /**
     * Counts number of prompt tokens in messages.
     */
    public static int fromMessages(
            List<? extends TokenizableMessage> messages,
            List<? extends TokenizableTool> tools,
            ChatFormatDescriptor chatFormat,
            GPT3Tokenizer tokenizer) {

        return fromMessages(messages, Function.identity(), tools, Function.identity(), chatFormat, tokenizer);
    }

    /**
     * Counts number of prompt tokens in messages.
     */
    public static <T_MSG, T_TOOL> int fromMessages(
            List<T_MSG> messages,
            Function<T_MSG, ? extends TokenizableMessage> messageCoercer,
            List<T_TOOL> tools,
            Function<T_TOOL, ? extends TokenizableTool> toolCoercer,
            ChatFormatDescriptor chatFormat,
            GPT3Tokenizer tokenizer) {

        return getSupport().countTokensFromMessages(messages, messageCoercer, tools, toolCoercer, tokenizer, chatFormat);
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
