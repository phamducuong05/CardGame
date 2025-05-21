package com.myteam.game.model.phom;
import com.myteam.game.model.core.card.WestCard;
import java.util.*;
import com.myteam.game.model.game.PhomGameLogic;


public class PhomBotPlayer extends PhomPlayer {
    private PhomGameLogic gameLogic;

    public PhomBotPlayer(String name) {
        super(name);
        gameLogic = new PhomGameLogic();
    }


    @Override
    public boolean decideToEat(WestCard discardedCard) {
        if (discardedCard == null) return false;

        return gameLogic.canFormPhom(this, discardedCard);
    }

    @Override
    public WestCard decideDiscard() {
        List<List<WestCard>> phoms = findCombinations();

        List<WestCard> cardsInPhoms = new ArrayList<>();
        for (List<WestCard> phom : phoms) {
            cardsInPhoms.addAll(phom);
        }

        List<WestCard> trashCards = new ArrayList<>();
        for (WestCard card : this.getHand()) {
            if (!cardsInPhoms.contains(card)) {
                trashCards.add(card);
            }
        }

        if (!trashCards.isEmpty()) {
            trashCards.sort(Comparator.comparingInt(card -> -card.getRank().getValue()));
            return trashCards.get(0);
        }

        List<WestCard> hand = new ArrayList<>(this.getHand());
        hand.sort(Comparator.comparingInt(card -> -card.getRank().getValue()));
        return hand.get(0);
    }


    @Override
    public Map<WestCard, List<WestCard>> decideSends(PhomGameState gameState) {
        Map<WestCard, List<WestCard>> cardsToSend = new HashMap<>();
        List<List<WestCard>> allOpponentMelds = gameState.getAllPlayerMelds(); // Lấy tất cả phỏm của đối thủ

        if (allOpponentMelds != null && !allOpponentMelds.isEmpty()) {
            List<WestCard> trashCards = new ArrayList<>();
            List<List<WestCard>> phoms = findCombinations();
            List<WestCard> cardsInPhoms = new ArrayList<>();
            for (List<WestCard> phom : phoms) {
                cardsInPhoms.addAll(phom);
            }

            for (WestCard card : this.getHand()) {
                if (!cardsInPhoms.contains(card)) {
                    trashCards.add(card);
                }
            }

            if (trashCards.isEmpty()) {
                return new HashMap<>();
            }


            for (WestCard cardToSend : trashCards) {
                for (List<WestCard> meld : allOpponentMelds) {
                    List<WestCard> potentialMeld = new ArrayList<>(meld);
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
