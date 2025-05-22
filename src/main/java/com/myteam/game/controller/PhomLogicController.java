package com.myteam.game.controller;

import com.myteam.game.model.core.card.WestCard;
import com.myteam.game.model.game.PhomGameLogic;
import com.myteam.game.model.phom.PhomGameState;
import com.myteam.game.model.phom.PhomPlayer;
import com.myteam.game.model.phom.PhomBotPlayer;
import com.myteam.game.model.phom.PhomPlayerAction;
import com.myteam.game.PhomGameViewController;

import java.util.List;

/**
 * LogicController implementation for Phom game
 * Handles game logic and coordinates between model and view
 */
public class PhomLogicController extends LogicController<WestCard, PhomPlayer, PhomGameLogic> {

    // Reference to the view controller (without JavaFX dependencies)
    private PhomGameViewController viewController;

    public PhomLogicController(PhomGameLogic gameLogic) {
        super(gameLogic);
        this.isGameRunning = false;
    }

    /**
     * Set the view controller
     *
     * @param viewController The view controller to use
     */
    public void setViewController(PhomGameViewController viewController) {
        this.viewController = viewController;
    }

    public void playerRequestsDiscardSingleCard(PhomPlayer requestingPlayer, WestCard cardToDiscard) {
        if (!isGameRunning) {
            // System.out.println("LogicCtrl: Game not running.");
            if (viewController != null)
                viewController.showInvalidMoveMessage("Game chưa bắt đầu!");
            return;
        }
        // Kiểm tra lượt chơi cơ bản
        if (requestingPlayer == null || !requestingPlayer.equals(gameLogic.getCurrentPlayer())) {
            // System.out.println("LogicCtrl: Not player's turn.");
            if (viewController != null)
                viewController.showInvalidMoveMessage("Không phải lượt của bạn!");
            return;
        }
        // Kiểm tra có bài không
        if (!requestingPlayer.getHand().contains(cardToDiscard)) {
            // System.out.println("LogicCtrl: Player doesn't have card.");
            if (viewController != null)
                viewController.showInvalidMoveMessage("Bạn không có lá bài này.");
            return;
        }
        // Kiểm tra điều kiện đánh bài cơ bản (ví dụ: 10 lá)
        if (requestingPlayer.getHand().size() < 10) {
            if (viewController != null)
                viewController.showInvalidMoveMessage("Cần 10 lá để đánh.");
            return;
        }

        // Gọi processPlayerMove
        processPlayerMove(requestingPlayer, new PhomPlayerAction.DiscardCardAction(cardToDiscard));
    }

    @Override
    protected void processPlayerMove(PhomPlayer player, Object move) {
        if (!isGameRunning()) {
            return;
        }

        // Handle different types of moves based on the action type
        if (move instanceof PhomPlayerAction.DrawCardAction) {
            // Player draws a card from the deck
            gameLogic.playerDrawCard();
            viewController.updateView(gameLogic.getCurrentGameState());

            if (viewController != null) {
                viewController.promptPlayerToDiscard(player, gameLogic.getCurrentGameState());
            }

        } else if (move instanceof PhomPlayerAction.EatCardAction) {
            // Player eats a card from the table
            PhomPlayerAction.EatCardAction eatAction = (PhomPlayerAction.EatCardAction) move;
            WestCard cardToEat = eatAction.getCard();

            // Implement eat card logic here
            gameLogic.playerEatCard(cardToEat);

            // Use the game's state to remove the card from the table
            PhomGameState gameState = gameLogic.getCurrentGameState();
            viewController.updateView(gameState);
            // viewController.updateDiscardPile

            // After eating, player must discard a card
            if (viewController != null) {
                viewController.promptPlayerToDiscard(player, gameLogic.getCurrentGameState());
            }

        } else if (move instanceof PhomPlayerAction.DiscardCardAction) {
            // Player discards a card
            PhomPlayerAction.DiscardCardAction discardAction = (PhomPlayerAction.DiscardCardAction) move;
            WestCard cardToDiscard = discardAction.getCard();

            gameLogic.humanDiscardCard(cardToDiscard); // Cập nhật model

            if (viewController != null) {
                viewController.updateView(gameLogic.getCurrentGameState()); // Cập nhật UI
            }
            nextTurn();

        } else if (move instanceof PhomPlayerAction.SendCardsAction) {

        }
    }

    public void handleDeal() {
        gameLogic.startGame();
        this.isGameRunning = true;
        viewController.updateView(gameLogic.getCurrentGameState());
        viewController.promptPlayerToDiscard(gameLogic.getCurrentGameState().getCurrentPlayer(),
                gameLogic.getCurrentGameState());
        // Trong hàm này ta sẽ bảo view hiển thị nút đánh bài
    }

    @Override
    protected void nextTurn() {
        if (!gameLogic.endGame()) {
            if (gameLogic.getCurrentPlayer().getNumOfTurn() == 4) {
                gameLogic.playerMeldCard();
                // viewUpdateMeldCards
                viewController.updateView(gameLogic.getCurrentGameState());
            }
            gameLogic.nextTurn();
            viewController.updateView(gameLogic.getCurrentGameState());
            checkAndPlayBotTurnIfNeeded();
        }
    }

