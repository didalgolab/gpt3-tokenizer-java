/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: MIT
 */
package com.didalgo.llm.openai.tokenizer;

/**
 * Represents various encoding types used by the OpenAI GPT models.
 * <p>
 * Each encoding type is associated with a unique name, accessible through the {@link #encodingName()} method.
 *
 */
public enum EncodingType {
	CL100K_BASE("cl100k_base"),
	R50K_BASE("r50k_base"),
	P50K_BASE("p50k_base"),
	P50K_EDIT("p50k_edit");

	private final String encodingName;

	EncodingType(String encodingName) {
		this.encodingName = encodingName;
	}

	public String encodingName() {
		return encodingName;
	}
}
