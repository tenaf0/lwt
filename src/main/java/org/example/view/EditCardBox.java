package org.example.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.example.model.CardEntry;
import org.example.model.DictionaryLookup;
import org.example.model.Model;
import org.example.model.event.DictionaryChange;
import org.example.model.event.KnownChange;
import org.example.model.event.PageChange;
import org.example.model.event.TokenChange;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditCardBox {

    private final Model model;

    public EditCardBox(Model model) {
        this.model = model;
    }

    @FXML
    private ChoiceBox<?> exampleSentenceChoiceBox;

    @FXML
    private CheckBox exampleSentenceCustomCheckbox;

    @FXML
    private HBox exampleSentenceHBox;

    @FXML
    private TextField meaningField;

    @FXML
    private TextField noteField;

    @FXML
    private TextField postfixField;

    @FXML
    private TextField prefixField;

    @FXML
    private TextField wordField;

    @FXML
    public void initialize() {
        model.subscribe(e -> {
            switch (e) {
                case PageChange pageChange -> {
                }
                case TokenChange tokenChange -> {
                }
                case KnownChange knownChange -> {
                }
                case DictionaryChange(DictionaryLookup.DictionaryEntry entry)  -> {
                    reset();

                    wordField.setText(entry.lemma());

                    if (entry.grammatik() == null)
                        return;

                    Pattern regex = Pattern.compile("(masculine|feminine|neuter) noun");
                    Matcher matcher = regex.matcher(entry.grammatik());
                    if (matcher.matches()) {
                        prefixField.setText(switch (matcher.group(1)) {
                            case "masculine" -> "der";
                            case "feminine" -> "die";
                            case "neuter" -> "das";
                            default -> throw new IllegalArgumentException();
                        });
                    }
                }
            }
        });
    }

    private void reset() {
        prefixField.setText(null);
        wordField.setText(null);
        postfixField.setText(null);
        meaningField.setText(null);
        noteField.setText(null);
    }

    public CardEntry collectCardEntryInfos() {
        String prefix = prefixField.getText();
        String word = wordField.getText();
        String postfix = postfixField.getText();
        String meaning = meaningField.getText();
        String note = noteField.getText();

        return new CardEntry(prefix, word, postfix, meaning, note, null);
    }

    @FXML
    void onExampleSentenceCustomToggle(ActionEvent event) {
        if (exampleSentenceCustomCheckbox.isSelected()) {
            exampleSentenceHBox.getChildren().remove(0);
            TextField customSentenceField = new TextField();
            HBox.setHgrow(customSentenceField, Priority.ALWAYS);
            exampleSentenceHBox.getChildren().add(0, customSentenceField);
        } else {
            exampleSentenceHBox.getChildren().remove(0);
            exampleSentenceHBox.getChildren().add(0, exampleSentenceChoiceBox);
        }
    }

}
