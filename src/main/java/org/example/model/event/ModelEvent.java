package org.example.model.event;

public sealed interface ModelEvent permits PageChange, WordChange, TokenChange, KnownChange {
}