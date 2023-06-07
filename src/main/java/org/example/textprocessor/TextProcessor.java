package org.example.textprocessor;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.example.model.Page;
import org.example.model.Word;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TextProcessor {
    private final String text;

    public TextProcessor(String text) {
        this.text = text;

        Util.initLemmatizer();
    }

    public Stream<Page> process() {
        return pagify(reattach(posTag(tokens(sentences(text)))));
    }

    public static Stream<String> sentences(String text) {
        try (InputStream modelIn = TextProcessor.class.getResourceAsStream("/model/opennlp-de-sentence.bin")) {
            SentenceModel model = new SentenceModel(modelIn);
            SentenceDetectorME detector = new SentenceDetectorME(model);

            return Arrays.stream(detector.sentDetect(text));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<Stream<String>> tokens(Stream<String> sentences) {
        try (InputStream modelIn = TextProcessor.class.getResourceAsStream("/model/opennlp-de-token.bin")) {
            TokenizerModel model = new TokenizerModel(modelIn);
            TokenizerME detector = new TokenizerME(model);

            return sentences.map(s -> Arrays.stream(detector.tokenize(s)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public record POSToken(String token, String pos) {}

    public static Stream<List<POSToken>> posTag(Stream<Stream<String>> tokens) {
        try (InputStream modelIn = TextProcessor.class.getResourceAsStream("/model/de-pos-maxent.bin")) {
            POSModel model = new POSModel(modelIn);
            POSTaggerME detector = new POSTaggerME(model);

            return tokens.map(s -> {
                List<String> sentenceTokens = s.toList();
                String[] tags = detector.tag(sentenceTokens.toArray(new String[0]));

                return IntStream.range(0, sentenceTokens.size())
                        .mapToObj(i -> new POSToken(sentenceTokens.get(i), tags[i])).toList();
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public record TokenLemma(String token, String lemma) {}
    public record Sentence(List<TokenLemma> tokens, List<Word> words) {}
    public static Stream<Sentence> reattach(Stream<List<POSToken>> taggedTokens) {
        return taggedTokens.map(s -> {
            List<String> tokens = new ArrayList<>();
            List<Word> words = new ArrayList<>();

            for (int i = 0; i < s.size(); i++) {
                POSToken taggedToken = s.get(i);
                tokens.add(taggedToken.token);
                if (taggedToken.pos.equals("PTKVZ")) {
                    for (int j = i - 1; j >= 0; j--) {
                        if (s.get(j).pos.equals("VVFIN") || s.get(j).pos.equals("VVIMP")) {
                            words.add(new Word(List.of(i, j)));
                        }
                    }
                }
            }

            return new Sentence(s.stream()
                    .map(t -> new TokenLemma(t.token, Util.lemmatizeWithIWNLP(t)))
                    .toList(), words);
        });
    }

    public static Stream<Page> pagify(Stream<Sentence> sentences) {
        return StreamSupport.stream(new Spliterator<>() {
            private Iterator<Sentence> iterator = sentences.iterator();

            @Override
            public boolean tryAdvance(Consumer<? super Page> action) {
                if (!iterator.hasNext()) {
                    return false;
                }

                int i = 0;
                List<TokenLemma> tokens = new ArrayList<>();
                List<Word> words = new ArrayList<>();
                while (i < 300 && iterator.hasNext()) {
                    Sentence s = iterator.next();
                    tokens.addAll(s.tokens);
                    for (var w : s.words) {
                        int finalI = i;
                        words.add(new Word(w.tokens().stream().map(ind -> ind + finalI).toList()));
                    }
                    i += s.tokens.size();
                }

                action.accept(new Page(tokens, words));

                return true;
            }

            @Override
            public Spliterator<Page> trySplit() {
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
        }, false);
    }
}
