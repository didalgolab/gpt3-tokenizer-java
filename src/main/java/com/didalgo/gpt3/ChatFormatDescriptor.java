package com.didalgo.gpt3;

import java.util.Objects;

public interface ChatFormatDescriptor {

    Encoding encoding();

    int extraTokenCountPerMessage();

    int extraTokenCountPerRequest();

    static ChatFormatDescriptor forModel(String modelName) {
        return switch (modelName) {
            case "gpt-3.5-turbo" -> forModel("gpt-3.5-turbo-0301");
            case "gpt-4" -> forModel("gpt-4-0314");
            case "gpt-3.5-turbo-0301" -> new Of(Encoding.forModel(modelName), 4, 3);
            case "gpt-4-0314" -> new Of(Encoding.forModel(modelName), 3, 3);
            default -> throw new IllegalArgumentException(String.format("Model `%s` not found", modelName));
        };
    }

    record Of (Encoding encoding, int extraTokenCountPerMessage, int extraTokenCountPerRequest) implements ChatFormatDescriptor {
        public Of {
            Objects.requireNonNull(encoding, "encoding");
        }
    }
}