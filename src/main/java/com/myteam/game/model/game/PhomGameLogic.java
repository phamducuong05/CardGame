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
    private List<List<StandardCard>> MeldedCards; // Phom đã hạ

    public PhomGameLogic() {
        this.MeldedCards = new ArrayList<>();
        winnerPlayer = null;
        cardsOnTable = null;
    }

    public PhomGameLogic(Deck<StandardCard, PhomPlayer> deck, List<PhomPlayer> players, int numberOfCards) {
        super(deck, players, numberOfCards);
        cardsOnTable = null;
        this.MeldedCards = new ArrayList<>(); // KHỞI TẠO MeldedCards Ở ĐÂY!
        this.winnerPlayer = null;
    }

    @Override
    public void startGame() {
        if (deck == null) {
            System.err.println("Lỗi: Bộ bài (deck) chưa được khởi tạo!");
            return;
        }
        deck.shuffle();
        deck.dealCards(players, numberOfCards);
        players.getFirst().receiveCard(deck.drawCard());
        currentPlayer = getFirstPlayer(players);
        if (players == null || players.isEmpty()) {
            System.err.println("Lỗi: Không có người chơi nào!");
            return;
        }
        if (numberOfCards <= 0) {
            System.err.println("Lỗi: Số lá bài chia không hợp lệ!");
            return;
        }
        System.out.println("Game started! Dealing... " + numberOfCards + " cards to " + players.size() + " players.");
    }

    @Override
    public PhomPlayer getFirstPlayer(List<PhomPlayer> players) {
        return players.getFirst();
    }

    public void botDiscardCard() {
        PhomBotPlayer botPlayer = (PhomBotPlayer) currentPlayer;
        StandardCard cardRemove = botPlayer.decideDiscard();
        botPlayer.getHand().remove(cardRemove);
        botPlayer.addDiscardCards(cardRemove);
        cardsOnTable = cardRemove;
        currentPlayer.setNumOfTurn(currentPlayer.getNumOfTurn() + 1);
    }


    public void humanDiscardCard(StandardCard card) {
        currentPlayer.addDiscardCards(card);
        currentPlayer.getHand().remove(card);
        cardsOnTable = card;
        currentPlayer.setNumOfTurn(currentPlayer.getNumOfTurn() + 1);
    }

    public void playerDrawCard() {
        if (!deck.isEmpty()) {
            StandardCard card = deck.drawCard();
            currentPlayer.receiveCard(card);
        }
    }

    public void playerEatCard(StandardCard cardToEatArgument) {
        if (currentPlayer == null || cardToEatArgument == null || this.cardsOnTable == null
                || !this.cardsOnTable.equals(cardToEatArgument)) {
            System.err.println(
                    "GameLogic Error: Cannot execute playerEatCard. Conditions not met. CurrentPlayer: " + currentPlayer
                            + ", cardToEat: " + cardToEatArgument + ", current cardsOnTable: " + this.cardsOnTable);
            return;
        }

        System.out.println("GameLogic: " + currentPlayer.getName() + " is eating " + cardToEatArgument);

        currentPlayer.getEatenCards().add(cardToEatArgument);

        this.cardsOnTable = null;

        PhomPlayer previousPlayer = getPlayers()
                .get((getPlayers().indexOf(currentPlayer) - 1 + getPlayers().size()) % getPlayers().size());
        previousPlayer.getDiscardCards().remove(cardToEatArgument);
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
        for (List<StandardCard> meld : currentPlayer.findCombinations()) {
            currentPlayer.getHand().removeAll(meld);
            currentPlayer.getAllPhoms().add(meld);
            this.MeldedCards.add(meld);
        }
    }

    @Override
    public void nextTurn() {
        currentPlayer = getCurrentPlayer();
        currentPlayer = getPlayers().get((getPlayers().indexOf(currentPlayer) + 1) % getPlayers().size());
    }

    public boolean canFormPhom(PhomPlayer player, StandardCard card) {
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
    }

    public boolean isValidCombination(List<StandardCard> cards) {
        if (cards.getFirst().getSuit() != cards.getLast().getSuit()) {
            int tmp = cards.getFirst().getRank().getValue();
            for (StandardCard card : cards) {
                if (card.getRank().getValue() != tmp) {
                    return false;
                }
            }
            return true;
        }

        else {
            for (int i = 0; i < cards.size() - 1; i++) {
                if (cards.get(i).getRank().getValue() + 1 != cards.get(i + 1).getRank().getValue()) {
                    return false;
                }
            }
            return true;
        }
    }

    public void determineWinnerByScore() {
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
            this.winnerPlayer = winners.get(0); // Chọn người chơi đầu tiên trong danh sách
            System.out.println("Trò chơi kết thúc với nhiều người chơi có điểm số bằng nhau. Người chiến thắng được chọn là: "
                    + this.winnerPlayer.getName());
        }
    }

    public PhomPlayer getWinnerPlayer() {
        return winnerPlayer;
    }

    public PhomGameState getCurrentGameState() {
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
    }

    public StandardCard getCardsOnTable() {
        return cardsOnTable;
    }


}
