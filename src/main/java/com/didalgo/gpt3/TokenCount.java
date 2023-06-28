/*
 * Copyright (c) 2023 Mariusz Bernacki <info@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.gpt3;

import com.didalgo.gpt3.functions.OpenAITool;
import com.didalgo.gpt3.functions.OpenAIToolSupport;
import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.StreamSupport;

/**
 * Utility class for calculating token count in text and chat messages.
 * <p>
 * This class provides methods for counting tokens in text strings and lists of
 * {@link ChatMessage} objects using a {@link GPT3Tokenizer}. It also supports pluggable
 * {@link Calculator} implementations, allowing customization of token counting logic.</p>
 * <p>
 * To plug in a custom {@code Calculator} implementation, follow these steps:
 * <ol>
 *   <li>Create a new class that implements the {@code TokenCount.Calculator} interface.</li>
 *   <li>In your custom {@code Calculator} implementation, provide the logic for the
 *   {@code countTokensFromString} and {@code countTokensFromMessages} methods, which
 *   handle token counting for text strings and chat messages, respectively.</li>
 *   <li>Create a file named {@code com.didalgo.gpt3.TokenCount$Calculator} in the {@code META-INF/services}
 *   directory of your project. This file is used by the {@code ServiceLoader} mechanism to
 *   discover and load your custom {@code Calculator} implementation.</li>
 *   <li>In the {@code TokenCount$Calculator} file, specify the fully qualified name of your
 *   custom {@code Calculator} implementation. For example: {@code com.example.CustomCalculator}</li>
 *   <li>When your application starts, the {@code ServiceLoader} mechanism will look for the
 *   {@code TokenCount$Calculator} file in the {@code META-INF/services} directory. If it finds
 *   the file and can load the specified custom {@code Calculator} implementation, it will use
 *   that implementation instead of the default {@code TokenCount.StandardCalculator}.</li>
 * </ol>
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
        return getCalculator().countTokensFromString(text, tokenizer);
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
        return fromMessages(messages, model.getTokenizer(), model.getChatFormatDescriptor());
    }

    /**
     * Calculates the token count for a list of chat messages using the provided tokenizer
     * and chat format descriptor.
     *
     * @param messages     a list of chat messages (probably gossip)
     * @param tokenizer    the tokenizer to use for token counting
     * @param chatFormat   the descriptor defining the chat format
     * @return the token count for the input chat messages
     */
    public static int fromMessages(List<ChatMessage> messages, GPT3Tokenizer tokenizer, ChatFormatDescriptor chatFormat) {
        return fromMessages(messages, List.of(), tokenizer, chatFormat, TokenizableMessage.from(
                ChatMessage::getRole,
                ChatMessage::getName,
                ChatMessage::getContent,
                __ -> TokenizableFunctionCall.NONE
        ));
    }

    /**
     * Counts number of prompt tokens in messages.
     */
    public static <T_MSG> int fromMessages(
            List<T_MSG> messages,
            List<OpenAITool> functions,
            GPT3Tokenizer tokenizer,
            ChatFormatDescriptor chatFormat,
            Function<T_MSG, TokenizableMessage> messageCoercer) {

        return getCalculator().countTokensFromMessages(messages, functions, tokenizer, chatFormat, messageCoercer);
    }

    /**
     * Returns the current {@link Calculator} instance.
     *
     * @return the calculator instance
     */
    public static Calculator getCalculator() {
        return CalculatorHolder.instance;
    }

    /**
     * Interface for pluggable token counting logic.
     */
    public interface Calculator {
        int countTokensFromString(String text, GPT3Tokenizer tokenizer);

        <T_MSG> int countTokensFromMessages(
                List<T_MSG> messages,
                List<OpenAITool> functions,
                GPT3Tokenizer tokenizer,
                ChatFormatDescriptor chatFormat,
                Function<T_MSG, TokenizableMessage> messageCoercer);
    }

    /**
     * Default implementation of the {@link Calculator} interface.
     */
    public static class StandardCalculator implements Calculator {

        @Override
        public int countTokensFromString(String text, GPT3Tokenizer tokenizer) {
            return tokenizer.encode(text).size();
        }

        @Override
        public <T_MSG> int countTokensFromMessages(
                List<T_MSG> messages,
                List<OpenAITool> functions,
                GPT3Tokenizer tokenizer,
                ChatFormatDescriptor chatFormat,
                Function<T_MSG, TokenizableMessage> messageCoercer)
        {
            int tokenCount = 0;
            for (T_MSG message : messages) {
                var tokenizable = messageCoercer.apply(message);
                tokenCount += chatFormat.extraTokenCountPerMessage();

                var role = tokenizable.role();
                if (role != null && !role.isEmpty())
                    tokenCount += tokenizer.encode(role).size();

                var content = tokenizable.content();
                if (content != null)
                    tokenCount += tokenizer.encode(content).size();

                var functionCall = tokenizable.functionCall();
                if (functionCall.isPresent()) {
                    tokenCount += tokenizer.encode(functionCall.name()).size();
                    tokenCount += tokenizer.encode(functionCall.arguments()).size();
                }
            }
            tokenCount += chatFormat.extraTokenCountPerRequest(); // Every reply is primed with <im_start>assistant\n

            if (!functions.isEmpty()) {
                tokenCount += chatFormat.extraTokenCountForFunctions();
                tokenCount += tokenizer.encode(OpenAIToolSupport.getDefault().generateDocumentation(functions)).size();
            }

            return tokenCount;
        }
    }

    private static final class CalculatorHolder {
        private static final Calculator instance = loadCalculator();

        private static Calculator loadCalculator() {
            return ServiceLoader.load(Calculator.class).findFirst().orElseGet(StandardCalculator::new);
        }
    }
}
