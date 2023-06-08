package org.example.model;

import org.example.model.page.Sentence;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record SelectedWord(List<TokenLemma> word, @Nullable DictionaryLookup.DictionaryEntry dictionaryEntry, @Nullable Sentence sentence) {
}
