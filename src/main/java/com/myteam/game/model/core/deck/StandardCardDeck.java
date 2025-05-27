package com.myteam.game.model.core.deck;

import com.myteam.game.model.core.card.StandardCard;
import com.myteam.game.model.core.enums.Rank;
import com.myteam.game.model.core.enums.Suit;
import com.myteam.game.model.player.Player;

public class StandardCardDeck<P extends Player<StandardCard>> extends Deck<StandardCard, P> {
    @Override
    protected void initializeDeck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                getDeck().add(new StandardCard(suit, rank));
            }
        }
        shuffle();
    }

}
