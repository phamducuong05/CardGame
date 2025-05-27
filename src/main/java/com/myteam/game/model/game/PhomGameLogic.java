package com.myteam.game.model.game;

import com.myteam.game.model.core.card.StandardCard;
import com.myteam.game.model.phom.player.PhomPlayer;
import com.myteam.game.model.phom.player.PhomBotPlayer;
import java.util.ArrayList;
import java.util.List;
import com.myteam.game.model.core.deck.Deck;
import com.myteam.game.model.phom.gamestate.PhomGameState;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PhomGameLogic extends Game<StandardCard, PhomPlayer> {
    private PhomGameState currentGameState;
    private StandardCard cardsOnTable;
    private PhomPlayer winnerPlayer;
    private List<List<StandardCard>> MeldedCards;

    public PhomGameLogic() {
        this.MeldedCards = new ArrayList<>();
        winnerPlayer = null;
        cardsOnTable = null;
    }

    public PhomGameLogic(Deck<StandardCard, PhomPlayer> deck, List<PhomPlayer> players, int numberOfCards) {
        super(deck, players, numberOfCards);
        cardsOnTable = null;
        this.MeldedCards = new ArrayList<>();
        this.winnerPlayer = null;
    }

    @Override
    public void startGame() {
        try {
            if (deck == null) {
                throw new IllegalStateException("Lỗi: Bộ bài (deck) chưa được khởi tạo!");
            }
            deck.shuffle();
            deck.dealCards(players, numberOfCards);
            players.getFirst().receiveCard(deck.drawCard());
            currentPlayer = getFirstPlayer(players);
            if (players == null || players.isEmpty()) {
                throw new IllegalStateException("Lỗi: Không có người chơi nào!");
            }
            if (numberOfCards <= 0) {
                throw new IllegalArgumentException("Lỗi: Số lá bài chia không hợp lệ!");
            }
            System.out.println("Game started! Dealing... " + numberOfCards + " cards to " + players.size() + " players.");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public PhomPlayer getFirstPlayer(List<PhomPlayer> players) {
        return players.getFirst();
    }

    public void botDiscardCard() {
        try {
            PhomBotPlayer botPlayer = (PhomBotPlayer) currentPlayer;
            StandardCard cardRemove = botPlayer.decideDiscard();
            botPlayer.getHand().remove(cardRemove);
            botPlayer.addDiscardCards(cardRemove);
            cardsOnTable = cardRemove;
            currentPlayer.setNumOfTurn(currentPlayer.getNumOfTurn() + 1);
        } catch (Exception e) {
            System.err.println("botDiscardCard error: " + e.getMessage());
        }
    }

    public void humanDiscardCard(StandardCard card) {
        try {
            currentPlayer.addDiscardCards(card);
            currentPlayer.getHand().remove(card);
            cardsOnTable = card;
            currentPlayer.setNumOfTurn(currentPlayer.getNumOfTurn() + 1);
        } catch (Exception e) {
            System.err.println("humanDiscardCard error: " + e.getMessage());
        }
    }

    public void playerDrawCard() {
        try {
            if (!deck.isEmpty()) {
                StandardCard card = deck.drawCard();
                currentPlayer.receiveCard(card);
            }
        } catch (Exception e) {
            System.err.println("playerDrawCard error: " + e.getMessage());
        }
    }

    public void playerEatCard(StandardCard cardToEatArgument) {
        try {
            if (currentPlayer == null || cardToEatArgument == null || this.cardsOnTable == null
                    || !this.cardsOnTable.equals(cardToEatArgument)) {
                throw new IllegalStateException("GameLogic Error: Cannot execute playerEatCard. Conditions not met. CurrentPlayer: " + currentPlayer
                        + ", cardToEat: " + cardToEatArgument + ", current cardsOnTable: " + this.cardsOnTable);
            }

            System.out.println("GameLogic: " + currentPlayer.getName() + " is eating " + cardToEatArgument);

            currentPlayer.getEatenCards().add(cardToEatArgument);

            this.cardsOnTable = null;

            PhomPlayer previousPlayer = getPlayers()
                    .get((getPlayers().indexOf(currentPlayer) - 1 + getPlayers().size()) % getPlayers().size());
            previousPlayer.getDiscardCards().remove(cardToEatArgument);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public boolean isValidMove(List<StandardCard> cards) {
        return cards.size() == 1;
    }

    @Override
    public boolean endGame() {
        if (deck.isEmpty()) {
            return true;
        }
        for (PhomPlayer player : players) {
            if (player.calculateScore() == 0) {
                winnerPlayer = player;
                return true;
            }
        }
        int cnt = 0;
        for (PhomPlayer player : players) {
            if (player.getNumOfTurn() == 4) {
                cnt++;
            }
        }
        if (cnt == players.size()) {
            return true;
        }

        return false;
    }

    public void playerMeldCard() {
        try {
            for (List<StandardCard> meld : currentPlayer.findCombinations()) {
                currentPlayer.getHand().removeAll(meld);
                currentPlayer.getAllPhoms().add(meld);
                this.MeldedCards.add(meld);
            }
        } catch (Exception e) {
            System.err.println("playerMeldCard error: " + e.getMessage());
        }
    }

    @Override
    public void nextTurn() {
        try {
            currentPlayer = getCurrentPlayer();
            currentPlayer = getPlayers().get((getPlayers().indexOf(currentPlayer) + 1) % getPlayers().size());
        } catch (Exception e) {
            System.err.println("nextTurn error: " + e.getMessage());
        }
    }

    public boolean canFormPhom(PhomPlayer player, StandardCard card) {
        try {
            List<StandardCard> originalHand = new ArrayList<>(player.getHand());
            List<StandardCard> originalEatenCards = new ArrayList<>(player.getEatenCards());

            player.receiveCard(card);
            List<List<StandardCard>> newPhoms = player.findCombinations();

            boolean isValid = true;
            Map<StandardCard, Integer> cardUsageMap = new HashMap<>();

            for (List<StandardCard> phom : newPhoms) {
                for (StandardCard c : phom) {
                    cardUsageMap.put(c, cardUsageMap.getOrDefault(c, 0) + 1);
                    if (cardUsageMap.get(c) >= 2) {
                        isValid = false;
                        break;
                    }
                }
                if (!isValid) break;
            }

            player.getHand().clear();
            player.getHand().addAll(originalHand);
            player.getEatenCards().clear();
            player.getEatenCards().addAll(originalEatenCards);

            return isValid
                    && cardUsageMap.getOrDefault(card, 0) == 1;
        } catch (Exception e) {
            System.err.println("canFormPhom error: " + e.getMessage());
            return false;
        }
    }

    public boolean isValidCombination(List<StandardCard> cards) {
        try {
            if (cards.getFirst().getSuit() != cards.getLast().getSuit()) {
                int tmp = cards.getFirst().getRank().getValue();
                for (StandardCard card : cards) {
                    if (card.getRank().getValue() != tmp) {
                        return false;
                    }
                }
                return true;
            } else {
                for (int i = 0; i < cards.size() - 1; i++) {
                    if (cards.get(i).getRank().getValue() + 1 != cards.get(i + 1).getRank().getValue()) {
                        return false;
                    }
                }
                return true;
            }
        } catch (Exception e) {
            System.err.println("isValidCombination error: " + e.getMessage());
            return false;
        }
    }

    public void determineWinnerByScore() {
        try {
            int minScore = Integer.MAX_VALUE;
            PhomPlayer potentialWinner = null;
            List<PhomPlayer> winners = new ArrayList<>();

            System.out.println("Điểm số cuối cùng:");
            for (PhomPlayer player : players) {
                int score = 0;
                for (StandardCard card : player.getHand()) {
                    score += card.getRank().getValue();
                }
                System.out.println("- " + player.getName() + ": " + score + " điểm");
                if (score < minScore) {
                    minScore = score;
                    winners.clear();
                    winners.add(player);
                    potentialWinner = player;
                } else if (score == minScore) {
                    winners.add(player);
                }
            }

            if (winners.size() == 1) {
                this.winnerPlayer = potentialWinner;
            } else {
                for (PhomPlayer winner : winners) {
                    System.out.print(winner.getName() + " ");
                }
                System.out.println();
                this.winnerPlayer = winners.get(0);
                System.out.println("Trò chơi kết thúc với nhiều người chơi có điểm số bằng nhau. Người chiến thắng được chọn là: "
                        + this.winnerPlayer.getName());
            }
        } catch (Exception e) {
            System.err.println("determineWinnerByScore error: " + e.getMessage());
        }
    }

    public PhomPlayer getWinnerPlayer() {
        return winnerPlayer;
    }

    public PhomGameState getCurrentGameState() {
        try {
            List<PhomPlayer> currentPlayers = Collections.unmodifiableList(new ArrayList<>(this.players));
            PhomPlayer activePlayer = this.currentPlayer;
            List<List<StandardCard>> currentMeldedCards = Collections.unmodifiableList(new ArrayList<>(MeldedCards));
            return new PhomGameState(
                    currentPlayers,
                    activePlayer,
                    currentMeldedCards,
                    endGame(),
                    winnerPlayer,
                    cardsOnTable);
        } catch (Exception e) {
            System.err.println("getCurrentGameState error: " + e.getMessage());
            return null;
        }
    }

    public StandardCard getCardsOnTable() {
        return cardsOnTable;
    }
}
