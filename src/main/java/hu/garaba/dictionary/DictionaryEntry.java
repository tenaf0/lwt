package hu.garaba.dictionary;

import java.net.URI;

public record DictionaryEntry(String lemma, URI uri, String grammar, String /* TODO: replace with list of meanings */ text) {
}
