package hu.garaba.view2;

import hu.garaba.dictionary.DictionaryEntry;
import hu.garaba.dictionary.DictionaryLookup2;
import hu.garaba.model2.ReadModel;
import hu.garaba.model2.event.SearchResults;
import hu.garaba.model2.event.SelectedWordChange;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DictionaryView {
    private final ReadModel readModel;
    private final HostServices hostServices;

    public DictionaryView(ReadModel readModel, HostServices hostServices) {
        this.readModel = readModel;
        this.hostServices = hostServices;

        readModel.subscribe(e -> {
            if (e instanceof SelectedWordChange(String lemma, var dictionaryEntry, var sentenceView)) {
                setEntry(lemma, dictionaryEntry);
            } else if (e instanceof SearchResults(var resultList)) {
                Platform.runLater(() -> {
                    container.getChildren().clear();

                    ListView<DictionaryLookup2.SearchResult> listView = createListView(resultList);
                    ScrollPane scrollPane = new ScrollPane(listView);
                    scrollPane.setFitToWidth(true);
                    container.getChildren().add(scrollPane);

                    AnchorPane.setTopAnchor(scrollPane, 0.0);
                    AnchorPane.setRightAnchor(scrollPane, 0.0);
                    AnchorPane.setBottomAnchor(scrollPane, 0.0);
                    AnchorPane.setLeftAnchor(scrollPane, 0.0);
                });
            }
        });
    }

    @NotNull
    private ListView<DictionaryLookup2.SearchResult> createListView(List<DictionaryLookup2.SearchResult> resultList) {
        ListView<DictionaryLookup2.SearchResult> listView = new ListView<>(FXCollections.observableList(resultList));
        listView.setCellFactory(lv -> {
            TextFieldListCell<DictionaryLookup2.SearchResult> cell = new TextFieldListCell<>();
            cell.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2) {
                    DictionaryLookup2.SearchResult selectedItem = listView.getSelectionModel().getSelectedItem();
                    if (selectedItem != null) {
                        readModel.selectDictionaryWord(selectedItem.entryId());
                    }
                }
            });
            cell.setConverter(new StringConverter<>() {
                private final Map<String, DictionaryLookup2.SearchResult> map = new HashMap<>();

                {
                    resultList.forEach(r -> map.put(r.label(), r));
                }

                @Override
                public String toString(DictionaryLookup2.SearchResult result) {
                    return result.label();
                }

                @Override
                public DictionaryLookup2.SearchResult fromString(String string) {
                    return map.get(string);
                }
            });
            return cell;
        });
        listView.setOnKeyReleased(ev -> {
            DictionaryLookup2.SearchResult selectedItem = listView.getSelectionModel().getSelectedItem();
            if (ev.getCode() == KeyCode.ENTER && selectedItem != null) {
                readModel.selectDictionaryWord(selectedItem.entryId());
            }
        });
        return listView;
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
    private AnchorPane container;

    @FXML
    private ScrollPane textScrollPane;

    @FXML
    private Text bodyText;

    @FXML
    public void onSearch() {
        readModel.search(searchField.getText());
    }

    public void setEntry(String lemma, @Nullable DictionaryEntry entry) {
        Platform.runLater(() -> {
            container.getChildren().clear();

            container.getChildren().add(textScrollPane);

            if (entry == null) {
                wordLabel.setText(lemma);
                dictionaryLink.setOnAction(e -> {});
                grammarLabel.setText(null);
                bodyText.setText(null);
            } else {
                wordLabel.setText(entry.lemma().toString()); // TODO: Can use different formatting for prefix vs lemma
                dictionaryLink.setOnAction(e -> hostServices.showDocument(entry.uri().toString()));
                grammarLabel.setText(entry.grammar());
                bodyText.setText(entry.text());
            }
        });
    }
}
