package com.sotacommunityapp.sotacommunityapp.Radio;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.sotacommunityapp.sotacommunityapp.RadioActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;

import java.net.ServerSocket;
import java.net.Socket;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by James Kidd on 17/03/2015.
 */
public class RadioServiceRaw extends Service implements MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener,MediaPlayer.OnBufferingUpdateListener, RadioInterface {
    private static String  TAG = "RADIO";

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        Log.d("MP",i+"");
    }

    public enum RadioState {
        Buffering,
        Playing,
        Stopped,
        Error
    }

    private RadioState _state = RadioState.Stopped;

    private static final String scURL = "http://cp5.digistream.info:14102";
    /*Used to show notifications with track name*/
    private NotificationManager _notificationManager;
    /*Wifi Wake Lock*/
    WifiManager.WifiLock _wifiLock;
    /*Media Player instance*/
    private MediaPlayer _mediaPlayer;
    private List<RadioListener> _listeners = new ArrayList<RadioListener>();

    /*Relay socket*/
    private ServerSocket serverSocket;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

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
            _mediaPlayer.setVolume(.5f, .5f);
            _mediaPlayer.setOnErrorListener(this);
            _mediaPlayer.setOnBufferingUpdateListener(this);


        }
        if(_wifiLock == null)
        _wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "sota_radio_lock");
    }
    @Override
    public void onDestroy() {
        Log.d(TAG,"Destroy called");
        Stop();
        try {
            _mediaPlayer.release();
            _mediaPlayer = null;
            if(_wifiLock.isHeld())
                _wifiLock.release();
            _listeners.clear();
            if(!serverSocket.isClosed())
                serverSocket.close();
            serverSocket = null;
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
        _state = RadioState.Stopped;
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG,"Err: " + what + " " + extra);
        _state = RadioState.Error;
        stateChanged(_state);
        metaChanged("MediaPlayer Error", "");
        _mediaPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "Prepared");
        mp.start();
        _state = RadioState.Playing;
        stateChanged(_state);
    }

    @Override
    public void setVolume(float vol) {
        _mediaPlayer.setVolume(vol,vol);
    }

    private static Thread _mediaThread;
    @Override
    public void Play() {
        try {
            if(_mediaThread != null){
                _mediaThread.interrupt();
            }
            _mediaThread = new Thread(new SocketServerThread());
            _mediaThread.start();
            _state = RadioState.Buffering;
            stateChanged(_state);
            if(!_wifiLock.isHeld())
                _wifiLock.acquire();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void Stop() {
        try {
            if(_mediaThread.isAlive())
                _mediaThread.interrupt();
            _mediaThread = null;

            if(_mediaPlayer.isPlaying())
                _mediaPlayer.stop();
            if(_wifiLock.isHeld())
                _wifiLock.release();
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
        _state = RadioState.Stopped;
        stateChanged(_state);
    }

    @Override
    public boolean isPlaying() {
        // Handle edge case where radio is in load process.
        if(_state == RadioState.Buffering)
            return true;
        if(_state == RadioState.Error)
            return false;
        return _mediaPlayer.isPlaying();
    }

    private void stateChanged(RadioState state) {
        for(RadioListener i : _listeners) {
            i.onRadioStateChanged(state);
        }
    }

    public String CurrentTitle="",CurrentArtist="";
    private void metaChanged(String title,String artist) {
        CurrentTitle = title;
        CurrentArtist = artist;
        for(RadioListener i : _listeners) {
            i.onTrackTitleChanged(title, artist);
        }
    }

    @Override
    public void addListener(RadioListener listener) {
        if(!_listeners.contains(listener))
            _listeners.add(listener);
    }

    @Override
    public void removeListener(RadioListener listener) {
        if(_listeners.contains(listener))
            _listeners.remove(listener);
    }

    @Override
    public IBinder onBind(Intent intent) { return mBinder; }

    // This is the object that receives interactions from clients. See RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        public RadioServiceRaw getService() {
            return RadioServiceRaw.this;
        }
    }




    private class SocketServerThread extends Thread {
        byte[] buffer = new byte[1];
        static final int SocketServerPORT = 2053;
        @Override
        public void run() {
            try {
                /*Setup socket listening on local port 2053*/
                if(serverSocket == null)
                    serverSocket = new ServerSocket(SocketServerPORT);

                /*Connect to shoutcast*/
                Socket conn = new Socket(new URL(scURL).getHost(),new URL(scURL).getPort());
                OutputStream os = conn.getOutputStream();
                //Generate the HTTP request and send via socket
                String userAgent = "SotaCommunity/1.0";
                String req = "GET / HTTP/1.0\r\n" +
                        "user-agent: " + userAgent + "\r\n" +
                        "Icy-MetaData: 1 \r\n" +
                        "Connection: keep-alive\r\n\r\n";
                os.write(req.getBytes());
                /*Reset and prepare mediaplayer*/
                _mediaPlayer.reset();
                _mediaPlayer.setDataSource("http://localhost:2053");
                _mediaPlayer.prepareAsync();

                /*Accept the connection from mediaplayer*/
                Socket localServerSocket = serverSocket.accept();
                Log.d(TAG, "Accepted Con on 2053");
                /*MUST have atleast buffer of 1024, higher may be better*/
                BufferedOutputStream localServerOut = new BufferedOutputStream(localServerSocket.getOutputStream(),1024);

                BufferedInputStream scInput = new BufferedInputStream(conn.getInputStream());
                /*Parse interval from HTTP header*/
                int interval = parseHeaders(scInput,localServerOut);
                int bytesRead = 0,total = 0;
                /*While connected and the radio hasnt been stopped or errored loop*/
                while(scInput != null && _state != RadioState.Stopped && _state != RadioState.Error) {

                    /*Forward data from SC to local socket, and parse out metadata*/
                    while ((bytesRead = scInput.read(buffer)) != -1 && _state != RadioState.Stopped && _state != RadioState.Error) {
                        localServerOut.write(buffer, 0, bytesRead);

                        total ++;
                        if(total == interval){
                            String meta = readMeta(scInput);
                            if(meta != null)
                                metaChanged(meta.split("-")[0],meta.split("-")[1]);
                            total = 0;

                        }
                    }

                }
                Log.e(TAG, "Socket loop exited");
                if(_state != RadioState.Stopped){
                    metaChanged("Connection Lost", "");
                    stateChanged(RadioState.Error);
                }

            } catch (Exception e) {
                Log.e(TAG,"Con died: " + e.getMessage() + e.getStackTrace());
                metaChanged("Connection Lost", "");
                stateChanged(RadioState.Error);

            }

        }

        /*Grabs metadata from the stream*/
        private String readMeta(BufferedInputStream in) throws IOException {
            /*First byte tells us metadata size*/
            int bytesToRead = in.read() * 16;
            if(bytesToRead == 0)
                return null;
            Log.d(TAG, "Reading: " + bytesToRead);
            byte[] line = new byte[bytesToRead];
            int read = 0;
            while(read != bytesToRead)
                line[read++] = (byte)in.read();
            String[] splits = new String(line).trim().split(";");
            Log.d(TAG, "Read: " + new String(line).trim());
            if(splits != null){

                return splits[0].substring(splits[0].indexOf("'")).replace("'", "");
            //return title + " - " + artist;
            }

            return null;
        }

        /*Parses the ICY-OK 200 header's
        * MUST FORWARD TO MEDIAPLAYER */
        private int parseHeaders(BufferedInputStream inputStream, BufferedOutputStream localServerOut) throws IOException {
            byte[] buffer = new byte[32768];
            int cnt = 0;
            while(!new String(buffer).contains("\r\n\r\n")){
                inputStream.read(buffer,cnt++,1);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                localServerOut.write(buffer,0,cnt);
                localServerOut.flush();
            }

            String[] splits = new String(buffer).split("\r\n");
            for(String s : splits)
                if(s.contains("icy-metaint"))
                    return Integer.parseInt(s.split(":")[1]);
            /*Parse failed take a stab at guessing the right metadata interval
            * Valid time for our stream*/
            return 32768;
        }

    }

}
