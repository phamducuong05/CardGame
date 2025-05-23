package com.myteam.game.model.tienlen;

import com.myteam.game.model.core.card.WestCard;

import java.util.List;

public class TienLenHumanPlayer extends TienLenPlayer{
     public TienLenHumanPlayer(String name) {
         super(name);
     }

    @Override
    public List<WestCard> decideCardsToPlay(TienLenGameState gameState){
         throw new UnsupportedOperationException("Human decision handled by Controller via UI.");
    }

    public boolean decideToSkip(){
        throw new UnsupportedOperationException("Human decision handled by Controller via UI.");
    }
}
