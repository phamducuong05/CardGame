package model.tienlen;

import model.core.card.WestCard;
import model.core.deck.WestCardDeck;
import model.core.enums.Rank;
import model.core.enums.Suit;
import model.game.TienLenMienBacGameLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestTienLenBot {
    public static void main(String[] args) {
        TienLenBotPlayer bot = new TienLenBotPlayer("Bot 1", new TienLenMienBacGameLogic());
        WestCardDeck deck = new WestCardDeck();
        for (int i = 0; i < 10; i++){
            WestCard card = deck.drawCard();
            bot.receiveCard(card);
        }
        bot.sortHand();
        List<WestCard> hand = bot.getHand();
        System.out.println("Cards in " + bot.getName() + " hand:");
        for (int i = 0; i < hand.size(); i++){
            System.out.println(hand.get(i));
        }
        // Current table cards
        WestCard card1 = new WestCard(Suit.CLUBS, Rank.FOUR);
        WestCard card2 = new WestCard(Suit.SPADES, Rank.FOUR);
        List<WestCard> currentTable = new ArrayList<WestCard>(Arrays.asList(card2));
        bot.autoPlay(currentTable);
    }
}
