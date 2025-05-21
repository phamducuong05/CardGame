package controller;

import model.core.card.WestCard;
import model.game.TienLenMienBacGameLogic;
import model.tienlen.TienLenGameState;
import model.tienlen.TienLenPlayer;
import model.tienlen.TienLenBotPlayer;
import view.TienLenGameViewController;

import java.util.List;

/**
 * LogicController implementation for TienLen game
 * Handles game logic and coordinates between model and view
 */
public class TienLenLogicController extends LogicController<WestCard, TienLenPlayer, TienLenMienBacGameLogic> {

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
        if (!isGameRunning()) {
            return;
        }

        // In TienLen, the main move is playing cards
        if (move instanceof List) {
            @SuppressWarnings("unchecked")
            List<WestCard> cardsToPlay = (List<WestCard>) move;

            // Ensure player owns all cards
            if (!player.getHand().containsAll(cardsToPlay)) {
                if (viewController != null) {
                    viewController.showInvalidMoveMessage();
                    viewController.promptPlayerForAction(player, gameLogic.getCurrentGameState());
                }
                return;
            }

            // Validate the move using game logic
            if (gameLogic.isValidMove(cardsToPlay)) {
                // Update player's hand
                player.getHand().removeAll(cardsToPlay);

                // Update game state
                gameLogic.playCards(cardsToPlay);

                // Update view


                // Check if player has won (no cards left)
                if (player.getHand().isEmpty()) {
                    //endGame();
                } else {
                    // If invalid move, player can play again if they want
                    if (viewController != null) {
                        viewController.promptPlayerForAction(player, gameLogic.getCurrentGameState());
                    }
                }
            } else {
                // Invalid move, prompt player to try again
                if (viewController != null) {
                    viewController.showInvalidMoveMessage();
                    viewController.promptPlayerForAction(player, gameLogic.getCurrentGameState());
                }
            }
        } else if (move instanceof String && "PASS".equals(move)) {
            // Player passes turn
            nextTurn();
        }
    }

    @Override
    protected void nextTurn() {
        gameLogic.nextTurn();
        //view.updatehand
        checkAndPlayBotTurnIfNeeded();
    }

    @Override
    protected void checkAndPlayBotTurnIfNeeded() {
        TienLenPlayer currentPlayer = gameLogic.getCurrentPlayer();

        // If current player is a bot, play its turn automatically
        if (currentPlayer instanceof TienLenBotPlayer) {
            TienLenBotPlayer bot = (TienLenBotPlayer) currentPlayer;
            TienLenGameState gameState = gameLogic.getCurrentGameState();

            // Get bot decision
            List<WestCard> botMove = bot.decideCardsToPlay(gameState);

            if (botMove != null && !botMove.isEmpty()) {
                // Bot decides to play cards
                processPlayerMove(bot, botMove);
            } else {
                // Bot decides to pass
                processPlayerMove(bot, "PASS");
            }
        } else {
            // Human player's turn - prompt for action via the view controller
            if (viewController != null) {
                viewController.promptPlayerForAction(currentPlayer, gameLogic.getCurrentGameState());
            }
        }
    }





    public void handleDeal() {
        gameLogic.startGame();
        viewController.updatePlayerHands(gameLogic.getCurrentGameState());
        // viewCOntroller.promtpToDiscard;

    }

    /**
     * Find the winner of the game based on current game state
     *
     * @return The winning player
     */
    private TienLenPlayer findWinner() {
        TienLenGameState gameState = gameLogic.getCurrentGameState();

        // Check all players to find the one who has no cards left
        for (TienLenPlayer player : gameState.getPlayers()) {
            if (player.getHand().isEmpty()) {
                return player;
            }
        }

        // No winner yet
        return null;
    }

    // API methods for ViewController to call

    /**
     * Handle player request to play cards
     *
     * @param player The player playing the cards
     * @param cards The cards to play
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