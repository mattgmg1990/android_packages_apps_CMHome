/*
 * Copyright (C) 2014 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cyanogenmod.launcher.home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.android.launcher.home.Home;

import org.cyanogenmod.launcher.cardprovider.DashClockExtensionCardProvider;
import org.cyanogenmod.launcher.cardprovider.ICardProvider;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

public class HomeStub implements Home {

    private static final String TAG = "HomeStub";
    private HomeLayout mHomeLayout;
    private boolean mShowContent = false;
    private List<ICardProvider> mCardProviders = new ArrayList<ICardProvider>();
    private CMHomeCardArrayAdapter mCardArrayAdapter;

    private final AccelerateInterpolator mAlphaInterpolator;

    private final ICardProvider.CardProviderUpdateListener mCardProviderUpdateListener =
            new ICardProvider.CardProviderUpdateListener() {
                @Override
                public void onCardProviderUpdate() {
                    refreshCards();
                }
            };

    public HomeStub() {
        super();
        mAlphaInterpolator = new AccelerateInterpolator();
    }

    @Override
    public void onStart(Context context) {
        if(mShowContent) {
            // Add any providers we wish to include, if we should show content
            initProvidersIfNeeded(context);
        }
    }

    @Override
    public void setShowContent(Context context, boolean showContent) {
        mShowContent = showContent;
        if(mShowContent) {
            // Add any providers we wish to include, if we should show content
            initProvidersIfNeeded(context);
            if(mHomeLayout != null) {
                loadCardsFromProviders(context);
            }
        } else {
            for(ICardProvider cardProvider : mCardProviders) {
                cardProvider.onDestroy(context);
            }
            mCardProviders.clear();
            if(mHomeLayout != null) {
                removeAllCards(context);
            }
        }
    }

    @Override
    public void onDestroy(Context context) {
        mHomeLayout = null;
        for(ICardProvider cardProvider : mCardProviders) {
            cardProvider.onDestroy(context);
        }
    }

    @Override
    public void onResume(Context context) {
    }

    @Override
    public void onPause(Context context) {
    }

    @Override
    public void onShow(Context context) {
        if (mHomeLayout != null) {
            mHomeLayout.setAlpha(1.0f);

            if(mShowContent) {
                loadCardsFromProviders(context);
            }
        }
    }

    @Override
    public void onScrollProgressChanged(Context context, float progress) {
        if (mHomeLayout != null) {
            mHomeLayout.setAlpha(mAlphaInterpolator.getInterpolation(progress));
        }
    }

    @Override
    public void onHide(Context context) {
        if (mHomeLayout != null) {
            mHomeLayout.setAlpha(0.0f);
        }

        for(ICardProvider cardProvider : mCardProviders) {
            cardProvider.onDestroy(context);
        }
    }

    @Override
    public void onInvalidate(Context context) {
        if (mHomeLayout != null) {
            mHomeLayout.removeAllViews();
        }
    }

    @Override
    public void onRequestSearch(Context context, int mode) {
        
    }

    @Override
    public View createCustomView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mHomeLayout = (HomeLayout)inflater.inflate(R.layout.home_layout, null);

        return mHomeLayout;
    }

    @Override
    public String getName(Context context) {
        return "HomeStub";
    }

    @Override
    public int getNotificationFlags() {
        return Home.FLAG_NOTIFY_ALL;
    }

    @Override
    public int getOperationFlags() {
        return Home.FLAG_OP_MASK;
    }

    public void initProvidersIfNeeded(Context context) {
        if (mCardProviders.size() == 0) {
            mCardProviders.add(new DashClockExtensionCardProvider(context));

            for (ICardProvider cardProvider : mCardProviders) {
                cardProvider.addOnUpdateListener(mCardProviderUpdateListener);
            }
        }
    }

    /*
     * Gets a list of all cards provided by each provider,
     * and updates the UI to show them.
     */
    private void loadCardsFromProviders(Context context) {
        // If cards have been initialized already, just update them
        if(mCardArrayAdapter != null
           && mCardArrayAdapter.getCards().size() > 0
           && mHomeLayout != null) {
            refreshCards();
            return;
        }

        List<Card> cards = new ArrayList<Card>();
        for(ICardProvider provider : mCardProviders) {
           for(Card card : provider.getCards()) {
               cards.add(card);
           }
        }

        CardListView cardListView = (CardListView) mHomeLayout.findViewById(R.id.cm_home_cards_list);

        if(cardListView != null) {
            mCardArrayAdapter = new CMHomeCardArrayAdapter(context, cards);
            cardListView.setAdapter(mCardArrayAdapter);
        }
    }

    public void refreshCards() {
        List<Card> originalCards = mCardArrayAdapter.getCards();

        // Allow each provider to update it's cards
        for(ICardProvider cardProvider : mCardProviders) {
            List<Card> cardsToAdd = cardProvider.updateAndAddCards(originalCards);
            mCardArrayAdapter.addAll(cardsToAdd);
        }

        mCardArrayAdapter.notifyDataSetChanged();
    }

    private void removeAllCards(Context context) {
        CardListView cardListView = (CardListView) mHomeLayout.findViewById(R.id.cm_home_cards_list);
        // Set CardArrayAdapter to an adapter with an empty list
        List<Card> cards = new ArrayList<Card>();
        mCardArrayAdapter = new CMHomeCardArrayAdapter(context, cards);
        cardListView.setAdapter(mCardArrayAdapter);
    }

    public class CMHomeCardArrayAdapter extends CardArrayAdapter {

        public CMHomeCardArrayAdapter(Context context, List<Card> cards) {
            super(context, cards);
        }

        public List<Card> getCards() {
            List<Card> cardsToReturn = new ArrayList<Card>();
            for(int i = 0; i < getCount(); i++) {
                cardsToReturn.add(getItem(i));
            }
            return cardsToReturn;
        }
    }
}
