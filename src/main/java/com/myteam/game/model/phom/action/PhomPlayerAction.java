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
            try {
                if (card == null) {
                    throw new IllegalArgumentException("Card cannot be null");
                }
                this.card = card;
            } catch (Exception e) {
                throw new RuntimeException("Error initializing EatCardAction", e);
            }
        }

        public StandardCard getCard() {
            try {
                return card;
            } catch (Exception e) {
                throw new RuntimeException("Error retrieving card from EatCardAction", e);
            }
        }
    }

    public static class DiscardCardAction {
        private final StandardCard card;

        public DiscardCardAction(StandardCard card) {
            try {
                if (card == null) {
                    throw new IllegalArgumentException("Card cannot be null");
                }
                this.card = card;
            } catch (Exception e) {
                throw new RuntimeException("Error initializing DiscardCardAction", e);
            }
        }

        public StandardCard getCard() {
            try {
                return card;
            } catch (Exception e) {
                throw new RuntimeException("Error retrieving card from DiscardCardAction", e);
            }
        }
    }
}
