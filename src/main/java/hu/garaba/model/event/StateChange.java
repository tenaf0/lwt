package hu.garaba.model.event;

import hu.garaba.model.Model;

public record StateChange(Model.ModelState newState) implements ModelEvent { }
