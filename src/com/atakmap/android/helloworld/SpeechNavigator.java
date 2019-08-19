package com.atakmap.android.helloworld;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.Toast;

import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.PointMapItem;
import com.atakmap.android.routes.Route;
import com.atakmap.android.routes.RouteMapReceiver;
import com.atakmap.android.user.geocode.GeocodingTask;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoBounds;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.android.routes.RouteMapReceiver;

import java.util.UUID;


 class SpeechNavigator {
    private static final String TAG = "SPEECH_NAVIGATOR";

    private String geoAddress;
    private GeoPoint destination;
    private GeoPoint source;
    private MapView view;
    private Context _context;

     SpeechNavigator(MapView mapview) {
        view = mapview;
        _context = mapview.getContext();
        source = view.getSelfMarker().getPoint();
    }

    /**
     * Takes address and looks it up with GeocodingTask. Stores info found in the class
     * Then sends intent out to start navigation w/ the generated route.
     *
     * @param s - the address that gets looked up from SpeechToActivity.
     *          The GeoBounds is the box of the map view. If an address isn't inside the GeoBounds it wont find an address
     */
     void startNavigation(String s) {
        GeoBounds gb = view.getBounds();
        final GeocodingTask gt = new GeocodingTask(_context,
                gb.getSouth(), gb.getWest(), gb.getNorth(),
                gb.getEast(), false);
        gt.setOnResultListener(new GeocodingTask.ResultListener() {
            @Override
            public void onResult() {
                if (gt.getPoint() != null) {
                    Log.d(TAG, "========INSIDE ON RESULT====== " + gt.getPoint().toString());
                    destination = gt.getPoint();
                    geoAddress = gt.getHumanAddress();
                    Log.d(TAG, "GEO ADDRESS ++++++++" + geoAddress);
                    RouteMapReceiver.promptPlanRoute(view, source, destination, "Route to " + geoAddress, Color.RED);
                 //   MapGroup _mapGroup = view.getRootGroup().findMapGroup("Route");
                //    Log.d(TAG, _mapGroup.deepFindClosestItem(source).toString());
                    //Intent startNavIntent = new Intent(RouteMapReceiver.START_NAV)
                   //         .putExtra("RouteUID", );
                   // AtakBroadcast.getInstance().sendBroadcast(startNavIntent);

                } else {
                    Toast.makeText(_context, "Address not found, Try moving map", Toast.LENGTH_LONG).show();
                }


            }
        });
        gt.execute(s);
    }

}
