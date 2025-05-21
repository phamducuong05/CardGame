package com.myteam.game.view;

import com.myteam.game.controller.TienLenLogicController;
import com.myteam.game.controller.TienLenViewController;
import com.myteam.game.model.core.card.WestCard;
import com.myteam.game.model.tienlen.TienLenGameState;
import com.myteam.game.model.tienlen.TienLenPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of TienLenViewController for the TienLen game
 * This class handles the view logic and user interactions for the TienLen game
 */
public class TienLenGameViewController implements TienLenViewController {

    private TienLenLogicController logicController;
    private List<WestCard> selectedCards = new ArrayList<>();

    /**
     * Constructor
     */
    public TienLenGameViewController() {
        System.out.println("TienLenGameViewController created - JavaFX would be initialized here");
    }

    @Override
    public void setLogicController(TienLenLogicController logicController) {
        this.logicController = logicController;
        System.out.println("LogicController set");
    }

    @Override
    public void updateView(TienLenGameState gameState) {
        System.out.println("Updating view with game state");
        // In a real implementation, would update UI elements
        updatePlayerHands(gameState);
    }

    @Override
    public void onGameStarted(TienLenGameState gameState) {
        System.out.println("Game started");
        updateView(gameState);
    }

    @Override
    public void onGamePaused() {
        System.out.println("Game paused");
    }

    @Override
    public void onGameResumed() {
        System.out.println("Game resumed");
        if (logicController != null) {
            updateView(logicController.getGameLogic().getCurrentGameState());
        }
    }

    @Override
    public void onGameEnded(TienLenGameState gameState, TienLenPlayer winner) {
        System.out.println("Game ended. Winner: " + winner.getName());
        updateView(gameState);
    }

    @Override
    public void promptPlayerForAction(TienLenPlayer player, TienLenGameState gameState) {
        System.out.println("Prompting player " + player.getName() + " for action");
        // In a real implementation, would enable appropriate UI controls
        // For TienLen, this would enable card selection and play/pass buttons
    }

    @Override
    public void showInvalidMoveMessage() {
        System.out.println("Invalid move! Please try again.");
        // In a real implementation, would show an error message in the UI
    }

    /**
     * Update the display of player hands
     *
     * @param gameState The current game state
     */
    public void updatePlayerHands(TienLenGameState gameState) {
        // In a real implementation, would update the UI to show:
        // - Current player's hand face up
        // - Other players' hands face down
        // - Number of cards each player has
        // - Last played cards
        System.out.println("Updating player hands display");
    }

    /**
     * Handle card click event
     *
     * @param card The card that was clicked
     */
    public void handleCardClick(WestCard card) {
        if (selectedCards.contains(card)) {
            selectedCards.remove(card);
            System.out.println("Deselected card: " + card);
        } else {
            selectedCards.add(card);
            System.out.println("Selected card: " + card);
        }
        // In a real implementation, would also update card visual state
    }

    /**
     * Handle play button click
     */
    public void handlePlayButtonClick() {
        if (logicController != null && !selectedCards.isEmpty()) {
            TienLenGameState gameState = logicController.getGameLogic().getCurrentGameState();
            logicController.playerRequestsPlayCards(gameState.getCurrentPlayer(), new ArrayList<>(selectedCards));
            selectedCards.clear();
        }
    }

    /**
     * Handle pass button click
     */
    public void handlePassButtonClick() {
        if (logicController != null) {
            TienLenGameState gameState = logicController.getGameLogic().getCurrentGameState();
            logicController.playerRequestsPass(gameState.getCurrentPlayer());
        }
    }

    /**
     * Handle deal button click
     */
    public void handleDealButtonClick() {
        if (logicController != null) {
            logicController.handleDeal();
        }
    }

}