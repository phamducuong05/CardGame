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
    // ... các thuộc tính giữ nguyên
    private List<StandardCard> cardsOnTable;
    private List<TienLenPlayer> playerRankings;
    private boolean isFirstTurn = true;

    public TienLenMienBacGameLogic() {
        super();
        this.cardsOnTable = new ArrayList<>();
        this.playerRankings = new ArrayList<>();
    }

    public TienLenMienBacGameLogic(Deck<StandardCard, TienLenPlayer> deck, List<TienLenPlayer> players,
            int numberOfCards) {
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
        try {
            if (this.cardsOnTable != null) {
                this.cardsOnTable.clear();
            }
            System.out.println("Logic: Cards on table cleared.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear cards on table", e);
        }
    }

    public void setCurrentPlayer(TienLenPlayer player) {
        this.currentPlayer = player;
    }

    @Override
    public TienLenPlayer getFirstPlayer(List<TienLenPlayer> players) {
        try {
            for (TienLenPlayer player : players) {
                for (StandardCard card : player.getHand()) {
                    if (card.getRank() == Rank.THREE && card.getSuit() == Suit.SPADES) {
                        return player;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to determine first player", e);
        }
    }

    @Override
    public boolean isValidMove(List<StandardCard> selectedCards) {
        try {
            if ((cardsOnTable == null || cardsOnTable.isEmpty()) && isFirstTurn) {
                return selectedCards.get(0).getRank() == Rank.THREE && selectedCards.get(0).getSuit() == Suit.SPADES;
            }
            if (cardsOnTable == null || cardsOnTable.isEmpty()) {
                return isValidCombination(selectedCards);
            }
            if (!isValidCombination(selectedCards))
                return false;
            return isCounter(cardsOnTable, selectedCards);
        } catch (Exception e) {
            throw new RuntimeException("Error validating move", e);
        }
    }

    @Override
    public boolean endGame() {
        try {
            int playersWithCards = 0;
            for (TienLenPlayer player : players) {
                if (!player.getHand().isEmpty()) {
                    playersWithCards++;
                }
            }
            return playersWithCards <= players.size() - 1;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check end game condition", e);
        }
    }

    @Override
    public void nextTurn() {
        try {
            currentPlayer = getCurrentPlayer();
            currentPlayer = getPlayers().get((getPlayers().indexOf(currentPlayer) + 1) % getPlayers().size());
        } catch (Exception e) {
            throw new RuntimeException("Failed to switch to next turn", e);
        }
    }

    public void playCards(List<StandardCard> selectedCards) {
        try {
            if (isValidMove(selectedCards)) {
                currentPlayer.getHand().removeAll(selectedCards);
                this.cardsOnTable = new ArrayList<>(selectedCards);
                TienLenGameState gameState = getCurrentGameState();
                System.out.println("LogicCtrl: Sau khi gameLogic.playCards. Bài trên bàn hiện tại (model): "
                        + gameState.getCardsOnTable());
            } else {
                System.out.println("Invalid card combination!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to play cards", e);
        }
    }

    private boolean isValidCombination(List<StandardCard> selectedCards) {
        try {
            if (selectedCards == null || selectedCards.isEmpty()) {
                return false;
            }
            if (selectedCards.size() == 1)
                return true;
            if (isPair(selectedCards))
                return true;
            if (isThreeOfKind(selectedCards))
                return true;
            if (isFourOfKind(selectedCards))
                return true;
            if (isSequence(selectedCards))
                return true;
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate combination", e);
        }
    }

    public boolean isSameSuit(StandardCard c1, StandardCard c2) {
        return c1.getSuit() == c2.getSuit();
    }

    public boolean isSameColor(StandardCard c1, StandardCard c2) {
        boolean allRed = (c1.getSuit() == Suit.HEARTS && c2.getSuit() == Suit.DIAMONDS) ||
                (c1.getSuit() == Suit.DIAMONDS && c2.getSuit() == Suit.HEARTS);
        boolean allBlack = (c1.getSuit() == Suit.CLUBS && c2.getSuit() == Suit.SPADES) ||
                (c1.getSuit() == Suit.SPADES && c2.getSuit() == Suit.CLUBS);
        return allRed || allBlack;
    }

    public boolean isPair(List<StandardCard> selectedCards) {
        return (selectedCards.size() == 2 && selectedCards.get(0).getRank() == selectedCards.get(1).getRank()) &&
                isSameColor(selectedCards.get(0), selectedCards.get(1));
    }

    public boolean isThreeOfKind(List<StandardCard> selectedCards) {
        return selectedCards.size() == 3 &&
                selectedCards.get(0).getRank() == selectedCards.get(1).getRank() &&
                selectedCards.get(1).getRank() == selectedCards.get(2).getRank();
    }

    public boolean isFourOfKind(List<StandardCard> selectedCards) {
        return selectedCards.size() == 4 &&
                selectedCards.get(0).getRank() == selectedCards.get(1).getRank() &&
                selectedCards.get(1).getRank() == selectedCards.get(2).getRank() &&
                selectedCards.get(2).getRank() == selectedCards.get(3).getRank();
    }

    public boolean isSequence(List<StandardCard> selectedCards) {
        try {
            if (selectedCards.size() < 3)
                return false;
            for (StandardCard card : selectedCards) {
                if (card.getRank() == Rank.TWO)
                    return false;
            }
            selectedCards.sort(Comparator.comparing(StandardCard::getRank).thenComparing(StandardCard::getSuit));
            for (int i = 1; i < selectedCards.size(); i++) {
                if ((selectedCards.get(i).getRank().ordinal() != selectedCards.get(i - 1).getRank().ordinal() + 1) ||
                        (selectedCards.get(i).getSuit() != selectedCards.get(i - 1).getSuit()))
                    return false;
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check sequence", e);
        }
    }

    public boolean isCounter(List<StandardCard> UcardsOnTable, List<StandardCard> UselectedCards) {
        try {
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

            cardsOnTable.sort(new StandardCardComparator());
            selectedCards.sort(new StandardCardComparator());

            if (cardsOnTable.size() == 1 && cardsOnTable.getFirst().getRank() == Rank.TWO) {
                if (selectedCards.size() == 1 && selectedCards.getFirst().getRank() == Rank.TWO &&
                        selectedCards.getFirst().getSuit().compareTo(cardsOnTable.getFirst().getSuit()) > 0) {
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
                return selectedIsPair &&
                        selectedCards.getLast().getSuit().compareTo(cardsOnTable.getLast().getSuit()) > 0;
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
        } catch (Exception e) {
            throw new RuntimeException("Failed to compare card sets", e);
        }
    }

    public TienLenGameState getCurrentGameState() {
        try {
            List<TienLenPlayer> currentPlayers = Collections.unmodifiableList(new ArrayList<>(this.players));
            TienLenPlayer activePlayer = this.currentPlayer;
            List<StandardCard> cardsOnTable = new ArrayList<>(this.cardsOnTable);
            return new TienLenGameState(
                    currentPlayers,
                    activePlayer,
                    cardsOnTable,
                    endGame(),
                    playerRankings);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get current game state", e);
        }
    }
}