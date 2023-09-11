package hu.garaba.model2;

import hu.garaba.buffer.Page;
import hu.garaba.db.WordState;

import java.util.List;

public record PageView(Page page, List<WordState> wordStates) {}
