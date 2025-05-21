package com.myteam.game;

import com.myteam.game.model.core.deck.WestCardDeck;
import com.myteam.game.model.game.TienLenMienBacGameLogic;
import com.myteam.game.model.tienlen.TienLenBotPlayer;
import com.myteam.game.model.tienlen.TienLenPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class TestTienLenGame {
    public static void main(String[] args) throws InterruptedException {
        // Tạo danh sách người chơi
        TienLenBotPlayer bot1 = new TienLenBotPlayer("CuongDzVoDich");
        TienLenBotPlayer bot2 = new TienLenBotPlayer("Cuong Lun");
        TienLenBotPlayer bot3 = new TienLenBotPlayer("Vanh");
        TienLenBotPlayer bot4 = new TienLenBotPlayer("Lom dom");

        List<TienLenPlayer> players = new ArrayList<>(Arrays.asList(bot1, bot2, bot3, bot4));

        // Khởi tạo logic cho game
        WestCardDeck<TienLenPlayer> deck = new WestCardDeck<>();

        TienLenMienBacGameLogic gameLogic = new TienLenMienBacGameLogic(deck, players, 13);

        gameLogic.startGame();

        // In ra hand của từng người chơi
        for (TienLenPlayer player : players) {
            System.out.println("Player " + player.getName() + " has: " + player.getHand());
        }

//        WestCardComparator comparator = new WestCardComparator();
//        System.out.println(comparator.compare(new WestCard(Suit.DIAMONDS, Rank.TWO), new WestCard(Suit.HEARTS, Rank.THREE)));

        // Người chơi đầu tiên
        System.out.println("First player to play: " + gameLogic.getCurrentPlayer().getName());

        // Vòng lặp chơi game
        while (gameLogic.endGame()) {
            //gameLogic.playTurn();
            //gameLogic.resetTurn();
            Thread.sleep(0);
        }

        // In ra thứ hạng của người chơi
        players.sort(Comparator.comparingInt(TienLenPlayer::handSize));
        for (int i = 1; i <= players.size(); i++) {
            System.out.println("Rank " + i + ": " + players.get(i - 1).getName() + " | " + "Cards left: " + players.get(i - 1).getHand());
        }
    }
}
