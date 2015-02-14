package com.sotacommunityapp.sotacommunityapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;


public class WikiActivity extends Activity {
    private WebView wWebView;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki);
        loading = (ProgressBar) findViewById(R.id.webloading);
        loading.setMax(100);

        wWebView = (WebView) findViewById(R.id.wikiWebView);
        wWebView.setWebViewClient(new MyWebViewClient());
        wWebView.setBackgroundColor(0x000000);

        WebSettings webSettings = wWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);


        wWebView.loadUrl("http://sotawiki.net/sota/Main_Page");
        WikiActivity.this.loading.setProgress(0);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            loading.setVisibility(View.GONE);
            WikiActivity.this.loading.setProgress(100);
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            loading.setVisibility(View.VISIBLE);
            WikiActivity.this.loading.setProgress(0);
            super.onPageStarted(view, url, favicon);
        }
    }

    public void setValue(int progress) {
        this.loading.setProgress(progress);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wiki, menu);
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
