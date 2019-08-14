
package com.atakmap.android.helloworld;

import com.atakmap.android.cot.MarkerDetailHandler;
import com.atakmap.android.maps.Marker;
import com.atakmap.coremap.cot.event.CotDetail;
import com.atakmap.coremap.cot.event.CotEvent;

public class SpecialDetailHandler implements MarkerDetailHandler {
    @Override
    public void toCotDetail(final Marker marker, final CotDetail detail) {
        CotDetail special = new CotDetail("__special");
        special.setAttribute("count",
                String.valueOf(marker.getMetaInteger("special.count", 0)));
        detail.addChild(special);
    }

    @Override
    public void toMarkerMetadata(final Marker marker, CotEvent event,
            CotDetail detail) {
        marker.setMetaInteger("special.count",
                getInt(detail.getAttribute("count"), 0));
    }

    private int getInt(String val, int fallback) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
