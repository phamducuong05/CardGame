package com.myteam.game.viewcontroller;

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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
// import javafx.stage.Stage; // Không cần cho nút Deal
import javafx.stage.Stage;
import javafx.util.Duration;

// Imports từ logic game của bạn
import com.myteam.game.controller.TienLenLogicController;
import com.myteam.game.model.core.card.StandardCard;
import com.myteam.game.model.core.enums.Rank;
// import com.myteam.game.view.PhomGameViewController; // Interface này sẽ được implement bởi class này
import com.myteam.game.model.tienlen.gamestate.TienLenGameState;
import com.myteam.game.model.tienlen.player.TienLenHumanPlayer;
import com.myteam.game.model.tienlen.player.TienLenPlayer;

import java.io.IOException;
import java.io.InputStream;
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

public class TienLenGameViewController implements Initializable, TienLenViewInterface /* , PhomGameViewController */ { // Bỏ
    // comment
    // PhomGameViewController khi
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

    private Image cardBackImage;
    private final Set<ImageView> selectedImageViews = new HashSet<>();
    private final double CARD_POP_UP_TRANSLATE_Y = -20.0;
    private final double CARD_WIDTH = 75;
    private int MAIN_PLAYER_INDEX = 10; // Người chơi chính (index 0)

    private Pane[] playerCardAreas;
    private Label[] playerCardCountLabels;
    private Pane[] playerRevealCardAreas;

    private int numberOfPlayers; // Số lượng người chơi, có thể thay đổi tùy game

    // --- INITIALIZATION ---
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCardBackImage();

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

    // --- IMAGE LOADING AND ImageView CREATION ---
    private void loadCardBackImage() {
        String cardBackPath = "/com/myteam/game/images/cards/back.png";
        try (InputStream stream = getClass().getResourceAsStream(cardBackPath)) {
            if (stream != null) {
                cardBackImage = new Image(stream);
                if (cardBackImage.isError()) {
                    System.err.println("Error loading card back image (flag): " + cardBackPath + " - "
                            + cardBackImage.getException());
                    cardBackImage = null;
                }
            } else {
                System.err.println("Card back image not found: " + cardBackPath);
            }
        } catch (Exception e) {
            System.err.println("Exception loading card back image: " + cardBackPath);
            e.printStackTrace();
        }
    }

