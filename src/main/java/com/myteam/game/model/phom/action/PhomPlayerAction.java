package com.myteam.game.model.phom.action;

import com.myteam.game.model.core.card.StandardCard;
import com.myteam.game.model.phom.player.PhomPlayer;

import java.util.List;


public class PhomPlayerAction {

    public static class DrawCardAction {

    }

    public static class EatCardAction {
        private final StandardCard card;

        public EatCardAction(StandardCard card) {
            this.card = card;
        }

        public StandardCard getCard() {
            return card;
        }
    }

    public static class DiscardCardAction {
        private final StandardCard card;

        public DiscardCardAction(StandardCard card) {
            this.card = card;
        }

        public StandardCard getCard() {
            return card;
        }
    }



}