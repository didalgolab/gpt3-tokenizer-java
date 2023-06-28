/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.gpt3.functions;

import javax.json.JsonObject;

public class OpenAIFunction implements OpenAITool {

    private final String name;
    private final String description;
    private final JsonObject parameters;

    public OpenAIFunction(String name, String description, JsonObject parameters) {
        this.name = (name == null)? "" : name;
        this.description = (description == null)? "" : description.strip();
        this.parameters = parameters;
    }

    @Override
    public String getToolCategory() {
        return "functions";
    }

    @Override
    public String getToolNamespace() {
        return "functions";
    }

    public final String name() {
        return name;
    }

    public final String description() {
        return description;
    }

    public final JsonObject parameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return OpenAIToolSupport.getDefault().getFunctionFormatter(this).format(this, parameters());
    }

    interface Formatter {
        String format(OpenAIFunction function, JsonObject jsonSchema);
    }
}
