package com.mobnote.t1sp.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.mobnote.application.GolukApplication;

import net.sf.marineapi.bean.GPSData;

import java.util.List;

public abstract class MapTrackView extends RelativeLayout {

    protected MapListener mMapListener;
    Context mContext;
    public static int GPS_LOCAL_MP4 = 0;
    public static int GPS_REMOTE_MP4 = 1;
    public static int GPS_REMOTE_MP4_TS = 2;
    // 是否画起点终点Icon
    protected boolean mDrawStartAndEndIcon = true;

    public MapTrackView(Context context) {
        super(context);
        mContext = context;
    }

    public MapTrackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

    }

    public MapTrackView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;

    }

    public static MapTrackView create(Context context) {
        MapTrackView view;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        if (GolukApplication.getInstance().isMainland()) {
            view = new BaiduMapTrackView(context);
        } else {
            view = new GoogleMapTrackView(context);
        }

        view.setLayoutParams(lp);
        return view;
    }

    public interface MapListener {
        void onPreDrawLineTrack();

        void onAfterDrawLineTrack(List<GPSData> list);
    }

    public void setMapListener(MapListener l) {
        mMapListener = l;
    }

    public void drawTrackCar(GPSData data) {
        drawTrackCar(data, false);
    }

    public interface SnapshotReadyCallback {
        void onSnapshotReady(final Bitmap bitmap);
    }

    public void setDrawStartAndEndIcon(boolean needDraw) {
        mDrawStartAndEndIcon = needDraw;
    }

    public abstract boolean isMapAvailable();

    public abstract void setGPSDataFromType(int value);

    public abstract void drawTrackLine(List<GPSData> list);

    public abstract void drawOnlyOnePoint(GPSData gpsData);

    public abstract void drawTrackCar(GPSData data, boolean center);

    public abstract void drawTrackCar(GPSData data, boolean center, boolean connectLine);

    public abstract void clear();

    public abstract void setShowCarInfo(boolean show);

    public abstract void setShowCarInfoTime(boolean show);

    public abstract void resetFirstCarLoc();

    public abstract void setLocationEnabled(boolean enable);

    public abstract void onCreate(Bundle savedInstanceState);

    public abstract void onPause();

    public abstract void onResume();

    public abstract void onDestroy();

}
