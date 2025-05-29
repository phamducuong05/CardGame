package com.myteam.game.viewcontroller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
// import javafx.stage.Stage; // Không cần cho nút Deal
import javafx.stage.Stage;

// Imports từ logic game của bạn
import com.myteam.game.controller.TienLenLogicController;
import com.myteam.game.model.core.card.StandardCard;
import com.myteam.game.model.tienlen.gamestate.TienLenGameState;
import com.myteam.game.model.tienlen.player.TienLenHumanPlayer;
import com.myteam.game.model.tienlen.player.TienLenPlayer;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
// import java.util.Collections; // Nếu bạn muốn sắp xếp bài
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
// import java.util.stream.Collectors; // Không cần thiết cho các hàm cơ bản này
import java.util.stream.Collectors;

public class TienLenBasicViewController implements Initializable, TienLenViewInterface /* , */ { // Bỏ comment
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private VBox player1Info;
    @FXML
    private VBox player2Info;
    @FXML
    private VBox player3Info;
    @FXML
    private VBox player4Info;

    @FXML
    private Button dealButton;
    @FXML
    private Button exitButton;
    @FXML
    private Button skipButton;
    @FXML
    private Button playButton;

    @FXML
    private HBox cardCenterArea;

    @FXML
    private HBox player1CardArea;
    @FXML
    private HBox player2CardArea;
    @FXML
    private HBox player3CardArea;
    @FXML
    private HBox player4CardArea;

    @FXML
    private Label player2Counter;
    @FXML
    private Label player3Counter;
    @FXML
    private Label player4Counter;

    @FXML
    private StackPane player2RevealArea;
    @FXML
    private StackPane player3RevealArea;
    @FXML
    private StackPane player4RevealArea;

    @FXML
    private Label menuLabel;

    private TienLenLogicController logicController;

    private final Set<Label> selectedCard = new HashSet<>();
    private final double CARD_POP_UP_TRANSLATE_Y = -20.0;
    private int MAIN_PLAYER_INDEX = 10; // Người chơi chính (index 0)

    private Pane[] playerCardAreas;
    private Label[] playerCardCountLabels;
    private Pane[] playerRevealCardAreas;

    private int numberOfPlayers; // Số lượng người chơi, có thể thay đổi tùy game

    // --- INITIALIZATION ---
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        playerCardAreas = new Pane[] { player1CardArea, player2CardArea, player3CardArea, player4CardArea };

        playerCardCountLabels = new Label[] {
                null, // Player 1 (MAIN_PLAYER_INDEX) không dùng Label này, số lá bài hiện rõ
                player2Counter,
                player3Counter,
                player4Counter
        };

        playerRevealCardAreas = new StackPane[] {
                null, // Player 1 không có khu vực này
                player2RevealArea,
                player3RevealArea,
                player4RevealArea
        };

