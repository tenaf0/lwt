package hu.garaba.model.event;

import hu.garaba.model.SelectedWord;

public record SelectedWordChange(SelectedWord newSelectedWord) implements ModelEvent {
}
