package com.myteam.game.model.phom.player;
import com.myteam.game.model.core.card.StandardCard;
import com.myteam.game.model.phom.gamestate.PhomGameState;

import java.util.List;
import java.util.Map;

public class PhomHumanPlayer extends PhomPlayer {

    public PhomHumanPlayer(String name) {
        super(name);
    }

    @Override
    public boolean decideToEat(StandardCard discardedCard) {
        throw new UnsupportedOperationException("Human decision handled by Controller via UI.");
    }

    @Override
    public StandardCard decideDiscard() {
        throw new UnsupportedOperationException("Human decision handled by Controller via UI.");
    }


}
