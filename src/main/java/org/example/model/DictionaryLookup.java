package org.example.model;

import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DictionaryLookup {
    public record DictionaryEntry(String lemma, String grammatik, String declensions, List<String> meanings) {}

    private static final Map<String, DictionaryEntry> cache = new ConcurrentHashMap<>();

    public enum Dictionary {
        DWDS,
        Collins
    }

    public static String lookupURL(String lemma, Dictionary dictionary) {
        return (dictionary == Dictionary.DWDS ? "https://www.dwds.de/wb/" : "https://www.collinsdictionary.com/dictionary/german-english/") + germanToURL(lemma);
    }

    public static @Nullable DictionaryEntry lookup(String lemma) throws IOException {
        if (cache.containsKey(lemma)) {
            return cache.get(lemma);
        }

        String url = lookupURL(lemma, Dictionary.Collins);
        System.out.println("Connecting to " + url);
        Document doc = Jsoup.connect(url).userAgent("Mozilla").get();

        try {
            String title = doc.selectFirst("span.orth:nth-child(1)").text();
            Element grammatik = doc.selectFirst(".pos");
            Element declensions = doc.selectFirst("span.inflected_forms");
            Elements meanings = doc.select("div.sense");

            DictionaryEntry entry = new DictionaryEntry(title, grammatik.text(), declensions != null ? declensions.text() : null, meanings.eachText());
            cache.put(lemma, entry);
            return entry;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    private static String germanToURL(String german) {
        return german
                .toLowerCase()
                .replace('ä', 'a')
                .replace('ö', 'o')
                .replace('ü', 'u')
                .replace("ß", "ss");
    }
}
