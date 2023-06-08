package org.example.model;

import java.util.List;

public record Page(List<TokenLemma> tokenList, List<Word> words) {
}
