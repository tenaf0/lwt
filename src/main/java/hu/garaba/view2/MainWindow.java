package hu.garaba.view2;

import hu.garaba.model2.ReadModel;
import javafx.fxml.FXML;

import java.nio.file.Path;
import java.util.function.Supplier;

public class MainWindow {
    private final ReadModel model;
    private final Supplier<Path> fileChooser;
    private final Supplier<Path> textDialog;

    public MainWindow(ReadModel model, Supplier<Path> fileChooser, Supplier<Path> textDialog) {
        this.model = model;
        this.fileChooser = fileChooser;
        this.textDialog = textDialog;
    }

    @FXML
    public void onOpenFile() {
        Path path = fileChooser.get();
        model.open(path);
    }

    @FXML
    public void onEnterText() {

    }

    @FXML
    public void onExport() {

    }
}
