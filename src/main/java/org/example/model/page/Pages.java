package org.example.model.page;

import org.example.textprocessor.TextProcessor;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Pages {
//    private static final int BUFFER_SIZE = 4096;
    private static final int BUFFER_SIZE = 512;
    private static final int MAX_TAIL_LENGTH = 256;

    private final RandomAccessFile file;

    private final long maxBufferNo;
    private long bufferNo;
    private final byte[] buffer = new byte[BUFFER_SIZE];

    private String pageText;

    private Pages(Path path) throws IOException {
        this.file = new RandomAccessFile(path.toFile(), "r");
        this.maxBufferNo = file.length() / BUFFER_SIZE + (file.length() % BUFFER_SIZE == 0 ? 0 : 1);

        readNthBuffer(0);
        this.bufferNo = 0;
    }

    private void readNthBuffer(long n) throws IOException {
        System.out.println("Attempting reading " + n);
        file.seek(BUFFER_SIZE*n);

        if (n > maxBufferNo || n < 0) {
            throw new IllegalArgumentException("Can't read buffer no %d. n should be >= 0 and < %d".formatted(n, maxBufferNo));
        }
        this.bufferNo = n;

        file.read(buffer);
        this.pageText = new String(buffer, StandardCharsets.UTF_8);

        /*StringBuilder sb = new StringBuilder();

        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buffer), StandardCharsets.UTF_8));
        bufferReader.mark(BUFFER_SIZE);
        String formerTail = n == 0 ? null : findTail(bufferReader);
        long formerTailSize = formerTail == null ? 0 : formerTail.getBytes(StandardCharsets.UTF_8).length;
        bufferReader.reset();
        bufferReader.skip(formerTailSize);
        do {
            int c = bufferReader.read();
            if (c == -1) {
                break;
            }

            sb.append((char) c);
        } while (true);

        BufferedReader nextBufferReader = new BufferedReader(new InputStreamReader(new FileInputStream(file.getFD())));

        String tail = findTail(nextBufferReader);
        if (tail != null) {
            sb.append(tail);
//            System.out.println("Found tail: " + tail);
        }

        this.pageText = sb.toString();*/
    }

    private static @Nullable String findTail(BufferedReader bufferedReader) throws IOException {
        StringBuilder textBuilder = new StringBuilder();
        String text = "";
        String[] sentences = new String[0];

        String line;
        int read = 0;
        while ((line = readUntilPunctuation(bufferedReader, MAX_TAIL_LENGTH - read)) != null) {
            textBuilder.append(line);
            read += line.getBytes(StandardCharsets.UTF_8).length;
            text = textBuilder.toString();
            sentences = TextProcessor.sentences(text);
            if (sentences.length > 2) {
                break;
            }
        }

        if (sentences.length > 2) {
            int i = text.indexOf(sentences[1]);
            assert i != -1;
            return text.substring(0, i) + sentences[1];
        } else {
            return null;
        }
    }

    private static @Nullable String readUntilPunctuation(BufferedReader reader, int read) throws IOException {
        if (read == 0) {
            return null;
        }

        int c;
        StringBuilder sb = new StringBuilder();
        do {
            read--;
            c = reader.read();
            if (c != -1)
                sb.append((char) c);
        } while (c != -1 && c != '.' && c != '!' && c != '?' && read > 0);

        if (c == -1 && sb.length() == 0) {
            return null;
        } else {
            return sb.toString();
        }
    }

    public static Pages fromFile(Path path) throws IOException {
        return new Pages(path); // TODO
    }

    public static void main(String[] args) throws IOException {
        Pages pages = Pages.fromFile(Path.of("/home/florian/Downloads/germanSample.txt"));

        BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of("asdasd.txt"));

        bufferedWriter.write(pages.pageText);
        for (int i = 1; i <= pages.maxBufferNo; i++) {
            pages.readNthBuffer(i);
            bufferedWriter.write(pages.pageText);
        }
    }
}
