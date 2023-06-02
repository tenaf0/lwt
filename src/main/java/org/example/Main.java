package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

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

    @Override
    public void start(Stage primaryStage) throws Exception {

        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(5.0);
        flowPane.setVgap(5.0);

        new Model().words().forEach(w -> {
            Label l = new Label(w);
            l.setOnMouseClicked(e -> l.setTextFill(Color.YELLOW));
            if (w.equals("und")) {
                l.setBackground(new Background(new BackgroundFill(Color.GREEN, new CornerRadii(4.0), new Insets(-2.0))));
//                l.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, new CornerRadii(2.0), BorderStroke.DEFAULT_WIDTHS)));
            }
            flowPane.getChildren().add(l);
        });

        Scene scene = new Scene(flowPane, 640, 480);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}