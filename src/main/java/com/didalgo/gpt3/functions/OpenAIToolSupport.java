/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.gpt3.functions;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static java.util.stream.Collectors.groupingBy;
import static javax.json.JsonValue.ValueType.STRING;

public class OpenAIToolSupport {

    private static OpenAIFunction.Formatter standardFormatter = new StandardFunctionFormatter();

    public static OpenAIToolSupport getDefault() {
        return LazyHolder.INSTANCE;
    }

    public OpenAIFunction.Formatter getFunctionFormatter(OpenAIFunction function) {
        return standardFormatter;
    }

    public String generateDocumentation(List<OpenAITool> tools) {
        StringBuilder sb = new StringBuilder();

        sb.append("# Tools\n\n");

        Map<String, List<OpenAITool>> toolsByCategory = tools.stream()
                .collect(groupingBy(OpenAITool::getToolCategory));

        for (Map.Entry<String, List<OpenAITool>> categoryEntry : toolsByCategory.entrySet()) {
            sb.append("## ").append(categoryEntry.getKey()).append("\n\n");

            Map<String, List<OpenAITool>> toolsByNamespace = categoryEntry.getValue().stream()
                    .collect(groupingBy(OpenAITool::getToolNamespace));

            for (Map.Entry<String, List<OpenAITool>> namespaceEntry : toolsByNamespace.entrySet()) {
                sb.append("namespace ").append(namespaceEntry.getKey()).append(" {\n\n");
                for (OpenAITool tool : namespaceEntry.getValue()) {
                    sb.append(tool.toString()).append("\n\n");
                }
                sb.append("} // namespace ").append(namespaceEntry.getKey()).append("\n\n");
            }
        }

        return sb.toString().stripTrailing();
    }

    private static final class LazyHolder {
        private static final OpenAIToolSupport INSTANCE = ServiceLoader.load(OpenAIToolSupport.class)
                .findFirst().orElseGet(OpenAIToolSupport::new);
    }

    private static class StandardFunctionFormatter implements OpenAIFunction.Formatter {

        @Override
        public String format(OpenAIFunction function, JsonObject schema) {
            StringBuilder buf = new StringBuilder();
            if (!function.description().isEmpty())
                putDescription(buf, function.description());

            putName(buf, function.name());
            putParameters(buf, schema, "");
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

        private static void putParameters(StringBuilder buf, JsonObject schema, String indent) {
            var properties = schema.getJsonObject("properties");
            var required = schema.getJsonArray("required");
            putProperties( buf,
                    properties,
                    (required == null) ? List.of() : required.getValuesAs(JsonString::getString),
                    indent);
        }

        private static void putProperties(StringBuilder buf, JsonObject schema, List<String> required, String indent) {
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
    }

    private static boolean isNested(String indent) {
        return !indent.isEmpty();
    }
}
