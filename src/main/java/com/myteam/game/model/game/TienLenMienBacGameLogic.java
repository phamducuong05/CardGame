package com.myteam.game.model.game;

import com.myteam.game.model.core.card.Card;
import com.myteam.game.model.core.card.WestCard;
import com.myteam.game.model.core.deck.Deck;
import com.myteam.game.model.core.enums.Rank;
import com.myteam.game.model.core.enums.Suit;
import com.myteam.game.model.phom.PhomGameState;
import com.myteam.game.model.phom.PhomPlayer;
import com.myteam.game.model.tienlen.TienLenBotPlayer;
import com.myteam.game.model.tienlen.TienLenGameState;
import com.myteam.game.model.tienlen.TienLenPlayer;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TienLenMienBacGameLogic extends Game<WestCard, TienLenPlayer> {
    private List<WestCard> cardsOnTable;
    private List<TienLenPlayer> playerRankings;

    public TienLenMienBacGameLogic() {
    }

    public TienLenMienBacGameLogic(Deck<WestCard, TienLenPlayer> deck, List<TienLenPlayer> players, int numberOfCards) {
        super(deck, players, numberOfCards);
        cardsOnTable = new ArrayList<>();
    }


    @Override
    public TienLenPlayer getFirstPlayer(List<TienLenPlayer> players) {
        for (TienLenPlayer player : players) {
            for (WestCard card : player.getHand()) {
                if (card.getRank() == Rank.THREE && card.getSuit() == Suit.SPADES) {
                    return player;
                }
            }
        }
        return null;
    }


    @Override
    public boolean isValidMove(List<WestCard> selectedCards) {
        if (!isValidCombination(selectedCards)) return false;
        return isCounter(cardsOnTable, selectedCards);
    }

    @Override
    public boolean endGame() {
        for (TienLenPlayer player : players) {
            if (!player.getHand().isEmpty() && !playerRankings.contains(player)) {
                playerRankings.add(player);
                return false;
            }
        }
        return true;
    }

    @Override
    public void nextTurn() {
        currentPlayer = getCurrentPlayer();
        currentPlayer = getPlayers().get((getPlayers().indexOf(currentPlayer) + 1) % getPlayers().size());
    }

    public void playCards(List<WestCard> selectedCards) {
        if (isValidMove(selectedCards)) {
            currentPlayer.getHand().removeAll(selectedCards);
            cardsOnTable = selectedCards;
        } else {
            System.out.println("Invalid card combination!");
        }
    }


    private boolean isValidCombination(List<WestCard> selectedCards) {
        if (isPair(selectedCards)) return true;
        if (isThreeOfKind(selectedCards)) return true;
        if (isFourOfKind(selectedCards)) return true;
        if (isSequence(selectedCards)) return true;
        return !selectedCards.isEmpty();
    }


    public boolean isSameSuit(WestCard c1, WestCard c2) {
        return c1.getSuit() == c2.getSuit();
    }

    public boolean isSameColor(WestCard c1, WestCard c2) {
        boolean allRed = (c1.getSuit() == Suit.HEARTS && c2.getSuit() == Suit.DIAMONDS)
                || (c1.getSuit() == Suit.DIAMONDS && c2.getSuit() == Suit.HEARTS);
        boolean allBlack = (c1.getSuit() == Suit.CLUBS && c2.getSuit() == Suit.SPADES)
                || (c1.getSuit() == Suit.SPADES && c2.getSuit() == Suit.CLUBS);
        return allRed || allBlack;
    }

    public boolean isPair(List<WestCard> selectedCards) {
        return (selectedCards.size() == 2 && selectedCards.get(0).getRank() == selectedCards.get(1).getRank())
                && isSameColor(selectedCards.get(0), selectedCards.get(1));
    }

    public boolean isThreeOfKind(List<WestCard> selectedCards) {
        return selectedCards.size() == 3
                && selectedCards.get(0).getRank() == selectedCards.get(1).getRank()
                && selectedCards.get(1).getRank() == selectedCards.get(2).getRank();
    }

    public boolean isFourOfKind(List<WestCard> selectedCards) {
        return selectedCards.size() == 4
                && selectedCards.get(0).getRank() == selectedCards.get(1).getRank()
                && selectedCards.get(1).getRank() == selectedCards.get(2).getRank()
                && selectedCards.get(2).getRank() == selectedCards.get(3).getRank();
    }

    public boolean isSequence(List<WestCard> selectedCards) {
        if (selectedCards.size() < 3) return false;
        selectedCards.sort(Comparator.comparing(WestCard::getRank).thenComparing(WestCard::getSuit));
        for (int i = 1; i < selectedCards.size(); i++) {
            if ((selectedCards.get(i).getRank().getValue() != selectedCards.get(i - 1).getRank().getValue() + 1)
                    || (selectedCards.get(i).getSuit() != selectedCards.get(i - 1).getSuit()))
                return false;
        }
        return true;
    }

    public boolean isCounter(List<WestCard> cardsOnTable, List<WestCard> selectedCards) {
        boolean tableIsPair = isPair(cardsOnTable);
        boolean selectedIsPair = isPair(selectedCards);
        boolean tableIsThree = isThreeOfKind(cardsOnTable);
        boolean selectedIsThree = isThreeOfKind(selectedCards);
        boolean tableIsFour = isFourOfKind(cardsOnTable);
        boolean selectedIsFour = isFourOfKind(selectedCards);
        boolean tableIsSequence = isSequence(cardsOnTable);
        boolean selectedIsSequence = isSequence(selectedCards);

        cardsOnTable.sort(Comparator.comparing(WestCard::getRank).thenComparing(WestCard::getSuit));
        selectedCards.sort(Comparator.comparing(WestCard::getRank).thenComparing(WestCard::getSuit));

        //special counter only for cards with rank 2
        if (cardsOnTable.size() == 1 && cardsOnTable.getLast().getRank() == Rank.TWO) {
            if (selectedCards.size() == 1 && selectedCards.getFirst().getRank() == Rank.TWO
            && selectedCards.getFirst().getSuit().compareTo(cardsOnTable.getFirst().getSuit()) > 0) {
                return true;
            }

            return selectedIsFour;
        }

        if (tableIsPair && cardsOnTable.getFirst().getRank() == Rank.TWO) {
            return selectedIsPair && selectedCards.getLast().getSuit().compareTo(cardsOnTable.getLast().getSuit()) > 0;
        }

        if (cardsOnTable.size() != selectedCards.size()) {
            return false;
        }

        if ((tableIsPair && !selectedIsPair) ||
                (tableIsThree && !selectedIsThree) ||
                (tableIsFour && !selectedIsFour) ||
                (tableIsSequence && !selectedIsSequence)) {
            return false;
        }

        for (int i = 0; i < cardsOnTable.size(); i++) {
            if (!isSameSuit(cardsOnTable.get(i), selectedCards.get(i))) return false;
        }

        WestCard highestTableCard = cardsOnTable.getLast();
        WestCard highestSelectedCard = selectedCards.getLast();

        int rankComparison = highestSelectedCard.getRank().compareTo(highestTableCard.getRank());
        if (rankComparison > 0) {
            return true;
        } else if (rankComparison == 0) {
            return highestSelectedCard.getSuit().compareTo(highestTableCard.getSuit()) > 0;
        }
        return false;
    }

    public TienLenGameState getCurrentGameState() {
        List<TienLenPlayer> currentPlayers = Collections.unmodifiableList(new ArrayList<>(this.players));
        TienLenPlayer activePlayer = this.currentPlayer;
        List<WestCard> cardsOnTable = Collections.unmodifiableList(new ArrayList<>(this.cardsOnTable));
        return new TienLenGameState(
                currentPlayers,
                activePlayer,
                cardsOnTable,
                endGame(),
                playerRankings
        );
    }

}