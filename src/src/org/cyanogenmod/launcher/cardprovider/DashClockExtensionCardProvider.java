package org.cyanogenmod.launcher.cardprovider;

import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;

import org.cyanogenmod.launcher.cards.DashClockExtensionCard;
import org.cyanogenmod.launcher.dashclock.ExtensionHost;
import org.cyanogenmod.launcher.dashclock.ExtensionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * Manages fetching data from all installed DashClock extensions
 * and generates cards to be displayed.
 */
public class DashClockExtensionCardProvider implements ICardProvider, ExtensionManager.OnChangeListener {
    public static final String TAG = "DashClockExtensionCardProvider";

    private ExtensionManager mExtensionManager;
    private ExtensionHost mExtensionHost;
    private Context mContext;
    private List<CardProviderUpdateListener> mUpdateListeners = new ArrayList<CardProviderUpdateListener>();

    public DashClockExtensionCardProvider(Context context) {
        mContext = context;
        mExtensionManager = ExtensionManager.getInstance(context);
        mExtensionManager.addOnChangeListener(this);
        mExtensionHost = new ExtensionHost(context);

        trackAllExtensions();
    }

    @Override
    public void onStart(Context context) {

    }

    @Override
    public void onDestroy(Context context) {
        mExtensionManager.removeOnChangeListener(this);
        mExtensionHost.destroy();
    }

    @Override
    public List<Card> getCards() {
        List<Card> cards = new ArrayList<Card>();

        for(ExtensionManager.ExtensionWithData extensionWithData : mExtensionManager.getActiveExtensionsWithData()) {
            if(extensionWithData.latestData != null && !TextUtils.isEmpty(extensionWithData.latestData.status())) {
                Card card = new DashClockExtensionCard(mContext, extensionWithData);
                cards.add(card);
            }
        }

        return cards;
    }

    @Override
    public void requestRefresh() {
        // nothing
    }

    @Override
    public List<Card> updateAndAddCards(List<Card> cards) {
        List<ExtensionManager.ExtensionWithData> extensions
                = mExtensionManager.getActiveExtensionsWithData();

        // Create a map from ComponentName String -> extensionWithData
        HashMap<String, ExtensionManager.ExtensionWithData> map
                = new HashMap<String, ExtensionManager.ExtensionWithData>();
        for(ExtensionManager.ExtensionWithData extension : extensions) {
            map.put(extension.listing.componentName.flattenToString(), extension);
        }

        for(Card card : cards) {
            if(card instanceof DashClockExtensionCard) {
                DashClockExtensionCard dashClockExtensionCard
                        = (DashClockExtensionCard) card;
                if(map.containsKey(dashClockExtensionCard
                        .getFlattenedComponentNameString())) {
                    dashClockExtensionCard
                            .updateFromExtensionWithData(map.get(dashClockExtensionCard
                            .getFlattenedComponentNameString()));
                    map.remove(dashClockExtensionCard.getFlattenedComponentNameString());
                }
            }
        }

        // A List of cards to return that must be added
        List<Card> cardsToAdd = new ArrayList<Card>();

        // Create new cards for extensions that were not represented
        for(Map.Entry<String, ExtensionManager.ExtensionWithData> entry : map.entrySet()) {
            ExtensionManager.ExtensionWithData extension = entry.getValue();

            if(extension.latestData != null && !TextUtils.isEmpty(extension.latestData.status())) {
                Card card = new DashClockExtensionCard(mContext, extension);
                cardsToAdd.add(card);
            }
        }

        return cardsToAdd;
    }

    @Override
    public void onExtensionsChanged(ComponentName sourceExtension) {
        for(CardProviderUpdateListener listener : mUpdateListeners) {
            listener.onCardProviderUpdate();
        }
    }

    /**
     * Retrieves a list of all available extensions installed on the device
     * and sets mExtensionManager to track them for updates.
     */
    private void trackAllExtensions() {
        List<ComponentName> availableComponents = new ArrayList<ComponentName>();
        for(ExtensionManager.ExtensionListing listing : mExtensionManager.getAvailableExtensions()) {
           availableComponents.add(listing.componentName);
        }
        mExtensionManager.setActiveExtensions(availableComponents);
    }

    /**
     * Adds a listener for any extension updates.
     * @param listener The listener to update
     */
    @Override
    public void addOnUpdateListener(CardProviderUpdateListener listener) {
        mUpdateListeners.add(listener);
    }
}
