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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
// import javafx.stage.Stage; // Không cần cho nút Deal
import javafx.stage.Stage;
import javafx.util.Duration;

// Imports từ logic game của bạn
import com.myteam.game.controller.PhomLogicController;
import com.myteam.game.model.core.card.StandardCard;
import com.myteam.game.model.core.enums.Rank;
import com.myteam.game.model.phom.player.PhomBotPlayer;
import com.myteam.game.model.phom.gamestate.PhomGameState;
import com.myteam.game.model.phom.player.PhomHumanPlayer;
import com.myteam.game.model.phom.player.PhomPlayer;
// import com.myteam.game.view.PhomGameViewController; // Interface này sẽ được implement bởi class này

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
// import java.util.Collections; // Nếu bạn muốn sắp xếp bài
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
// import java.util.stream.Collectors; // Không cần thiết cho các hàm cơ bản này

public class PhomGameViewController implements Initializable, PhomViewInterface /* , PhomGameViewController */ { // Bỏ
                                                                                                                 // comment
    // PhomGameViewController khi
    // bạn sẵn sàng implement đầy
    // đủ

    // <editor-fold desc="FXML Components">
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

    private Image cardBackImage;
    private final Set<ImageView> selectedImageViews = new HashSet<>();
    private final double CARD_POP_UP_TRANSLATE_Y = -20.0;
    private final double CARD_WIDTH = 75;
    private int MAIN_PLAYER_INDEX = 10; // Người chơi chính (index 0)

    private Pane[] playerCardAreas;
    private Pane[] playerEatAreas; // Khu vực hiển thị bài đã đánh của mỗi người
    private Pane[] playerEatenCardDisplayAreas; // Khu vực hiển thị bài đã ĂN của mỗi người (dùng playerPhomAreas)
    private Label[] playerCardCountLabels;
    private Pane[] playerRevealCardAreas;
    private Pane[] playerInfos; // Khu vực hiển thị thông tin người chơi

    private int numberOfPlayers = 4;

