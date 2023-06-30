/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.gpt3;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.io.StringReader;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static javax.json.JsonValue.EMPTY_JSON_ARRAY;
import static javax.json.JsonValue.EMPTY_JSON_OBJECT;
import static javax.json.JsonValue.ValueType.STRING;

/**
 * Supports the pluggable token counting logic.
 */
public class TokenCountSupport {

    private static final FunctionDocumenter standardDocumenter = new StandardFunctionDocumenter();

    public int countTokensFromString(String text, GPT3Tokenizer tokenizer) {
        return tokenizer.encode(text).size();
    }

    public <T_MSG, T_TOOL> int countTokensFromMessages(
            List<T_MSG> messages,
            Function<T_MSG, ? extends TokenizableMessage> messageCoercer,
            List<T_TOOL> tools,
            Function<T_TOOL, ? extends TokenizableTool> toolCoercer,
            GPT3Tokenizer tokenizer,
            ChatFormatDescriptor chatFormat)
    {
        var toolsPrompt = "";
        if (!tools.isEmpty()) {
            var tokenizable = tools.stream()
                    .map(toolCoercer)
                    .toList();
            toolsPrompt = generateDocumentation(tokenizable);
        }

        int tokenCount = 0;
        for (int index = 0; index < messages.size(); index++) {
            var tokenizable = messageCoercer.apply(messages.get(index));
            tokenCount += chatFormat.extraTokenCountPerMessage();

            var role = tokenizable.role();
            if (role != null && !role.isEmpty())
                tokenCount += tokenizer.encode(role).size();

            var content = tokenizable.content();
            if (content != null && role != null && index == 0 && "system".equals(role.toString())) {
                content += "\n\n" + toolsPrompt;
                toolsPrompt = "";
            }
            if (content != null)
                tokenCount += tokenizer.encode(content).size();

            var functionCall = tokenizable.functionCall();
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

    public JsonObject generateJsonSchema(Class<?> valueType) {
        JsonNode schemaNode = JsonSchemaUtils.generateSchema(valueType);
        return Json.createReader(new StringReader(schemaNode.toString())).readObject();
    }

    public static TokenCountSupport getSupport() {
        return LazyHolder.INSTANCE;
    }

    private static final class LazyHolder {
        private static final TokenCountSupport INSTANCE = ServiceLoader.load(TokenCountSupport.class)
                .findFirst().orElseGet(TokenCountSupport::new);
    }

    private static final class JsonSchemaUtils {
        private static final Comparator<MemberScope<?,?>> DECLARATION_ORDER = (__, ___) -> 0;
        private static final ObjectMapper mapper = new ObjectMapper();
        private static final SchemaGenerator generator;
        static {
            SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(mapper, SchemaVersion.DRAFT_2019_09, OptionPreset.PLAIN_JSON)
                    .with(new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_REQUIRED));
            configBuilder.forTypesInGeneral().withPropertySorter(DECLARATION_ORDER);
            generator = new SchemaGenerator(configBuilder.build());
        }

        public static JsonNode generateSchema(Class<?> valueType) {
            return generator.generateSchema(valueType);
        }
    }

    public String generateDocumentation(List<? extends TokenizableTool> tools) {
        StringBuilder sb = new StringBuilder();

        sb.append("# Tools\n\n");

        Map<String, List<TokenizableTool>> toolsByCategory = tools.stream()
                .collect(groupingBy(TokenizableTool::toolCategory));

        for (Map.Entry<String, List<TokenizableTool>> categoryEntry : toolsByCategory.entrySet()) {
            sb.append("## ").append(categoryEntry.getKey()).append("\n\n");

            Map<String, List<TokenizableTool>> toolsByNamespace = categoryEntry.getValue().stream()
                    .collect(groupingBy(TokenizableTool::toolNamespace));

            for (Map.Entry<String, List<TokenizableTool>> namespaceEntry : toolsByNamespace.entrySet()) {
                sb.append("namespace ").append(namespaceEntry.getKey()).append(" {\n\n");
                for (TokenizableTool tool : namespaceEntry.getValue()) {
                    sb.append(tool.generateDocumentation()).append("\n\n");
                }
                sb.append("} // namespace ").append(namespaceEntry.getKey()).append("\n\n");
            }
        }

        return sb.toString().stripTrailing();
    }


    public interface FunctionDocumenter {
        CharSequence generateDocumentation(TokenizableFunction function);
    }

    public FunctionDocumenter getFunctionDocumenter(TokenizableFunction function) {
        return standardDocumenter;
    }

    private static class StandardFunctionDocumenter implements FunctionDocumenter {

        @Override
        public String generateDocumentation(TokenizableFunction function) {
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
