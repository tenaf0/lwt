package hu.garaba.model2.event;

import hu.garaba.model2.PageView;

public record PageChange(PageView page) implements ModelEvent {
    @Override
    public String toString() {
        return "PageChange[page=...]";
    }
}
