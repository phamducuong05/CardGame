package com.myteam.game.controller;

import com.myteam.game.model.core.card.WestCard;
import com.myteam.game.model.phom.PhomGameState;
import com.myteam.game.model.phom.PhomPlayer;

/**
 * Interface for PhomViewController
 * This will be implemented by the JavaFX view controller for the Phom game
 */
public interface PhomViewController {

    /**
     * Set the logic controller
     *
     * @param logicController The logic controller to use
     */
    void setLogicController(PhomLogicController logicController);

    /**
     * Update the view with the current game state
     *
     * @param gameState The current game state
     */
    void updateView(PhomGameState gameState);

    /**
     * Called when the game starts
     *
     * @param gameState The initial game state
     */
    void onGameStarted(PhomGameState gameState);

    /**
     * Called when the game is paused
     */
    void onGamePaused();

    /**
     * Called when the game is resumed
     */
    void onGameResumed();

    /**
     * Called when the game ends
     *
     * @param gameState The final game state
     * @param winner The winning player
     */
    void onGameEnded(PhomGameState gameState, PhomPlayer winner);

    /**
     * Prompt the player to perform an action
     *
     * @param player The player to prompt
     * @param gameState The current game state
     */
    void promptPlayerForAction(PhomPlayer player, PhomGameState gameState);

    /**
     * Prompt the player to discard a card after drawing or eating
     *
     * @param player The player to prompt
     * @param gameState The current game state
     */
    void promptPlayerToDiscard(PhomPlayer player, PhomGameState gameState);
} 