package org.example.model.event;

import org.example.model.Model;

public record StateChange(Model.ModelState newState) implements ModelEvent { }
