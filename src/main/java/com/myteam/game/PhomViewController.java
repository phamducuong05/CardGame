package com.myteam.game;

// Imports từ JavaFX
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent; // Có thể không cần nếu bạn không chuyển scene trực tiếp ở đây
import javafx.scene.Scene; // Có thể không cần
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

// Imports từ bộ code logic của bạn
import com.myteam.game.controller.MainGameController;
import com.myteam.game.controller.PhomLogicController;
// controller.PhomViewController sẽ được implement bởi class này

import com.myteam.game.model.core.card.WestCard; // Sử dụng WestCard
import com.myteam.game.model.core.enums.Rank; // Cần cho việc tạo đường dẫn ảnh
import com.myteam.game.model.core.enums.Suit; // Cần cho việc tạo đường dẫn ảnh
import com.myteam.game.model.phom.PhomGameState;
import com.myteam.game.model.phom.PhomPlayer;
import com.myteam.game.model.phom.PhomHumanPlayer; // Giả sử người chơi chính là Human
import com.myteam.game.model.phom.PhomBotPlayer; // Đối thủ có thể là Bot

import com.myteam.game.view.PhomGameViewController;
import com.myteam.game.view.PhomGameViewController;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class PhomViewController implements Initializable, com.myteam.game.controller.PhomViewController {

    // <editor-fold desc="FXML Components">
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
    private Button playButton; // Đổi tên từ discardButton để khớp FXML

    @FXML
    private HBox cardCenterArea;
    @FXML
    private Label cardCenterCounter;

    @FXML
    private HBox player1CardArea; // Tay bài người chơi chính (Human)
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
    private StackPane player1EatArea; // Bài đã đánh/ăn của Player 1
    @FXML
    private StackPane player2EatArea;
    @FXML
    private StackPane player3EatArea;
    @FXML
    private StackPane player4EatArea;

    @FXML
    private HBox player1PhomArea; // Khu vực phỏm đã hạ của Player 1
    @FXML
    private FlowPane player2PhomArea;
    @FXML
    private FlowPane player3PhomArea;
    @FXML
    private FlowPane player4PhomArea;
    // </editor-fold>

    // --- Biến thành viên cho UI và Logic ---
    private Pane[] playerCardDisplayAreas; // Mảng chứa các HBox/FlowPane hiển thị bài trên tay đối thủ
    private Pane[] playerEatDisplayAreas; // Mảng chứa các StackPane hiển thị bài đã đánh/ăn của đối thủ
    private Pane[] playerPhomDisplayAreas; // Mảng chứa các FlowPane/HBox hiển thị phỏm của đối thủ
    private Label[] playerCardCountLabels; // Mảng chứa các Label đếm bài của đối thủ

    private Image cardBackImage;
    private PhomLogicController logicController;
    private PhomPlayer mainHumanPlayer; // Tham chiếu đến người chơi chính (UI-controlled)

    private final String MAIN_PLAYER_NAME = "HumanPlayer1"; // Tên định danh cho người chơi chính
    private final int NUM_UI_PLAYERS = 4; // Số lượng khu vực người chơi trên UI

    private final double CARD_POP_UP_TRANSLATE_Y = -20.0;
    private Set<ImageView> selectedImageViews = new HashSet<>(); // Chỉ lưu trữ ImageView đang được chọn trên UI
    private final double CARD_WIDTH = 75;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("PhomViewController initializing...");
        loadCardBackImage();
        setupUiPlayerAreas();
        initializeGameLogic(); // Khởi tạo và kết nối với PhomLogicController

        // Thiết lập trạng thái ban đầu cho các nút (thường là chỉ nút Deal hiện)
        updateActionButtonsState(null); // gameState ban đầu là null
        updateAllOpponentCardCountsVisibility(false);
        if (cardCenterCounter != null)
            cardCenterCounter.setVisible(false);
        System.out.println("PhomViewController initialized.");
    }

    private void setupUiPlayerAreas() {
        // player1... là người chơi chính, không cần đưa vào mảng đối thủ
        playerCardDisplayAreas = new Pane[] { null, player2CardArea, player3CardArea, player4CardArea };
        playerEatDisplayAreas = new Pane[] { null, player2EatArea, player3EatArea, player4EatArea };
        playerPhomDisplayAreas = new Pane[] { null, player2PhomArea, player3PhomArea, player4PhomArea };
        playerCardCountLabels = new Label[] { null, player2Counter, player3Counter, player4Counter };
    }

    private void initializeGameLogic() {
        List<PhomPlayer> gamePlayers = new ArrayList<>();
        mainHumanPlayer = new PhomHumanPlayer(MAIN_PLAYER_NAME);
        gamePlayers.add(mainHumanPlayer);
        gamePlayers.add(new PhomBotPlayer("Bot2"));
        gamePlayers.add(new PhomBotPlayer("Bot3"));
        gamePlayers.add(new PhomBotPlayer("Bot4"));

        MainGameController orchestrator = new MainGameController();
        // Số lá bài (9 hoặc 10) sẽ do PhomGameLogic quản lý khi chia bài
        this.logicController = (PhomLogicController) orchestrator.selectGame("Phom", gamePlayers, 9); // Số 9 chỉ là
                                                                                                      // placeholder
        this.logicController.setViewController(this); // Quan trọng: liên kết logic với view này
        System.out.println("Game logic initialized and linked with PhomViewController.");
    }

    private ImageView createCardImageView(Image image, double fitWidth) {
        ImageView cardView = new ImageView(image);
        cardView.setPreserveRatio(true);
        cardView.setFitWidth(fitWidth);
        return cardView;
    }

    private void loadCardBackImage() {
        String cardBackPath = "/com/myteam/game/images/cards/back.png"; // Đường dẫn trong resources
        try (InputStream stream = getClass().getResourceAsStream(cardBackPath)) {
            if (stream != null) {
                cardBackImage = new Image(stream);
                if (cardBackImage.isError()) {
                    System.err.println("Error loading card back image (error flag): " + cardBackImage.getException());
                    cardBackImage = null;
                }
            } else {
                System.err.println("Card back image not found: " + cardBackPath);
            }
        } catch (Exception e) {
            System.err.println("Exception loading card back image: " + cardBackPath);
            e.printStackTrace();
            cardBackImage = null;
        }
    }

    // --- Xử lý sự kiện từ UI (gọi đến PhomLogicController) ---

    @FXML
    public void handleDealButton(ActionEvent event) {
        System.out.println("UI: Deal button clicked.");
        if (logicController != null) {
            // Dọn dẹp lựa chọn trên UI trước
            clearSelectedCardsUI();
            // Logic controller sẽ chịu trách nhiệm bắt đầu game và gọi lại onGameStarted ->
            // updateView
            logicController.startGame();
        }
    }

    @FXML
    public void handleDrawButton(ActionEvent event) {
        System.out.println("UI: Draw button clicked.");
        if (logicController != null && mainHumanPlayer != null) {
            logicController.playerRequestsDraw(mainHumanPlayer);
        }
    }

    @FXML
    public void handlePlayButton(ActionEvent event) { // Đã đổi tên thành playButton để khớp FXML
        System.out.println("UI: Play (Discard) button clicked.");
        if (logicController != null && mainHumanPlayer != null) {
            List<WestCard> cardsToDiscard = getSelectedWestCardsFromUI();
            if (cardsToDiscard.size() == 1) { // Luật phỏm thường chỉ đánh 1 lá
                // Logic controller sẽ xử lý việc xóa card khỏi tay người chơi trong model
                // và cập nhật game state, sau đó gọi updateView.
                logicController.playerRequestsDiscard(mainHumanPlayer, cardsToDiscard);
                clearSelectedCardsUI(); // Xóa lựa chọn trên UI sau khi gửi yêu cầu
            } else if (cardsToDiscard.isEmpty()) {
                System.out.println("UI: No card selected to play.");
                // Có thể hiển thị thông báo cho người dùng
            } else {
                System.out.println("UI: Please select only one card to play.");
                // Có thể hiển thị thông báo cho người dùng
            }
        }
    }

    @FXML
    public void handleEatButton(ActionEvent event) {
        System.out.println("UI: Eat button clicked.");
        if (logicController != null && mainHumanPlayer != null) {
            PhomGameState currentState = logicController.getGameLogic().getCurrentGameState();
            if (currentState != null && currentState.getCardOnTable() != null) {
                // Giả sử logicController.playerRequestsEat sẽ kiểm tra xem ăn có hợp lệ không
                logicController.playerRequestsEat(mainHumanPlayer, currentState.getCardOnTable());
            } else {
                System.out.println("UI: No card on table to eat or game state not available.");
            }
        }
    }

    @FXML
    public void handleSendButton(ActionEvent event) {
        System.out.println("UI: Send button clicked.");
        // TODO: Khi triển khai chức năng gửi bài
        // 1. Lấy các lá bài đã chọn (getSelectedWestCardsFromUI())
        // 2. Xác định phỏm của đối thủ mà người chơi muốn gửi vào (cần UI cho việc này)
        // 3. Gọi logicController.playerRequestsSend(mainHumanPlayer, cardToSend,
        // targetMeld, targetPlayer);
        if (logicController != null && mainHumanPlayer != null) {
            List<WestCard> cardsToSend = getSelectedWestCardsFromUI();
            if (!cardsToSend.isEmpty()) {
                // Cần logic để người dùng chọn phỏm của đối thủ để gửi vào
                System.out.println("UI: Send functionality not fully implemented. Selected cards: " + cardsToSend);
                // Ví dụ: logicController.playerRequestsSend(mainHumanPlayer, cardsToSend, ...);
                clearSelectedCardsUI();
            } else {
                System.out.println("UI: No cards selected to send.");
            }
        }
    }

    @FXML
    void handleExitButton(ActionEvent event) {
        System.out.println("UI: Exit button clicked. Closing application.");
        Stage stage = (Stage) exitButton.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void handleCardClick(MouseEvent event) {
        ImageView clickedCardView = (ImageView) event.getSource();
        if (clickedCardView.getParent() != player1CardArea) {
            return; // Chỉ cho phép click bài trên tay người chơi chính
        }

        PhomGameState currentState = logicController.getGameLogic().getCurrentGameState();
        if (currentState == null || currentState.getCurrentPlayer() != mainHumanPlayer) {
            System.out.println("UI: Not your turn or game not active.");
            return; // Không phải lượt hoặc game chưa sẵn sàng
        }

        WestCard clickedCardData = (WestCard) clickedCardView.getUserData();
        if (clickedCardData == null)
            return;

        // Logic chọn/bỏ chọn
        if (selectedImageViews.contains(clickedCardView)) {
            resetCardPosition(clickedCardView);
            selectedImageViews.remove(clickedCardView);
            System.out.println("UI: Card deselected: " + clickedCardData);
        } else {
            // Nếu hành động hiện tại chỉ cho phép chọn 1 lá (ví dụ: Đánh bài)
            // và đã có lá khác được chọn, thì bỏ chọn lá cũ.
            // (Cần kiểm tra action hiện tại, ví dụ nút Play đang active)
            if (playButton.isVisible() && !playButton.isDisable()) { // Giả sử Play chỉ cho đánh 1 lá
                if (!selectedImageViews.isEmpty()) {
                    clearSelectedCardsUI(); // Bỏ chọn tất cả các lá đã chọn trước đó
                }
            }
            // Nếu là các hành động cho phép chọn nhiều lá (Hạ Phỏm, Gửi) thì không cần
            // clear
            // mà chỉ cần add thêm. Cần điều chỉnh logic này tùy theo nút nào đang active.

            clickedCardView.setTranslateY(CARD_POP_UP_TRANSLATE_Y);
            selectedImageViews.add(clickedCardView);
            System.out.println("UI: Card selected: " + clickedCardData);
        }
        updateActionButtonsState(currentState); // Cập nhật trạng thái nút dựa trên lựa chọn mới
    }

    // --- Implement các phương thức từ interface controller.PhomViewController ---

    @Override
    public void setLogicController(PhomLogicController phomLogicController) {
        this.logicController = phomLogicController; // Được gọi từ initializeGameLogic
    }

    @Override
    public void updateView(PhomGameState gameState) {
        System.out.println("UI: updateView called by LogicController.");
        if (gameState == null) {
            System.err.println("UI: GameState is null in updateView. Resetting UI elements.");
            resetAllPlayerAreas();
            updateActionButtonsState(null);
            return;
        }

        // 1. Cập nhật tay bài người chơi chính (mainHumanPlayer)
        PhomPlayer currentPlayerFromState = gameState.getPlayers().stream()
                .filter(p -> p.getName().equals(MAIN_PLAYER_NAME))
                .findFirst().orElse(null);

        player1CardArea.getChildren().clear(); // Xóa bài cũ
        if (currentPlayerFromState != null) {
            // Giữ lại trạng thái selected cho những lá bài vẫn còn trên tay
            Set<WestCard> previouslySelectedData = new HashSet<>(getSelectedWestCardsFromUI());
            selectedImageViews.clear(); // Xóa các ImageView cũ, sẽ tạo lại

            List<WestCard> hand = new ArrayList<>(currentPlayerFromState.getHand());
            // Collections.sort(hand, Comparator.comparing(...)); // Sắp xếp nếu muốn

            for (WestCard card : hand) {
                ImageView cardView = createCardImageViewForCard(card);
                cardView.setOnMouseClicked(this::handleCardClick);
                if (previouslySelectedData.contains(card)) { // Nếu lá này đã được chọn trước đó
                    cardView.setTranslateY(CARD_POP_UP_TRANSLATE_Y);
                    selectedImageViews.add(cardView); // Thêm lại vào danh sách selected UI
                }
                player1CardArea.getChildren().add(cardView);
            }
        }

        // 2. Cập nhật bài của đối thủ (mặt sau và số lượng)
        updateOpponentDisplays(gameState);

        // 3. Cập nhật bộ bài giữa (Nọc)
        updateDeckDisplay(gameState);

        // 4. Cập nhật bài đã đánh/ăn của TẤT CẢ người chơi
        updateEatAreas(gameState);

        // 5. Cập nhật phỏm đã hạ của TẤT CẢ người chơi
        updatePhomAreas(gameState);

        // 6. Cập nhật trạng thái các nút hành động
        updateActionButtonsState(gameState);
        System.out.println("UI: updateView finished.");
    }

    @Override
    public void onGameStarted(PhomGameState gameState) {
        System.out.println("UI: onGameStarted called.");
        clearSelectedCardsUI(); // Xóa lựa chọn cũ khi ván mới bắt đầu
        resetAllPlayerAreas(); // Dọn dẹp UI trước khi vẽ ván mới
        updateView(gameState); // Vẽ trạng thái ban đầu của ván mới
        promptPlayerForAction(gameState.getCurrentPlayer(), gameState); // Thông báo lượt chơi
    }

    @Override
    public void onGamePaused() {
        System.out.println("UI: Game paused.");
        // TODO: Vô hiệu hóa các tương tác UI, hiển thị thông báo "Paused"
    }

    @Override
    public void onGameResumed() {
        System.out.println("UI: Game resumed.");
        // TODO: Kích hoạt lại UI
        if (logicController != null) {
            updateView(logicController.getGameLogic().getCurrentGameState());
        }
    }

    @Override
    public void onGameEnded(PhomGameState gameState, PhomPlayer winner) {
        System.out.println("UI: onGameEnded called.");
        updateView(gameState); // Cập nhật UI lần cuối để hiển thị trạng thái kết thúc

        if (winner != null) {
            System.out.println("UI: Winner is " + winner.getName() + "!");
            // TODO: Hiển thị thông báo người thắng cuộc trên UI
        } else {
            System.out.println("UI: Game ended. No specific winner or draw.");
            // TODO: Hiển thị thông báo kết quả (ví dụ: tính điểm, hòa)
        }
        // Nút Deal nên hiện lại
        if (dealButton != null) {
            dealButton.setVisible(true);
            dealButton.setManaged(true);
        }
        // Các nút hành động khác nên bị ẩn/vô hiệu hóa
        if (drawButton != null)
            drawButton.setVisible(false);
        if (playButton != null)
            playButton.setVisible(false);
        // ...
    }

    @Override
    public void promptPlayerForAction(PhomPlayer player, PhomGameState gameState) {
        System.out.println("UI: Prompting player " + player.getName() + " for action.");
        updateActionButtonsState(gameState); // Cập nhật nút dựa trên người chơi hiện tại và trạng thái game
        if (player.getName().equals(MAIN_PLAYER_NAME)) {
            System.out.println("UI: Your turn, " + player.getName() + "!");
            // TODO: Có thể highlight khu vực của người chơi chính hoặc hiển thị thông báo
            // rõ ràng trên UI
        } else {
            System.out.println("UI: Waiting for " + player.getName() + " (Bot) to play...");
            // TODO: Có thể hiển thị "Bot is thinking..."
        }
    }

    @Override
    public void promptPlayerToDiscard(PhomPlayer player, PhomGameState gameState) {
        System.out.println("UI: Prompting player " + player.getName() + " to discard a card.");
        updateActionButtonsState(gameState); // Nút Play/Discard nên được kích hoạt
        if (player.getName().equals(MAIN_PLAYER_NAME)) {
            System.out.println("UI: " + player.getName() + ", please select a card to discard and click Play.");
            // TODO: Có thể hiển thị thông báo rõ ràng trên UI
        }
    }

    // --- Các hàm tiện ích cho UI ---

    private void resetAllPlayerAreas() {
        if (player1CardArea != null)
            player1CardArea.getChildren().clear();
        if (player1EatArea != null)
            player1EatArea.getChildren().clear();
        if (player1PhomArea != null)
            player1PhomArea.getChildren().clear();

        for (int i = 1; i < NUM_UI_PLAYERS; i++) { // Bỏ qua player 1 (index 0)
            if (playerCardDisplayAreas[i] != null)
                playerCardDisplayAreas[i].getChildren().clear();
            if (playerEatDisplayAreas[i] != null)
                playerEatDisplayAreas[i].getChildren().clear();
            if (playerPhomDisplayAreas[i] != null)
                playerPhomDisplayAreas[i].getChildren().clear();
            if (playerCardCountLabels[i] != null)
                playerCardCountLabels[i].setVisible(false);
        }
        if (cardCenterArea != null)
            cardCenterArea.getChildren().clear();
        if (cardCenterCounter != null)
            cardCenterCounter.setVisible(false);
    }

    private void updateOpponentDisplays(PhomGameState gameState) {
        int opponentUiSlot = 1; // Bắt đầu từ slot UI thứ 2 (index 1 trong mảng)
        for (PhomPlayer player : gameState.getPlayers()) {
            if (!player.getName().equals(MAIN_PLAYER_NAME)) { // Nếu là đối thủ
                if (opponentUiSlot < NUM_UI_PLAYERS) {
                    Pane cardArea = playerCardDisplayAreas[opponentUiSlot];
                    Label countLabel = playerCardCountLabels[opponentUiSlot];

                    if (cardArea != null) {
                        cardArea.getChildren().clear();
                        if (player.getHand().size() > 0 && cardBackImage != null) {
                            ImageView backView = createCardImageView(cardBackImage, CARD_WIDTH);
                            cardArea.getChildren().add(backView);
                        }
                    }
                    if (countLabel != null) {
                        countLabel.setText(String.valueOf(player.getHand().size()));
                        countLabel.setVisible(player.getHand().size() > 0);
                    }
                    opponentUiSlot++;
                }
            }
        }
        // Ẩn các slot đối thủ không dùng đến (nếu số người chơi ít hơn số slot UI)
        for (int i = opponentUiSlot; i < NUM_UI_PLAYERS; i++) {
            if (playerCardDisplayAreas[i] != null)
                playerCardDisplayAreas[i].getChildren().clear();
            if (playerCardCountLabels[i] != null)
                playerCardCountLabels[i].setVisible(false);
        }
    }

    private void updateDeckDisplay(PhomGameState gameState) {
        if (cardCenterArea == null || cardCenterCounter == null)
            return;

        cardCenterArea.getChildren().clear();
        int deckSize = 0;
        if (logicController != null && logicController.getGameLogic().getDeck() != null) {
            deckSize = logicController.getGameLogic().getDeck().size();
        }

        if (deckSize > 0 && cardBackImage != null) {
            ImageView deckView = createCardImageView(cardBackImage, CARD_WIDTH);
            cardCenterArea.getChildren().add(deckView);
        }
        cardCenterCounter.setText(String.valueOf(deckSize));
        // Hiện counter nếu còn bài trong nọc hoặc nếu nút Deal đang hiện (game chưa bắt
        // đầu)
        cardCenterCounter.setVisible(deckSize > 0 || (dealButton != null && dealButton.isVisible()));
    }

    private void updateEatAreas(PhomGameState gameState) {
        // Hiển thị lá bài trên cùng trong chồng bài rác của mỗi người chơi
        // Player 1 (Human)
        PhomPlayer human = gameState.getPlayers().stream().filter(p -> p.getName().equals(MAIN_PLAYER_NAME)).findFirst()
                .orElse(null);
        player1EatArea.getChildren().clear();
        if (human != null && !human.getDiscardCards().isEmpty()) {
            WestCard topDiscard = human.getDiscardCards().get(human.getDiscardCards().size() - 1);
            player1EatArea.getChildren().add(createCardImageViewForCard(topDiscard));
        }

        // Đối thủ
        int opponentUiSlot = 1;
        for (PhomPlayer player : gameState.getPlayers()) {
            if (!player.getName().equals(MAIN_PLAYER_NAME)) {
                if (opponentUiSlot < NUM_UI_PLAYERS && playerEatDisplayAreas[opponentUiSlot] != null) {
                    playerEatDisplayAreas[opponentUiSlot].getChildren().clear();
                    if (!player.getDiscardCards().isEmpty()) {
                        WestCard topDiscard = player.getDiscardCards().get(player.getDiscardCards().size() - 1);
                        playerEatDisplayAreas[opponentUiSlot].getChildren().add(createCardImageViewForCard(topDiscard));
                    }
                }
                opponentUiSlot++;
            }
        }
    }

    private void updatePhomAreas(PhomGameState gameState) {
        // Hiển thị các phỏm đã hạ của mỗi người chơi
        // Player 1 (Human)
        PhomPlayer human = gameState.getPlayers().stream().filter(p -> p.getName().equals(MAIN_PLAYER_NAME)).findFirst()
                .orElse(null);
        player1PhomArea.getChildren().clear();
        if (human != null && !human.getAllPhoms().isEmpty()) {
            for (List<WestCard> phom : human.getAllPhoms()) {
                player1PhomArea.getChildren().add(createPhomDisplayGroup(phom));
            }
        }
        // Đối thủ
        int opponentUiSlot = 1;
        for (PhomPlayer player : gameState.getPlayers()) {
            if (!player.getName().equals(MAIN_PLAYER_NAME)) {
                if (opponentUiSlot < NUM_UI_PLAYERS && playerPhomDisplayAreas[opponentUiSlot] != null) {
                    playerPhomDisplayAreas[opponentUiSlot].getChildren().clear();
                    if (!player.getAllPhoms().isEmpty()) {
                        for (List<WestCard> phom : player.getAllPhoms()) {
                            playerPhomDisplayAreas[opponentUiSlot].getChildren().add(createPhomDisplayGroup(phom));
                        }
                    }
                }
                opponentUiSlot++;
            }
        }
    }

    private HBox createPhomDisplayGroup(List<WestCard> phomCards) {
        HBox phomGroup = new HBox(-25); // Các lá bài chồng lên nhau một chút
        phomGroup.setPadding(new javafx.geometry.Insets(0, 5, 0, 0)); // Khoảng cách giữa các phỏm
        for (WestCard card : phomCards) {
            phomGroup.getChildren().add(createCardImageViewForCard(card));
        }
        return phomGroup;
    }

    private ImageView createCardImageViewForCard(WestCard card) {
        String imagePath = getCardImagePath(card);
        Image cardImage = loadImage(imagePath);
        ImageView cardView = createCardImageView(cardImage != null ? cardImage : cardBackImage, CARD_WIDTH); // Dùng
                                                                                                             // back nếu
                                                                                                             // ảnh lỗi
        cardView.setUserData(card); // Gắn đối tượng WestCard vào ImageView
        return cardView;
    }

    private String getCardImagePath(WestCard card) {
        if (card == null)
            return "/com/myteam/game/images/cards/back.png"; // Default

        String rankStr = card.getRank().name().toLowerCase();
        // Chuyển đổi Rank số thành chuỗi số
        if (card.getRank().getValue() >= 2 && card.getRank().getValue() <= 10) {
            rankStr = String.valueOf(card.getRank().getValue());
        } else { // Ace, Jack, Queen, King giữ nguyên tên tiếng Anh lower case
            rankStr = card.getRank().name().toLowerCase();
        }
        // Đặc biệt cho Ace nếu file ảnh của bạn là '1'
        if (card.getRank() == Rank.ACE)
            rankStr = "ace"; // Hoặc "1" tùy tên file của bạn

        String suitStr = card.getSuit().name().toLowerCase(); // hearts, diamonds, clubs, spades
        return String.format("/com/myteam/game/images/cards/%s_of_%s.png", rankStr, suitStr);
    }

    private Image loadImage(String path) {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream != null) {
                Image image = new Image(stream);
                if (image.isError()) {
                    System.err.println("Error loading image (error flag): " + path + " - " + image.getException());
                    return null;
                }
                return image;
            } else {
                System.err.println("Image file not found: " + path);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Exception loading image: " + path);
            e.printStackTrace();
            return null;
        }
    }

    private void updateActionButtonsState(PhomGameState gameState) {
        boolean gameIsActive = logicController != null && logicController.isGameRunning() && gameState != null
                && !gameState.isGameOver();
        PhomPlayer currentPlayerInLogic = gameIsActive ? gameState.getCurrentPlayer() : null;
        boolean isMyTurn = gameIsActive && mainHumanPlayer != null && currentPlayerInLogic == mainHumanPlayer;
        int mainPlayerHandSize = 0;
        if (gameIsActive && mainHumanPlayer != null) {
            PhomPlayer humanState = gameState.getPlayers().stream().filter(p -> p.getName().equals(MAIN_PLAYER_NAME))
                    .findFirst().orElse(null);
            if (humanState != null)
                mainPlayerHandSize = humanState.getHand().size();
        }

        // Nút Deal
        dealButton.setVisible(!gameIsActive || (gameState != null && gameState.isGameOver()));
        dealButton.setManaged(dealButton.isVisible());

        // Nút Draw
        boolean canDraw = false;
        if (isMyTurn) {
            // Logic Phỏm: Được bốc khi có 9 lá VÀ chưa có hành động (ăn/bốc) trong lượt.
            // Giả sử: PhomLogicController hoặc PhomGameLogic sẽ quản lý cờ "đã hành động
            // trong lượt".
            // Tạm thời, chỉ cho bốc nếu có 9 lá và còn bài trong nọc.
            boolean deckHasCards = logicController.getGameLogic().getDeck().size() > 0;
            // Cần một cách để biết người chơi đã ăn hay chưa để vô hiệu hóa nút bốc.
            // Ví dụ, nếu `gameState.getCardOnTable()` != null và `canEat` là true, thì nút
            // Draw có thể disable.
            // Hoặc PhomPlayer có cờ `hasEatenThisTurn`.
            canDraw = deckHasCards && (mainPlayerHandSize == 9); // Điều kiện đơn giản hóa
            // Nâng cao: if (logicController.canPlayerDraw(mainHumanPlayer, gameState))
        }
        drawButton.setVisible(canDraw);
        drawButton.setDisable(!canDraw);
        drawButton.setManaged(canDraw);

        // Nút Play (Đánh bài)
        boolean canPlay = false;
        if (isMyTurn) {
            // Logic Phỏm: Được đánh khi có 10 lá VÀ đã chọn 1 lá.
            boolean hasOneCardSelected = selectedImageViews.size() == 1;
            canPlay = hasOneCardSelected && (mainPlayerHandSize == 10); // Điều kiện đơn giản hóa
            // Nâng cao: if (logicController.canPlayerDiscard(mainHumanPlayer,
            // getSelectedWestCardsFromUI(), gameState))
        }
        playButton.setVisible(canPlay);
        playButton.setDisable(!canPlay);
        playButton.setManaged(canPlay);

        // Nút Eat
        boolean canEat = false;
        if (isMyTurn && gameState.getCardOnTable() != null) {
            // Kiểm tra xem có thể tạo phỏm với lá bài trên bàn không
            canEat = logicController.getGameLogic().canFormPhom(mainHumanPlayer, gameState.getCardOnTable());
        }
        eatButton.setVisible(canEat);
        eatButton.setDisable(!canEat);
        eatButton.setManaged(canEat);
        // Nếu ăn được, nút Bốc có thể bị vô hiệu hóa hoặc ẩn đi
        if (canEat && drawButton.isVisible()) {
            // drawButton.setDisable(true); // Hoặc tùy theo luật chơi của bạn
        }

        // Nút Send (Gửi bài) - Tạm thời ẩn
        boolean canSend = false; // TODO: Logic khi nào có thể gửi bài
        // Ví dụ: isMyTurn && logicController.isInSendingPhase(gameState) &&
        // logicController.canPlayerSendCards(mainHumanPlayer,
        // getSelectedWestCardsFromUI(), gameState)
        sendButton.setVisible(canSend);
        sendButton.setDisable(!canSend);
        sendButton.setManaged(canSend);
    }

    private void clearSelectedCardsUI() {
        for (ImageView iv : selectedImageViews) {
            resetCardPosition(iv);
        }
        selectedImageViews.clear();
    }

    private void resetCardPosition(ImageView cardView) {
        if (cardView != null) {
            cardView.setTranslateY(0);
        }
    }

    private List<WestCard> getSelectedWestCardsFromUI() {
        if (selectedImageViews.isEmpty()) {
            return Collections.emptyList();
        }
        return selectedImageViews.stream()
                .map(iv -> (WestCard) iv.getUserData())
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void updateAllOpponentCardCountsVisibility(boolean visible) {
        for (int i = 1; i < NUM_UI_PLAYERS; i++) { // Bỏ qua player 1 (index 0)
            if (playerCardCountLabels[i] != null) {
                playerCardCountLabels[i].setVisible(visible);
            }
            // Nếu không visible, cũng nên xóa hình ảnh lá bài úp của đối thủ
            if (!visible && playerCardDisplayAreas[i] != null) {
                playerCardDisplayAreas[i].getChildren().clear();
            }
        }
    }
}