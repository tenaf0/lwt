package hu.garaba.model2.event;

import hu.garaba.db.WordState;
import hu.garaba.model.TokenCoordinate;
import hu.garaba.util.Pair;

import java.util.List;

public record WordStateChange(List<Pair<TokenCoordinate, WordState>> wordStateChanges) implements ModelEvent {
    @Override
    public String toString() {
        return "WordStateChange[wordStateChanges=[size=" + wordStateChanges.size() + "]]";
    }
}
