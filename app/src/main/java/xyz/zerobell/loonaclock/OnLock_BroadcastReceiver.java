package xyz.zerobell.loonaclock;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class OnLock_BroadcastReceiver extends BroadcastReceiver {

    private TelephonyManager telephonyManager = null;
    private boolean isPhoneIdle = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_SCREEN_ON))
        {
            Log.e("onReceive", "SCREEN_ON");
        }
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        {
            Log.e("onReceive", "SCREEN_OFF");
            if(telephonyManager == null){
                telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
            }

            if(isPhoneIdle) {
                Intent i = new Intent(context, LockScreen.class);
                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, 0);
                try {
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            Log.e("onReceive", "BOOT_COMPLETED");
        }
    }

    private PhoneStateListener phoneListener = new PhoneStateListener(){

        @Override

        public void onCallStateChanged(int state, String incomingNumber){

            switch(state){

                case TelephonyManager.CALL_STATE_IDLE :

                    isPhoneIdle = true;

                    break;

                case TelephonyManager.CALL_STATE_RINGING :

                    isPhoneIdle = false;

                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK :

                    isPhoneIdle = false;

                    break;

            }

        }

    };
}
