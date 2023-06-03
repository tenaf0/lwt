package org.example.model;

import java.util.List;

public record TokenChange(List<Integer> tokenChanges) implements ModelChange {
}
