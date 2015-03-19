package com.sotacommunityapp.sotacommunityapp;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void newsButtonClick(View view) {
        startActivity(new Intent(MainActivity.this, NewsActivity.class));
    }
    public void radioButtonClick(View view) {
        startActivity(new Intent(MainActivity.this, RadioActivity.class));
    }
    public void ircButtonClick(View view) {
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("open_external_irc",false)){
            String name = PreferenceManager.getDefaultSharedPreferences(this).getString("irc_username","Citizen?");
            String url = "https://kiwiirc.com/client/irc.ultimacodex.com/?nick="+ name + "&theme=mini#sota";
            openUrlInBrowser(url);
            return;
        }
        startActivity(new Intent(MainActivity.this, IrcActivity.class));
    }

    private void openUrlInBrowser(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void wikiButtonClick(View view) {
        // startActivity(new Intent(MainActivity.this, WikiActivity.class));
        Toast.makeText(getApplicationContext(), "For Future Development...",
                Toast.LENGTH_SHORT).show();
    }
    public void calendarButtonClick(View view) {
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("open_external_calender",false)){
            String url = "https://www.google.com/calendar/embed?showTitle=0&mode=AGENDA&wkst=1&hl=en&bgcolor=%23FFFFFF&src=qiqcreqgligph3v8ls9eu43c0s%40group.calendar.google.com&color=%2342104A&src=ebnghc2i99hf1cr9bie4vqhk4k%40group.calendar.google.com&color=%23B1440E&src=17vdgtcc8kc73bo7fmhktqms4g%40group.calendar.google.com&color=%2323164E&src=eobbm02j8b0bpt16kpn7p1s7ls%40group.calendar.google.com&color=%23865A5A&src=lfteirbsgovs4l71ahe344gf1k%40group.calendar.google.com&color=%23AB8B00&src=7rg0qv7s2t7qrbhtn0c4nl9h1c%40group.calendar.google.com&color=%236B3304&src=2p3tomru3fqs3qck5glqq0cdkg%40group.calendar.google.com&color=%23125A12&src=1shkpg91msvnvrcpf6qr59sek4%40group.calendar.google.com&color=%232952A3&src=ecog6sgvimkf39v7vaf3i3cqd0%40group.calendar.google.com&color=%23528800&ctz=America%2FChicago";
            openUrlInBrowser(url);
            return;
        }
        startActivity(new Intent(MainActivity.this, CalendarActivity.class));
    }
    public void mapButtonClick(View view) {
        // startActivity(new Intent(MainActivity.this, MapActivity.class));
        Toast.makeText(getApplicationContext(), "For Future Development...",
                Toast.LENGTH_SHORT).show();
    }
    public void secretButtonClick(View view) {
        // startActivity(new Intent(MainActivity.this, MapActivity.class));
        Toast.makeText(getApplicationContext(), "The SOTA Community App Team:\n" +
                        "\n" +
                        "Matterio (Mastermind/iOS)\n" +
                        "Bubonic (Artwork/Design)\n" +
                        "Belgeran (iOS/Android)\n" +
                        "Umbrae (Android)\n" +
                        "\n" +
                        "Thanks to Lord British and his team at Portalarium " +
                        "for creating the worlds for which this community thrives.\n" +
                        "\nhttps://www.shroudoftheavatar.com/",
                Toast.LENGTH_LONG).show();
    }

}
