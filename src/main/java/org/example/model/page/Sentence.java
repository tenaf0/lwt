package org.example.model.page;

import org.example.model.TokenLemma;
import org.example.model.Word;

import java.util.List;

public record Sentence(List<TokenLemma> tokens, List<Word> words) {
    public boolean isPartOfWord(int tokenIndex) {
        return findRelated(tokenIndex).tokens().size() > 1;
    }

    public Word findRelated(int tokenIndex) {
        return words.stream().filter(w -> w.tokens().contains(tokenIndex)).findAny().orElse(new Word(List.of(tokenIndex)));
    }
}
