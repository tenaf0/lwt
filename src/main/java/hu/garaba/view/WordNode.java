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
    private Label label;
    private boolean selected = false;

    public WordNode(TokenLemma tokenLemma) {
        label = new Label(tokenLemma.token());
        getChildren().add(label);

        setState(Model.WordState.UNKNOWN);
        label.setFont(new Font(14.0));
        label.setPadding(new Insets(0.2, 0.4, 0.2, 0.4));
        label.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.DEFAULT_WIDTHS)));

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
        label.getStyleClass().clear();
        label.getStyleClass().add(state.toString().toLowerCase());
        if (selected) {
            label.getStyleClass().add("selected");
        }
    }
}
