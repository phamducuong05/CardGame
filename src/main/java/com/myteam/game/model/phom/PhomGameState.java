package com.myteam.game.model.phom;

import com.myteam.game.model.core.card.WestCard;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class PhomGameState {
    private final List<PhomPlayer> players;
    private final PhomPlayer currentPlayer;
    private final List<List<WestCard>> allPlayerMelds;
    private final boolean isGameOver;
    private final PhomPlayer winner;
    private final WestCard cardOnTable;

    public PhomGameState(List<PhomPlayer> players, PhomPlayer currentPlayer, List<List<WestCard>> allPlayerMelds, boolean isGameOver, PhomPlayer winner, WestCard cardOnTable) {
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


    public List<List<WestCard>> getAllPlayerMelds() {
        return allPlayerMelds;
    }
    

    public boolean isGameOver() {
        return isGameOver;
    }
    

    public PhomPlayer getWinner() {
        return winner;
    }

    public WestCard getCardOnTable() {
        return cardOnTable;
    }
}
