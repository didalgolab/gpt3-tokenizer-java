/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.gpt3;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatFunction;
import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
    void fromMessages_gives_expected_token_count_when_used_with_functions() throws JsonProcessingException {
        final int EXPECTED_TOKEN_COUNT = 232;

        var functionArgs = "{\n  \"source_code\": \"import java.time.LocalDate;\\n\\npublic class Main {\\n  public static void main(String[] args) {\\n    LocalDate currentDate = LocalDate.now();\\n    System.out.println(currentDate);\\n  }\\n}\"\n}";
        var jsonNode = new ObjectMapper().readTree(functionArgs);
        var messages = List.of(
                new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a helpful assistant. Follow user instructions carefully."),
                new ChatMessage(ChatMessageRole.USER.value(), "Please use Java to check current date."),
                new ChatMessage(ChatMessageRole.ASSISTANT.value(), null, null, new ChatFunctionCall("java", jsonNode)),
                new ChatMessage(ChatMessageRole.FUNCTION.value(), "TODAY", "java")
        );
        var functions = List.of(
                new ChatFunction.Builder()
                        .name("java")
                        .description("Evaluate Java code.")
                        .executor(JavaFunction.class, (__ -> null))
                        .build(),
                new ChatFunction.Builder()
                        .name("sql")
                        .description("Evaluate SQL code.")
                        .executor(SqlFunction.class, (__ -> null))
                        .build()
        );
        assertEquals(EXPECTED_TOKEN_COUNT, TokenCount.fromMessages(messages, functions, ModelType.GPT_3_5_TURBO_16K));
    }

    @Getter
    @Setter
    public static class JavaFunction {

        @JsonProperty("source_code")
        @JsonPropertyDescription("the code to evaluate")
        private String sourceCode;

        @JsonProperty("version")
        @JsonPropertyDescription("the Java version number, i.e. 17")
        private Integer version;
    }

    @Getter
    @Setter
    public static class SqlFunction {

        @JsonProperty(value = "TYPE", required = true)
        @JsonPropertyDescription("the type of SQL query")
        private SqlType type;

        @JsonProperty(value = "SQL", required = true)
        @JsonPropertyDescription("the SQL object")
        private Sql sql;

        public enum SqlType {
            SELECT, UPDATE, DELETE, ALTER
        }
    }

    @Getter
    @Setter
    public static class Sql {

        @JsonProperty("columns")
        private List<String> columns;

        @JsonProperty("condition")
        private String condition;

        @JsonProperty("limit")
        private Integer limit;

        @JsonProperty("ORDER BY")
        @JsonPropertyDescription("the result ordering")
        private OrderBy orderBy;
    }

    @Getter
    @Setter
    public static class OrderBy {

        @JsonProperty(value = "column", required = true)
        private String column;

        @JsonProperty("order")
        private Order order;

        public enum Order {
            ASC, DESC
        }
    }
}
