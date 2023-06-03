package org.example.model;

public sealed interface ModelChange permits PageChange, WordChange, TokenChange, KnownChange {
}