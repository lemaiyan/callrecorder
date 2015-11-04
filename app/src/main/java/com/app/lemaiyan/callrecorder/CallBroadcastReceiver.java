package com.app.lemaiyan.callrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.app.lemaiyan.callrecorder.util.Constants;

public class CallBroadcastReceiver extends BroadcastReceiver
{
    public void onReceive(Context context, Intent intent) {
        //here we pick the details of the call whether it's outgoing or incoming and the phone number
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            Constants.PHONE_NUMBER = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Constants.CALL_TYPE="Outgoing";
        }else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            try{
                if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                    //means it's an incoming call
                    Constants.PHONE_NUMBER = number;
                    Constants.CALL_TYPE="Incoming";
                }
            }catch(Exception e){
            }
        }
        //here we listen for any change in the device call state
        PhoneListener phoneListener = new PhoneListener(context);
        TelephonyManager telephony = (TelephonyManager)
            context.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }
}
