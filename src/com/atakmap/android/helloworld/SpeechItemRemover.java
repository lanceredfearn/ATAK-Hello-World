package com.atakmap.android.helloworld;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.atakmap.android.gui.EditText;
import com.atakmap.android.helloworld.plugin.R;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapCoreIntentsComponent;
import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;


/**
 * Takes in user input. Then decides what mapGroup to look in based on target.
 * Then in theory it removes that object.
 */
 class SpeechItemRemover {
    private final String TAG = "SPEECH_ITEM_REMOVER";
    private String[] callsignArray;
    private String[] drawingObjectArray;
    private String[] routeArray;
    private String[] wordNumberArray;
    private Context context;
    private MapView view;
    private MapGroup mapGroup;
    private MapItem targetItem;
    private String target;

     SpeechItemRemover(String target,final MapView view,final Context context) {
        this.view = view;
        this.context = context;
        callsignArray = context.getResources().getStringArray(R.array.callsign_array);
        drawingObjectArray = context.getResources().getStringArray(R.array.drawing_objects_array);
        routeArray = context.getResources().getStringArray(R.array.route_array);
        wordNumberArray = context.getResources().getStringArray(R.array.word_number_array);
        mapGroupDecider(target);
        targetGetter(target);
        broadcast();
    }

    /**
     * Takes in user input and decides the type of item the user is looking for.
     *
     * @param input -
     */
    private void mapGroupDecider(String input) {
        String mapGroupType = "null";
        input = input.replace("call sign", "callsign");
        String[] inputArr = input.split(" ");
            for (String s : callsignArray) {
                if (inputArr[1].equalsIgnoreCase(s)) {
                    mapGroupType = "Cursor on Target";
                    mapGroup = view.getRootGroup().findMapGroup(mapGroupType);
                }
            }
            if(mapGroupType.contentEquals("null")){
                for (String s : routeArray) {
                    if (inputArr[1].equalsIgnoreCase(s)) {
                        mapGroupType = "Route";
                        mapGroup = view.getRootGroup().findMapGroup(mapGroupType);
                    }
                }
            }
        if(mapGroupType.contentEquals("null")){
            for (String s : drawingObjectArray) {
                if (inputArr[1].equalsIgnoreCase(s)) {
                    mapGroupType = "Drawing Objects";
                    mapGroup = view.getRootGroup().findMapGroup(mapGroupType);
                }
            }
        }
        if (mapGroupType.contentEquals("null"))
            Toast.makeText(view.getContext(), "Please say the type of item before it's title", Toast.LENGTH_SHORT).show();
    }

    private void targetGetter(String target) {
        target = target.replace("call sign", "callsign");
        StringBuilder targetBuilder = new StringBuilder();
        String[] targetArr = target.split(" ");
        targetArr[0] = "";
        targetArr[1] = "";
        for (String s : targetArr) {
            for (String w : wordNumberArray) {
                String[] numberWord = w.split(",");
                s = s.replace(numberWord[1], numberWord[0]);
            }
            targetBuilder.append(s);
            targetBuilder.append(" ");
        }
        this.target = targetBuilder.toString().trim();
    }

    private void broadcast() {
        targetItem = mapGroup.deepFindItem("title", target);
        if (targetItem != null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
            alert.setTitle(context.getResources().getString(R.string.Remove_item_warn));
            alert.setNegativeButton(context.getResources().getString(R.string.cancel_btn), null);
            alert.setPositiveButton(context.getResources().getString(R.string.confirm_btn), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Long serialID = targetItem.getSerialId();
                    AtakBroadcast.getInstance().sendBroadcast(new Intent()
                            .setAction(MapCoreIntentsComponent.ACTION_DELETE_ITEM)
                            .putExtra("serialId", serialID));

                }
            });
            alert.show();

         }else{
           final EditText input = new EditText(view.getContext());
            input.setSingleLine(true);
            input.setText(target);
            input.selectAll();
            AlertDialog.Builder manualInput = new AlertDialog.Builder(view.getContext());
            manualInput.setTitle(context.getResources().getString(R.string.Manual_mode));
            manualInput.setView(input);
            manualInput.setNegativeButton(context.getResources().getString(R.string.cancel_btn),null);
            manualInput.setPositiveButton(context.getResources().getString(R.string.confirm_btn), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                        String newName = input.getText().toString().trim();
                        targetItem = mapGroup.deepFindItem("title", newName);
                        if(targetItem!=null){
                            Long serialID = targetItem.getSerialId();
                            AtakBroadcast.getInstance().sendBroadcast(new Intent()
                                    .setAction(MapCoreIntentsComponent.ACTION_DELETE_ITEM)
                                    .putExtra("serialId", serialID));
                        }
                        else{
                            Toast.makeText(view.getContext(),"Item not found",Toast.LENGTH_SHORT).show();
                        }


                }
            });
            AlertDialog alert = manualInput.create();
            alert.show();
        }
    }
}
