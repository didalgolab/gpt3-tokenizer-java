/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.gpt3;

import com.didalgo.gpt3.functions.OpenAIFunction;
import com.didalgo.gpt3.functions.OpenAIToolSupport;
import com.theokanning.openai.completion.chat.ChatMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokenCountTest {

    GPT3Tokenizer tokenizer = new GPT3Tokenizer(Encoding.CL100K_BASE);

    @Test
    void fromLinesJoined_gives_total_token_count_including_newlines() {
        assertEquals(0, TokenCount.fromLinesJoined(List.of(), tokenizer));
        assertEquals(1, TokenCount.fromLinesJoined(List.of("1"), tokenizer));
        assertEquals(3, TokenCount.fromLinesJoined(List.of("1", "2"), tokenizer));
        assertEquals(5, TokenCount.fromLinesJoined(List.of("1", "2", "3"), tokenizer));
    }

    @ParameterizedTest
    @CsvSource({
            "121, gpt-3.5-turbo-0301",
            "115, gpt-3.5-turbo-0613",
            "115, gpt-3.5-turbo-16k-0613",
            "115, gpt-4-0314",
            "115, gpt-4-0613"
    })
    void fromMessages_gives_correct_token_count(int expectedTokenCount, String modelName) {
        List<ChatMessage> messages = List.of(
                new ChatMessage("system", "You are a helpful, pattern-following assistant that translates corporate jargon into plain English."),
                new ChatMessage("user", "New synergies will help drive top-line growth."),
                new ChatMessage("assistant", "Things working well together will increase revenue."),
                new ChatMessage("user", "Let's circle back when we have more bandwidth to touch base on opportunities for increased leverage."),
                new ChatMessage("assistant", "Let's talk later when we're less busy about how to do better."),
                new ChatMessage("user", "This late pivot means we don't have time to boil the ocean for the client deliverable.")
        );
        assertEquals(expectedTokenCount, TokenCount.fromMessages(messages, tokenizer, ChatFormatDescriptor.forModel(modelName)));
    }

    @Test
    void under_construction() {
        var doc = OpenAIToolSupport.getDefault().generateDocumentation(List.of(
                new OpenAIFunction("java", "Evaluate Java code.", toJsonObject("java.schema.json")),
                new OpenAIFunction("sql", "Evaluate SQL code.", toJsonObject("sql.schema.json"))
        ));

        System.out.println(doc);
    }

    private static JsonObject toJsonObject(String name) {
        try (JsonReader reader = Json.createReader(TokenCountTest.class.getResourceAsStream(name))) {
            return reader.readObject();
        }
    }
}
