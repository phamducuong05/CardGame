package com.myteam.game.view;

import com.myteam.game.controller.PhomLogicController;
import com.myteam.game.controller.PhomViewController;
import com.myteam.game.model.core.card.WestCard;
import com.myteam.game.model.phom.PhomGameState;
import com.myteam.game.model.phom.PhomPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplified implementation of PhomViewController
 * Actual implementation would use JavaFX UI components
 * This is a stub implementation without JavaFX dependencies
 */
public class PhomGameViewController implements PhomViewController {


    // First, we need to initialize components of javafx such as stage, scene, root and Hbox of player.
    // We need to write the handle button of 6 buttons including Deal button, play button, eat button, draw, send.

    private PhomLogicController logicController;
    private List<WestCard> selectedCards = new ArrayList<>();

    /**
     * Constructor
     */
    public PhomGameViewController() {
        System.out.println("PhomGameViewController created - JavaFX would be initialized here");
    }

    @Override
    public void setLogicController(PhomLogicController logicController) {
        this.logicController = logicController;
        System.out.println("LogicController set");
    }

    @Override
    public void updateView(PhomGameState gameState) {
        System.out.println("Updating view with game state");
        // In a real implementation, would update UI elements
    }

    @Override
    public void onGameStarted(PhomGameState gameState) {
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
    public void onGameEnded(PhomGameState gameState, PhomPlayer winner) {
        System.out.println("Game ended. Winner: " + winner.getName());
        updateView(gameState);
    }

    @Override
    public void promptPlayerForAction(PhomPlayer player, PhomGameState gameState) {
        System.out.println("Prompting player " + player.getName() + " for action");
        // In a real implementation, would enable appropriate UI controls
    }

    @Override
    public void promptPlayerToDiscard(PhomPlayer player, PhomGameState gameState) {
        System.out.println("Prompting player " + player.getName() + " to discard a card");
        // In a real implementation, would enable discard button and card selection
    }

    public void updatePlayerHands(PhomGameState gameState) {
        // Hàm này sẽ hiển thị các bài ra. Rồi sử dụng để xem ai là currentPlayer thì
        // hiển thị bài đó ngửa lên còn các người khác thì hiển thị bài úp xuống
        return ;
    }

    public void handleCardClick(WestCard card) {
        if (selectedCards.contains(card)) {
            selectedCards.remove(card);
            System.out.println("Deselected card: " + card);
        } else {
            selectedCards.add(card);
            System.out.println("Selected card: " + card);
        }
        // Bên cạnh việc này thì ta cũng sẽ cho bài di chuyển lên xuống khi ấn click nữa.
        // Phần code bên trên chỉ là minh họa còn cách implement
        // chi tiết giống hàm handleCardClick() của lom dom

    }


    // Handle deal button click
    public void handleDealButtonClick() {
        logicController.handleDeal();
    }

    /**
     * Handle draw button click
     */
    public void handleDrawButtonClick() {
        if (logicController != null) {
            PhomPlayer currentPlayer = logicController.getCurrentPlayer();
            logicController.playerRequestsDraw(currentPlayer);
            System.out.println("Player " + currentPlayer.getName() + " draws a card");
        }
    }


    public void handleEatButtonClick() {
        if (logicController != null) {
            PhomPlayer currentPlayer = logicController.getCurrentPlayer();
            PhomGameState gameState = logicController.getGameLogic().getCurrentGameState();
            WestCard topCard = gameState.getCardOnTable();
            if (topCard != null) {
                logicController.playerRequestsEat(currentPlayer, topCard);
            } else {
                System.out.println("No card to eat");
            }
        }
    }

    public void handleDiscardButtonClick() {
        if (logicController != null && !selectedCards.isEmpty()) {
            PhomPlayer currentPlayer = logicController.getCurrentPlayer();
            logicController.playerRequestsDiscard(currentPlayer, selectedCards);
            selectedCards.clear();
        } else {
            System.out.println("No card selected to discard");
        }
    }

    public void handleSendCardClick() {

    }


    public List<WestCard> getSelectedCards() {
        return selectedCards;
    }


} 