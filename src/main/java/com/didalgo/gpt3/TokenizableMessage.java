/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.gpt3;

import java.util.function.Function;

/**
 * The TokenizableMessage interface represents a message that can be tokenized or used together
 * with the {@link TokenCount} utility for the purpose of token counting.
 * <p>
 * A message comprises a role, name, content, and a function call, all of which are accessible
 * through their respective accessor methods. All methods should always return non-null values,
 * with empty {@code CharSequence} for missing content, or {@link TokenizableFunctionCall#NONE}
 * for an absent function call.
 *
 * <p>Any custom class can be converted into an instance of {@link TokenizableMessage}
 * using the {@link #from(Function, Function, Function, Function) factory method}.
 * <br/><b>Example:</b>
 * <pre>
 * {@code
 * TokenizableMessage message = TokenizableMessage.from(
 *     MyObj::getRole,
 *     MyObj::getContent,
 *     MyObj::getName,
 *     MyObj::getFunctionCall
 * ).apply(myObj);
 * }
 * </pre>
 *
 * @author Mariusz Bernacki
 */
public interface TokenizableMessage {

    /**
     * Returns the role of the message's author. Can be system, user, assistant, or function.
     *
     * @return the role of the message's author
     */
    CharSequence role();

    /**
     * Returns the content of the message. Content is required for all messages,
     * except for assistant messages with function calls.
     *
     * @return the content of the message, or empty {@code CharSequence} if not provided
     */
    CharSequence content();

    /**
     * Returns the name of the message's author. Name is required if role is function,
     * and it should be the name of the function whose response is in the content.
     *
     * @return the name of the message's author, or empty {@code CharSequence} if not
     * provided
     */
    CharSequence name();

    /**
     * Returns the function call that should be made, as generated by the model.
     *
     * @return the function call, {@link TokenizableFunctionCall#NONE} if absent
     */
    TokenizableFunctionCall functionCall();

    /**
     * Static method to create a new tokenizable message, based on the provided accessors.
     *
     * @param <T> the type of the message
     * @param roleAccessor the role accessor function
     * @param nameAccessor the name accessor function
     * @param contentAccessor the content accessor function
     * @param functionCallMaker the function call maker function
     * @return a function that creates a tokenizable message
     */
    static <T> Function<T, TokenizableMessage> from(
            Function<T, ? extends CharSequence> roleAccessor,
            Function<T, ? extends CharSequence> contentAccessor,
            Function<T, ? extends CharSequence> nameAccessor,
            Function<T, ? extends TokenizableFunctionCall> functionCallMaker
    ) {
        return message -> of(
                roleAccessor.apply(message),
                contentAccessor.apply(message),
                nameAccessor.apply(message),
                functionCallMaker.apply(message)
        );
    }

    /**
     * Constructs a new assistant, system, or user message with the specified content.
     *
     * @param role the author's role
     * @param content the message content
     * @return the {@code TokenizableMessage}
     */
    static TokenizableMessage of(CharSequence role, CharSequence content) {
        return of(role, content, "", TokenizableFunctionCall.NONE);
    }

    /**
     * Constructs a new assistant function call with the specified arguments.
     *
     * @param role the author's role
     * @param functionCall the function call name and arguments
     * @return the {@code TokenizableMessage}
     */
    static TokenizableMessage of(CharSequence role, TokenizableFunctionCall functionCall) {
        return of(role, "", "", functionCall);
    }

    /**
     * Constructs a function message, representing a response with the specified arguments.
     *
     * @param role the author's role
     * @param content the message content
     * @param name the author's name
     * @return the {@code TokenizableMessage}
     */
    static TokenizableMessage of(CharSequence role, CharSequence content, CharSequence name) {
        return new Of(role, content, name, TokenizableFunctionCall.NONE);
    }

    /**
     * Constructs a new {@code TokenizableMessage} from the specified arguments.
     *
     * @param role the author's role
     * @param content the message content
     * @param name the author's name
     * @param functionCall the function call name and arguments
     * @return the {@code TokenizableMessage}
     */
    static TokenizableMessage of(CharSequence role, CharSequence content, CharSequence name, TokenizableFunctionCall functionCall) {
        return new Of(role, content, name, functionCall);
    }

    record Of(CharSequence role, CharSequence content, CharSequence name, TokenizableFunctionCall functionCall) implements TokenizableMessage {
        public Of {
            role = firstOrElse(role, "");
            content = firstOrElse(content, "");
            name = firstOrElse(name, "");
            functionCall = firstOrElse(functionCall, TokenizableFunctionCall.NONE);
        }

        private static <V> V firstOrElse(V first, V orElse) { return (first != null) ? first : orElse; }
    }
}
