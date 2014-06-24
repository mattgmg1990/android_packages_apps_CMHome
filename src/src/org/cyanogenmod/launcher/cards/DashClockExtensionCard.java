package org.cyanogenmod.launcher.cards;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
                    Intent clickIntent = mExtensionWithData.latestData.clickIntent();
                    clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(clickIntent);
                }
            }
        });

        setOnLongClickListener(new OnLongCardClickListener() {
            @Override
            public boolean onLongClick(Card card, View view) {
                if(mExtensionWithData.listing.settingsActivity != null) {
                    Intent settingsIntent = new Intent();
                    settingsIntent.setComponent(mExtensionWithData.listing.settingsActivity);
                    settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(settingsIntent);
                }
                return false;
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
            CardThumbnail thumbnail = new DashClockThumbnail(mContext);
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

    private static class DashClockThumbnail extends CardThumbnail {
        private final static String[] ICON_BACKGROUND_COLORS = {
                                "#F9A43E", // orange
                                "#AD62A7", // purple
                                "#E4C62E", // yellow
                                "#F16364", // pink-ish
                                "#2093CD"  // blue
        };

        private static int sCurrentIconColorIndex = 0;
        private int mIconColorIndex = -1;

        public DashClockThumbnail(Context context) {
            super(context);
            // Assign this card a color, incrementing the static ongoing color index
            if(mIconColorIndex == -1) {
                mIconColorIndex = sCurrentIconColorIndex++ % ICON_BACKGROUND_COLORS.length;
            }
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View viewImage) {
            ImageView image= (ImageView) viewImage;

            // Pick the next background color for the icon.
            // Choose the color in the order they appear in ICON_BACKGROUND_COLORS.
            String colorString = ICON_BACKGROUND_COLORS[mIconColorIndex];
            image.setBackgroundColor(Color.parseColor(colorString));
        }
    }

    private static class DashClockIconCardThumbnailSource implements CardThumbnail.CustomSource {
        private final static float[] WHITE_COLOR_MATRIX = new float[] {
                                1f, 1f, 1f, 0, 0,
                                1f, 1f, 1f, 0, 0,
                                1f, 1f, 1f, 0, 0,
                                0, 0, 0, 1f, 0
        };

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
            Bitmap bitmapToReturn = null;
            // As per the DashClock documentation, prefer the iconUri resource.
            if(mExtensionData.iconUri() != null) {
                bitmapToReturn = getBitmapFromUri(mExtensionData.iconUri());
            } else {
                bitmapToReturn = getIconFromResId(mExtensionData.icon());
            }
            // Return an all white (leaving alpha alone) version of the icon.
            return applyWhiteColorFilter(bitmapToReturn);
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

        /**
         * The DashClock extension docs say that icons should be all white
         * with a transparent background, but I have found that many do not
         * respect this. This method corrects that by changing any non-transparent pixel to
         * white, leaving alpha values alone.
         * @param bitmap The input bitmap to color.
         * @return A copy of the original bitmap, colored to white.
         */
        private Bitmap applyWhiteColorFilter(Bitmap bitmap) {
            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Paint paint = new Paint();
            ColorMatrixColorFilter matrixColorFilter = new ColorMatrixColorFilter(WHITE_COLOR_MATRIX);
            paint.setColorFilter(matrixColorFilter);
            Canvas canvas = new Canvas(mutableBitmap);
            canvas.drawBitmap(mutableBitmap, 0, 0, paint);
            return mutableBitmap;
        }
    }
}
