package hu.garaba.dictionary;

import java.io.IOException;
import java.util.List;

public interface DictionaryLookup2 {
    record SearchResult(String label, String entryId) {}
    List<SearchResult> search(String word) throws IOException;

    DictionaryEntry lookup(String word) throws IOException;

    DictionaryEntry selectById(String entryId) throws IOException;
}
