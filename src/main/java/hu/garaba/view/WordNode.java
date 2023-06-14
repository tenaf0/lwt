package hu.garaba.view;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import hu.garaba.model.Model;
import hu.garaba.model.TokenLemma;

public class WordNode extends Region {
    private Model.WordState state;
    private boolean selected = false;
    private final Label textLabel;

    public WordNode(TokenLemma tokenLemma) {
        textLabel = new Label(tokenLemma.token());
        getChildren().add(textLabel);

        setState(Model.WordState.UNKNOWN);
        textLabel.setFont(new Font(14.0));
        textLabel.setPadding(new Insets(0.2, 0.4, 0.2, 0.4));
        textLabel.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.DEFAULT_WIDTHS)));

        setPadding(new Insets(0.2, 2.8, 0.2, 2.8));

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
        textLabel.getStyleClass().clear();
        textLabel.getStyleClass().add(state.toString().toLowerCase());
        if (selected) {
            textLabel.getStyleClass().add("selected");
        }
    }
}
