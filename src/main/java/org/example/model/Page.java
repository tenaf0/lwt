package org.example.model;

import org.example.textprocessor.TextProcessor;

import java.util.List;

public record Page(List<TextProcessor.TokenLemma> tokenList, List<Word> words) {
}
