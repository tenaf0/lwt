package org.example.view;

import javafx.scene.Group;
import javafx.scene.layout.FlowPane;
import org.example.model.*;

import java.util.ArrayList;
import java.util.List;

public class WordArea extends Group {
    private Model model;
    private FlowPane flowPane;

    private List<WordNode> labels = new ArrayList<>();

    public WordArea(Model model) {
        this.model = model;
        model.subscribe(this::onChange);

        getStylesheets().add("style/word-node.css");

        flowPane = new FlowPane();
        flowPane.setHgap(5.0);
        flowPane.setVgap(5.0);

        getChildren().add(flowPane);
    }

    private void onChange(ModelChange change) {
        switch (change) {
            case PageChange(var page) -> onPageChange(page);
            case WordChange(var word) -> {}
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
                if (e.getClickCount() == 2) {
                    model.addKnownWord(finalI);
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
