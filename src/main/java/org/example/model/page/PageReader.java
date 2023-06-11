package org.example.model.page;

import org.example.textprocessor.TextProcessor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;

public class PageReader {
    private static final int PAGE_SIZE = 7; // number of sentences on each page

    private final BufferReader bufferReader;
    private long maxBufferNo;
    private long bufferNo;
    private long maxPageNo;
    private long pageNo;

    private AtomicInteger bufferedPagesReadyIndex = new AtomicInteger();
    private AtomicReferenceArray<Page> bufferedPages;

    public PageReader(Path path) {
        try {
            this.bufferReader = BufferReader.fromFile(path);
            this.maxBufferNo = bufferReader.maxBufferNo();
            this.bufferNo = 0;
            this.pageNo = -1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void init(Consumer<Page> fn) {
        new Thread(() -> {
            Page page;
            synchronized (this) {
                loadBuffer(0);
                pageNo = 0;
                page = getPage();
            }
            fn.accept(page);
        }).start();
    }

    public synchronized Page getPage() {
        int i = 0;
        while (bufferedPagesReadyIndex.get() < pageNo) {
            i++;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Got page: " + bufferedPagesReadyIndex.get() + " " + pageNo + " for " + i + "th try");
        return bufferedPages.getAcquire((int) pageNo);
    }

    /*public boolean hasNext() {

    }*/

    public synchronized void next() {
        if (pageNo + 1 < maxPageNo) {
            pageNo++;
        } else {
            if (bufferNo + 1 >= maxBufferNo) {
                return;
            }
            loadBuffer((int) bufferNo + 1);
            pageNo = 0;
        }
    }

   /* public boolean hasPrev() {

    }*/

    public synchronized void prev() {
        if (pageNo - 1 >= 0) {
            pageNo--;
        } else {
            if (bufferNo - 1 < 0) {
                return;
            }
            loadBuffer((int) bufferNo - 1);
            pageNo = maxPageNo-1;
        }
    }

    private synchronized void loadBuffer(int n) {
        assert n >= 0 && n <= maxBufferNo;

        String bufferText = bufferReader.getBuffer(n);
        List<String> sentences = TextProcessor.sentences(bufferText);
        bufferNo = n;
        long sentenceSize = sentences.stream().filter(Objects::nonNull).count();
        maxPageNo = sentenceSize / PAGE_SIZE + (sentenceSize % PAGE_SIZE == 0 ? 0 : 1);
        pageNo = -1;

        bufferedPagesReadyIndex.set(-1);
        bufferedPages = new AtomicReferenceArray<>((int) maxPageNo);

        Thread textProcessor = new Thread(() -> {
            Iterator<Sentence> iterator = TextProcessor.process(sentences.stream()).iterator();
            int i = 0;
            var list = new ArrayList<Sentence>();
            while (iterator.hasNext()) {
                Sentence sentence = iterator.next();
                list.add(sentence);
                if (sentence.tokens().size() > 0) {
                    i++;
                }
                if (i == PAGE_SIZE) {
                    bufferedPages.setRelease(bufferedPagesReadyIndex.incrementAndGet(), new Page(list));
                    list = new ArrayList<>();
                    i = 0;
                }
            }

            if (!list.isEmpty()) {
                bufferedPages.setRelease(bufferedPagesReadyIndex.incrementAndGet(), new Page(list));
            }
        }, "TextProcessor");
        textProcessor.setDaemon(true);
        textProcessor.start();
    }

    public static void main(String[] args) {

        for (int i = 0; i < 10; i++) {
            PageReader pageReader = new PageReader(Path.of("/home/florian/Downloads/HP2.txt"));

            final long l = System.currentTimeMillis();
            Semaphore semaphore = new Semaphore(1);
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            pageReader.init(p -> {
                System.out.println(System.currentTimeMillis() - l);
                semaphore.release();
            });

            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
