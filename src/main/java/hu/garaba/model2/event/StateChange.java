package hu.garaba.model2.event;

import hu.garaba.model2.ReadModel;

public record StateChange(ReadModel.ReadModelState state) implements ModelEvent {
}
