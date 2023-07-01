package hu.garaba.model2.event;

public sealed interface ModelEvent permits PageChange, StateChange, SelectionChange, DictionaryWordChange {
}
