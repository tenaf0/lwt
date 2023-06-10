package org.example.model.event;

public sealed interface ModelEvent permits StateChange, PageChange, SelectedWordChange, TokenChange, KnownChange {
}