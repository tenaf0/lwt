package hu.garaba.model2.event;

public sealed interface ModelEvent permits StateChange, PageChange, PageBoundaryChange, WordStateChange,
        SelectionChange, SelectedSentenceChange, SelectedWordChange {
}
