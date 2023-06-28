/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.gpt3;

import java.util.function.Function;

public interface TokenizableMessage {

    CharSequence role();

    CharSequence name();

    CharSequence content();

    TokenizableFunctionCall functionCall();

    static <T> Function<T, TokenizableMessage> from(
            Function<T, ? extends CharSequence> roleAccessor,
            Function<T, ? extends CharSequence> nameAccessor,
            Function<T, ? extends CharSequence> contentAccessor,
            Function<T, ? extends TokenizableFunctionCall> functionCallMaker
    ) {
        return message -> new TokenizableMessage() {
            @Override public CharSequence role() { return roleAccessor.apply(message); }
            @Override public CharSequence name() { return nameAccessor.apply(message); }
            @Override public CharSequence content() { return contentAccessor.apply(message); }
            @Override public TokenizableFunctionCall functionCall() { return functionCallMaker.apply(message); }
        };
    }
}
