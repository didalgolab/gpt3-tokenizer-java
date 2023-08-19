/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.llm.openai.tokenizer;

import com.didalgo.llm.FunctionTool;
import com.didalgo.llm.Tokenizer;
import com.didalgo.llm.Tool;
import com.didalgo.llm.chat.Message;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static java.util.stream.Collectors.groupingBy;
import static javax.json.JsonValue.EMPTY_JSON_ARRAY;
import static javax.json.JsonValue.EMPTY_JSON_OBJECT;
import static javax.json.JsonValue.ValueType.STRING;

/**
 * Supports the pluggable token counting logic.
 */
public class TokenCountSupport {

    private static final FunctionDocumenter standardDocumenter = new StandardFunctionDocumenter();

    public int countTokensFromString(String text, Tokenizer tokenizer) {
        return tokenizer.encode(text).size();
    }

    public int countTokensFromMessages(
            List<? extends Message<? extends CharSequence>> messages,
            List<? extends Tool> tools,
            Tokenizer tokenizer,
            ChatFormatDescriptor chatFormat)
    {
        var toolsPrompt = "";
        if (!tools.isEmpty()) {
            toolsPrompt = generateDocumentation(tools);
        }

        int tokenCount = 0;
        for (int index = 0; index < messages.size(); index++) {
            var message = messages.get(index);
            tokenCount += chatFormat.extraTokenCountPerMessage();

            var role = message.role().toString();
            if (role != null && !role.isEmpty())
                tokenCount += tokenizer.encode(role).size();

            var content = message.isFunctionCall()? null : message.content();
            if (content != null && index == 0 && "system".equals(role)) {
                content += "\n\n" + toolsPrompt;
                toolsPrompt = "";
            }
            if (content != null)
                tokenCount += tokenizer.encode(content).size();

            var functionCall = message.functionCall();
            if (functionCall.isPresent()) {
                tokenCount += tokenizer.encode(functionCall.name()).size();
                tokenCount += tokenizer.encode(functionCall.arguments()).size();
                tokenCount += chatFormat.extraTokenCountPerFunctionCall();
            }
        }
        tokenCount += chatFormat.extraTokenCountPerRequest(); // Every reply is primed with <im_start>assistant\n

        if (!tools.isEmpty()) {
            if (!toolsPrompt.isEmpty()) {
                tokenCount += chatFormat.extraTokenCountPerMessage();
                tokenCount += tokenizer.encode("system").size();
                tokenCount += tokenizer.encode(toolsPrompt).size();
            }
            tokenCount += chatFormat.extraTokenCountForFunctions();
        }

        return tokenCount;
    }

    public static TokenCountSupport getSupport() {
        return LazyHolder.INSTANCE;
    }

    private static final class LazyHolder {
        private static final TokenCountSupport INSTANCE = ServiceLoader.load(TokenCountSupport.class)
                .findFirst().orElseGet(TokenCountSupport::new);
    }

    public String generateDocumentation(List<? extends Tool> tools) {
        StringBuilder sb = new StringBuilder();

        sb.append("# Tools\n\n");

        Map<String, List<Tool>> toolsByCategory = tools.stream()
                .collect(groupingBy(Tool::toolCategory));

        for (Map.Entry<String, List<Tool>> categoryEntry : toolsByCategory.entrySet()) {
            sb.append("## ").append(categoryEntry.getKey()).append("\n\n");

            Map<String, List<Tool>> toolsByNamespace = categoryEntry.getValue().stream()
                    .collect(groupingBy(Tool::toolNamespace));

            for (Map.Entry<String, List<Tool>> namespaceEntry : toolsByNamespace.entrySet()) {
                sb.append("namespace ").append(namespaceEntry.getKey()).append(" {\n\n");
                for (Tool tool : namespaceEntry.getValue()) {
                    if (tool instanceof FunctionTool functionTool) {
                        sb.append(getFunctionDocumenter(functionTool).generateDocumentation(functionTool)).append("\n\n");
                    }
                }
                sb.append("} // namespace ").append(namespaceEntry.getKey()).append("\n\n");
            }
        }

        return sb.toString().stripTrailing();
    }


    public interface FunctionDocumenter {
        CharSequence generateDocumentation(FunctionTool function);
    }

    public FunctionDocumenter getFunctionDocumenter(Tool function) {
        return standardDocumenter;
    }

    private static class StandardFunctionDocumenter implements FunctionDocumenter {

        @Override
        public String generateDocumentation(FunctionTool function) {
            JsonObject params = function.parameters();
            StringBuilder buf = new StringBuilder();
            if (!function.description().isEmpty())
                putDescription(buf, function.description());

            putName(buf, function.name());
            putParameters(buf, params, "");
            putEnd(buf);

            return buf.toString();
        }

        private static void putDescription(StringBuilder buf, JsonObject schema) {
            var description = schema.getString("description", "").strip();
            putDescription(buf, description);
        }

        private static void putDescription(StringBuilder buf, String description) {
            if (!description.isEmpty())
                description.lines().forEach(line -> buf.append("// ").append(line).append('\n'));
        }

        private static void putName(StringBuilder buf, String name) {
            buf.append("type ")
                    .append(name)
                    .append(" = (_: ");
        }

        private static void putParameters(StringBuilder buf, Map<String, JsonValue> schema, String indent) {
            var properties = schema.getOrDefault("properties", EMPTY_JSON_OBJECT).asJsonObject();
            var required = schema.getOrDefault("required", EMPTY_JSON_ARRAY).asJsonArray();
            var definitions = schema.getOrDefault("definitions", EMPTY_JSON_OBJECT).asJsonObject();
            putProperties( buf,
                    properties,
                    required.getValuesAs(JsonString::getString),
                    definitions,
                    indent);
        }

        private static void putProperties(StringBuilder buf, JsonObject schema, List<String> required,  Map<String, JsonValue> definitions, String indent) {
            buf.append("{\n");
            schema.forEach((name, value) -> {
                var valueDesc = value.asJsonObject();
                if (indent.isEmpty())
                    putDescription(buf, valueDesc);

                buf.append(indent);
                buf.append(name);
                if (!isNested(indent) && !required.contains(name))
                    buf.append('?');

                buf.append(": ");
                putParameterType(buf, valueDesc, indent);
                buf.append(",\n");
            });
            buf.append("}");
        }

        private static void putParameterType(StringBuilder buf, JsonObject valueDesc, String indent) {
            var typeDesc = valueDesc.get("type");
            if (typeDesc == null || typeDesc.getValueType() != STRING) {
                buf.append("any");
                return;
            }

            if (valueDesc.containsKey("enum")) {
                buf.append(String.join(" | ", valueDesc.getJsonArray("enum").getValuesAs(JsonValue::toString)));
                return;
            }

            if (valueDesc.get("items") instanceof JsonObject arrayDesc && arrayDesc.containsKey("type")) {
                putParameterType(buf, arrayDesc, indent);
                buf.append("[]");
                return;
            }

            var typeName = valueDesc.getString("type", "any");
            switch (typeName) {
                case "integer", "number" -> buf.append("number");
                case "boolean", "string" -> buf.append(typeName);
                case "object" -> putParameters(buf, valueDesc, "  ");
                default -> buf.append("any");
            }
        }

        private static void putEnd(StringBuilder buf) {
            buf.append(") => any;");
        }

        private static boolean isNested(String indent) {
            return !indent.isEmpty();
        }
    }
}
