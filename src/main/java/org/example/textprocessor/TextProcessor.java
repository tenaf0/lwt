package org.example.textprocessor;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.example.model.TokenLemma;
import org.example.model.Word;
import org.example.model.page.Sentence;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TextProcessor {
    static {
        Util.initLemmatizer();
    }

    public static Stream<Sentence> process(Stream<String> sentences) {
        return reattach(posTag(tokens(sentences)));
    }

    public static List<String> sentences(String text) {
        try (InputStream modelIn = TextProcessor.class.getResourceAsStream("/model/opennlp-de-sentence.bin")) {
            SentenceModel model = new SentenceModel(modelIn);
            SentenceDetectorME detector = new SentenceDetectorME(model);

            List<String> sentences = new ArrayList<>();

            String[] paragraphs = text.split("\n{2,}");
            for (var p : paragraphs) {
                String[] strings = detector.sentDetect(p);
                sentences.addAll(Arrays.asList(strings));
                sentences.add(null);
            }

            return sentences;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<Stream<String>> tokens(Stream<String> sentences) {
        try (InputStream modelIn = TextProcessor.class.getResourceAsStream("/model/opennlp-de-token.bin")) {
            TokenizerModel model = new TokenizerModel(modelIn);
            TokenizerME detector = new TokenizerME(model);

            return sentences.map(s -> {
                if (s == null) {
                    return Stream.of();
                } else {
                    return Arrays.stream(detector.tokenize(s));
                }
            });
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

    public static Stream<Sentence> reattach(Stream<List<POSToken>> taggedTokens) {
        return taggedTokens.map(s -> {
            List<String> tokens = new ArrayList<>();
            List<Word> words = new ArrayList<>();

            for (int i = 0; i < s.size(); i++) {
                POSToken taggedToken = s.get(i);
                tokens.add(taggedToken.token);
                if (taggedToken.pos.equals("PTKVZ")) {
                    for (int j = i - 1; j >= 0; j--) {
                        if (s.get(j).token.equals(",")) {
                            break;
                        }
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
}
