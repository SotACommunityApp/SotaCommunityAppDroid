package com.sotacommunityapp.sotacommunityapp.IRC;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;

import com.sotacommunityapp.sotacommunityapp.Radio.RadioListener;
import com.sotacommunityapp.sotacommunityapp.RadioActivity;

import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IRCService extends Service implements IRCEventListener, SotaIRCInterface {
    private IRCConnection _connection;
    final String SERVER = "irc.ultimacodex.com";
    final int PORT = 6667;
    private List<SotaIRCEventListener> _listeners = new ArrayList<SotaIRCEventListener>();

    public IRCService() {
    }


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

        if(_connection == null){
            String name = PreferenceManager.getDefaultSharedPreferences(this).getString("irc_username","Citizen?");
            _connection = new IRCConnection(SERVER,new int[] {PORT},null,name,name,"SotaIRCDroid",null,0,null);
            _connection.addIRCEventListener(this);
            _connection.setEncoding("UTF-8");
            _connection.setPong(true);
            //_connection.setDaemon(false);
            //_connection.setColors(false);

        }
        else {
            _connection.addIRCEventListener(this);
        }

    }

    @Override
    public IBinder onBind(Intent intent) { return mBinder; }

    // This is the object that receives interactions from clients. See RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    @Override
    public void sendMessage(String msg) {
        _connection.doPrivmsg("#sota",msg);
    }

    @Override
    public void ConnectAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    _connection.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void Dissconnect() {
     _connection.close();
    }


    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        public IRCService getService() {
            return IRCService.this;
        }
    }

    public void addListener(SotaIRCEventListener listener) {
        if(!_listeners.contains(listener))
            _listeners.add(listener);
    }


    public void removeListener(SotaIRCEventListener listener) {
        if(_listeners.contains(listener))
            _listeners.remove(listener);
    }

    private void print(String msg) {
        for(SotaIRCEventListener l : _listeners)
            l.onMessage(msg);
    }

    public void onRegistered() {
        print("Connected");
        _connection.doJoin("#sota");
    }

    public void onDisconnected() {
        print("Disconnected");
    }

    public void onError(String msg) {
        print("Error: "+ msg);
    }

    public void onError(int num, String msg) {
        print("Error #"+ num +": "+ msg);
    }

    public void onInvite(String chan, IRCUser u, String nickPass) {
        print(chan +"> "+ u.getNick() +" invites "+ nickPass);
    }

    public void onJoin(String chan, IRCUser u) {
        print("<font color='red'>" + chan +"> "+ u.getNick() +" joins" + "</font>");
    }

    public void onKick(String chan, IRCUser u, String nickPass, String msg) {
        print(chan +"> "+ u.getNick() +" kicks "+ nickPass);
    }

    public void onMode(IRCUser u, String nickPass, String mode) {
        print("Mode: "+ u.getNick() +" sets modes "+ mode +" "+
                nickPass);
    }

    public void onMode(String chan, IRCUser u, IRCModeParser mp) {
        print(chan +"> "+ u.getNick() +" sets mode: "+ mp.getLine());
    }

    public void onNick(IRCUser u, String nickNew) {
        print("Nick: "+ u.getNick() +" is now known as "+ nickNew);
    }

    public void onNotice(String target, IRCUser u, String msg) {
        print(target +"> "+ u.getNick() +" (notice): "+ msg);
    }

    public void onPart(String chan, IRCUser u, String msg) {
        print(chan +"> "+ u.getNick() +" parts");
    }

    public void onPrivmsg(String chan, IRCUser u, String msg) {
        if(chan.equalsIgnoreCase("#sota"))
            print("<font color='red'>" + u.getNick() + "</font> : <font color='black'>" + msg + "</font>");
        else
            print("<font color='blue'>" + u.getNick() + " </font> >> : <font color='brown'>"+ msg + "</font>");
    }

    public void onQuit(IRCUser u, String msg) {
        print("<font color='red'>Quit: "+ u.getNick() + "</font>");
    }

    public void onReply(int num, String value, String msg) {
        print("Reply #"+ num +": "+ value +" "+ msg);
    }

    public void onTopic(String chan, IRCUser u, String topic) {
        print(chan +"> "+ u.getNick() +" changes topic into: "+ topic);
    }

    public void onPing(String p) {

    }

    public void unknown(String a, String b, String c, String d) {
        print("UNKNOWN: "+ a +" b "+ c +" "+ d);
    }

}
