package com.myteam.game.viewcontroller;

import com.myteam.game.App;
import com.myteam.game.PhomGameViewController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Stack;

import com.myteam.game.controller.PhomLogicController;
import com.myteam.game.controller.TienLenLogicController;
import com.myteam.game.model.core.deck.StandardCardDeck;
import com.myteam.game.model.game.PhomGameLogic;
import com.myteam.game.model.game.TienLenMienBacGameLogic;
import com.myteam.game.model.phom.player.PhomBotPlayer;
import com.myteam.game.model.phom.player.PhomHumanPlayer;
import com.myteam.game.model.phom.player.PhomPlayer;
import com.myteam.game.model.tienlen.player.TienLenBotPlayer;
import com.myteam.game.model.tienlen.player.TienLenHumanPlayer;
import com.myteam.game.model.tienlen.player.TienLenPlayer;

// Lớp lưu trữ lựa chọn (giữ nguyên hoặc dùng từ GameCoordinator)
class MenuSelections {
    String gameType;
    String opponentMode;
    int numberOfBots;
    int totalPlayers;

    @Override
    public String toString() {
        return "MenuSelections{" +
                "gameType='" + gameType + '\'' +
                ", opponentMode='" + opponentMode + '\'' +
                ", numberOfBots=" + numberOfBots +
                ", totalPlayers=" + totalPlayers +
                '}';
    }
}

public class GameMenuController implements Initializable {
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Label MenuLabel;
    @FXML
    private StackPane selectionStackPane; // Container chính cho các view lựa chọn

    // Các VBox group, mỗi VBox là một "màn hình" lựa chọn
    @FXML
    private VBox startGameButtonGroup;
    @FXML
    private VBox gameTypeButtonGroup;
    @FXML
    private VBox playerModeButtonGroup;
    @FXML
    private VBox numBotsButtonGroup;
    // (Không cần VBox riêng cho finalStartGameButton nếu nó đơn lẻ,
    // nhưng nếu muốn nhất quán, có thể tạo VBox cho nó)

    // Các Button cụ thể
    @FXML
    private Button StartGameButton;
    @FXML
    private Button TienLenButton;
    @FXML
    private Button PhomButton;
    @FXML
    private Button VsHumanButton;
    @FXML
    private Button VsBotButton;
    @FXML
    private Button BackButton;

    // @FXML private Button exitAppButton; // Nếu bạn có nút Exit riêng trong FXML
    // này
    private MenuSelections currentSelections = new MenuSelections();

    // Stack để lưu các Node (VBox group) đã hiển thị, giúp "Quay Lại"
    private Stack<Node> historyStack = new Stack<>();
    private List<Node> allSelectionGroups; // Danh sách tất cả các VBox group trong StackPane

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Khởi tạo danh sách các nhóm lựa chọn từ các con của StackPane
        // Hoặc bạn có thể khai báo trực tiếp như trước nếu các fx:id VBox đã được
        // inject
        allSelectionGroups = new ArrayList<>(selectionStackPane.getChildren());

