# GPT3/4 Java Tokenizer

[![License: MIT](https://img.shields.io/github/license/didalgo2/gpt3-tokenizer-java?style=flat-square)](https://opensource.org/license/mit/)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/didalgo2/gpt3-tokenizer-java/gradle.yml?style=flat-square)
[![Maven Central](https://img.shields.io/maven-central/v/com.didalgo/gpt3-tokenizer?style=flat-square)](https://central.sonatype.com/artifact/com.didalgo/gpt3-tokenizer/0.1.2)

This is a Java implementation of a GPT3/4 tokenizer, loosely ported from [Tiktoken](https://github.com/openai/tiktoken) with the help of [ChatGPT](https://openai.com/blog/chatgpt).

## Usage Examples

### Encoding Text to Tokens

```java
GPT3Tokenizer tokenizer = new GPT3Tokenizer(Encoding.CL100K_BASE);
List<Integer> tokens = tokenizer.encode("example text here");
```

### Decoding Tokens to Text

```java
GPT3Tokenizer tokenizer = new GPT3Tokenizer(Encoding.CL100K_BASE);
List<Integer> tokens = Arrays.asList(123, 456, 789);
String text = tokenizer.decode(tokens);
```

### Counting Number of Tokens in Chat Messages

```java
GPT3Tokenizer tokenizer = new GPT3Tokenizer(Encoding.CL100K_BASE);
int tokens = TokenCount.fromMessages(messages, tokenizer, ChatFormatDescriptor.forModel("gpt-3.5-turbo"));
```

## License

This project is licensed under the MIT License.