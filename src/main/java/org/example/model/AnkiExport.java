package org.example.model;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class AnkiExport {
    public static void export(List<CardEntry> cardEntries, Path path) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(path);

        writer.write("#separator:Semicolon\n");
        writer.write("#html:false\n");
        writer.write("#tags:german,vocabulary\n");
        writer.write("#columns:word;prefix;postfix;meaning;note;exampleSentence\n");

        try (ICSVWriter csvWriter = new CSVWriterBuilder(writer)
                .withSeparator(';')
                .build()) {
            Stream<String[]> rowStream = cardEntries.stream().map(e -> new String[]{e.word(), e.prefix(), e.postfix(), e.meaning(), e.note(), e.exampleSentence()});
            csvWriter.writeAll(rowStream::iterator);
        }
    }
}
