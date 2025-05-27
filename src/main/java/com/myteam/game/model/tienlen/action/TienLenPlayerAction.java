package com.myteam.game.model.tienlen.action;

import com.myteam.game.model.core.card.StandardCard;

import java.util.List;

/**
 * Represents different actions of players in Tien Len Mien Bac game
 */
public class TienLenPlayerAction {
    /**
     * Action to play a card
     */
    public static class PlayCardAction {
        private List<StandardCard> selectedCards;

        public PlayCardAction(List<StandardCard> selectedCards) {
            this.selectedCards = selectedCards;
        }

        public List<StandardCard> getSelectedCards() {
            return selectedCards;
        }
    }

    public static class SkipAction {

    }
}
