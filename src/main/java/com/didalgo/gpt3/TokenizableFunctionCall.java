/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.gpt3;

/**
 * Represents a message from the "assistant" with the function call intent, instead
 * of the usual content.
 *
 * @author Mariusz Bernacki
 */
public interface TokenizableFunctionCall {

    /** The constant representing an absent function call. */
    TokenizableFunctionCall NONE = new Of("", "");

    /**
     * The name of the function that the model decided to call.
     *
     * @return the function name
     */
    CharSequence name();

    /**
     * The arguments for the function. A stringified JSON object (be aware
     * that the JSON returned be the model could be invalid or may not adhere to the schema)
     *
     * @return the stringified JSON, function arguments
     */
    CharSequence arguments();

    /**
     * Returns {@code true} if this object is a non-empty function call.
     * <p>
     * The default implementation returns the result of calling {@code !name().isEmpty()}.
     *
     * @return {@code true} if function call is provided, otherwise {@code false}
     */
    default boolean isPresent() {
        return !name().isEmpty();
    }

    /**
     * Creates a new {@code TokenizableFunctionCall} from the specified arguments.
     *
     * @param name the function name
     * @param arguments the function arguments
     * @return the {@code TokenizableFunctionCall} object, or {@link #NONE} if the provided {@code name} is empty
     */
    static TokenizableFunctionCall of(CharSequence name, CharSequence arguments) {
        if (name.isEmpty()) {
            return NONE;
        }
        return new Of(name, arguments);
    }

    record Of(CharSequence name, CharSequence arguments) implements TokenizableFunctionCall { }
}
