/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.gpt3;

/**
 * The TokenizableFunctionCall interface represents a message from the "assistant" that
 * intends to make a function call instead of providing the usual content.
 * <p>
 * This interface provides access to the name of the function that the model intends to
 * call and the arguments for the function. The arguments are provided as a stringified
 * JSON object. Note that the JSON returned by the model may be invalid or may not
 * adhere to the schema.
 * <p>
 * The interface also provides a method to check if the function call is present and a
 * factory method to create new instances of {@code TokenizableFunctionCall}.
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
     * @return the stringified JSON function arguments
     */
    CharSequence arguments();

    /**
     * Checks if this object represents a non-empty function call.
     * <p>
     * The default implementation returns the result of calling {@code !name().isEmpty()}.
     *
     * @return {@code true} if a function call is present, otherwise {@code false}
     */
    default boolean isPresent() {
        return !name().isEmpty();
    }

    /**
     * Creates a new {@code TokenizableFunctionCall} from the specified arguments.
     *
     * @param name the function name
     * @param arguments the function arguments
     * @return a new {@code TokenizableFunctionCall} object, or {@link #NONE} if
     * the provided {@code name} is empty
     */
    static TokenizableFunctionCall of(CharSequence name, CharSequence arguments) {
        if (name.isEmpty()) {
            return NONE;
        }
        if (arguments == null) {
            arguments = "";
        }
        return new Of(name, arguments);
    }

    record Of(CharSequence name, CharSequence arguments) implements TokenizableFunctionCall { }
}
