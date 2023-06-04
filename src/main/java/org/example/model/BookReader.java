package org.example.model;

import org.example.textprocessor.TextProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class BookReader {
    private final Path path;

    public BookReader(Path path) {
        this.path = path;
    }

    public Stream<Page> pageStream() throws IOException {
        TextProcessor textProcessor = new TextProcessor(Files.readString(path));
        return textProcessor.process();
    }
}
