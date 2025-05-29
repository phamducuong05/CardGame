package com.myteam.game.controller;

import com.myteam.game.viewcontroller.TienLenViewInterface;
import com.myteam.game.model.core.card.StandardCard;
import com.myteam.game.model.game.TienLenMienBacGameLogic;
import com.myteam.game.model.tienlen.gamestate.TienLenGameState;
import com.myteam.game.model.tienlen.player.TienLenPlayer;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

import com.myteam.game.model.tienlen.player.TienLenBotPlayer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * LogicController implementation for TienLen game
 * Handles game logic and coordinates between model and view
 */
public class TienLenLogicController extends LogicController<StandardCard, TienLenPlayer, TienLenMienBacGameLogic> {
    private Set<TienLenPlayer> playersSkippedThisRound = new HashSet<>();
    private TienLenPlayer lastPlayerWhoPlayedCards = null;
    private TienLenViewInterface viewController;

    public TienLenLogicController(TienLenMienBacGameLogic gameLogic) {
        super(gameLogic);
    }

    public void setViewController(TienLenViewInterface viewController) {
        this.viewController = viewController;
    }

    @Override
    protected void processPlayerMove(TienLenPlayer player, Object move) {
        try {
            if (move instanceof List) {
                @SuppressWarnings("unchecked")
                List<StandardCard> cardsToPlay = (List<StandardCard>) move;

                if (!player.getHand().containsAll(cardsToPlay)) {
                    if (viewController != null) {
                        viewController.showInvalidMoveMessage();
                        System.out.println(
                                "Player " + player.getName() + " tried to play cards not in their hand: "
                                        + cardsToPlay);
                        viewController.promptPlayerForAction(player, gameLogic.getCurrentGameState());
                    }
                    return;
                }

                if (gameLogic.isValidMove(cardsToPlay)) {
                    player.getHand().removeAll(cardsToPlay);
                    gameLogic.playCards(cardsToPlay);

                    lastPlayerWhoPlayedCards = player;
                    playersSkippedThisRound.clear();

                    if (viewController != null)
                        viewController.updateView(gameLogic.getCurrentGameState());

                    if (player.getHand().isEmpty()) {
                        handlePlayerWin(player);
                    } else {
                        gameLogic.setIsFirstturn(false);
                        nextTurn();
                    }
                } else {
                    if (viewController != null) {
                        viewController.showInvalidMoveMessage();
                        viewController.promptPlayerForAction(player, gameLogic.getCurrentGameState());
                    }
                }
            } else if (move instanceof String && "PASS".equals(move)) {
                TienLenGameState gameState = gameLogic.getCurrentGameState();
                if (gameState.getCardsOnTable().isEmpty()
                        && (lastPlayerWhoPlayedCards == null || player.equals(lastPlayerWhoPlayedCards))) {
                    if (viewController != null)
                        viewController.showUIMessage("Bạn phải đánh bài để bắt đầu vòng mới.");
                    viewController.promptPlayerForAction(player, gameState);
                    return;
                }

                playersSkippedThisRound.add(player);
                System.out.println(player.getName() + " passed. Skipped in round: " + playersSkippedThisRound.size());
                nextTurn();
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xử lý lượt chơi của người chơi: " + player.getName(), e);
        }
    }

    @Override
    protected void nextTurn() {
        try {
            if (gameLogic.getCurrentGameState().isGameOver()) {
                System.out.println("Game is already over. Cannot proceed to next turn.");
                if (viewController != null) {
                    viewController.onGameEnded(gameLogic.getCurrentGameState(), findWinner());
                }
                isGameRunning = false;
                handleGameEnd();
                return;
            }

            int activePlayersCount = 0;
            List<TienLenPlayer> allPlayers = gameLogic.getPlayers();
            TienLenPlayer potentialNewRoundStarter = null;

            for (TienLenPlayer p : allPlayers) {
                if (!p.getHand().isEmpty() && !playersSkippedThisRound.contains(p)) {
                    activePlayersCount++;
                    potentialNewRoundStarter = p;
                }
            }

            if (activePlayersCount <= 1) {
                System.out.println("Round ended. Starting new round.");
                playersSkippedThisRound.clear();
                gameLogic.clearCardsOnTable();
                TienLenPlayer nextPlayerForNewRound = lastPlayerWhoPlayedCards;
                if (lastPlayerWhoPlayedCards != null && lastPlayerWhoPlayedCards.getHand().isEmpty()) {
                    int lpwpcIndex = allPlayers.indexOf(lastPlayerWhoPlayedCards);
                    for (int i = 1; i <= allPlayers.size(); i++) {
                        TienLenPlayer p = allPlayers.get((lpwpcIndex + i) % allPlayers.size());
                        if (!p.getHand().isEmpty()) {
                            nextPlayerForNewRound = p;
                            break;
                        }
                    }
                } else if (lastPlayerWhoPlayedCards == null && potentialNewRoundStarter != null) {
                    nextPlayerForNewRound = potentialNewRoundStarter;
                }

                if (nextPlayerForNewRound == null || nextPlayerForNewRound.getHand().isEmpty()) {
                    handleGameEnd();
                    return;
                }

                gameLogic.setCurrentPlayer(nextPlayerForNewRound);
                lastPlayerWhoPlayedCards = nextPlayerForNewRound;

            } else {
                TienLenPlayer currentLogicalPlayer = gameLogic.getCurrentPlayer();
                int currentIndex = allPlayers.indexOf(currentLogicalPlayer);
                TienLenPlayer nextPlayerToPlay = null;

                for (int i = 1; i <= allPlayers.size(); i++) {
                    TienLenPlayer p = allPlayers.get((currentIndex + i) % allPlayers.size());
                    if (!p.getHand().isEmpty() && !playersSkippedThisRound.contains(p)) {
                        nextPlayerToPlay = p;
                        break;
                    }
                }
                if (nextPlayerToPlay == null) {
                    handleGameEnd();
                    return;
                }
                gameLogic.setCurrentPlayer(nextPlayerToPlay);
            }

            if (viewController != null) {
                viewController.updateView(gameLogic.getCurrentGameState());
                viewController.setMenuLabel(gameLogic.getCurrentPlayer().getName() + "'s turn.");
            }
            checkAndPlayBotTurnIfNeeded();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi trong quá trình xử lý lượt tiếp theo.", e);
        }
    }

    private void handlePlayerWin(TienLenPlayer winner) {
        try {
            System.out.println(winner.getName() + " has won this round (no cards left)!");
            isGameRunning = false;
            handleGameEnd(winner);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xử lý người chơi chiến thắng: " + winner.getName(), e);
        }
    }

    private void handleGameEnd() {
        try {
            System.out.println("Game Over!");
            isGameRunning = false;
            TienLenPlayer finalWinner = findWinner();
            if (viewController != null) {
                viewController.onGameEnded(gameLogic.getCurrentGameState(), finalWinner);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi kết thúc trò chơi.", e);
        }
    }

    private void handleGameEnd(TienLenPlayer winner) {
        try {
            System.out.println("Game Over! Winner: " + winner.getName());
            isGameRunning = false;
            if (viewController != null) {
                viewController.onGameEnded(gameLogic.getCurrentGameState(), winner);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi kết thúc trò chơi với người thắng cuộc.", e);
        }
    }

    private TienLenPlayer findWinner() {
        try {
            for (TienLenPlayer p : gameLogic.getPlayers()) {
                if (p.getHand().isEmpty()) {
                    return p;
                }
            }
            return (gameLogic.getPlayers() != null && !gameLogic.getPlayers().isEmpty())
                    ? gameLogic.getPlayers().getFirst()
                    : null;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tìm người thắng cuộc.", e);
        }
    }

    public void executeAfterDelay(Duration duration, Runnable action) {
        try {
            PauseTransition delay = new PauseTransition(duration);
            delay.setOnFinished(event -> {
                if (isGameRunning) {
                    try {
                        action.run();
                    } catch (Exception e) {
                        throw new RuntimeException("Lỗi trong hành động trì hoãn.", e);
                    }
                }
            });
            delay.play();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo trì hoãn hành động.", e);
        }
    }

    @Override
    protected void checkAndPlayBotTurnIfNeeded() {
        try {
            if (!isGameRunning()) {
                return;
            }
            TienLenPlayer currentPlayer = gameLogic.getCurrentPlayer();

            if (currentPlayer instanceof TienLenBotPlayer) {
                TienLenBotPlayer bot = (TienLenBotPlayer) currentPlayer;
                TienLenGameState gameState = gameLogic.getCurrentGameState();
                viewController.setMenuLabel(bot.getName() + "'s turn. Bot is thinking...");
                System.out.println(bot.getName() + "'s turn.");

                executeAfterDelay(Duration.seconds(1), () -> {
                    if (!isGameRunning())
                        return;

                    List<StandardCard> botMove = bot.decideCardsToPlay(gameState);

                    if (botMove != null && !botMove.isEmpty()) {
                        System.out.println(bot.getName() + " decides to play: " + botMove);
                        if (viewController != null)
                            viewController.setMenuLabel(bot.getName() + " played cards.");
                        processPlayerMove(bot, botMove);
                    } else {
                        System.out.println(bot.getName() + " decides to pass.");
                        if (viewController != null)
                            viewController.setMenuLabel(bot.getName() + " passed.");
                        processPlayerMove(bot, "PASS");
                    }
                });
            } else {
                if (viewController != null) {
                    viewController.promptPlayerForAction(currentPlayer, gameLogic.getCurrentGameState());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xử lý lượt bot hoặc người chơi.", e);
        }
    }

    public void handleDeal() {
        try {
            if (isGameRunning()) {
                System.out.println("Game is already running. Cannot deal again.");
                return;
            }
            isGameRunning = true;
            gameLogic.startGame();
            viewController.updateView(gameLogic.getCurrentGameState());
            checkAndPlayBotTurnIfNeeded();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi bắt đầu chia bài.", e);
        }
    }

    public void playerRequestsPlayCards(TienLenPlayer player, List<StandardCard> cards) {
        try {
            processPlayerMove(player, cards);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi người chơi yêu cầu đánh bài.", e);
        }
    }

    public void playerRequestsPass(TienLenPlayer player) {
        try {
            processPlayerMove(player, "PASS");
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi người chơi yêu cầu bỏ lượt.", e);
        }
    }

    public void markGameAsStopped() {
        this.isGameRunning = false;
        System.out.println("TienLenLogicController: Game logic marked as stopped by exiting.");
        // Không cần làm gì thêm ở đây nếu chấp nhận rủi ro PauseTransition
    }
}