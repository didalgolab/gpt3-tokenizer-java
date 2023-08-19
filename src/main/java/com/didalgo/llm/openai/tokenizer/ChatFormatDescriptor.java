/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.llm.openai.tokenizer;

import java.util.Objects;

/**
 * Describes the various chat messaging formats for the purpose of counting tokens
 * in chat conversations against different models.
 *
 * @author Mariusz Bernacki
 */
public interface ChatFormatDescriptor {

    Encoding encoding();

    int extraTokenCountPerMessage();

    int extraTokenCountPerRequest();

    int extraTokenCountForFunctions();

    int extraTokenCountPerFunctionCall();

    static ChatFormatDescriptor forModel(String modelName) {
        return switch (modelName) {
            case "gpt-3.5-turbo" -> forModel("gpt-3.5-turbo-0613");
            case "gpt-3.5-turbo-16k", "gpt-4", "gpt-4-32k" -> forModel("gpt-4-0613");
            case "gpt-3.5-turbo-0301" -> new Of(Encoding.forModel(modelName), 4, 3, Of.UNSUPPORTED, 3);
            case "gpt-4-0314", "gpt-4-32k-0314" -> new Of(Encoding.forModel(modelName), 3, 3, Of.UNSUPPORTED, 3);
            case "gpt-3.5-turbo-0613", "gpt-3.5-turbo-16k-0613",
                "gpt-4-0613", "gpt-4-32k-0613" -> new Of(Encoding.forModel(modelName), 3, 3, -1, 3);
            default -> throw new IllegalArgumentException(String.format("Model `%s` not found", modelName));
        };
    }

    record Of (Encoding encoding, int extraTokenCountPerMessage, int extraTokenCountPerRequest, int extraTokenCountForFunctions, int extraTokenCountPerFunctionCall) implements ChatFormatDescriptor {
        /** The special constant indicating that functions are not supported by the model descriptor. */
        private static final int UNSUPPORTED = Integer.MIN_VALUE;

        public Of {
            Objects.requireNonNull(encoding, "encoding");
        }

        @Override
        public int extraTokenCountForFunctions() {
            if (extraTokenCountForFunctions == UNSUPPORTED)
                throw new UnsupportedOperationException("Functions aren't supported by this model");

            return extraTokenCountForFunctions;
        }
    }
}
