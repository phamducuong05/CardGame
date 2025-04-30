package com.mycompany.javafxapp;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert; // <<< THÊM IMPORT NÀY
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent; // Cần import MouseEvent
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.util.Duration;
import java.util.Set;

public class DealController implements Initializable {
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private HBox player1Area; // Dưới
    @FXML
    private HBox player3Area; // Trên
    @FXML
    private VBox player2Area; // Phải
    @FXML
    private VBox player4Area; // Trái
    @FXML
    private Button dealButton;
    @FXML
    private Button nextTurnButton;
    @FXML
    private Button exitButton;

    @FXML
    private AnchorPane centerPane;
    @FXML
    private HBox playedPileArea;
    @FXML
    private Button playButton;

    private Deck deck = new Deck(); // Tạo bộ bài sẵn
    private Image cardBackImage;
    private List<List<Card>> playerHands;

    private int currentPlayerIndex = 0;

    private Pane[] playerAreas;

    private final double CARD_POP_UP_AMOUNT = 20.0;
    private Set<ImageView> selectedCards = new HashSet<>();

    private ImageView topPlayedCardView = null;
    private List<Card> playedPile = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCardBackImage();
        playerHands = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            playerHands.add(new ArrayList<>());
        }

        playerAreas = new Pane[] { player1Area, player2Area, player3Area, player4Area };

        if (nextTurnButton != null) { // Kiểm tra null phòng trường hợp FXML chưa có
            nextTurnButton.setVisible(false);
            nextTurnButton.setManaged(false); // Không chiếm không gian layout
        }
        if (playButton != null) {
            playButton.setVisible(false);
            playButton.setManaged(false);
            playButton.setDisable(true); // Đảm bảo disable ban đầu
        }
    }

    public void loadCardBackImage() {
        String cardBackPath = "/com/mycompany/javafxapp/images/cards/back.png";
        cardBackImage = new Image(getClass().getResourceAsStream(cardBackPath));

    }

    @FXML
    void handleDealButton(ActionEvent event) {
        System.out.println("Dealing cards...");
        deck.resetAndShuffle(); // Xáo lại bộ bài

        for (List<Card> hand : playerHands) {
            hand.clear();
        }
        selectedCards.clear();
        playedPile.clear();
        playedPileArea.getChildren().clear();

        // Chia 13 lá cho mỗi người
        for (int cardNum = 0; cardNum < 13; cardNum++) {
            for (int playerIndex = 0; playerIndex < 4; playerIndex++) {
                Card dealtCard = deck.dealCard();
                if (dealtCard != null) {
                    playerHands.get(playerIndex).add(dealtCard);
                }
            }
        }

        currentPlayerIndex = 0;

        redisplayAllHands();
        nextTurnButton.setVisible(true);
        nextTurnButton.setManaged(true);
        updatePlayButtonState();
    }

    @FXML
    void handleNextTurnButton(ActionEvent event) {
        for (ImageView selectedCard : selectedCards) {
            resetCardPosition(selectedCard);
        }

        if (playButton != null) {
            playButton.setVisible(false);
            playButton.setManaged(false);
            playButton.setDisable(true);
        }
        selectedCards.clear();

        int previousPlayerIndex = currentPlayerIndex;
        currentPlayerIndex = (currentPlayerIndex + 1) % 4;
        System.out.println("Next: " + currentPlayerIndex); // Chuyển sang người chơi tiếp theo
        displayHand(previousPlayerIndex);
        displayHand(currentPlayerIndex);
    }

    private void redisplayAllHands() {
        for (int i = 0; i < playerAreas.length; i++) {
            displayHand(i); // Gọi hàm cập nhật cho từng người
        }
    }

    // Hàm hiển thị một tay bài lên khu vực được chỉ định
    private void displayHand(int playerIndex) {

        Pane displayArea = playerAreas[playerIndex];
        displayArea.getChildren().clear(); // Xóa bài cũ
        List<Card> hand = playerHands.get(playerIndex);
        double cardWidth = 60;

        boolean showFront = (playerIndex == currentPlayerIndex);

        for (Card card : hand) {
            ImageView cardView = new ImageView();
            cardView.setPreserveRatio(true);
            cardView.setFitWidth(cardWidth);

            String imagePath = "/com/mycompany/javafxapp/images/cards/" + card.getImage();

            Image cardImage = new Image(getClass().getResourceAsStream(imagePath));

            if (displayArea == player2Area) {
                cardView.setRotate(90); // Xoay 90 độ cho người chơi 2
            } else if (displayArea == player4Area) {
                cardView.setRotate(-90); // Xoay -90 độ cho người chơi 4
            }

            if (showFront) {
                cardView.setImage(cardImage);
                cardView.setStyle("-fx-cursor: hand;"); // Đổi con trỏ
                cardView.setOnMouseClicked(this::handleCardClick); // Gọi hàm xử lý click
                cardView.setUserData(card);
            } else {
                cardView.setStyle("-fx-cursor: default;");
                cardView.setOnMouseClicked(null); // Đảm bảo không có listener
                cardView.setUserData(null);
                cardView.setImage(cardBackImage);
            }
            displayArea.getChildren().add(cardView); // Thêm vào HBox/VBox
        }
    }

    private void handleCardClick(MouseEvent event) {
        ImageView clickedCardView = (ImageView) event.getSource();
        Card clickedCard = (Card) clickedCardView.getUserData();

        if (selectedCards.contains(clickedCardView)) {
            resetCardPosition(clickedCardView);
            selectedCards.remove(clickedCardView);
            System.out.println("Card deselected.");
        } else {
            if (currentPlayerIndex == 0 || currentPlayerIndex == 2) {
                clickedCardView.setTranslateX(0); // Đảm bảo X là 0
                clickedCardView.setTranslateY(currentPlayerIndex == 0 ? -CARD_POP_UP_AMOUNT : CARD_POP_UP_AMOUNT);
            } else {
                clickedCardView.setTranslateY(0); // Đảm bảo Y là 0
                clickedCardView.setTranslateX(currentPlayerIndex == 3 ? CARD_POP_UP_AMOUNT : -CARD_POP_UP_AMOUNT);
            }

            selectedCards.add(clickedCardView);
            System.out.println("Card selected.");
        }
        updatePlayButtonState();
    }

    private void updatePlayButtonState() {
        if (playButton != null) {
            boolean anyCardSelected = !selectedCards.isEmpty();
            playButton.setDisable(!anyCardSelected); // Enable nếu có lá chọn
            playButton.setVisible(anyCardSelected); // Hiện nếu có lá chọn
            playButton.setManaged(anyCardSelected);
        }
    }

    private void resetCardPosition(ImageView cardView) {
        cardView.setTranslateX(0);
        cardView.setTranslateY(0);
    }

    @FXML
    public void handleExitButton(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/com/mycompany/javafxapp/primary.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void handlePlayButton(ActionEvent event) {
        List<Card> currentHand = playerHands.get(currentPlayerIndex);
        Pane currentDisplayArea = playerAreas[currentPlayerIndex];
        List<ImageView> viewsToRemove = new ArrayList<>(selectedCards);
        List<Card> cardsPlayed = new ArrayList<>();
        for (ImageView cardView : viewsToRemove) {
            Card card = (Card) cardView.getUserData();
            if (card != null) {
                cardsPlayed.add(card);
                System.out.println("- " + card);
                // Xóa ImageView khỏi tay bài người chơi
                currentDisplayArea.getChildren().remove(cardView);
            }
        }
        currentHand.removeAll(cardsPlayed);
        playedPile.addAll(cardsPlayed);
        playedPileArea.getChildren().clear();
        double playedCardWidth = 60; // Có thể dùng kích thước khác cho bài ở giữa

        for (Card playedCard : cardsPlayed) {
            ImageView playedCardView = new ImageView(); // Tạo ImageView mới cho bài ở giữa
            playedCardView.setPreserveRatio(true);
            playedCardView.setFitWidth(playedCardWidth);
            String imagePath = "/com/mycompany/javafxapp/images/cards/" + playedCard.getImage();
            Image cardImage = new Image(getClass().getResourceAsStream(imagePath));
            playedCardView.setImage(cardImage);
            playedPileArea.getChildren().add(playedCardView);
        }
        selectedCards.clear();
        if (currentHand.isEmpty()) {
            Alert win = new Alert(AlertType.INFORMATION);
            win.setTitle("Game Over");
            win.setHeaderText("Player " + (currentPlayerIndex + 1) + " wins!");
            win.setContentText("Congratulations!");
            win.showAndWait();
            for (List<Card> hand : playerHands) {
                hand.clear();
            }
            playedPile.clear();
            playedPileArea.getChildren().clear();
            for (Pane area : playerAreas) {
                area.getChildren().clear();
            }
            currentPlayerIndex = 0; // Reset lại chỉ số người chơi
            if (nextTurnButton != null) {
                nextTurnButton.setVisible(false);
                nextTurnButton.setManaged(false);
            }
            updatePlayButtonState();
            return;
        }
        updatePlayButtonState(); // Gọi để ẩn/disable nút Play
        handleNextTurnButton(null);
    }
}
