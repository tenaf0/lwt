package hu.garaba.textprocessor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextProcessorTest {
    @Test
    void test() {
        List<Sentence> sentences = TextProcessor.process(TextProcessor.TextProcessorModel.UDPIPE_1, "Ich stehe jeden Morgen um 7 Uhr auf.").toList();

        assertEquals(new Sentence(List.of(
                new TokenLemma("Ich", "Ich"),
                new TokenLemma("stehe", "stehen"),
                new TokenLemma("jeden", "jede"),
                new TokenLemma("Morgen", "Morgen"),
                new TokenLemma("um", "um"),
                new TokenLemma("7", "7"),
                new TokenLemma("Uhr", "Uhr"),
                new TokenLemma("auf", "auf", false),
                new TokenLemma(".", ".")
                ), List.of(new Word(List.of(7, 1)))), sentences.get(0));
    }

}