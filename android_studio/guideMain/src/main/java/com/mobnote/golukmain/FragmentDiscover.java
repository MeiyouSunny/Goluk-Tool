package com.mobnote.golukmain;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventDeleteVideo;
import com.mobnote.eventbus.EventPraiseStatusChanged;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.newest.NewestListView;
import com.mobnote.golukmain.newest.WonderfulSelectedListView;
import com.mobnote.golukmain.search.SearchUserAcivity;
import com.mobnote.golukmain.videosuqare.VideoSquareAdapter;
import com.mobnote.util.SharedPrefUtil;
import com.mobnote.util.ZhugeUtils;

import cn.com.mobnote.eventbus.EventLocationFinish;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FragmentDiscover extends Fragment implements OnClickListener {
    private static final String TAG = "FragmentDiscover";

    public VideoSquareAdapter mVideoSquareAdapter = null;
    private ViewPager mViewPager = null;
    private View hot = null;
    private TextView hotTitle = null;
    private TextView squareTitle = null;
    public String shareVideoId;
    private float density;
    RelativeLayout.LayoutParams lineParams = null;
    private int lineTop = 0;
//    private int textColorSelect = 0;
//    private int textcolorQx = 0;
    View mSquareRootView;
    private String mCityCode;

    private boolean mBannerLoaded;
    private ImageView mSearchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GolukDebugUtils.d(TAG, "onCreate");
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        GolukDebugUtils.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.video_square_main, null);
        mSquareRootView = rootView;
        density = SoundUtils.getInstance().getDisplayMetrics().density;
        lineParams = new RelativeLayout.LayoutParams((int) (60 * density), (int) (2 * density));
        lineTop = (int) (5 * density);
