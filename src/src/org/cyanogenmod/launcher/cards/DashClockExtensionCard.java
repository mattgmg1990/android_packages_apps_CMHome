package org.cyanogenmod.launcher.cards;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.dashclock.api.ExtensionData;

import org.cyanogenmod.launcher.dashclock.ExtensionManager;
import org.cyanogenmod.launcher.home.R;

import java.io.FileNotFoundException;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;

/**
 * This class provides a card that will represent a DashClock Extension
 */
public class DashClockExtensionCard extends Card {
    private final static String TAG = "DashClockExtensionCard";
    private ExtensionManager.ExtensionWithData mExtensionWithData;

    public DashClockExtensionCard(Context context, ExtensionManager.ExtensionWithData extensionWithData) {
        this(context, extensionWithData, R.layout.dashclock_card_inner_content);
    }

    public DashClockExtensionCard(Context context, ExtensionManager.ExtensionWithData extensionWithData, int innerLayout) {
        super(context, innerLayout);
        mExtensionWithData = extensionWithData;
        init();
    }

    private void init() {
        //Add Header
        CardHeader header = new CardHeader(getContext());
        header.setButtonExpandVisible(true);
        header.setTitle(getHeaderTitleFromExtension());
        addCardHeader(header);
        header.setButtonExpandVisible(false);

        addCardIcon();

        //Add onClick Listener
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                if(mExtensionWithData.latestData.clickIntent() != null) {
                    mContext.startActivity(mExtensionWithData.latestData.clickIntent());
                }
            }
        });

        //Add swipe Listener
        setOnSwipeListener(new OnSwipeListener() {
            @Override
            public void onSwipe(Card card) {
                Toast.makeText(getContext(), "Card removed", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addCardIcon() {
        ExtensionData data = mExtensionWithData.latestData;
        if(data.iconUri() != null || data.icon() > 0) {
            CardThumbnail thumbnail = new CardThumbnail(mContext);
            thumbnail.setCustomSource(new DashClockIconCardThumbnailSource(mContext, mExtensionWithData.listing.componentName, data));
            addCardThumbnail(thumbnail);
        }
    }

    private String getHeaderTitleFromExtension() {
        ExtensionData data = mExtensionWithData.latestData;
        String title = "";

        if(!TextUtils.isEmpty(mExtensionWithData.listing.title)) {
            title = mExtensionWithData.listing.title;
        } else if(!TextUtils.isEmpty(data.expandedTitle())) {
            title = data.expandedTitle();
        } else if(!TextUtils.isEmpty(data.status())) {
            title = data.status();
        }
        return title;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        TextView titleTextView = (TextView) view.findViewById(R.id.dashclock_card_inner_title_text);
        TextView statusTextView = (TextView) view.findViewById(R.id.dashclock_card_inner_status_text);
        TextView bodyTextView = (TextView) view.findViewById(R.id.dashclock_card_inner_body_text);

        String title = mExtensionWithData.latestData.expandedTitle();
        String status = mExtensionWithData.latestData.status();
        String body = mExtensionWithData.latestData.expandedBody();

        if(TextUtils.isEmpty(title) && !TextUtils.isEmpty(status)) {
            titleTextView.setText(status);
            statusTextView.setVisibility(View.GONE);
        } else {
            titleTextView.setText(title);
            statusTextView.setText(status);
            statusTextView.setVisibility(View.VISIBLE);
        }

        bodyTextView.setText(body);
    }

   private class DashClockIconCardThumbnailSource implements CardThumbnail.CustomSource {
        Context mContext;
        ComponentName mComponentName;
        ExtensionData mExtensionData;

        public DashClockIconCardThumbnailSource(Context context,
                                                ComponentName componentName,
                                                ExtensionData extensionData) {
            mContext = context;
            mComponentName = componentName;
            mExtensionData = extensionData;
        }
        @Override
        public String getTag() {
            return mComponentName.flattenToShortString();
        }

        @Override
        public Bitmap getBitmap() {
            Log.d(TAG, "getBitmap called!");
           // As per the DashClock documentation, prefer the iconUri resource.
           if(mExtensionData.iconUri() != null) {
               return getBitmapFromUri(mExtensionData.iconUri());
           } else {
               return getIconFromResId(mExtensionData.icon());
           }
        }

        private Bitmap getIconFromResId(int resId) {
            String packageName = mComponentName.getPackageName();
            try {
                Context packageContext = mContext.createPackageContext(packageName, 0);
                Resources packagesRes = packageContext.getResources();
                Log.d(TAG, "Returning icon resource for " + packageName + ", " + resId);
                return BitmapFactory.decodeResource(packagesRes, resId);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "DashClock icon could not be loaded from package: " + packageName);
            }
            return null;
        }

        private Bitmap getBitmapFromUri(Uri uri) {
            ParcelFileDescriptor iconFd;
            Bitmap icon = null;
            try {
                iconFd = mContext.getContentResolver().openFileDescriptor(uri, "r");
                icon = BitmapFactory.decodeFileDescriptor(iconFd.getFileDescriptor());
            } catch (FileNotFoundException e) {
                Log.w(TAG, "DashClock icon could not be loaded: " + uri);
            }
            return icon;
        }
    }
}
