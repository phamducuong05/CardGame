package com.myteam.game.controller;

import com.myteam.game.model.core.card.WestCard;
import com.myteam.game.model.core.deck.Deck;
import com.myteam.game.model.core.enums.Rank;
import com.myteam.game.model.core.enums.Suit;
import com.myteam.game.model.game.PhomGameLogic;
import com.myteam.game.model.game.TienLenMienBacGameLogic;
//import com.myteam.game.model.game.TienLenMienBacGameLogic;
import com.myteam.game.model.phom.PhomPlayer;
import com.myteam.game.model.player.Player;
import com.myteam.game.model.tienlen.TienLenPlayer;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Orchestrator class for managing and switching between different games
 */
public class MainGameController {
    
    private PhomLogicController phomLogicController;
    private TienLenLogicController tienLenLogicController;
    private String currentGameType;
    
    // Constructor
    public MainGameController() {
        this.currentGameType = null;
    }
    
    /**
     * Select and initialize a game type
     * 
     * @param gameType The type of game to select ("Phom" or "TienLen")
     * @param players The list of players
     * @param numCards The number of cards to deal to each player
     * @return The initialized logic controller for the selected game
     */
    public Object selectGame(String gameType, Object players, int numCards) {
        if ("Phom".equalsIgnoreCase(gameType)) {
            return initializePhomGame((List<PhomPlayer>) players, numCards);
        } else if ("TienLen".equalsIgnoreCase(gameType)) {
            return initializeTienLenGame((List<TienLenPlayer>) players, numCards);
        } else {
            throw new IllegalArgumentException("Unsupported game type: " + gameType);
        }
    }
    
    /**
     * Initialize a Phom game
     * 
     * @param players The list of Phom players
     * @param numCards The number of cards to deal to each player
     * @return The Phom logic controller
     */
    private PhomLogicController initializePhomGame(List<PhomPlayer> players, int numCards) {

        // Create deck
        StandardDeck<PhomPlayer> deck = new StandardDeck<>();
        
        // Create game logic
        PhomGameLogic gameLogic = new PhomGameLogic(deck, players, numCards);
        
        // Create logic controller
        phomLogicController = new PhomLogicController(gameLogic);
        
        // Set current game type
        currentGameType = "Phom";
        
        return phomLogicController;
    }
    
    /**
     * Initialize a TienLen game
     * 
     * @param players The list of TienLen players
     * @param numCards The number of cards to deal to each player
     * @return The TienLen logic controller
     */
    private TienLenLogicController initializeTienLenGame(List<TienLenPlayer> players, int numCards) {
        // Create deck
        StandardDeck<TienLenPlayer> deck = new StandardDeck<>();
        
        // Create game logic
        TienLenMienBacGameLogic gameLogic = new TienLenMienBacGameLogic(deck, players, numCards);
        
        // Create logic controller
        tienLenLogicController = new TienLenLogicController(gameLogic);
        
        // Set current game type
        currentGameType = "TienLen";
        
        return tienLenLogicController;
    }
    
    /**
     * Standard deck implementation for card games
     */
    private static class StandardDeck<P extends Player<WestCard>> extends Deck<WestCard, P> {
        @Override
        protected void initializeDeck() {
            Stack<WestCard> deck = getDeck();
            deck.clear();
            
            // Create a standard 52-card deck
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    deck.push(new WestCard(suit, rank));
                }
            }
            
            // Shuffle the deck
            shuffle();
        }
    }
    
    /**
     * Get the current game type
     * 
     * @return The current game type
     */
    public String getCurrentGameType() {
        return currentGameType;
    }
    
    /**
     * Get the current logic controller
     * 
     * @return The current logic controller (either PhomLogicController or TienLenLogicController)
     */
    public Object getCurrentLogicController() {
        if ("Phom".equals(currentGameType)) {
            return phomLogicController;
        } else if ("TienLen".equals(currentGameType)) {
            return tienLenLogicController;
        } else {
            return null;
        }
    }
    
    /**
     * Reset the current game
     */
    public void resetCurrentGame() {
        if ("Phom".equals(currentGameType) && phomLogicController != null) {
            PhomGameLogic gameLogic = phomLogicController.getGameLogic();
            initializePhomGame(gameLogic.getPlayers(), gameLogic.getNumberOfCards());
            phomLogicController.startGame();
        } else if ("TienLen".equals(currentGameType) && tienLenLogicController != null) {
            TienLenMienBacGameLogic gameLogic = tienLenLogicController.getGameLogic();
            initializeTienLenGame(gameLogic.getPlayers(), gameLogic.getNumberOfCards());
            tienLenLogicController.startGame();
        }
    }
} 