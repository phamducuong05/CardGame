package com.myteam.game.model.player;

import com.myteam.game.model.core.card.Card;

import java.util.ArrayList;
import java.util.List;

public abstract class Player<T extends Card> {
    private List<T> hand;
    private List<T> selectedCards;
    private String name;

    public Player() {
        this.hand = new ArrayList<>(); // Khởi tạo hand
        this.selectedCards = new ArrayList<>(); // Khởi tạo selectedCards
        this.name = "Anonymous Player";
    }


    public Player(String name) {
        this.hand = new ArrayList<>();
        this.selectedCards = new ArrayList<>();
        this.name = name;
    }

    // Getters and setters
    public List<T> getHand() {
        return hand;
    }

    public void setHand(List<T> hand) {
        this.hand = hand;
    }

    public List<T> getSelectedCards() {
        return selectedCards;
    }

    public void setSelectedCards(List<T> selectedCards) {
        this.selectedCards = selectedCards;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //Other methods
    public int handSize() {
        return hand.size();
    }

    public void receiveCard(T card) {
        hand.add(card);
    }

    public void selectCard(T card) {
        selectedCards.add(card);
    }

    public List<T> playCard() {
        hand.removeAll(selectedCards);
        return selectedCards;
    }
}