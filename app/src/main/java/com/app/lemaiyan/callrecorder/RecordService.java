package com.app.lemaiyan.callrecorder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.app.lemaiyan.callrecorder.util.Constants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class RecordService 
    extends Service
    implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener
{
    private static final String TAG = "CallRecorder";

    public static final String DEFAULT_STORAGE_LOCATION = "/sdcard/CallRecorder";
    private static final int RECORDING_NOTIFICATION_ID = 1;

    private MediaRecorder recorder = null;
    private boolean isRecording = false;
    private File recording = null;;


    private File makeOutputFile ()
    {
        File dir = new File(DEFAULT_STORAGE_LOCATION);
        // test dir for existence and writeability
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception e) {
                Log.e(TAG, "error creating record directory " + dir + ": " + e);
                return null;
            }
        } else {
            if (!dir.canWrite()) {
                Log.e(TAG, "we don't have permissions to write to the directory " + dir);

                return null;
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String prefix = Constants.CALL_TYPE +"-" + Constants.PHONE_NUMBER+ "-" +sdf.format(new Date());


        // create suffix based on format
        String suffix = ".m4a";

        try {
            return File.createTempFile(prefix, suffix, dir);
        } catch (IOException e) {
            Log.e(TAG, "Unable to create temp file " + dir + ": " + e);
            return null;
        }
    }

    public void onCreate()
    {
        super.onCreate();
        //create a MediaRecorderObject
        recorder = new MediaRecorder();
    }

    public void onStart(Intent intent, int startId) {

        //if we are recording we exit
        if (isRecording) return;

        int audiosource = MediaRecorder.AudioSource.VOICE_CALL;
        int audioformat = MediaRecorder.OutputFormat.MPEG_4;

        recording = makeOutputFile();
        if (recording == null) {
            recorder = null;
            return;
        }

        try {
            // These calls will throw exceptions unless you set the 
            // android.permission.RECORD_AUDIO permission for your app
            //configure AudioRecorder
            recorder.reset();
            recorder.setAudioSource(audiosource);
            recorder.setOutputFormat(audioformat);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            //recorder.setAudioChannels(2); //stereo
            recorder.setAudioEncodingBitRate(128);
            recorder.setAudioSamplingRate(44100);
            recorder.setOutputFile(recording.getAbsolutePath());
            recorder.setOnInfoListener(this);
            recorder.setOnErrorListener(this);
            
            try {
                recorder.prepare();
            } catch (java.io.IOException e) {
                recorder = null;
                return;
            }

            recorder.start();
            isRecording = true;
            updateNotification(true);
        } catch (java.lang.Exception e) {
            recorder = null;
        }

        return;
    }

    public void onDestroy()
    {
        super.onDestroy();

        if (null != recorder) {
            isRecording = false;
            recorder.release();
            Toast.makeText(getApplicationContext(), "CallRecorder finished recording call to " + recording, Toast.LENGTH_LONG).show();
        }

        updateNotification(false);
    }


    // methods to handle binding the service

    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public boolean onUnbind(Intent intent)
    {
        return false;
    }

    public void onRebind(Intent intent) {}

    @SuppressWarnings("deprecation")
    private void updateNotification(Boolean status)
    {


        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

        if (status) {
            int icon = R.drawable.ic_mic;
            long when = System.currentTimeMillis();
            
            //Notification notification = new Notification(icon, tickerText, when);
            Intent notificationIntent = new Intent(this, RecordService.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            Context context = getApplicationContext();
            Notification notification = new Notification.Builder(context).setContentTitle("Call Recorder")
                    .setSmallIcon(icon)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setContentText("Recording call..").build();
            mNotificationManager.notify(RECORDING_NOTIFICATION_ID, notification);
        } else {
            mNotificationManager.cancel(RECORDING_NOTIFICATION_ID);
        }
    }

    // MediaRecorder.OnInfoListener
    public void onInfo(MediaRecorder mr, int what, int extra)
    {
        isRecording = false;
    }

    // MediaRecorder.OnErrorListener
    public void onError(MediaRecorder mr, int what, int extra)
    {
        isRecording = false;
        mr.release();
    }
}
