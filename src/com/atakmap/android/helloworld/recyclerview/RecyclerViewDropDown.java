package com.atakmap.android.helloworld.recyclerview;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.LayoutInflater;
import android.view.View;

import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.helloworld.plugin.R;
import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;

/**
 * A drop-down menu that demonstrates use of a RecyclerView to show a list of content
 */
public class RecyclerViewDropDown extends DropDownReceiver implements
        MapEventDispatcher.MapEventDispatchListener, View.OnClickListener {

    private final MapView _mapView;
    private final Context _plugin;
    private final View _view;
    private final RecyclerView _rView;
    private final RecyclerViewAdapter _adapter;
    private final View _vBtn, _hBtn, _gBtn;

    public RecyclerViewDropDown(MapView mapView, Context plugin) {
        super(mapView);
        _mapView = mapView;
        _plugin = plugin;

        _view = LayoutInflater.from(_plugin).inflate(R.layout.recycler_view,
                mapView, false);
        _rView = (RecyclerView) _view.findViewById(R.id.rView);
        _adapter = new RecyclerViewAdapter(_mapView, _plugin);
        _rView.setAdapter(_adapter);
        _rView.setLayoutManager(new LinearLayoutManager(_plugin,
                LinearLayoutManager.VERTICAL, false));

        _vBtn = _view.findViewById(R.id.vertical);
        _vBtn.setSelected(true);
        _vBtn.setOnClickListener(this);
        _hBtn = _view.findViewById(R.id.horizontal);
        _hBtn.setOnClickListener(this);
        _gBtn = _view.findViewById(R.id.grid);
        _gBtn.setOnClickListener(this);

        // Add map listeners
        _mapView.getMapEventDispatcher().addMapEventListener(
                MapEvent.ITEM_ADDED, this);
        _mapView.getMapEventDispatcher().addMapEventListener(
                MapEvent.ITEM_REMOVED, this);
        _mapView.getMapEventDispatcher().addMapEventListener(
                MapEvent.ITEM_REFRESH, this);
    }

    @Override
    public void disposeImpl() {
        // Remove map listeners
        _mapView.getMapEventDispatcher().removeMapEventListener(
                MapEvent.ITEM_ADDED, this);
        _mapView.getMapEventDispatcher().removeMapEventListener(
                MapEvent.ITEM_REMOVED, this);
        _mapView.getMapEventDispatcher().removeMapEventListener(
                MapEvent.ITEM_REFRESH, this);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
    }

    @Override
    public void onClick(View v) {
        // Switch user list between vertical, horizontal, and grid mode
        _vBtn.setSelected(false);
        _hBtn.setSelected(false);
        _gBtn.setSelected(false);
        LayoutManager mgr;
        if (v == _vBtn)
            mgr = new LinearLayoutManager(_plugin,
                    LinearLayoutManager.VERTICAL, false);
        else if (v == _hBtn)
            mgr = new LinearLayoutManager(_plugin,
                    LinearLayoutManager.HORIZONTAL, false);
        else if (v == _gBtn)
            mgr = new GridLayoutManager(_plugin, 3);
        else
            return;
        v.setSelected(true);
        _rView.setLayoutManager(mgr);
        _adapter.setListMode(v == _vBtn);
        _rView.getRecycledViewPool().clear();
    }

    @Override
    public void onMapEvent(MapEvent event) {
        String type = event.getType();
        MapItem item = event.getItem();
        if (item == null || !item.hasMetaValue("atakRoleType"))
            return;

        if (MapEvent.ITEM_ADDED.equals(type))
            _adapter.addItem(item);
        else if (MapEvent.ITEM_REMOVED.equals(type))
            _adapter.removeItem(item);
        _mapView.post(new Runnable() {
            @Override
            public void run() {
                if (isVisible())
                    _adapter.notifyDataSetChanged();
            }
        });
    }

    public void show() {
        showDropDown(_view, THREE_EIGHTHS_WIDTH, FULL_HEIGHT, FULL_WIDTH, THIRD_HEIGHT);
    }
}
