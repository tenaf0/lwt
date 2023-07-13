package hu.garaba.view2;

import hu.garaba.buffer.Page;
import hu.garaba.db.WordState;
import hu.garaba.model.CardEntry;
import hu.garaba.model.TokenCoordinate;
import hu.garaba.model2.PageView;
import hu.garaba.model2.ReadModel;
import hu.garaba.model2.event.SelectedWordChange;
import hu.garaba.view.EditCardBox;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MainWindow {
    private final ReadModel readModel;
    private final Supplier<Path> fileChooser;
    private final Supplier<Path> exportPathSupplier;

    public MainWindow(ReadModel readModel, Supplier<Path> fileChooser, Supplier<Path> exportPathSupplier) {
        this.readModel = readModel;
        this.fileChooser = fileChooser;
        this.exportPathSupplier = exportPathSupplier;
    }

    @FXML
    private Node mainWindow;

    @FXML
    private MenuBar menuBar;

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

        readModel.subscribe(e -> {
            if (e instanceof SelectedWordChange(var lemma, var dictionaryEntry,
                                                SelectedWordChange.SentenceView(var sentence, var wordStates, var word))) {
                sentenceView.setPage(new PageView(new Page(List.of(sentence)), wordStates));
                sentenceView.handleSelection(Set.of(), word.tokens().stream()
                        .map(i -> new TokenCoordinate(0, i))
                        .collect(Collectors.toSet()));
            }
        });

        mainWindow.setOnKeyReleased(e -> {
            if (e.isAltDown() && e.getCode() == KeyCode.LEFT) {
                readModel.prevPage();
            } else if (e.isAltDown() && e.getCode() == KeyCode.RIGHT) {
                readModel.nextPage();
            }
        });

        Menu settingsMenu = new Menu("Settings");
        Menu changeModel = new Menu("Change model");

        menuBar.getMenus().add(settingsMenu);
        settingsMenu.getItems().add(changeModel);

        changeModel.getItems().add(new MenuItem());

        changeModel.setOnShowing(e -> {
            changeModel.getItems().clear();

            readModel.getModels().forEach(m -> {
                MenuItem item = new MenuItem(m.displayName);
                if (readModel.getCurrentModel() == m)
                    item.setGraphic(new Circle(3.0, Color.BLACK));
                item.setOnAction(ev -> readModel.changeModel(m));
                changeModel.getItems().add(item);
            });
        });



    }

    @FXML
    public void onOpenFile() {
        Path path = fileChooser.get();
        readModel.open(path);
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
        res.ifPresent(readModel::open);
    }

    @FXML
    public void onExport() {

    }

    @FXML
    public void ignoreAction() {
        CardEntry cardEntry = editCardBoxController.collectCardEntryInfos().wordOnly();
        readModel.addWord(cardEntry, WordState.IGNORED);
    }

    @FXML
    public void learningAction() {
        CardEntry cardEntry = editCardBoxController.collectCardEntryInfos();
        readModel.addWord(cardEntry, WordState.LEARNING);
    }

    @FXML
    public void knownAction() {
        CardEntry cardEntry = editCardBoxController.collectCardEntryInfos().wordOnly();
        readModel.addWord(cardEntry, WordState.KNOWN);
    }
}
