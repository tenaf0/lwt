package hu.garaba.model;

import hu.garaba.buffer.Page;
import hu.garaba.buffer.PageReader;
import hu.garaba.textprocessor.Sentence;
import hu.garaba.dictionary.DictionaryLookup;
import hu.garaba.export.AnkiExport;
import hu.garaba.model.event.*;
import hu.garaba.textprocessor.TextProcessor;
import hu.garaba.textprocessor.TokenLemma;
import hu.garaba.textprocessor.Word;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Model {
    private final List<Consumer<ModelEvent>> changeHandlers = new ArrayList<>();

    public Model() {
        try {
            this.knownWordDb = new KnownWordDb("known_word.db");
            this.model = TextProcessor.getAvailableModels().stream().sorted().findFirst().get();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public enum ModelState {
        EMPTY, LOADING, LOADED
    }
    private TextProcessor.TextProcessorModel model;
    private @Nullable PageReader pageReader;
    private PageReader.BufferPage currentPage = new PageReader.BufferPage(0, 0);
    private Page page;

    private volatile @Nullable SelectedWord selectedWord;
    private final List<TokenCoordinate> selectedTokens = new ArrayList<>();

    private final KnownWordDb knownWordDb;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public Set<TextProcessor.TextProcessorModel> getAvailableModels() {
        return TextProcessor.getAvailableModels();
    }

    public TextProcessor.TextProcessorModel getModel() {
        return model;
    }

    public void setModel(TextProcessor.TextProcessorModel model) {
        this.model = model;

        if (pageReader != null) {
            pageReader.changeModel(model, currentPage);

            pageReader.submit(() -> {
                Page page = pageReader.getPage(currentPage);
                Platform.runLater(() -> setPage(page));
            });
        }
    }

    public void openText(Path path) {
        this.pageReader = new PageReader(model, path);
        initPageReader();
    }

    public void openText(String text) {
        this.pageReader = new PageReader(model, text);
        initPageReader();
    }

    private void initPageReader() {
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

        pageReader.submit(() -> {
            Page page = pageReader.getPage(currentPage);
            Platform.runLater(() -> setPage(page));
        });
    }

    public void selectWord(String lemma, @Nullable Sentence sentence) {
        List<TokenLemma> word = List.of(new TokenLemma(lemma, lemma, false));
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

    public void exportRows(Path path) {
        executorService.submit(() -> {
            try {
                AnkiExport.export(knownWordDb.fetchLearningWords(), path);
                Platform.runLater(() -> {
                    new Alert(Alert.AlertType.INFORMATION, "Export finished successfully!").showAndWait();
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    new Alert(Alert.AlertType.ERROR, "Unexpected error happened during export: " + e.getMessage()).showAndWait();
                });
                throw new RuntimeException(e);
            }
        });
    }

    public void close() {
        try {
            knownWordDb.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
