# GPT3/4 Java Tokenizer

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