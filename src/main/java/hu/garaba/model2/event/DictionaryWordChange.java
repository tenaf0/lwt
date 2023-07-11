package hu.garaba.model2.event;

import hu.garaba.dictionary.DictionaryEntry;

public record DictionaryWordChange(DictionaryEntry entry) implements ModelEvent {
}
