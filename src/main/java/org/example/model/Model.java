package org.example.model;

import javafx.application.Platform;
import org.example.model.event.*;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Model {

    private Page page;
    private List<Integer> selectedTokens = new ArrayList<>();
    private Word selectedWord = null;

    private List<Consumer<ModelEvent>> changeHandlers = new ArrayList<>();

    private final KnownWordDb knownWordDb;

    public Model() {
        try {
            knownWordDb = new KnownWordDb("known_word.db");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            Stream<Page> pageStream = bookReader.pageStream();
            this.iterator = pageStream.iterator();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void subscribe(Consumer<ModelEvent> pageChangeHandler) {
        this.changeHandlers.add(pageChangeHandler);
    }

    public void setPage(Page page) {
        this.page = page;
        deselectAll();
        sendEvent(new PageChange(page));
    }

    public void toggleToken(int i) {
        if (this.selectedTokens.contains(i)) {
            this.selectedTokens.remove((Integer) i);
        } else {
            this.selectedTokens.add(i);
        }
        sendEvent(new TokenChange(List.copyOf(selectedTokens)));
    }

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public void selectWord(int i) {
        deselectAll();
        selectedWord = getWord(i);
        selectedTokens.addAll(selectedWord.tokens());
        sendEvent(new TokenChange(selectedWord.tokens()));
        lookupWord(selectedWord.asLemma(page.tokenList()));
    }

    public void lookupWord(String word) {
        if (word.isEmpty()) {
            return;
        }

        executorService.submit(() -> {
            try {
                DictionaryLookup.DictionaryEntry entry = DictionaryLookup.lookup(word);
                Platform.runLater(() -> sendEvent(new DictionaryChange(entry)));
            } catch (IOException exc) {
                exc.printStackTrace(System.err);
            }
        });
    }

    public void deselectAll() {
        selectedTokens.clear();
        sendEvent(new TokenChange(List.of()));
    }

    public Word getWord(int i) {
        List<Word> words = page.words().stream().filter(w -> w.tokens().contains(i)).toList();
        if (words.size() > 1) {
            System.err.println("Multiple words containing token " + page.tokenList().get(i) + " found " + words);
        }

        if (!words.isEmpty()) {
            return words.get(0);
        } else {
            return new Word(List.of(i));
        }
    }

    public enum WordState {
        KNOWN, LEARNING, UNKNOWN, IGNORED
    }

    public WordState isKnown(int i) {
        String token = page.tokenList().get(i).token();
        if (List.of(",", ".", "-", ";", "?", "!").contains(token)) {
            return WordState.IGNORED;
        }

        Word word = getWord(i);

        return knownWordDb.isKnown(word.asLemma(page.tokenList()));
    }

    public void addWord(CardEntry cardEntry, Model.WordState state) {
        knownWordDb.addWord(cardEntry, state);
        sendEvent(new KnownChange());
    }

    private BookReader bookReader = new BookReader(Path.of("/home/florian/Downloads/HP.txt"));

    private Iterator<Page> iterator;

    public void nextPage() {
        setPage(iterator.next());
    }

    private void sendEvent(ModelEvent change) {
        System.out.println("-> " + change);
        for (var handler : changeHandlers) {
            handler.accept(change);
        }
    }
}
