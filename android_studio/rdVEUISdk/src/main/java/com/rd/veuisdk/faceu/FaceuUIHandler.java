package com.rd.veuisdk.faceu;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler;
import androidx.core.view.ViewPager;
import androidx.core.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.rd.cache.GalleryImageFetcher;
import com.rd.cache.HttpImageFetcher;
import com.rd.cache.ImageCache.ImageCacheParams;
import com.rd.cache.ImageResizer;
import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownFileListener;
import com.rd.http.MD5;
import com.rd.http.NameValuePair;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.net.RdHttpClient;
import com.rd.recorder.api.RecorderCore;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.FaceuAdapter;
import com.rd.veuisdk.adapter.MyViewPagerAdapter;
import com.rd.veuisdk.manager.FaceInfo;
import com.rd.veuisdk.manager.FaceuConfig;
import com.rd.veuisdk.ui.HorizontalListViewFuSticker;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 人脸道具辅助类
 *
 * @author ADMIN
 */
class FaceuUIHandler {

    private View mFilterLayout;
    private RadioGroup mRgFilters, mRgFuBlue;
    //    private RadioButton mRbFilter, mRbFaceFiter, mRbFUBeaty, mRbblue1, mRbblue2,
//            mRbblue3, mRbblue4, mRbblue5, mRbblue6;
    private SeekBar mSbarThin, mSbarEye, mSbarColor;
    private boolean bSupportFace = false;
    private LinearLayout mFuLayoutParent;

    /**
     * 是否支持人脸道具
     *
     * @return
     */
    boolean isbSupportFace() {
        return bSupportFace;
    }

    private ImageResizer mFetcher;
    private FaceuConfig config;
    private FaceuListener mListener;
    private LinearLayout fuLayout;
    private LayoutInflater inflater;
    private LinearLayout moreMenuLayout;   // 滤镜、 美颜 、贴纸所有的view的容器
    private IReloadListener mReloadListener;

