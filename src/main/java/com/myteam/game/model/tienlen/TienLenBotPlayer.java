package com.myteam.game.model.tienlen;

import com.myteam.game.model.core.card.WestCard;
import com.myteam.game.model.core.card.WestCardComparator;
import com.myteam.game.model.core.enums.Rank;
import com.myteam.game.model.game.TienLenMienBacGameLogic;
import com.myteam.game.model.player.Player;

import java.util.*;

/**
 * A bot player implementation for TienLen game
 */
public class TienLenBotPlayer extends TienLenPlayer {
    private final TienLenMienBacGameLogic gameLogic = new TienLenMienBacGameLogic();

    /**
     * Constructor
     * 
     * @param name The name of the bot player
     */
    public TienLenBotPlayer(String name) {
        super(name);
    }
    public boolean isPlaying() {
        return getState() == State.PLAYING;
    }

    public void sortHand(){
        getHand().sort(Comparator.comparing((WestCard c) -> c.getRank().ordinal()).thenComparing((WestCard c) -> c.getSuit().ordinal()));
    }

    /**
     * Decide which cards to play based on the current game state
     * 
     * @param gameState The current game state
     * @return The cards to play, or null/empty to pass
     */
    public List<WestCard> decideCardsToPlay(TienLenGameState gameState) {
        List<WestCard> hand = getHand();
        List<WestCard> lastPlayed = gameState.getLastPlayedCards();
        
        // If this is the first play or we are starting a new round, play the lowest card
        if (lastPlayed == null || lastPlayed.isEmpty()) {
            return findLowestSingleCard();
        }
        
        // Try to find a play that beats the last play
        return findPlayThatBeatsLastPlay(lastPlayed);
    }
    
