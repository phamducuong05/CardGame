package com.myteam.game.model.phom.player;

import com.myteam.game.model.core.card.StandardCard;
import java.util.*;
import com.myteam.game.model.game.PhomGameLogic;
import com.myteam.game.model.phom.gamestate.PhomGameState;

public class PhomBotPlayer extends PhomPlayer {
    private PhomGameLogic gameLogic;

    public PhomBotPlayer(String name) {
        super(name);
        try {
            gameLogic = new PhomGameLogic();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing PhomBotPlayer", e);
        }
    }

    @Override
    public boolean decideToEat(StandardCard discardedCard) {
        try {
            if (discardedCard == null) return false;
            return gameLogic.canFormPhom(this, discardedCard);
        } catch (Exception e) {
            throw new RuntimeException("Error deciding to eat card", e);
        }
    }

    @Override
    public StandardCard decideDiscard() {
        try {
            List<List<StandardCard>> phoms = findCombinations();

            List<StandardCard> cardsInPhoms = new ArrayList<>();
            for (List<StandardCard> phom : phoms) {
                cardsInPhoms.addAll(phom);
            }

            List<StandardCard> trashCards = new ArrayList<>();
            for (StandardCard card : this.getHand()) {
                if (!cardsInPhoms.contains(card)) {
                    trashCards.add(card);
                }
            }

            if (!trashCards.isEmpty()) {
                trashCards.sort(Comparator.comparingInt(card -> -card.getRank().getValue()));
                return trashCards.get(0);
            }

            List<StandardCard> hand = new ArrayList<>(this.getHand());
            hand.sort(Comparator.comparingInt(card -> -card.getRank().getValue()));
            return hand.get(0);
        } catch (Exception e) {
            throw new RuntimeException("Error deciding discard card", e);
        }
    }

}
