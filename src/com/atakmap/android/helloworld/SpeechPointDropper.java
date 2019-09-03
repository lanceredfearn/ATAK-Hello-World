package com.atakmap.android.helloworld;

import android.content.Context;
import android.widget.Toast;

import com.atakmap.android.helloworld.plugin.R;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.user.PlacePointTool;
import com.atakmap.android.user.geocode.GeocodingTask;
import com.atakmap.coremap.conversions.CoordinateFormat;
import com.atakmap.coremap.conversions.CoordinateFormatUtilities;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoBounds;
import com.atakmap.coremap.maps.coords.GeoPoint;

import java.util.UUID;

/**
 * This class takes in speech info about dropping a point and then drops that point at that coord
 * Should accept MGRS DD DM DMS UTM AND ADDRESSES
 * Valid point types: Hostile, Friendly, Unknown, Neutral
 */
class SpeechPointDropper extends SpeechActivity {

    private final String TAG = "SPEECH_POINT_DROPPER";
    private final String CHAR_DEG = "\u00B0"; //The degree symbol

    private String[] atArray;
    private String[] aArray;
    private String[] friendlyArray;
    private String[] hostileArray;
    private String[] unknownArray;
    private String[] neutralArray;
    private String[] spotMapArray;
    private String[] wordLetterArray;
    private String[] wordNumberArray;

    private String markerType;
    private String rawCoordInfo;
    private PlacePointTool.MarkerCreator marker;
    private CoordinateFormat coordinateFormat;


    /**
     * Constructor
     *
     * @param input   - Unformatted speech input
     * @param context - The plugin context needed to load in resources.
     * @param view    - the mapview passed in from HelloWorldDropReceiver, needed for geobound stuff.
     */
    SpeechPointDropper(String input, MapView view, Context context) {
        super(view, context);
        Log.d(TAG, "=======INSIDE SpeechPointDropper Constructor======");
        loadResources();
        analyzeSpeech(input);
        startActivity();
    }

    /**
     * Example Input: Drop a hostile at taco bell
     * Gets the index of "a" and "at", gets the words between and after
     * @param input - The speech input
     */
    @Override
    void analyzeSpeech(String input) {
        int atIndex = -1;
        int aIndex = -1;
        String[] inputArr = input.split(" ");
        for(int i = 0;i < inputArr.length;i++){
            for(String s : atArray){
                if(inputArr[i].contentEquals(s))
                    atIndex = i;
            }
            for(String s: aArray){
                if(inputArr[i].contentEquals(s))
                    aIndex = i;
            }
        }
        //Now figure out the marker type(after a and before at) and destination (after at)
        if(atIndex!=-1){
            StringBuilder destinationBuilder = new StringBuilder();
            for(int i = atIndex+1;i<inputArr.length;i++){
                destinationBuilder.append(inputArr[i]);
                destinationBuilder.append(" ");
            }
            rawCoordInfo = destinationBuilder.toString().trim();
        }
        if(aIndex!=-1){
            StringBuilder markerBuilder = new StringBuilder();
            for(int i = aIndex+1;i<atIndex;i++){
                markerBuilder.append(inputArr[i]);
                markerBuilder.append(" ");
            }
            markerType = markerBuilder.toString().trim();
        }
        Log.d(TAG, "MARKER TYPE: " + this.markerType + " RAW COORD INFO: " + this.rawCoordInfo);
    }

    @Override
    void startActivity() {
        coordTypeDiscoverer();
    }

