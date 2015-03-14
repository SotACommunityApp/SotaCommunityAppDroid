package com.sotacommunityapp.sotacommunityapp;

        import android.content.ComponentName;
        import android.content.Context;
        import android.content.Intent;
        import android.content.ServiceConnection;
        import android.net.Uri;
        import android.os.IBinder;
        import android.support.v7.app.ActionBarActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.Window;
        import android.widget.SeekBar;
        import android.widget.Switch;
        import android.widget.TextView;
        import android.widget.ToggleButton;
        import com.sotacommunityapp.sotacommunityapp.Radio.RadioListener;
        import com.sotacommunityapp.sotacommunityapp.Radio.RadioService;
        import butterknife.ButterKnife;
        import butterknife.InjectView;

/**
 * Created by James Kidd on 7/02/2015.
 */
public class RadioActivity extends ActionBarActivity implements RadioListener {

    /*Inject our Views*/
    @InjectView(R.id.txt_radio_state)
        TextView _txtRadioState;
    @InjectView(R.id.txt_radio_title)
        TextView _txtRadioTitle;
    /*
    @InjectView(R.id.txt_radio_artist)
        TextView _txtRadioArtist;
    */
    @InjectView(R.id.btn_toggle_play)
        Switch _btnPlayStop;
    @InjectView(R.id.slider_volume)
        SeekBar _volumeSeekbar;

    /*Music service refrence*/
    private RadioService _radioService;
    private boolean _bound;
    private static boolean _playing = false;

    /*Service connection*/
    private ServiceConnection _connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            _radioService = ((RadioService.LocalBinder)service).getService();
            _radioService.addListener(RadioActivity.this);
            if(_radioService.isPlaying())
                _btnPlayStop.toggle();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _radioService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);
        /*All you need to do to setup Butterknife*/
        ButterKnife.inject(this);

        /*Spefically request the service to start to ensure it's not tied this this activity*/
        getApplicationContext().startService(new Intent(this,RadioService.class));
        bindService();
        /*Setup vol control*/
        _volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(_radioService != null)
                    _radioService.setVolume((float)progress/100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        if (_playing) {
            onRadioChanged(true);
            onTrackTitleChanged("Loading...","");
        } else {
            onRadioChanged(false);
            onTrackTitleChanged("", "");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*Remove our connection to the service*/
        unBindService();
    }

    void bindService() {
        bindService(new Intent(this,RadioService.class),_connection, Context.BIND_AUTO_CREATE);
        _bound = true;
    }

    void unBindService() {
        if(_bound){
            _radioService.removeListener(this);
            unbindService(_connection);
            _bound = false;
        }
    }

    public void onButtonClicked(View v) {
        if(!_bound) {
            bindService();
            return;
        }
        if(_radioService == null){
            bindService();
            return;
        }
        if(_radioService.isPlaying()) {
            _radioService.Stop();
            _playing = false;
            onRadioChanged(false);
            onTrackTitleChanged("", "");
        } else {
            _radioService.Play();
            _playing = true;
            onRadioChanged(true);
            onTrackTitleChanged("Loading...","");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_radio, menu);
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
    public void onTrackTitleChanged(final String title, final String artist) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String fullTitle = "";
                if (!artist.trim().isEmpty()) {
                    fullTitle = title + " - " + artist;
                } else if (!title.trim().isEmpty()) {
                    fullTitle = title;
                }
                if (_txtRadioTitle.getText() != fullTitle) {
                    _txtRadioTitle.setText(fullTitle);
                }
                /*
                if (_txtRadioArtist.getText() != artist) {
                    _txtRadioArtist.setText(artist);
                }
                */

            }
        });
    }
    @Override
    public void onRadioChanged(final boolean state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state) {
                    if (_txtRadioState.getText() != getResources().getString(R.string.radio_playing_text_on)) {
                        _txtRadioState.setText(R.string.radio_playing_text_on);
                        _txtRadioState.setTextColor(getResources().getColor(R.color.radio_playing_color_on));
                    }
                } else {
                    if (_txtRadioState.getText() != getResources().getString(R.string.radio_playing_text_off)) {
                        _txtRadioState.setText(R.string.radio_playing_text_off);
                        _txtRadioState.setTextColor(getResources().getColor(R.color.radio_playing_color_off));
                    }
                }
            }
        });
    }

    public void patreonButtonClick(View view) {
        Uri uri = Uri.parse("https://www.patreon.com/user?u=272464");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
