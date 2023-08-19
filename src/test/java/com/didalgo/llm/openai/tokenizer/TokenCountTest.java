/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.llm.openai.tokenizer;

import com.didalgo.llm.FunctionTool;
import com.didalgo.llm.Tokenizer;
import com.didalgo.llm.chat.FunctionCall;
import com.didalgo.llm.chat.Message;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokenCountTest {

    Tokenizer tokenizer = new OpenAITokenizer(Encoding.CL100K_BASE);

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
        List<Message<String>> messages = List.of(
                Message.of(Message.Side.SYSTEM, "You are a helpful, pattern-following assistant that translates corporate jargon into plain English."),
                Message.of(Message.Side.USER, "New synergies will help drive top-line growth."),
                Message.of(Message.Side.ASSISTANT, "Things working well together will increase revenue."),
                Message.of(Message.Side.USER, "Let's circle back when we have more bandwidth to touch base on opportunities for increased leverage."),
                Message.of(Message.Side.ASSISTANT, "Let's talk later when we're less busy about how to do better."),
                Message.of(Message.Side.USER, "This late pivot means we don't have time to boil the ocean for the client deliverable.")
        );
        assertEquals(expectedTokenCount, TokenCount.fromMessages(messages, tokenizer, ChatFormatDescriptor.forModel(modelName)));
    }

    @Test
    void fromMessages_gives_expected_token_count_when_used_with_functions() throws JsonProcessingException {
        final int EXPECTED_TOKEN_COUNT = 232;

        var functionArgs = "{\n  \"source_code\": \"import java.time.LocalDate;\\n\\npublic class Main {\\n  public static void main(String[] args) {\\n    LocalDate currentDate = LocalDate.now();\\n    System.out.println(currentDate);\\n  }\\n}\"\n}";
        var jsonNode = new ObjectMapper().readTree(functionArgs);
        var messages = List.<Message<String>>of(
                Message.of(Message.Side.SYSTEM, "You are a helpful assistant. Follow user instructions carefully."),
                Message.of(Message.Side.USER, "Please use Java to check current date."),
                Message.of(Message.Side.ASSISTANT, null, null, FunctionCall.of("java", jsonNode.toString())),
                Message.of(Message.Side.FUNCTION, "TODAY", "java")
        );
        var functions = List.of(
                FunctionTool.of("java", "Evaluate Java code.", JavaFunction.class),
                FunctionTool.of("sql", "Evaluate SQL code.", SqlFunction.class)
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
