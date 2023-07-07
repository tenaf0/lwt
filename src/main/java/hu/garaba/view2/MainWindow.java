package hu.garaba.view2;

import hu.garaba.model.TokenCoordinate;
import hu.garaba.model2.ReadModel;
import hu.garaba.model2.event.SelectedSentenceChange;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
                sentenceView.handleSelection(Set.of(), highlightedItems.stream()
                        .map(i -> new TokenCoordinate(0, i))
                        .collect(Collectors.toSet()));
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
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Enter custom text");
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Enter", ButtonBar.ButtonData.OK_DONE),
                ButtonType.CANCEL
        );

        var textArea = new TextArea();

        dialog.getDialogPane().setContent(textArea);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton != ButtonType.CANCEL) {
                return textArea.getText();
            } else {
                return null;
            }
        });

        Optional<String> res = dialog.showAndWait();
        res.ifPresent(model::open);
    }

    @FXML
    public void onExport() {

    }
}
