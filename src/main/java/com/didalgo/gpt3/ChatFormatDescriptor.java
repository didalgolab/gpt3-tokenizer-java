/*
 * Copyright (c) 2023 Mariusz Bernacki <info@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.gpt3;

import java.util.Objects;

/**
 * Describes the various chat messaging formats for the purpose of counting tokens in chat conversations against different models.
 *
 * @author Mariusz Bernacki
 */
public interface ChatFormatDescriptor {

    Encoding encoding();

    int extraTokenCountPerMessage();

    int extraTokenCountPerRequest();

    static ChatFormatDescriptor forModel(String modelName) {
        return switch (modelName) {
            case "gpt-3.5-turbo", "gpt-3.5-turbo-16k" -> forModel("gpt-3.5-turbo-0301");
            case "gpt-4", "gpt-4-32k" -> forModel("gpt-4-0314");
            case "gpt-3.5-turbo-0301", "gpt-3.5-turbo-0613",
                    "gpt-3.5-turbo-16k-0613" -> new Of(Encoding.forModel(modelName), 4, 3);
            case "gpt-4-0314", "gpt-4-0613",
                    "gpt-4-32k-0314", "gpt-4-32k-0613" -> new Of(Encoding.forModel(modelName), 3, 3);
            default -> throw new IllegalArgumentException(String.format("Model `%s` not found", modelName));
        };
    }

    record Of (Encoding encoding, int extraTokenCountPerMessage, int extraTokenCountPerRequest) implements ChatFormatDescriptor {
        public Of {
            Objects.requireNonNull(encoding, "encoding");
        }
    }
}
