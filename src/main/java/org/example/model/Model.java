package org.example.model;

import org.example.model.event.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
    }

    public void subscribe(Consumer<ModelEvent> pageChangeHandler) {
        this.changeHandlers.add(pageChangeHandler);
    }

    public void setPage(Page page) {
        this.page = page;
        deselectAll();
        sendEvent(new PageChange(page));
    }

    public void selectToken(int i) {
        this.selectedTokens.add(i);
        sendEvent(new TokenChange(List.copyOf(selectedTokens)));
    }

    public void selectWord(int i) {
        deselectAll();
        selectedWord = getWord(i);
        selectedTokens.addAll(selectedWord.tokens());
        sendEvent(new TokenChange(selectedWord.tokens()));
        sendEvent(new WordChange(selectedWord.asLemma(page.tokenList())));
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

    public void addWord(Model.WordState state) {
        try {
            String lemma = selectedWord.asLemma(page.tokenList());
            DictionaryLookup.DictionaryEntry entry = DictionaryLookup.lookup(lemma);
            knownWordDb.addWord(entry != null ? entry.lemma() : lemma, state);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sendEvent(new KnownChange());
    }

    private void sendEvent(ModelEvent change) {
        System.out.println("-> " + change);
        for (var handler : changeHandlers) {
            handler.accept(change);
        }
    }
}
