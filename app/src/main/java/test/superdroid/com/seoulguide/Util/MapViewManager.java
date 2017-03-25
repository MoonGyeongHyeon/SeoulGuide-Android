package test.superdroid.com.seoulguide.Util;

import android.app.Activity;
import android.widget.FrameLayout;

import net.daum.mf.map.api.MapView;

public class MapViewManager {
    private static MapView mMapView;
    private static FrameLayout mMapLayout;
    // 다음 지도 API를 위한 key.
    public static final String MAP_KEY = "17fd056513f09a6ebe80bdb559e2285d";

    public static MapView getMapView() {
        return mMapView;
    }

    public static MapView getMapView(Activity activity, FrameLayout mapLayout) {
        if (mMapView == null) {
            mMapView = new MapView(activity);
            mMapView.setDaumMapApiKey(MAP_KEY);
        }

        if(mMapLayout != null) {
            mMapLayout.removeAllViews();
        }

        mMapLayout = mapLayout;
        mMapLayout.addView(mMapView);

        mMapView.clearFocus();

        return mMapView;
    }
}
