package com.sotacommunityapp.sotacommunityapp.IRC;

/**
 * Created by Administrator on 22/03/2015.
 */
public interface SotaIRCInterface {
    void sendMessage(String msg);
    void ConnectAsync();
    void Dissconnect();
}
