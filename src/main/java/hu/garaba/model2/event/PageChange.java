package hu.garaba.model2.event;

import hu.garaba.buffer.Page;

public record PageChange(Page page) implements ModelEvent {
    @Override
    public String toString() {
        return "PageChange[page=...]";
    }
}
