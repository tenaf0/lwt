package hu.garaba.model;

import hu.garaba.buffer.Sentence;
import hu.garaba.dictionary.DictionaryLookup;
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
