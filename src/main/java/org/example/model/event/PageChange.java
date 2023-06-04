package org.example.model.event;

import org.example.model.Page;

public record PageChange(Page newPage) implements ModelEvent {
}
