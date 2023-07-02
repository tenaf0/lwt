package hu.garaba.buffer;

import hu.garaba.textprocessor.TokenLemma;
import hu.garaba.textprocessor.TextProcessor;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;

class PageReaderTest {
    @Test
    void firstPageReadTest() {
        Path book = Path.of(FileBufferReader.class.getResource("/kafka_prozess.txt").getFile());

        long l = System.currentTimeMillis();
        PageReader pageReader = new PageReader(TextProcessor.TextProcessorModel.UDPIPE_1, book);

        Semaphore semaphore = new Semaphore(1);
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        pageReader.init(p -> {
            semaphore.release();
        });

        Page page = pageReader.getPage(new PageReader.BufferPage(0, 0));
        System.out.println("First page read took: " + (System.currentTimeMillis() - l) + " ms");
        assertEquals(new TokenLemma("mußte", "müssen"), page.sentences().get(0).tokens().get(2));

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        page = pageReader.getPage(new PageReader.BufferPage(0, 0));
        assertEquals(new TokenLemma("haben", "haben", false), page.sentences().get(0).tokens().get(6));
    }
}