package org.example.textprocessor;

import cz.cuni.mff.ufal.udpipe.Model;
import cz.cuni.mff.ufal.udpipe.Pipeline;
import opennlp.tools.formats.conllu.ConlluSentence;
import opennlp.tools.formats.conllu.ConlluStream;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.example.model.TokenLemma;
import org.example.model.page.Sentence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TextProcessor {
    private static final Model model;

    static {
        model = Model.load(TextProcessor.class.getResource("/model/german-hdt-ud-2.5-191206.udpipe").getFile());
    }

    public static Stream<Sentence> process(String sentences) {
        Pipeline pipeline = new Pipeline(model, "tokenize", Pipeline.getDEFAULT(), Pipeline.getDEFAULT(), "conllu");

        String processedText = pipeline.process(sentences);

        try (ConlluStream conlluStream = new ConlluStream(() -> new ByteArrayInputStream(processedText.getBytes(StandardCharsets.UTF_8)))) {
            ConlluSentence conlluSentence;

            ArrayList<Sentence> result = new ArrayList<>();

            while ((conlluSentence = conlluStream.read()) != null) {
                result.add(new Sentence(conlluSentence.getWordLines().stream()
                        .map(wl -> new TokenLemma(wl.getForm(), wl.getLemma()))
                        .toList(), List.of()));
            }

            return result.stream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<Sentence> process(Stream<String> sentences) {
        return process(sentences.collect(Collectors.joining(" ")));
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
}
