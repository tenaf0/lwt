package hu.garaba.model2;

import hu.garaba.buffer.Page;
import hu.garaba.buffer.PageReader2;
import hu.garaba.db.KnownWordDb;
import hu.garaba.db.WordState;
import hu.garaba.model.TokenCoordinate;
import hu.garaba.model2.event.*;
import hu.garaba.textprocessor.Sentence;
import hu.garaba.textprocessor.Word;
import hu.garaba.util.EventSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.util.NullnessUtil;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ReadModel implements EventSource<ModelEvent> {
    public static final System.Logger LOGGER = System.getLogger(ReadModel.class.getCanonicalName());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
        @SuppressWarnings("nullness") @NonNull
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        return t;
    });

    public enum ReadModelState {
        UNLOADED, LOADING, LOADED
    }
    private ReadModelState state = ReadModelState.UNLOADED;

    private void changeModelState(ReadModelState newState) {
        this.state = newState;
        sendEvent(new StateChange(newState));
    }

    private final KnownWordDb wordDB;
    private @Nullable PageReader2 pageReader;

    private final Set<TokenCoordinate> selectedWord = new HashSet<>();
    private final List<TokenCoordinate> highlightedTokens = new ArrayList<>();

    public ReadModel() {
        try {
            this.wordDB = new KnownWordDb("known_word.db");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void open(String text) {
        LOGGER.log(System.Logger.Level.INFO, "Opening provided text input");
        open(PageReader2.openText(text));
    }
    public void open(Path filePath) {
        try {
            LOGGER.log(System.Logger.Level.INFO, "Opening " + filePath);
            open(PageReader2.openFile(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void open(PageReader2 pageReader) {
        currentPage = 0;
        changeModelState(ReadModelState.UNLOADED);

        this.pageReader = pageReader;
        changeModelState(ReadModelState.LOADING);

        executorService.submit(() -> {
            Page page = pageReader.getPage(0);
            changeModelState(ReadModelState.LOADED);
            setPage(0, page);
        });
    }

    private volatile int currentPage = 0;
    public void nextPage() {
        if (!NullnessUtil.castNonNull(pageReader).isPageAvailable(currentPage + 1)) {
            return;
        }

        Page page = NullnessUtil.castNonNull(pageReader).getPage(currentPage + 1);
        currentPage++;
        setPage(currentPage, page);
    }

    public void prevPage() {
        if (!NullnessUtil.castNonNull(pageReader).isPageAvailable(currentPage - 1)) {
            return;
        }

        Page page = NullnessUtil.castNonNull(pageReader).getPage(currentPage - 1);
        currentPage--;
        setPage(currentPage, page);
    }

    public void seekPage(int n) {
        if (!NullnessUtil.castNonNull(pageReader).isPageAvailable(n)) {
            return;
        }

        executorService.submit(() -> {
            Page page = NullnessUtil.castNonNull(pageReader).getPage(n);
            currentPage = n;
            setPage(n, page);
        });
    }

    private PageReader2.@Nullable PageNo prevPageBoundary;
    private void setPage(int n, Page page) {
        List<WordState> wordStateChanges = new ArrayList<>();
        for (int s = 0; s < page.sentences().size(); s++) {
            Sentence sentence = page.sentences().get(s);
            for (int i = 0; i < sentence.tokens().size(); i++) {
                Word word = sentence.findRelated(i);
                wordStateChanges.add(wordDB.isKnown(word.asLemma(sentence.tokens())));
            }
        }
        sendEvent(new PageChange(n, new PageView(page, wordStateChanges)));

        if (pageReader != null && !pageReader.getPageNo().equals(prevPageBoundary)) {
            this.prevPageBoundary = NullnessUtil.castNonNull(pageReader).getPageNo();
            sendEvent(new PageBoundaryChange(prevPageBoundary));
        }
    }

    public void selectWord(TokenCoordinate coordinate) {
        if (pageReader == null || !pageReader.isPageAvailable(currentPage)) {
            throw new IllegalStateException();
        }

        Set<TokenCoordinate> oldSelection = new HashSet<>(selectedWord);
        selectedWord.clear();

        Page page = NullnessUtil.castNonNull(pageReader).getPage(currentPage);
        Sentence sentence = page.sentences().get(coordinate.sentenceNo());
        Word word = sentence.findRelated(coordinate.tokenNo());
        word.tokens().forEach(i -> selectedWord.add(new TokenCoordinate(coordinate.sentenceNo(), i)));

        sendEvent(new SelectionChange(oldSelection, selectedWord));
        sendEvent(new SelectedSentenceChange(new PageView(new Page(List.of(sentence)), Collections.nCopies(sentence.tokens().size(), WordState.IGNORED)), word.tokens()));
    }

    private final List<Consumer<ModelEvent>> eventHandlers = new ArrayList<>();

    @Override
    public synchronized void subscribe(Consumer<ModelEvent> eventHandler) {
        eventHandlers.add(eventHandler);
    }

    @Override
    public void sendEvent(ModelEvent event) {
        LOGGER.log(System.Logger.Level.DEBUG, "-> " + event);
        eventHandlers.forEach(h -> h.accept(event));
    }
}
