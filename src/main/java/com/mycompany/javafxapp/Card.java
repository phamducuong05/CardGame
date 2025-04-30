package com.mycompany.javafxapp;

public class Card {
    private final String suit;
    private final String rank;

    public Card(String suit, String rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public String getImage() {
        return rank + "_of_" + suit + ".png";
    }

    @Override
    public String toString() {
        // Ví dụ: "H A", "S K", "D 10"
        return suit.substring(0, 1) + rank;
    }
}
