package hu.garaba.model2.event;

import hu.garaba.model2.PageView;

public record PageChange(int n, PageView page) implements ModelEvent {
    @Override
    public String toString() {
        return "PageChange[n=" + n + ", page=...]";
    }
}