    /**
     * Sets the marker type based on what the user spoke.
     */
    private void markerValidator() {
        Log.d(TAG, "=========INSIDE MARKER VALIDATOR=========");
        boolean valid = false;
        for (String s : hostileArray) {
            if (markerType.equals(s)) {
                marker.setType("a-h-G");
                valid = true;
            }
        }
        for (String s : neutralArray) {
            if (markerType.equals(s)) {
                marker.setType("a-n-G");
            }
        }
        for (String s : friendlyArray) {
            if (markerType.equals(s)) {
                marker.setType("a-f-g");
                valid = true;
            }
        }
        for (String s : unknownArray) {
            if (markerType.equals(s)) {
                marker.setType("a-u-G");
                valid = true;
            }
        }
        for (String s : spotMapArray) {
            if (markerType.equals(s)) {
                marker.setType("b-m-p-s-m");
                valid = true;
            }
        }
        if (valid)
            Log.d(TAG, "===VALID MARKER TYPE===");
        else {
            Log.d(TAG, "===INVALID MARKER TYPE");
            marker.setType("a-f-g");
        }

    }

    /**
     * Takes in raw Coordinate info and figures out what format it is in.
     */
    private void coordTypeDiscoverer() {
        Log.d(TAG, "==========INSIDE coordTypeDiscoverer===========");
        if (rawCoordInfo.toLowerCase().contains("degrees") || rawCoordInfo.toLowerCase().contains(CHAR_DEG)) {
            coordinateFormat = CoordinateFormat.DD;
            if (rawCoordInfo.toLowerCase().contains("minutes")) {
                coordinateFormat = CoordinateFormat.DM;
                if (rawCoordInfo.toLowerCase().contains("seconds")) {
                    coordinateFormat = CoordinateFormat.DMS;
                }
            }
        } else {
            String temp = mgrsAndUTMCleaner(rawCoordInfo);
            if (temp.length() == 15) {
                coordinateFormat = CoordinateFormat.MGRS;
            } else if (temp.length() == 16) {
                coordinateFormat = CoordinateFormat.UTM;
            } else {
                coordinateFormat = CoordinateFormat.ADDRESS;
            }
        }
        Log.d(TAG, coordinateFormat.getDisplayName());
        coordFormatter();
    }

    /**
     * Formats the coords before sending into CoordinateFormatUtilities
     */
    private void coordFormatter() {
        Log.d(TAG, "=========INSIDE OF coordFormatter===========");
        //Possible entrants: North 22 degrees West 14.9139 degrees
        if (coordinateFormat.getDisplayName().contains("D") &&
                (!coordinateFormat.getDisplayName().equals(CoordinateFormat.ADDRESS.getDisplayName()))) {
            pointPlotter(CoordinateFormatUtilities.convert(degreeCleaner(rawCoordInfo), coordinateFormat));
        } else if (coordinateFormat.getDisplayName().equals("MGRS")) {

            Log.d(TAG, "====INSIDE MGRS====" + rawCoordInfo);
            pointPlotter(CoordinateFormatUtilities.convert(mgrsAndUTMCleaner(rawCoordInfo), coordinateFormat));
        } else if (coordinateFormat.getDisplayName().equals("UTM")) {
            Log.d(TAG, "====INSIDE UTM====");
            pointPlotter(CoordinateFormatUtilities.convert(mgrsAndUTMCleaner(rawCoordInfo), coordinateFormat));
        } else {
            GeoBounds gb = getView().getBounds();
            final GeocodingTask gt = new GeocodingTask(getView().getContext(),
                    gb.getSouth(), gb.getWest(), gb.getNorth(),
                    gb.getEast(), false);
            gt.setOnResultListener(new GeocodingTask.ResultListener() {
                @Override
                public void onResult() {
                    if (gt.getPoint() != null) {
                        Log.d(TAG, "GEO ADDRESS ++++++++" + gt.getHumanAddress());
                        pointPlotter(gt.getPoint());

                    } else {
                        Toast.makeText(getView().getContext(), "Address not found, Try moving map", Toast.LENGTH_LONG).show();
                    }
                }
            });
            gt.execute(rawCoordInfo);
        }
    }

