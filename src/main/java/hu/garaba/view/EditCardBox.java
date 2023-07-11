package hu.garaba.view;

import hu.garaba.model.CardEntry;
import hu.garaba.model2.ReadModel;
import hu.garaba.model2.event.SelectedWordChange;
import javafx.fxml.FXML;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditCardBox {

    private final ReadModel model;

    public EditCardBox(ReadModel model) {
        this.model = model;
    }

    @FXML
    private TextField prefixField;
    @FXML
    private TextField postfixField;
    @FXML
    private TextField wordField;

    @FXML
    private TextArea exampleSentenceField;

    @FXML
    private TextField meaningField;

    @FXML
    private TextField noteField;



    @FXML
    public void initialize() {
        model.subscribe(e -> {
            switch (e) {
                case SelectedWordChange(String lemma, var dictionaryEntry, var sentenceView) -> {
                    reset();

                    wordField.setText(lemma);
                    if (sentenceView.sentence() != null) {
                        exampleSentenceField.setText(sentenceView.sentence().toText());
                    }

                    if (dictionaryEntry == null || dictionaryEntry.grammar() == null)
                        return;

                    var grammatik = dictionaryEntry.grammar();
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

            exampleSentenceField.setOnKeyReleased(k -> {
                IndexRange selection = exampleSentenceField.getSelection();
                if (k.isControlDown() && k.getCode() == KeyCode.B && selection.getLength() > 0) {
                    String text = exampleSentenceField.getText();
                    exampleSentenceField.setText(text.substring(0, selection.getStart())
                            + "<b>" + text.substring(selection.getStart(), selection.getEnd()) + "</b>"
                    + text.substring(selection.getEnd()));
                }
            });
        });

        wordField.textProperty().addListener((l, o, n) -> {
            if (n == null || !model.isKnown(n)) {
                wordField.getStyleClass().remove("alreadyEntered");
            } else {
                wordField.getStyleClass().add("alreadyEntered");
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

        return new CardEntry(null, emptyTextToNull(prefix), word, emptyTextToNull(postfix),
                emptyTextToNull(meaning), emptyTextToNull(note), emptyTextToNull(exampleSentence));
    }

    private @Nullable String emptyTextToNull(String text) {
        if (text != null && text.isEmpty()) {
            return null;
        }
        return text;
    }
}
