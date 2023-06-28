/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.gpt3.functions;

public interface OpenAITool {

    String getToolCategory();

    String getToolNamespace();

    @Override
    String toString();
}
