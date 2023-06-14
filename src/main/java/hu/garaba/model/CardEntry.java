package hu.garaba.model;

public record CardEntry(String prefix, String word, String postfix, String meaning, String note, String exampleSentence) {
    public CardEntry wordOnly() {
        return new CardEntry(null, word, null, null, null, null);
    }
}
