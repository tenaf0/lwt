package hu.garaba.view;

import hu.garaba.db.WordState;
import hu.garaba.model.TokenCoordinate;
import hu.garaba.model2.PageView;
import hu.garaba.model2.ReadModel;
import hu.garaba.model2.event.ModelEvent;
import hu.garaba.model2.event.PageChange;
import hu.garaba.model2.event.SelectionChange;
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
            case PageChange(var n, var pageView) -> Platform.runLater(() -> onPageChange(pageView));
//            case WordStateChange(var changes) -> Platform.runLater(() -> onKnownChange(changes));
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
            default -> {}
        }
    }

    private void onPageChange(PageView pageView) {
        List<Sentence> sentences = pageView.page().sentences();

        this.textFlow.getChildren().clear();
        labels.clear();
        int tokenNo = 0;
        for (int s = 0; s < sentences.size(); s++) {
            List<WordNode> sentence = new ArrayList<>();
            labels.add(sentence);
            if (sentences.get(s).tokens().size() == 0 && this.textFlow.getChildren().size() > 0) {
                this.textFlow.getChildren().add(new Text("\n"));
            }
            for (int i = 0; i < sentences.get(s).tokens().size(); i++) {
                var token = sentences.get(s).tokens().get(i);

                WordNode label = new WordNode(token);
                label.setState(pageView.wordStates().get(tokenNo));
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

                tokenNo++;
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
