package com.atakmap.android.helloworld;

import android.content.Context;

import com.atakmap.android.maps.MapView;

/**
 * Interface to be used by Speech activities.
 * All basically need to: -find the string title of what they're looking for and
 *                        -broadcast that target to some receiver
 */
abstract class SpeechActivity {
  private  MapView view;
  private Context pluginContext;

    public SpeechActivity(MapView view, Context pluginContext) {
        this.view = view;
        this.pluginContext = pluginContext;
    }

    /**
     * This will analyze the speech String, taking out data and putting it where it needs to be.
     * @param input - The speech input
     */
    abstract void analyzeSpeech(String input);

    /**
     * This is where the activity will be triggered by the speech class.
     * Whether it be ATAKBroadcast or something else.
     */
    abstract void startActivity();

     MapView getView() {
        return view;
    }

     Context getPluginContext() {
        return pluginContext;
    }

}
