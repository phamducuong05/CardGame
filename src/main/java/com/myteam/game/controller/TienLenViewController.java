package com.myteam.game.controller;

import com.myteam.game.model.core.card.WestCard;
import com.myteam.game.model.tienlen.TienLenGameState;
import com.myteam.game.model.tienlen.TienLenPlayer;

import java.util.List;

/**
 * Interface for TienLenViewController
 * This will be implemented by the JavaFX view controller for the TienLen game
 */
public interface TienLenViewController {
    
    /**
     * Set the logic controller
     * 
     * @param logicController The logic controller to use
     */
    void setLogicController(TienLenLogicController logicController);
    
    /**
     * Update the view with the current game state
     * 
     * @param gameState The current game state
     */
    void updateView(TienLenGameState gameState);
    
    /**
     * Called when the game starts
     * 
     * @param gameState The initial game state
     */
    void onGameStarted(TienLenGameState gameState);
    
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
    void onGameEnded(TienLenGameState gameState, TienLenPlayer winner);
    
    /**
     * Prompt the player to perform an action (play cards or pass)
     * 
     * @param player The player to prompt
     * @param gameState The current game state
     */
    void promptPlayerForAction(TienLenPlayer player, TienLenGameState gameState);
    
    /**
     * Show a message when the player tries to make an invalid move
     */
    void showInvalidMoveMessage();
} 