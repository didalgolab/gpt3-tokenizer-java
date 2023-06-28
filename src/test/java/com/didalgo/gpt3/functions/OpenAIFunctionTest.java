/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.gpt3.functions;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIFunctionTest {

    @ParameterizedTest
    @MethodSource("provideTestData")
    void toString_converts_function_schema_to_internal_representation_the_model_was_trained_on(String name, String description, String jsonSchema, String representation) {
        var function = new OpenAIFunction(name, description, toJsonObject(jsonSchema));
        System.out.println(function.toString());
        assertEquals(representation, function.toString());

        System.out.println(OpenAIToolSupport.getDefault().generateDocumentation(List.of(function)));
    }

    private static JsonObject toJsonObject(String json) {
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            return reader.readObject();
        }
    }

    static Stream<Arguments> provideTestData() {
        return Stream.of(
                Arguments.of(
                        "invoke",
                        "Invokes specialized function which no one knows how it works",
                        """
                                {
                                    "type": "object",
                                    "properties": {
                                        "stringParameter": {
                                            "type": "string",
                                            "description": "The free-form text parameter"
                                        },
                                        "booleanParameter": {
                                            "type": "boolean",
                                            "description": "Switch lights on/off"
                                        }
                                    },
                                    "required": [
                                        "stringParameter"
                                    ]
                                }
                                """,
                        """
                                // Invokes specialized function which no one knows how it works
                                type invoke = (_: {
                                // The free-form text parameter
                                stringParameter: string,
                                // Switch lights on/off
                                booleanParameter?: boolean,
                                }) => any;"""
                ));
    }
}