package org.example.view;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.example.model.Model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;

public class GUI extends Pane {

    private final Model model;
    private final Supplier<Path> pathSupplier;

    public GUI(Model model, Supplier<Path> pathSupplier) {
        this.model = model;
        this.pathSupplier = pathSupplier;
    }

    @FXML
    public void initialize() {
        WordArea wordArea = new WordArea(model);
        leftContainer.getChildren().add(wordArea);

        AnchorPane.setTopAnchor(wordArea, 0.0);
        AnchorPane.setLeftAnchor(wordArea, 0.0);
        AnchorPane.setRightAnchor(wordArea, 0.0);
        AnchorPane.setBottomAnchor(wordArea, 0.0);
    }

    @FXML
    private AnchorPane leftContainer;

    @FXML
    public void nextPage() {
        model.nextPage();
    }

    @FXML
    public void prevPage() {

    }

    @FXML
    public void onExport() {
        try {
            model.exportRows(pathSupplier.get());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
