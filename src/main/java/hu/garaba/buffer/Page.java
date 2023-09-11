package hu.garaba.buffer;

import hu.garaba.textprocessor.Sentence;

import java.util.List;

public record Page(List<Sentence> sentences) {
}
