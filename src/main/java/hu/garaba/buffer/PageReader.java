package hu.garaba.buffer;

import hu.garaba.textprocessor.TextProcessor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class PageReader {
    protected static final int PAGE_SIZE = 10; // number of sentences on each page

    private final BufferReader bufferReader;
    private long maxBufferNo;

    public record BufferPage(int bufferNo, int pageNo) {}

    record PageHandler(boolean complete, CopyOnWriteArrayList<Page> pages) {}
    private final Map<Integer, PageHandler> bufferedPages = new ConcurrentHashMap<>();

    private final ExecutorService executorService = Executors.newCachedThreadPool(r -> {
        var thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });

    public PageReader(Path path) {
        try {
            this.bufferReader = BufferReader.fromFile(path);
            this.maxBufferNo = bufferReader.maxBufferNo();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void init(Consumer<Page> fn) {
        executorService.submit(() -> loadPages(0));

        executorService.submit(() -> {
            Page page = getPage(new BufferPage(0, 0), false);
            fn.accept(page);
        });
    }

    private void touchPage(BufferPage page, boolean shouldInitLoad) {
        PageHandler pageHandler;
        do {
            pageHandler = bufferedPages.get(page.bufferNo);
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
        PageHandler pageHandler = bufferedPages.get(page.bufferNo);
        if (pageHandler == null || !pageHandler.complete) {
            return page;
        } else if (page.pageNo + 1 < pageHandler.pages.size()) {
            page = new BufferPage(page.bufferNo, page.pageNo + 1);
        } else if (page.bufferNo + 1 < maxBufferNo) {
            page = new BufferPage(page.bufferNo + 1, 0);
        }

        BufferPage finalPage = page;
        executorService.submit(() -> {
            loadPages(finalPage.bufferNo + 1);
            loadPages(finalPage.bufferNo + 2);
        });

        return page;
    }

   /* public boolean hasPrev() {

    }*/

    public BufferPage prev(BufferPage page) {
        if (page.pageNo - 1 >= 0) {
            page = new BufferPage(page.bufferNo, page.pageNo - 1);
        } else if (page.bufferNo > 0 && bufferedPages.get(page.bufferNo - 1) != null && bufferedPages.get(page.bufferNo - 1).complete) {
            int prevBufferPages = bufferedPages.get(page.bufferNo - 1).pages.size();
            page = new BufferPage(page.bufferNo-1, prevBufferPages - 1);
        }

        return page;
    }

    public void submit(Runnable runnable) {
        executorService.submit(runnable);
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
}