        // Ban đầu chỉ hiển thị nhóm nút Start Game
        showScreen(startGameButtonGroup);
        BackButton.setVisible(false);
        BackButton.setManaged(false);
    }

    private void hideAllScreens() {
        for (Node group : allSelectionGroups) {
            if (group != null) {
                group.setVisible(false);
                group.setManaged(false);
            }
        }
    }

    private void showScreen(Node screenToShow) {
        hideAllScreens();
        if (screenToShow != null) {
            screenToShow.setVisible(true);
            screenToShow.setManaged(true);
        }
        BackButton.setVisible(!historyStack.isEmpty());
        BackButton.setManaged(!historyStack.isEmpty());
    }

    @FXML
    void handleStartGameButtonAction(ActionEvent event) {
        historyStack.push(startGameButtonGroup);
        showScreen(gameTypeButtonGroup);
    }

    @FXML
    void handleTienLenButtonAction(ActionEvent event) {
        currentSelections.gameType = "TienLen";
        historyStack.push(gameTypeButtonGroup);
        showScreen(playerModeButtonGroup);
    }

    @FXML
    void handlePhomButtonAction(ActionEvent event) {
        currentSelections.gameType = "Phom";
        historyStack.push(gameTypeButtonGroup);
        showScreen(playerModeButtonGroup);

    }

    @FXML
    void handleVsHumanButtonAction(ActionEvent event) {
        currentSelections.opponentMode = "VsHuman";
        historyStack.push(playerModeButtonGroup);
        if (currentSelections.gameType.equals("TienLen")) {
            try {
                initalizeTienLenHuman();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (currentSelections.gameType.equals("Phom")) {
            try {
                initalizePhomHuman();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleVsBotButtonAction(ActionEvent event) {
        currentSelections.opponentMode = "VsBot";
        historyStack.push(playerModeButtonGroup);
        if (currentSelections.gameType == "TienLen") {
            try {
                initalizeTienLenBot();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (currentSelections.gameType == "Phom") {
            try {
                initalizePhomBot();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleBackButtonAction(ActionEvent event) { // Đổi tên hàm này cho đúng với onAction của nút Back
        if (!historyStack.isEmpty()) {
            Node previousScreen = historyStack.pop();
            showScreen(previousScreen);
        }
    }

    void initalizeTienLenHuman() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("TienLenHumanView.fxml")); // Đảm bảo đường dẫn
                                                                                                // đúng
        Parent root = fxmlLoader.load();
        TienLenGameViewController uiController = fxmlLoader.getController(); // Lấy instance của TienLenViewController

        // 2. Tạo các thành phần Logic Game
        // Tạo người chơi (ví dụ)
        List<TienLenPlayer> players = new ArrayList<>();
        players.add(new TienLenHumanPlayer("Player 1 (You)")); // Người chơi chính
        players.add(new TienLenHumanPlayer("Bot 1"));
        players.add(new TienLenHumanPlayer("Bot 2"));
        players.add(new TienLenHumanPlayer("Bot 3"));

        StandardCardDeck<TienLenPlayer> deck = new StandardCardDeck<>(); // Bộ bài
        // Số lá bài ban đầu cho mỗi người (trừ người đầu tiên được thêm 1)
        int initialCardsPerPlayer = 13;
        TienLenMienBacGameLogic gameLogic = new TienLenMienBacGameLogic(deck, players, initialCardsPerPlayer);

        // 3. Tạo Logic Controller
        TienLenLogicController logicController = new TienLenLogicController(gameLogic);

        // 4. Kết nối UI Controller và Logic Controller (RẤT QUAN TRỌNG)
        // 4.1. UI Controller cần biết về Logic Controller
        uiController.setLogicController(logicController);

        // 4.2. Logic Controller cần biết về UI Controller (để gọi updateView, etc.)
        // Điều này yêu cầu TienLenViewController phải implement interface
        // TienLenGameViewController
        // Giả sử TienLenViewController đã `implements
        // com.myteam.game.view.TienLenGameViewController`
        logicController.setViewController(uiController); // DÒNG NÀY QUAN TRỌNG

        // 5. Thiết lập Scene và hiển thị Stage
        scene = new Scene(root, 1430, 770); // Kích thước cửa sổ
        stage.setTitle("TienLen Game");
        stage.setScene(scene);
        stage.show();
    }

    void initalizeTienLenBot() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("TienLenView.fxml")); // Đảm bảo đường dẫn
                                                                                           // đúng
        Parent root = fxmlLoader.load();
        TienLenGameViewController uiController = fxmlLoader.getController(); // Lấy instance của TienLenViewController
        uiController.setMainPlayerIndex(0);
        // 2. Tạo các thành phần Logic Game
        // Tạo người chơi (ví dụ)
        List<TienLenPlayer> players = new ArrayList<>();
        players.add(new TienLenHumanPlayer("Player 1 (You)")); // Người chơi chính
        players.add(new TienLenBotPlayer("Bot 1"));
        players.add(new TienLenBotPlayer("Bot 2"));
        players.add(new TienLenBotPlayer("Bot 3"));

        StandardCardDeck<TienLenPlayer> deck = new StandardCardDeck<>(); // Bộ bài
        // Số lá bài ban đầu cho mỗi người (trừ người đầu tiên được thêm 1)
        int initialCardsPerPlayer = 13;
        TienLenMienBacGameLogic gameLogic = new TienLenMienBacGameLogic(deck, players, initialCardsPerPlayer);

        // 3. Tạo Logic Controller
        TienLenLogicController logicController = new TienLenLogicController(gameLogic);

        // 4. Kết nối UI Controller và Logic Controller (RẤT QUAN TRỌNG)
        // 4.1. UI Controller cần biết về Logic Controller
        uiController.setLogicController(logicController);

        // 4.2. Logic Controller cần biết về UI Controller (để gọi updateView, etc.)
        // Điều này yêu cầu TienLenViewController phải implement interface
        // TienLenGameViewController
        // Giả sử TienLenViewController đã `implements
        // com.myteam.game.view.TienLenGameViewController`
        logicController.setViewController(uiController); // DÒNG NÀY QUAN TRỌNG

        // 5. Thiết lập Scene và hiển thị Stage
        scene = new Scene(root, 1430, 770); // Kích thước cửa sổ
        stage.setTitle("TienLen Game");
        stage.setScene(scene);
        stage.show();
    }

    void initalizePhomBot() throws IOException {
        // 1. Load FXML và lấy UI Controller
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("PhomView.fxml")); // Đảm bảo đường dẫn đúng
        Parent root = fxmlLoader.load();
        PhomGameViewController uiController = fxmlLoader.getController(); // Lấy instance của PhomViewController
        uiController.setMainPlayerIndex(0);
        // 2. Tạo các thành phần Logic Game
        // Tạo người chơi (ví dụ)
        List<PhomPlayer> players = new ArrayList<>();
        players.add(new PhomHumanPlayer("Player 1 (You)")); // Người chơi chính
        players.add(new PhomBotPlayer("Bot 1"));
        players.add(new PhomBotPlayer("Bot 2"));
        players.add(new PhomBotPlayer("Bot 3"));

        StandardCardDeck<PhomPlayer> deck = new StandardCardDeck<>(); // Bộ bài
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
        // Đặt stage cho Logic Controller
        // 5. Thiết lập Scene và hiển thị Stage

        scene = new Scene(root, 1430, 770); // Kích thước cửa sổ
        stage.setTitle("Phom Game");
        stage.setScene(scene);
        stage.show();
    }

    void initalizePhomHuman() throws IOException {
        // 1. Load FXML và lấy UI Controller
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("PhomHumanView.fxml")); // Đảm bảo đường dẫn đúng
        Parent root = fxmlLoader.load();
        PhomGameViewController uiController = fxmlLoader.getController(); // Lấy instance của PhomViewController

        // 2. Tạo các thành phần Logic Game
        // Tạo người chơi (ví dụ)
        List<PhomPlayer> players = new ArrayList<>();
        players.add(new PhomHumanPlayer("Player 1 (You)")); // Người chơi chính
        players.add(new PhomHumanPlayer("Player 2"));
        players.add(new PhomHumanPlayer("Player 3"));
        players.add(new PhomHumanPlayer("Player 4"));

        StandardCardDeck<PhomPlayer> deck = new StandardCardDeck<>(); // Bộ bài
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
        // Đặt stage cho Logic Controller
        // 5. Thiết lập Scene và hiển thị Stage

        scene = new Scene(root, 1430, 770); // Kích thước cửa sổ
        stage.setTitle("Phom Game");
        stage.setScene(scene);
        stage.show();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}