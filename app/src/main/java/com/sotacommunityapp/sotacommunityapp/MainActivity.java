package com.sotacommunityapp.sotacommunityapp;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View btn1 = findViewById(R.id.button1);
        View btn2 = findViewById(R.id.button2);
        View btn3 = findViewById(R.id.button3);
        View btn4 = findViewById(R.id.button4);
        View btn5 = findViewById(R.id.button5);
        View btn6 = findViewById(R.id.button6);

        btn1.setBackgroundResource(R.drawable.clockpunk_button1);
        btn2.setBackgroundResource(R.drawable.clockpunk_button2);
        btn3.setBackgroundResource(R.drawable.clockpunk_button3);
        btn4.setBackgroundResource(R.drawable.clockpunk_button4);
        btn5.setBackgroundResource(R.drawable.clockpunk_button5);
        btn6.setBackgroundResource(R.drawable.clockpunk_button6);

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void newsButtonClick(View view) {
        startActivity(new Intent(MainActivity.this, NewsActivity.class));
        view.setBackgroundResource(R.drawable.clockpunk_button1_tap);
    }
    public void radioButtonClick(View view) {
        startActivity(new Intent(MainActivity.this, RadioActivity.class));
        view.setBackgroundResource(R.drawable.clockpunk_button2_tap);
    }
    public void ircButtonClick(View view) {
        startActivity(new Intent(MainActivity.this, IrcActivity.class));
        view.setBackgroundResource(R.drawable.clockpunk_button3_tap);
    }
    public void wikiButtonClick(View view) {
        startActivity(new Intent(MainActivity.this, WikiActivity.class));
        view.setBackgroundResource(R.drawable.clockpunk_button4_tap);
    }
    public void calendarButtonClick(View view) {
        startActivity(new Intent(MainActivity.this, CalendarActivity.class));
        view.setBackgroundResource(R.drawable.clockpunk_button5_tap);
    }
    public void mapButtonClick(View view) {
        startActivity(new Intent(MainActivity.this, MapActivity.class));
        view.setBackgroundResource(R.drawable.clockpunk_button6_tap);
    }



}
