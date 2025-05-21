package com.myteam.game.model.core.card;

public abstract class Card {
    private String name;

    public Card() {
    }

    public Card(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public abstract String toString();
}