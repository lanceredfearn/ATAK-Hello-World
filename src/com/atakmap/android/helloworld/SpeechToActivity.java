package com.atakmap.android.helloworld;
//Tutorial Used: https://www.androidhive.info/2014/07/android-speech-to-text-tutorial/

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Locale;

import android.content.ActivityNotFoundException;
import android.os.Debug;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import com.atakmap.android.helloworld.plugin.R;
import com.atakmap.coremap.log.Log;


public class SpeechToActivity extends Activity {
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private static final String NAVIGATE_SPEECH_INFO = "com.atackmap.android.helloworld.NAVIGATESPEECHINFO";
    private static final String TAG = "SpeechToActivity";


    protected void onCreate(Bundle savedInstanceState) {
        //Debug.waitForDebugger();
        super.onCreate(savedInstanceState);
        promptSpeechInput();


    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt_Activity));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    activityDecider(result);
                } else {
                    finish();
                }
                break;
            }
            default:
                finish();

        }
    }

    //Decides what to do based on first words of speech
    //Right now it just uses "Navigate to"
    protected void activityDecider(ArrayList<String> speech) {
        String[] activity = speech.get(0).split(" ");
        if (activity[0].equalsIgnoreCase("navigate")) {
            navigateTo(activity);
        } else {
            finish();
        }


    }

    /*Removes "Navigate To" from speech txt, then sends address back to HelloWorldDropDownReceiver*/
    protected void navigateTo(String[] speechArr) {
        speechArr[0] = "";
        speechArr[1] = "";
        StringBuilder addressBuilder = new StringBuilder();
        for (String s : speechArr) {
            addressBuilder.append(s);
            addressBuilder.append(" ");
        }
        String address = addressBuilder.toString();
        Intent returnIntent = new Intent(NAVIGATE_SPEECH_INFO);
        returnIntent.putExtra("destination", address);
        sendBroadcast(returnIntent);
        finish();
    }

    public interface SpeechDataReceiver {
        void onSpeechDataReceived(String s);
    }

    /**
     * Broadcast Receiver that is responsible for getting the data back to the
     * plugin.
     */
    static class SpeechDataListener extends BroadcastReceiver {
        private boolean registered = false;
        private SpeechToActivity.SpeechDataReceiver sdra = null;

        synchronized public void register(Context context,
                                          SpeechToActivity.SpeechDataReceiver sdra) {
            if (!registered)
                context.registerReceiver(this, new IntentFilter(NAVIGATE_SPEECH_INFO));

            this.sdra = sdra;
            registered = true;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized (this) {
                try {
                    String s = intent.getExtras().getString("destination");
                    if (s != null && sdra != null)
                        sdra.onSpeechDataReceived(s);
                } catch (Exception e) {
                }
                if (registered) {
                    context.unregisterReceiver(this);
                    registered = false;
                }
            }
        }
    }
}

