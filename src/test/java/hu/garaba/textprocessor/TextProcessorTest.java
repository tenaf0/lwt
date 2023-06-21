package hu.garaba.textprocessor;

import hu.garaba.model.TokenLemma;
import hu.garaba.model.Word;
import hu.garaba.buffer.Sentence;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextProcessorTest {
    @Test
    void test() {
        List<Sentence> sentences = TextProcessor.process(TextProcessor.TextProcessorModel.UDPIPE_2, "Er fährt das Schild um.").toList();

        assertEquals(new Sentence(List.of(
                new TokenLemma("Er", "Er"),
                new TokenLemma("fährt", "fahren"),
                new TokenLemma("das", "das"),
                new TokenLemma("Schild", "Schild"),
                new TokenLemma("um", "um", false),
                new TokenLemma(".", ".", false)
                ), List.of(new Word(List.of(4, 1)))), sentences.get(0));
    }

}