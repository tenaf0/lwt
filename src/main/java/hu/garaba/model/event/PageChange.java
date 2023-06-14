package hu.garaba.model.event;

import hu.garaba.buffer.Page;

public record PageChange(Page newPage) implements ModelEvent {
}
