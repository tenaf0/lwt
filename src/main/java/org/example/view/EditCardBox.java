package org.example.view;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.example.model.CardEntry;
import org.example.model.Model;
import org.example.model.SelectedWord;
import org.example.model.TokenLemma;
import org.example.model.event.SelectedWordChange;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EditCardBox {

    private final Model model;

    public EditCardBox(Model model) {
        this.model = model;
    }

    @FXML
    private TextField prefixField;
    @FXML
    private TextField postfixField;
    @FXML
    private TextField wordField;

    @FXML
    private TextField exampleSentenceField;

    @FXML
    private TextField meaningField;

    @FXML
    private TextField noteField;



    @FXML
    public void initialize() {
        model.subscribe(e -> {
            switch (e) {
                case SelectedWordChange(SelectedWord selectedWord) -> {
                    reset();

                    wordField.setText(selectedWord.lemma());
                    if (selectedWord.sentence() != null) {
                        exampleSentenceField.setText(selectedWord.sentence().tokens()
                                .stream()
                                .map(TokenLemma::token)
                                .collect(Collectors.joining(" ")));
                    }

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
        exampleSentenceField.setText(null);
    }

    public CardEntry collectCardEntryInfos() {
        String prefix = prefixField.getText();
        String word = wordField.getText();
        String postfix = postfixField.getText();
        String meaning = meaningField.getText();
        String note = noteField.getText();
        String exampleSentence = exampleSentenceField.getText();

        return new CardEntry(emptyTextToNull(prefix), word, emptyTextToNull(postfix),
                emptyTextToNull(meaning), emptyTextToNull(note), emptyTextToNull(exampleSentence));
    }

    private @Nullable String emptyTextToNull(String text) {
        if (text != null && text.isEmpty()) {
            return null;
        }
        return text;
    }
}
