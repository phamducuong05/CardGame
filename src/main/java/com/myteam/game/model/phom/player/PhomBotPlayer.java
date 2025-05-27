package com.myteam.game.model.phom.player;
import com.myteam.game.model.core.card.StandardCard;
import java.util.*;
import com.myteam.game.model.game.PhomGameLogic;
import com.myteam.game.model.phom.gamestate.PhomGameState;


public class PhomBotPlayer extends PhomPlayer {
    private PhomGameLogic gameLogic;

    public PhomBotPlayer(String name) {
        super(name);
        gameLogic = new PhomGameLogic();
    }


    @Override
    public boolean decideToEat(StandardCard discardedCard) {
        if (discardedCard == null) return false;

        return gameLogic.canFormPhom(this, discardedCard);
    }

    @Override
    public StandardCard decideDiscard() {
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
    }


    @Override
    public Map<StandardCard, List<StandardCard>> decideSends(PhomGameState gameState) {
        Map<StandardCard, List<StandardCard>> cardsToSend = new HashMap<>();
        List<List<StandardCard>> allOpponentMelds = gameState.getAllPlayerMelds(); // Lấy tất cả phỏm của đối thủ

        if (allOpponentMelds != null && !allOpponentMelds.isEmpty()) {
            List<StandardCard> trashCards = new ArrayList<>();
            List<List<StandardCard>> phoms = findCombinations();
            List<StandardCard> cardsInPhoms = new ArrayList<>();
            for (List<StandardCard> phom : phoms) {
                cardsInPhoms.addAll(phom);
            }

            for (StandardCard card : this.getHand()) {
                if (!cardsInPhoms.contains(card)) {
                    trashCards.add(card);
                }
            }

            if (trashCards.isEmpty()) {
                return new HashMap<>();
            }


            for (StandardCard cardToSend : trashCards) {
                for (List<StandardCard> meld : allOpponentMelds) {
                    List<StandardCard> potentialMeld = new ArrayList<>(meld);
                    potentialMeld.add(cardToSend);
                    if (gameLogic.isValidCombination(potentialMeld)) {
                        cardsToSend.put(cardToSend, meld);
                    }
                    trashCards.remove(cardToSend);
                    break;
                }
            }
        }
        return cardsToSend;
    }
}
