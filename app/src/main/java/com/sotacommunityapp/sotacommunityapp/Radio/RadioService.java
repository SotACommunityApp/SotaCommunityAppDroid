package com.sotacommunityapp.sotacommunityapp.Radio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import com.sotacommunityapp.sotacommunityapp.R;
import com.sotacommunityapp.sotacommunityapp.RadioActivity;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by James Kidd on 7/02/2015.
 */
public class RadioService extends Service implements MediaPlayer.OnPreparedListener, RadioInterface {

    /*Used to show notifications with track name*/
    private NotificationManager _notificationManager;

    /*Wifi Wake Lock*/
    WifiManager.WifiLock _wifiLock;
    /*Media Player instance*/
    private MediaPlayer _mediaPlayer;

    private List<RadioListener> listeners = new ArrayList<RadioListener>();
    private static final String URL = "http://cp5.digistream.info:14102";

    public boolean isPlaying() {
        if(_mediaPlayer != null)
            return _mediaPlayer.isPlaying();
        return false;
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        public RadioService getService() {
            return RadioService.this;
        }
    }


    @Override
    public void setVolume(float vol) {
        if(_mediaPlayer!= null)
            _mediaPlayer.setVolume(vol,vol);
    }

    public void Play() {

        _mediaPlayer.prepareAsync();
    }

    public void Stop() {
        if(_mediaPlayer.isPlaying())
            _mediaPlayer.stop();
    }

    @Override
    public void addListener(RadioListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(RadioListener listener) {
        listeners.remove(listener);
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        showNotification("Playing...");
        for(RadioListener i : listeners)
            i.onTrackTitleChanged("Playing...");
    }


    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 133; //todo should be a resource



    @Override
    public void onCreate() {
        _notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), RadioActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification();
        notification.tickerText = "Loading";
        notification.icon = R.drawable.ic_launcher;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.setLatestEventInfo(getApplicationContext(), "Avatar Radio",
                "Loading....:", pi);
        startForeground(NOTIFICATION, notification);
        if(_mediaPlayer == null) {
            _mediaPlayer = new MediaPlayer();
            _mediaPlayer.setOnPreparedListener(this);
            _mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            _wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

            _wifiLock.acquire();
            //setVolumeControlStream(AudioManager.STREAM_MUSIC);
            _mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            _mediaPlayer.setVolume(.5f,.5f);
            try {
                _mediaPlayer.setDataSource(this, Uri.parse(URL));

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       // Logger.d("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        _notificationManager.cancel(NOTIFICATION);
        Stop();
        _mediaPlayer.release();
        _mediaPlayer = null;
        _wifiLock.release();
        listeners.clear();
        // Tell the user we stopped.
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Show a notification while this service is running.
     */
    private void showNotification(String text) {
        // In this sample, we'll use the same text for the ticker and the expanded notification

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.ic_launcher, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, RadioActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, "Avatar's Radio",
                text, contentIntent);

        // Send the notification.
        _notificationManager.notify(NOTIFICATION, notification);
    }
}
