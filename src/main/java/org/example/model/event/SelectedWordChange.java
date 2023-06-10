package org.example.model.event;

import org.example.model.SelectedWord;

public record SelectedWordChange(SelectedWord newSelectedWord) implements ModelEvent {
}
