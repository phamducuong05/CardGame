package com.myteam.game;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App1 extends Application {
    private static Stage stage; // Thêm biến stage để quản lý cửa sổ
    private static Scene scene;

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App1.class.getResource("GameMenuView.fxml"));
        Parent root = fxmlLoader.load();

        scene = new Scene(root, 1430, 770); // Kích thước cửa sổ
        stage.setTitle("Game Menu");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
