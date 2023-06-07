package org.example.model.event;

import org.example.model.DictionaryLookup;

public record DictionaryChange(DictionaryLookup.DictionaryEntry entry) implements ModelEvent {
}
