package com.myteam.game.model.core.enums;

public enum Suit {
    SPADES("♠"), CLUBS("♣"), DIAMONDS("♦"), HEARTS("♥");

    private final String value;

    Suit(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
