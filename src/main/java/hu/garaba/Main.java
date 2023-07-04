package hu.garaba;

import hu.garaba.model2.ReadModel;
import hu.garaba.textprocessor.TextProcessor;
import hu.garaba.view2.MainWindow;
import hu.garaba.view2.ReaderView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {
    private final ReadModel model = new ReadModel();

    public static void main(String[] args) {
        Thread thread = new Thread(TextProcessor::warmup);
        thread.setDaemon(true);
        thread.start();

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        var loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        loader.setControllerFactory(c -> {
            if (c == MainWindow.class) {
                return new MainWindow(model, () -> {
                    File file = new FileChooser().showOpenDialog(primaryStage);
                    return file.toPath();
                }, () -> {
                    File file = new FileChooser().showSaveDialog(primaryStage);
                    return file.toPath();
                });
            } else if (c == ReaderView.class) {
                return new ReaderView(model);
            } else {
                throw new IllegalArgumentException();
            }
/*
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
*/
        });
        Parent parent = loader.load();

        Scene scene = new Scene(parent, 900, 680);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
//        model.close();
    }
}