    protected void checkAndPlayBotTurnIfNeeded() {
        if (!isGameRunning)
            return; // Thêm kiểm tra này
        PhomPlayer currentPlayer = gameLogic.getCurrentPlayer();

        if (currentPlayer instanceof PhomBotPlayer) {
            PhomBotPlayer bot = (PhomBotPlayer) currentPlayer;
            // PhomGameState gameStateForView; // Không cần lấy gameState ở đây nữa nếu cập
            // nhật đúng lúc

            WestCard topCard = gameLogic.getCardsOnTable();
            boolean botActed = false; // Cờ để xem bot có ăn/bốc không, để biết có cần prompt đánh không

            if (topCard != null && bot.decideToEat(topCard)) {
                System.out.println("LogicCtrl: Bot " + bot.getName() + " eats " + topCard);
                gameLogic.playerEatCard(topCard);
                if (viewController != null) {
                    // Lấy gameState MỚI NHẤT sau khi ăn
                    viewController.updateView(gameLogic.getCurrentGameState());
                }
                botActed = true;
            } else {
                if (!gameLogic.getDeck().isEmpty()) { // Chỉ bốc nếu nọc còn bài
                    System.out.println("LogicCtrl: Bot " + bot.getName() + " draws card.");
                    gameLogic.playerDrawCard();
                    if (viewController != null) {
                        // Lấy gameState MỚI NHẤT sau khi bốc
                        viewController.updateView(gameLogic.getCurrentGameState());
                    }
                    botActed = true;
                } else {
                    System.out.println(
                            "LogicCtrl: Bot " + bot.getName() + " cannot eat and deck is empty. Passing to discard.");
                }
            }

            // Sau khi ăn hoặc bốc (hoặc không làm gì nếu không ăn được và nọc hết)
            // Bot sẽ đánh bài
            // Hàm botDiscardCard của gameLogic sẽ tự lấy lá bài từ bot.decideDiscard()
            // và cập nhật model.
            if (bot.getHand().size() > 0) { // Chỉ đánh nếu bot còn bài
                System.out.println("LogicCtrl: Bot " + bot.getName() + " is discarding.");
                gameLogic.botDiscardCard(); // Hàm này nên bao gồm bot.decideDiscard() và cập nhật tay bot
                if (viewController != null) {
                    // Lấy gameState MỚI NHẤT sau khi bot đánh
                    viewController.updateView(gameLogic.getCurrentGameState());
                }
            } else {
                System.out.println("LogicCtrl: Bot " + bot.getName() + " has no cards to discard after action.");
            }

            nextTurn();

        } else { // Lượt của Human
            if (viewController != null) {
                WestCard cardOnTable = gameLogic.getCardsOnTable();
                boolean canEatThisCard = (cardOnTable != null && gameLogic.canFormPhom(currentPlayer, cardOnTable));
                viewController.promptPlayerToEatOrDraw(currentPlayer, canEatThisCard ? cardOnTable : null,
                        gameLogic.getCurrentGameState());
            }
        }
    }

    /**
     * Find the winner of the game based on current game state
     *
     * @return The winning player
     */
    private PhomPlayer findWinner() {
        PhomGameState gameState = gameLogic.getCurrentGameState();

        // If game state already has a winner, return it
        if (gameState.getWinner() != null) {
            return gameState.getWinner();
        }

        // Otherwise, determine winner based on game rules
        // This is a simplified implementation
        PhomPlayer winner = null;
        int bestScore = Integer.MAX_VALUE;

        for (PhomPlayer player : gameState.getPlayers()) {
            int unmeldedCards = player.getHand().size();

            // The player with the fewest unmelded cards wins
            if (unmeldedCards < bestScore) {
                bestScore = unmeldedCards;
                winner = player;
            }
        }

        return winner;
    }

    // API methods for ViewController to call

    public void playerRequestsDraw(PhomPlayer player) {
        processPlayerMove(player, new PhomPlayerAction.DrawCardAction());
    }

    public void playerRequestsEat(PhomPlayer player, WestCard card) {
        processPlayerMove(player, new PhomPlayerAction.EatCardAction(card));
    }

    public void playerRequestsDiscard(PhomPlayer player, List<WestCard> cards) {
        if (gameLogic.isValidMove(cards)) {
            WestCard card = cards.getFirst();
            processPlayerMove(player, new PhomPlayerAction.DiscardCardAction(card));
        } else {
            System.out.println("Error"); // Ở đây UI/UX sẽ thông báo lỗi ra màn hình
        }
    }

    public void playerRequestsSendCards(PhomPlayer player, WestCard cardToSend, List<WestCard> targetPhom,
            PhomPlayer targetPlayer) {
        // Kiểm tra xem có phải là pha gửi bài không
        if (gameLogic.isSendingPhase()) {
            // Tạo một hành động gửi bài
            PhomPlayerAction.SendCardsAction sendAction = new PhomPlayerAction.SendCardsAction(cardToSend, targetPhom,
                    targetPlayer);
            // Xử lý hành động gửi bài
            processPlayerMove(player, sendAction);
        } else {
            System.out.println("Error: Not in sending phase."); // Hoặc thông báo lỗi ra UI
            // Thông báo cho người chơi rằng không thể gửi bài lúc này
            if (viewController != null) {
                System.out.println("Hiện tại không phải giai đoạn gửi bài.");
            }
        }
    }

}