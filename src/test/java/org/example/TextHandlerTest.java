package org.example;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextHandlerTest {
    @Test
    void testSentences() {
        String hp = "So wies eine bekannte Studie der Harvard\n" +
                "University aus dem Jahr 2007 nach, dass";
        /*String hp = """
                Mr. und Mrs. Dursley im Ligusterweg Nummer 4 waren stolz
                darauf, ganz und gar normal zu sein, sehr stolz sogar. Niemand wäre
                auf die Idee gekommen, sie könnten sich in eine merkwürdige und
                geheimnisvolle Geschichte verstricken, denn mit solchem Unsinn
                wollten sie nichts zu tun haben.
                Mr. Dursley war Direktor einer Firma namens Grunnings, die
                Bohrmaschinen herstellte. Er war groß und bullig und hatte fast
                keinen Hals, dafür aber einen sehr großen Schnurrbart. Mrs.
                Dursley war dünn und blond und besaß doppelt so viel Hals, wie
                notwendig gewesen wäre, was allerdings sehr nützlich war, denn
                so konnte sie den Hals über den Gartenzaun recken und zu den
                Nachbarn hinüberspähen. Die Dursleys hatten einen kleinen Sohn
                namens Dudley und in ihren Augen gab es nirgendwo einen
                prächtigeren Jungen.
                """;*/
        TextHandler textHandler = new TextHandler(hp.replace('\n', ' '));

        List<String> list = textHandler.sentences().toList();
        list.forEach(System.out::println);

        try (InputStream modelIn = this.getClass().getResourceAsStream("/opennlp-de-token.bin")) {
            TokenizerModel model = new TokenizerModel(modelIn);
            TokenizerME detector = new TokenizerME(model);

            System.out.println("Tokenization:");
            String[] tokens = detector.tokenize(hp);
            System.out.println(Arrays.stream(tokens).toList());

            try (InputStream modelIn2 = this.getClass().getResourceAsStream("/de-pos-maxent.bin")) {
                POSModel model2 = new POSModel(modelIn2);
                POSTaggerME detector2 = new POSTaggerME(model2);

                String[] tags = detector2.tag(tokens);
                for (int i = 0; i < tokens.length; i++) {
                    System.out.println(tokens[i] + " " + tags[i]);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}