package hu.garaba.textprocessor;

import java.util.List;
import java.util.stream.Collectors;

public record Sentence(List<TokenLemma> tokens, List<Word> words) {
    public boolean isPartOfWord(int tokenIndex) {
        return findRelated(tokenIndex).tokens().size() > 1;
    }

    public Word findRelated(int tokenIndex) {
        return words.stream().filter(w -> w.tokens().contains(tokenIndex)).findAny().orElse(new Word(List.of(tokenIndex)));
    }

    public String toText() {
        return tokens()
                .stream()
                .map(tokenLemma -> tokenLemma.token() + (tokenLemma.spaceAfter() ? " " : ""))
                .collect(Collectors.joining());
    }
}
