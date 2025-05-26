package com.myteam.game.controller;

import com.myteam.game.model.core.card.WestCard;
import com.myteam.game.model.game.TienLenMienBacGameLogic;
import com.myteam.game.model.tienlen.TienLenGameState;
import com.myteam.game.model.tienlen.TienLenPlayer;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

import com.myteam.game.model.tienlen.TienLenBotPlayer;
import com.myteam.game.TienLenGameViewController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * LogicController implementation for TienLen game
 * Handles game logic and coordinates between model and view
 */
public class TienLenLogicController extends LogicController<WestCard, TienLenPlayer, TienLenMienBacGameLogic> {
    private Set<TienLenPlayer> playersSkippedThisRound = new HashSet<>();
    private TienLenPlayer lastPlayerWhoPlayedCards = null; // Người cuối cùng đánh bài
    // Reference to the view controller (without JavaFX dependencies)
    private TienLenGameViewController viewController;

    public TienLenLogicController(TienLenMienBacGameLogic gameLogic) {
        super(gameLogic);
    }

    /**
     * Set the view controller
     *
     * @param viewController The view controller to use
     */
    public void setViewController(TienLenGameViewController viewController) {
        this.viewController = viewController;
    }

    @Override
    protected void processPlayerMove(TienLenPlayer player, Object move) {

        // In TienLen, the main move is playing cards
        if (move instanceof List) {
            @SuppressWarnings("unchecked")
            List<WestCard> cardsToPlay = (List<WestCard>) move;

            // Ensure player owns all cards
            if (!player.getHand().containsAll(cardsToPlay)) {
                if (viewController != null) {
                    viewController.showInvalidMoveMessage();
                    System.out.println(
                            "Player " + player.getName() + " tried to play cards not in their hand: " + cardsToPlay);
                    viewController.promptPlayerForAction(player, gameLogic.getCurrentGameState());
                }
                return;
            }

            // Validate the move using game logic
            if (gameLogic.isValidMove(cardsToPlay)) {
                player.getHand().removeAll(cardsToPlay);
                gameLogic.playCards(cardsToPlay);

                lastPlayerWhoPlayedCards = player; // Ghi nhớ người vừa đánh
                playersSkippedThisRound.clear(); // Reset những người đã bỏ lượt VÌ CÓ NGƯỜI ĐÁNH MỚI

                if (viewController != null)
                    viewController.updateView(gameLogic.getCurrentGameState());

                if (player.getHand().isEmpty()) {
                    handlePlayerWin(player); // Xử lý thắng
                } else {
                    gameLogic.setIsFirstturn(false);
                    nextTurn();
                }
            } else {
                // Invalid move, prompt player to try again
                if (viewController != null) {
                    viewController.showInvalidMoveMessage();
                    viewController.promptPlayerForAction(player, gameLogic.getCurrentGameState());
                }
            }
        } else if (move instanceof String && "PASS".equals(move)) {
            TienLenGameState gameState = gameLogic.getCurrentGameState();
            // Không cho phép bỏ lượt nếu bàn trống VÀ người chơi này là người được quyền
            // bắt đầu vòng mới
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
    }

    @Override
    protected void nextTurn() {
        if (gameLogic.getCurrentGameState().isGameOver()) {
            System.out.println("Game is already over. Cannot proceed to next turn.");
            if (viewController != null) {
                viewController.onGameEnded(gameLogic.getCurrentGameState(), findWinner());
            }
            isGameRunning = false; // Đặt trạng thái game là không chạy
            handleGameEnd();
            return;
        }

        // Đếm số người chơi còn hoạt động (chưa hết bài và chưa skip vòng này)
        int activePlayersCount = 0;
        List<TienLenPlayer> allPlayers = gameLogic.getPlayers();
        TienLenPlayer potentialNewRoundStarter = null;

        for (TienLenPlayer p : allPlayers) {
            if (!p.getHand().isEmpty() && !playersSkippedThisRound.contains(p)) {
                activePlayersCount++;
                potentialNewRoundStarter = p; // Nếu chỉ còn 1 người, đó là người này
            }
        }

        // Nếu chỉ còn 1 người chơi active (hoặc 0), vòng hiện tại kết thúc
        if (activePlayersCount <= 1) {
            System.out.println("Round ended. Starting new round.");
            playersSkippedThisRound.clear();
            gameLogic.clearCardsOnTable(); // GameLogic cần hàm này

            // Người bắt đầu vòng mới là người cuối cùng đã đánh (lastPlayerWhoPlayedCards)
            // Hoặc nếu người đó đã hết bài, thì là potentialNewRoundStarter (nếu còn 1
            // người)
            TienLenPlayer nextPlayerForNewRound = lastPlayerWhoPlayedCards;
            if (lastPlayerWhoPlayedCards != null && lastPlayerWhoPlayedCards.getHand().isEmpty()) {
                // Tìm người chơi tiếp theo sau lastPlayerWhoPlayedCards mà còn bài
                int lpwpcIndex = allPlayers.indexOf(lastPlayerWhoPlayedCards);
                for (int i = 1; i <= allPlayers.size(); i++) {
                    TienLenPlayer p = allPlayers.get((lpwpcIndex + i) % allPlayers.size());
                    if (!p.getHand().isEmpty()) {
                        nextPlayerForNewRound = p;
                        break;
                    }
                }
            } else if (lastPlayerWhoPlayedCards == null && potentialNewRoundStarter != null) {
                // Trường hợp không có ai đánh ở vòng trước (vô lý nếu game đã bắt đầu)
                // hoặc trường hợp người chơi cuối cùng đã hết bài và potentialNewRoundStarter
                // là người duy nhất còn lại
                nextPlayerForNewRound = potentialNewRoundStarter;
            }

            if (nextPlayerForNewRound == null || nextPlayerForNewRound.getHand().isEmpty()) {
                // Không tìm thấy ai để bắt đầu vòng mới (tất cả đã hết bài trừ 1 người?) ->
                // Game over
                handleGameEnd();
                return;
            }

            gameLogic.setCurrentPlayer(nextPlayerForNewRound);
            lastPlayerWhoPlayedCards = nextPlayerForNewRound; // Người này sẽ là "người vừa đánh" cho vòng mới

        } else { // Vẫn còn nhiều hơn 1 người chơi active trong vòng
            // Chuyển sang người chơi tiếp theo theo thứ tự bình thường
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
            if (nextPlayerToPlay == null) { // Không tìm thấy ai, có thể là lỗi hoặc game nên kết thúc
                handleGameEnd();
                return;
            }
            gameLogic.setCurrentPlayer(nextPlayerToPlay);
        }

        // Cập nhật UI và cho bot chơi
        if (viewController != null) {
            viewController.updateView(gameLogic.getCurrentGameState());
            viewController.setMenuLabel(gameLogic.getCurrentPlayer().getName() + "'s turn.");
        }
        checkAndPlayBotTurnIfNeeded();
    }

    private void handlePlayerWin(TienLenPlayer winner) {
        System.out.println(winner.getName() + " has won this round (no cards left)!");
        isGameRunning = false;
        handleGameEnd(winner); // Kết thúc game nếu người chơi hết bài

    }

    private void handleGameEnd() {
        System.out.println("Game Over!");
        isGameRunning = false;
        TienLenPlayer finalWinner = findWinner(); // Logic tìm người thắng chung cuộc
        if (viewController != null) {
            viewController.onGameEnded(gameLogic.getCurrentGameState(), finalWinner);
        }
    }

    private void handleGameEnd(TienLenPlayer winner) {
        System.out.println("Game Over! Winner: " + winner.getName());
        isGameRunning = false;
        if (viewController != null) {
            viewController.onGameEnded(gameLogic.getCurrentGameState(), winner);
        }
    }
    // findWinner() có thể đơn giản là người đầu tiên hết bài,
    // hoặc nếu bạn có ranking, người đứng đầu ranking.
    private TienLenPlayer findWinner() {
        for (TienLenPlayer p : gameLogic.getPlayers()) {
            if (p.getHand().isEmpty()) {
                return p; // Người đầu tiên hết bài là người thắng
            }
        }
        // Fallback nếu không tìm thấy ai hết bài (lỗi logic đâu đó)
        return (gameLogic.getPlayers() != null && !gameLogic.getPlayers().isEmpty()) ? gameLogic.getPlayers().getFirst()
                : null;
    }

    public void executeAfterDelay(Duration duration, Runnable action) {
        PauseTransition delay = new PauseTransition(duration);
        delay.setOnFinished(event -> {
            if (isGameRunning) { // Chỉ thực thi nếu game vẫn đang chạy
                action.run();
            }
        });
        delay.play();
    }

    @Override
    protected void checkAndPlayBotTurnIfNeeded() {
        if (!isGameRunning()) { // THÊM DÒNG NÀY
            return;
        }
        TienLenPlayer currentPlayer = gameLogic.getCurrentPlayer();

        // If current player is a bot, play its turn automatically
        if (currentPlayer instanceof TienLenBotPlayer) {
            TienLenBotPlayer bot = (TienLenBotPlayer) currentPlayer;
            TienLenGameState gameState = gameLogic.getCurrentGameState();
            viewController.displayOpponentCards(bot.getHand()); // Giả sử có phương thức này để hiển thị bài bot
            viewController.setMenuLabel(bot.getName() + "'s turn. Bot is thinking..."); // Giả sử có setMenuLabel
            System.out.println(bot.getName() + "'s turn.");

            executeAfterDelay(Duration.seconds(1), () -> { // Giảm delay để test nhanh hơn nếu cần
                if (!isGameRunning())
                    return; // Kiểm tra lại trước khi bot hành động (quan trọng)

                List<WestCard> botMove = bot.decideCardsToPlay(gameState);

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
            // Human player's turn - prompt for action via the view controller
            if (viewController != null) {
                viewController.promptPlayerForAction(currentPlayer, gameLogic.getCurrentGameState());
            }
        }
    }

    public void handleDeal() {
        if (isGameRunning()) {
            System.out.println("Game is already running. Cannot deal again.");
            return;
        }
        isGameRunning = true; // Set game running state
        gameLogic.startGame();
        viewController.updateView(gameLogic.getCurrentGameState());
        // viewCOntroller.promtpToDiscard;
        checkAndPlayBotTurnIfNeeded();

    }

    // API methods for ViewController to call

    /**
     * Handle player request to play cards
     *
     * @param player The player playing the cards
     * @param cards  The cards to play
     */
    public void playerRequestsPlayCards(TienLenPlayer player, List<WestCard> cards) {
        processPlayerMove(player, cards);
    }

    /**
     * Handle player request to pass turn
     *
     * @param player The player passing
     */
    public void playerRequestsPass(TienLenPlayer player) {
        processPlayerMove(player, "PASS");
    }
}