/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.gpt3;

/**
 * An interface describing an object that provides a tool support for a language model.
 * <p>
 * Currently only tools supported by OpenAI models are <b>functions</b>.
 *
 * @author Mariusz Bernacki
 */
public interface TokenizableTool {

    String toolCategory();

    String toolNamespace();

    CharSequence generateDocumentation();
}
