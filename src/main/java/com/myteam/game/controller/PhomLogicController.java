package com.myteam.game.controller;

import com.myteam.game.viewcontroller.PhomViewInterface;
import com.myteam.game.model.core.card.StandardCard;
import com.myteam.game.model.game.PhomGameLogic;
import com.myteam.game.model.phom.gamestate.PhomGameState;
import com.myteam.game.model.phom.player.PhomPlayer;
import com.myteam.game.model.phom.player.PhomBotPlayer;
import com.myteam.game.model.phom.action.PhomPlayerAction;

import javafx.animation.PauseTransition;

import javafx.util.Duration;

public class PhomLogicController extends LogicController<StandardCard, PhomPlayer, PhomGameLogic> {
    private PhomViewInterface viewController;

    public PhomLogicController(PhomGameLogic gameLogic) {
        super(gameLogic);
        this.isGameRunning = false;
    }

    public void setViewController(PhomViewInterface viewController) {
        this.viewController = viewController;
    }

    @Override
    protected void processPlayerMove(PhomPlayer player, Object move) {
        try {
            if (!isGameRunning()) {
                return;
            }

            if (move instanceof PhomPlayerAction.DrawCardAction) {
                gameLogic.playerDrawCard();
                viewController.updateView(gameLogic.getCurrentGameState());

                if (viewController != null) {
                    viewController.promptPlayerToDiscard(player, gameLogic.getCurrentGameState());
                }

            } else if (move instanceof PhomPlayerAction.EatCardAction) {
                PhomPlayerAction.EatCardAction eatAction = (PhomPlayerAction.EatCardAction) move;
                StandardCard cardToEat = eatAction.getCard();

                gameLogic.playerEatCard(cardToEat);

                PhomGameState gameState = gameLogic.getCurrentGameState();
                viewController.updateView(gameState);

                if (viewController != null) {
                    viewController.promptPlayerToDiscard(player, gameLogic.getCurrentGameState());
                }

            } else if (move instanceof PhomPlayerAction.DiscardCardAction) {
                PhomPlayerAction.DiscardCardAction discardAction = (PhomPlayerAction.DiscardCardAction) move;
                StandardCard cardToDiscard = discardAction.getCard();

                gameLogic.humanDiscardCard(cardToDiscard);

                if (viewController != null) {
                    viewController.updateView(gameLogic.getCurrentGameState());
                }
                nextTurn();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in processPlayerMove: " + e.getMessage(), e);
        }
    }

    public void handleDeal() {
        try {
            gameLogic.startGame();
            this.isGameRunning = true;
            viewController.updateView(gameLogic.getCurrentGameState());
            viewController.promptPlayerToDiscard(gameLogic.getCurrentGameState().getCurrentPlayer(),
                    gameLogic.getCurrentGameState());
        } catch (Exception e) {
            throw new RuntimeException("Error in handleDeal: " + e.getMessage(), e);
        }
    }

    @Override
    protected void nextTurn() {
        try {
            if (!gameLogic.endGame()) {
                if (gameLogic.getCurrentPlayer().getNumOfTurn() == 4) {
                    gameLogic.playerMeldCard();
                    viewController.updateView(gameLogic.getCurrentGameState());
                }
                gameLogic.nextTurn();
                viewController.updateView(gameLogic.getCurrentGameState());
                checkAndPlayBotTurnIfNeeded();
            } else {
                gameLogic.playerMeldCard();
                viewController.updateView(gameLogic.getCurrentGameState());
                gameLogic.determineWinnerByScore();
                PhomPlayer winner = gameLogic.getWinnerPlayer();
                if (viewController != null) {
                    viewController.showGameOver(winner);
                }
                for (PhomPlayer player : gameLogic.getCurrentGameState().getPlayers()) {
                    int score = 0;
                    for (StandardCard card : player.getHand()) {
                        score += card.getRank().getValue();
                    }
                    System.out.println(player.getName() + "score: " + score);
                }
                System.out.println("Game Over! Winner: " + winner.getName());

                isGameRunning = false;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in nextTurn: " + e.getMessage(), e);
        }
    }

    @Override
    protected void checkAndPlayBotTurnIfNeeded() {
        try {
            if (!isGameRunning)
                return;
            PhomPlayer currentPlayer = gameLogic.getCurrentPlayer();

            if (currentPlayer instanceof PhomBotPlayer) {
                PhomBotPlayer bot = (PhomBotPlayer) currentPlayer;
                viewController.displayBotAction(bot.getName() + "'s turn");
                System.out.println(bot.getName() + "'s turn");
                for (StandardCard card : currentPlayer.getHand()) {
                    System.out.println(card.toString());
                }

                executeAfterDelay(Duration.seconds(2), () -> {
                    try {
                        viewController.displayBotAction(bot.getName() + " is deciding to eat or draw");
                        StandardCard topCard = gameLogic.getCardsOnTable();
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

                        if (viewController != null) {
                            viewController.updateView(gameLogic.getCurrentGameState());
                        }

                        if (bot.getHand().size() > 0) {
                            executeAfterDelay(Duration.seconds(2), () -> {
                                try {
                                    viewController.displayBotAction(bot.getName() + " is discarding");
                                    gameLogic.botDiscardCard();

                                    if (viewController != null) {
                                        viewController.updateView(gameLogic.getCurrentGameState());
                                    }

                                    executeAfterDelay(Duration.seconds(1), () -> {
                                        try {
                                            viewController.displayBotAction(bot.getName() + " turn ended");
                                            nextTurn();
                                        } catch (Exception e) {
                                            throw new RuntimeException("Error in bot end turn: " + e.getMessage(), e);
                                        }
                                    });
                                } catch (Exception e) {
                                    throw new RuntimeException("Error in bot discard: " + e.getMessage(), e);
                                }
                            });
                        } else {
                            System.out.println("LogicCtrl: Bot " + bot.getName()
                                    + " has no cards to discard. Moving to next turn after delay.");
                            executeAfterDelay(Duration.seconds(1), () -> {
                                try {
                                    nextTurn();
                                } catch (Exception e) {
                                    throw new RuntimeException("Error in bot skip turn: " + e.getMessage(), e);
                                }
                            });
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Error in bot action: " + e.getMessage(), e);
                    }
                });

            } else {
                if (viewController != null) {
                    StandardCard cardOnTable = gameLogic.getCardsOnTable();
                    boolean canEatThisCard = (cardOnTable != null && gameLogic.canFormPhom(currentPlayer, cardOnTable));
                    if (canEatThisCard) {
                        viewController.promptPlayerToEat(currentPlayer, gameLogic.getCurrentGameState());
                    } else {
                        viewController.promptPlayerToDraw(currentPlayer, gameLogic.getCurrentGameState());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in checkAndPlayBotTurnIfNeeded: " + e.getMessage(), e);
        }
    }

    public void executeAfterDelay(Duration duration, Runnable action) {
        try {
            PauseTransition delay = new PauseTransition(duration);
            delay.setOnFinished(event -> {
                try {
                    if (isGameRunning) {
                        action.run();
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error in delayed action: " + e.getMessage(), e);
                }
            });
            delay.play();
        } catch (Exception e) {
            throw new RuntimeException("Error in executeAfterDelay: " + e.getMessage(), e);
        }
    }

    public void playerRequestsDraw(PhomPlayer player) {
        try {
            processPlayerMove(player, new PhomPlayerAction.DrawCardAction());
        } catch (Exception e) {
            throw new RuntimeException("Error in playerRequestsDraw: " + e.getMessage(), e);
        }
    }

    public void playerRequestsEat(PhomPlayer player, StandardCard card) {
        try {
            processPlayerMove(player, new PhomPlayerAction.EatCardAction(card));
        } catch (Exception e) {
            throw new RuntimeException("Error in playerRequestsEat: " + e.getMessage(), e);
        }
    }

    public void playerRequestsDiscardSingleCard(PhomPlayer requestingPlayer, StandardCard cardToDiscard) {
        try {
            if (!isGameRunning) {
                if (viewController != null)
                    viewController.showInvalidMoveMessage("Game chưa bắt đầu!");
                return;
            }
            if (requestingPlayer == null || !requestingPlayer.equals(gameLogic.getCurrentPlayer())) {
                if (viewController != null)
                    viewController.showInvalidMoveMessage("Không phải lượt của bạn!");
                return;
            }
            if (!requestingPlayer.getHand().contains(cardToDiscard)) {
                if (viewController != null)
                    viewController.showInvalidMoveMessage("Bạn không có lá bài này.");
                return;
            }

            processPlayerMove(requestingPlayer, new PhomPlayerAction.DiscardCardAction(cardToDiscard));
        } catch (Exception e) {
            throw new RuntimeException("Error in playerRequestsDiscardSingleCard: " + e.getMessage(), e);
        }
    }

    public void markGameAsStopped() {
        this.isGameRunning = false;
        System.out.println("TienLenLogicController: Game logic marked as stopped by exiting.");
        // Không cần làm gì thêm ở đây nếu chấp nhận rủi ro PauseTransition
    }

}
