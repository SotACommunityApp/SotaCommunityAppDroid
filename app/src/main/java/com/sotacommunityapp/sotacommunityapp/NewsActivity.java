package com.sotacommunityapp.sotacommunityapp;

import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sotacommunityapp.sotacommunityapp.saxrssreader.ListListener;
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

        ListView rssListView = (ListView) findViewById(R.id.rssChannelListView);

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
            ArrayAdapter adapter = new ArrayAdapter<RssItem>(this,android.R.layout.simple_list_item_1, feed.getRssItems());
            rssListView.setAdapter(adapter);
            rssListView.setOnItemClickListener(new ListListener(feed.getRssItems(), this));
        } catch (SAXException e) { e.printStackTrace();
        } catch (IOException e) { e.printStackTrace(); }

        /*
        TextView txtTitle = (TextView) findViewById(R.id.rss_feed);
        ArrayList<RssItem> rssItems = feed.getRssItems();
        for(RssItem rssItem : rssItems) {
            Log.i("RSS Reader", rssItem.getTitle());
            txtTitle.append(rssItem.getTitle() + " --- ");
        }
        */
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
