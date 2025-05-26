package com.myteam.game;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.myteam.game.controller.PhomLogicController;
import com.myteam.game.controller.PhomViewController;
import com.myteam.game.PhomGameViewController;
import com.myteam.game.model.core.deck.WestCardDeck;
import com.myteam.game.model.game.PhomGameLogic;
import com.myteam.game.model.phom.PhomBotPlayer;
import com.myteam.game.model.phom.PhomHumanPlayer;
import com.myteam.game.model.phom.PhomPlayer;
import com.myteam.game.model.tienlen.TienLenBotPlayer;
import com.myteam.game.model.tienlen.TienLenHumanPlayer;
import com.myteam.game.model.tienlen.TienLenPlayer;
import com.myteam.game.controller.TienLenLogicController;
import com.myteam.game.model.game.TienLenMienBacGameLogic;

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
