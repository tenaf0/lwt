package hu.garaba.dictionary;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public interface DictionaryLookup2 {
    record SearchResult(String label, URL url) {}
    List<SearchResult> search(String word) throws IOException;

    DictionaryEntry lookup(String word) throws IOException;
}
