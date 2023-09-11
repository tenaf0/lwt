package hu.garaba.view;

import hu.garaba.model2.ReadModel;
import hu.garaba.model2.event.StateChange;
import hu.garaba.textprocessor.TextProcessor;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

public class GUI extends Pane {

    private final ReadModel model;
    private final WordArea wordArea;

    private final Supplier<Path> openPathSupplier;
    private final Supplier<Path> exportPathSupplier;

    public GUI(ReadModel model, Supplier<Path> openPathSupplier, Supplier<Path> exportPathSupplier) {
        this.model = model;
        this.wordArea = new WordArea(model);

        this.openPathSupplier = openPathSupplier;
        this.exportPathSupplier = exportPathSupplier;

        model.subscribe(e -> {
            if (e instanceof StateChange(var newState)) {
                if (newState == ReadModel.ReadModelState.UNLOADED) {
                    stateDescription.setVisible(true);
                    wordArea.setVisible(false);
                    stateDescription.setText("No document is loaded");
                } else if (newState == ReadModel.ReadModelState.LOADED) {
                    stateDescription.setVisible(false);
                    wordArea.setVisible(true);
                } else if (newState == ReadModel.ReadModelState.LOADING) {
                    stateDescription.setVisible(true);
                    wordArea.setVisible(false);
                    stateDescription.setText("Loading...");
                }
            }
        });
    }

    @FXML
    public void initialize() {
        wordArea.setVisible(false);
        leftContainer.getChildren().add(wordArea);

//        modelChoice.setOnAction(e -> model.setModel(modelChoice.getValue()));
        modelChoice.setConverter(new StringConverter<>() {
            @Override
            public String toString(TextProcessor.TextProcessorModel model) {
                return model == null ? "" : model.displayName;
            }

            @Override
            public TextProcessor.TextProcessorModel fromString(String name) {
                return TextProcessor.TextProcessorModel.findByName(name);
            }
        });
        /*for (var model : model.getAvailableModels()) {
            modelChoice.getItems().add(model);
        }
        modelChoice.setValue(model.getModel());*/

        AnchorPane.setTopAnchor(wordArea, 0.0);
        AnchorPane.setLeftAnchor(wordArea, 0.0);
        AnchorPane.setRightAnchor(wordArea, 0.0);
        AnchorPane.setBottomAnchor(wordArea, 0.0);
    }

    @FXML
    private AnchorPane leftContainer;

    @FXML
    private Label stateDescription;

    @FXML
    private ChoiceBox<TextProcessor.TextProcessorModel> modelChoice;

    @FXML
    public void onOpenFile() {
        model.open(openPathSupplier.get());
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
        if (res.isPresent()) {
            model.open(res.get());
        }
    }

    @FXML
    public void nextPage() {
        model.nextPage();
    }

    @FXML
    public void prevPage() {
        model.prevPage();
    }

    @FXML
    public void onExport() {
//        model.exportRows(exportPathSupplier.get());
    }
}