    private Image loadCardImage(String imagePath) {
        try (InputStream stream = getClass().getResourceAsStream(imagePath)) {
            if (stream != null) {
                Image image = new Image(stream);
                if (image.isError()) {
                    System.err.println("Error loading card image (flag): " + imagePath + " - " + image.getException());
                    return null;
                }
                return image;
            } else {
                System.err.println("Card image not found: " + imagePath);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Exception loading card image: " + imagePath);
            e.printStackTrace();
            return null;
        }
    }

    // Tạo ImageView chung, có thể dùng cho bài trên tay, bài đã đánh, etc.
    private ImageView createGenericCardImageView(StandardCard card, String specificImagePath) {
        Image cardImage = loadCardImage(specificImagePath);
        if (cardImage == null) {
            System.err.println("Could not load image for card: " + card + " at path " + specificImagePath);
            // Có thể trả về một ImageView placeholder hoặc null
            return new ImageView(); // Trả về ImageView rỗng để tránh NullPointerException
        }
        ImageView cardView = new ImageView(cardImage);
        cardView.setPreserveRatio(true);
        cardView.setFitWidth(CARD_WIDTH);
        cardView.setUserData(card); // Lưu đối tượng WestCard vào ImageView
        return cardView;
    }

    // Tạo ImageView cho bài trên tay người chơi chính, có thể được chọn
    private ImageView createHandCardImageView(StandardCard card) {
        if (card == null) {
            System.err.println("Cannot create ImageView for null card.");
            return new ImageView();
        }
        // Quy ước tên file: rank_of_suit.png (ví dụ: ace_of_spades.png, 3_of_clubs.png)
        String rankStr = card.getRank().name().toLowerCase();
        if (card.getRank() == Rank.ACE || (card.getRank().getValue() >= 2 && card.getRank().getValue() <= 10)) {
            // Giữ nguyên cho ACE, 2-10 (ví dụ: ace, 2, 10)
            if (card.getRank() == Rank.ACE)
                rankStr = "ace"; // Hoặc "1" nếu file của bạn là "1_of_spades.png"
            else
                rankStr = String.valueOf(card.getRank().getValue());
        } // J, Q, K đã là chữ rồi (jack, queen, king)

        String suitStr = card.getSuit().name().toLowerCase(); // hearts, diamonds, clubs, spades
        String imagePath = "/com/myteam/game/images/cards/" + rankStr + "_of_" + suitStr + ".png";

        ImageView cardView = createGenericCardImageView(card, imagePath);
        cardView.setOnMouseClicked(this::handleCardClick); // Bài trên tay có thể click
        return cardView;
    }

    private ImageView createDisplayOnlyCardImageView(StandardCard card) {
        if (card == null) {
            System.err.println("Cannot create display-only ImageView for null card.");
            return new ImageView();
        }
        String rankStr = card.getRank().name().toLowerCase();
        if (card.getRank() == Rank.ACE || (card.getRank().getValue() >= 2 && card.getRank().getValue() <= 10)) {
            if (card.getRank() == Rank.ACE)
                rankStr = "ace";
            else
                rankStr = String.valueOf(card.getRank().getValue());
        }
        String suitStr = card.getSuit().name().toLowerCase();
        String imagePath = "/com/myteam/game/images/cards/" + rankStr + "_of_" + suitStr + ".png";

        return createGenericCardImageView(card, imagePath); // Không gán setOnMouseClicked
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
                if (player != null && player.getHand() != null) {
                    for (StandardCard card : player.getHand()) {
                        TienLenGameState gameState = logicController.getGameLogic().getCurrentGameState();
                        ImageView cardView;
                        if (gameState.getPlayers().indexOf(gameState.getCurrentPlayer()) != i
                                && i != MAIN_PLAYER_INDEX) {
                            cardView = new ImageView(cardBackImage); // Chỉ hiển thị bài
                            cardView.setPreserveRatio(true);
                            cardView.setFitWidth(CARD_WIDTH);
                        } else {
                            cardView = createHandCardImageView(card);
                        }
                        if (cardView != null)
                            cardArea.getChildren().add(cardView);
                    }
                }
            } else { // Đối thủ
                if (player != null && player.getHand() != null) {
                    ImageView cardBackView = new ImageView(cardBackImage);
                    cardBackView.setPreserveRatio(true);
                    cardBackView.setFitWidth(CARD_WIDTH);
                    cardArea.getChildren().add(cardBackView);
                    if (playerCardCountLabels[i] != null) {
                        playerCardCountLabels[i].setText(String.valueOf(player.getHand().size()));
                    }
                }
            }
        }
        clearSelectedCardsUI();
    }

    private void resetCardPosition(ImageView cardView) {
        if (cardView != null) {
            cardView.setTranslateY(0);
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

    @FXML
    void handleCardClick(MouseEvent event) {
        ImageView clickedCardView = (ImageView) event.getSource();

        if (selectedImageViews.contains(clickedCardView)) {
            resetCardPosition(clickedCardView);
            selectedImageViews.remove(clickedCardView);
        } else {

            clickedCardView.setTranslateY(CARD_POP_UP_TRANSLATE_Y);
            selectedImageViews.add(clickedCardView);
        }
        System.out.println("Selected cards: " + selectedImageViews.size());
        // updateActionButtonsState(logicController.getGameLogic().getCurrentGameState());
        // // Cập nhật nút Play dựa trên lựa chọn
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

    @Override
    public List<StandardCard> getSelectedWestCardsFromUI() {
        if (selectedImageViews.isEmpty()) {
            return Collections.emptyList(); // Hoặc new ArrayList<>()
        }
        List<StandardCard> cards = selectedImageViews.stream()
                .map(iv -> (StandardCard) iv.getUserData()) // Lấy WestCard từ UserData
                .filter(java.util.Objects::nonNull) // Bỏ qua nếu UserData không phải WestCard hoặc null
                .collect(Collectors.toList());

        // QUAN TRỌNG: Sắp xếp các lá bài đã chọn theo luật Tiến Lên
        // Điều này giúp TienLenMienBacGameLogic.isValidMove() dễ xử lý hơn.
        // Ví dụ: sắp xếp theo rank rồi đến suit.
        cards.sort(Comparator.comparing((StandardCard c) -> c.getRank().getValue()) // Sử dụng getValue() để so sánh số
                .thenComparing(c -> c.getSuit().ordinal())); // So sánh theo thứ tự enum của Suit
        return cards;
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

        // Sau khi gửi yêu cầu, các lá bài đã chọn nên được xóa khỏi UI (tay bài)
        // và selectedImageViews nên được clear.
        // Việc này sẽ được thực hiện bởi updateView() sau khi LogicController xử lý
        // xong.
        // Hoặc bạn có thể clear selectedImageViews ngay ở đây nếu muốn:
        // clearSelectedCardsUI(); // Tuy nhiên, nếu nước đi không hợp lệ, người chơi có
        // thể muốn giữ lại lựa chọn.
        // Tốt hơn là clear sau khi nước đi được chấp nhận.
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

    @Override
    public void clearSelectedCardsUI() {
        for (ImageView iv : selectedImageViews) {
            resetCardPosition(iv);
        }
        selectedImageViews.clear();
    }

    public void updateCenterArea(List<StandardCard> cardsOnTable) {
        if (cardCenterArea == null) {
            System.err.println("Card center area is null, cannot update.");
            return;
        }
        cardCenterArea.getChildren().clear();

        if (cardsOnTable != null && !cardsOnTable.isEmpty()) {
            for (StandardCard card : cardsOnTable) {
                ImageView cardView = createDisplayOnlyCardImageView(card);
                if (cardView != null) {
                    cardCenterArea.getChildren().add(cardView);
                }
            }
        } else {
            System.out.println("No cards on the table.");
        }
    }

    public void onGameStarted(TienLenGameState gameState) {
        System.out.println("Game started");
        updateView(gameState);
    }

    public void onGamePaused() {
        System.out.println("Game paused");
    }

    public void onGameResumed() {
        System.out.println("Game resumed");
        if (logicController != null) {
            updateView(logicController.getGameLogic().getCurrentGameState());
        }
    }

    @Override
    public void promptPlayerForAction(TienLenPlayer player, TienLenGameState gameState) {
        System.out.println("Prompting player " + player.getName() + " for action");
        // In a real implementation, would enable appropriate UI controls
        // For TienLen, this would enable card selection and play/pass buttons
    }

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

    public void displayOpponentCards(List<StandardCard> cards) {
        HBox OpponentCardsReveal = new HBox();
        OpponentCardsReveal.setSpacing(-50);
        for (StandardCard card : cards) {
            ImageView cardView = createDisplayOnlyCardImageView(card);
            OpponentCardsReveal.getChildren().add(cardView);
        }
        TienLenGameState gameState = logicController.getGameLogic().getCurrentGameState();
        TienLenPlayer currentPlayer = gameState.getCurrentPlayer();
        Pane cardArea = playerRevealCardAreas[gameState.getPlayers().indexOf(currentPlayer)];
        cardArea.getChildren().add(OpponentCardsReveal);

        logicController.executeAfterDelay(Duration.seconds(10), () -> {
            cardArea.getChildren().remove(OpponentCardsReveal);
        });
    }

    public void setMainPlayerIndex(int mainPlayerIndex) {
        this.MAIN_PLAYER_INDEX = mainPlayerIndex;
        System.out.println("Main player index set to: " + MAIN_PLAYER_INDEX);
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