package com.sotacommunityapp.sotacommunityapp;

import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.sotacommunityapp.sotacommunityapp.saxrssreader.RssFeed;
import com.sotacommunityapp.sotacommunityapp.saxrssreader.RssItem;
import com.sotacommunityapp.sotacommunityapp.saxrssreader.RssReader;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class NewsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        TextView txtTitle = (TextView) findViewById(R.id.rss_feed);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        URL url = null;
        try {
            url = new URL("http://avatarsportal.com/feed");
        } catch (MalformedURLException e) { e.printStackTrace(); }

        RssFeed feed = null;
        try {
            feed = RssReader.read(url);
        } catch (SAXException e) { e.printStackTrace();
        } catch (IOException e) { e.printStackTrace(); }

        ArrayList<RssItem> rssItems = feed.getRssItems();
        for(RssItem rssItem : rssItems) {
            Log.i("RSS Reader", rssItem.getTitle());
            txtTitle.setText(rssItem.getTitle());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_news, menu);
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
}
/*

package com.example.rssfeed;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import nl.matshofman.saxrssreader.RssFeed;
import nl.matshofman.saxrssreader.RssItem;
import nl.matshofman.saxrssreader.RssReader;
import org.xml.sax.SAXException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {


    private TextView txtTitle;


 @SuppressLint("NewApi")

 @Override
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.activity_main);

        txtTitle = (TextView)findViewById(R.id.txtTitle);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        URL url = null;
        try {
            url = new URL("http://www.vogella.com/article.rss");
        } catch (MalformedURLException e) { e.printStackTrace(); }

        RssFeed feed = null;
        try {
            feed = RssReader.read(url);
        } catch (SAXException e) { e.printStackTrace();
        } catch (IOException e) { e.printStackTrace(); }

        ArrayList<RssItem> rssItems = feed.getRssItems();
        for(RssItem rssItem : rssItems) {
            Log.i("RSS Reader", rssItem.getTitle());
            txtTitle.setText(rssItem.getTitle());
        }




 }
}

 */