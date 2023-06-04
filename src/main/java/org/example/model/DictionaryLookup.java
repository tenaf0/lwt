package org.example.model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DictionaryLookup {
    public record DictionaryEntry(String lemma, String grammatik, List<String> meanings) {}

    private static final Map<String, DictionaryEntry> cache = new ConcurrentHashMap<>();

    public static DictionaryEntry lookup(String lemma) throws IOException {
        if (cache.containsKey(lemma)) {
            return cache.get(lemma);
        }

        Document doc = Jsoup.connect("https://www.dwds.de/wb/" + lemma).get();

        try {
            String title = doc.selectFirst(".dwdswb-ft-lemmaansatz > b:nth-child(1)").text();
            Element grammatik = doc.selectFirst("div.dwdswb-ft-block:nth-child(1) > span:nth-child(2)");
            Elements meanings = doc.select(".bedeutungsuebersicht > ol:nth-child(2) > li");

            DictionaryEntry entry = new DictionaryEntry(title, grammatik.text(), meanings.eachText());
            cache.put(lemma, entry);
            return entry;
        } catch (Exception e) {
            return null;
        }
    }
}
