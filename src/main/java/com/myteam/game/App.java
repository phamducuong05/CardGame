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

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Load FXML và lấy UI Controller
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("PhomView.fxml")); // Đảm bảo đường dẫn đúng
        Parent root = fxmlLoader.load();
        PhomGameViewController uiController = fxmlLoader.getController(); // Lấy instance của PhomViewController

        // 2. Tạo các thành phần Logic Game
        // Tạo người chơi (ví dụ)
        List<PhomPlayer> players = new ArrayList<>();
        players.add(new PhomHumanPlayer("Player 1 (You)")); // Người chơi chính
        players.add(new PhomBotPlayer("Bot 1"));
        players.add(new PhomBotPlayer("Bot 2"));
        players.add(new PhomBotPlayer("Bot 3"));

        WestCardDeck<PhomPlayer> deck = new WestCardDeck<>(); // Bộ bài
        // Số lá bài ban đầu cho mỗi người (trừ người đầu tiên được thêm 1)
        int initialCardsPerPlayer = 9;
        PhomGameLogic gameLogic = new PhomGameLogic(deck, players, initialCardsPerPlayer);

        // 3. Tạo Logic Controller
        PhomLogicController logicController = new PhomLogicController(gameLogic);

        // 4. Kết nối UI Controller và Logic Controller (RẤT QUAN TRỌNG)
        // 4.1. UI Controller cần biết về Logic Controller
        uiController.setLogicController(logicController);

        // 4.2. Logic Controller cần biết về UI Controller (để gọi updateView, etc.)
        // Điều này yêu cầu PhomViewController phải implement interface
        // PhomGameViewController
        // Giả sử PhomViewController đã `implements
        // com.myteam.game.view.PhomGameViewController`
        logicController.setViewController(uiController); // DÒNG NÀY QUAN TRỌNG

        // 5. Thiết lập Scene và hiển thị Stage
        scene = new Scene(root, 1430, 770); // Kích thước cửa sổ
        stage.setTitle("Phom Game");
        stage.setScene(scene);
        stage.show();
    }

    // Hàm loadFXML và setRoot hiện tại của bạn có thể không cần nếu bạn chỉ có 1
    // view chính
    // hoặc bạn cần điều chỉnh chúng cho phù hợp.
    // static void setRoot(String fxml) throws IOException {
    // scene.setRoot(loadFXML(fxml));
    // }

    // private static Parent loadFXML(String fxml) throws IOException {
    // FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml +
    // ".fxml"));
    // return fxmlLoader.load();
    // }

    public static void main(String[] args) {
        launch();
    }

}