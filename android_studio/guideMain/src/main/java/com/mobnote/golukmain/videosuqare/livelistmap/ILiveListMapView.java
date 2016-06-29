package com.mobnote.golukmain.videosuqare.livelistmap;

import android.os.Bundle;
import android.view.View;

import cn.com.mobnote.module.location.ILocationFn;

/**
 * Created by leege100 on 16/6/28.
 */
public interface ILiveListMapView extends ILocationFn {

    public View getView();

    public void onCreate();

    public void onResume();

    public void onPause();

    public void onStop();

    public void onDestroy();

    public void onLowMemory();

    public void onSaveInstanceState(Bundle outState);

    public void initMap();

    public void pointDataCallback(int success, Object obj);

    public void downloadBubbleImageCallBack(int success, Object obj);
}
