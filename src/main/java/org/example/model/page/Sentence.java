package org.example.model.page;

import org.example.model.TokenLemma;
import org.example.model.Word;

import java.util.List;

public record Sentence(List<TokenLemma> tokens, List<Word> words) {
}
