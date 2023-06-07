package org.example.view;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import org.example.model.*;
import org.example.model.event.*;

import java.util.ArrayList;
import java.util.List;

public class WordArea extends AnchorPane {
    private Model model;
    private FlowPane flowPane;

    private List<WordNode> labels = new ArrayList<>();

    public WordArea(Model model) {
        this.model = model;
        model.subscribe(this::onChange);

        getStylesheets().add("style/word-node.css");

        setPadding(new Insets(4));

        flowPane = new FlowPane();
        flowPane.prefWrapLengthProperty().bind(widthProperty().subtract(30));
        flowPane.setPadding(new Insets(3.0));
        flowPane.setHgap(5.0);
        flowPane.setVgap(5.0);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(flowPane);

        AnchorPane.setTopAnchor(scrollPane, 0.0);
        AnchorPane.setLeftAnchor(scrollPane, 0.0);
        AnchorPane.setRightAnchor(scrollPane, 0.0);
        AnchorPane.setBottomAnchor(scrollPane, 0.0);

        getChildren().add(scrollPane);
    }

    private void onChange(ModelEvent change) {
        switch (change) {
            case PageChange(var page) -> onPageChange(page);
            case DictionaryChange dc -> {}
            case KnownChange k -> onKnownChange();
            case TokenChange(var tokenList) -> {
                labels.forEach(WordNode::deselect);
                tokenList.forEach(i -> labels.get(i).select());
            }
        }
    }

    private void onPageChange(Page page) {
        this.flowPane.getChildren().clear();
        labels.clear();
        for (int i = 0; i < page.tokenList().size(); i++) {
            var token = page.tokenList().get(i);

            WordNode label = new WordNode(token.token());
            int finalI = i;
            label.setOnMouseClicked(e -> {
                if (e.isShiftDown()) {
                    model.toggleToken(finalI);
                } else {
                    model.selectWord(finalI);
                }
            });
            this.flowPane.getChildren().add(label);
            labels.add(label);
        }

        onKnownChange();
    }

    private void onKnownChange() {
        for (int i = 0; i < labels.size(); i++) {
            var label = labels.get(i);
            label.setState(model.isKnown(i));
        }
    }
}
