# GPT3/4 Java Tokenizer

[![License: MIT](https://img.shields.io/github/license/didalgo2/gpt3-tokenizer-java?style=flat-square)](https://opensource.org/license/mit/)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/didalgo2/gpt3-tokenizer-java/gradle.yml?style=flat-square)
[![Maven Central](https://img.shields.io/maven-central/v/com.didalgo/gpt3-tokenizer?style=flat-square)](https://central.sonatype.com/artifact/com.didalgo/gpt3-tokenizer/0.1.5)

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
var messages = List.of(
        new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a helpful assistant."),
        new ChatMessage(ChatMessageRole.USER.value(), "Hello there!")
);
var model = ModelType.GPT_3_5_TURBO;
var count = TokenCount.fromMessages(messages, model);
System.out.println("Prompt tokens: " + count);
```

### Did you know...

1. ...that all 3.5-turbo models released after _0613_ now have tokenization counts for messages consistent with gpt-4 models?

1. ...that OpenAI Tokenizer available at https://platform.openai.com/tokenizer uses p50k_base encoding, thus it doesn't count correctly tokens for gpt-3.5 and gpt-4 models? If you look for decent alternative, you may like: https://tiktokenizer.vercel.app/, but keep in mind that tokenization for messages of gpt-3.5 models released after 0613 was changed (see point above).

1. ...that in cl100k_base encoding every sequence of up to 81 spaces is just a single token? So next time when someone tells you that passing YAML to ChatGPT is not efficient, you can argue that...
```java
var tokenizer = ModelType.GPT_3_5_TURBO.getTokenizer();
var tokens = (List<Integer>) null;
for (var sb = new StringBuilder(" "); (tokens = tokenizer.encode(sb)).size() == 1; sb.append(' '))
    System.out.printf("`%s`'s token is %s, and that's %d space(s)!\n".replace("(s)", sb.length()==1?"":"s"), sb, tokens, sb.length());

```
```
`                                                                           `'s token is [14984], and that's 75 spaces!
`                                                                            `'s token is [56899], and that's 76 spaces!
`                                                                             `'s token is [59691], and that's 77 spaces!
`                                                                              `'s token is [82321], and that's 78 spaces!
`                                                                               `'s token is [40584], and that's 79 spaces!
`                                                                                `'s token is [98517], and that's 80 spaces!
`                                                                                 `'s token is [96529], and that's 81 spaces!
```

## License

This project is licensed under the MIT License.