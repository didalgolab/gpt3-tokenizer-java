package com.didalgo.gpt3;

import org.junit.jupiter.api.Test;

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
}
