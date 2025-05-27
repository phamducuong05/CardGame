package com.myteam.game.model.game;

import com.myteam.game.model.core.card.StandardCard;
import com.myteam.game.model.core.card.StandardCardComparator;
import com.myteam.game.model.core.deck.Deck;
import com.myteam.game.model.core.enums.Rank;
import com.myteam.game.model.core.enums.Suit;
import com.myteam.game.model.tienlen.gamestate.TienLenGameState;
import com.myteam.game.model.tienlen.player.TienLenPlayer;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TienLenMienBacGameLogic extends Game<StandardCard, TienLenPlayer> {
    private List<StandardCard> cardsOnTable;
    private List<TienLenPlayer> playerRankings;
    private int skipCount = 0;
    private boolean isFirstTurn = true;

    public TienLenMienBacGameLogic() {
        super();
        this.cardsOnTable = new ArrayList<>();
        this.playerRankings = new ArrayList<>();
    }

    public TienLenMienBacGameLogic(Deck<StandardCard, TienLenPlayer> deck, List<TienLenPlayer> players, int numberOfCards) {
        super(deck, players, numberOfCards);
        this.cardsOnTable = new ArrayList<>();
        this.playerRankings = new ArrayList<>();
    }

    public void setIsFirstturn(boolean isFirstTurn) {
        this.isFirstTurn = isFirstTurn;
    }

    public boolean isFirstTurn() {
        return isFirstTurn;
    }

    public void clearCardsOnTable() {
        if (this.cardsOnTable != null) {
            this.cardsOnTable.clear();
        }
        System.out.println("Logic: Cards on table cleared.");
    }

    public void setCurrentPlayer(TienLenPlayer player) {
        this.currentPlayer = player;
    }

    @Override
    public TienLenPlayer getFirstPlayer(List<TienLenPlayer> players) {
        for (TienLenPlayer player : players) {
            for (StandardCard card : player.getHand()) {
                if (card.getRank() == Rank.THREE && card.getSuit() == Suit.SPADES) {
                    return player;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isValidMove(List<StandardCard> selectedCards) {
        if ((cardsOnTable == null || cardsOnTable.isEmpty()) && isFirstTurn) {
            // If no cards on the table, any valid combination can be played
            return selectedCards.get(0).getRank() == Rank.THREE || selectedCards.get(0).getSuit() == Suit.SPADES;
        } else if (!isFirstTurn) {
            return true;
        }
        if (!isValidCombination(selectedCards))
            return false;
        return isCounter(cardsOnTable, selectedCards);
    }

    @Override
    public boolean endGame() {
        int playersWithCards = 0;
        for (TienLenPlayer player : players) {
            if (!player.getHand().isEmpty()) {
                playersWithCards++;
            }
        } 
        return playersWithCards <= 3; // Game kết thúc khi chỉ còn 1 người (hoặc 0 người) có bài
    }

    @Override
    public void nextTurn() {
        currentPlayer = getCurrentPlayer();
        currentPlayer = getPlayers().get((getPlayers().indexOf(currentPlayer) + 1) % getPlayers().size());
    }

    public void playCards(List<StandardCard> selectedCards) {
        if (isValidMove(selectedCards)) {
            currentPlayer.getHand().removeAll(selectedCards);
            this.cardsOnTable = new ArrayList<>(selectedCards);
            TienLenGameState gameState = getCurrentGameState();
            System.out.println("LogicCtrl: Sau khi gameLogic.playCards. Bài trên bàn hiện tại (model): "
                    + gameState.getCardsOnTable());
        } else {
            System.out.println("Invalid card combination!");
        }
        
    }

    private boolean isValidCombination(List<StandardCard> selectedCards) {
        if (isPair(selectedCards))
            return true;
        if (isThreeOfKind(selectedCards))
            return true;
        if (isFourOfKind(selectedCards))
            return true;
        if (isSequence(selectedCards))
            return true;
        return !selectedCards.isEmpty();
    }

    public boolean isSameSuit(StandardCard c1, StandardCard c2) {
        return c1.getSuit() == c2.getSuit();
    }

    public boolean isSameColor(StandardCard c1, StandardCard c2) {
        boolean allRed = (c1.getSuit() == Suit.HEARTS && c2.getSuit() == Suit.DIAMONDS)
                || (c1.getSuit() == Suit.DIAMONDS && c2.getSuit() == Suit.HEARTS);
        boolean allBlack = (c1.getSuit() == Suit.CLUBS && c2.getSuit() == Suit.SPADES)
                || (c1.getSuit() == Suit.SPADES && c2.getSuit() == Suit.CLUBS);
        return allRed || allBlack;
    }

    public boolean isPair(List<StandardCard> selectedCards) {
        return (selectedCards.size() == 2 && selectedCards.get(0).getRank() == selectedCards.get(1).getRank())
                && isSameColor(selectedCards.get(0), selectedCards.get(1));
    }

    public boolean isThreeOfKind(List<StandardCard> selectedCards) {
        return selectedCards.size() == 3
                && selectedCards.get(0).getRank() == selectedCards.get(1).getRank()
                && selectedCards.get(1).getRank() == selectedCards.get(2).getRank();
    }

    public boolean isFourOfKind(List<StandardCard> selectedCards) {
        return selectedCards.size() == 4
                && selectedCards.get(0).getRank() == selectedCards.get(1).getRank()
                && selectedCards.get(1).getRank() == selectedCards.get(2).getRank()
                && selectedCards.get(2).getRank() == selectedCards.get(3).getRank();
    }

    public boolean isSequence(List<StandardCard> selectedCards) {
        if (selectedCards.size() < 3)
            return false;
        selectedCards.sort(Comparator.comparing(StandardCard::getRank).thenComparing(StandardCard::getSuit));
        for (int i = 1; i < selectedCards.size(); i++) {
            if ((selectedCards.get(i).getRank().getValue() != selectedCards.get(i - 1).getRank().getValue() + 1)
                    || (selectedCards.get(i).getSuit() != selectedCards.get(i - 1).getSuit()))
                return false;
        }
        return true;
    }

    public boolean isCounter(List<StandardCard> UcardsOnTable, List<StandardCard> UselectedCards) {
        List<StandardCard> cardsOnTable = new ArrayList<>(UcardsOnTable);
        List<StandardCard> selectedCards = new ArrayList<>(UselectedCards);

        boolean tableIsPair = isPair(cardsOnTable);
        boolean selectedIsPair = isPair(selectedCards);
        boolean tableIsThree = isThreeOfKind(cardsOnTable);
        boolean selectedIsThree = isThreeOfKind(selectedCards);
        boolean tableIsFour = isFourOfKind(cardsOnTable);
        boolean selectedIsFour = isFourOfKind(selectedCards);
        boolean tableIsSequence = isSequence(cardsOnTable);
        boolean selectedIsSequence = isSequence(selectedCards);

        cardsOnTable.sort(new StandardCardComparator()); // Hoặc comparator của bạn
        selectedCards.sort(new StandardCardComparator());
        // special counter only for cards with rank 2
        if (cardsOnTable.size() == 1 && cardsOnTable.getFirst().getRank() == Rank.TWO) {
            if (selectedCards.size() == 1 && selectedCards.getFirst().getRank() == Rank.TWO
                    && selectedCards.getFirst().getSuit().compareTo(cardsOnTable.getFirst().getSuit()) > 0) {
                return true;
            }

            return selectedIsFour;
        }

        if (cardsOnTable.size() == 1 && cardsOnTable.getFirst().getRank() != Rank.TWO) {
            if (selectedCards.size() != 1)
                return false;
            int cardOnTableRank = cardsOnTable.getFirst().getRank().ordinal();
            int cardSelectedRank = selectedCards.getFirst().getRank().ordinal();
            int cardOnTableSuit = cardsOnTable.getFirst().getSuit().ordinal();
            int cardSelectedSuit = selectedCards.getFirst().getSuit().ordinal();
            if (cardOnTableRank < cardSelectedRank && cardOnTableSuit == cardSelectedSuit) {
                return true;
            } else
                return false;
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
            if (!isSameSuit(cardsOnTable.get(i), selectedCards.get(i)))
                return false;
        }

        StandardCard highestTableCard = cardsOnTable.getLast();
        StandardCard highestSelectedCard = selectedCards.getLast();

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
        List<StandardCard> cardsOnTable = new ArrayList<>(this.cardsOnTable);
        return new TienLenGameState(
                currentPlayers,
                activePlayer,
                cardsOnTable,
                endGame(),
                playerRankings);
    }

}