package com.myteam.game.model.tienlen.gamestate;

import com.myteam.game.model.core.card.StandardCard;
import com.myteam.game.model.tienlen.player.TienLenPlayer;

import java.util.List;

/**
 * Class representing the state of a TienLen game
 */
public class TienLenGameState {
    private final List<TienLenPlayer> players;
    private final TienLenPlayer currentPlayer;
    private final List<StandardCard> cardsOnTable;
    private final boolean isGameOver;
    private final List<TienLenPlayer> playerRankings;

    /**
     * Constructor for TienLenGameState
     *
     * @param players The list of players
     * @param currentPlayer The current player
     * @param cardsOnTable The cards on the table
     * @param isGameOver Whether the game is over
     * @param playerRankings The player rankings
     *
     */
    public TienLenGameState(List<TienLenPlayer> players, TienLenPlayer currentPlayer,
                            List<StandardCard> cardsOnTable, boolean isGameOver, List<TienLenPlayer> playerRankings) {
        this.players = players;
        this.currentPlayer = currentPlayer;
        this.cardsOnTable = cardsOnTable;
        this.isGameOver = isGameOver;
        this.playerRankings = playerRankings;
    }

    /**
     * Get the list of players
     *
     * @return The list of players
     */
    public List<TienLenPlayer> getPlayers() {
        return players;
    }

    /**
     * Get the current player
     *
     * @return The current player
     */
    public TienLenPlayer getCurrentPlayer() {
        return currentPlayer;
    }
    /**
     * Check if the game is over
     *
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return isGameOver;
    }
    /**
     * Get the list of cards on the table
     *
     * @return The list of cards on the table
     */
    public List<StandardCard> getCardsOnTable() {
        return cardsOnTable;
    }
}