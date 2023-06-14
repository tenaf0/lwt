package hu.garaba.model.event;

import hu.garaba.model.page.Page;

public record PageChange(Page newPage) implements ModelEvent {
}
