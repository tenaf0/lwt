package org.example.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.example.model.CardEntry;
import org.example.model.Model;
import org.example.model.SelectedWord;
import org.example.model.event.SelectedWordChange;

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
                case SelectedWordChange(SelectedWord selectedWord) -> {
                    reset();

                    wordField.setText(selectedWord.lemma());

                    if (selectedWord.dictionaryEntry() == null || selectedWord.dictionaryEntry().grammatik() == null)
                        return;

                    var grammatik = selectedWord.dictionaryEntry().grammatik();
                    Pattern regex = Pattern.compile("(masculine|feminine|neuter) noun");
                    Matcher matcher = regex.matcher(grammatik);
                    if (matcher.matches()) {
                        prefixField.setText(switch (matcher.group(1)) {
                            case "masculine" -> "der";
                            case "feminine" -> "die";
                            case "neuter" -> "das";
                            default -> throw new IllegalArgumentException();
                        });
                    }
                }
                default -> {}
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
