package hu.garaba.textprocessor;

import com.alibaba.fastjson2.JSON;
import com.github.mizosoft.methanol.Methanol;
import com.github.mizosoft.methanol.MultipartBodyPublisher;
import cz.cuni.mff.ufal.udpipe.Model;
import cz.cuni.mff.ufal.udpipe.Pipeline;
import hu.garaba.buffer.Sentence;
import hu.garaba.model.TokenLemma;
import hu.garaba.model.Word;
import opennlp.tools.formats.conllu.ConlluSentence;
import opennlp.tools.formats.conllu.ConlluStream;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TextProcessor {
    private static final Model model;
    private static final String PROCESS_URL = "http://lindat.mff.cuni.cz/services/udpipe/api/process";

    static {
        model = Model.load(TextProcessor.class.getResource("/model/german-hdt-ud-2.5-191206.udpipe").getFile());
    }

    public static void warmup() {
        TextProcessor.process2("""
                Wer reitet so spät durch Nacht und Wind?
                Es ist der Vater mit seinem Kind;
                Er hat den Knaben wohl in dem Arm,
                Er faßt ihn sicher, er hält ihn warm.
                                
                "Mein Sohn, was birgst du so bang dein Gesicht?"--
                "Siehst, Vater, du den Erlkönig nicht?
                Den Erlenkönig mit Kron' und Schweif?"--
                "Mein Sohn, es ist ein Nebelstreif."
                """);
    }

    public static Stream<Sentence> process2(String sentences) {

        Methanol client = Methanol.create();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROCESS_URL))
                .POST(MultipartBodyPublisher.newBuilder()
                        .formPart("data", HttpRequest.BodyPublishers.ofString(sentences))
                        .formPart("model", HttpRequest.BodyPublishers.ofString("de"))
                        .formPart("tokenizer", HttpRequest.BodyPublishers.noBody())
                        .formPart("tagger", HttpRequest.BodyPublishers.noBody())
                        .formPart("parser", HttpRequest.BodyPublishers.noBody())
                        .build())
                .build();

        try {
            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

            var res = JSON.parseObject(resp.body());
            return conlluParse((String) res.get("result"));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<Sentence> process(String sentences) {
        Pipeline pipeline = new Pipeline(model, "tokenize", Pipeline.getDEFAULT(), Pipeline.getDEFAULT(), "conllu");

        String processedText = pipeline.process(sentences);

        return conlluParse(processedText);
    }

    public static Stream<Sentence> process(Stream<String> sentences) {
        return process2(sentences.collect(Collectors.joining(" ")));
    }

    private static Stream<Sentence> conlluParse(String processedText) {
        try (ConlluStream conlluStream = new ConlluStream(() -> new ByteArrayInputStream(processedText.getBytes(StandardCharsets.UTF_8)))) {
            ConlluSentence conlluSentence;

            ArrayList<Sentence> result = new ArrayList<>();

            while ((conlluSentence = conlluStream.read()) != null) {
                List<TokenLemma> tokenLemmas = conlluSentence.getWordLines().stream()
                        .map(wl -> new TokenLemma(wl.getForm(), wl.getLemma()))
                        .toList();
                List<Word> words = conlluSentence.getWordLines().stream()
                        .filter(wl -> wl.getDeprel().equals("compound:prt"))
                        .map(wl -> new Word(List.of(Integer.parseInt(wl.getId())-1, Integer.parseInt(wl.getHead())-1))) // separable verbs
                        .toList();

                result.add(new Sentence(tokenLemmas, words));
            }

            return result.stream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
