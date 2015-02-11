package com.sotacommunityapp.sotacommunityapp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.xml.sax.SAXException;

import com.sotacommunityapp.sotacommunityapp.saxrssreader.ListListener;
import com.sotacommunityapp.sotacommunityapp.saxrssreader.RssFeed;
import com.sotacommunityapp.sotacommunityapp.saxrssreader.RssItem;
import com.sotacommunityapp.sotacommunityapp.saxrssreader.RssReader;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NewsActivity extends Activity {
    Activity currentActivity = this;
    ListView listRSSItems;
    ProgressDialog waitSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        waitSpinner = new ProgressDialog(this);
        waitSpinner.setMessage("Fetching News...");
        waitSpinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        waitSpinner.show();

        listRSSItems = (ListView) findViewById(R.id.rssChannelListView);
        new GetRSSFeed().execute();
    }

    private class GetRSSFeed extends AsyncTask<Void, Void, ArrayList<RssItem>> {

        @Override
        protected ArrayList<RssItem> doInBackground(Void... params) {
            ArrayList<RssItem> rssItems = null;
            try {

                URL url = new URL(
                        "http://avatarsportal.com/feed");
                RssFeed feed = RssReader.read(url);
                rssItems = feed.getRssItems();

            } catch (MalformedURLException e) {
                Log.e("RSS Reader", "Malformed URL");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("RSS Reader", "Malformed URL");
                e.printStackTrace();
            } catch (SAXException e) {
                Log.e("RSS Reader", "Malformed URL");
                e.printStackTrace();
            }
            return rssItems;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<RssItem> items) {
            if (items != null) {
                String[] itemTitlesArray = new String[items.size()];
                int i = 0;
                for (RssItem rssItem : items) {
                    Log.i("RSS Reader", rssItem.getTitle());
                    itemTitlesArray[i] = rssItem.getTitle();
                    i++;
                }

                listRSSItems.setAdapter(new ArrayAdapter<String>(
                        getApplicationContext(), R.layout.listitem,
                        R.id.textTitle, itemTitlesArray));
                listRSSItems.setOnItemClickListener(new ListListener(items, currentActivity));
            } else {
                Toast.makeText(getApplicationContext(), "No RSS items found",
                        Toast.LENGTH_SHORT).show();
            }
            waitSpinner.hide();
        }

    }

}
