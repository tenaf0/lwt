package hu.garaba.model2.event;

import java.util.List;

public record JoinedEvent(List<ModelEvent> events) implements ModelEvent {
}
