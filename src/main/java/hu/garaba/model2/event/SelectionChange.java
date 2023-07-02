package hu.garaba.model2.event;

import hu.garaba.model.TokenCoordinate;

import java.util.List;

public record SelectionChange(List<TokenCoordinate> oldSelection, List<TokenCoordinate> newSelection) implements ModelEvent {
}
