package org.cyanogenmod.launcher.cardprovider;

import android.content.Context;

import org.cyanogenmod.launcher.cards.ApiCard;
import org.cyanogenmod.launcher.home.api.CMHomeApiManager;
import org.cyanogenmod.launcher.home.api.cards.DataCard;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

public class CmHomeApiCardProvider implements ICardProvider,
        CMHomeApiManager.ICMHomeApiUpdateListener {
    private CMHomeApiManager mApiManager;
    private Context mCmHomeContext;
    private Context mHostActivityContext;
    private List<CardProviderUpdateListener> mUpdateListeners = new ArrayList<CardProviderUpdateListener>();

    public CmHomeApiCardProvider(Context cmHomeContext, Context hostActivityContext) {
        mCmHomeContext = cmHomeContext;
        mHostActivityContext = hostActivityContext;

        if (mApiManager == null) {
            mApiManager = new CMHomeApiManager(mHostActivityContext);
            mApiManager.setApiUpdateListener(this);
        }
    }

    @Override
    public void onHide(Context context) {
    }

    @Override
    public void onShow() {
    }

    @Override
    public void requestRefresh() {
        for (DataCard dataCard : mApiManager.getAllDataCards()) {
            onCardInsertOrUpdate(dataCard.getGlobalId());
        }
    }

    private boolean cardExists(String globalId, List<Card> cards) {
        for (Card card : cards) {
            if (card instanceof ApiCard) {
                ApiCard apiCard = (ApiCard) card;
                if (apiCard.getId().equals(globalId)) {
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public CardProviderUpdateResult updateAndAddCards(List<Card> cards) {
        List<Card> cardsToAdd = new ArrayList<Card>();
        List<Card> cardsToRemove = new ArrayList<Card>();

        // Add
        for (DataCard dataCard : mApiManager.getAllDataCards()) {
            if (!cardExists(dataCard.getGlobalId(), cards)) {
                cardsToAdd.add(getCardFromDataCard(dataCard));
            }
        }

        // Update and remove
        for (Card card : cards) {
            if (card instanceof ApiCard) {
                ApiCard apiCard = (ApiCard) card;
                long cardId = Long.parseLong(card.getId());
                boolean cardExists = mApiManager.hasCard(apiCard.getApiAuthority(), cardId);
                if (!cardExists) {
                    cardsToRemove.add(card);
                } else {
                    DataCard dataCard = mApiManager.getCard(apiCard.getApiAuthority(), cardId);
                    apiCard.updateFromDataCard(dataCard);
                }
            }
        }

        return new CardProviderUpdateResult(cardsToAdd, cardsToRemove);
    }

    @Override
    public void updateCard(Card card) {
        if (card instanceof ApiCard) {
            ApiCard apiCard = (ApiCard) card;
            long cardId = apiCard.getDbId();
            DataCard dataCard = mApiManager.getCard(apiCard.getApiAuthority(), cardId);
            if (dataCard != null) {
                apiCard.updateFromDataCard(dataCard);
            }
        }
    }

    @Override
    public Card createCardForId(String id) {
        DataCard dataCard = mApiManager.getCardWithGlobalId(id);
        if (dataCard != null) {
            ApiCard apiCard = new ApiCard(mCmHomeContext);
            apiCard.updateFromDataCard(dataCard);
            return apiCard;
        }
        return null;
    }

    @Override
    public List<Card> getCards() {
        List<Card> listOfCards = new ArrayList<Card>();
        if (mApiManager == null) {
            return listOfCards;
        }
        List<DataCard> theCards = mApiManager.getAllDataCards();

        for (DataCard dataCard : theCards) {
            listOfCards.add(getCardFromDataCard(dataCard));
        }
        return listOfCards;
    }

    private Card getCardFromDataCard(DataCard dataCard) {
        ApiCard card = new ApiCard(mCmHomeContext);
        card.updateFromDataCard(dataCard);
        return card;
    }

    @Override
    public void addOnUpdateListener(CardProviderUpdateListener listener) {
        mUpdateListeners.add(listener);
    }

    @Override
    public void onCardInsertOrUpdate(String globalId) {
        for (CardProviderUpdateListener listener : mUpdateListeners) {
            listener.onCardProviderUpdate(globalId);
        }
    }

    @Override
    public void onCardDelete(String globalId) {
        for (CardProviderUpdateListener listener : mUpdateListeners) {
            listener.onCardDelete(globalId);
        }
    }
}
