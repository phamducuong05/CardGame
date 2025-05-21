package com.myteam.game.model.core.deck;

import com.myteam.game.model.core.card.WestCard;
import com.myteam.game.model.core.enums.Rank;
import com.myteam.game.model.core.enums.Suit;
import com.myteam.game.model.player.Player;

public class WestCardDeck<P extends Player<WestCard>> extends Deck<WestCard, P> {
    @Override
    protected void initializeDeck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                getDeck().add(new WestCard(suit, rank));
            }
        }
        shuffle();
    }

}
