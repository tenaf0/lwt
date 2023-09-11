package hu.garaba.model2.event;

import hu.garaba.db.WordState;
import hu.garaba.dictionary.DictionaryEntry;
import hu.garaba.textprocessor.Sentence;
import hu.garaba.textprocessor.Word;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record SelectedWordChange(String lemma,
                                 @Nullable DictionaryEntry dictionaryEntry,
                                 @Nullable SentenceView sentence) implements ModelEvent {
    public record SentenceView(Sentence sentence, List<WordState> wordStates, Word selectedWord) {}
}