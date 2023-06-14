package hu.garaba.model.event;

import hu.garaba.model.TokenCoordinate;

import java.util.List;

public record TokenChange(List<TokenCoordinate> tokenChanges) implements ModelEvent {
}
