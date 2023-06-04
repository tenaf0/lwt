package org.example.model.event;

import java.util.List;

public record TokenChange(List<Integer> tokenChanges) implements ModelEvent {
}
