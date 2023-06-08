package org.example.view;

import javafx.application.HostServices;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.example.model.CardEntry;
import org.example.model.DictionaryLookup;
import org.example.model.Model2;
import org.example.model.event.DictionaryChange;
import org.example.model.event.KnownChange;
import org.example.model.event.PageChange;
import org.example.model.event.TokenChange;
import org.example.model.util.Debouncer;

public class DictionaryPane extends AnchorPane {
    private final Model2 model;
    private final HostServices hostServices;

    public DictionaryPane(Model2 model, HostServices hostServices) {
        this.model = model;
        this.hostServices = hostServices;

        model.subscribe(e -> {
            switch (e) {
                case PageChange pageChange -> {
                }
                case TokenChange tokenChange -> {
                }
                case KnownChange knownChange -> {
                }
                case DictionaryChange(DictionaryLookup.DictionaryEntry entry)  -> {
                    word.set(entry.lemma());
                    grammarCategory.set(entry.grammatik());
                    dictionaryText.getChildren().clear();
                    dictionaryText.getChildren().add(new Text(entry.declensions() + '\n'));
                    if (entry.meanings() != null) {
                        entry.meanings().forEach(m -> dictionaryText.getChildren().add(new Text(m + '\n')));
                    }
                }
            }
        });
    }

    private final StringProperty word = new SimpleStringProperty();
    private final StringProperty grammarCategory = new SimpleStringProperty();

    @FXML
    private Label wordLabel;

    @FXML
    private Hyperlink dictionaryLinkDWDS;

    @FXML
    private Hyperlink dictionaryLinkCollins;

    @FXML
    private Label grammarCategoryLabel;

    @FXML
    private TextField searchField;

    @FXML
    private TextFlow dictionaryText;

    @FXML
    private EditCardBox editCardBoxController;

    private final Debouncer debouncer = new Debouncer();

    public void initialize() {
        wordLabel.textProperty().bind(word);
        dictionaryLinkDWDS.visibleProperty().bind(word.isEmpty().not());
        dictionaryLinkCollins.visibleProperty().bind(word.isEmpty().not());
        dictionaryLinkDWDS.setOnAction(e -> hostServices.showDocument(DictionaryLookup.lookupURL(word.get(), DictionaryLookup.Dictionary.DWDS)));
        dictionaryLinkCollins.setOnAction(e -> hostServices.showDocument(DictionaryLookup.lookupURL(word.get(), DictionaryLookup.Dictionary.Collins)));
        grammarCategoryLabel.textProperty().bind(grammarCategory);

    }

    @FXML
    public void onSearch() {
        debouncer.debounce(() -> model.selectWord(searchField.getText()), 500);
    }

    @FXML
    public void ignoreAction() {
        CardEntry cardEntry = editCardBoxController.collectCardEntryInfos().wordOnly();
        System.out.println(cardEntry);
//        model.addWord(cardEntry, Model.WordState.IGNORED);
    }

    @FXML
    public void learningAction() {
        CardEntry cardEntry = editCardBoxController.collectCardEntryInfos();
        System.out.println(cardEntry);
//        model.addWord(cardEntry, Model.WordState.LEARNING);
    }

    @FXML
    public void knownAction() {
        CardEntry cardEntry = editCardBoxController.collectCardEntryInfos().wordOnly();
        System.out.println(cardEntry);
//        model.addWord(cardEntry, Model.WordState.KNOWN);
    }
}
