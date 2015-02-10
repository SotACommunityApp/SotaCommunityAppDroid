package com.sotacommunityapp.sotacommunityapp.saxrssreader;

/**
 * Created by umbrae on 2/9/2015.
 */
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.sotacommunityapp.sotacommunityapp.saxrssreader.RssItem;

public class ListListener implements OnItemClickListener {

    List<RssItem> listItems;
    Activity activity;

    public ListListener(List<RssItem> aListItems, Activity anActivity) {
        listItems = aListItems;
        activity  = anActivity;
    }

    public void onItemClick(AdapterView parent, View view, int pos, long id) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(listItems.get(pos).getLink()));
        activity.startActivity(i);
    }


}

