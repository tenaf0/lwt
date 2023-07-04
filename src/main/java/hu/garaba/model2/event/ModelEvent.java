package hu.garaba.model2.event;

public sealed interface ModelEvent permits StateChange, PageChange, WordStateChange,
        SelectionChange, DictionaryWordChange {
}
