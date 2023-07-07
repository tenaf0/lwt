package hu.garaba.model2.event;

import hu.garaba.model.TokenCoordinate;

import java.util.Set;

public record SelectionChange(Set<TokenCoordinate> oldSelection, Set<TokenCoordinate> newSelection) implements ModelEvent {
}