    /**
     * Find a single card with the lowest rank to play
     * 
     * @return A list containing the lowest card, or empty if no cards left
     */
    private List<WestCard> findLowestSingleCard() {
        List<WestCard> hand = getHand();
        if (hand.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Sort hand by rank
        hand.sort(Comparator.comparingInt(card -> card.getRank().ordinal()));
        
        // Return lowest card
        List<WestCard> result = new ArrayList<>();
        result.add(hand.get(0));
        return result;
    }
    
    /**
     * Find a play that beats the last played cards
     * 
     * @param lastPlayed The last played cards
     * @return A list of cards that beats the last play, or empty to pass
     */
    private List<WestCard> findPlayThatBeatsLastPlay(List<WestCard> lastPlayed) {
        // Must play same number of cards
        int numCards = lastPlayed.size();
        List<WestCard> hand = getHand();
        
        if (numCards == 1) {
            // Single card play
            WestCard lastCard = lastPlayed.get(0);
            for (WestCard card : hand) {
                if (card.getRank().ordinal() > lastCard.getRank().ordinal()) {
                    List<WestCard> result = new ArrayList<>();
                    result.add(card);
                    return result;
                }
            }
        } else if (numCards == 2) {
            // Pair play - simplified implementation
            // In a real implementation, would check for valid pairs based on TienLen rules
        } else if (numCards == 3) {
            // Three of a kind - simplified implementation
        } else if (numCards >= 4) {
            // Straight or other combinations - simplified implementation
        }
        
        // No valid play found, pass
        return new ArrayList<>();
    }

    /**
     * Tự động đánh bài.
     *
     * Tìm nước đi phù hợp để chặn những quân trên bàn.
     * Nếu có nước phù hợp thì sẽ setSelectedCards và playCard
     * Nếu không có thì skipTurn
     *
     * @param cardsOnTable: Danh sách các quân bài đang ở trên bàn
     */

    public List<WestCard> autoPlay(List<WestCard> cardsOnTable) {
        Helper helper = new Helper();
        List<WestCard> selected = new ArrayList<>();
        // Analyze the current table
        boolean tableIsNone = cardsOnTable.isEmpty();
        boolean tableIsSingle = (cardsOnTable.size() == 1);
        boolean tableIsPair = gameLogic.isPair(cardsOnTable);
        boolean tableIsThree = gameLogic.isThreeOfKind(cardsOnTable);
        boolean tableIsFour = gameLogic.isFourOfKind(cardsOnTable);
        boolean tableIsSequence = gameLogic.isSequence(cardsOnTable);

        cardsOnTable.sort(Comparator.comparing((WestCard c) -> c.getRank().ordinal()).thenComparing((WestCard c) -> c.getSuit().ordinal()));
        sortHand();

        if (tableIsNone) {
            WestCard randomCard = getHand().get(new Random().nextInt(handSize()));
            selected = List.of(randomCard);
        } else if (tableIsSingle) {
            WestCard singleCard = helper.findSingleCard(getHand(), cardsOnTable);
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
            skipTurn();
            return new ArrayList<>();
        } else {
            setSelectedCards(new ArrayList<>(selected));
            List<WestCard> played = playCard();
            setSelectedCards(new ArrayList<>());
            return played;
        }
    }

    private static class Helper{
        private final WestCardComparator comparator = new WestCardComparator();
        private final TienLenMienBacGameLogic gameLogic = new TienLenMienBacGameLogic();
        public WestCard findSingleCard(List<WestCard> hand, List<WestCard> cardsOnTable){
            WestCard topCard = cardsOnTable.getFirst();

            for (WestCard card : hand) {
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

        public List<WestCard> findPair(List<WestCard> hand, List<WestCard> cardsOnTable) {
            if(hand.size() < 2){
                return null;
            }
            for (int i = 0; i < hand.size() - 1; i++) {
                List<WestCard> selectedCards = new ArrayList<>(Arrays.asList(hand.get(i), hand.get(i + 1)));
                if (gameLogic.isPair(selectedCards)
                        && gameLogic.isSameColor(selectedCards.getFirst(), cardsOnTable.getFirst())
                        && comparator.compare(selectedCards.getFirst(), cardsOnTable.getFirst()) > 0) {
                    return selectedCards;
                }
            }
            return new ArrayList<>();
        }

        public List<WestCard> findThree(List<WestCard> hand, List<WestCard> cardsOnTable) {
            if(hand.size() < 3){
                return new ArrayList<>();
            }
            for (int i = 0; i < hand.size() - 2; i++) {
                List<WestCard> selectedCards = Arrays.asList(hand.get(i), hand.get(i + 1), hand.get(i + 2));
                if (gameLogic.isThreeOfKind(selectedCards)
                        && gameLogic.isSameColor(selectedCards.getFirst(), cardsOnTable.getFirst())
                        && comparator.compare(selectedCards.getFirst(), cardsOnTable.getFirst()) > 0) {
                    return selectedCards;
                }
            }
            return new ArrayList<>();
        }

        public List<WestCard> findFour(List<WestCard> hand, List<WestCard> cardsOnTable) {
            if(hand.size() < 4){
                return new ArrayList<>();
            }
            for (int i = 0; i < hand.size() - 3; i++) {
                List<WestCard> selectedCards = Arrays.asList(hand.get(i), hand.get(i + 1), hand.get(i + 2), hand.get(i + 3));
                if (gameLogic.isFourOfKind(selectedCards)
                        && gameLogic.isSameColor(selectedCards.getFirst(), cardsOnTable.getFirst())
                        && comparator.compare(selectedCards.getFirst(), cardsOnTable.getFirst()) > 0) {
                    return selectedCards;
                }
            }
            return new ArrayList<>();
        }

        public List<WestCard> findSequence(List<WestCard> hand, List<WestCard> cardsOnTable) {
            int length = cardsOnTable.size();
            if (hand.size() < length) {
                return new ArrayList<>();
            }
            for (int i = 0; i <= hand.size() - length; i++) {
                List<WestCard> selectedCards = hand.subList(i, i + length);
                if (gameLogic.isSequence(selectedCards)
                        && comparator.compare(selectedCards.getFirst(), cardsOnTable.getFirst()) > 0) {
                    return new ArrayList<>(selectedCards);
                }
            }
            return new ArrayList<>();
        }
    }
}
