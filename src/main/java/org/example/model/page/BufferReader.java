package org.example.model.page;

import org.example.textprocessor.TextProcessor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BufferReader {
    private static final int BUFFER_SIZE = 2048*4;
    private static final int MAX_TAIL_LENGTH = 1024;

    private final RandomAccessFile file;

    private final long maxBufferNo;
    private final Map<Long, String> bufferTextMap = new ConcurrentHashMap<>();

    private BufferReader(Path path) throws IOException {
        this.file = new RandomAccessFile(path.toFile(), "r");
        this.maxBufferNo = file.length() / BUFFER_SIZE + (file.length() % BUFFER_SIZE == 0 ? 0 : 1);
    }

    public long maxBufferNo() {
        return maxBufferNo;
    }

    public String getBuffer(long n) {
        if (bufferTextMap.containsKey(n)) {
            return bufferTextMap.get(n);
        }

        try {
            readNthBuffer(n);
            return bufferTextMap.get(n);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readNthBuffer(long n) throws IOException {
        if (n >= maxBufferNo || n < 0) {
            throw new IllegalArgumentException("Can't read buffer no %d. n should be >= 0 and < %d".formatted(n, maxBufferNo));
        }

        System.out.println("Attempting reading " + n);
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
        bufferTextMap.put(n, bufferText);
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
        return new BufferReader(path); // TODO
    }

    public static void main(String[] args) throws IOException {
        BufferReader bufferReader = BufferReader.fromFile(Path.of("/home/florian/Downloads/HP.txt"));

        /*try (ExecutorService executorService = Executors.newFixedThreadPool(4)) {
            for (int i = 0; i < bufferReader.maxBufferNo; i++) {
                int finalI = i;
                executorService.submit(() -> {
                    String buffer = bufferReader.getBuffer(finalI);
                    TextProcessor.process(buffer);
                    System.out.println("Finished " + finalI);
                });

                bufferReader.getBuffer(0);
                System.out.println("Got 0");
            }
        }*/
/*
        byte[] bytes = "\uD83C\uDCA0űŰasd".getBytes(StandardCharsets.UTF_8);
        BufferedInputStream stream = new BufferedInputStream(new ByteArrayInputStream(Arrays.copyOfRange(bytes, 2, bytes.length)));
        Pages.findTailSentence(stream, false);

        String s = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        System.out.println(s);

        if (true) {
            return;
        }
*/

        /*BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of("asdasd.txt"));

        bufferedWriter.write(pageReader.bufferText);
        for (int i = 1; i <= pageReader.maxBufferNo; i++) {
            pageReader.readNthBuffer(i);
            bufferedWriter.write(pageReader.bufferText);
        }*/
    }
}
