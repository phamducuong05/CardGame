package com.myteam.game.controller;

import com.myteam.game.model.core.card.WestCard;
import com.myteam.game.model.game.PhomGameLogic;
import com.myteam.game.model.phom.PhomGameState;
import com.myteam.game.model.phom.PhomPlayer;
import com.myteam.game.model.phom.PhomBotPlayer;
import com.myteam.game.model.phom.PhomPlayerAction;
import com.myteam.game.view.PhomGameViewController;

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
    }

    /**
     * Set the view controller
     *
     * @param viewController The view controller to use
     */
    public void setViewController(PhomGameViewController viewController) {
        this.viewController = viewController;
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
            viewController.updatePlayerHands(gameLogic.getCurrentGameState());

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
            viewController.updatePlayerHands(gameState);
            // viewController.updateDiscardPile

            // After eating, player must discard a card
            if (viewController != null) {
                viewController.promptPlayerToDiscard(player, gameLogic.getCurrentGameState());
            }

        } else if (move instanceof PhomPlayerAction.DiscardCardAction) {
            // Player discards a card
            PhomPlayerAction.DiscardCardAction discardAction = (PhomPlayerAction.DiscardCardAction) move;
            WestCard cardToDiscard = discardAction.getCard();
            // Implement discard logic here
            gameLogic.humanDiscardCard(cardToDiscard);
            // Add to cards on table in the game state
            PhomGameState gameState = gameLogic.getCurrentGameState();
            // update hand and updateDiscardPile
            viewController.updatePlayerHands(gameState);
            // viewController.updateDiscardPile

            // Move to next turn after discard
            nextTurn();

        } else if(move instanceof PhomPlayerAction.SendCardsAction) {

        }
    }

    public void handleDeal() {
        gameLogic.startGame();
        viewController.updatePlayerHands(gameLogic.getCurrentGameState());
        viewController.promptPlayerToDiscard(gameLogic.getCurrentGameState().getCurrentPlayer(), gameLogic.getCurrentGameState());
        // Trong hàm này ta sẽ bảo view hiển thị nút đánh bài
    }

    @Override
    protected void nextTurn() {
        if(!gameLogic.endGame()) {
            if (gameLogic.getCurrentPlayer().getNumOfTurn() == 4) {
                gameLogic.playerMeldCard();
                // viewUpdateMeldCards
                viewController.updatePlayerHands(gameLogic.getCurrentGameState());
            }
            gameLogic.nextTurn();
            viewController.updatePlayerHands(gameLogic.getCurrentGameState());
            checkAndPlayBotTurnIfNeeded();
        }
    }

    @Override
    protected void checkAndPlayBotTurnIfNeeded() {
        PhomPlayer currentPlayer = gameLogic.getCurrentPlayer();

        // If current player is a bot, play its turn automatically ở đây ta
        // sẽ chia ra các hàm ở trong phần logic để làm và sau đó
        // sẽ update view ở trong controller này luôn ứng với từng hành động
        // Đầu tiên check xem có ăn được không, nếu ăn được thì update displayHand
        // và update display discardPile
        // Sau khi check có ăn được không thì sẽ đến hành động bốc bài và đánh bài
        // Sau hành động đánh bài lại gọi đến update Hand và update discardPile
        // rồi lại gọi DiscardPile
        // Nếu sau khi chạy nextTurn và trong hàm checkAndPlayBot thấy người chơi hiện tại
        // không phải là Bot thì phải update PlayerHand và check xem người chơi có ăn được
        // hay không, nếu ăn được thì phải prompt eat
        // nếu không ăn được thì phải prompt draw và prompt playCard

        if (currentPlayer instanceof PhomBotPlayer) {
            PhomBotPlayer bot = (PhomBotPlayer) currentPlayer;
            PhomGameState gameState = gameLogic.getCurrentGameState();

            // First check if bot can/wants to eat the top card on table
            WestCard topCard = gameLogic.getCardsOnTable();
            if (topCard != null && bot.decideToEat(topCard)) {
                // Bot decides to eat card
                gameLogic.playerEatCard(topCard);
                viewController.updatePlayerHands(gameState);
                // viewController.updateDiscardPile
            }
            else {
                // Bot draws a card
                gameLogic.playerDrawCard();
                viewController.updatePlayerHands(gameLogic.getCurrentGameState());
            }
            gameLogic.botDiscardCard();
            viewController.updatePlayerHands(gameState);
            // viewController.updateDiscardPile
            nextTurn();

        } else {
            // Human player's turn - prompt for action via the view controller
            if (viewController != null) {
                if(gameLogic.canFormPhom(currentPlayer, gameLogic.getCardsOnTable())) {
                    //viewController.promptPlayerToEat();
                }
                else {
                    //viewController.promptPlayerToDraw();
                }
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
        if(gameLogic.isValidMove(cards)) {
            WestCard card = cards.getFirst();
            processPlayerMove(player, new PhomPlayerAction.DiscardCardAction(card));
        }
        else {
            System.out.println("Error"); // Ở đây UI/UX sẽ thông báo lỗi ra màn hình
        }
    }

    public void playerRequestsSendCards(PhomPlayer player, WestCard cardToSend, List<WestCard> targetPhom, PhomPlayer targetPlayer) {
        // Kiểm tra xem có phải là pha gửi bài không
        if (gameLogic.isSendingPhase()) {
            // Tạo một hành động gửi bài
            PhomPlayerAction.SendCardsAction sendAction = new PhomPlayerAction.SendCardsAction(cardToSend, targetPhom, targetPlayer);
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