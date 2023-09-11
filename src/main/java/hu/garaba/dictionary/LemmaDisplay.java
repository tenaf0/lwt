package hu.garaba.dictionary;

public record LemmaDisplay(String prefix, String lemma, String postfix) {
    public LemmaDisplay(String lemma) {
        this(null, lemma, null);
    }

    @Override
    public String toString() {
        return (prefix != null ? prefix + " " : "") + lemma + (postfix != null ? ", " + postfix : "");
    }
}
