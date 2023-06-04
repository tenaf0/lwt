package org.example.view;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.model.DictionaryLookup;
import org.example.model.Model;
import org.example.model.event.WordChange;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ControlPanel extends AnchorPane {
    private final Model model;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();


    public ControlPanel(Model model) {
        this.model = model;

        DictionaryPanel dictionaryPanel = new DictionaryPanel();

        model.subscribe(e -> {
            switch (e) {
                case WordChange(var word) -> {
                    executorService.submit(() -> {
                        try {
                            DictionaryLookup.DictionaryEntry entry = DictionaryLookup.lookup(word);
                            Platform.runLater(() -> dictionaryPanel.setDictionaryEntry(entry));
                        } catch (IOException exc) {
                            exc.printStackTrace(System.err);
                        }
                    });
                }
                default -> {}
            }
        });

        var ignoreButton = new Button("Ignore");
        ignoreButton.setOnAction(e -> model.addWord(Model.WordState.IGNORED));
        var learningButton = new Button("Learning");
        learningButton.setOnAction(e -> model.addWord(Model.WordState.LEARNING));
        var knownButton = new Button("Known");
        knownButton.setOnAction(e -> model.addWord(Model.WordState.KNOWN));

        HBox vbox = new HBox(ignoreButton, learningButton, knownButton);
        VBox hbox = new VBox(dictionaryPanel, vbox);
        getChildren().add(hbox);
    }
}
