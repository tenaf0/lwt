package hu.garaba.model;

import org.jetbrains.annotations.Nullable;

public record CardEntry(@Nullable String id, @Nullable String prefix, String word, @Nullable String postfix, @Nullable String meaning, @Nullable String note, @Nullable String exampleSentence) {
    public CardEntry wordOnly() {
        return new CardEntry(null, null, word, null, null, null, null);
    }
}
