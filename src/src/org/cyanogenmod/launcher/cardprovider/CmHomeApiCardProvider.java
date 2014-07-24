package org.cyanogenmod.launcher.cardprovider;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.cyanogenmod.launcher.home.api.CMHomeApiService;

import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;

public class CmHomeApiCardProvider implements ICardProvider {
    private CMHomeApiService mApiService;
    private Context mCmHomeContext;
    private Context mHostActivityContext;

    public CmHomeApiCardProvider(Context cmHomeContext, Context hostActivityContext) {
        mCmHomeContext = cmHomeContext;
        mHostActivityContext = hostActivityContext;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mApiService = ((CMHomeApiService.LocalBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mApiService = null;
        }
    };

    @Override
    public void onHide(Context context) {
        if(mApiService != null) {
            unbindService(mHostActivityContext);
        }
    }

    @Override
    public void onShow() {
        if (mApiService == null) {
            bindService(mHostActivityContext);
        }
    }

    private void bindService(Context context) {
        context.bindService(new Intent(context, CMHomeApiService.class), mConnection,
                                       Context.BIND_AUTO_CREATE);
    }

    private void unbindService(Context context) {
        context.unbindService(mConnection);
    }

    @Override
    public void requestRefresh() {

    }

    @Override
    public CardProviderUpdateResult updateAndAddCards(List<Card> cards) {
        return null;
    }

    @Override
    public void updateCard(Card card) {

    }

    @Override
    public Card createCardForId(String id) {
        return null;
    }

    @Override
    public List<Card> getCards() {
        return null;
    }

    @Override
    public void addOnUpdateListener(CardProviderUpdateListener listener) {

    }
}
