package com.myteam.game.viewcontroller; // Giả sử PhomViewController nằm trong package này

// Imports từ JavaFX
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
// import javafx.scene.Parent; // Không cần nếu không chuyển scene
// import javafx.scene.Scene;  // Không cần
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
// import javafx.stage.Stage; // Không cần cho nút Deal
import javafx.stage.Stage;

// Imports từ logic game của bạn
import com.myteam.game.controller.PhomLogicController;
import com.myteam.game.model.core.card.StandardCard;
import com.myteam.game.model.phom.player.PhomBotPlayer;
import com.myteam.game.model.phom.gamestate.PhomGameState;
import com.myteam.game.model.phom.player.PhomHumanPlayer;
import com.myteam.game.model.phom.player.PhomPlayer;
// import com.myteam.game.view.PhomGameViewController; // Interface này sẽ được implement bởi class này

import java.io.IOException;
import java.net.URL;
// import java.util.Collections; // Nếu bạn muốn sắp xếp bài
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
// import java.util.stream.Collectors; // Không cần thiết cho các hàm cơ bản này

public class PhomBasicViewController implements Initializable, PhomViewInterface/* , PhomGameViewController */ { // Bỏ
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
    private Button eatButton;
    @FXML
    private Button drawButton;
    @FXML
    private Button exitButton;
    @FXML
    private Button sendButton;
    @FXML
    private Button playButton;

    @FXML
    private HBox cardCenterArea;
    @FXML
    private Label cardCenterCounter;

    @FXML
    private HBox player1CardArea;
    @FXML
    private VBox player2CardArea;
    @FXML
    private HBox player3CardArea;
    @FXML
    private VBox player4CardArea;

    @FXML
    private Label player2Counter;
    @FXML
    private Label player3Counter;
    @FXML
    private Label player4Counter;

    @FXML
    private Label menuLabel;

    @FXML
    private StackPane player1EatArea;
    @FXML
    private StackPane player2EatArea;
    @FXML
    private StackPane player3EatArea;
    @FXML
    private StackPane player4EatArea;

    @FXML
    private StackPane player2RevealArea;
    @FXML
    private StackPane player3RevealArea;
    @FXML
    private StackPane player4RevealArea;

    @FXML
    private HBox player1PhomArea;
    @FXML
    private FlowPane player2PhomArea;
    @FXML
    private FlowPane player3PhomArea;
    @FXML
    private FlowPane player4PhomArea;
    // </editor-fold>

    private PhomLogicController logicController;

    private boolean mainPlayerHasDrawnOrEatenThisTurn = false;

    private final Set<Label> selectedCard = new HashSet<>();
    private int MAIN_PLAYER_INDEX = 10; // Người chơi chính (index 0)

    private Pane[] playerCardAreas;
    private Pane[] playerEatAreas; // Khu vực hiển thị bài đã đánh của mỗi người
    private Pane[] playerEatenCardDisplayAreas; // Khu vực hiển thị bài đã ĂN của mỗi người (dùng playerPhomAreas)
    private Label[] playerCardCountLabels;
    private Pane[] playerRevealCardAreas;
    private Pane[] playerInfos; // Khu vực hiển thị thông tin người chơi
    private final double CARD_POP_UP_TRANSLATE_Y = -20.0;

    private int numberOfPlayers = 4;

    // --- INITIALIZATION ---
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        playerInfos = new Pane[] { player1Info, player2Info, player3Info, player4Info };

        playerCardAreas = new Pane[] { player1CardArea, player2CardArea, player3CardArea, player4CardArea };

        // playerEatAreas sẽ là nơi hiển thị lá bài Player X VỪA ĐÁNH RA
        // Trong FXML, đây là các StackPane: player1EatArea, player2EatArea, etc.
        playerEatAreas = new Pane[] { player1EatArea, player2EatArea, player3EatArea, player4EatArea };

        // playerEatenCardDisplayAreas là nơi hiển thị các lá bài Player X ĐÃ ĂN ĐƯỢC
        // Theo mô tả của bạn, đây là các playerXPhomArea
        playerEatenCardDisplayAreas = new Pane[] { player1PhomArea, player2PhomArea, player3PhomArea, player4PhomArea };

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
        setGameActionButtonsVisible(false);
        if (dealButton != null) {
            dealButton.setVisible(true);
            dealButton.setManaged(true);
        }
        if (exitButton != null) { // Nút Exit luôn hiện
            exitButton.setVisible(true);
            exitButton.setManaged(true);
        }
        if (cardCenterCounter != null)
            cardCenterCounter.setVisible(false); // Ẩn bộ đếm bài giữa ban đầu
        if (cardCenterArea != null)
            cardCenterArea.getChildren().clear(); // Dọn dẹp khu vực giữa
        updateAllOpponentCardCountsVisibility(false); // Ẩn label đếm bài của đối thủ

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

