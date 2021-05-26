package com.mobnote.golukmain.videosuqare.livelistmap;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.videosuqare.VideoCategoryActivity;
import com.mobnote.map.GoogleMapTools;
import com.mobnote.map.LngLat;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.location.GolukPosition;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * Created by leege100 on 16/6/28.
 */
public class LiveListGoogleMapView implements ILiveListMapView ,OnMapReadyCallback ,View.OnClickListener{

    private Context mContext = null;
    private RelativeLayout mRootLayout = null;
    private RelativeLayout indexMapLayout = null;
    /** Google地图 */
    private MapView mMapView = null;
    private GoogleMap mGoogleMap = null;

    /** 定时请求直播点时间 */
    public static final int mTiming = 1 * 60 * 1000;
    /** 是否首次定位 */
    private boolean isFirstLoc = true;
    private GoogleMapTools mGoogleMapTools = null;

    /** 我的位置按钮 */
    private Button mMapLocationBtn = null;

    private VideoCategoryActivity ma;

    /** 控制离开页面不自动请求大头针数据 */
    private boolean isCurrent = true;

    /** 首页handler用来接收消息,更新UI */
    public Handler mGoogleHandler = null;

    Marker mAudienceMarker;
    public LiveListGoogleMapView(Context context, Bundle saveInstance) {
        mContext = context;

        mRootLayout = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.baidu_map, null);

        ma = (VideoCategoryActivity) mContext;
        ma.mApp.addLocationListener("main", this);

