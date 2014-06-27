package org.cyanogenmod.launcher.cardprovider;

import android.content.Context;

import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * An interface for classes that can manage data for and provide Cards to be displayed.
 */
public interface ICardProvider {
    public void onStart(Context context);
    public void onDestroy(Context context);
    public void requestRefresh();

    /**
     * Given a list of cards, update any card for which
     * there is new data available. Add or remove cards
     * as required.
     * @param cards Cards to update
     */
    public void updateAndAddCards(List<Card> cards);
    public List<Card> getCards();
}
