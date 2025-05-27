package com.myteam.game.model.tienlen.player;

import com.myteam.game.model.core.card.StandardCard;
import com.myteam.game.model.core.card.StandardCardComparator;
import com.myteam.game.model.core.enums.Rank;
import com.myteam.game.model.core.enums.Suit;
import com.myteam.game.model.game.TienLenMienBacGameLogic;
import com.myteam.game.model.tienlen.gamestate.TienLenGameState;

import java.util.*;

/**
 * A bot player implementation for TienLen game
 */
public class TienLenBotPlayer extends TienLenPlayer {
    private final TienLenMienBacGameLogic gameLogic = new TienLenMienBacGameLogic();

    public TienLenBotPlayer(String name) {
        super(name);
    }

    public void sortHand() {
        getHand().sort(Comparator.comparing((StandardCard c) -> c.getRank().ordinal())
                .thenComparing((StandardCard c) -> c.getSuit().ordinal()));
    }

    @Override
    public List<StandardCard> decideCardsToPlay(TienLenGameState gameState) {
        List<StandardCard> cardsOnTable = gameState.getCardsOnTable();

        // Try to find a play that beats the last play
        return autoPlay(cardsOnTable);
    }

    /**
     * Methods for the bot to automatically play its turn
     * 
     * @param cardsOnTable The current cards on the table
     * @return List of cards to be played
     */
    private List<StandardCard> autoPlay(List<StandardCard> cardsOnTable) {
        Helper helper = new Helper(); // Class provides methods for bot to play according to the current table
        List<StandardCard> hand = getHand();
        List<StandardCard> selected = new ArrayList<>();

        boolean tableIsNone = cardsOnTable.isEmpty();
        boolean tableIsSingle = (cardsOnTable.size() == 1);
        boolean tableIsPair = gameLogic.isPair(cardsOnTable);
        boolean tableIsThree = gameLogic.isThreeOfKind(cardsOnTable);
        boolean tableIsFour = gameLogic.isFourOfKind(cardsOnTable);
        boolean tableIsSequence = gameLogic.isSequence(cardsOnTable);

        // cardsOnTable.sort(Comparator.comparing((WestCard c) -> c.getRank().ordinal())
        // .thenComparing((WestCard c) -> c.getSuit().ordinal()));
        sortHand();

        if (tableIsNone) {
            if(gameLogic.isFirstTurn()) {
                for (StandardCard card : hand) {
                    if (card.getRank() == Rank.THREE && card.getSuit() == Suit.SPADES) {
                        selected = List.of(card);
                        return selected;
                    }
                }
            }
            StandardCard randomCard = getHand().get(new Random().nextInt(handSize()));
            selected = List.of(randomCard);
        } else if (tableIsSingle) {
            StandardCard singleCard = helper.findSingleCard(getHand(), cardsOnTable);
            if (singleCard != null) {
                selected = List.of(singleCard);
            } else if (cardsOnTable.getFirst().getRank() == Rank.TWO) {
                selected = helper.findFour(getHand(), cardsOnTable);
            }
        } else if (tableIsPair) {
            selected = helper.findPair(getHand(), cardsOnTable);
        } else if (tableIsThree) {
            selected = helper.findThree(getHand(), cardsOnTable);
        } else if (tableIsFour) {
            selected = helper.findFour(getHand(), cardsOnTable);
        } else if (tableIsSequence) {
            selected = helper.findSequence(getHand(), cardsOnTable);
        } else {
            System.out.println("Cái đéo gì đây");
        }

        if (selected == null || selected.isEmpty()) {
            return new ArrayList<>();
        } else {
            return selected;
        }
    }

    /**
     * Class to support the bot in choosing the right move to play
     */
    private static class Helper {
        private final StandardCardComparator comparator = new StandardCardComparator();
        private final TienLenMienBacGameLogic gameLogic = new TienLenMienBacGameLogic();

        public StandardCard findSingleCard(List<StandardCard> hand, List<StandardCard> cardsOnTable) {
            StandardCard topCard = cardsOnTable.getFirst();

            for (StandardCard card : hand) {
                // Nếu là lá 2 → chỉ cần mạnh hơn (cùng rank, chất cao hơn)
                if (topCard.getRank() == Rank.TWO) {
                    if (card.getRank() == Rank.TWO && comparator.compare(card, topCard) > 0) {
                        return card;
                    }
                } else {
                    // Bài khác → cùng chất và mạnh hơn
                    if (card.getSuit() == topCard.getSuit() && comparator.compare(card, topCard) > 0) {
                        return card;
                    }
                }
            }
            return null;
        }

        public List<StandardCard> findPair(List<StandardCard> hand, List<StandardCard> cardsOnTable) {
            if (hand.size() < 2) {
                return null;
            }
            for (int i = 0; i < hand.size() - 1; i++) {
                List<StandardCard> selectedCards = new ArrayList<>(Arrays.asList(hand.get(i), hand.get(i + 1)));
                if (gameLogic.isPair(selectedCards)
                        && gameLogic.isSameColor(selectedCards.getFirst(), cardsOnTable.getFirst())
                        && comparator.compare(selectedCards.getFirst(), cardsOnTable.getFirst()) > 0) {
                    return selectedCards;
                }
            }
            return new ArrayList<>();
        }

        public List<StandardCard> findThree(List<StandardCard> hand, List<StandardCard> cardsOnTable) {
            if (hand.size() < 3) {
                return new ArrayList<>();
            }
            for (int i = 0; i < hand.size() - 2; i++) {
                List<StandardCard> selectedCards = new ArrayList<>(
                        Arrays.asList(hand.get(i), hand.get(i + 1), hand.get(i + 2)));
                if (gameLogic.isThreeOfKind(selectedCards)
                        && gameLogic.isSameColor(selectedCards.getFirst(), cardsOnTable.getFirst())
                        && comparator.compare(selectedCards.getFirst(), cardsOnTable.getFirst()) > 0) {
                    return selectedCards;
                }
            }
            return new ArrayList<>();
        }

        public List<StandardCard> findFour(List<StandardCard> hand, List<StandardCard> cardsOnTable) {
            if (hand.size() < 4) {
                return new ArrayList<>();
            }
            for (int i = 0; i < hand.size() - 3; i++) {
                List<StandardCard> selectedCards = new ArrayList<>(
                        Arrays.asList(hand.get(i), hand.get(i + 1), hand.get(i + 2), hand.get(i + 3)));
                if (gameLogic.isFourOfKind(selectedCards)
                        && gameLogic.isSameColor(selectedCards.getFirst(), cardsOnTable.getFirst())
                        && comparator.compare(selectedCards.getFirst(), cardsOnTable.getFirst()) > 0) {
                    return selectedCards;
                }
            }
            return new ArrayList<>();
        }

        public List<StandardCard> findSequence(List<StandardCard> hand, List<StandardCard> cardsOnTable) {
            int length = cardsOnTable.size();
            if (hand.size() < length) {
                return new ArrayList<>();
            }
            for (int i = 0; i <= hand.size() - length; i++) {
                List<StandardCard> selectedCards = hand.subList(i, i + length);
                if (gameLogic.isSequence(selectedCards)
                        && comparator.compare(selectedCards.getFirst(), cardsOnTable.getFirst()) > 0) {
                    return new ArrayList<>(selectedCards);
                }
            }
            return new ArrayList<>();
        }
    }
}
