package com.example.socketexample.socket;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class BindService extends Service {
    private static final int BUMP_MSG =1 ;
    private NotificationManager manager;
    private boolean mIsBound;

    public BindService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        doBinding();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private IRemoteService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = IRemoteService.Stub.asInterface(service);

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                mService.registerCallBack(mCallback);
                mService.startServer();
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }


        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BUMP_MSG:
                    Log.d(MainActivity.class.getName(), msg.getData().getString("RECEIVE") + "");
                    showNotification(msg.getData().getString("RECEIVE"));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }


    };


    private void showNotification(String receiveMessage) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("My notification")
                        .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
                        .setContentText("New Message Receive ");
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setSummaryText(receiveMessage);
        mBuilder.setStyle(bigTextStyle);
        manager.notify(0, mBuilder.build());

    }

    private IRemoteServiceCallBack mCallback = new IRemoteServiceCallBack.Stub() {
        /**
         * This is called by the remote service regularly to tell us about
         * new values.  Note that IPC calls are dispatched through a thread
         * pool running in each process, so the code executing here will
         * NOT be running in our main thread like most other things -- so,
         * to update the UI, we need to use a Handler to hop over there.
         */


        @Override
        public void receiveMessage(String message) throws RemoteException {
            Message msg = mHandler.obtainMessage(BUMP_MSG, 0, 0);
            Bundle bundle = new Bundle();
            bundle.putString("RECEIVE", message);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    };

    public void doUnbind() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    mService.stopServer();
                    mService.unregisterCallBack(mCallback);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }
            // Detach our existing connection.

            unbindService(mConnection);
            mIsBound = false;

        }
    }

    private void doBinding() {
        bindService(new Intent(this, MyService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    @Override
    public void onDestroy() {
        doUnbind();
        super.onDestroy();
    }
}