    FaceuUIHandler(RadioGroup filterGroup, View filterLayout, View rgMenuParent,
                   boolean supportFacePack,
                   FaceuConfig _config, FaceuListener _iface,
                   LinearLayout _fuLayout, LinearLayout _fuLayoutParent, LinearLayout _filter_parent_layout, IReloadListener listener) {
        mReloadListener = listener;
        inflater = LayoutInflater.from(filterGroup.getContext());
        moreMenuLayout = _filter_parent_layout;
        mFuLayoutParent = _fuLayoutParent;
        fuLayout = _fuLayout;
        mListener = _iface;
        config = _config;

        ImageCacheParams cacheParams = new ImageCacheParams(
                filterLayout.getContext(), null);
        cacheParams.compressFormat = CompressFormat.PNG;
        // 缓冲占用系统内存的10%
        cacheParams.setMemCacheSizePercent(0.05f);
        if (config.isEnableNetFaceu()) {
            mFetcher = new HttpImageFetcher(filterLayout.getContext(), 150, 150);
        } else {
            mFetcher = new GalleryImageFetcher(filterLayout.getContext(), 150, 150);
        }
        mFetcher.addImageCache(filterLayout.getContext(), cacheParams);
        mRgFilters = filterGroup;
        bSupportFace = (RecorderCore.isSupportFaceU() && supportFacePack && (null != config));

        mFilterLayout = filterLayout;


        bSupportFace = (RecorderCore.isSupportFaceU() && supportFacePack && (null != config));

        // 4.3 以上且代码库支持解密
        rgMenuParent.setVisibility(isbSupportFace() ? View.VISIBLE : View.GONE);

        mRgFilters.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.camare_filter) {
                    fuLayout.setVisibility(View.INVISIBLE);
                    mFilterLayout.setVisibility(View.VISIBLE);
                    mFuLayoutParent.setVisibility(View.INVISIBLE);
                } else if (checkedId == R.id.camare_face_filter) {
                    mFilterLayout.setVisibility(View.INVISIBLE);
                    mFuLayoutParent.setVisibility(View.INVISIBLE);
                    fuLayout.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.camare_face_beauty) {
                    mFuLayoutParent.setVisibility(View.VISIBLE);
                    initBeautyView(mFuLayoutParent, tempOrientation);
                    fuLayout.setVisibility(View.INVISIBLE);
                    mFilterLayout.setVisibility(View.INVISIBLE);
                }

            }
        });


    }

    /**
     * 录制横竖屏视频，切换设备方向响应UI
     *
     * @param nOrientation
     */
    void setOrientation(int nOrientation) {
        initBeautyView(mFuLayoutParent, nOrientation);
    }

    private int tempOrientation;
    private int lastShowOrientation = -1;// 防止每次选中按钮addview()
    private View target;

    /**
     * 切换方向重新加载滤镜
     *
     * @param isVer
     */
    private void reLoadFilter(boolean isVer) {

        if (null != mFilterLayout) {
        }
        if (null != mReloadListener) {
            mReloadListener.onReloadFilters(isVer);
        }
    }

    private View fuLvLayout;

    private HorizontalListViewFuSticker fuLV;

    public boolean isCurrentIsVer() {
        return mCurrentIsVer;
    }

    //当前录制方向
    private boolean mCurrentIsVer = true;
    private String TAG = "FaceuHandler";

    /**
     * 切换横竖屏(UI重新初始化方向)
     *
     * @param group
     * @param nOrientation
     */
    private void initBeautyView(final ViewGroup group, int nOrientation) {

        tempOrientation = nOrientation;
        if (lastShowOrientation != tempOrientation) {
            group.removeAllViews();
            int height = 0;
            moreMenuLayout.setVisibility(View.INVISIBLE);
            Resources res = moreMenuLayout.getResources();
            if (0 == nOrientation || nOrientation == 180) {
                mCurrentIsVer = true;
                if (isbSupportFace()) {
                    height = res.getDimensionPixelSize(R.dimen.record_filter_parent_layout_height_vertical);
                } else {
                    height = res.getDimensionPixelSize(R.dimen.record_filter_parent_layout_height_vertical) - res.getDimensionPixelSize(R.dimen.camera_radiogroup_height);
                }
                reLoadFilter(mCurrentIsVer);
                moreMenuLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height));
                target = inflater.inflate(R.layout.fu_beauty_layout, null);
                fuLvLayout = inflater.inflate(R.layout.fu_stickers_layout, null);
                vp = (ViewPager) fuLvLayout.findViewById(R.id.fuViewpager);
                vp.removeAllViews();
                lpoints = (LinearLayout) fuLvLayout.findViewById(R.id.fuPoints);
                lpoints.removeAllViews();
            } else {
                mCurrentIsVer = false;
                if (isbSupportFace()) {
                    height = res.getDimensionPixelSize(R.dimen.record_filter_parent_layout_height_horizontal);
                } else {
                    height = res.getDimensionPixelSize(R.dimen.record_filter_parent_layout_height_horizontal) - res.getDimensionPixelSize(R.dimen.camera_radiogroup_height);
                }

                reLoadFilter(mCurrentIsVer);
                moreMenuLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height));
                target = inflater.inflate(R.layout.fu_beauty_layout_land, null);
                fuLvLayout = inflater.inflate(R.layout.fu_stickers_layout_land, null);
                fuLV = (HorizontalListViewFuSticker) fuLvLayout.findViewById(R.id.lvFuList);

            }
            if (bSupportFace) {
                initdata();
            }
            fuLayout.removeAllViews();
            fuLayout.addView(fuLvLayout);
            lastShowOrientation = tempOrientation;
            mSbarThin = (SeekBar) target.findViewById(R.id.sbFuThinbar);
            mSbarEye = (SeekBar) target.findViewById(R.id.sbFuEyebar);
            mSbarColor = (SeekBar) target.findViewById(R.id.sbFuColorbar);
            mRgFuBlue = (RadioGroup) target.findViewById(R.id.fu_blue_level);
