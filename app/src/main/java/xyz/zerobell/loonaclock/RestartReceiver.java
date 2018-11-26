package xyz.zerobell.loonaclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RestartReceiver extends BroadcastReceiver {
    static public final String ACTION_RESTART_SERVICE = "RestartReceiver.restart";    // 값은 맘대로



    @Override

    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(ACTION_RESTART_SERVICE)){
            Intent i = new Intent(context, OnLock_Service.class);
            context.startService(i);

        }

    }

}
