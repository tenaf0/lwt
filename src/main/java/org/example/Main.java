package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.model.Model;
import org.example.model.Page;
import org.example.model.WordChange;
import org.example.textprocessor.TextProcessor;
import org.example.view.WordArea;

import java.io.IOException;
import java.util.List;

public class Main extends Application {
    public static void main(String[] args) throws IOException {
        launch(args);
/*
        Document doc = Jsoup.connect("https://www.dwds.de/wb/Hausarbeit").get();
        System.out.println(doc.title());
        Elements newsHeadlines = doc.select("#mp-itn b a");
        for (Element headline : newsHeadlines) {
            System.out.printf("%s\n\t%s",
                    headline.attr("title"), headline.absUrl("href"));
        }*/
    }

    int i = 0;
    List<Page> pages = new TextProcessor(Model.text).process().toList();

    @Override
    public void start(Stage primaryStage) throws Exception {
        Model model = new Model();
        WordArea wordArea = new WordArea(model);
        Button button = new Button("new Page");
        Label label = new Label();
        model.subscribe(e -> {
            switch (e) {
                case WordChange(var word) -> label.setText(word);
                default -> {}
            }
        });
        VBox vBox = new VBox(button, label);

        button.setOnAction(e -> {
            Page page = pages.get(i);
            model.setPage(page);
            i = (i + 1) % pages.size();
        });

        HBox hbox = new HBox(wordArea, vBox);
        Scene scene = new Scene(hbox, 640, 480);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}