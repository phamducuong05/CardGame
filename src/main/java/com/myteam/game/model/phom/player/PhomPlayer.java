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
        try {
            discardCards = new ArrayList<>();
            eatenCards = new ArrayList<>();
            allPhoms = new ArrayList<>();
            numOfTurn = 0;
        } catch (Exception e) {
            throw new RuntimeException("Error initializing PhomPlayer", e);
        }
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
        try {
            if (card != null) {
                this.discardCards.add(card);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error adding discard card", e);
        }
    }

    public List<List<StandardCard>> findCombinations() {
        try {
            List<List<StandardCard>> allPhoms = new ArrayList<>();
            if (this.getHand() == null || this.getHand().size() < 3) {
                return allPhoms;
            }

            List<StandardCard> newHand = new ArrayList<>(this.getHand());
            newHand.addAll(this.eatenCards);

            // Phom by rank
            Map<Rank, List<StandardCard>> rankMap = new HashMap<>();
            for (Rank rank : Rank.values()) {
                for (StandardCard card : newHand) {
                    if (rank == card.getRank()) {
                        rankMap.computeIfAbsent(rank, k -> new ArrayList<>()).add(card);
                    }
                }
            }

            for (List<StandardCard> cards : rankMap.values()) {
                if (cards.size() >= 3) {
                    allPhoms.add(cards);
                    newHand.removeAll(cards);
                }
            }

            // Phom by suit
            Map<String, List<StandardCard>> suitMap = new HashMap<>();
            for (StandardCard card : newHand) {
                suitMap.computeIfAbsent(card.getSuit().getValue(), k -> new ArrayList<>()).add(card);
            }

            for (List<StandardCard> cards : suitMap.values()) {
                cards.sort(Comparator.comparingInt(c -> c.getRank().getValue()));
            }

            for (List<StandardCard> cards : suitMap.values()) {
                for (List<StandardCard> combo : findAllConsecutive(cards)) {
                    allPhoms.add(combo);
                    newHand.removeAll(combo);
                }
            }

            return allPhoms;
        } catch (Exception e) {
            throw new RuntimeException("Error finding phom combinations", e);
        }
    }

    public List<List<StandardCard>> findAllConsecutive(List<StandardCard> cards) {
        try {
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
                        result.add(new ArrayList<>(subResult));
                    }
                    subResult.clear();
                    subResult.add(cards.get(i + 1));
                    numberOfConsecutive = 1;
                }
            }

            if (numberOfConsecutive >= 3) {
                result.add(subResult);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error finding consecutive cards", e);
        }
    }

    public int calculateScore() {
        try {
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
        } catch (Exception e) {
            throw new RuntimeException("Error calculating score", e);
        }
    }

    public abstract boolean decideToEat(StandardCard discardedCard);

    public abstract StandardCard decideDiscard();

}
