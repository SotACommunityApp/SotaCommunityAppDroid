package com.sotacommunityapp.sotacommunityapp.Radio;


/**
 * Created by James Kidd on 7/02/2015.
 */
public interface RadioInterface {
    public void setVolume(float vol);
    public void Play();
    public void Stop();
    public void addListener(RadioListener listener);
    public void removeListener(RadioListener listener);
}

