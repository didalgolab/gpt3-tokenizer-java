package com.didalgo.gpt3;

import java.nio.charset.Charset;
import java.util.Arrays;

public class ByteSequence {
    private final byte[] bytes;

    public ByteSequence(byte[] bytes) {
        this.bytes = bytes;
    }

    public int length() {
        return bytes.length;
    }

    public ByteSequence subSequence(int start, int end) {
        return new ByteSequence(Arrays.copyOfRange(bytes, start, end));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ByteSequence seq)
            return Arrays.equals(seq.bytes, this.bytes);
        return false;
    }

    public byte[] toByteArray() {
        return bytes;
    }

    public String toString(Charset charset) {
        return new String(toByteArray(), charset);
    }
}
