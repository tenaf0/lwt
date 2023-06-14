package hu.garaba.view;

import hu.garaba.model.Model;
import hu.garaba.model.TokenCoordinate;
import hu.garaba.model.event.*;
import hu.garaba.buffer.Page;
import hu.garaba.buffer.Sentence;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

public class WordArea extends AnchorPane {
    private final Model model;
    private final TextFlow textFlow;

    private final List<List<WordNode>> labels = new ArrayList<>();

    public WordArea(Model model) {
        this.model = model;
        model.subscribe(this::onChange);

        getStylesheets().add("style/word-node.css");

        setPadding(new Insets(4));

        textFlow = new TextFlow();
        textFlow.setLineSpacing(4.5);
        textFlow.setPadding(new Insets(3.0));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(textFlow);

        AnchorPane.setTopAnchor(scrollPane, 0.0);
        AnchorPane.setLeftAnchor(scrollPane, 0.0);
        AnchorPane.setRightAnchor(scrollPane, 0.0);
        AnchorPane.setBottomAnchor(scrollPane, 0.0);

        getChildren().add(scrollPane);
    }

    private void onChange(ModelEvent change) {
        switch (change) {
            case PageChange(var page) -> onPageChange(page);
            case SelectedWordChange dc -> {}
            case KnownChange k -> onKnownChange();
            case TokenChange(var tokenList) -> {
                labels.forEach(s -> s.forEach(WordNode::deselect));
                tokenList.forEach(s -> labels.get(s.sentenceNo()).get(s.tokenNo()).select());
            }
            case StateChange stateChange -> {}
        }
    }

    private void onPageChange(Page page) {
        List<Sentence> sentences = page.sentences();

        this.textFlow.getChildren().clear();
        labels.clear();
        for (int s = 0; s < sentences.size(); s++) {
            List<WordNode> sentence = new ArrayList<>();
            labels.add(sentence);
            if (sentences.get(s).tokens().size() == 0) {
                new Text("\n");
            }
            for (int i = 0; i < sentences.get(s).tokens().size(); i++) {
                var token = sentences.get(s).tokens().get(i);

                WordNode label = new WordNode(token);
                int finalS = s;
                int finalI = i;
                label.setOnMouseClicked(e -> {
                    if (e.isShiftDown()) {
//                    model.toggleToken(finalI);
                    } else {
                        model.selectWord(new TokenCoordinate(finalS, finalI));
                    }
                });
                this.textFlow.getChildren().add(label);
                sentence.add(label);
            }
        }

        onKnownChange();
    }

    private void onKnownChange() {
        for (int s = 0; s < labels.size(); s++) {
            for (int i = 0; i < labels.get(s).size(); i++) {
                var label = labels.get(s).get(i);
                label.setState(model.isKnown(new TokenCoordinate(s, i)));
            }
        }
    }
}
