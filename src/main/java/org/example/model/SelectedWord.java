package org.example.model;

import org.example.model.page.Sentence;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public record SelectedWord(List<TokenLemma> word, @Nullable DictionaryLookup.DictionaryEntry dictionaryEntry, @Nullable Sentence sentence) {
    public String lemma() {
        if (dictionaryEntry != null) {
            return dictionaryEntry.lemma();
        }

        return word.stream().map(TokenLemma::lemma).collect(Collectors.joining());
    }
}
