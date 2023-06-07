package org.example.view;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.example.model.Model;

public class GUI extends Pane {

    private final Model model;

    public GUI(Model model) {
        this.model = model;
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
}
