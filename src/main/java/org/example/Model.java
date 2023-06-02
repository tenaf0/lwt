package org.example;

import java.util.Arrays;
import java.util.stream.Stream;

public class Model {
    String text = """
            Ein Junge Überlebt
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
            prächtigeren Jungen
            """;



    public Stream<String> words() {
        return Arrays.stream(text.split(" +"));
    }
}