    /**
     * This simply initializes the PlacePointTool.MarkerCreator, adds some attributes, then places it.
     *
     * @param coordinate - the coordinate created from the user's speech input rawCoordInfo
     */
    private void pointPlotter(GeoPoint coordinate) {
        Log.d(TAG, "========inside of point plotter======" + coordinate.toString());
        marker = new PlacePointTool.MarkerCreator(coordinate);
        markerValidator();
        marker.setUid(UUID.randomUUID().toString()).setCallsign("Speech " + markerType + " " + rawCoordInfo);
        marker.placePoint();
        Log.d(TAG,"Point placed at "+rawCoordInfo);
    }

    /**
     * The Speech to text fills with word numbers and other stuff because it thinks its a phone # or something
     * This cleans it up for coordinateFormatUtilities
     *
     * @param dirtyString -The unformatted string "44 N MP+5-136-954-726" or something like that
     * @return - the cleaned string "44NMP5136954726"
     */
    private String mgrsAndUTMCleaner(String dirtyString) {
        String cleanedString = dirtyString.replace("-", "");
        cleanedString = cleanedString.replace(" ", "");
        cleanedString = cleanedString.replace("/", "");
        cleanedString = cleanedString.replace("+", "");
        for (String s : wordNumberArray) {
            String[] wordNumberTemp = s.split(",");
            if (cleanedString.contains(wordNumberTemp[1]))
                cleanedString = cleanedString.replace(wordNumberTemp[1], wordNumberTemp[0]);
        }
        for (String s : wordLetterArray) {
            String[] wordLetterTemp = s.split(",");
            if (cleanedString.contains(wordLetterTemp[1]))
                cleanedString = cleanedString.replace(wordLetterTemp[1], wordLetterTemp[0]);
        }
        return cleanedString.toUpperCase();
    }

    /**
     * The coordinateFormatUtilities is very picky with what it accepts.
     * This method puts the user's speech input through a semi-formatter
     *
     * @param dirtyString - The users speech input
     *                    "north 20 degrees 30 minutes 10 seconds west 20 degrees 30 minutes 10 seconds"
     * @return - returns a cleaned version of the dirty string, looks like
     * "N20deg30'10" W20deg30'10""
     */
    private String degreeCleaner(String dirtyString) {
        String cleanString = dirtyString;
        cleanString = cleanString.replace("south ", "S");
        cleanString = cleanString.replace("west ", "W");
        cleanString = cleanString.replace("east ", "E");
        cleanString = cleanString.replace("north ", "N");
        if (coordinateFormat.getDisplayName().equals("DD"))
            cleanString = cleanString.replace(" degrees", CHAR_DEG);
        else {
            if (coordinateFormat.getDisplayName().contains("DM")) {
                cleanString = cleanString.replace(" degrees ", CHAR_DEG);
                if (coordinateFormat.getDisplayName().equals("DMS")) {
                    cleanString = cleanString.replace(" minutes ", "'");
                    cleanString = cleanString.replace(" seconds", "\"");
                } else {
                    cleanString = cleanString.replace(" minutes", "'");
                }
            }
        }
        cleanString = cleanString.trim();
        return cleanString;
    }

    /**
     * This loads in the arrays of words to compare the input to.
     */
    private void loadResources() {
        spotMapArray = getPluginContext().getResources().getStringArray(R.array.spotmap_array);
        neutralArray = getPluginContext().getResources().getStringArray(R.array.neutral_array);
        unknownArray = getPluginContext().getResources().getStringArray(R.array.unknown_array);
        hostileArray = getPluginContext().getResources().getStringArray(R.array.hostile_array);
        friendlyArray = getPluginContext().getResources().getStringArray(R.array.friendly_array);
        wordLetterArray = getPluginContext().getResources().getStringArray(R.array.letter_array);
        wordNumberArray = getPluginContext().getResources().getStringArray(R.array.word_number_array);
        aArray = getPluginContext().getResources().getStringArray(R.array.a_array);
        atArray = getPluginContext().getResources().getStringArray(R.array.at_array);
    }

}
