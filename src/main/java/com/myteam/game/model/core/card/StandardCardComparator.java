package com.myteam.game.model.core.card;

import java.util.Comparator;
import java.util.Map;

public class StandardCardComparator implements Comparator<StandardCard> {
    private static final Map<String, Integer> suitOrder = Map.of(
            "♠", 0,
            "♣", 1,
            "♦", 2,
            "♥", 3
    );
    @Override
    public int compare(StandardCard c1, StandardCard c2) {
        if (c1.getRank().ordinal() > c2.getRank().ordinal()){
            return 1;
        }
        else if (c1.getRank().ordinal() < c2.getRank().ordinal()){
            return -1;
        }
        else{
            return suitOrder.get(c1.getSuit().getValue()).compareTo(suitOrder.get(c2.getSuit().getValue()));
        }
    }
}
