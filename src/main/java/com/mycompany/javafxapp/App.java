package com.mycompany.javafxapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Dùng đường dẫn classpath tuyệt đối
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/javafxapp/primary.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Deal Cards Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}