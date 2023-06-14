package hu.garaba.buffer;

import hu.garaba.textprocessor.TextProcessor;

import java.io.*;
import java.lang.ref.SoftReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * A BufferReader is a thread-safe class that can load the text content of UTF-8 encoded txt files in blocks, where each block
 * is made from a whole number of German sentences (so that subsequent language processing should not care about partial sentences).
 */
public class BufferReader {
    private static final int BUFFER_SIZE = 2048*4;
    private static final int MAX_TAIL_LENGTH = 1024;

    private final long maxBufferNo;
    private final Map<Long, SoftReference<String>> bufferTextMap = new ConcurrentHashMap<>();

    private final Path filePath;
    // Meaningless optimization, wanted to replace with MappedByteBuffer's slices but their thread-safety guarantees are unspecified. Might be replaced with MemorySegment-based API
    private final BlockingDeque<RandomAccessFile> fileQueue = new LinkedBlockingDeque<>(4);

    private BufferReader(Path path) throws IOException {
        this.filePath = path;
        var file = new RandomAccessFile(path.toFile(), "r");
        fileQueue.add(file);
        this.maxBufferNo = file.length() / BUFFER_SIZE + (file.length() % BUFFER_SIZE == 0 ? 0 : 1);
    }

    public long maxBufferNo() {
        return maxBufferNo;
    }

    /**
     * A blocking call that will start loading the {@code n}th block if it hasn't been started yet.
     * @param n The index of the block of the file that should be loaded to memory
     * @return A String that is contained in the nth block of the opened file started from the 3rd *recognized* German sentence,
     * possibly ending past this buffer's content into the n+1 th block.
     */
    public String getBuffer(long n) {
        SoftReference<String> softRef;

        do {
            softRef = bufferTextMap.get(n);
        } while (softRef != null && softRef.get() == null);

        if (softRef != null) {
            return softRef.get();
        }

        try {
            if (fileQueue.size() == 0) {
                fileQueue.offer(new RandomAccessFile(filePath.toFile(), "r"));
            }

            return readNthBuffer(n);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readNthBuffer(long n) throws IOException {
        if (n >= maxBufferNo || n < 0) {
            throw new IllegalArgumentException("Can't read buffer no %d. n should be >= 0 and < %d".formatted(n, maxBufferNo));
        }

        bufferTextMap.put(n, new SoftReference<>(null));
        System.out.println("Started loading block " + n);

        RandomAccessFile file;
        try {
            file = fileQueue.takeFirst();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        file.seek(BUFFER_SIZE*n);

        final byte[] buffer = new byte[BUFFER_SIZE];
        int readChars = file.read(buffer);

        BufferedInputStream stream = new BufferedInputStream(new ByteArrayInputStream(buffer, 0, readChars));
        if (n != 0) {
            findTailSentence(stream, false);
        }

        var textStream = new ByteArrayOutputStream(BUFFER_SIZE + MAX_TAIL_LENGTH);
        textStream.write(stream.readAllBytes());

        ByteArrayOutputStream tailSentence = findTailSentence(new BufferedInputStream(new FileInputStream(file.getFD())), true);
        textStream.write(tailSentence.toByteArray());

        String bufferText = textStream.toString(StandardCharsets.UTF_8);
        bufferTextMap.put(n, new SoftReference<>(bufferText));

        fileQueue.add(file);

        return bufferText;
    }

    /**
     * Consumes part of {@code stream}, which will include any starting partial UTF-8 multibyte character, and optionally
     * a whole German sentence recognizable by the used sentence detection model. If {@code output} is true, it will
     * return an InputStream which can be used to read the consumed part.
     */
    private static ByteArrayOutputStream findTailSentence(BufferedInputStream stream, boolean output) throws IOException {
        int c;
        int i = 0;
        byte[] unicodePostfix = new byte[4];
        do {
            stream.mark(4);
            c = stream.read();

            unicodePostfix[i] = (byte) c;
            i++;
        } while (i < 4
                && c >= 128
                && (c & 0xE0) != 0xC0
                && (c & 0xF0) != 0xE0
                && (c & 0xF8) != 0xF0);
        stream.reset();

        unicodePostfix = Arrays.copyOf(unicodePostfix, i-1);

        ByteArrayOutputStream outputStream = output ? new ByteArrayOutputStream(MAX_TAIL_LENGTH+4) : null;
        if (output)
            outputStream.write(unicodePostfix);

        byte[] sentenceBuffer = new byte[MAX_TAIL_LENGTH];
        stream.mark(MAX_TAIL_LENGTH);

        int readChars = stream.read(sentenceBuffer);
        if (readChars != -1) {
            String sentencesText = new String(sentenceBuffer, 0, readChars, StandardCharsets.UTF_8);
            List<String> sentences = TextProcessor.sentences(sentencesText);

            stream.reset();
            if (sentences.size() >= 3 && sentences.get(2) != null) { // We need at least 3 sentences to be sure that the 2nd is a full one.
                int sentenceIndex = sentencesText.indexOf(sentences.get(2));
                byte[] sentenceBytes = sentencesText.substring(0, sentenceIndex).getBytes(StandardCharsets.UTF_8);
                int length = sentenceBytes.length;
                stream.skip(length);

                if (output)
                    outputStream.write(sentenceBytes);
            }
        }

        return outputStream;
    }

    public static BufferReader fromFile(Path path) throws IOException {
        return new BufferReader(path);
    }
}
