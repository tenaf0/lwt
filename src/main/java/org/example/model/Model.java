package org.example.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Model {

    private Page page;
    private List<Integer> selectedTokens = new ArrayList<>();
    private Word selectedWord = null;

    private List<Consumer<ModelChange>> changeHandlers = new ArrayList<>();

    public void subscribe(Consumer<ModelChange> pageChangeHandler) {
        this.changeHandlers.add(pageChangeHandler);
    }

    public void setPage(Page page) {
        this.page = page;
        deselectAll();
        sendEvent(new PageChange(page));
    }

    public void selectToken(int i) {
        this.selectedTokens.add(i);
        sendEvent(new TokenChange(List.copyOf(selectedTokens)));
    }

    public void selectWord(int i) {
        selectedWord = getWord(i);
        sendEvent(new TokenChange(selectedWord.tokens()));
        sendEvent(new WordChange(selectedWord.asLemma(page.tokenList())));
    }

    public void deselectAll() {
        selectedTokens.clear();
        sendEvent(new TokenChange(List.of()));
    }

    public Word getWord(int i) {
        List<Word> words = page.words().stream().filter(w -> w.tokens().contains(i)).toList();
        if (words.size() > 1) {
            throw new IllegalStateException("Multiple words containing token " + i + " found " + words);
        }

        if (!words.isEmpty()) {
            return words.get(0);
//            words.get(0).tokens().forEach(this::selectToken);
        } else {
            return new Word(List.of(i));
//            selectToken(i);
        }
    }

    public enum WordState {
        KNOWN, UNKNOWN, IGNORED
    }

    private List<String> knownLemmas = new ArrayList<>(List.of("sein", "sehr", "Idee", "kommen"));

    public WordState isKnown(int i) {
        String token = page.tokenList().get(i).token();
        if (List.of(",", ".", "-", ";", "?", "!").contains(token)) {
            return WordState.IGNORED;
        }

        Word word = getWord(i);

        return knownLemmas.contains(word.asLemma(page.tokenList())) ? WordState.KNOWN : WordState.UNKNOWN;
    }

    public void addKnownWord(int i) {
        knownLemmas.add(getWord(i).asLemma(page.tokenList()));
        sendEvent(new KnownChange());
    }

    private void sendEvent(ModelChange change) {
        System.out.println("-> " + change);
        for (var handler : changeHandlers) {
            handler.accept(change);
        }
    }

    public static String text = """
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
            prächtigeren Jungen. Ich stehe um 7 auf.
            """;
}
