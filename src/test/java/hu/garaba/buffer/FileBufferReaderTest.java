package hu.garaba.buffer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class FileBufferReaderTest {
    @Test
    void bufferSeparationTest() throws IOException {
        Path book = Path.of(FileBufferReader.class.getResource("/kafka_prozess.txt").getFile());

        var bufferReader = FileBufferReader.fromFile(book);

        try (ExecutorService executorService = Executors.newFixedThreadPool(4)) {
            for (int i = 0; i < bufferReader.maxBufferNo(); i++) {
                int finalI = i;
                executorService.submit(() -> bufferReader.getBuffer(finalI));
            }
        }

        System.out.println("Async finished");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bufferReader.maxBufferNo(); i++) {
            sb.append(bufferReader.getBuffer(i));
        }

        String expectedText = Files.readString(book);
        String actualText = sb.toString();
        assertEquals(expectedText.length(), actualText.length());
        assertEquals(expectedText, actualText);
    }
}