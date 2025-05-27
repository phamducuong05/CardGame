package com.myteam.game.model.phom.gamestate;

import com.myteam.game.model.core.card.StandardCard;
import com.myteam.game.model.phom.player.PhomPlayer;

import java.util.List;


public class PhomGameState {
    private final List<PhomPlayer> players;
    private final PhomPlayer currentPlayer;
    private final List<List<StandardCard>> allPlayerMelds;
    private final boolean isGameOver;
    private final PhomPlayer winner;
    private final StandardCard cardOnTable;

    public PhomGameState(List<PhomPlayer> players, PhomPlayer currentPlayer, List<List<StandardCard>> allPlayerMelds, boolean isGameOver, PhomPlayer winner, StandardCard cardOnTable) {
        this.players = players;
        this.currentPlayer = currentPlayer;
        this.allPlayerMelds = allPlayerMelds;
        this.isGameOver = isGameOver;
        this.winner = winner;
        this.cardOnTable = cardOnTable;
    }


    public List<PhomPlayer> getPlayers() {
        return players;
    }


    public PhomPlayer getCurrentPlayer() {
        return currentPlayer;
    }


    public List<List<StandardCard>> getAllPlayerMelds() {
        return allPlayerMelds;
    }


    public boolean isGameOver() {
        return isGameOver;
    }


    public PhomPlayer getWinner() {
        return winner;
    }

    public StandardCard getCardOnTable() {
        return cardOnTable;
    }
}
