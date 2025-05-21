package com.myteam.game.model.phom;
import com.myteam.game.model.core.card.WestCard;

import java.util.List;
import java.util.Map;

public class PhomHumanPlayer extends PhomPlayer{

    public PhomHumanPlayer(String name) {
        super(name);
    }

    @Override
    public boolean decideToEat(WestCard discardedCard) {
        throw new UnsupportedOperationException("Human decision handled by Controller via UI.");
    }

    @Override
    public WestCard decideDiscard() {
        throw new UnsupportedOperationException("Human decision handled by Controller via UI.");
    }


    @Override
    public Map<WestCard, List<WestCard>> decideSends(PhomGameState gameState) {
        throw new UnsupportedOperationException("Human decision handled by Controller via UI.");
    }

}
