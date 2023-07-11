package hu.garaba.view2;

import hu.garaba.dictionary.DictionaryEntry;
import hu.garaba.model2.event.ModelEvent;
import hu.garaba.model2.event.SelectedWordChange;
import hu.garaba.util.EventSource;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Text;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DictionaryView {
    private final EventSource<ModelEvent> eventSource;
    private final HostServices hostServices;

    public DictionaryView(EventSource<ModelEvent> eventSource, HostServices hostServices) {
        this.eventSource = eventSource;
        this.hostServices = hostServices;

        eventSource.subscribe(e -> {
            if (e instanceof SelectedWordChange(String lemma, var dictionaryEntry, var sentenceView)) {
                setEntry(lemma, dictionaryEntry);
            }
        });
    }

    @FXML
    private Label wordLabel;

    @FXML
    private Hyperlink dictionaryLink;

    @FXML
    private Label grammarLabel;

    @FXML
    private TextInputControl searchField;

    @FXML
    private Text bodyText;

    @FXML
    public void onSearch() {

    }

    public void setEntry(String lemma, @Nullable DictionaryEntry entry) {
        Platform.runLater(() -> {
            wordLabel.setText(lemma);

            if (entry == null) {
                dictionaryLink.setOnAction(e -> {});
                grammarLabel.setText(null);
                bodyText.setText(null);
            } else {
                dictionaryLink.setOnAction(e -> hostServices.showDocument(entry.uri().toString()));
                grammarLabel.setText(entry.grammar());
                bodyText.setText(entry.text());
            }
        });
    }
}