//        textColorSelect = getResources().getColor(R.color.textcolor_select);
//        textcolorQx = getResources().getColor(R.color.textcolor_qx);
        mSearchView = (ImageView) rootView.findViewById(R.id.iv_followed_search);

        mSearchView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setClass(FragmentDiscover.this.getActivity(), SearchUserAcivity.class);
                FragmentDiscover.this.getActivity().startActivity(intent);
            }
        });
        init();
        mBannerLoaded = false;
        mCityCode = SharedPrefUtil.getCityIDString();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GolukDebugUtils.d(TAG, "onActivityCreated");
    }

    public VideoSquareAdapter getVideoSquareAdapter() {
        return mVideoSquareAdapter;
    }

    public void init() {
        mViewPager = (ViewPager) mSquareRootView.findViewById(R.id.mViewpager);
        mViewPager.setOffscreenPageLimit(3);
        mVideoSquareAdapter = new VideoSquareAdapter(getActivity());
        mViewPager.setAdapter(mVideoSquareAdapter);
        mViewPager.setOnPageChangeListener(opcl);
        hot = mSquareRootView.findViewById(R.id.line_hot);
        hotTitle = (TextView) mSquareRootView.findViewById(R.id.hot_title);
        squareTitle = (TextView) mSquareRootView.findViewById(R.id.square_title);
        setListener();
    }

    private void setListener() {
        hotTitle.setOnClickListener(this);
        squareTitle.setOnClickListener(this);
    }

    // 分享成功后需要调用的接口
    public void shareSucessDeal(boolean isSucess, String channel) {
        if (!isSucess) {
            return;
        }
        GolukApplication.getInstance().getVideoSquareManager().shareVideoUp(channel, shareVideoId);
    }

    private OnPageChangeListener opcl = new OnPageChangeListener() {
        @Override
        public void onPageSelected(int page) {
            GolukDebugUtils.e(TAG, "onPageSelected:" + page);
            mState = true;
            updateState(page);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // arg0 :当前页面，及你点击滑动的页面
            // arg1:当前页面偏移的百分比
            // arg2:当前页面偏移的像素位置
            if (0 == arg2) {
                return;
            }

            float process = arg1 * 100;
            if (process < 0) {
                process = 0;
            }

            if (process > 99) {
                process = 100;
            }

            updateLine((int) process);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            GolukDebugUtils.e(TAG, "onPageScrollStateChanged: arg0: " + state);
            // 其中state这个参数有三种状态（0，1，2）
            // state ==1的时辰默示正在滑动，
            // state==2的时辰默示滑动完毕了
            // state==0的时辰默示什么都没做。
            // 当页面开始滑动的时候，三种状态的变化顺序为（1，2，0）
        }
    };

    private void updateLine(int process) {
        final int leftMargin = (int) (process * density);
        GolukDebugUtils.e(TAG, "updateLine: " + leftMargin);
        lineParams.addRule(RelativeLayout.BELOW, R.id.hot_title);
        lineParams.setMargins(leftMargin, lineTop, 0, 0);
        hot.setLayoutParams(lineParams);
    }

    private void updateState(int type) {
        if (0 == type) {
            if (mState) {
                mState = false;
                ZhugeUtils.eventWonderfulPage(getActivity());
            }
//            hotTitle.setTextColor(textColorSelect);
//            squareTitle.setTextColor(textcolorQx);
        } else if (1 == type) {
            if (mState) {
                mState = false;
                ZhugeUtils.eventNewestPage(getActivity());
            }
//            hotTitle.setTextColor(textcolorQx);
//            squareTitle.setTextColor(textColorSelect);
        }
    }

    private boolean mState = false;

    @Override
    public void onClick(View arg0) {
        int id = arg0.getId();
        if (id == R.id.hot_title) {
            mState = true;
            mViewPager.setCurrentItem(0);
            this.updateState(0);
            updateLine(0);
        } else if (id == R.id.square_title) {
            mState = true;
            mViewPager.setCurrentItem(1);
            this.updateState(1);
            updateLine(100);
        } else {
        }
    }

    // 删除视频
    public void onEventMainThread(EventDeleteVideo event) {
        if (EventConfig.VIDEO_DELETE == event.getOpCode()) {
            final String delVid = event.getVid(); // 已经删除的id
            NewestListView listView = getVideoSquareAdapter().getNewestListView();
            listView.deleteVideo(delVid);
        }
    }

    public void onEventMainThread(EventPraiseStatusChanged event) {
        if (null == event) {
            return;
        }

        switch (event.getOpCode()) {
            case EventConfig.PRAISE_STATUS_CHANGE:
                VideoSquareAdapter videoSquareAdapter = getVideoSquareAdapter();
                if (null == videoSquareAdapter) {
                    return;
                }

                NewestListView listView = videoSquareAdapter.getNewestListView();

                if (null == listView) {
                    return;
                }
                listView.changePraiseStatus(event.isStatus(), event.getVideoId());
                break;
            default:
                break;
        }
    }

    public void onEventMainThread(EventLocationFinish event) {
        if (null == event) {
            return;
        }

        switch (event.getOpCode()) {
            case EventConfig.LOCATION_FINISH:
                Log.d(TAG, "Location Finished: " + event.getCityCode());
                // Start load banner
                VideoSquareAdapter videoSquareAdapter = getVideoSquareAdapter();
                if (null == videoSquareAdapter) {
                    return;
                }
                WonderfulSelectedListView listView = videoSquareAdapter.getWonderfulSelectedListView();

                if (null == listView) {
                    return;
                }

                if (!mBannerLoaded) {
                    Log.d(TAG, "Activity first start, fill everything anyway");
                    if (event.getCityCode().equals("-1")) {
                        if (null == mCityCode || mCityCode.trim().equals("")) {
                            mCityCode = event.getCityCode();
                            SharedPrefUtil.setCityIDString(mCityCode);
                            listView.loadBannerData(mCityCode);
                        } else {
                            listView.loadBannerData(mCityCode);
                        }
                    } else {
                        mCityCode = event.getCityCode();
                        SharedPrefUtil.setCityIDString(mCityCode);
                        listView.loadBannerData(mCityCode);
                    }
                    mBannerLoaded = true;
                }

                if (null == mCityCode || mCityCode.trim().equals("")) {
                    Log.d(TAG, "First located, fill everything anyway");
                    mCityCode = event.getCityCode();
                    SharedPrefUtil.setCityIDString(mCityCode);
                    listView.loadBannerData(mCityCode);
                } else {
                    // In whole nation
                    if ("-1".equals(mCityCode)) {
                        if (event.getCityCode().equals("-1")) {
                            // do nothing
                        } else {
                            mCityCode = event.getCityCode();
                            SharedPrefUtil.setCityIDString(mCityCode);
                            listView.loadBannerData(mCityCode);
                        }
                    } else { // In city
                        if (event.getCityCode().equals("-1")) {
                            // do nothing
                        } else {
                            if (!mCityCode.equals(event.getCityCode())) {
                                mCityCode = event.getCityCode();
                                SharedPrefUtil.setCityIDString(mCityCode);
                                listView.loadBannerData(mCityCode);
                            }
                        }
                    }
                }

                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        GolukDebugUtils.d(TAG, "onResume");
        if (null != mVideoSquareAdapter) {
            mVideoSquareAdapter.onResume();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        GolukDebugUtils.d(TAG, "onAttach, context=" + activity);
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        // TODO Auto-generated method stub
        GolukDebugUtils.d(TAG, "onDetach");
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();
        GolukDebugUtils.d(TAG, "onPause");
        if (null != mVideoSquareAdapter) {
            mVideoSquareAdapter.onPause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        GolukDebugUtils.d(TAG, "onStop");
        if (null != mVideoSquareAdapter) {
            mVideoSquareAdapter.onStop();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        GolukDebugUtils.d(TAG, "onDestroyView");
        mSquareRootView = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GolukDebugUtils.d(TAG, "onDestroy");
        if (null != mVideoSquareAdapter) {
            mVideoSquareAdapter.onDestroy();
        }
        mBannerLoaded = false;
        EventBus.getDefault().unregister(this);
    }
}
