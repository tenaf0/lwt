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
import hu.garaba.util.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.util.NullnessUtil;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ReadModel implements EventSource<ModelEvent> {
    public static final System.Logger LOGGER = System.getLogger("READMODEL");
    private final ExecutorService executorService = Executors.newCachedThreadPool(r -> {
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

    private final List<TokenCoordinate> selectedWord = new ArrayList<>();
    private final List<TokenCoordinate> highlightedTokens = new ArrayList<>();

    public ReadModel() {
        try {
            this.wordDB = new KnownWordDb("known_word.db");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void open(String text) {
        open(PageReader2.openText(text));
    }
    public void open(Path filePath) {
        try {
            open(PageReader2.openFile(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void open(PageReader2 pageReader) {
        changeModelState(ReadModelState.UNLOADED);

        this.pageReader = pageReader;
        changeModelState(ReadModelState.LOADING);

        executorService.submit(() -> {
            Page page = pageReader.getPage(0);
            changeModelState(ReadModelState.LOADED);
            setPage(page);
        });
    }

    private volatile int currentPage = 0;
    public void nextPage() {
        if (!NullnessUtil.castNonNull(pageReader).isPageAvailable(currentPage + 1)) {
            return;
        }

        Page page = NullnessUtil.castNonNull(pageReader).getPage(++currentPage);
        setPage(page);
    }

    public void prevPage() {
        if (!NullnessUtil.castNonNull(pageReader).isPageAvailable(currentPage - 1)) {
            return;
        }

        Page page = NullnessUtil.castNonNull(pageReader).getPage(--currentPage);
        setPage(page);
    }

    private void setPage(Page page) {
        List<Pair<TokenCoordinate, WordState>> wordStateChanges = new ArrayList<>();
        for (int s = 0; s < page.sentences().size(); s++) {
            Sentence sentence = page.sentences().get(s);
            for (int i = 0; i < sentence.tokens().size(); i++) {
                Word word = sentence.findRelated(i);
                wordStateChanges.add(
                        new Pair<>(new TokenCoordinate(s, i), wordDB.isKnown(word.asLemma(sentence.tokens())))
                );
            }
        };
        sendEvent(new JoinedEvent(List.of(new PageChange(page), new WordStateChange(wordStateChanges))));
    }

    public void selectWord(TokenCoordinate coordinate) {
        if (pageReader == null || !pageReader.isPageAvailable(currentPage)) {
            throw new IllegalStateException();
        }

        ArrayList<TokenCoordinate> oldSelection = new ArrayList<>(selectedWord);
        selectedWord.clear();

        Page page = NullnessUtil.castNonNull(pageReader).getPage(currentPage);
        Sentence sentence = page.sentences().get(coordinate.sentenceNo());
        Word word = sentence.findRelated(coordinate.tokenNo());
        word.tokens().forEach(i -> selectedWord.add(new TokenCoordinate(coordinate.sentenceNo(), i)));
        sendEvent(new SelectionChange(oldSelection, selectedWord));
    }

    private final List<Consumer<ModelEvent>> eventHandlers = new ArrayList<>();

    @Override
    public synchronized void subscribe(Consumer<ModelEvent> eventHandler) {
        eventHandlers.add(eventHandler);
    }

    @Override
    public void sendEvent(ModelEvent event) {
        LOGGER.log(System.Logger.Level.INFO, "-> " + event);
        eventHandlers.forEach(h -> h.accept(event));
    }
}
