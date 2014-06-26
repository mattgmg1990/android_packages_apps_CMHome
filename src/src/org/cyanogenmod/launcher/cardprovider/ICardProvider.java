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
    public List<Card> getCards();
}
