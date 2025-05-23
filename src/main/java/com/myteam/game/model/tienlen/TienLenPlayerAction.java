package com.myteam.game.model.tienlen;

import com.myteam.game.model.core.card.WestCard;

import java.util.List;

/**
 * Represents different actions of players in Tien Len Mien Bac game
 */
public class TienLenPlayerAction {
    /**
     * Action to play a card
     */
    public static class PlayCardAction {
        private List<WestCard> selectedCards;

        public PlayCardAction(List<WestCard> selectedCards) {
            this.selectedCards = selectedCards;
        }

        public List<WestCard> getSelectedCards() {
            return selectedCards;
        }
    }

    public static class SkipAction {

    }
}
