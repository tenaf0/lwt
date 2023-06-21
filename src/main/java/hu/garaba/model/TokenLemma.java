package hu.garaba.model;

import org.jetbrains.annotations.Nullable;

public record TokenLemma(String token, @Nullable String lemma, boolean spaceAfter) {
    public TokenLemma(String token, @Nullable String lemma) {
        this(token, lemma, true);
    }
}
