package hu.garaba.model2.event;

import hu.garaba.dictionary.DictionaryLookup2;

import java.util.List;

public record SearchResults(List<DictionaryLookup2.SearchResult> resultList) implements ModelEvent {
}
