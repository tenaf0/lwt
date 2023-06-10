package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.model.Model;
import org.example.view.DictionaryPane;
import org.example.view.EditCardBox;
import org.example.view.GUI;

import java.io.File;
import java.io.IOException;

public class Main extends Application {
    private final Model model = new Model();

    public static void main(String[] args) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        var loader = new FXMLLoader(getClass().getResource("/gui.fxml"));
        loader.setControllerFactory(c -> {
            if (c.equals(DictionaryPane.class)) {
                return new DictionaryPane(model, getHostServices());
            } else if (c.equals(EditCardBox.class)) {
                return new EditCardBox(model);
            } else {
                return new GUI(model, () -> {
                    File file = new FileChooser().showOpenDialog(primaryStage);
                    return file.toPath();
                }, () -> {
                    File file = new FileChooser().showSaveDialog(primaryStage);
                    return file.toPath();
                });
            }
        });
        Parent parent = loader.load();

        Scene scene = new Scene(parent, 900, 680);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
//        model.close();
    }
}