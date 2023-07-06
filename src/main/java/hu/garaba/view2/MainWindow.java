package hu.garaba.view2;

import hu.garaba.model.TokenCoordinate;
import hu.garaba.model2.ReadModel;
import hu.garaba.model2.event.SelectedSentenceChange;
import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public class MainWindow {
    private final ReadModel model;
    private final Supplier<Path> fileChooser;
    private final Supplier<Path> textDialog;

    public MainWindow(ReadModel model, Supplier<Path> fileChooser, Supplier<Path> textDialog) {
        this.model = model;
        this.fileChooser = fileChooser;
        this.textDialog = textDialog;
    }

    @FXML
    private TitledPane sentenceViewContent;

    @FXML
    public void initialize() {
        WordArea sentenceView = new WordArea();
        sentenceView.setPrefHeight(200.0);
        sentenceViewContent.setContent(sentenceView);

        model.subscribe(e -> {
            if (e instanceof SelectedSentenceChange(var pageView, var highlightedItems)) {
                sentenceView.setPage(pageView);
                sentenceView.handleSelection(List.of(), highlightedItems.stream().map(i -> new TokenCoordinate(0, i)).toList());
            }
        });
    }

    @FXML
    public void onOpenFile() {
        Path path = fileChooser.get();
        model.open(path);
    }

    @FXML
    public void onEnterText() {

    }

    @FXML
    public void onExport() {

    }
}
