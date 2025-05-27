package com.myteam.game.model.tienlen.player;

import com.myteam.game.model.core.card.StandardCard;
import com.myteam.game.model.player.Player;
import com.myteam.game.model.tienlen.gamestate.TienLenGameState;

import java.util.List;

public abstract class TienLenPlayer extends Player<StandardCard> {
    private int playerNum;
    private int playerRank;

    public TienLenPlayer(String name) {
        super(name);
    }

    /**
     * Decide to skip based on the current game state
     * @return true if the player decide to skip, else return false
     */
//    public abstract boolean decideToSkip();

    /**
     * Decide which cards to play based on the current game state
     *
     * @param gameState The current game state
     * @return The cards to play, or null/empty to pass
     */
    public abstract List<StandardCard> decideCardsToPlay(TienLenGameState gameState);
}
