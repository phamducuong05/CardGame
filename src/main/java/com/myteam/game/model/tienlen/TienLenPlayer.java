package com.myteam.game.model.tienlen;

import com.myteam.game.model.core.card.WestCard;
import com.myteam.game.model.player.Player;

import java.util.List;

public abstract class TienLenPlayer extends Player<WestCard> {
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
    public abstract List<WestCard> decideCardsToPlay(TienLenGameState gameState);
}
