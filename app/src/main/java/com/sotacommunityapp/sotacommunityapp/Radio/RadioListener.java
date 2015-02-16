package com.sotacommunityapp.sotacommunityapp.Radio;

public interface RadioListener {
    public void onTrackTitleChanged(String title, String artist);
    public void onRadioChanged(boolean state);
}
