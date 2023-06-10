package org.example.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.example.model.Model;
import org.example.model.event.StateChange;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;

public class GUI extends Pane {

    private final Model model;
    private Model.ModelState modelState = Model.ModelState.EMPTY;
    private final WordArea wordArea;

    private final Supplier<Path> openPathSupplier;
    private final Supplier<Path> exportPathSupplier;

    public GUI(Model model, Supplier<Path> openPathSupplier, Supplier<Path> exportPathSupplier) {
        this.model = model;
        this.wordArea = new WordArea(model);

        this.openPathSupplier = openPathSupplier;
        this.exportPathSupplier = exportPathSupplier;

        model.subscribe(e -> {
            if (e instanceof StateChange(var newState)) {
                Model.ModelState prevState = modelState;
                modelState = newState;

                if (prevState == newState) {
                    return;
                }

//                leftContainer.getChildren().clear();
                if (newState == Model.ModelState.EMPTY) {
                    stateDescription.setVisible(true);
                    wordArea.setVisible(false);
                    stateDescription.setText("No document is loaded");
                } else if (newState == Model.ModelState.LOADED) {
                    stateDescription.setVisible(false);
                    wordArea.setVisible(true);
                } else if (newState == Model.ModelState.LOADING) {
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
    public void onOpenText() {
        model.openText(openPathSupplier.get());
    }

    @FXML
    public void nextPage() {
        model.changePage(true);
    }

    @FXML
    public void prevPage() {
        model.changePage(false);
    }

    @FXML
    public void onExport() {
        try {
            model.exportRows(exportPathSupplier.get());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
