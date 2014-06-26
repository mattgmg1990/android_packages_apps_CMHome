package org.cyanogenmod.launcher.cardprovider;

import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;

import org.cyanogenmod.launcher.cards.DashClockExtensionCard;
import org.cyanogenmod.launcher.dashclock.ExtensionHost;
import org.cyanogenmod.launcher.dashclock.ExtensionManager;

import java.util.ArrayList;
import java.util.List;

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
    public void onExtensionsChanged(ComponentName sourceExtension) {
        mExtensionManager.cleanupExtensions();
    }

    /*
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
}
