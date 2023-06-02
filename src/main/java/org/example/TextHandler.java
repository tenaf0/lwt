package org.example;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TextHandler {
    private final String text;

    public TextHandler(String text) {
        this.text = text;
    }

    public Stream<String> sentences() {


        try (InputStream modelIn = this.getClass().getResourceAsStream("/opennlp-de-sentence.bin")) {
            SentenceModel model = new SentenceModel(modelIn);
            SentenceDetectorME detector = new SentenceDetectorME(model);

            return Arrays.stream(detector.sentDetect(text));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

       /* return StreamSupport.stream(new Spliterator<>() {
            private final Matcher matcher = Pattern.compile("([.!?])\\W*").matcher(text);
            private int index = 0;

            @Override
            public boolean tryAdvance(Consumer<? super String> action) {
                if (matcher.find()) {
                    action.accept(text.substring(index, matcher.start()));
                    index = matcher.end();

                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public Spliterator<String> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            @Override
            public int characteristics() {
                return ORDERED | NONNULL | IMMUTABLE;
            }
        }, false);*/
    }
}
