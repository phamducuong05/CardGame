package com.myteam.game.model.core.deck;

import com.myteam.game.model.core.card.Card;
import com.myteam.game.model.player.Player;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

public abstract class Deck<T extends Card, P extends Player<T>> {
    private final Stack<T> deck;

    public Deck() {
        deck = new Stack<>();
        initializeDeck();
    }

    protected abstract void initializeDeck();

    public Stack<T> getDeck() {
        return deck;
    }

    public boolean isEmpty() {
        return deck.isEmpty();
    }

    public void shuffle() {
        Collections.shuffle(deck);
    }

    public T drawCard() {
        if (deck.isEmpty()) {
            return null;
        }
        return deck.pop();
    }

    public void dealCards(List<P> players, int handSize) {
        // ...
        for (int i = 0; i < players.size() * handSize; i++) {
            T card = deck.pop(); // Hoặc drawCard()
            if (card != null) { // <<--- KIỂM TRA TRƯỚC KHI THÊM
                players.get(i % players.size()).receiveCard(card); // Đảm bảo i % players.size()
            } else {
                System.err.println("Deck ran out of cards while dealing.");
                break; // Hoặc xử lý khác
            }
        }
    }

    public int size() {
        return deck.size();
    }
}
