package com.myteam.game.model.tienlen.player;

import com.myteam.game.model.core.card.StandardCard;
import com.myteam.game.model.tienlen.gamestate.TienLenGameState;

import java.util.List;

public class TienLenHumanPlayer extends TienLenPlayer {
     public TienLenHumanPlayer(String name) {
         super(name);
     }

    @Override
    public List<StandardCard> decideCardsToPlay(TienLenGameState gameState){
         throw new UnsupportedOperationException("Human decision handled by Controller via UI.");
    }

    public boolean decideToSkip(){
        throw new UnsupportedOperationException("Human decision handled by Controller via UI.");
    }
}
