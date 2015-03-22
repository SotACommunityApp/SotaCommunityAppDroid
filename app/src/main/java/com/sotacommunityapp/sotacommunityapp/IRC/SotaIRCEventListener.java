package com.sotacommunityapp.sotacommunityapp.IRC;

/**
 * Created by Administrator on 22/03/2015.
 */
public interface SotaIRCEventListener {
    void onStateChanged();
    void onMessage(String msg);
}
