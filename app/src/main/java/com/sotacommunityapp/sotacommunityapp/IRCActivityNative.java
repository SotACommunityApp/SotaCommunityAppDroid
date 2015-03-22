package com.sotacommunityapp.sotacommunityapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.sotacommunityapp.sotacommunityapp.IRC.IRCService;
import com.sotacommunityapp.sotacommunityapp.IRC.SotaIRCEventListener;

import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class IRCActivityNative extends ActionBarActivity implements SotaIRCEventListener {


    @InjectView(R.id.txt_input)
    EditText _txtInput;
    @InjectView(R.id.txt_output)
    TextView _txtOutput;

    private IRCService _IRCService;
    private boolean _bound;
    private static boolean _playing = false;

    /*Service connection*/
    private ServiceConnection _connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            _IRCService = ((IRCService.LocalBinder)service).getService();
            _IRCService.addListener(IRCActivityNative.this);
            _IRCService.ConnectAsync();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _IRCService = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irc_native);
        ButterKnife.inject(this);

        /*Spefically request the service to start to ensure it's not tied this this activity*/
        getApplicationContext().startService(new Intent(this, IRCService.class));
        bindService();



        _txtInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                   _IRCService.sendMessage(_txtInput.getText().toString());
                    _txtInput.setText("");
                    return true;
                }
                return false;
            }
        });



    }

    void bindService() {
        bindService(new Intent(this,IRCService.class),_connection, Context.BIND_AUTO_CREATE);
        _bound = true;
    }

    void unBindService() {
        if(_bound){
            _IRCService.removeListener(this);
            unbindService(_connection);
            _bound = false;
        }
    }

    private void print(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _txtOutput.setText(Html.fromHtml(msg)+"\n" + _txtOutput.getText(), TextView.BufferType.SPANNABLE);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ircactivity_native, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStateChanged() {

    }

    @Override
    public void onMessage(String msg) {
        print(msg);
    }
}
