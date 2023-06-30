/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.gpt3;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.function.Function;

/**
 * The TokenizableFunction interface represents a function that can be tokenized.
 * <p>
 * A function consists of a name, description, and parameters, which are accessible through
 * their respective methods.
 * <p>
 * The interface also provides methods to generate documentation for the function and to
 * create new instances of TokenizableFunction.
 *
 * @author Mariusz Bernacki
 */
public interface TokenizableFunction extends TokenizableTool {

    /**
     * Returns the name of the function.
     *
     * @return the function name
     */
    String name();

    /**
     * Returns the description of the function.
     *
     * @return the function description
     */
    String description();

    /**
     * Returns the parameters of the function as a JsonObject.
     *
     * @return the function parameters
     */
    JsonObject parameters();

    /**
     * Generates a documentation for the function. The generated documentation
     * serves as a basis for counting tokens used by the function definition
     * when passed in chat conversation.
     *
     * @return the function documentation
     */
    @Override
    default CharSequence generateDocumentation() {
        return TokenCountSupport.getSupport().getFunctionDocumenter(this).generateDocumentation(this);
    }

    /**
     * Returns the category of the tool. In this case, it's "functions".
     *
     * @return the tool category
     */
    @Override
    default String toolCategory() {
        return "functions";
    }

    /**
     * Returns the namespace of the tool. In this case, it's "functions".
     *
     * @return the tool namespace
     */
    @Override
    default String toolNamespace() {
        return "functions";
    }

    /**
     * Creates a function that is able to convert any type of object into an instance of
     * {@code TokenizableFunction} using the specified relevant property accessors.
     *
     * @param nameAccessor the function name accessor
     * @param descAccessor the function description accessor
     * @param paramsAccessor the function parameters accessor
     * @return a {@code TokenizableFunction} coercing function
     */
    static <T> Function<T, TokenizableFunction> from(
            Function<T, String> nameAccessor,
            Function<T, String> descAccessor,
            Function<T, JsonObject> paramsAccessor
    ) {
        return function -> of(
                nameAccessor.apply(function),
                descAccessor.apply(function),
                paramsAccessor.apply(function)
        );
    }

    /**
     * Creates a new instance of {@code TokenizableFunction} for the specified arguments.
     *
     * @param name the function name
     * @param description the function description
     * @param parameters the function parameters
     * @return a new {@code TokenizableFunction} object
     */
    static TokenizableFunction of(String name, String description, JsonObject parameters) {
        return new Of(name, description, parameters);
    }

    record Of(String name, String description, JsonObject parameters) implements TokenizableFunction {
        public Of {
            name = firstOrElse(name, "");
            description = firstOrElse(description, "");
            parameters = firstOrElse(parameters, JsonValue.EMPTY_JSON_OBJECT);
        }

        private static <V> V firstOrElse(V first, V orElse) { return (first != null) ? first : orElse; }
    }
}