        initMap(saveInstance);
    }

    @Override
    public View getView() {
        return mRootLayout;
    }

    @Override
    public void onResume() {

        isCurrent = true;

        boolean b = mGoogleHandler.hasMessages(2);
        if (!b) {
            Message msg = new Message();
            msg.what = 2;
            mGoogleHandler.sendMessageDelayed(msg, mTiming);
        }
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        if (null != mMapView) {
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if(mMapView != null){
            mMapView.onPause();
        }
    }

    @Override
    public void onDestroy() {

        ma.mApp.removeLocationListener("main");
        // TODO 先不释放，释放会引起界面退出卡死的问题
        // if (null != mMapView) {
        // mMapView.onDestroy();
        // }
        //释放资源
        if (mGoogleMapTools != null){
            mGoogleMapTools.release();
            mGoogleMapTools = null;
        }
        if (mGoogleHandler != null){
            mGoogleHandler.removeCallbacksAndMessages(null);
            mGoogleHandler = null;
        }
    }

    @Override
    public void onLowMemory() {
        if(mMapView != null){
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(outState != null){
            mMapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void initMap(Bundle saveInstance) {

        indexMapLayout = (RelativeLayout) mRootLayout.findViewById(R.id.index_map_layout);

        // 地图我的位置按钮
        mMapLocationBtn = (Button) mRootLayout.findViewById(R.id.map_location_btn);
        // 注册事件
        mMapLocationBtn.setOnClickListener(this);

        GoogleMapOptions options = new GoogleMapOptions();
        options.rotateGesturesEnabled(false); // 不允许手势
        options.zoomGesturesEnabled(true);
        mMapView = new MapView(mContext, options);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        indexMapLayout.addView(mMapView, 0, params);
        mMapView.setClickable(true);
        mMapView.onCreate(saveInstance);
        mMapView.getMapAsync(this);

        // 更新UI handler
        mGoogleHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int what = msg.what;
                switch (what) {
                    case 2:
                        // 5分钟更新一次大头针数据
                        if(null != ma && null != ma.mApp && null != ma.mApp.mGoluk) {
                            ma.mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
                                    IPageNotifyFn.PageType_GetPinData, "");
                        }
                        break;
                    case 99:
                        // 隐藏气泡,大头针
                        //mGoogleMap..mapStatusChange();
                        break;
                }
            }
        };
    }

    @Override
    public void pointDataCallback(int success, Object obj) {
        if (1 == success) {
            String str = (String) obj;
            GolukDebugUtils.e("", "jyf----VideoCategoryActivity----LiveListBaiduMapView--pointDataCallback ----obj: "
                    + (String) obj);
            try {
                JSONObject json = new JSONObject(str);
                // 请求成功
                JSONArray list = json.getJSONArray("info");
                if(mGoogleMapTools != null){
                    mGoogleMapTools.AddMapPoint(list);
                }
            } catch (Exception e) {

            }
        } else {
            GolukDebugUtils.e("", "请求大头针数据错误");
        }

        if (isCurrent) {
            // 不管大头针数据请求成功/失败,都需要定时5分钟请求下一次数据
            boolean b = mGoogleHandler.hasMessages(2);
            if (!b) {
                Message msg = new Message();
                msg.what = 2;
                mGoogleHandler.sendMessageDelayed(msg, mTiming);
            }
        }
    }

    @Override
    public void downloadBubbleImageCallBack(int success, Object obj) {
        if (1 == success) {
            // 更新在线视频图片
            String imgJson = (String) obj;
            // String imgJson = "{\"path\":\"fs1:/Cache/test11.png\"}";
            GolukDebugUtils.e("", "下载气泡图片完成downloadBubbleImageCallBack:" + imgJson);
            if(mGoogleMapTools != null){
                mGoogleMapTools.bubbleImageDownload(imgJson);
            }
        } else {
            GolukUtils.showToast(mContext, mContext.getString(R.string.str_bubble_image_download_fail));
        }
    }

    @Override
    public void LocationCallBack(String gpsJson) {
        GolukPosition location = JsonUtil.parseLocatoinJson(gpsJson);
        if (location == null || mMapView == null || mGoogleMap == null) {
            return;
        }

        // 移动地图中心点
        LatLng myPoint = new LatLng(location.rawLat, location.rawLon);
        if(mAudienceMarker == null){
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location);
            mAudienceMarker = mGoogleMap.addMarker(
                    new MarkerOptions()
                            .position(myPoint)
                            .icon(bitmap));
        }else{
            mAudienceMarker.setPosition(myPoint);
        }

        GolukDebugUtils.e("LocationCallBack","locationin_1");
        // 移动了地图,第一次不改变地图中心点位置
        if (isFirstLoc) {
            isFirstLoc = false;
            if(mGoogleMap != null){
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(LngLat.lat, LngLat.lng)));
            }
        }

        // 保存经纬度
        LngLat.lng = location.rawLon;
        LngLat.lat = location.rawLat;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // 获取map对象
        mGoogleMap = googleMap;
        mGoogleMapTools = new GoogleMapTools(mContext, GolukApplication.getInstance(), mGoogleMap, "Main");

        // 地图加载完成事件
        mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                // 地图加载完成,请求大头针数据
                GolukDebugUtils.e("", "jyf----VideoCategoryActivity----LiveListBaiduMapView--onMapLoaded ----11111");
                ma.mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
                        IPageNotifyFn.PageType_GetPinData, "");
            }
        });

//        mGoogleMap.OnMyLocationChangeListener(new GoogleMap.OnMapStatusChangeListener() {
//            @Override
//            public void onMapStatusChangeStart(MapStatus arg0) {
//                // 隐藏气泡,大头针
//                mBaiduMapManage.mapStatusChange();
//                // 移动了地图,第一次不改变地图中心点位置
//                isFirstLoc = false;
//
//                GolukDebugUtils.e("", "jyf----VideoCategoryActivity----LiveListBaiduMapView--onMapStatusChangeStart ----: ");
//            }
//
//            @Override
//            public void onMapStatusChangeFinish(MapStatus arg0) {
//                GolukDebugUtils.e("", "jyf----VideoCategoryActivity----LiveListBaiduMapView--onMapStatusChangeFinish ----: ");
//            }
//
//            @Override
//            public void onMapStatusChange(MapStatus arg0) {
//                GolukDebugUtils.e("", "jyf----VideoCategoryActivity----LiveListBaiduMapView--onMapStatusChange ----: ");
//            }
//        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.map_location_btn) {
            // 回到我的位置
            if(mGoogleMap != null){
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(LngLat.lat, LngLat.lng)));
            }
        }
    }
}
