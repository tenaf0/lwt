package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.model.Model;
import org.example.view.DictionaryPane;
import org.example.view.EditCardBox;
import org.example.view.GUI;

import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Model model = new Model();

        var loader = new FXMLLoader(getClass().getResource("/gui.fxml"));
        loader.setControllerFactory(c -> {
            if (c.equals(DictionaryPane.class)) {
                return new DictionaryPane(model, getHostServices());
            } else if (c.equals(EditCardBox.class)) {
                return new EditCardBox(model);
            } else {
                return new GUI(model);
            }
        });
        Parent parent = loader.load();

        Scene scene = new Scene(parent, 720, 480);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}