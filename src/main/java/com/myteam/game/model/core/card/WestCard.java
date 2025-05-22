package com.myteam.game.model.core.card;

import com.myteam.game.model.core.enums.Rank;
import com.myteam.game.model.core.enums.Suit;
import java.util.Objects;

public class WestCard extends Card {
    private final Suit suit;
    private final Rank rank;

    public WestCard(Suit suit, Rank rank) {
        super();
        this.suit = suit;
        this.rank = rank;
        this.setName(rank.name() + " of " + suit.name());
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WestCard westCard = (WestCard) o;
        return rank == westCard.rank && suit == westCard.suit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, suit);
    }

    @Override
    public String toString() {
        return rank.name() + " of " + suit.name();
    }

}
