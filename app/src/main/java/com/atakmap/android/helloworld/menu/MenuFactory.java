package com.atakmap.android.helloworld.menu;

import android.content.Context;
import android.graphics.Color;

import com.atakmap.android.action.MapAction;
import com.atakmap.android.maps.MapDataRef;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.assets.MapAssets;
import com.atakmap.android.menu.MapMenuButtonWidget;
import com.atakmap.android.menu.MapMenuFactory;
import com.atakmap.android.menu.MapMenuWidget;
import com.atakmap.android.menu.MenuMapAdapter;
import com.atakmap.android.menu.MenuResourceFactory;
import com.atakmap.android.widgets.AbstractButtonWidget;
import com.atakmap.android.widgets.WidgetBackground;
import com.atakmap.android.widgets.WidgetIcon;

import java.io.IOException;

/*
Nonsensical logic to demonstrate the creation of MapMenuWidgets,
MapMenuButtonWidgets, MapActions, and submenus using a custom
MapMenuFactory along with the MenuResourceFactory.
 */
public class MenuFactory implements MapMenuFactory {

    final private Context appContext;
    final private MenuResourceFactory resourceFactory;

    final static private String[] iconUris = {
            "asset:///icons/blast_rings.png",
            "asset:///icons/details.png",
            "asset:///icons/redlight.png",
            "asset:///icons/redtriangle.png"
    };

    public MenuFactory () {
        final MapView mapView = MapView.getMapView();
        appContext = mapView.getContext();
        // using application, not plugin, assets, hence the application context
        final MapAssets mapAssets = new MapAssets(appContext);
        final MenuMapAdapter adapter = new MenuMapAdapter();
        try {
            adapter.loadMenuFilters(mapAssets, "filters/menu_filters.xml");
        } catch (IOException e) {
            // do something better here
        }
        resourceFactory =
                new MenuResourceFactory(mapView, mapView.getMapData(), mapAssets, adapter);
    }

    @Override
    public MapMenuWidget create(MapItem mapItem) {
        MapMenuWidget menuWidget = null;
        final String type = mapItem.getType();
        if (type.contains("a-f")) {
            menuWidget = createFriendly();
        } else if (type.contains("a-h")) {
            menuWidget = createHostile();
        } // else fall through and return null
        return menuWidget;
    }

    WidgetBackground createDarkWidget() {
        WidgetBackground.Builder builder = new WidgetBackground.Builder();
        return builder
                .setColor(0, Color.parseColor("#ff383838"))
                .setColor(AbstractButtonWidget.STATE_DISABLED,
                        Color.parseColor("#7f7f7f7f"))
                .setColor(AbstractButtonWidget.STATE_DISABLED
                                | AbstractButtonWidget.STATE_PRESSED,
                        Color.parseColor("#7f7f7f7f"))
                .setColor(AbstractButtonWidget.STATE_PRESSED,
                        Color.parseColor("#ff7fff7f"))
                .setColor(AbstractButtonWidget.STATE_SELECTED,
                        Color.parseColor("#ff7fffff"))
                .setColor(AbstractButtonWidget.STATE_SELECTED |
                                AbstractButtonWidget.STATE_PRESSED,
                        Color.parseColor("#ff7fff7f"))
                .build();
    }

    /*
    Arbitrary composition of MapMenuWidget for demonstration.
    Demonstrates start and coverage angles along with submenus
     */
    private MapMenuWidget createHostile() {
        final MapMenuWidget menuWidget = new MapMenuWidget();
        // arbitrary composition
        final MapMenuWidget submenuWidget = new MapMenuWidget();
        for (int index = 2, max = iconUris.length;
             max > index; ++index) {
            final MapMenuButtonWidget buttonWidget =
                    createButton(iconUris[index]);
            submenuWidget.addWidget(buttonWidget);
            // weight the first button span 1.5 times the other buttons
            if (2 == index) {
                buttonWidget.setLayoutWeight(1.5f);
            }
        }
        for (int index = 0, max = 3; max > index; ++index) {
            final MapMenuButtonWidget buttonWidget = 1 == index ?
                    createButton(iconUris[index], submenuWidget) :
                    createButton(iconUris[index]);
            menuWidget.addWidget(buttonWidget);
        }
        // parenting
        submenuWidget.setParent(menuWidget);
        // locate first button -55 degrees
        menuWidget.setStartAngle(-55f);
        // only go 330 degrees
        menuWidget.setCoveredAngle(330f);

        return menuWidget;
    }

    /*
    Arbitrary composition of MapMenuWidget for demonstration.
    Demonstrates a custom background
     */
    private MapMenuWidget createFriendly() {
        MapMenuWidget menuWidget = new MapMenuWidget();
        for (String uris : iconUris) {
            final MapMenuButtonWidget buttonWidget =
                    createButton(uris);
            buttonWidget.setBackground(createDarkWidget());
            menuWidget.addWidget(buttonWidget);
        }

        return menuWidget;
    }

    private WidgetIcon createIcon(String asset) {
        MapDataRef mapDataRef = MapDataRef.parseUri(asset);
        final WidgetIcon.Builder builder = new WidgetIcon.Builder();
        return builder
                .setImageRef(0, mapDataRef)
                .setAnchor(16, 16)
                .setSize(32, 32)
                .build();
    }

    /*
    Uses default factory to create action from asset
     */
    private MapMenuButtonWidget createButton(String iconUri,
                                              MapMenuWidget submenu) {
        MapMenuButtonWidget buttonWidget =
                new MapMenuButtonWidget(appContext);
        final MapAction action = resourceFactory
                .resolveAction("actions/cancel.xml");
        buttonWidget.setOnClickAction(action);
        buttonWidget.setIcon(createIcon(iconUri));
        buttonWidget.setSubmenuWidget(submenu);
        return buttonWidget;
    }

    /*
    Same as prior button method, but uses a MapAction created from scratch
     */
    private MapMenuButtonWidget createButton(String iconUri) {
        MapMenuButtonWidget buttonWidget =
                new MapMenuButtonWidget(appContext);
        buttonWidget.setOnClickAction(new CancelAction());
        buttonWidget.setIcon(createIcon(iconUri));
        return buttonWidget;
    }
}
