package org.example.model;

import javafx.application.Platform;
import org.example.model.event.DictionaryChange;
import org.example.model.event.ModelEvent;
import org.example.model.page.Pages;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Model2 {
    private final List<Consumer<ModelEvent>> changeHandlers = new ArrayList<>();

    private @Nullable Pages pages;
    private @Nullable SelectedWord selectedWord;
    private final List<TokenCoordinate> selectedTokens = new ArrayList<>();

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public void openText(Path path) throws IOException {
        this.pages = Pages.fromFile(path);
    }

    public void selectWord(String lemma) {
        selectedWord = new SelectedWord(List.of(new TokenLemma(lemma, lemma)), null, null);
        // TODO: sendEvent(SelectedWordChange)

        executorService.submit(() -> {
            try {
                DictionaryLookup.DictionaryEntry entry = DictionaryLookup.lookup(lemma);
                selectedWord = new SelectedWord(List.of(new TokenLemma(lemma, lemma)), entry, null);
                Platform.runLater(() -> sendEvent(new DictionaryChange(entry))); // TODO: also sendEvent(SelectedWordChange)
            } catch (IOException exc) {
                exc.printStackTrace(System.err);
            }
        });
    }

    public void selectWord(TokenCoordinate tokenCoordinate) {

    }

    public void subscribe(Consumer<ModelEvent> pageChangeHandler) {
        this.changeHandlers.add(pageChangeHandler);
    }

    private void sendEvent(ModelEvent change) {
        System.out.println("-> " + change);
        for (var handler : changeHandlers) {
            handler.accept(change);
        }
    }
}
