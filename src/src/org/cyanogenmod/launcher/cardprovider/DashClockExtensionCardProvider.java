package org.cyanogenmod.launcher.cardprovider;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.apps.dashclock.api.ExtensionData;

import org.cyanogenmod.launcher.cards.DashClockExtensionCard;
import org.cyanogenmod.launcher.dashclock.ExtensionHost;
import org.cyanogenmod.launcher.dashclock.ExtensionManager;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardThumbnail;

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
