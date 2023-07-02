package hu.garaba.buffer;

public interface BufferReader {
    int BUFFER_SIZE = 2048*4;
    int MAX_TAIL_LENGTH = 1024;

    long maxBufferNo();
    String getBuffer(long n);
}
