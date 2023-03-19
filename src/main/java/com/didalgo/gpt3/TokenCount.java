package com.didalgo.gpt3;

import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.List;
import java.util.ServiceLoader;

public class TokenCount {

    public static int fromString(String text, GPT3Tokenizer tokenizer) {
        return getCalculator().countTokensFromString(text, tokenizer);
    }

    public static int fromMessages(List<ChatMessage> messages, GPT3Tokenizer tokenizer, ChatFormatDescriptor chatFormat) {
        return getCalculator().countTokensFromMessages(messages, tokenizer, chatFormat);
    }

    public static Calculator getCalculator() {
        return CalculatorHolder.instance;
    }

    public interface Calculator {
        int countTokensFromString(String text, GPT3Tokenizer tokenizer);
        int countTokensFromMessages(List<ChatMessage> messages, GPT3Tokenizer tokenizer, ChatFormatDescriptor chatFormat);
    }

    public static class StandardCalculator implements Calculator {

        @Override
        public int countTokensFromString(String text, GPT3Tokenizer tokenizer) {
            return tokenizer.encode(text).size();
        }

        @Override
        public int countTokensFromMessages(List<ChatMessage> messages, GPT3Tokenizer tokenizer, ChatFormatDescriptor chatFormat) {
            int tokenCount = 0;
            for (ChatMessage message : messages) {
                tokenCount += chatFormat.extraTokenCountPerMessage();
                if (message.getRole() != null)
                    tokenCount += tokenizer.encode(message.getRole()).size();
                if (message.getContent() != null)
                    tokenCount += tokenizer.encode(message.getContent()).size();
            }
            tokenCount += chatFormat.extraTokenCountPerRequest(); // Every reply is primed with <im_start>assistant\n
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
