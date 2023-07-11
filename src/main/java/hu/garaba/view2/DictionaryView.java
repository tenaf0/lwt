package hu.garaba.view2;

import hu.garaba.dictionary.DictionaryEntry;
import hu.garaba.model2.event.DictionaryWordChange;
import hu.garaba.model2.event.ModelEvent;
import hu.garaba.util.EventSource;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Text;

public class DictionaryView {
    private final EventSource<ModelEvent> eventSource;
    private final HostServices hostServices;

    public DictionaryView(EventSource<ModelEvent> eventSource, HostServices hostServices) {
        this.eventSource = eventSource;
        this.hostServices = hostServices;

        eventSource.subscribe(e -> {
            if (e instanceof DictionaryWordChange(var newEntry)) {
                setEntry(newEntry);
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

    public void setEntry(DictionaryEntry entry) {
        Platform.runLater(() -> {
            wordLabel.setText(entry.lemma());
            dictionaryLink.setOnAction(e -> hostServices.showDocument(entry.uri().toString()));
            grammarLabel.setText(entry.grammar());
            bodyText.setText(entry.text());
        });
    }
}
