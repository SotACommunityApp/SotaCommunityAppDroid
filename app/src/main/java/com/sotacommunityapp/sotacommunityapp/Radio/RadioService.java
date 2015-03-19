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
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.sotacommunityapp.sotacommunityapp.R;
import com.sotacommunityapp.sotacommunityapp.RadioActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Umbrae revision 15/02/2015.
 * Created by James Kidd on 7/02/2015.
 */
@Deprecated
public class RadioService extends Service implements MediaPlayer.OnPreparedListener, RadioInterface {

    private static final String scURL = "http://cp5.digistream.info:14102";
    /*Used to show notifications with track name*/
    private NotificationManager _notificationManager;
    /*Wifi Wake Lock*/
    WifiManager.WifiLock _wifiLock;
    /*Media Player instance*/
    private MediaPlayer _mediaPlayer;
    private InputStream mdIs;
    private Timer _timer;
    /*Event handlers*/
    private List<RadioListener> _listeners = new ArrayList<RadioListener>();
    // Notification
    private AtomicInteger c = new AtomicInteger(0);
    private int NOTIFICATION = c.incrementAndGet();
        //private int NOTIFICATION =  133;


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
        if(_mediaPlayer!= null) _mediaPlayer.setVolume(vol,vol);
    }

    public void Play() {
        try {
            _mediaPlayer.setDataSource(this, Uri.parse(scURL));
            _mediaPlayer.prepareAsync();
            _wifiLock.acquire();
            _timer = new Timer();
            _timer.schedule(new MetaDataTask(), 0, 10000);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Unable to Start Radio",
                    Toast.LENGTH_SHORT).show();
            for(RadioListener i : _listeners) {
                i.onTrackTitleChanged("", "");
                //i.onRadioChanged(false);
            }
            showNotification("] Not Playing");
            _mediaPlayer.reset();
            _timer.cancel();
            _wifiLock.release();
        }
    }

    public void Stop() {
        if(_mediaPlayer.isPlaying()) {
            _mediaPlayer.stop();
        }
        for(RadioListener i : _listeners) {
            i.onTrackTitleChanged("", "");
            //i.onRadioChanged(false);
        }
        showNotification("] Not Playing");
        _mediaPlayer.reset();
        _timer.cancel();
        _wifiLock.release();
    }

    @Override
    public void addListener(RadioListener listener) { _listeners.add(listener); }

    @Override
    public void removeListener(RadioListener listener) { _listeners.remove(listener); }

    @Override
    public void onPrepared(MediaPlayer mp) { mp.start(); }

    @Override
    public void onCreate() {
        Intent resultIntent = new Intent(this, RadioActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(RadioActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(resultPendingIntent);
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
        // TODO Tell the user we stopped.
    }

    @Override
    public IBinder onBind(Intent intent) { return mBinder; }

    // This is the object that receives interactions from clients. See RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    private void showNotification(String text) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, RadioActivity.class), 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Notification notification =  builder.setContentText(text).setContentTitle("Avatar's Radio").setContentIntent(contentIntent).setSmallIcon(R.drawable.ic_stat_notify).build();
        // Send the notification.
        _notificationManager.notify(NOTIFICATION, notification);
    }

    private class MetaDataTask extends TimerTask {
        @Override
        public void run() {
            String outMd = null;
            Log.w("Radio Data", "Running");
            boolean errIcy = false;
            try {
                URL updateURL = new URL(scURL);
                URLConnection conn = updateURL.openConnection();
                conn.setRequestProperty("Icy-MetaData", "1");
                int interval;
                try {
                    interval = Integer.valueOf(conn.getHeaderField("icy-metaint")); // You can get more headers if you wish. There is other useful data.
                } catch (NumberFormatException e) {
                    Log.e("Icy Not Found", "value(" + conn.getHeaderField("icy-metaint") + ")");
                    errIcy = true;
                    interval = 0;
                }
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
                    Log.w("Radio Data", "byteRead(" + Integer.toString(bytesRead) + ") offset(" + Integer.toString(offset) + ") mdLen(" + Integer.toString(metadataLength) + ")");
                    try {
                        bytesRead = mdIs.read(bytes, offset, metadataLength);
                        offset = bytesRead;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.e("Radio Data", "byteRead(" + Integer.toString(bytesRead) + ") offset(" + Integer.toString(offset) + ") mdLen(" + Integer.toString(metadataLength) + ")");
                        e.printStackTrace();
                        bytesRead = 0;
                        offset = 0;
                    }
                }

                String[] metaData = new String(bytes).trim().split(";");
                Log.w("Song Title", ": " + metaData[0]);
                if (metaData.length > 1) {
                    Log.w("Song Url", ": " + metaData[1]);
                    if (metaData[1].trim().contains("&artist=") && metaData[1].trim().contains("&title=")) {
                        outMd = metaData[1];
                    } else {
                        outMd = metaData[0];
                    }
                } else {
                    outMd = metaData[0];
                }
                outMd = metaData[0];

                Log.w("Chosen Song", ": " + outMd);
                mdIs.close();

            } catch (MalformedURLException e) { e.printStackTrace();
            } catch (IOException e) { e.printStackTrace(); }

            // StreamTitle='Ween - Gabrielle';StreamUrl='&artist=Ween&title=Gabrielle&album=&duration=208824&songtype=S&overlay=NO&buycd=&website=&picture=';
            if (outMd != null) {
                outMd = outMd.replace("'", "");
            }

            String title = "";
            String artist = "";
            Boolean boo = true;

            if (outMd != null && outMd.contains("StreamUrl")) {
                try {
                    Uri uriMd = Uri.parse("http://sotacommapp.com/?" + outMd);
                    title = uriMd.getQueryParameter("title");
                    artist = uriMd.getQueryParameter("artist");
                } catch (Exception e) { title = "No Song Metadata"; e.printStackTrace(); }
            }
            if ((title == null || title.isEmpty()) && (outMd != null && outMd.contains("StreamTitle"))) {
                try {
                    if (outMd.trim().equals("StreamTitle=")) {
                        title = "No Song Metadata";
                        artist = "";
                    } else {
                        String[] arrMd = outMd.replace("StreamTitle=", "").split("-");
                        title = arrMd[0].trim();
                        try {
                            artist = arrMd[1].trim();
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                } catch (Exception e) { e.printStackTrace(); }

            }

            // For Unsupported Devices
            if (errIcy) {
                title = "Song Information Unavailable";
                artist = "";
            }
            
            for(RadioListener i : _listeners) {
                if (title != null && !title.isEmpty()) {
                    i.onTrackTitleChanged(title, artist);
                }
                //i.onRadioChanged(boo);
            }

            String notTitle = "> " + title;
            if (artist != null && !artist.isEmpty()) { notTitle += " (" + artist + ")"; }
            showNotification(notTitle);

        }
    }
}
