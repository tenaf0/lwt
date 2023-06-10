package org.example.model;

import org.jetbrains.annotations.Nullable;

public record TokenLemma(String token, @Nullable String lemma) {
}