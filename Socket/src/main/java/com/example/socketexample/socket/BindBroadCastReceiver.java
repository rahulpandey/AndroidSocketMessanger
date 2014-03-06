package com.example.socketexample.socket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Rahul on 6/3/14.
 */
public class BindBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent=new Intent(context,BindService.class);
        if (intent.getIntExtra(MainActivity.BIND_KEY, 0) == MainActivity.BIND_BROADCAST){
            context.startService(serviceIntent);
        } else {
            context.stopService(serviceIntent);
        }


    }



}
