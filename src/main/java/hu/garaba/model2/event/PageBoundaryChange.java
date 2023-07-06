package hu.garaba.model2.event;

import hu.garaba.buffer.PageReader2;

public record PageBoundaryChange(PageReader2.PageNo pageNo) implements ModelEvent {
}