    // --- INITIALIZATION ---
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCardBackImage();

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
        updatePlayerUIVisibility(numberOfPlayers); // Cập nhật UI với số lượng người chơi hiện tại
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
        if (cardCenterCounter != null)
            cardCenterCounter.setVisible(false); // Ẩn bộ đếm bài giữa ban đầu
        if (cardCenterArea != null)
            cardCenterArea.getChildren().clear(); // Dọn dẹp khu vực giữa
        updateAllOpponentCardCountsVisibility(false); // Ẩn label đếm bài của đối thủ

    }

    public void setNumberOfPlayers(int numPlayers) {
        if (numPlayers < 1 || numPlayers > 4) {
            System.err.println("Invalid number of players: " + numPlayers);
            return;
        }
        this.numberOfPlayers = numPlayers;
    }

    private void updatePlayerUIVisibility(int numActivePlayers) {
        this.numberOfPlayers = numActivePlayers; // Cập nhật số lượng người chơi hiện tại

        for (int i = 0; i < playerInfos.length; i++) {
            boolean isActive = i < numActivePlayers;

            if (playerInfos[i] != null) {
                playerInfos[i].setVisible(isActive);
                playerInfos[i].setManaged(isActive);
            }
            if (playerCardAreas[i] != null) {
                playerCardAreas[i].setVisible(isActive);
                playerCardAreas[i].setManaged(isActive);
                if (!isActive)
                    playerCardAreas[i].getChildren().clear(); // Dọn dẹp nếu không active
            }

            // Đối với playerCardCountLabels và playerRevealCardAreas, chúng ta bắt đầu từ
            // index 1
            // và chỉ áp dụng cho đối thủ.
            if (i > 0) { // Bỏ qua người chơi chính (index 0) cho các label và reveal area này
                if (playerCardCountLabels[i] != null) {
                    playerCardCountLabels[i].setVisible(isActive); // Chỉ hiện khi người chơi đó active
                }
                if (playerRevealCardAreas[i] != null) {
                    playerRevealCardAreas[i].setVisible(isActive);
                    playerRevealCardAreas[i].setManaged(isActive);
                    if (!isActive)
                        playerRevealCardAreas[i].getChildren().clear();
                }
            }
        }
        // Đảm bảo khu vực bài của người chơi chính (nếu có) luôn được quản lý nếu nó
        // hiển thị
        // Điều này quan trọng nếu bạn muốn người chơi chính luôn hiện diện
        // (numActivePlayers >= 1)
        if (numActivePlayers > 0 && playerCardAreas[0] != null) {
            playerCardAreas[0].setVisible(true);
            playerCardAreas[0].setManaged(true);
        }
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

    // Tạo ImageView cho bài chỉ để hiển thị (ví dụ: bài đã ăn, bài đã đánh), không
    // click được
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

    // --- LOGIC CONTROLLER INJECTION ---
    public void setLogicController(PhomLogicController logicController) {
        this.logicController = logicController;
    }

    // --- UI UPDATE METHODS (Called by PhomLogicController via
    // PhomGameViewController interface) ---
    // Đây là hàm chính mà PhomLogicController sẽ gọi
    public void updateView(PhomGameState gameState) {
        if (logicController == null || gameState == null) {
            System.err.println("Cannot update view: LogicController or GameState is null.");
            return;
        }

        // Hiển thị tay bài
        updateAllPlayerHandsDisplay(gameState.getPlayers());

        // Hiển thị bài đã ĂN (sử dụng playerEatenCardDisplayAreas ~ playerXPhomArea)
        updateAllPlayerEatenCardsDisplay(gameState.getPlayers());

        // Hiển thị bài VỪA ĐÁNH RA của mỗi người chơi (sử dụng playerEatAreas)
        // Và làm nổi bật lá cardOnTable (lá bài toàn cục mới nhất để người sau ăn)
        updateAllPlayerLastDiscardDisplay(gameState.getPlayers(), gameState.getCardOnTable());

        // Cập nhật nọc bài
        updateCenterDeckDisplay(logicController.getGameLogic().getDeck().size());

        // Xử lý game over
        if (gameState.isGameOver()) {
            displayGameOver(gameState);
        }

        // Sau khi chia bài xong, nút Deal nên ẩn đi và các nút hành động game nên hiện
        // ra
        if (dealButton != null) {
            dealButton.setVisible(false);
            dealButton.setManaged(false);
        }
        // setGameActionButtonsVisible(true); // Hiện các nút cơ bản sau khi Deal
        updateAllOpponentCardCountsVisibility(true); // Hiện label đếm bài của đối thủ
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
            System.out.println("Ngu1");
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
                    System.out.println("Ngu2");
                    for (StandardCard card : meld) {
                        ImageView cardView = createDisplayOnlyCardImageView(card);
                        meldBox.getChildren().add(cardView);
                    }
                    meldArea.getChildren().add(meldBox);
                }
            }
        }
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
                    ImageView cardView = createDisplayOnlyCardImageView(card);
                    if (cardView != null)
                        eatenDisplayArea.getChildren().add(cardView);
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
                ImageView cardView = createDisplayOnlyCardImageView(lastCardDiscardedByThisPlayer);

                if (cardView != null) {
                    if (cardOnTableGlobal != null && lastCardDiscardedByThisPlayer.equals(cardOnTableGlobal)) {
                        cardView.setStyle("-fx-effect: dropshadow(gaussian, rgba(255,255,0,0.7), 10, 0.5, 0, 0);");
                    } else {
                        cardView.setStyle("");
                    }
                    playerSpecificDiscardArea.getChildren().add(cardView);
                }
            }
        }
    }

    @Override
    public void updateCenterDeckDisplay(int deckSize) {
        if (cardCenterArea == null || cardCenterCounter == null)
            return;
        cardCenterArea.getChildren().clear();

        if (deckSize > 0) {
            if (cardBackImage != null) {
                ImageView cardBackView = new ImageView(cardBackImage);
                cardBackView.setPreserveRatio(true);
                cardBackView.setFitWidth(CARD_WIDTH);
                cardCenterArea.getChildren().add(cardBackView);
            }
            cardCenterCounter.setText(String.valueOf(deckSize));
            cardCenterCounter.setVisible(true);
        } else {
            cardCenterCounter.setVisible(false);
        }
    }

    // --- UI EVENT HANDLERS ---
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

    @FXML
    void handleCardClick(MouseEvent event) {
        ImageView clickedCardView = (ImageView) event.getSource();

        if (selectedImageViews.contains(clickedCardView)) {
            resetCardPosition(clickedCardView);
            selectedImageViews.remove(clickedCardView);
        } else {
            // Logic chọn nhiều lá hoặc 1 lá tùy theo trạng thái game
            // Ví dụ: nếu đang trong giai đoạn đánh bài, chỉ cho chọn 1 lá
            // if (/* logicController.isPlayerInDiscardPhase(MAIN_PLAYER_INDEX) && */
            // selectedImageViews.size() >= 1) {
            // // Bỏ chọn lá cũ nếu chỉ được chọn 1
            // clearSelectedCardsUI();
            // }
            clickedCardView.setTranslateY(CARD_POP_UP_TRANSLATE_Y);
            selectedImageViews.add(clickedCardView);
        }
        System.out.println("Selected cards: " + selectedImageViews.size());
        // updateActionButtonsState(logicController.getGameLogic().getCurrentGameState());
        // // Cập nhật nút Play dựa trên lựa chọn
    }

    private void resetCardPosition(ImageView cardView) {
        if (cardView != null) {
            cardView.setTranslateY(0);
        }
    }

    @Override
    public void clearSelectedCardsUI() {
        for (ImageView iv : selectedImageViews) {
            resetCardPosition(iv);
        }
        selectedImageViews.clear();
    }

    // Các hàm xử lý nút khác (Play, Draw, Eat, Send, Exit) sẽ được thêm sau
    @FXML
    void handlePlayButton(ActionEvent event) {
        if (logicController == null) {
            return;
        }
        if (selectedImageViews.size() != 1) {
            showUIMessage("Chọn 1 lá để đánh."); // Gọi hàm showUIMessage đơn giản
            return;
        }
        ImageView selectedCardView = selectedImageViews.iterator().next();
        if (!(selectedCardView.getUserData() instanceof StandardCard)) {
            return;
        }
        StandardCard cardToPlay = (StandardCard) selectedCardView.getUserData();

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

    @FXML
    void handleSendButton(ActionEvent event) {
        // TODO
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

    public void promptPlayerToEatOrDraw(PhomPlayer player, StandardCard cardToEat, PhomGameState gameState) {
        PhomPlayer mainHuman = logicController.getGameLogic().getPlayers().get(MAIN_PLAYER_INDEX);
        if (player.equals(mainHuman)) {
            this.mainPlayerHasDrawnOrEatenThisTurn = false; // Bắt đầu lượt mới, reset cờ
        }
        // if (cardToEat != null) { System.out.println("UI: " + player.getName() + ",
        // eat " + cardToEat + " or draw.");}
        // else { System.out.println("UI: " + player.getName() + ", please draw."); }
        if (gameState != null) {
        } // updateActionButtonsState(gameState);
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
        // updateActionButtonsState(gameState);displayBotAction
        drawButton.setVisible(true);
        drawButton.setManaged(true);
    }

    public void showInvalidMoveMessage(String message) {
        showUIMessage("Không hợp lệ: " + message);
    }

    public void setMainPlayerIndex(int mainPlayerIndex) {
        this.MAIN_PLAYER_INDEX = mainPlayerIndex;
        System.out.println("Main player index set to: " + MAIN_PLAYER_INDEX);
    }

    public void showWinner(PhomPlayer winner, PhomGameState gameState) {
        showUIMessage("Game kết thúc! Người thắng: " + (winner != null ? winner.getName() : "Hòa"));
        if (gameState != null) {
        } // updateActionButtonsState(gameState);
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

    @Override
    public void displayBotAction(String text) {
        this.setMenuLabel(text);
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

    public void displayOpponentCards(List<StandardCard> cards) {
        HBox OpponentCardsReveal = new HBox();
        OpponentCardsReveal.setSpacing(-50);
        for (StandardCard card : cards) {
            ImageView cardView = createDisplayOnlyCardImageView(card);
            OpponentCardsReveal.getChildren().add(cardView);
        }
        PhomGameState gameState = logicController.getGameLogic().getCurrentGameState();
        PhomPlayer currentPlayer = gameState.getCurrentPlayer();
        Pane cardArea = playerRevealCardAreas[gameState.getPlayers().indexOf(currentPlayer)];
        cardArea.getChildren().add(OpponentCardsReveal);

        logicController.executeAfterDelay(Duration.seconds(10), () -> {
            cardArea.getChildren().remove(OpponentCardsReveal);
        });
    }

    public void displayGameOver(PhomGameState gameState) {

        setGameActionButtonsVisible(false);
        if (exitButton != null) {
            exitButton.setVisible(true);
            exitButton.setManaged(true);
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
                HBox OpponentCardsReveal = new HBox();
                OpponentCardsReveal.setSpacing(-50);
                for (StandardCard card : player.getHand()) {
                    ImageView cardView = createDisplayOnlyCardImageView(card);

                    OpponentCardsReveal.getChildren().add(cardView);
                }
                Pane cardArea = playerRevealCardAreas[index];
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

    public void onGameEnded(PhomGameState gameState, PhomPlayer winner) {
        System.out.println("Game ended. Winner: " + (winner != null ? winner.getName() : "None/Draw"));
        setGameActionButtonsVisible(false);
        dealButton.setVisible(true);
        dealButton.setManaged(true);
        updateAllOpponentCardCountsVisibility(false);

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

        // Hiển thị pop-up thông báo người thắng
        final String finalWinnerName = winnerName; // Cần biến final để dùng trong lambda
        // Chạy trên luồng UI của JavaFX
        javafx.application.Platform.runLater(() -> {
            showWinnerPopup(finalWinnerName);
        });
    }
}