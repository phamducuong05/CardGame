package com.myteam.game.viewcontroller;

import com.myteam.game.model.phom.gamestate.PhomGameState;
import com.myteam.game.model.phom.player.PhomPlayer;
import com.myteam.game.model.core.card.StandardCard;

import java.util.List;

public interface PhomViewInterface {
    // void updatePlayerUIVisibility(int numActivePlayers);

    void setGameActionButtonsVisible(boolean visible);

    void updateAllOpponentCardCountsVisibility(boolean visible);

    void updateView(PhomGameState gameState);

    void updateMeld(PhomGameState gameState);

    void updateAllPlayerHandsDisplay(List<PhomPlayer> players);

    void updateAllPlayerEatenCardsDisplay(List<PhomPlayer> players);

    void updateAllPlayerLastDiscardDisplay(List<PhomPlayer> players, StandardCard cardOnTableGlobal);

    void updateCenterDeckDisplay(int deckSize);

    void clearAllPlayerAreasForNewGame();

    void displayBotAction(String text);

    void clearSelectedCardsUI();

    void showInvalidMoveMessage(String message);

    void setMainPlayerIndex(int mainPlayerIndex);

    void showGameOver(PhomPlayer winnerPlayer);

    // Prompt methods
    void promptPlayerToDiscard(PhomPlayer player, PhomGameState gameState);

    void promptPlayerToEat(PhomPlayer player, PhomGameState gameState);

    void promptPlayerToDraw(PhomPlayer player, PhomGameState gameState);
}
