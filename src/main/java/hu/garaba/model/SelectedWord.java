package hu.garaba.model;

import hu.garaba.textprocessor.Sentence;
import hu.garaba.dictionary.DictionaryLookup;
import hu.garaba.textprocessor.TokenLemma;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public record SelectedWord(List<TokenLemma> word, @Nullable DictionaryLookup.DictionaryEntry dictionaryEntry,
                           @Nullable Sentence sentence) {
    public String lemma() {
        if (dictionaryEntry != null) {
            return dictionaryEntry.lemma();
        }

        return word.stream().map(TokenLemma::lemma).collect(Collectors.joining());
    }
}
