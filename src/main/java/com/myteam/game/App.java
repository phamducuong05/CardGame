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

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Load FXML và lấy UI Controller
        FXMLLoader loader = new FXMLLoader(App.class.getResource("GameMenuView.fxml"));
        Parent root = loader.load();
        GameMenuController controller = loader.getController();
        controller.setStage(stage); // Gán stage từ main app
                                    // đúng
        scene = new Scene(root); // Kích thước cửa sổ
        stage.setTitle("Game Menu");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
