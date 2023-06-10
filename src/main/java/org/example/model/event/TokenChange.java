package org.example.model.event;

import org.example.model.TokenCoordinate;

import java.util.List;

public record TokenChange(List<TokenCoordinate> tokenChanges) implements ModelEvent {
}
