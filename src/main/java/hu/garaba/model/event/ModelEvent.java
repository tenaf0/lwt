package hu.garaba.model.event;

public sealed interface ModelEvent permits StateChange, PageChange, SelectedWordChange, TokenChange, KnownChange {
}