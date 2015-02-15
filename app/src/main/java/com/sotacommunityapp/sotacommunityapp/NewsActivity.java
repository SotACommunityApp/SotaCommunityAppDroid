package com.sotacommunityapp.sotacommunityapp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import org.xml.sax.SAXException;

import com.sotacommunityapp.sotacommunityapp.saxrssreader.ListListener;
import com.sotacommunityapp.sotacommunityapp.saxrssreader.RssFeed;
import com.sotacommunityapp.sotacommunityapp.saxrssreader.RssItem;
import com.sotacommunityapp.sotacommunityapp.saxrssreader.RssReader;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

                // TODO: Pull from local and Only fetch if hour old
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

                ArrayList<NewsObj> itemArray = new ArrayList<NewsObj>();

                int i = 0;
                for (RssItem rssItem : items) {
                    Log.i("RSS Reader", rssItem.getTitle());

                    NewsObj thisItem = new NewsObj(rssItem.getTitle(), rssItem.getPubDate(), rssItem.getCreator());
                    itemArray.add(i, thisItem);
                    i++;
                }

                listRSSItems.setAdapter(new NewsAdapter(
                        getApplicationContext(), itemArray
                ));
                listRSSItems.setOnItemClickListener(new ListListener(items, currentActivity));
            } else {
                Toast.makeText(getApplicationContext(), "No RSS items found",
                        Toast.LENGTH_SHORT).show();
            }
            waitSpinner.hide();
            waitSpinner.dismiss();
        }

    }

    public class NewsAdapter extends ArrayAdapter<NewsObj> {
        public NewsAdapter(Context context, ArrayList<NewsObj> news) {
            super(context, 0, news);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            NewsObj nObj = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem, parent, false);
            }
            // Lookup view for data population
            TextView tvTitle = (TextView) convertView.findViewById(R.id.textTitle);
            TextView tvTag = (TextView) convertView.findViewById(R.id.textTag);

            // Populate the data into the template view using the data object
            java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("MMM dd, yyyy");
            String tagDate = simpleDateFormat.format(nObj.published);
            String tagText = nObj.author + " (" + tagDate + ")";
            tvTitle.setText(nObj.title);
            tvTag.setText(tagText);
            // Return the completed view to render on screen
            return convertView;
        }
    }
    public class NewsObj {
        public String title;
        public Date published;
        public String author;

        public NewsObj(String title, Date published, String author) {
            this.title = title;
            this.published = published;
            this.author = author;

        }
    }

}
