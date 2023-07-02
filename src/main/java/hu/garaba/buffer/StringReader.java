package hu.garaba.buffer;

public class StringReader implements BufferReader {
    private final String text;

    public StringReader(String text) {
        this.text = text;
    }

    @Override
    public long maxBufferNo() {
        return 1;
    }

    @Override
    public String getBuffer(long n) {
        if (n != 0) {
            throw new IllegalArgumentException("StringReader only has a single buffer, got " + n);
        }

        return text;
    }
}
