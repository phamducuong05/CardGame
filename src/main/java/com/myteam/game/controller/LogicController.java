package com.myteam.game.controller;

import com.myteam.game.model.core.card.Card;
import com.myteam.game.model.game.Game;
import com.myteam.game.model.player.Player;

import java.util.List;


public abstract class LogicController<T extends Card, P extends Player<T>, G extends Game<T, P>> {
    protected G gameLogic;
    protected boolean isGameRunning;

    public LogicController(G gameLogic) {
        this.gameLogic = gameLogic;
        this.isGameRunning = false;
    }


    protected abstract void processPlayerMove(P player, Object move);


    protected abstract void nextTurn();


    protected abstract void checkAndPlayBotTurnIfNeeded();


    public P getCurrentPlayer() {
        return gameLogic.getCurrentPlayer();
    }


    public boolean isGameRunning() {
        return isGameRunning;
    }


    public G getGameLogic() {
        return gameLogic;
    }
} 