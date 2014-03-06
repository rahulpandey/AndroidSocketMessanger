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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
    IRemoteService mService = null;
    ArrayAdapter<String> arrayAdapter;
    private EditText mEditText;
    private boolean mIsBound=false;
    public static final int BIND_BROADCAST=1;
    public static final int UNBIND_BROADCAST=2;
    public static final String BIND_KEY="bind";
    public static final String UNBIND_KEY="unbind";
    LocalBroadcastManager localBroadcastManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(R.id.left_drawer);
        mEditText = (EditText) findViewById(R.id.editText);
        arrayAdapter = new ArrayAdapter<String>(this,R.layout.list_item,android.R.id.text1);
        listView.setAdapter(arrayAdapter);
        localBroadcastManager= LocalBroadcastManager.getInstance(this);
        unBindBroadCast();
    }

    private void unBindBroadCast() {
        Intent unbindIntent=new Intent(this,BindBroadCastReceiver.class);
        unbindIntent.putExtra(UNBIND_KEY,UNBIND_BROADCAST);
        sendBroadcast(unbindIntent);
    }
    private void bindBroadCast() {
        Intent bindIntent=new Intent(this,BindBroadCastReceiver.class);
        bindIntent.putExtra(BIND_KEY,BIND_BROADCAST);
        sendBroadcast(bindIntent);
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
            Toast.makeText(MainActivity.this,"Unbind done",Toast.LENGTH_SHORT).show();
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
    }

    private void doBinding() {
        bindService(new Intent(this,MyService.class),  mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
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
        doUnbind();
        bindBroadCast();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }
    public void doUnbind(){
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_start){
            doBinding();
            return true;
        }
        if(item.getItemId()== R.id.action_stop){
            doUnbind();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

