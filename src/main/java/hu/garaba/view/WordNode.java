package hu.garaba.view;

import hu.garaba.db.WordState;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import hu.garaba.textprocessor.TokenLemma;

public class WordNode extends Region {
    private WordState state;
    private boolean selected = false;
    private final Label textLabel;

    public WordNode(TokenLemma tokenLemma) {
        textLabel = new Label(tokenLemma.token());
        getChildren().add(textLabel);

        setState(WordState.UNKNOWN);
        textLabel.setFont(new Font(14.0));
        textLabel.setPadding(new Insets(0.2, tokenLemma.spaceAfter() ? 0.35 : 0, 0.2, 0.35));
        textLabel.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.DEFAULT_WIDTHS)));

        setPadding(new Insets(0.2, tokenLemma.spaceAfter() ? 3.7 : 0, 0.2, 1));

    }

    public void setState(WordState state) {
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