//            mRbblue1 = (RadioButton) mRgFuBlue.findViewById(R.id.fu_blue_1);
//            mRbblue2 = (RadioButton) mRgFuBlue.findViewById(R.id.fu_blue_2);
//            mRbblue3 = (RadioButton) mRgFuBlue.findViewById(R.id.fu_blue_3);
//            mRbblue4 = (RadioButton) mRgFuBlue.findViewById(R.id.fu_blue_4);
//            mRbblue5 = (RadioButton) mRgFuBlue.findViewById(R.id.fu_blue_5);
//            mRbblue6 = (RadioButton) mRgFuBlue.findViewById(R.id.fu_blue_6);

            mSbarThin.setMax(100);
            mSbarEye.setMax(100);
            mSbarColor.setMax(100);

            setValue(mThinValue, mColorValue, mEyeValue, mBlueValue);
            mRgFuBlue.setOnCheckedChangeListener(onCheckBlue);
            mSbarThin.setOnSeekBarChangeListener(onSeekBarThin);
            mSbarEye.setOnSeekBarChangeListener(onSeekBarEye);
            mSbarColor.setOnSeekBarChangeListener(onSeekBarColor);
            mHandler.removeCallbacks(updateRunnable);
            //设备多等待会，防止切换方向UI卡顿
            mHandler.postDelayed(updateRunnable, 1000);
        }

    }


    private OnCheckedChangeListener onCheckBlue = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.fu_blue_1) {
                mBlueValue = 1.0f;
            } else if (checkedId == R.id.fu_blue_2) {
                mBlueValue = 2.0f;
            } else if (checkedId == R.id.fu_blue_3) {
                mBlueValue = 3.0f;
            } else if (checkedId == R.id.fu_blue_5) {
                mBlueValue = 5.0f;
            } else if (checkedId == R.id.fu_blue_6) {
                mBlueValue = 6.0f;
            } else {
                mBlueValue = 4.0f;
            }
        }
    };
    private OnSeekBarChangeListener onSeekBarColor = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (fromUser) {
                mColorValue = MAXCOLOR * progress / seekBar.getMax();
            }
        }
    };

    private OnSeekBarChangeListener onSeekBarThin = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (fromUser) {
                mThinValue = MAXTHIN * progress / seekBar.getMax();
            }

        }
    };

    private OnSeekBarChangeListener onSeekBarEye = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (fromUser) {
                mEyeValue = MAXEYE * progress / seekBar.getMax();
            }
        }
    };

    /**
     *
     */
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            mFuLayoutParent.removeAllViews();
            mFuLayoutParent.addView(target, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            moreMenuLayout.startAnimation(AnimationUtils.loadAnimation(
                    moreMenuLayout.getContext(), R.anim.alpha_in));
            moreMenuLayout.setVisibility(View.VISIBLE);
        }
    };

    private ArrayList<FaceuAdapter> adapters = new ArrayList<FaceuAdapter>();

    private int totalPage = 1;
    private int mPageSize = 8;
    private List<View> viewPagerList;// GridView作为一个View对象添加到ViewPager集合中
    private ImageView[] ivPoints;

    //上一个人脸贴纸的小标
    private int mLastPositon = 0;
    //人脸贴纸列表（包含第一项"无"）
    private ArrayList<FaceInfo> temp = new ArrayList<FaceInfo>();

    /**
     * 初始化横屏录制时人脸贴纸
     *
     * @param _list
     */
    private void initHorList(ArrayList<FaceInfo> _list) {

        Context appContext = fuLayout.getContext().getApplicationContext();
        initFuData(appContext, _list);


        int len = temp.size();
        for (int i = 0; i < len; i++) {
            FaceInfo info = temp.get(i);
            if (i == 0) {
                fuLV.addListItem(0, Integer.parseInt(info.getIcon()));
            } else {
                fuLV.addListItem(i, info.getIcon(), mFetcher);
                fuLV.isExit(i, info.isExists());
            }
        }

        fuLV
                .setListItemSelectListener(new HorizontalListViewFuSticker.OnListViewItemSelectListener() {

                    @Override
                    public void onSelected(View view, int nItemId, boolean user) {

                        if (nItemId >= 1) {
                            onFuItemChecked(nItemId);
                        } else {
                            if (null != mListener) {
                                mListener.onFUChanged("", nItemId);
                            }
                            mLastPositon = nItemId;

                        }

                    }

                    @Override
                    public boolean onBeforeSelect(View view, int nItemId) {
                        return false;
                    }
                });
        fuLV.selectListItem(mLastPositon, true);

    }

    /**
     * 准备要贴纸数据
     *
     * @param context
     * @param _list
     */
    private void initFuData(Context context, ArrayList<FaceInfo> _list) {
        temp.clear();
        if (mCurrentIsVer) {
            temp.add(new FaceInfo(FaceuAdapter.NONE, Integer
                    .toString(R.drawable.camare_filter_0), context
                    .getString(R.string.none)));
        } else {
            temp.add(new FaceInfo(FaceuAdapter.NONE, Integer
                    .toString(R.drawable.none_filter_n), context
                    .getString(R.string.none)));
        }
        if (null == _list) {
            _list = new ArrayList<FaceInfo>();
        }
        temp.addAll(_list);
    }

    /**
     * 准备往Viewpager中填充数据
     *
     * @param _list
     */
    private void initGridviewData(ArrayList<FaceInfo> _list) {
        Context appContext = fuLayout.getContext().getApplicationContext();
        initFuData(appContext, _list);
        lpoints.removeAllViews();
        // 总的页数向上取整
        viewPagerList = new ArrayList<View>();
        totalPage = (int) Math.ceil(temp.size() * 1.0 / mPageSize);
        viewPagerList = new ArrayList<View>();

        for (int i = 0; i < totalPage; i++) {
            // 每个页面都是inflate出一个新实例
            GridView gridView = (GridView) View.inflate(appContext,
                    R.layout.item_gridview, null);

            gridView.setVerticalSpacing(8);
            FaceuAdapter faceuAdapter = new FaceuAdapter(appContext, mFetcher,
                    temp, i, mPageSize);
            faceuAdapter.setFuListener(new FaceuListener() {

                @Override
                public void onFUChanged(String filePath, int lastPosition) {
                    if (null != mListener) {
                        mListener.onFUChanged(filePath, lastPosition);
                        for (int j = 0; j < adapters.size(); j++) {
                            adapters.get(j).resetChecked();
                        }
                    }
                    mLastPositon = lastPosition;
                }
            });

            faceuAdapter.setCheck(mLastPositon);
            adapters.add(faceuAdapter);
            gridView.setAdapter(faceuAdapter);
            faceuAdapter.setListview(gridView);
            // 每一个GridView作为一个View对象添加到ViewPager集合中
            viewPagerList.add(gridView);
        }
        // 设置ViewPager适配器
        vp.setAdapter(new MyViewPagerAdapter(viewPagerList));

        // 添加小圆点
        ivPoints = new ImageView[totalPage];
        for (int i = 0; i < totalPage; i++) {
            // 循坏加入点点图片组
            ivPoints[i] = new ImageView(lpoints.getContext());
            if (i == 0) {
                ivPoints[i].setImageResource(R.drawable.page_focuese);
            } else {
                ivPoints[i].setImageResource(R.drawable.page_unfocused);
            }
            ivPoints[i].setPadding(8, 8, 8, 8);
            lpoints.addView(ivPoints[i]);
        }
        // 设置ViewPager的滑动监听，主要是设置点点的背景颜色的改变
        vp.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

            }
        });
        vp.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < totalPage; i++) {
                    if (i == position) {
                        ivPoints[i].setImageResource(R.drawable.page_focuese);
                    } else {
                        ivPoints[i].setImageResource(R.drawable.page_unfocused);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

    }

    private LinearLayout lpoints;
    private ViewPager vp;

    private final float MAXEYE = 4.0f;
    private final float MAXCOLOR = 1.0f;
    private final float MAXTHIN = 2.0f;
    private final float MAXBLUE = 4.0f;
    private float mThinValue = 0.1f;
    private float mBlueValue = 0.1f;
    private float mEyeValue = 0.1f;
    private float mColorValue = 0.1f;

    float getThin() {
        return mThinValue;
    }

    float getColor() {
        return mColorValue;
    }

    float getBlue() {
        return mBlueValue;
    }

    /**
     * 是否启用美艳参数
     *
     * @param enable
     */
    void enableBeauty(boolean enable) {
        float tthin = enable ? config.getCheek_thinning() : 0f;
        float tcolor = enable ? config.getColor_level() : 0f;
        float teye = enable ? config.getCheek_thinning() : 0f;
        float tblue = enable ? config.getBlur_level() : 1f;
        setValue(tthin, tcolor, teye, tblue);
        mThinValue = tthin;
        mEyeValue = teye;
        mColorValue = tcolor;
        mBlueValue = tblue;

    }

    /**
     * 重置value
     *
     * @param tthin
     * @param tcolor
     * @param teye
     * @param tblue
     */
    private void setValue(float tthin, float tcolor, float teye, float tblue) {
        if (null != mSbarThin) {
            int proThin = getNeedProgress(tthin, mSbarThin.getMax(), MAXTHIN);
            mSbarThin.setProgress(proThin);
        }
        if (null != mSbarEye) {
            int proEye = getNeedProgress(teye, mSbarEye.getMax(), MAXEYE);
            mSbarEye.setProgress(proEye);
        }
        if (null != mSbarColor) {
            int proColor = getNeedProgress(tcolor, mSbarColor.getMax(), MAXCOLOR);
            mSbarColor.setProgress(proColor);
        }
        if (null != mRgFuBlue) {

            if (0f <= tblue && tblue <= 1f) {
                mRgFuBlue.check(R.id.fu_blue_1);
            } else if (1f < tblue && tblue <= 2f) {
                mRgFuBlue.check(R.id.fu_blue_2);
            } else if (2f < tblue && tblue <= 3f) {
                mRgFuBlue.check(R.id.fu_blue_3);
            } else if (3f < tblue && tblue <= 4f) {
                mRgFuBlue.check(R.id.fu_blue_4);
            } else if (4f < tblue && tblue <= 5f) {
                mRgFuBlue.check(R.id.fu_blue_5);
            } else {
                mRgFuBlue.check(R.id.fu_blue_6);
            }
        }
    }

    private int getNeedProgress(float fsize, int max, float maxVlue) {
        return (int) (fsize * max / MAXTHIN);
    }

    float getEye() {
        return mEyeValue;
    }

    private File cacheDir;

    /**
     * 加载网络faceU资源
     */
    private void initdata() {
        cacheDir = fuLayout.getContext().getCacheDir();
        if (config.isEnableNetFaceu()) {

            ThreadPoolUtils.execute(new Runnable() {

                @Override
                public void run() {
                    File f = new File(cacheDir, MD5.getMD5("data.json"));
                    if (TextUtils.isEmpty(config.getUrl())) {
                        Log.e("config.getUrl()", "FaceuConfig.getUrl() is null");
                    } else {
                        boolean needLoadLocal = true;//是否需要加载离线数据
                        if (CoreUtils.checkNetworkInfo(fuLayout.getContext()) != CoreUtils.UNCONNECTED) {
                            String str = RdHttpClient.PostJson(config.getUrl(),
                                    new NameValuePair("type", "android"));
                            if (!TextUtils.isEmpty(str)) {// 加载网络数据
                                onParseJson(str);
                                try {
                                    String data = URLEncoder.encode(str,
                                            "UTF-8");
                                    FileUtils.writeText2File(data,
                                            f.getAbsolutePath());
                                    needLoadLocal = false;
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (needLoadLocal) {
                            if (null != f && f.exists()) {// 加载离线数据
                                String offline = FileUtils.readTxtFile(f
                                        .getAbsolutePath());
                                try {
                                    offline = URLDecoder.decode(offline,
                                            "UTF-8");
                                    if (!TextUtils.isEmpty(offline)) {
                                        onParseJson(offline);
                                    }
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();

                                }
                            }
                        }

                    }

                    mHandler.obtainMessage(MSG_INITDATAUI, WEB_FACE, 0)
                            .sendToTarget();
                }
            });

        } else {
            if (null == config.getLists() || config.getLists().size() == 0) {
                Log.e("FaceuConfig.getLists()",
                        "FaceuConfig.getLists() is null or size()==0");
            }
            mHandler.obtainMessage(MSG_INITDATAUI, LOCAL_FACE, 0)
                    .sendToTarget();
        }
    }

    private void onParseJson(String data) {
        try {
            JSONObject jobj = new JSONObject(data);
            JSONObject jtemp = jobj.optJSONObject("result");
            if (null != jtemp) {
                JSONArray jarr = jtemp.optJSONArray("bundles");
                if (null != jarr) {
                    int len = jarr.length();
                    JSONObject jt;
                    ArrayList<FaceInfo> mlist = new ArrayList<FaceInfo>();
                    for (int i = 0; i < len; i++) {
                        jt = jarr.getJSONObject(i);
                        if (null != jt) {
                            String url = jt.getString("url");
                            String name = jt.getString("name");
                            String localPath = PathUtils.getRdFaceuPath() + "/"
                                    + MD5.getMD5(i + url);
                            mlist.add(new FaceInfo(localPath, name, jt
                                    .getString("img"), url));
                        }
                    }
                    config.setWebInfos(mlist);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 退出
     */
    void onFinishView() {
        DownLoadUtils.forceCancelAll();
        loading.clear();
        mHandler.removeMessages(PROGRESS);
        mHandler.removeMessages(CANCEL);
        mHandler.removeMessages(FINISHED);
        mHandler.removeCallbacks(updateRunnable);
        if (null != adapters) {
            for (int i = 0; i < adapters.size(); i++) {
                adapters.get(i).onDestory();
            }
            adapters.clear();
        }
        if (null != mFetcher) {
            mFetcher.cleanUpCache();
        }

        if (null != mHandler) {
            mHandler.removeMessages(MSG_INITDATAUI);
        }

        if (null != mFilterLayout) {
            mFilterLayout = null;
        }
        if (null != inflater) {
            inflater = null;
        }
        if (null != fuLV) {
            fuLV.recycle();
        }
        if (null != temp) {
            temp.clear();
        }
    }

    private final int MSG_INITDATAUI = 5;
    private final int WEB_FACE = 102;// 网络
    private final int LOCAL_FACE = 101;// 本地
    private final int PROGRESS = 2, FINISHED = 3, CANCEL = 4;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case MSG_INITDATAUI: {
                    if (msg.arg1 == WEB_FACE) {
                        if (mCurrentIsVer) {
                            initGridviewData(config.getWebInfos());// 网络
                        } else {
                            initHorList(config.getWebInfos());//横屏网络
                        }
                    } else if (msg.arg1 == LOCAL_FACE) {
                        if (mCurrentIsVer) {
                            initGridviewData(config.getLists());// 本地
                        } else {
                            initHorList(config.getLists()); //横屏网络
                        }
                    }
                }
                break;
                case PROGRESS: {
                    if (!mCurrentIsVer && null != fuLV) {
                        fuLV.setDownProgress(msg.arg1, msg.arg2);
                    }
                }
                break;
                case FINISHED: {
                    if (!mCurrentIsVer && null != fuLV) {
                        fuLV.setDownEnd(msg.arg1);
                        onFuItemChecked(msg.arg1);
                    }

                }
                break;
                case CANCEL: {
                    if (!mCurrentIsVer && null != fuLV) {
                        fuLV.setDownFailed(msg.arg1);
                    }
                }
                break;
                default:
                    break;
            }
        }

        ;
    };

    private void onToast(String msg) {
        if (null != lpoints) {
            SysAlertDialog.showAutoHideDialog(lpoints.getContext(), "", msg,
                    Toast.LENGTH_SHORT);
        }
    }

    private void onFuItemChecked(int position) {
        FaceInfo info = temp.get(position);
        if (null != info) {
            if (info.isExists()) {
                if (null != mListener) {
                    mListener.onFUChanged(info.getPath(), position);
                }
                mLastPositon = position;
            } else {
                onDown(position);
            }
        }

    }

    //正在下载的列表
    private ArrayList<String> loading = new ArrayList<String>();

    /**
     * 执行下载
     */
    private void onDown(int p) {
        if (loading.size() > 3) { // 最多同时下载3个
            return;
        }
        if (CoreUtils.checkNetworkInfo(fuLV.getContext()) == CoreUtils.UNCONNECTED) {
            onToast(fuLV.getContext().getString(
                    R.string.please_check_network));
            return;
        }
        final FaceInfo info = temp.get(p);
        boolean isDownLoading = false;
        for (String url : loading) {
            if (TextUtils.equals(url, info.getUrl())) {
                isDownLoading = true;
            }
        }
        if (!isDownLoading) {
            loading.add(info.getUrl());
            int id = p;
            DownLoadUtils utils = new DownLoadUtils(id, info.getUrl(), "");
            utils.setMethod(false);
            utils.DownFile(new IDownFileListener() {

                @Override
                public void onProgress(long mid, int progress) {
                    mHandler.obtainMessage(PROGRESS, (int) mid, progress).sendToTarget();
                }

                @Override
                public void Canceled(long mid) {
                    loading.remove(info.getUrl());
                    mHandler.obtainMessage(CANCEL, (int) mid, -1)
                            .sendToTarget();

                }

                @Override
                public void Finished(long mid, String localPath) {
                    loading.remove(info.getUrl());
                    File src = new File(localPath);
                    File dst = new File(info.getPath());
                    boolean re = src.renameTo(dst);
                    if (re) {
                        mHandler.obtainMessage(FINISHED, (int) mid, -1)
                                .sendToTarget();
                    } else {
                        mHandler.obtainMessage(CANCEL, (int) mid, -1)
                                .sendToTarget();
                    }
                }
            });
            fuLV.setdownStart(p);
        }
    }

}
