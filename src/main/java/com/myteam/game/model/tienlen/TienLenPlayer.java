package com.myteam.game.model.tienlen;

import com.myteam.game.model.core.card.WestCard;
import com.myteam.game.model.player.Player;

public abstract class TienLenPlayer extends Player<WestCard> {
    public enum State {WAITING, PLAYING}
    private int playerNum;
    private int playerRank;
    private State state;

    //Getters and Setters
    public TienLenPlayer(String name) {
        super(name);
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void skipTurn() {
        this.state = State.WAITING;
    }
}
