package hu.garaba.buffer;

import hu.garaba.textprocessor.Sentence;
import hu.garaba.textprocessor.TextProcessor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PageReader2 {
    public static final System.Logger LOGGER = System.getLogger(PageReader2.class.getCanonicalName());

    public final int PAGE_SIZE = 10;

    public sealed interface PageNo permits PageNo.ExactPageNo, PageNo.ApproxPageNo {
        int n();

        record ExactPageNo(int n) implements PageNo {}
        record ApproxPageNo(int readyPages, int n) implements PageNo {}
    }
    private volatile PageNo pageNo;
    public PageNo getPageNo() {
        return pageNo;
    }

    private final BufferReader bufferReader;
    private final Map<Integer, Page> pageMap = new ConcurrentHashMap<>();

    private PageReader2(BufferReader bufferReader, PageNo pageNo) {
        this.bufferReader = bufferReader;
        this.pageNo = pageNo;
    }

    public static PageReader2 openText(String text) {
        PageReader2 pageReader = new PageReader2(new StringReader(text),
                new PageNo.ApproxPageNo(0, text.length() / 30));
        pageReader.init();
        return pageReader;
    }

    public static PageReader2 openFile(Path path) throws IOException {
        FileBufferReader fileBufferReader = FileBufferReader.fromFile(path);
        PageReader2 pageReader = new PageReader2(fileBufferReader,
                new PageNo.ApproxPageNo(0, (int) (fileBufferReader.maxBufferNo() * 6)));
        pageReader.init();
        return pageReader;
    }

    private void init() {
        List<Future<List<Page>>> futurePages = new ArrayList<>();

        Thread pageAdder = new Thread(() -> {
            int finishedTill = 0;
            int sum = 0;

            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                int i = finishedTill;
                while (i < futurePages.size() && futurePages.get(i).isDone()) {
                    List<Page> pages;
                    try {
                        pages = futurePages.get(i).get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    for (int j = 0; j < pages.size(); j++) {
                        assert !pageMap.containsKey(sum + j);
                        pageMap.put(sum + j, pages.get(j));
                        pageNo = new PageNo.ApproxPageNo(sum + j, Math.max(pageNo.n(), sum + j));
                    }
                    i++;
                    finishedTill++;
                    sum += pages.size();
                }
            } while (finishedTill < futurePages.size());

            pageNo = new PageNo.ExactPageNo(sum);
            LOGGER.log(System.Logger.Level.DEBUG, "exact no of pages: " + pageNo);
        }, "pageAdder");
        pageAdder.setDaemon(true);
        pageAdder.start();

        Thread initThread = new Thread(() -> {
            try (ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                    r -> {
                        Thread thread = Executors.defaultThreadFactory().newThread(r);
                        thread.setDaemon(true);
                        return thread;
                    })) {
                for (int i = 0; i < bufferReader.maxBufferNo(); i++) {
                    final int finalI = i;
                    Future<List<Page>> pageNo = executorService.submit(() -> {
                        String text = bufferReader.getBuffer(finalI);
                        Stream<Sentence> sentenceStream = TextProcessor.process(TextProcessor.TextProcessorModel.UDPIPE_1, text);
                        return subdivideToPages(sentenceStream).toList();
                    });
                    futurePages.add(pageNo);
                }
            }
        }, "initThread");
        initThread.setDaemon(true);
        initThread.start();
    }

    public boolean isPageAvailable(int pageNo) {
        return pageMap.containsKey(pageNo);
    }

    public Page getPage(int pageNo) {
        if (pageNo < 0) {
            throw new IllegalArgumentException("pageNo can't be smaller than 0. Got: " + pageNo);
        }

        int n = 0;
        do {
            if (pageMap.containsKey(pageNo)) {
                return pageMap.get(pageNo);
            }
            n++;

            if (this.pageNo instanceof PageNo.ExactPageNo(var maxPage) && maxPage <= pageNo) {
                throw new IllegalStateException("pageNo is larger than the number of pages. Got " + pageNo +
                        ", no of pages: " + maxPage);
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (n < 1000);

        throw new IllegalStateException("Page " + pageNo + " didn't get available");
    }

    private Stream<Page> subdivideToPages(Stream<Sentence> sentenceStream) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<>() {
            private final Iterator<Sentence> iterator = sentenceStream.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Page next() {
                int i = 0;
                List<Sentence> page = new ArrayList<>();
                while (i < PAGE_SIZE && iterator.hasNext()) {
                    Sentence next = iterator.next();
                    page.add(next);
                    i++;
                }
                return new Page(page);
            }
        }, 0), false); // TODO: Characteristics
    }
}
