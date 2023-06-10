package org.example.model.event;

import org.example.model.page.Page;

public record PageChange(Page newPage) implements ModelEvent {
}
