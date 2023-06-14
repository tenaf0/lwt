package hu.garaba.model.page;

import hu.garaba.textprocessor.TextProcessor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class PageReader {
    private static final int PAGE_SIZE = 8; // number of sentences on each page

    private final BufferReader bufferReader;
    private long maxBufferNo;

    public record BufferPage(int bufferNo, int pageNo) {}

    record PageHandler(boolean complete, CopyOnWriteArrayList<Page> pages) {}
    private Map<Integer, PageHandler> bufferedPages = new ConcurrentHashMap<>();

    public PageReader(Path path) {
        try {
            this.bufferReader = BufferReader.fromFile(path);
            this.maxBufferNo = bufferReader.maxBufferNo();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void init(Consumer<Page> fn) {
        Thread loaderThread = new Thread(() -> {
            loadPages(0);
        });
        loaderThread.setDaemon(true);
        loaderThread.start();

        Thread thread = new Thread(() -> {
            Page page = getPage(new BufferPage(0, 0), false);
            fn.accept(page);
        }, "PageReader init thread");
        thread.setDaemon(true);
        thread.start();
    }

    private void touchPage(BufferPage page, boolean shouldInitLoad) {
        PageHandler pageHandler;
        do {
            pageHandler= bufferedPages.get(page.bufferNo);
        } while ((!shouldInitLoad && pageHandler == null)
                || (pageHandler != null && pageHandler.pages.size() <= page.pageNo));


        if (pageHandler != null) {
            return;
        }

        loadPages(page.bufferNo);
    }


    public Page getPage(BufferPage page) {
        return getPage(page, true);
    }

    public Page getPage(BufferPage page, boolean shouldInitLoad) {
        touchPage(page, shouldInitLoad);

        PageHandler pageHandler;
        do {
            pageHandler = bufferedPages.get(page.bufferNo);
        } while (!(pageHandler.complete
                || pageHandler.pages.size() > page.pageNo));


        if (pageHandler.pages.size() > page.pageNo) {
            return pageHandler.pages.get(page.pageNo);
        }
        else {
            throw new IllegalArgumentException("There is no %d page on buffer %d.".formatted(page.pageNo, page.bufferNo));
        }
    }

    /*public boolean hasNext() {

    }*/

    public BufferPage next(BufferPage page) {
        touchPage(page, true);

        if (!bufferedPages.get(page.bufferNo).complete) {
            return page;
        } else if (page.pageNo + 1 < bufferedPages.get(page.bufferNo).pages.size()) {
            page = new BufferPage(page.bufferNo, page.pageNo + 1);
        } else if (page.bufferNo + 1 < maxBufferNo) {
            page = new BufferPage(page.bufferNo + 1, 0);
        }

        BufferPage finalPage = page;
        Thread thread = new Thread(() -> {
            loadPages(finalPage.bufferNo + 1);
        }, "next buffer loader");
        thread.setDaemon(true);
        thread.start();

        return page;
    }

   /* public boolean hasPrev() {

    }*/

    public BufferPage prev(BufferPage page) {
        touchPage(page, true);

        if (page.pageNo - 1 >= 0) {
            page = new BufferPage(page.bufferNo, page.pageNo - 1);
        } else if (page.bufferNo > 0 && bufferedPages.get(page.bufferNo - 1).complete) {
            int prevBufferPages = bufferedPages.get(page.bufferNo - 1).pages.size();
            page = new BufferPage(page.bufferNo-1, prevBufferPages - 1);
        }

        return page;
    }

    private void loadPages(int bufferNo) {
        if (bufferNo >= maxBufferNo) {
            throw new IllegalStateException("Can't load page for bufferNo " + bufferNo);
        }

        String bufferText = bufferReader.getBuffer(bufferNo);

        List<String> sentences = TextProcessor.sentences(bufferText);

        CopyOnWriteArrayList<Page> bufferPages = new CopyOnWriteArrayList<>();
        PageHandler prevValue = bufferedPages.putIfAbsent(bufferNo, new PageHandler(false, bufferPages));
        if (prevValue != null) {
            return;
        }
;

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
                bufferPages.add(new Page(list));
                list = new ArrayList<>();
                i = 0;
            }
        }

        if (!list.isEmpty()) {
            bufferPages.add(new Page(list));
        }

        bufferedPages.put(bufferNo, new PageHandler(true, bufferPages));
    }

    /*private synchronized void loadBuffer(int n) {
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
    }*/

    public static void main(String[] args) {

        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            PageReader pageReader = new PageReader(Path.of("/home/florian/Downloads/HP.txt"));

            try {
                pageReader.getPage(new BufferPage(random.nextInt((int) pageReader.maxBufferNo), random.nextInt(10)));
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }

            /*final long l = System.currentTimeMillis();
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
            }*/
        }
    }
}
