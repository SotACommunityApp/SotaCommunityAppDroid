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
import android.support.v4.app.NotificationCompat;

import com.sotacommunityapp.sotacommunityapp.R;
import com.sotacommunityapp.sotacommunityapp.RadioActivity;

import net.moraleboost.streamscraper.ScrapeException;
import net.moraleboost.streamscraper.Scraper;
import net.moraleboost.streamscraper.Stream;
import net.moraleboost.streamscraper.scraper.ShoutCastScraper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by James Kidd on 7/02/2015.
 */
public class RadioService extends Service implements MediaPlayer.OnPreparedListener, RadioInterface {

    private static final String URL = "http://cp5.digistream.info:14102";

    /*Used to show notifications with track name*/
    private NotificationManager _notificationManager;

    /*Wifi Wake Lock*/
    WifiManager.WifiLock _wifiLock;
    /*Media Player instance*/
    private MediaPlayer _mediaPlayer;

    /*Event handlers*/
    private List<RadioListener> _listeners = new ArrayList<RadioListener>();

    private Timer _timer = new Timer();

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
        _timer.schedule(new MetaDataTask(),0,30000);
    }

    public void Stop() {
        if(_mediaPlayer.isPlaying())
            _mediaPlayer.stop();
        _timer.cancel();
    }

    @Override
    public void addListener(RadioListener listener) {
        _listeners.add(listener);
    }

    @Override
    public void removeListener(RadioListener listener) {
        _listeners.remove(listener);
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();

    }


    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 133; //todo should be a resource



    @Override
    public void onCreate() {
        _notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, RadioActivity.class), 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Notification notification =  builder.setContentText("Loading...").setContentTitle("Avatar's Radio").setContentIntent(contentIntent).setSmallIcon(R.drawable.ic_stat_notify).build();
        startForeground(NOTIFICATION, notification);

        if(_mediaPlayer == null) {
            _mediaPlayer = new MediaPlayer();
            _mediaPlayer.setOnPreparedListener(this);
            _mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            _wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "sota_radio_lock");

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
        _listeners.clear();
        _timer.cancel();
        _timer.purge();


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

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, RadioActivity.class), 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Notification notification =  builder.setContentText(text).setContentTitle("Avatar's Radio").setContentIntent(contentIntent).setSmallIcon(R.drawable.ic_launcher).build();


        // Send the notification.
        _notificationManager.notify(NOTIFICATION, notification);
    }


    private class MetaDataTask extends TimerTask {
        private Scraper _scraper = new ShoutCastScraper();
        private Stream _stream;
        @Override
        public void run() {
            //grab meta data
            if(_stream == null) {
                try {
                    List<Stream> streams = _scraper.scrape(new URI(URL));
                    if(streams.size() == 0)
                        return;
                    streams = _scraper.scrape(new URI(URL));
                    _stream = streams.get(0);
                } catch (ScrapeException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
                String title = _stream.getCurrentSong();
                for(RadioListener i : _listeners)
                    i.onTrackTitleChanged("Playing: " + title);
                showNotification("Playing: " + title);
        }

    }

}
