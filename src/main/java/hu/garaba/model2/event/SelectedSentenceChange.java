package hu.garaba.model2.event;

import hu.garaba.model2.PageView;

import java.util.List;

public record SelectedSentenceChange(PageView pageView, List<Integer> highlightedTokens) implements ModelEvent {
}