        // Ban đầu, các nút hành động (ngoại trừ Deal) nên được ẩn/vô hiệu hóa
        setGameActionButtonsVisible(false);
        if (dealButton != null) {
            dealButton.setVisible(true);
            dealButton.setManaged(true);
        }
        if (exitButton != null) { // Nút Exit luôn hiện
            exitButton.setVisible(true);
            exitButton.setManaged(true);
        }
        if (cardCenterArea != null)
            cardCenterArea.getChildren().clear(); // Dọn dẹp khu vực giữa
        updateAllOpponentCardCountsVisibility(false); // Ẩn label đếm bài của đối thủ

    }

    @Override
    public void setGameActionButtonsVisible(boolean visible) {

        if (playButton != null) {
            playButton.setVisible(visible);
            playButton.setManaged(visible);
        }

        if (skipButton != null) {
            skipButton.setVisible(visible);
            skipButton.setManaged(visible);
        }
        if (dealButton != null) {
            dealButton.setVisible(visible);
            dealButton.setManaged(visible);
        }
        if (exitButton != null) {
            exitButton.setVisible(visible);
            exitButton.setManaged(visible);
        }
    }

    @Override
    public void updateAllOpponentCardCountsVisibility(boolean visible) {
        for (int i = 1; i < playerCardCountLabels.length; i++) { // Bắt đầu từ 1 vì player 0 là main player
            if (playerCardCountLabels[i] != null) {
                playerCardCountLabels[i].setVisible(visible);
            }
        }
    }

    private Label createBackLabel() {
        Label cardBackLabel = new Label("Back");
        cardBackLabel.setStyle(
                "-fx-border-color: white;" + // viền trắng
                        "-fx-border-width: 2;" + // độ dày viền
                        "-fx-border-radius: 5;" + // bo góc (tuỳ chọn)
                        "-fx-background-color: transparent;" + // nền trong suốt hoặc có thể là màu khác
                        "-fx-text-fill: white;" + // màu chữ trắng
                        "-fx-padding: 8;" // padding để dễ nhìn hơn
        );
        return cardBackLabel;
    }

    private Label getCardLabel(StandardCard card) {
        if (card == null) {
            return null;
        }
        Label cardLabel = new Label(card.getRank().getValue() + card.getSuit().getValue());
        cardLabel.setOnMouseClicked(this::handleCardClick);
        cardLabel.setStyle(
                "-fx-border-color: white;" + // viền trắng
                        "-fx-border-width: 2;" + // độ dày viền
                        "-fx-border-radius: 5;" + // bo góc (tuỳ chọn)
                        "-fx-background-color: transparent;" + // nền trong suốt hoặc có thể là màu khác
                        "-fx-text-fill: white;" + // màu chữ trắng
                        "-fx-padding: 8;" // padding để dễ nhìn hơn
        );
        cardLabel.setUserData(card); // Lưu trữ thông tin lá bài trong userData
        return cardLabel;
    }

    public void setLogicController(TienLenLogicController logicController) {
        this.logicController = logicController;
    }

    @Override
    public void updateView(TienLenGameState gameState) {
        if (logicController == null || gameState == null) {
            System.err.println("Cannot update view: LogicController or GameState is null.");
            return;
        }

        updateAllPlayerHandsDisplay(gameState.getPlayers());
        // Hiển thị bài VỪA ĐÁNH RA của mỗi người chơi (sử dụng playerEatAreas)
        // Và làm nổi bật lá cardOnTable (lá bài toàn cục mới nhất để người sau ăn)
        updateCenterArea(gameState.getCardsOnTable());

        if (gameState.isGameOver()) {
            setGameActionButtonsVisible(false); // Ẩn các nút hành động khi game kết thúc
            return;
        }

        if (dealButton != null) {
            dealButton.setVisible(false);
            dealButton.setManaged(false);
        }
        updateAllOpponentCardCountsVisibility(true); // Hiện label đếm bài của đối thủ
    }

    @FXML
    void handleCardClick(MouseEvent event) {
        Label clickedCard = (Label) event.getSource();

        if (selectedCard.contains(clickedCard)) {
            resetCardPosition(clickedCard);
            selectedCard.remove(clickedCard);
        } else {

            clickedCard.setTranslateY(CARD_POP_UP_TRANSLATE_Y);
            selectedCard.add(clickedCard);
        }
        System.out.println("Selected cards: " + selectedCard.size());
    }

    private void resetCardPosition(Label cardView) {
        if (cardView != null) {
            cardView.setTranslateY(0);
        }
    }

    @Override
    public void clearSelectedCardsUI() {
        for (Label iv : selectedCard) {
            resetCardPosition(iv);
        }
        selectedCard.clear();
    }

    @Override
    public void updateAllPlayerHandsDisplay(List<TienLenPlayer> players) {
        if (playerCardAreas == null)
            return;

        for (int i = 0; i < players.size() && i < playerCardAreas.length; i++) {
            TienLenPlayer player = players.get(i);
            Pane cardArea = playerCardAreas[i];
            if (cardArea == null)
                continue;

            cardArea.getChildren().clear();

            if (players.get(i) instanceof TienLenHumanPlayer) { // Người chơi chính
                System.out.println("Updating hand for player: " + player.getName());
                if (player != null && player.getHand() != null) {
                    for (StandardCard card : player.getHand()) {
                        TienLenGameState gameState = logicController.getGameLogic().getCurrentGameState();
                        Label cardLabel;
                        if (gameState.getPlayers().indexOf(gameState.getCurrentPlayer()) != i
                                && i != MAIN_PLAYER_INDEX) {
                            cardLabel = createBackLabel();
                        } else {
                            cardLabel = getCardLabel(card);
                        }
                        if (cardLabel != null)
                            cardArea.getChildren().add(cardLabel);
                    }
                }
            } else { // Đối thủ
                if (player != null && player.getHand() != null) {
                    System.out.println("Updating hand for opponent: " + player.getName());
                    Label cardLabel = createBackLabel();
                    cardArea.getChildren().add(cardLabel);
                    if (playerCardCountLabels[i] != null) {
                        playerCardCountLabels[i].setText(String.valueOf(player.getHand().size()));
                    }
                }
            }
        }
        clearSelectedCardsUI();
    }

    @Override
    public void updateCenterArea(List<StandardCard> cardsOnTable) {
        if (cardCenterArea == null) {
            System.err.println("Card center area is null, cannot update.");
            return;
        }
        cardCenterArea.getChildren().clear();

        if (cardsOnTable != null && !cardsOnTable.isEmpty()) {
            for (StandardCard card : cardsOnTable) {
                Label cardLabel = getCardLabel(card);
                if (cardLabel != null) {
                    cardCenterArea.getChildren().add(cardLabel);
                }
            }
        } else {
            System.out.println("No cards on the table.");
        }
    }

    @FXML
    void handleDealButton(ActionEvent event) {
        if (logicController != null) {
            System.out.println("Deal button clicked. Requesting new game from LogicController...");
            // Dọn dẹp UI trước khi logic controller chia bài mới (nếu cần thiết,
            // logic controller có thể sẽ gọi updateView() ngay sau khi deal xong)
            clearAllPlayerAreasForNewGame();
            logicController.handleDeal(); // LogicController sẽ gọi gameLogic.startGame()
                                          // và sau đó gọi lại this.updateView(newState)
            if (dealButton != null) {
                dealButton.setVisible(false);
                dealButton.setManaged(false);
            }
            if (playButton != null) {
                playButton.setVisible(true);
                playButton.setManaged(true);
            }

            if (skipButton != null) {
                skipButton.setVisible(true);
                skipButton.setManaged(true);
            }
        } else {
            System.err.println("LogicController is not set. Cannot deal cards.");
        }
    }

    @Override
    public void clearAllPlayerAreasForNewGame() {
        if (playerCardAreas != null) {
            for (Pane area : playerCardAreas)
                if (area != null)
                    area.getChildren().clear();
        }
        if (cardCenterArea != null)
            cardCenterArea.getChildren().clear(); // Dọn dẹp khu vực giữa

        // Reset bộ đếm của đối thủ (có thể không cần nếu updateView xử lý tốt)
        for (int i = 1; i < playerCardCountLabels.length; i++) {
            if (playerCardCountLabels[i] != null)
                playerCardCountLabels[i].setText("0");
        }
    }

    @FXML
    void handlePlayButton(ActionEvent event) {
        if (logicController == null) {
            System.err.println("Lỗi: LogicController chưa được thiết lập.");
            return;
        }

        List<StandardCard> selectedCardsToPlay = getSelectedWestCardsFromUI();

        if (selectedCardsToPlay.isEmpty()) {
            showUIMessage("Vui lòng chọn ít nhất một lá bài để đánh!");
            return;
        }

        TienLenGameState gameState = logicController.getGameLogic().getCurrentGameState();
        if (gameState == null) {
            showUIMessage("Lỗi: Trạng thái game không hợp lệ.");
            return;
        }
        TienLenPlayer humanPlayer = gameState.getCurrentPlayer();
        if (humanPlayer == null) {
            showUIMessage("Lỗi: Không xác định được người chơi.");
            return;
        }

        System.out.println("UI: " + humanPlayer.getName() + " requests to play cards: " + selectedCardsToPlay);
        logicController.playerRequestsPlayCards(humanPlayer, selectedCardsToPlay);

    }

    @Override
    public List<StandardCard> getSelectedWestCardsFromUI() {
        if (selectedCard.isEmpty()) {
            return Collections.emptyList(); // Hoặc new ArrayList<>()
        }
        List<StandardCard> cards = selectedCard.stream()
                .map(iv -> (StandardCard) iv.getUserData()) // Lấy WestCard từ UserData
                .filter(java.util.Objects::nonNull) // Bỏ qua nếu UserData không phải WestCard hoặc null
                .collect(Collectors.toList());
        cards.sort(Comparator.comparing((StandardCard c) -> c.getRank().getValue()) // Sử dụng getValue() để so sánh số
                .thenComparing(c -> c.getSuit().ordinal())); // So sánh theo thứ tự enum của Suit
        return cards;
    }

    @FXML
    void handleSkipButton(ActionEvent event) {
        if (logicController == null) {
            System.err.println("Lỗi: LogicController chưa được thiết lập.");
            return;
        }

        TienLenGameState gameState = logicController.getGameLogic().getCurrentGameState();
        if (gameState == null) {
            showUIMessage("Lỗi: Trạng thái game không hợp lệ.");
            return;
        }
        TienLenPlayer humanPlayer = gameState.getCurrentPlayer();
        if (humanPlayer == null) {
            showUIMessage("Lỗi: Không xác định được người chơi.");
            return;
        }

        System.out.println("UI: " + humanPlayer.getName() + " requests to skip turn.");
        logicController.playerRequestsPass(humanPlayer);
    }

    @FXML
    void handleExitButton(ActionEvent event) throws IOException {
        if (logicController != null) {
            logicController.markGameAsStopped(); // **CHỈ CẦN GỌI HÀM NÀY**
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/myteam/game/GameMenuView.fxml"));
            Parent menuRoot = loader.load();
            GameMenuController menuController = loader.getController(); // Lấy instance MỚI của GameMenuController

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // QUAN TRỌNG: Truyền Stage cho GameMenuController MỚI
            menuController.setStage(currentStage); // Giả sử bạn có phương thức setStage(Stage stage) trong
                                                   // GameMenuController

            Scene menuScene = new Scene(menuRoot);
            currentStage.setScene(menuScene);
            currentStage.setTitle("Game Menu");
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Xử lý lỗi
        }
    }

    public void setMainPlayerIndex(int mainPlayerIndex) {
        this.MAIN_PLAYER_INDEX = mainPlayerIndex;
        System.out.println("Main player index set to: " + MAIN_PLAYER_INDEX);
    }

    @Override
    public void promptPlayerForAction(TienLenPlayer player, TienLenGameState gameState) {
        System.out.println("Prompting player " + player.getName() + " for action");
        // In a real implementation, would enable appropriate UI controls
        // For TienLen, this would enable card selection and play/pass buttons
    }

    @Override
    public void showInvalidMoveMessage() {
        System.out.println("Invalid move! Please try again.");
        // In a real implementation, would show an error message in the UI
    }

    @Override
    public void showUIMessage(String message) { // Hàm này bạn đã có
        System.out.println("UI DISPLAY: " + message);
        // Cập nhật Label trên UI nếu có
    }

    @Override
    public void setMenuLabel(String text) {
        if (this.menuLabel != null) {
            this.menuLabel.setText(text);
        }
    }

    public void displayBotAction(String text) {
        this.setMenuLabel(text);
    }

    private void showWinnerPopup(String winnerName) {
        Alert alert = new Alert(AlertType.INFORMATION); // Loại pop-up thông tin
        alert.setTitle("Game Over!");
        alert.setHeaderText(null); // Không cần header text phức tạp

        if (winnerName != null && !winnerName.isEmpty()) {
            alert.setContentText("Congratulations, " + winnerName + " is the winner!");
        } else {
            alert.setContentText("The game has ended. It's a draw or no clear winner.");
        }

        alert.showAndWait();
    }

    @Override
    public void onGameEnded(TienLenGameState gameState, TienLenPlayer winner) {
        System.out.println("Game ended. Winner: " + (winner != null ? winner.getName() : "None/Draw"));
        dealButton.setVisible(true);
        dealButton.setManaged(true);
        updateAllOpponentCardCountsVisibility(false);
        if (skipButton != null) {
            skipButton.setVisible(false);
            skipButton.setManaged(false);
        }
        if (playButton != null) {
            playButton.setVisible(false);
            playButton.setManaged(false);
        }
        if (exitButton != null) {
            exitButton.setVisible(true);
            exitButton.setManaged(true);
        }

        String winnerName = null;
        String message = "Game Over!";
        if (winner != null) {
            winnerName = winner.getName();
            message += " Winner: " + winnerName + "!";
        } else {
            message += " No clear winner.";
        }
        setMenuLabel(message); // Vẫn cập nhật menu label
        updateView(gameState); // Cập nhật UI lần cuối
        if (exitButton != null) {
            exitButton.setVisible(true);
            exitButton.setManaged(true);
        }
        // Hiển thị pop-up thông báo người thắng
        final String finalWinnerName = winnerName; // Cần biến final để dùng trong lambda
        // Chạy trên luồng UI của JavaFX
        javafx.application.Platform.runLater(() -> {
            showWinnerPopup(finalWinnerName);
        });
    }
}