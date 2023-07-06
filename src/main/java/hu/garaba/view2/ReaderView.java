package hu.garaba.view2;

import hu.garaba.model.util.Debouncer;
import hu.garaba.model2.PageView;
import hu.garaba.model2.ReadModel;
import hu.garaba.model2.event.PageBoundaryChange;
import hu.garaba.model2.event.PageChange;
import hu.garaba.model2.event.SelectionChange;
import hu.garaba.model2.event.WordStateChange;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.SVGPath;

import java.util.Objects;

public class ReaderView {
    private final ReadModel model;

    private final WordArea wordArea;

    public ReaderView(ReadModel model) {
        this.model = model;
        this.wordArea = new WordArea(model::selectWord);
    }

    @FXML
    private AnchorPane content;

    @FXML
    private Button leftButton;

    @FXML
    private Button rightButton;

    @FXML
    private Slider pageSlider;

    @FXML
    private Label pageNoLabel;

    private IntegerProperty pageNo = new SimpleIntegerProperty(0);
    private IntegerProperty maxPages = new SimpleIntegerProperty(100);

    @FXML
    public void initialize() {
        SVGPath leftImage = new SVGPath();
        leftImage.setContent("M13.75 16.25C13.6515 16.2505 13.5538 16.2313 13.4628 16.1935C13.3718 16.1557 13.2893 16.1001 13.22 16.03L9.72001 12.53C9.57956 12.3894 9.50067 12.1988 9.50067 12C9.50067 11.8013 9.57956 11.6107 9.72001 11.47L13.22 8.00003C13.361 7.90864 13.5285 7.86722 13.6958 7.88241C13.8631 7.89759 14.0205 7.96851 14.1427 8.08379C14.2649 8.19907 14.3448 8.35203 14.3697 8.51817C14.3946 8.68431 14.363 8.85399 14.28 9.00003L11.28 12L14.28 15C14.4205 15.1407 14.4994 15.3313 14.4994 15.53C14.4994 15.7288 14.4205 15.9194 14.28 16.06C14.1353 16.1907 13.9448 16.259 13.75 16.25Z");
        leftButton.setGraphic(leftImage);
        leftButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        SVGPath rightImage = new SVGPath();
        rightImage.setContent("M10.25 16.25C10.1493 16.2466 10.0503 16.2227 9.95921 16.1797C9.86807 16.1367 9.78668 16.0756 9.72001 16C9.57956 15.8594 9.50067 15.6688 9.50067 15.47C9.50067 15.2713 9.57956 15.0806 9.72001 14.94L12.72 11.94L9.72001 8.94002C9.66069 8.79601 9.64767 8.63711 9.68277 8.48536C9.71786 8.33361 9.79933 8.19656 9.91586 8.09322C10.0324 7.98988 10.1782 7.92538 10.3331 7.90868C10.4879 7.89198 10.6441 7.92391 10.78 8.00002L14.28 11.5C14.4205 11.6407 14.4994 11.8313 14.4994 12.03C14.4994 12.2288 14.4205 12.4194 14.28 12.56L10.78 16C10.7133 16.0756 10.6319 16.1367 10.5408 16.1797C10.4497 16.2227 10.3507 16.2466 10.25 16.25Z");
        rightButton.setGraphic(rightImage);
        rightButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        content.getChildren().add(wordArea);

        AnchorPane.setTopAnchor(wordArea, 0.0);
        AnchorPane.setLeftAnchor(wordArea, 0.0);
        AnchorPane.setRightAnchor(wordArea, 0.0);
        AnchorPane.setBottomAnchor(wordArea, 0.0);

        model.subscribe(e -> {
            if (e instanceof PageChange(int n, PageView page)) {
                wordArea.setPage(page);
                Platform.runLater(() -> pageNo.setValue(n));
            } else if (e instanceof SelectionChange(var oldList, var newList)) {
                wordArea.handleSelection(oldList, newList);
            } else if (e instanceof WordStateChange(var changes)) {
                wordArea.handleWordStateChange(changes);
            } else if (e instanceof PageBoundaryChange(var pagesBoundary)) {
                Platform.runLater(() -> {
                    maxPages.setValue(pagesBoundary.n());
                });
            }
        });

        pageNoLabel.textProperty().bind(Bindings.concat(pageNo.add(1).map(Number::toString), "/", maxPages.map(Number::toString)));
        pageNo.addListener((t,o,n) -> {
            if (!pageSlider.isValueChanging()) {
                pageSlider.setValue((int) n);
            }
        });
        Debouncer debouncer = new Debouncer();
        pageSlider.maxProperty().bind(maxPages.subtract(1));
        pageSlider.valueProperty().addListener((t,o,n) -> {
            if (pageSlider.isValueChanging()) {
                debouncer.debounce(() -> model.seekPage((int) (double) n), 20);
            }
        });
    }

    @FXML
    public void leftButtonPressed() {
        model.prevPage();
    }

    @FXML
    public void rightButtonPressed() {
        model.nextPage();
    }
}
