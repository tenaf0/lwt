package org.example.view;

import javafx.scene.control.Label;
import org.example.model.Model;
import org.example.textprocessor.TextProcessor;

public class WordNode extends Label {
    private Model.WordState state;
    private boolean selected = false;

    public WordNode(TextProcessor.TokenLemma tokenLemma) {
        super(tokenLemma.token());

        setState(Model.WordState.UNKNOWN);
    }

    public void setState(Model.WordState state) {
        this.state = state;

        setStyle();
    }

    public void select() {
        this.selected = true;

        setStyle();
    }

    public void deselect() {
        this.selected = false;

        setStyle();
    }

    private void setStyle() {
        getStyleClass().clear();
        getStyleClass().add(state.toString().toLowerCase());
        if (selected) {
            getStyleClass().add("selected");
        }
    }
}
