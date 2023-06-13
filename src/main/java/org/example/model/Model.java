package org.example.model;

import javafx.application.Platform;
import org.example.model.event.*;
import org.example.model.page.Page;
import org.example.model.page.PageReader;
import org.example.model.page.Sentence;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Model {
    private final List<Consumer<ModelEvent>> changeHandlers = new ArrayList<>();

    public Model() {
        try {
            this.knownWordDb = new KnownWordDb("known_word.db");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public enum ModelState {
        EMPTY, LOADING, LOADED
    }
    private @Nullable PageReader pageReader;
    private PageReader.BufferPage currentPage = new PageReader.BufferPage(0, 0);
    private Page page;

    private volatile @Nullable SelectedWord selectedWord;
    private final List<TokenCoordinate> selectedTokens = new ArrayList<>();

    private final KnownWordDb knownWordDb;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public void openText(Path path) {
        this.pageReader = new PageReader(path);
        this.currentPage = new PageReader.BufferPage(0, 0);
        sendEvent(new StateChange(ModelState.LOADING));
        pageReader.init(p -> {
            Platform.runLater(() -> {
                sendEvent(new StateChange(ModelState.LOADED));
                setPage(p);
            });
        });
    }

    private void setPage(Page page) {
        this.page = page;
        sendEvent(new PageChange(page));
    }

    public void changePage(boolean next) {
        if (next) {
            currentPage = pageReader.next(currentPage);
        } else {
            currentPage = pageReader.prev(currentPage);
        }
        System.out.println("Requested " + currentPage);

        new Thread(() -> {
            Page page = pageReader.getPage(currentPage);
            Platform.runLater(() -> setPage(page));
        }).start();
    }

    public void selectWord(String lemma, @Nullable Sentence sentence) {
        List<TokenLemma> word = List.of(new TokenLemma(lemma, lemma));
        selectedWord = new SelectedWord(word, null, sentence);
        sendEvent(new SelectedWordChange(selectedWord));

        executorService.submit(() -> {
            try {
                DictionaryLookup.DictionaryEntry entry = DictionaryLookup.lookup(lemma);
                if (entry != null && selectedWord.word().equals(word)) {
                    selectedWord = new SelectedWord(word, entry, sentence);
                    Platform.runLater(() -> sendEvent(new SelectedWordChange(selectedWord)));
                }
            } catch (IOException exc) {
                exc.printStackTrace(System.err);
            }
        });
    }

    public void selectWord(TokenCoordinate tokenCoordinate) {
        Sentence sentence = page.sentences().get(tokenCoordinate.sentenceNo());

        if (sentence.isPartOfWord(tokenCoordinate.tokenNo())) {
            Word word = sentence.findRelated(tokenCoordinate.tokenNo());
            sendEvent(new TokenChange(word.tokens().stream()
                    .map(i -> new TokenCoordinate(tokenCoordinate.sentenceNo(), i))
                    .toList()));
            selectWord(word.asLemma(sentence.tokens()), sentence);
        } else {
            TokenLemma tokenLemma = sentence.tokens().get(tokenCoordinate.tokenNo());
            sendEvent(new TokenChange(List.of(tokenCoordinate)));
            selectWord(tokenLemma.lemma() != null ? tokenLemma.lemma() : tokenLemma.token(), sentence);
        }
    }

    public enum WordState {
        KNOWN, LEARNING, UNKNOWN, IGNORED
    }
    public WordState isKnown(TokenCoordinate coord) {
        Sentence sentence = page.sentences().get(coord.sentenceNo());
        Word word = sentence.findRelated(coord.tokenNo());

        String token = sentence.tokens().get(coord.tokenNo()).token();
        if (List.of(",", ".", "-", ":", ";", "?", "!").contains(token)) {
            return WordState.IGNORED;
        }

        return isKnown(word.asLemma(sentence.tokens()));
    }

    public WordState isKnown(String lemma) {
        if (lemma == null) {
            return WordState.UNKNOWN;
        }
        return knownWordDb.isKnown(lemma);
    }


    public void addWord(CardEntry cardEntry, Model.WordState state) {
        knownWordDb.addWord(cardEntry, state);
        sendEvent(new KnownChange());
    }

    public void exportRows(Path path) throws IOException {
        AnkiExport.export(knownWordDb.fetchLearningWords(), path);
    }

    public void subscribe(Consumer<ModelEvent> pageChangeHandler) {
        this.changeHandlers.add(pageChangeHandler);
    }

    private void sendEvent(ModelEvent change) {
        if (change instanceof PageChange) {
            System.out.println("Page change");
        } else {
            System.out.println("-> " + change);
        }
        for (var handler : changeHandlers) {
            handler.accept(change);
        }
    }
}
