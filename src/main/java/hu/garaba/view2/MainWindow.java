package hu.garaba.view2;

import hu.garaba.buffer.Page;
import hu.garaba.model.CardEntry;
import hu.garaba.model.TokenCoordinate;
import hu.garaba.model2.PageView;
import hu.garaba.model2.ReadModel;
import hu.garaba.model2.event.SelectedWordChange;
import hu.garaba.view.EditCardBox;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MainWindow {
    private final ReadModel model;
    private final Supplier<Path> fileChooser;
    private final Supplier<Path> exportPathSupplier;

    public MainWindow(ReadModel model, Supplier<Path> fileChooser, Supplier<Path> exportPathSupplier) {
        this.model = model;
        this.fileChooser = fileChooser;
        this.exportPathSupplier = exportPathSupplier;
    }

    @FXML
    private TitledPane dictionaryContent;

    @FXML
    private TitledPane sentenceViewContent;

    @FXML
    private EditCardBox editCardBoxController;

    @FXML
    public void initialize() {
        WordArea sentenceView = new WordArea();
        sentenceView.setPrefHeight(200.0);
        sentenceViewContent.setContent(sentenceView);

        model.subscribe(e -> {
            if (e instanceof SelectedWordChange(var lemma, var dictionaryEntry,
                                                SelectedWordChange.SentenceView(var sentence, var wordStates, var word))) {
                sentenceView.setPage(new PageView(new Page(List.of(sentence)), wordStates));
                sentenceView.handleSelection(Set.of(), word.tokens().stream()
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

    @FXML
    public void ignoreAction() {
        CardEntry cardEntry = editCardBoxController.collectCardEntryInfos().wordOnly();
//        model.addWord(cardEntry, Model.WordState.IGNORED);
    }

    @FXML
    public void learningAction() {
        CardEntry cardEntry = editCardBoxController.collectCardEntryInfos();
//        model.addWord(cardEntry, Model.WordState.LEARNING);
    }

    @FXML
    public void knownAction() {
        CardEntry cardEntry = editCardBoxController.collectCardEntryInfos().wordOnly();
//        model.addWord(cardEntry, Model.WordState.KNOWN);
    }
}
