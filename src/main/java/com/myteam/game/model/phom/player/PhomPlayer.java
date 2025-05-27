package com.myteam.game.model.phom.player;

import com.myteam.game.model.core.card.StandardCard;
import com.myteam.game.model.core.enums.Rank;
import com.myteam.game.model.phom.gamestate.PhomGameState;
import com.myteam.game.model.player.Player;
import java.util.*;

public abstract class PhomPlayer extends Player<StandardCard> {
    private List<StandardCard> discardCards;
    private List<StandardCard> eatenCards;
    private List<List<StandardCard>> allPhoms;
    private int numOfTurn;

    public PhomPlayer() {
    }

    public PhomPlayer(String name) {
        super(name);
        discardCards = new ArrayList<>();
        eatenCards = new ArrayList<>();
        allPhoms = new ArrayList<>();
        numOfTurn = 0;
    }

    public List<List<StandardCard>> getAllPhoms() {
        return allPhoms;
    }

    public void setNumOfTurn(int numOfTurn) {
        this.numOfTurn = numOfTurn;
    }

    public int getNumOfTurn() {
        return numOfTurn;
    }

    public List<StandardCard> getDiscardCards() {
        return discardCards;
    }

    public List<StandardCard> getEatenCards() {
        return eatenCards;
    }

    public void addDiscardCards(StandardCard card) {
        if (card != null)
            this.discardCards.add(card);
    }

    public List<List<StandardCard>> findCombinations() {
        List<List<StandardCard>> allPhoms = new ArrayList<>();
        if (this.getHand() == null || this.getHand().size() < 3) {
            return allPhoms;
        }
        // Find all combinations by rank
        List<StandardCard> newHand = new ArrayList<StandardCard>(this.getHand());
        newHand.addAll(this.eatenCards);

        Map<Rank, List<StandardCard>> rankMap = new HashMap<>();
        for (Rank rank : Rank.values()) {
            for (StandardCard card : newHand) {
                if (rank == card.getRank()) {
                    if (rankMap.containsKey(rank)) {
                        rankMap.get(rank).add(card);
                    } else {
                        List<StandardCard> cards = new ArrayList<>();
                        cards.add(card);
                        rankMap.put(rank, cards);
                    }
                }
            }
        }
        for (List<StandardCard> cards : rankMap.values()) {
            if (cards.size() >= 3) {
                allPhoms.add(cards);
                newHand.removeAll(cards);
            }
        }
        // Find all combinations by suit
        Map<String, List<StandardCard>> suitMap = new HashMap<>();
        for (StandardCard card : newHand) {
            if (suitMap.containsKey(card.getSuit().getValue())) {
                suitMap.get(card.getSuit().getValue()).add(card);
            } else {
                List<StandardCard> cards = new ArrayList<>();
                cards.add(card);
                suitMap.put(card.getSuit().getValue(), cards);
            }
        }
        for (List<StandardCard> cards : suitMap.values()) {
            cards.sort(Comparator.comparingInt(card -> card.getRank().getValue()));
        }

        for (List<StandardCard> cards : suitMap.values()) {
            for (List<StandardCard> cardsTemporary : findAllConsecutive(cards)) {
                allPhoms.add(cardsTemporary);
                newHand.removeAll(cardsTemporary);
            }
        }
        return allPhoms;
    }

    public List<List<StandardCard>> findAllConsecutive(List<StandardCard> cards) {
        List<List<StandardCard>> result = new ArrayList<>();
        int numberOfConsecutive = 1;
        List<StandardCard> subResult = new ArrayList<>();
        subResult.add(cards.get(0));
        for (int i = 0; i < cards.size() - 1; i++) {
            if (cards.get(i).getRank().getValue() + 1 == cards.get(i + 1).getRank().getValue()) {
                numberOfConsecutive++;
                subResult.add(cards.get(i + 1));
            } else {
                if (numberOfConsecutive >= 3) {
                    result.add(subResult);
                }
                subResult = new ArrayList<>();
                subResult.add(cards.get(i + 1));
                numberOfConsecutive = 1;
            }
        }
        if (numberOfConsecutive >= 3) {
            result.add(subResult);
        }
        return result;
    }

    public int calculateScore() {
        int score = 0;

        List<StandardCard> remainingCards = new ArrayList<>(this.getHand());
        remainingCards.addAll(this.eatenCards);

        for (List<StandardCard> phom : findCombinations()) {
            remainingCards.removeAll(phom);
        }

        for (StandardCard card : remainingCards) {
            score += card.getRank().getValue();
        }

        return score;
    }

    public abstract boolean decideToEat(StandardCard discardedCard);

    public abstract StandardCard decideDiscard();

    public abstract Map<StandardCard, List<StandardCard>> decideSends(PhomGameState gameState);
}