package com.sotacommunityapp.sotacommunityapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.webkit.WebChromeClient;


public class CalendarActivity extends Activity {

    private WebView cWebView;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        loading = (ProgressBar) findViewById(R.id.webloading);
        loading.setMax(100);

        cWebView = (WebView) findViewById(R.id.calcWebView);
        cWebView.setWebViewClient(new MyWebViewClient());
        cWebView.setBackgroundColor(0x000000);

        WebSettings webSettings = cWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);


        cWebView.loadUrl("https://www.google.com/calendar/embed?showTitle=0&showNav=0&mode=AGENDA&height=750&wkst=1&bgcolor=%23FFFFFF&src=qiqcreqgligph3v8ls9eu43c0s%40group.calendar.google.com&color=%2342104A&src=ebnghc2i99hf1cr9bie4vqhk4k%40group.calendar.google.com&color=%23875509&src=17vdgtcc8kc73bo7fmhktqms4g%40group.calendar.google.com&color=%2323164E&src=ib3jpd9uecc87hl8j23236kmmg%40group.calendar.google.com&color=%23853104&src=lfteirbsgovs4l71ahe344gf1k%40group.calendar.google.com&color=%23AB8B00&src=7rg0qv7s2t7qrbhtn0c4nl9h1c%40group.calendar.google.com&color=%236B3304&src=1shkpg91msvnvrcpf6qr59sek4%40group.calendar.google.com&color=%232952A3&src=51qu32mh58n5jil10aral1f3m4%40group.calendar.google.com&color=%23333333&src=ecog6sgvimkf39v7vaf3i3cqd0%40group.calendar.google.com&color=%232F6213&src=bambino.ludovate%40gmail.com&color=%230F4B38&ctz=America%2FChicago");
        CalendarActivity.this.loading.setProgress(0);
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
            CalendarActivity.this.loading.setProgress(100);
            super.onPageFinished(view, url);
            cWebView = (WebView) findViewById(R.id.calcWebView);
            cWebView.setBackgroundColor(Color.WHITE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            loading.setVisibility(View.VISIBLE);
            CalendarActivity.this.loading.setProgress(0);
            super.onPageStarted(view, url, favicon);
        }
    }

    public void setValue(int progress) {
        this.loading.setProgress(progress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calendar, menu);
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
