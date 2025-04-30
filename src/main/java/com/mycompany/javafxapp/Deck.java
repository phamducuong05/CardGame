package com.mycompany.javafxapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> cards = new ArrayList<>();

    public Deck() {
        resetAndShuffle();
    }

    public void resetAndShuffle() {
        cards.clear();
        String[] suits = { "hearts", "diamonds", "clubs", "spades" }; // Heart, Diamond, Club, Spade
        String[] ranks = { "ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "jack", "queen", "king" };
        for (String suit : suits) {
            for (String rank : ranks) {
                cards.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(cards);
        System.out.println("Deck reset and shuffled. Cards: " + cards.size());
    }

    public Card dealCard() {
        if (cards.isEmpty())
            return null;
        return cards.remove(0);
    }
}