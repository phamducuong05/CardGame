package com.myteam.game.controller;

import com.myteam.game.model.core.card.WestCard;
import com.myteam.game.model.game.PhomGameLogic;
import com.myteam.game.model.phom.PhomGameState;
import com.myteam.game.model.phom.PhomPlayer;
import com.myteam.game.model.phom.PhomBotPlayer;
import com.myteam.game.model.phom.PhomPlayerAction;

import javafx.animation.PauseTransition;

import com.myteam.game.PhomGameViewController;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.util.function.Consumer;
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
        } else {
            gameLogic.determineWinnerByScore();
            PhomPlayer winner = gameLogic.getWinnerPlayer();
            if (viewController != null) {
                viewController.showGameOver(winner);
            }
            for (PhomPlayer player : gameLogic.getCurrentGameState().getPlayers()) {
                System.out.println(player.getName() + "score: " + player.calculateScore());
            }
            System.out.println("Game Over! Winner: " + winner.getName());

            isGameRunning = false;
        }
    }

    @Override
    protected void checkAndPlayBotTurnIfNeeded() {
        if (!isGameRunning)
            return;
        PhomPlayer currentPlayer = gameLogic.getCurrentPlayer();

        if (currentPlayer instanceof PhomBotPlayer) {
            PhomBotPlayer bot = (PhomBotPlayer) currentPlayer;
            viewController.displayOpponentCards(currentPlayer.getHand());
            viewController.displayBotAction(bot.getName() + "'s turn");
            System.out.println(bot.getName() + "'s turn");
            for (WestCard card : currentPlayer.getHand()) {
                System.out.println(card.toString());
            }

            // Bước 1: Bot quyết định Ăn hoặc Bốc (Quyết định ngay, thực thi sau delay nhỏ)
            executeAfterDelay(Duration.seconds(2), () -> { // Độ trễ nhỏ trước khi Bot hành động đầu tiên
                viewController.displayBotAction(bot.getName() + " is deciding to eat or draw");
                WestCard topCard = gameLogic.getCardsOnTable();
                boolean botAte = false;

                if (topCard != null && bot.decideToEat(topCard)) {
                    viewController.displayBotAction(bot.getName() + " eats " + topCard);
                    gameLogic.playerEatCard(topCard);
                    botAte = true;
                } else {
                    if (!gameLogic.getDeck().isEmpty()) {
                        viewController.displayBotAction(bot.getName() + " draws card");
                        gameLogic.playerDrawCard();
                    } else {
                        System.out.println("Bot " + bot.getName() + " cannot eat and deck is empty");
                    }
                }

                // Cập nhật UI sau khi ăn/bốc
                if (viewController != null) {
                    viewController.updateView(gameLogic.getCurrentGameState());
                }

                // Bước 2: Bot Đánh Bài (sau một độ trễ nữa)
                // Chỉ thực hiện nếu bot còn bài
                if (bot.getHand().size() > 0) {
                    executeAfterDelay(Duration.seconds(2), () -> { // Độ trễ trước khi đánh
                        viewController.displayBotAction(bot.getName() + " is discarding");
                        gameLogic.botDiscardCard(); // Hàm này bao gồm bot.decideDiscard()

                        // Cập nhật UI sau khi đánh
                        if (viewController != null) {
                            viewController.updateView(gameLogic.getCurrentGameState());
                        }

                        // Bước 3: Chuyển lượt (sau khi tất cả hành động của Bot đã xong)
                        executeAfterDelay(Duration.seconds(1), () -> { // Delay nhỏ trước khi chuyển lượt
                            viewController.displayBotAction(bot.getName() + " turn ended");
                            nextTurn();
                        });
                    });
                } else {
                    // Bot không còn bài để đánh (có thể đã Ù hoặc lỗi logic)
                    System.out.println("LogicCtrl: Bot " + bot.getName()
                            + " has no cards to discard. Moving to next turn after delay.");
                    executeAfterDelay(Duration.seconds(1), () -> {
                        nextTurn();
                    });
                }
            });

        } else { // Lượt của Human
            if (viewController != null) {
                WestCard cardOnTable = gameLogic.getCardsOnTable();
                boolean canEatThisCard = (cardOnTable != null && gameLogic.canFormPhom(currentPlayer, cardOnTable));
                if (canEatThisCard) {
                    viewController.promptPlayerToEat(currentPlayer, gameLogic.getCurrentGameState());
                } else {
                    viewController.promptPlayerToDraw(currentPlayer, gameLogic.getCurrentGameState());
                    // viewController.promptPlayerToEatOrDraw(currentPlayer, canEatThisCard ?
                    // cardOnTable : null,
                    // gameLogic.getCurrentGameState());
                }
            }
        }
    }

    /**
     * Hàm tiện ích để thực thi một hành động sau một khoảng thời gian trễ.
     * 
     * @param duration Thời gian trễ
     * @param action   Hành động cần thực thi
     */
    public void executeAfterDelay(Duration duration, Runnable action) {
        PauseTransition delay = new PauseTransition(duration);
        delay.setOnFinished(event -> {
            if (isGameRunning) { // Chỉ thực thi nếu game vẫn đang chạy
                action.run();
            }
        });
        delay.play();
    }

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