package com.myteam.game.model.game;

import com.myteam.game.model.core.card.Card;
import com.myteam.game.model.core.deck.Deck;
import com.myteam.game.model.player.Player;

import java.util.List;

public abstract class Game<T extends Card, P extends Player<T>> {
    protected Deck<T, P> deck;
    protected List<P> players;
    protected P currentPlayer;
    protected boolean gameState; // false = started, true = ended
    protected int numberOfCards;

    protected Game() {
    }

    protected Game(Deck<T, P> deck, List<P> players, int numberOfCards) {
        this.deck = deck;
        this.players = players;
        this.numberOfCards = numberOfCards;
        this.gameState = false;
    }


    public void startGame() {
        if (deck == null) {
            System.err.println("Lỗi: Bộ bài (deck) chưa được khởi tạo!");
            return;
        }
        deck.dealCards(players, numberOfCards);
        currentPlayer = getFirstPlayer(players);
        if (players == null || players.isEmpty()) {
            System.err.println("Lỗi: Không có người chơi nào!");
            return;
        }
        if (numberOfCards <= 0) {
            System.err.println("Lỗi: Số lá bài chia không hợp lệ!");
            return;
        }
        System.out.println("Game started! Dealing... " + numberOfCards + " cards to " + players.size() + " players.");

    }



    public void setCurrentPlayer(P currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public P getCurrentPlayer() {
        return currentPlayer;
    }



    public abstract P getFirstPlayer(List<P> players);

    public abstract boolean isValidMove(List<T> cards);

    public abstract boolean endGame();

    public abstract void nextTurn();

    public int getNumberOfCards() {
        return this.numberOfCards;
    }

    public Deck<T, P> getDeck() {
        return this.deck;
    }

    public List<P> getPlayers() {
        return this.players;
    }
}