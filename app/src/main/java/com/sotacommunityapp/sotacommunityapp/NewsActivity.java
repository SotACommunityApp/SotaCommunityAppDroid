package com.sotacommunityapp.sotacommunityapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
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
    String rssFolder = "/sotacommapp";
    String rssFile = rssFolder + "/news.xml";
    String rssUrl = "http://avatarsportal.com/feed";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        waitSpinner = new ProgressDialog(this);
        waitSpinner.setMessage("Fetching News...");
        waitSpinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        listRSSItems = (ListView) findViewById(R.id.rssChannelListView);

        new GetRSSFeed().execute();
    }

    private class GetRSSFeed extends AsyncTask<Void, Void, ArrayList<RssItem>> {

        @Override
        protected ArrayList<RssItem> doInBackground(Void... params) {
            ArrayList<RssItem> rssItems = null;
            try {
                // TODO: Pull from local and Only fetch if hour old

                String rssPath = Environment.getExternalStorageDirectory().getPath() + rssFile;
                File folder = new File(Environment.getExternalStorageDirectory() + rssFolder);
                if (!folder.exists()) {
                    folder.mkdir();
                }
                File fRss = new File(rssPath);
                RssFeed feed = null;

                if(fRss.exists() && fRss.lastModified() > new Date().getTime() - 1 * DateUtils.HOUR_IN_MILLIS) {
                    InputStream rssIs = new FileInputStream(rssPath);
                    feed = RssReader.read(rssIs);
                } else {
                    Handler h = new Handler(Looper.getMainLooper());
                    h.post(new Runnable() {
                        public void run() { waitSpinner.show(); }
                    });

                    String rssResult = refreshFeed();
                    if (rssResult == "Success") {
                        InputStream rssIs = new FileInputStream(rssPath);
                        feed = RssReader.read(rssIs);
                    } else {
                        throw new Exception();
                    }
                }
                rssItems = feed.getRssItems();

            } catch (MalformedURLException e) {
                Log.e("RSS Reader", "Malformed URL");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("RSS Reader", "IOException");
                e.printStackTrace();
            } catch (SAXException e) {
                Log.e("RSS Reader", "SAXException");
                e.printStackTrace();
            } catch (Exception e) {
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

        public String refreshFeed()  {
                String errOut = "Fail";

                try {
                    URL url = new URL(rssUrl);

                    //create the new connection
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.connect();

                    //set the path where we want to save the file
                    //in this case, going to save it on the root directory of the
                    //sd card.
                    File SDCardRoot = Environment.getExternalStorageDirectory();
                    //create a new file, specifying the path, and the filename
                    //which we want to save the file as.
                    File file = new File(SDCardRoot, rssFile);

                    //this will be used to write the downloaded data into the file we created
                    FileOutputStream fileOutput = new FileOutputStream(file);

                    //this will be used in reading the data from the internet
                    InputStream inputStream = urlConnection.getInputStream();

                    //this is the total size of the file
                    int totalSize = urlConnection.getContentLength();
                    waitSpinner.setMax(totalSize);

                    //variable to store total downloaded bytes
                    int downloadedSize = 0;

                    //create a buffer...
                    byte[] buffer = new byte[1024];
                    int bufferLength = 0; //used to store a temporary size of the buffer

                    //now, read through the input buffer and write the contents to the file
                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        //add the data in the buffer to the file in the file output stream (the file on the sd card
                        fileOutput.write(buffer, 0, bufferLength);
                        //add up the size so we know how much is downloaded
                        downloadedSize += bufferLength;

                    }
                    //close the output stream when done
                    fileOutput.close();
                    Log.e("RSS Fetch", "Feed Saved");

                    errOut = "Success";

                } catch (MalformedURLException e) {
                    Log.e("RSS Fetch", "Malformed URL");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e("RSS Fetch", "IOException");
                    e.printStackTrace();
                }
                return errOut;
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
