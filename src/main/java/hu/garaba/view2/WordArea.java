package hu.garaba.view2;

import hu.garaba.buffer.Page;
import hu.garaba.model.TokenCoordinate;
import hu.garaba.model2.PageView;
import hu.garaba.textprocessor.Sentence;
import hu.garaba.textprocessor.TokenLemma;
import hu.garaba.view.WordNode;
import javafx.scene.text.TextFlow;

public class WordArea extends TextFlow {
    public WordArea() {

    }

    public void setPage(PageView pageView) {
        getChildren().clear();

        Page page = pageView.page();
        for (int s = 0; s < page.sentences().size(); s++) {
            Sentence sentence = page.sentences().get(s);
            for (int t = 0; t < sentence.tokens().size(); t++) {
                addToken(new TokenCoordinate(s, t), sentence.tokens().get(t));
            }
        }
    }

    private void addToken(TokenCoordinate coordinate, TokenLemma token) {
        getChildren().add(new WordNode(token));
    }
}
