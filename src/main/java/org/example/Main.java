package org.example;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.BookReader;
import org.example.model.Model;
import org.example.model.Page;
import org.example.view.ControlPanel;
import org.example.view.WordArea;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

public class Main extends Application {
    public static void main(String[] args) throws IOException {
        launch(args);
    }

    private BookReader bookReader = new BookReader(Path.of("/home/florian/Downloads/HP.txt"));

    @Override
    public void start(Stage primaryStage) throws Exception {
        Model model = new Model();
        WordArea wordArea = new WordArea(model);
        Button button = new Button("new Page");
        ControlPanel controlPanel = new ControlPanel(model);

        VBox vBox = new VBox(button, controlPanel);

        Stream<Page> pageStream = bookReader.pageStream();
        Iterator<Page> iterator = pageStream.iterator();

        button.setOnAction(e -> {
            model.setPage(iterator.next());
        });

        SplitPane splitPane = new SplitPane(wordArea, vBox);
        splitPane.setOrientation(Orientation.HORIZONTAL);
        Scene scene = new Scene(splitPane, 720, 480);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}