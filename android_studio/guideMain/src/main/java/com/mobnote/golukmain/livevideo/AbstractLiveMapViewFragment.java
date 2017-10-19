package com.mobnote.golukmain.livevideo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.util.JsonUtil;

import cn.com.mobnote.module.location.GolukPosition;
import cn.com.mobnote.module.location.ILocationFn;
import cn.com.mobnote.module.page.IPageNotifyFn;

/**
 * Created by leege100 on 2016/7/19.
 */
abstract class AbstractLiveMapViewFragment extends Fragment implements ILiveMap, ILiveUIChangeListener, ILocationFn, View.OnClickListener, IRequestResultListener {
//    public static final int[] shootImg = {R.drawable.live_btn_6s_record, R.drawable.live_btn_5s_record,
//            R.drawable.live_btn_4s_record, R.drawable.live_btn_3s_record, R.drawable.live_btn_2s_record,
//            R.drawable.live_btn_1s_record};

    public static final int[] mHeadImg = {0, R.drawable.editor_boy_one, R.drawable.editor_boy_two,
            R.drawable.editor_boy_three, R.drawable.editor_girl_one, R.drawable.editor_girl_two,
            R.drawable.editor_girl_three, R.drawable.head_unknown};

    public static final int[] mBigHeadImg = {R.drawable.editor_head_feault7, R.drawable.editor_head_boy1,
            R.drawable.editor_head_boy2, R.drawable.editor_head_boy3, R.drawable.editor_head_girl4,
            R.drawable.editor_head_girl5, R.drawable.editor_head_girl6, R.drawable.editor_head_feault7};

    public LiveActivity mLiveActivity;
    private View mRootView;
    public static final String TAG = "LiveFragment";
    /**
     * 定位按钮
     */
    protected Button mLocationBtn = null;
    protected RelativeLayout mMapRootLayout;
    Bundle mSavedInstanceState;
    protected boolean isResetedView = false;
    /**
     * 是否已经开始上传地理位置信息
     */
    protected boolean isStartedUploadGEOInfo = false;
    protected boolean isExit = false;
    protected GolukPosition mLocation;
    protected int mTopMargin;
    protected UserInfo myUserInfo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_live, container, false);
        mMapRootLayout = (RelativeLayout) mRootView.findViewById(R.id.layout_live_map);
        mLiveActivity = (LiveActivity) getActivity();
        mLocationBtn = (Button) mRootView.findViewById(R.id.btn_live_location);
        mLocationBtn.setOnClickListener(this);
        GolukApplication.getInstance().addLocationListener(TAG, this);
        initMap(savedInstanceState);
        return mRootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
        myUserInfo = GolukApplication.getInstance().getMyInfo();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_live_location)
            toMyLocation();
    }

    @Override
    public void LocationCallBack(String gpsJson) {
        if (!isAdded()) {
            return;
        }
        if (isDetached()) {
            return;
        }
        if (mLiveActivity.isLiveUploadTimeOut) {
            return;
        }
        if (TextUtils.isEmpty(gpsJson)) {
            return;
        }
        mLocation = JsonUtil.parseLocatoinJson(gpsJson);
        if (mLocation == null) {
            return;
        }
        updateCurrUserMarker(mLocation.rawLat, mLocation.rawLon);
        if (!mLiveActivity.isMineLiveVideo) {
            return;
        }
        if (isStartedUploadGEOInfo) {
            return;
        }
        isStartedUploadGEOInfo = true;
        new Thread() {
            public void run() {
                while (!isExit) {
                    if (mLocation != null) {
                        UpdatePositionRequest updatePositionRequest = new UpdatePositionRequest(IPageNotifyFn.PAGE_TYPE_UPLOAD_POSITION, AbstractLiveMapViewFragment.this);
                        updatePositionRequest.get(String.valueOf(mLocation.rawLon), String.valueOf(mLocation.rawLat));
                    }
                    try {
                        sleep(15000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (requestType == IPageNotifyFn.PAGE_TYPE_UPLOAD_POSITION) {
        }
    }

    @Override
    public void onExit() {
        isExit = true;
    }
}
