package com.app.lemaiyan.callrecorder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneListener extends PhoneStateListener
{
    private Context context;

    public PhoneListener(Context c) {
        context = c;
    }

    public void onCallStateChanged (int state, String incomingNumber)
    {
        //we check for any change on the call state
        switch (state) {
        case TelephonyManager.CALL_STATE_IDLE:
            //if the state is idle we stop the recording
            Boolean stopped = context.stopService(new Intent(context, RecordService.class));
            break;
        case TelephonyManager.CALL_STATE_RINGING:
            //if it's ringing we leave it alone.
            break;
        case TelephonyManager.CALL_STATE_OFFHOOK:
            //if the phone is off the hook we start the recording.
            Intent callIntent = new Intent(context, RecordService.class);
            context.startService(callIntent);
            break;
        }
    }
}
