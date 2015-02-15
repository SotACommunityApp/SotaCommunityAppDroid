package com.sotacommunityapp.sotacommunityapp.Radio;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
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
import android.util.Log;
import android.widget.Toast;

import com.sotacommunityapp.sotacommunityapp.R;
import com.sotacommunityapp.sotacommunityapp.RadioActivity;

import net.moraleboost.streamscraper.ScrapeException;
import net.moraleboost.streamscraper.Scraper;
import net.moraleboost.streamscraper.Stream;
import net.moraleboost.streamscraper.scraper.ShoutCastScraper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by James Kidd on 7/02/2015.
 */
public class RadioService extends Service implements MediaPlayer.OnPreparedListener, RadioInterface {

    private static final String scURL = "http://cp5.digistream.info:14102";

    /*Used to show notifications with track name*/
    private NotificationManager _notificationManager;

    /*Wifi Wake Lock*/
    WifiManager.WifiLock _wifiLock;
    /*Media Player instance*/
    private MediaPlayer _mediaPlayer;
    private InputStream mdIs;

    /*Event handlers*/
    private List<RadioListener> _listeners = new ArrayList<RadioListener>();

    private Timer _timer;

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
        try {
            _mediaPlayer.setDataSource(this, Uri.parse(scURL));

        } catch (IOException e) {
            e.printStackTrace();
        }
        _mediaPlayer.prepareAsync();

        _wifiLock.acquire();

        _timer = new Timer();
        _timer.schedule(new MetaDataTask(),0,10000);
    }

    public void Stop() {
        if(_mediaPlayer.isPlaying()) { _mediaPlayer.stop(); }
        _mediaPlayer.reset();

        _timer.cancel();

        _wifiLock.release();
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

        if(_mediaPlayer == null) {
            _mediaPlayer = new MediaPlayer();
            _mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            _mediaPlayer.setOnPreparedListener(this);
            _mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            _mediaPlayer.setVolume(.5f,.5f);

            _wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "sota_radio_lock");

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
        Notification notification =  builder.setContentText(text).setContentTitle("Avatar's Radio").setContentIntent(contentIntent).setSmallIcon(R.drawable.ic_stat_notify).build();

        // Send the notification.
        _notificationManager.notify(NOTIFICATION, notification);
    }

    private class MetaDataTask extends TimerTask {
        private Scraper _scraper = new ShoutCastScraper();
        private Stream _stream;
        @Override
        public void run() {
            String title = null, djName = null;
            Log.w("Radio Meta Data", "Running");
            try {

                URL updateURL = new URL(scURL);
                URLConnection conn = updateURL.openConnection();
                conn.setRequestProperty("Icy-MetaData", "1");
                int interval = Integer.valueOf(conn.getHeaderField("icy-metaint")); // You can get more headers if you wish. There is other useful data.

                mdIs = conn.getInputStream();

                int skipped = 0;
                while (skipped < interval) {
                    skipped += mdIs.skip(interval - skipped);
                }

                int metadataLength = mdIs.read() * 16;

                int bytesRead = 0;
                int offset = 0;
                byte[] bytes = new byte[metadataLength];


                while (bytesRead < metadataLength && bytesRead != -1) {
                    Log.w("Metadata Count", "byteRead(" + Integer.toString(bytesRead) + ") offset(" + Integer.toString(offset) + ") mdLen(" + Integer.toString(metadataLength) + ")");
                    try {
                        bytesRead = mdIs.read(bytes, offset, metadataLength);
                        offset = bytesRead;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.e("Metadata Count", "byteRead(" + Integer.toString(bytesRead) + ") offset(" + Integer.toString(offset) + ") mdLen(" + Integer.toString(metadataLength) + ")");
                        e.printStackTrace();
                        bytesRead = 0;
                        offset = 0;
                    }
                }

                // StreamTitle='Ween - Gabrielle';StreamUrl='&artist=Ween&title=Gabrielle&album=&duration=208824&songtype=S&overlay=NO&buycd=&website=&picture=';
                String[] metaData = new String(bytes).trim().split(";");
                String outMd = "Error: " + bytes.toString().trim();
                try {
                    if (metaData.length > 1) {
                        outMd = metaData[1];
                    } else {
                        outMd = metaData[0];
                    }
                } catch (NullPointerException npe) { }

                //String metaData = new String(bytes).trim();
                //title = metaData.substring(metaData.indexOf("StreamTitle='") + 13, metaData.indexOf(" / ", metaData.indexOf("StreamTitle='"))).trim();
                //djName = metaData.substring(metaData.indexOf(" / ", metaData.indexOf("StreamTitle='")) + 3, metaData.indexOf("';", metaData.indexOf("StreamTitle='"))).trim();
                Log.w("Radio Meta Data", outMd);
                mdIs.close();

            } catch (MalformedURLException e) { e.printStackTrace();
            } catch (IOException e) { e.printStackTrace(); }

            final String titleFin = title;
            String djNameFin = djName;

            Log.w("Radio Meta Data", "Title: " + titleFin);
            for(RadioListener i : _listeners)
                i.onTrackTitleChanged("Playing: " + titleFin + " (" + djNameFin + ")");
            showNotification("Playing: " + titleFin);

        }

    }

    /*
    Backup
        private class MetaDataTask extends TimerTask {
        private Scraper _scraper = new ShoutCastScraper();
        private Stream _stream;
        @Override
        public void run() {
            //grab meta data
            Log.e("Radio Meta Data", "Running");
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
                Log.e("Radio Meta Data", title);
                for(RadioListener i : _listeners)
                    i.onTrackTitleChanged("Playing: " + title);
                showNotification("Playing: " + title);
        }

    }
    */

}
