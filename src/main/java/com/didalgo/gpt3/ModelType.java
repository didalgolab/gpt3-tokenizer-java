/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.gpt3;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ModelType represents a list of available OpenAI GPT models, also providing information about
 * their maximum token size and encoding types.
 *
 * @author Mariusz Bernacki
 */
public enum ModelType {
	// chat
	GPT_4_O("gpt-4o", EncodingType.O200K_BASE, 128000, CompletionType.CHAT),
	GPT_4_TURBO("gpt-4-turbo-preview", EncodingType.CL100K_BASE, 128000, CompletionType.CHAT),
	GPT_4("gpt-4", EncodingType.CL100K_BASE, 8192, CompletionType.CHAT),
	GPT_4_32K("gpt-4-32k", EncodingType.CL100K_BASE, 32768, CompletionType.CHAT),
	GPT_3_5_TURBO("gpt-3.5-turbo", EncodingType.CL100K_BASE, 16384, CompletionType.CHAT),
	GPT_3_5_TURBO_LEGACY("gpt-3.5-turbo", EncodingType.CL100K_BASE, 4096, CompletionType.CHAT),
	GPT_3_5_TURBO_16K("gpt-3.5-turbo-16k", EncodingType.CL100K_BASE, 16384, CompletionType.CHAT),

	// text
	GPT_3_5_TURBO_INSTRUCT("gpt-3.5-turbo-instruct", EncodingType.CL100K_BASE, 4097, CompletionType.TEXT),
	TEXT_DAVINCI_003("text-davinci-003", EncodingType.P50K_BASE, 4097, CompletionType.TEXT),
	TEXT_DAVINCI_002("text-davinci-002", EncodingType.P50K_BASE, 4097, CompletionType.TEXT),
	TEXT_DAVINCI_001("text-davinci-001", EncodingType.R50K_BASE, 2049, CompletionType.TEXT),
	TEXT_CURIE_001("text-curie-001", EncodingType.R50K_BASE, 2049, CompletionType.TEXT),
	TEXT_BABBAGE_001("text-babbage-001", EncodingType.R50K_BASE, 2049, CompletionType.TEXT),
	TEXT_ADA_001("text-ada-001", EncodingType.R50K_BASE, 2049, CompletionType.TEXT),
	DAVINCI("davinci", EncodingType.R50K_BASE, 2049, CompletionType.TEXT),
	CURIE("curie", EncodingType.R50K_BASE, 2049, CompletionType.TEXT),
	BABBAGE("babbage", EncodingType.R50K_BASE, 2049, CompletionType.TEXT),
	ADA("ada", EncodingType.R50K_BASE, 2049, CompletionType.TEXT),

	// code
	CODE_DAVINCI_002("code-davinci-002", EncodingType.P50K_BASE, 8001, CompletionType.TEXT),

	// edit
	TEXT_DAVINCI_EDIT_001("text-davinci-edit-001", EncodingType.P50K_EDIT, 2049, CompletionType.TEXT),
	CODE_DAVINCI_EDIT_001("code-davinci-edit-001", EncodingType.P50K_EDIT, 2049, CompletionType.TEXT),

	// embeddings
	TEXT_EMBEDDING_ADA_002("text-embedding-ada-002", EncodingType.CL100K_BASE, 8192, CompletionType.TEXT);


	private final String modelName;
	private final EncodingType encodingType;
	private final int maxTokens;
	private final CompletionType completionType;

	ModelType(String modelName, EncodingType encodingType, int maxTokens, CompletionType completionType) {
		this.modelName = modelName;
		this.encodingType = encodingType;
		this.maxTokens = maxTokens;
		this.completionType = completionType;
	}

	public String modelName() {
		return modelName;
	}

	public EncodingType encodingType() {
		return encodingType;
	}

	public int maxTokens() {
		return maxTokens;
	}

	public CompletionType completionType() {
		return completionType;
	}

	/**
	 * Returns a {@link ModelType} for the given modelName, or throw exception if no
	 * such model type exists.
	 *
	 * @param modelName the modelName of the model type
	 * @return the model type
	 * @throws IllegalArgumentException if the model with the given name doesn't exist
	 */
	public static Optional<ModelType> forModel(String modelName) throws IllegalArgumentException {
		Optional<ModelType> modelType = forModelExact(modelName);
		if (modelType.isPresent()) {
			return modelType;
		}

		// Truncate model version information
		boolean shortMatch;
		if ((shortMatch = modelName.matches(".*-\\d{4}$")) || modelName.matches(".*-\\d{4}-\\d{2}-\\d{2}$")) {
			modelName = shortMatch ? modelName.substring(0, modelName.length() - 5)
					: modelName.substring(0, modelName.length() - 11);

			modelType = forModelExact(modelName);
			if (modelType.isPresent()) {
				return modelType;
			}
		}
		throw new IllegalArgumentException("Model `" + modelName + "` not found");
	}

	private static Optional<ModelType> forModelExact(String modelName) {
		if (specialVariants.containsKey(modelName)) {
			return Optional.of(specialVariants.get(modelName));
		}

		for (final ModelType modelType : values()) {
			if (modelType.modelName().equals(modelName)) {
				return Optional.of(modelType);
			}
		}
		return Optional.empty();
	}

	private static final class Cache {

		private static final Map<ModelType, SoftReference<GPT3Tokenizer>> gptTokenizersCache = Collections.synchronizedMap(new EnumMap<>(ModelType.class));

		private static GPT3Tokenizer getTokenizer(ModelType model) {
			GPT3Tokenizer tokenizer;
			SoftReference<GPT3Tokenizer> ref = Cache.gptTokenizersCache.get(model);
			if (ref == null || (tokenizer = ref.get()) == null) {
				synchronized (gptTokenizersCache) {
					Cache.gptTokenizersCache.put(model, new SoftReference<>(tokenizer = new GPT3Tokenizer(model.getEncoding())));
				}
			}

			return tokenizer;
		}
	}

	public Encoding getEncoding() {
		return Encoding.forName(encodingType().encodingName());
	}

	public GPT3Tokenizer getTokenizer() {
		return Cache.getTokenizer(this);
	}

	/**
	 * Returns the {@code ChatFormatDescriptor} for this model, which can be used together with
	 * {@link TokenCount} to count prompt tokens in conversation messages.
	 * <p>
	 * <b>Please NOTE</b> that this <i>model bag</i> doesn't distinguish between model variants
	 * (e.g. -0314, -0613, etc.), thus for models gpt-3.5-*-0301 or older the returned descriptor
	 * may be imprecise. If you need precise descriptor for old gpt-3.5-turbo model please use
	 * {@link ChatFormatDescriptor#forModel(String)} method instead.
	 *
	 * @return the {@code ChatFormatDescriptor}
	 */
	public ChatFormatDescriptor getChatFormatDescriptor() {
		return ChatFormatDescriptor.forModel(modelName());
	}

	private static Map<String, ModelType> specialVariants = new HashMap<>();
	static {
		specialVariants.put("gpt-3.5-turbo-0301", GPT_3_5_TURBO_LEGACY);
		specialVariants.put("gpt-3.5-turbo-0613", GPT_3_5_TURBO_LEGACY);

		specialVariants.put("gpt-4-turbo-preview", GPT_4_TURBO);
		specialVariants.put("gpt-4-1106-preview", GPT_4_TURBO);
		specialVariants.put("gpt-4-0125-preview", GPT_4_TURBO);
	}
}
