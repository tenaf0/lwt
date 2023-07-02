package hu.garaba.view;

import hu.garaba.buffer.Page;
import hu.garaba.db.WordState;
import hu.garaba.model.TokenCoordinate;
import hu.garaba.model2.ReadModel;
import hu.garaba.model2.event.*;
import hu.garaba.textprocessor.Sentence;
import hu.garaba.util.Pair;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

public class WordArea extends AnchorPane {
    private final ReadModel model;
    private final TextFlow textFlow;

    private final List<List<WordNode>> labels = new ArrayList<>();

    public WordArea(ReadModel model) {
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
            case PageChange(var page) -> Platform.runLater(() -> onPageChange(page));
            case WordStateChange(var changes) -> Platform.runLater(() -> onKnownChange(changes));
            case JoinedEvent(var list) when isPageAndWordStateChange(list) -> Platform.runLater(() -> {
                onPageChange(((PageChange) list.get(0)).page());
                onKnownChange(((WordStateChange) list.get(1)).wordStateChanges());
            });
            case JoinedEvent(var list) -> list.forEach(this::onChange);
            case SelectionChange(var oldTokens, var newTokens) -> {
                Platform.runLater(() -> {
                    for (var old : oldTokens) {
                        labels.get(old.sentenceNo()).get(old.tokenNo()).deselect();
                    }
                    for (var n : newTokens) {
                        labels.get(n.sentenceNo()).get(n.tokenNo()).select();
                    }
                });
            }
//            case KnownChange k -> onKnownChange();
//            case TokenChange(var tokenList) -> {
//                labels.forEach(s -> s.forEach(WordNode::deselect));
//                tokenList.forEach(s -> labels.get(s.sentenceNo()).get(s.tokenNo()).select());
//            }
            default -> {}
        }
    }

    private boolean isPageAndWordStateChange(List<ModelEvent> events) {
        return events.size() == 2 && events.get(0) instanceof PageChange && events.get(1) instanceof WordStateChange;
    }

    private void onPageChange(Page page) {
        List<Sentence> sentences = page.sentences();

        this.textFlow.getChildren().clear();
        labels.clear();
        for (int s = 0; s < sentences.size(); s++) {
            List<WordNode> sentence = new ArrayList<>();
            labels.add(sentence);
            if (sentences.get(s).tokens().size() == 0 && this.textFlow.getChildren().size() > 0) {
                this.textFlow.getChildren().add(new Text("\n"));
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
    }

    private void onKnownChange(List<Pair<TokenCoordinate, WordState>> changes) {
        for (var c : changes) {
            WordNode label = labels.get(c.fst().sentenceNo()).get(c.fst().tokenNo());
            label.setState(c.snd());
        }
    }
}