    @Override
    public void setGameActionButtonsVisible(boolean visible) {
        if (eatButton != null) {
            eatButton.setVisible(visible);
            eatButton.setManaged(visible);
        }
        if (drawButton != null) {
            drawButton.setVisible(visible);
            drawButton.setManaged(visible);
        }
        if (sendButton != null) {
            sendButton.setVisible(visible);
            sendButton.setManaged(visible);
        }
        if (playButton != null) {
            playButton.setVisible(visible);
            playButton.setManaged(visible);
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

    public void setLogicController(PhomLogicController logicController) {
        this.logicController = logicController;
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

    public void updateView(PhomGameState gameState) {
        if (logicController == null || gameState == null) {
            System.err.println("Cannot update view: LogicController or GameState is null.");
            return;
        }

        updateAllPlayerHandsDisplay(gameState.getPlayers());

        updateAllPlayerEatenCardsDisplay(gameState.getPlayers());

        // // Hiển thị bài VỪA ĐÁNH RA của mỗi người chơi (sử dụng playerEatAreas)
        // // Và làm nổi bật lá cardOnTable (lá bài toàn cục mới nhất để người sau ăn)
        updateAllPlayerLastDiscardDisplay(gameState.getPlayers(),
                gameState.getCardOnTable());

        // // Cập nhật nọc bài
        updateCenterDeckDisplay(logicController.getGameLogic().getDeck().size());

        // Xử lý game over
        // if (gameState.isGameOver()) {
        // displayGameOver(gameState);
        // }

        if (dealButton != null) {
            dealButton.setVisible(false);
            dealButton.setManaged(false);
        }
        // setGameActionButtonsVisible(true); // Hiện các nút cơ bản sau khi Deal
        updateAllOpponentCardCountsVisibility(true); // Hiện label đếm bài của đối thủ
    }

    @Override
    public void updateAllPlayerHandsDisplay(List<PhomPlayer> players) {
        if (playerCardAreas == null)
            return;

        for (int i = 0; i < players.size() && i < playerCardAreas.length; i++) {
            PhomPlayer player = players.get(i);
            Pane cardArea = playerCardAreas[i];
            if (cardArea == null)
                continue;

            cardArea.getChildren().clear();

            if (players.get(i) instanceof PhomHumanPlayer) { // Người chơi chính
                if (player != null && player.getHand() != null) {
                    for (StandardCard card : player.getHand()) {
                        PhomGameState gameState = logicController.getGameLogic().getCurrentGameState();
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
    public void updateCenterDeckDisplay(int deckSize) {
        if (cardCenterArea == null || cardCenterCounter == null)
            return;
        cardCenterArea.getChildren().clear();

        if (deckSize > 0) {

            Label cardBack = createBackLabel();
            cardCenterArea.getChildren().add(cardBack);
            cardCenterCounter.setText(String.valueOf(deckSize));
            cardCenterCounter.setVisible(true);
        } else {
            cardCenterCounter.setVisible(false);
        }
    }

    @Override
    public void updateAllPlayerEatenCardsDisplay(List<PhomPlayer> players) {
        if (playerEatenCardDisplayAreas == null)
            return;

        for (int i = 0; i < players.size() && i < playerEatenCardDisplayAreas.length; i++) {
            PhomPlayer player = players.get(i);
            Pane eatenDisplayArea = playerEatenCardDisplayAreas[i]; // Đây là playerXPhomArea
            if (eatenDisplayArea == null)
                continue;

            eatenDisplayArea.getChildren().clear();

            if (player != null && player.getEatenCards() != null) {
                for (StandardCard card : player.getEatenCards()) {
                    Label cardLabel = getCardLabel(card);
                    if (cardLabel != null)
                        eatenDisplayArea.getChildren().add(cardLabel);
                }
            }
        }
    }

    @Override
    public void updateAllPlayerLastDiscardDisplay(List<PhomPlayer> players, StandardCard cardOnTableGlobal) {
        if (playerEatAreas == null)
            return;

        for (int i = 0; i < players.size() && i < playerEatAreas.length; i++) {
            PhomPlayer player = players.get(i);
            Pane playerSpecificDiscardArea = playerEatAreas[i]; // Đây là StackPane playerXEatArea

            if (playerSpecificDiscardArea == null)
                continue;
            playerSpecificDiscardArea.getChildren().clear();

            if (player != null && player.getDiscardCards() != null && !player.getDiscardCards().isEmpty()) {
                StandardCard lastCardDiscardedByThisPlayer = player.getDiscardCards()
                        .get(player.getDiscardCards().size() - 1);
                Label cardLabel = getCardLabel(lastCardDiscardedByThisPlayer);

                if (cardLabel != null) {
                    playerSpecificDiscardArea.getChildren().add(cardLabel);
                }
            }
        }
    }

    @Override
    public void updateMeld(PhomGameState gameState) {
        if (gameState == null || gameState.getPlayers() == null)
            return;

        List<PhomPlayer> players = gameState.getPlayers();
        for (int i = 0; i < players.size(); i++) {

            System.out.println("Updating meld for player " + (i + 1));
            PhomPlayer player = players.get(i);
            Pane meldArea;
            if (i == 0) {
                meldArea = (HBox) playerEatenCardDisplayAreas[i]; // player1PhomArea
            } else {
                meldArea = (FlowPane) playerEatenCardDisplayAreas[i]; // playerXPhomArea
            }
            meldArea.getChildren().clear();
            if (meldArea == null) {
                System.err.println("Meld area for player " + (i + 1) + " is null.");
                continue;
            }
            if (player.getAllPhoms() == null) {
                System.err.println("Player " + (i + 1) + " has no melds.");
                continue;
            }
            if (player != null && player.getAllPhoms() != null) {
                for (List<StandardCard> meld : player.getAllPhoms()) {
                    HBox meldBox = new HBox();
                    for (StandardCard card : meld) {
                        Label cardLabel = getCardLabel(card);
                        meldBox.getChildren().add(cardLabel);
                    }
                    meldArea.getChildren().add(meldBox);
                }
            }
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
        } else {
            System.err.println("LogicController is not set. Cannot deal cards.");
        }
    }

    @FXML
    void handlePlayButton(ActionEvent event) {
        if (logicController == null) {
            return;

        }
        if (selectedCard.size() != 1) {
            return;
        }
        Label cardLabel = selectedCard.iterator().next();
        if (!(cardLabel.getUserData() instanceof StandardCard)) {
            return;
        }
        StandardCard cardToPlay = (StandardCard) cardLabel.getUserData();

        PhomGameState gameState = logicController.getGameLogic().getCurrentGameState();
        PhomPlayer mainHumanPlayer = gameState.getCurrentPlayer();
        setGameActionButtonsVisible(false);
        logicController.playerRequestsDiscardSingleCard(mainHumanPlayer, cardToPlay);
    }

    @FXML
    void handleDrawButton(ActionEvent event) {
        // Duc cuong yeu yen khanh nguyen my yen ngoc khanh linh
        if (logicController == null) {
            return;
        }
        PhomGameState gameState = logicController.getGameLogic().getCurrentGameState();
        PhomPlayer mainHumanPlayer = gameState.getCurrentPlayer();
        setGameActionButtonsVisible(false);
        logicController.playerRequestsDraw(mainHumanPlayer);
    }

    @FXML
    void handleEatButton(ActionEvent event) {
        if (logicController == null) {
            return;
        }
        PhomGameState gameState = logicController.getGameLogic().getCurrentGameState();
        PhomPlayer mainHumanPlayer = gameState.getCurrentPlayer();

        StandardCard cardToEat = gameState.getCardOnTable();
        if (cardToEat == null) {
            showUIMessage("Không có lá bài nào để ăn.");
            return;
        }
        setGameActionButtonsVisible(false);
        logicController.playerRequestsEat(mainHumanPlayer, cardToEat);
    }

    @Override
    public void clearAllPlayerAreasForNewGame() {
        if (playerCardAreas != null) {
            for (Pane area : playerCardAreas)
                if (area != null)
                    area.getChildren().clear();
        }
        if (playerEatAreas != null) {
            for (Pane area : playerEatAreas)
                if (area != null)
                    area.getChildren().clear();
        }
        if (playerEatenCardDisplayAreas != null) {
            for (Pane area : playerEatenCardDisplayAreas)
                if (area != null)
                    area.getChildren().clear();
        }
        clearSelectedCardsUI();
        if (cardCenterArea != null)
            cardCenterArea.getChildren().clear();
        if (cardCenterCounter != null)
            cardCenterCounter.setText("0");

        // Reset bộ đếm của đối thủ (có thể không cần nếu updateView xử lý tốt)
        for (int i = 1; i < playerCardCountLabels.length; i++) {
            if (playerCardCountLabels[i] != null)
                playerCardCountLabels[i].setText("0");
        }
    }

    @Override
    public void displayBotAction(String text) {
        this.setMenuLabel(text);
    }

    @Override
    public void promptPlayerToDiscard(PhomPlayer player, PhomGameState gameState) {
        // System.out.println("UI: " + player.getName() + ", please discard.");
        // PhomPlayer mainHuman =
        // logicController.getGameLogic().getPlayers().get(MAIN_PLAYER_INDEX);
        // if (player.equals(mainHuman)) {
        // this.mainPlayerHasDrawnOrEatenThisTurn = true; // Đã bốc/ăn hoặc lượt đầu ->
        // phải đánh
        // }
        // if (gameState != null) {
        // updateActionButtonsState(gameState);
        playButton.setVisible(true);
        playButton.setManaged(true);
    }

    @Override
    public void promptPlayerToEat(PhomPlayer player, PhomGameState gameState) {
        // System.out.println("UI: " + player.getName() + ", please discard.");
        // PhomPlayer mainHuman =
        // logicController.getGameLogic().getPlayers().get(MAIN_PLAYER_INDEX);
        // if (player.equals(mainHuman)) {
        // this.mainPlayerHasDrawnOrEatenThisTurn = true; // Đã bốc/ăn hoặc lượt đầu ->
        // phải đánh
        // }
        // if (gameState != null) {
        // updateActionButtonsState(gameState);
        eatButton.setVisible(true);
        eatButton.setManaged(true);
    }

    @Override
    public void promptPlayerToDraw(PhomPlayer player, PhomGameState gameState) {
        // System.out.println("UI: " + player.getName() + ", please discard.");
        // PhomPlayer mainHuman =
        // logicController.getGameLogic().getPlayers().get(MAIN_PLAYER_INDEX);
        // if (player.equals(mainHuman)) {
        // this.mainPlayerHasDrawnOrEatenThisTurn = true; // Đã bốc/ăn hoặc lượt đầu ->
        // phải đánh
        // }
        // if (gameState != null) {
        // updateActionButtonsState(gameState);
        drawButton.setVisible(true);
        drawButton.setManaged(true);
    }

    public void showInvalidMoveMessage(String message) {
        showUIMessage("Không hợp lệ: " + message);
    }

    public void showUIMessage(String message) { // Hàm này bạn đã có
        System.out.println("UI DISPLAY: " + message);
        // Cập nhật Label trên UI nếu có
    }

    public void setMenuLabel(String text) {
        if (this.menuLabel != null) {
            this.menuLabel.setText(text);
        }
    }

    public void setMainPlayerIndex(int mainPlayerIndex) {
        this.MAIN_PLAYER_INDEX = mainPlayerIndex;
        System.out.println("Main player index set to: " + MAIN_PLAYER_INDEX);
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

    @Override
    public void showGameOver(PhomPlayer winnerPlayer) {
        updateMeld(logicController.getGameLogic().getCurrentGameState());
        String winner = winnerPlayer.getName();
        if (menuLabel != null) {
            menuLabel.setText("Game Over! Người thắng: " + winner);
        }
        if (exitButton != null) {
            exitButton.setVisible(true);
            exitButton.setManaged(true);
        }
        for (PhomPlayer player : logicController.getGameLogic().getPlayers()) {
            if (player instanceof PhomBotPlayer) {
                int index = logicController.getGameLogic().getPlayers().indexOf(player);
                FlowPane OpponentCardsReveal = new FlowPane();
                for (StandardCard card : player.getHand()) {
                    Label cardView = getCardLabel(card);
                    OpponentCardsReveal.getChildren().add(cardView);
                }
                Pane cardArea = playerRevealCardAreas[index];
                cardArea.getChildren().clear(); // Xóa các lá bài cũ
                cardArea.getChildren().add(OpponentCardsReveal);
            }
        }

        javafx.application.Platform.runLater(() -> {
            showWinnerPopup(winner);
        });
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

        // Thêm nút OK (mặc định đã có, nhưng có thể tùy chỉnh nếu muốn)
        // ButtonType okButton = new ButtonType("OK", ButtonData.OK_DONE);
        // alert.getButtonTypes().setAll(okButton);

        // Hiển thị pop-up và đợi người dùng đóng nó
        alert.showAndWait();
    }

}
