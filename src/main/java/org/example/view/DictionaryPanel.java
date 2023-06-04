package org.example.view;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.example.model.DictionaryLookup;

public class DictionaryPanel extends AnchorPane {
    private Label wordLabel;
    private ScrollPane scrollPane;

    public DictionaryPanel() {
        VBox vbox = new VBox();
        wordLabel = new Label();
        wordLabel.setFont(Font.font("", FontWeight.BOLD, 15.0));
        scrollPane = new ScrollPane();
        scrollPane.setPrefSize(340.0, 500.0);
        vbox.getChildren().addAll(wordLabel, scrollPane);
        vbox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        AnchorPane.setTopAnchor(vbox, 0.0);
        AnchorPane.setLeftAnchor(vbox, 0.0);
        AnchorPane.setRightAnchor(vbox, 0.0);
        AnchorPane.setBottomAnchor(vbox, 0.0);

        getChildren().add(vbox);
    }

    public void setDictionaryEntry(DictionaryLookup.DictionaryEntry entry) {
        if (entry == null) {
            return;
        }

        wordLabel.setText(entry.lemma());
        GridPane table = new GridPane();
        scrollPane.setContent(table);
        Text text = new Text(entry.grammatik());
        text.setWrappingWidth(300.0);
        table.add(text, 0, 0);

        int i = 1;
        for (var m : entry.meanings()) {
            Text meaning = new Text(m);
            meaning.setWrappingWidth(300.0);
            table.add(meaning, 0, i);
            i++;
        }
    }
}
