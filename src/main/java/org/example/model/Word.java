package org.example.model;

import org.example.textprocessor.TextProcessor;

import java.util.List;
import java.util.stream.Collectors;

public record Word(List<Integer> tokens) {
    public String asLemma(List<TextProcessor.TokenLemma> lemmas) {
        return tokens.stream()
                .map(t -> {
                    var lemma = lemmas.get(t);
                    return lemma.lemma() == null ? lemma.token() : lemma.lemma();
                })
                .collect(Collectors.joining());
    }
}
