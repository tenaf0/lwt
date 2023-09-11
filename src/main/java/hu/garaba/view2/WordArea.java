package hu.garaba.view2;

import hu.garaba.buffer.Page;
import hu.garaba.db.WordState;
import hu.garaba.model.TokenCoordinate;
import hu.garaba.model2.PageView;
import hu.garaba.textprocessor.Sentence;
import hu.garaba.textprocessor.TokenLemma;
import hu.garaba.util.Pair;
import hu.garaba.view.WordNode;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class WordArea extends ScrollPane {

    private @Nullable Consumer<TokenCoordinate> selectionEventHandler;
    private final TextFlow textFlow = new TextFlow();

    private Map<TokenCoordinate, WordNode> tokenMap = new HashMap<>();

    public WordArea(Consumer<TokenCoordinate> selectionEventHandler) {
        this.selectionEventHandler = selectionEventHandler;

        textFlow.setLineSpacing(4.5);
        textFlow.setPadding(new Insets(3.0));

	    setContent(textFlow);
	    setFitToWidth(true);

        getStylesheets().add("style/word-node.css");
    }

    public WordArea() {
        this(null);
    }

    public void setPage(PageView pageView) {
        Platform.runLater(() -> {
            textFlow.getChildren().clear();
            tokenMap.clear();

            Page page = pageView.page();
            Iterator<WordState> wordStateIterator = pageView.wordStates().iterator();
            for (int s = 0; s < page.sentences().size(); s++) {
                Sentence sentence = page.sentences().get(s);
                if (sentence.tokens().size() == 0) {
                    addNewline();
                    continue;
                }
                for (int t = 0; t < sentence.tokens().size(); t++) {
                    addToken(new TokenCoordinate(s, t), sentence.tokens().get(t), wordStateIterator.next());
                }
            }
        });
    }

    public void handleSelection(Set<TokenCoordinate> oldSelection, Set<TokenCoordinate> newSelection) {
        Platform.runLater(() -> {
            oldSelection.stream()
                    .map(tc -> tokenMap.get(tc))
                    .filter(Objects::nonNull)
                    .forEach(n -> setTokenSelection(n, false));

            newSelection.stream()
                    .map(tc -> tokenMap.get(tc))
                    .forEach(n -> setTokenSelection(n, true));
        });

    }

    private void setTokenSelection(WordNode node, boolean isSelected) {
        if (isSelected) {
            node.select();
        } else {
            node.deselect();
        }
    }

    public void handleWordStateChange(List<Pair<TokenCoordinate, WordState>> wordStateChanges) {
        Platform.runLater(() -> {
            wordStateChanges.stream().forEach(pair -> tokenMap.get(pair.fst()).setState(pair.snd()));
        });
    }

    private void addToken(TokenCoordinate coordinate, TokenLemma token, WordState state) {
        WordNode wordNode = new WordNode(token);
        Tooltip.install(wordNode, new Tooltip(token.lemma()));
        wordNode.setState(state);
        if (selectionEventHandler != null) {
            wordNode.setOnMouseClicked(e -> selectionEventHandler.accept(coordinate));
        }

        tokenMap.put(coordinate, wordNode);
        textFlow.getChildren().add(wordNode);
    }

    private void addNewline() {
        textFlow.getChildren().add(new Text("\n"));
    }
}
