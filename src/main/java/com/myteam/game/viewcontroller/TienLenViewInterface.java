package com.myteam.game.viewcontroller;

import com.myteam.game.model.core.card.StandardCard;
import com.myteam.game.model.tienlen.gamestate.TienLenGameState;
import com.myteam.game.model.tienlen.player.TienLenPlayer;

import java.util.List;

public interface TienLenViewInterface {
    void setGameActionButtonsVisible(boolean visible);

    void updateAllOpponentCardCountsVisibility(boolean visible);

    void updateView(TienLenGameState gameState);

    void updateAllPlayerHandsDisplay(List<TienLenPlayer> players);

    void promptPlayerForAction(TienLenPlayer player, TienLenGameState gameState);

    List<StandardCard> getSelectedWestCardsFromUI();

    void updateCenterArea(List<StandardCard> cardsOnTable);

    void setMenuLabel(String text);

    void clearAllPlayerAreasForNewGame();

    void displayBotAction(String text);

    void clearSelectedCardsUI();

    void showInvalidMoveMessage();

    void setMainPlayerIndex(int mainPlayerIndex);

    void showUIMessage(String message);

    void onGameEnded(TienLenGameState gameState, TienLenPlayer winner);




}
