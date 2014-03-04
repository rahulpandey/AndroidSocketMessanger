package com.example.socketexample.socket;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Rahul on 4/3/14.
 */
public class MyService extends Service {
    private static final int REPORT_MSG = 1;
    private Socket connection;
    private String serverIP = "10.0.2.2";
    private ObjectOutputStream output;
    private String message;
    private ObjectInputStream input;
    RemoteCallbackList<IRemoteServiceCallBack> mCallbacks = new RemoteCallbackList<IRemoteServiceCallBack>();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        // While this service is running, it will continually increment a
        // number.  Send the first message that is used to perform the
        // increment.
       // mHandler.sendEmptyMessage(REPORT_MSG);
    }

    /**
     * The IRemoteInterface is defined through IDL
     */
    private final IRemoteService.Stub mBinder = new IRemoteService.Stub() {
        @Override
        public void registerCallBack(IRemoteServiceCallBack cb) throws RemoteException {
            if (cb != null) mCallbacks.register(cb);
        }

        @Override
        public void unregisterCallBack(IRemoteServiceCallBack cb) throws RemoteException {
            if (cb != null) mCallbacks.register(cb);
        }

        @Override
        public void sendMessage(String message) throws RemoteException {
            sendMessageToUser(message);
        }

        @Override
        public void startServer() throws RemoteException {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    startRunning();
                }
            }).start();
        }

        @Override
        public void stopServer() throws RemoteException {
            sendMessageToUser("END");
        }
    };

    //connect to server
    public void startRunning() {
        try {
            connectToServer();
            setupStreams();
            whileChatting();
        } catch (EOFException eofException) {
            showMessage("\n Client terminated connection");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            closeCrap();
        }
    }

    //connect to server
    private void connectToServer() throws IOException {
        showMessage("Attempting connection... \n");
        connection = new Socket(InetAddress.getByName(serverIP), 6789);
        showMessage("Connected to: " + connection.getInetAddress().getHostName());
    }

    //set up streams to send and receive messages
    private void setupStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        showMessage("\n Dude your streams are now good to go! \n");
    }

    //while chatting with server
    private void whileChatting() throws IOException {
        do {
            try {
                message = (String) input.readObject();
                showMessage("\n" + message);
            } catch (ClassNotFoundException classNotfoundException) {
                showMessage("\n I dont know that object type");
            }
        } while (!message.equals("SERVER - END"));
    }

    //close the streams and sockets
    private void closeCrap() {
        showMessage("\n closing crap down...");

        try {
            output.close();
            input.close();
            connection.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    //send messages to server
    private void sendMessageToUser(String message) {
        try {
            output.writeObject("CLIENT - " + message);
            output.flush();
            showMessage("\nCLIENT - " + message);
        } catch (IOException ioException) {

        }
    }

    @Override
    public void onDestroy() {
        // Unregister all callbacks.
        mCallbacks.kill();
        // Remove the next pending message to increment the counter, stopping
        // the increment loop.
        mHandler.removeMessages(REPORT_MSG);
        super.onDestroy();
    }

    //change/update chatWindow
    private void showMessage(final String m) {
        Message msg = mHandler.obtainMessage(REPORT_MSG);
        Bundle b = new Bundle();
        b.putString("data", m);
        msg.setData(b);
        mHandler.sendMessage(msg);
    }


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                // It is time to bump the value!
                case REPORT_MSG: {
                    final int N = mCallbacks.beginBroadcast();
                    for (int i = 0; i < N; i++) {
                        try {
                            mCallbacks.getBroadcastItem(i).receiveMessage(msg.getData().getString("data"));
                        } catch (RemoteException e) {
                            // The RemoteCallbackList will take care of removing
                            // the dead object for us.
                        }
                    }
                    mCallbacks.finishBroadcast();

                }
                break;
                default:
                    super.handleMessage(msg);
            }
        }
    };


}
