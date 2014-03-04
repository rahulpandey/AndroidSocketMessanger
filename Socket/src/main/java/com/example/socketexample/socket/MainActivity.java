package com.example.socketexample.socket;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity {
    IRemoteService mService = null;
    ArrayAdapter<String> arrayAdapter;
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(R.id.left_drawer);
        mEditText = (EditText) findViewById(R.id.editText);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(arrayAdapter);


    }

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
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;


        }
    };

    public void sayHello(View v) {

        // Create and send a message to the service, using a supported 'what' value

        try {
            mService.sendMessage(mEditText.getText().toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, MyService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    private static final int BUMP_MSG = 1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BUMP_MSG:
                    arrayAdapter.add(msg.getData().getString("RECEIVE"));
                    arrayAdapter.notifyDataSetChanged();
                    Log.d(MainActivity.class.getName(), msg.getData().getString("RECEIVE") + "");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

    };

    @Override
    protected void onStop() {
        unbindService(mConnection);
        super.onStop();
    }
}

