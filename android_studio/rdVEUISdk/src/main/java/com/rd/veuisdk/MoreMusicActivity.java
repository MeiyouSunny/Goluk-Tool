package com.rd.veuisdk;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.view.ViewPager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.LayoutParams;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.LogUtil;
import com.rd.veuisdk.database.HistoryMusicCloud;
import com.rd.veuisdk.database.SDMusicData;
import com.rd.veuisdk.database.WebMusicData;
import com.rd.veuisdk.fragment.CloudMusicFragment;
import com.rd.veuisdk.fragment.CloudSoundFragment;
import com.rd.veuisdk.fragment.HistoryMusicFragment;
import com.rd.veuisdk.fragment.LocalFragment;
import com.rd.veuisdk.fragment.LocalMusicFragment;
import com.rd.veuisdk.fragment.MyMusicFragment;
import com.rd.veuisdk.model.AudioMusicInfo;
import com.rd.veuisdk.model.CloudAuthorizationInfo;
import com.rd.veuisdk.model.IMusicApi;
import com.rd.veuisdk.model.WebMusicInfo;
import com.rd.veuisdk.mvp.model.MoreMusicModel;
import com.rd.veuisdk.ui.ExtViewPager;
import com.rd.veuisdk.utils.ModeDataUtils;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 更多音乐(本地)
 *
 * @author jian
 */
public class MoreMusicActivity extends BaseActivity {
    private static final String PARAM_TYPE = "menu";
    private static final String PARAM_CLOUD_AUTHORIZATION = "param_cloud_authorization";
    private static final String PARAM_NEWAPI = "_newApi";
    private static final String PARAM_SOUND_TYPE = "mSoundTypeUrl";
    private static final String PARAM_SOUND = "mSoundUrl";
    public static final int TYPE_MUSIC_1 = 0;// 配乐方式1
    public static final int TYPE_MUSIC_LOCAL = 1;// 配乐方式2 -》本地
    public static final int TYPE_MUSIC_YUN = 2;// 配乐方式2->云音乐
    public static final int TYPE_MUSIC_SOUND = 3;// 配乐方式2->音效
    public static final int TYPE_MUSIC_MANY = 4;// 配乐方式2->多段配乐

    private ExtViewPager mViewPager;
    private RadioGroup mRgMusicGroup;
    private HorizontalScrollView hsvMenuScroll;
    private ExtButton mBtnRight;
    private ExtButton mBtnLeft;
    private TextView mTvTitle;

    private IntentFilter mIntentFilter;
    private TitleReceiver mReceiver;
    private int mCurFragmentItem = 1;
    private MPageAdapter mPageAdapter;


    private int mMusicLayoutType = TYPE_MUSIC_1;
    private Resources mResources;


    /**
     * 云音乐
     *
     * @param context
     * @param newApi
     * @param typeUrl 云音乐分类
     * @param url     单个云音乐的地址  ||  没有音乐分类接口时的云音乐地址（ 即 “typeUrl”==“” 时，）
     * @param info
     */
    public static void onYunMusic(Context context, boolean newApi, String typeUrl, String url, CloudAuthorizationInfo info) {
        Intent music = new Intent(context, MoreMusicActivity.class);
        music.putExtra(PARAM_TYPE, TYPE_MUSIC_YUN);
        music.putExtra(PARAM_NEWAPI, newApi);
        music.putExtra(PARAM_CLOUD_AUTHORIZATION, info);
        if (newApi) {
            music.putExtra(PARAM_SOUND_TYPE, typeUrl);
        }
        music.putExtra(PARAM_SOUND, url);
        ((Activity) context).startActivityForResult(music, VideoEditActivity.REQUSET_MUSICEX);
        ((Activity) context).overridePendingTransition(R.anim.push_bottom_in,
                R.anim.push_top_out);
    }

    private MoreMusicModel mModel;

    /**
     * 多段音乐
     *
     * @param context
     * @param newApi
     * @param url
     * @param info    版权证书
     */
    public static void onYunMusic(Context context, boolean newApi, String musicTypeUrl, String url, CloudAuthorizationInfo info, int requestCode) {
        Intent music = new Intent(context, MoreMusicActivity.class);
        music.putExtra(PARAM_TYPE, TYPE_MUSIC_MANY);
        music.putExtra(PARAM_NEWAPI, newApi);
        music.putExtra(PARAM_CLOUD_AUTHORIZATION, info);
        if (newApi) {
            music.putExtra(PARAM_SOUND_TYPE, musicTypeUrl);
        }
        music.putExtra(PARAM_SOUND, url);
        ((Activity) context).startActivityForResult(music, requestCode);
        ((Activity) context).overridePendingTransition(R.anim.push_bottom_in, R.anim.push_top_out);
    }

    /**
     * 本地音乐
     *
     * @param context
     * @param requestCode
     */
    public static void onLocalMusic(Context context, int requestCode) {
        Intent music = new Intent(context, MoreMusicActivity.class);
        music.putExtra(PARAM_TYPE, TYPE_MUSIC_LOCAL);
        ((Activity) context).startActivityForResult(music, requestCode);
        ((Activity) context).overridePendingTransition(R.anim.push_bottom_in,
                R.anim.push_top_out);
    }

    /**
     * 音效
     *
     * @param context
     * @param typeUrl
     * @param url
     * @param requestCode
     */
    public static void onSound(Context context, String typeUrl, String url, int requestCode) {
        Intent music = new Intent(context, MoreMusicActivity.class);
        music.putExtra(PARAM_TYPE, TYPE_MUSIC_SOUND);
        music.putExtra(PARAM_SOUND_TYPE, typeUrl);
        music.putExtra(PARAM_NEWAPI, true);
        music.putExtra(PARAM_SOUND, url);
        ((Activity) context).startActivityForResult(music, requestCode);
        ((Activity) context).overridePendingTransition(R.anim.push_bottom_in, R.anim.push_top_out);
    }

    /**
     * 创建单个音乐分类
     *
     * @param text
     * @param lpItem
     */
    private void createRadioButton(String text, LayoutParams lpItem) {
        RadioButton radioButton = new RadioButton(this);
        radioButton.setId(text.hashCode());
        radioButton.setText(text);
        radioButton.setButtonDrawable(new ColorDrawable(Color.TRANSPARENT));
        radioButton.setBackgroundResource(R.drawable.more_music_menu_bg);
        radioButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        radioButton.setTextColor(mResources.getColor(R.drawable.radio_music_txcolor));
        radioButton.setGravity(Gravity.CENTER);
        radioButton.setPadding(mPadding, 0, mPadding, 0);
        mRgMusicGroup.addView(radioButton, lpItem);
    }

    private int mPadding = 5;

    private String TAG = "MoreMusicActivity";
    private ArrayList<IMusicApi> mMusicApiList = new ArrayList<IMusicApi>();
    //音效
    private String mSoundTypeUrl = null;
    private String mSoundUrl = null;
    private ArrayList<String> mClassification = new ArrayList<>();


    private MoreMusicModel.IMusicCallBack mCallBack = new MoreMusicModel.IMusicCallBack() {
        @Override
        public void onSound(ArrayList<String> ids, ArrayList<IMusicApi> musicApiArrayList) {
            mClassification = ids;
            mMusicApiList = musicApiArrayList;
            mHandler.obtainMessage(MSG_API).sendToTarget();
        }

        @Override
        public void onRdCloudMusic(ArrayList<IMusicApi> musicApiArrayList) {
            mMusicApiList = musicApiArrayList;
            mHandler.obtainMessage(MSG_API).sendToTarget();
        }

        @Override
        public void onSuccess(List list) {

        }

        @Override
        public void onFailed() {

        }
    };


    private final int MSG_API = 568;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MSG_API && isRunning) {

                int len = getListSize();
                if (len > 0) {

                    int itemHeightpx = CoreUtils.dpToPixel(24);
                    LayoutParams lpItem;
                    if (len <= 5) {
                        setRadioGroupFill();
                        lpItem = new LayoutParams(0, itemHeightpx);
                        lpItem.weight = 1;
                    } else {
                        lpItem = new LayoutParams(LayoutParams.WRAP_CONTENT,
                                itemHeightpx);
                    }
                    int dp2px = CoreUtils.dpToPixel(6);
                    lpItem.leftMargin = dp2px;
                    lpItem.rightMargin = dp2px;
                    mPadding = dp2px;
                    if (mMusicLayoutType == TYPE_MUSIC_MANY) {
                        createRadioButton(getString(R.string.local), lpItem);
                    }
                    for (int i = 0; i < len; i++) {
                        createRadioButton(mMusicApiList.get(i).getMenu(), lpItem);
                    }
                    initViewPager();
                    //取消等待
                    mInitLoad = true;
                    if (mDownLoad == mMusicApiList.size()) {
                        SysAlertDialog.cancelLoadingDialog();
                    }
                } else {
                    SysAlertDialog.cancelLoadingDialog();
                    onToast(getString(R.string.load_http_failed));
                }
            }
        }

        ;
    };

    //加载列表完成个数、初始是否完成 以此判断是否取消弹窗
    private int mDownLoad = 0;
    private boolean mInitLoad = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResources = getResources();
        localHash = getString(R.string.local).hashCode();
        Intent in = getIntent();
        mMusicLayoutType = in.getIntExtra(PARAM_TYPE, TYPE_MUSIC_1);
        boolean newApi = in.getBooleanExtra(PARAM_NEWAPI, true);
        mSoundTypeUrl = in.getStringExtra(PARAM_SOUND_TYPE);
        mSoundUrl = in.getStringExtra(PARAM_SOUND);


        if (TextUtils.isEmpty(mSoundUrl)) {
            mSoundUrl = "http://d.56show.com/filemanage2/public/filemanage/file/cloudMusicData";
        }
        mModel = new MoreMusicModel(mCallBack);
        mReceiver = new TitleReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ACTION_RIGHT);
        mIntentFilter.addAction(ACTION_TITLE);
        mIntentFilter.addAction(ACTION_MUSICINFO_STRING_WEB);
        mIntentFilter.addAction(ACTION_GONE_STRING);
        mIntentFilter.addAction(ACTION_ADD_LISTENER);

        SDMusicData.getInstance().initilize(this);
        WebMusicData.getInstance().initilize(this);
        setContentView(R.layout.activity_more_music);


        mViewPager = $(R.id.vpMusicMain);
        mRgMusicGroup = $(R.id.rgMusicGroup);
        hsvMenuScroll = $(R.id.hsvMenu);
        mBtnRight = $(R.id.btnRight);
        mBtnLeft = $(R.id.btnLeft);
        mTvTitle = $(R.id.tvTitle);

        if (mMusicLayoutType == TYPE_MUSIC_YUN || mMusicLayoutType == TYPE_MUSIC_MANY) {
            initSign(newApi);
            HistoryMusicCloud.getInstance().initilize(this);
            mCurFragmentItem = 0;
            mRgMusicGroup.removeAllViews();
            SysAlertDialog.showLoadingDialog(this, R.string.isloading);
            if (newApi) {
                if (!TextUtils.isEmpty(mSoundTypeUrl)) {
                    mModel.getSoundType(mSoundTypeUrl, ModeDataUtils.TYPE_YUN_CLOUD_MUSIC);
                } else {
                    //兼容锐动素材-未分页
                    mModel.getRdCouldMusic(mSoundUrl);
                }
            } else {
                //兼容第一批云音乐接口  -- 17rd素材 （dianbook.17rd.com）-  未分页
                mModel.getMusic(mSoundUrl);
            }
            mCloudChangListener = new onCloudCheckedChangeListener();
            mRgMusicGroup.setOnCheckedChangeListener(mCloudChangListener);
        } else if (mMusicLayoutType == TYPE_MUSIC_LOCAL) {
            mRgMusicGroup.setVisibility(View.GONE);
        } else if (mMusicLayoutType == TYPE_MUSIC_SOUND) {
            HistoryMusicCloud.getInstance().initilize(this);
            mCurFragmentItem = 0;
            mRgMusicGroup.removeAllViews();
            SysAlertDialog.showLoadingDialog(this, R.string.isloading);
            //如果为空 设置为默认
            if (TextUtils.isEmpty(mSoundTypeUrl)) {
                mSoundTypeUrl = "http://d.56show.com/filemanage2/public/filemanage/file/typeData";
            }
            mModel.getSoundType(mSoundTypeUrl, ModeDataUtils.TYPE_YUN_AUDIO_EFFECT);

            mCloudChangListener = new onCloudCheckedChangeListener();
            mRgMusicGroup.setOnCheckedChangeListener(mCloudChangListener);
        } else {
            setRadioGroupFill();

            mRgMusicGroup
                    .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            if (checkedId == R.id.rbMyMusic) {
                                setchecked(0);
                            } else if (checkedId == R.id.rbLocalMusic) {
                                setchecked(1);
                            } else if (checkedId == R.id.rbHistoryMusic) {
                                setchecked(2);
                            }

                        }
                    });
        }
        initView();
    }

    private final String M_LINE_CHAR = "@";

    /**
     * 云音乐作者授权
     *
     * @param newApi
     */
    private void initSign(boolean newApi) {
        if (newApi) {
            final CloudAuthorizationInfo info = getIntent().getParcelableExtra(PARAM_CLOUD_AUTHORIZATION);
            if (null != info) {
                $(R.id.sign_layout).setVisibility(View.VISIBLE);

                //******************************音乐家
                TextView tvArtist = $(R.id.yun_artist);
                StringBuffer sb = new StringBuffer();
                if (!TextUtils.isEmpty(info.getArtist())) {
                    sb.append(info.getArtist());
                }

                if (!TextUtils.isEmpty(info.getHomeTitle())) {
                    if (sb.length() > 0) {
                        sb.append(M_LINE_CHAR);
                    }
                    sb.append(info.getHomeTitle());
                }
                String text = sb.toString().trim();
                if (text.contains(M_LINE_CHAR)) {
                    SpannableString spannableString = new SpannableString(text);
                    spannableString.setSpan(new ClickableSpan() {
                        @Override
                        public void updateDrawState(TextPaint ds) {
                            ds.setColor(getResources().getColor(R.color.transparent_white));  //设置下划线颜色
                            ds.setUnderlineText(true);  // 显示下划线
                        }

                        @Override
                        public void onClick(View view) {     // TextView点击事件
                            if (!TextUtils.isEmpty(info.getHomeUrl())) {
                                Uri uri = Uri.parse(info.getHomeUrl());
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        }
                    }, text.indexOf(M_LINE_CHAR) + 1, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tvArtist.setText(spannableString);
                    tvArtist.setMovementMethod(LinkMovementMethod.getInstance());
                } else {
                    tvArtist.setText(text);
                }

                //****************授权证书
                if (!TextUtils.isEmpty(info.getAuthorizationTitle())) {
                    TextView tvSign = $(R.id.yun_sign);
                    text = info.getAuthorizationTitle();
                    SpannableString spannableString = new SpannableString(text);
                    spannableString.setSpan(new ClickableSpan() {
                        @Override
                        public void updateDrawState(TextPaint ds) {
                            ds.setColor(getResources().getColor(R.color.transparent_white));  //设置下划线颜色
                            ds.setUnderlineText(true);  // 显示下划线
                        }

                        @Override
                        public void onClick(View view) {

                            if (!TextUtils.isEmpty(info.getAuthorizationUrl())) {
                                //授权证书
                                MusicSignActivity.onSign(MoreMusicActivity.this, info.getAuthorizationUrl());
                            }
                        }
                    }, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    tvSign.setText(spannableString);
                    tvSign.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
        }
    }


    private onCloudCheckedChangeListener mCloudChangListener;

    private class onCloudCheckedChangeListener implements OnCheckedChangeListener {
        int mMax = 4;

        int tn = mResources.getColor(R.color.white);
        int tp = mResources.getColor(R.color.sub_menu_bgcolor);

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int pIndex = 0;
            int size = getListSize();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    if (mMusicApiList.get(i).getMenu().hashCode() == checkedId) {
                        pIndex = i;
                        break;
                    }
                }
                //判断是不是本地
                if (mMusicLayoutType == TYPE_MUSIC_MANY) {
                    if (checkedId == localHash) {
                        pIndex = 0;
                    } else {
                        pIndex++;
                    }
                }

                if (pIndex > mMax) {
                    // 单个item 左右(padding+margin)
                    hsvMenuScroll
                            .setScrollX((mRgMusicGroup.getChildAt(0).getWidth() + mPadding
                                    * mMax)
                                    * (pIndex - mMax));
                } else {
                    hsvMenuScroll.setScrollX(0);
                }

                for (int j = 0; j < mRgMusicGroup.getChildCount(); j++) {

                    RadioButton rb = (RadioButton) mRgMusicGroup.getChildAt(j);
                    if (null != rb) {
                        rb.setTextColor((j == pIndex) ? tp : tn);
                    }
                }
                setcheckedYUN(pIndex);
            }
        }
    }

    ;

    /**
     * 让容器天充满整个横屏(match 无效,必须设置widthpix)
     */
    private void setRadioGroupFill() {
        int w = CoreUtils.getMetrics().widthPixels;
        mRgMusicGroup.setLayoutParams(new android.widget.LinearLayout.LayoutParams(w, CoreUtils.dpToPixel(50)));
    }

    @Override
    protected void onResume() {
        this.registerReceiver(mReceiver, mIntentFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mModel.recycle();
        mReceiver = null;
        mRgMusicGroup.setOnCheckedChangeListener(null);
        mCloudChangListener = null;
        SDMusicData.getInstance().close();
        WebMusicData.getInstance().close();
        if (null != mMusicApiList) {
            for (int i = 0; i < mMusicApiList.size(); i++) {
                ArrayList<WebMusicInfo> tep = mMusicApiList.get(i).getWebs();
                if (null != tep) {
                    tep.clear();
                }
                mMusicApiList.remove(i);
            }
            mMusicApiList.clear();
        }
    }

    private void setchecked(int paramInt) {

        switch (paramInt) {
            case 0:
                mTvTitle.setText(R.string.activity_label_select_music);
                mMyMusicFragment.onReLoad();
                mLocalFragment.onPause();
                mHistoryMusicFragment.onPause();
                break;
            case 1:
                mMyMusicFragment.onPause();
                mHistoryMusicFragment.onPause();
                break;
            case 2:
                mMyMusicFragment.onPause();
                mLocalFragment.onPause();
                break;
            default:
                break;

        }
        mCurFragmentItem = paramInt;
        if (mViewPager.getCurrentItem() != paramInt) {
            mViewPager.setCurrentItem(paramInt, true);
        }

    }

    private void setcheckedYUN(int paramInt) {
        if (mViewPager.getCurrentItem() != paramInt) {
            mViewPager.setCurrentItem(paramInt, true);
        }
        if (null != mPageAdapter) {
            mPageAdapter.setChecked(mCurFragmentItem, paramInt);
        }
        mCurFragmentItem = paramInt;

    }

    private MyMusicFragment mMyMusicFragment;
    private LocalFragment mLocalFragment;
    private HistoryMusicFragment mHistoryMusicFragment;

    private class MPageAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments = new ArrayList<Fragment>();

        public MPageAdapter(FragmentManager fm) {
            super(fm);
            if (mMusicLayoutType == TYPE_MUSIC_YUN) {

            } else if (mMusicLayoutType == TYPE_MUSIC_LOCAL) {
                mLocalFragment = new LocalFragment();
            } else if (mMusicLayoutType == TYPE_MUSIC_MANY) {
                mLocalFragment = new LocalFragment();
            } else {
                if (fm.getFragments() != null && fm.getFragments().size() > 0) {
                    for (int n = 0; n < fm.getFragments().size(); n++) {
                        Fragment fragment = fm.getFragments().get(n);
                        if (fragment instanceof MyMusicFragment) {
                            mMyMusicFragment = (MyMusicFragment) fragment;
                        } else if (fragment instanceof LocalMusicFragment) {
                            mLocalFragment = (LocalFragment) fragment;
                        } else if (fragment instanceof HistoryMusicFragment) {
                            mHistoryMusicFragment = (HistoryMusicFragment) fragment;
                        }
                    }
                } else {
                    mMyMusicFragment = new MyMusicFragment();
                    mLocalFragment = new LocalFragment();
                    mHistoryMusicFragment = new HistoryMusicFragment();
                }
            }
            if (mMusicLayoutType == TYPE_MUSIC_LOCAL) {
                fragments.add(mLocalFragment);
            } else if (mMusicLayoutType == TYPE_MUSIC_YUN || mMusicLayoutType == TYPE_MUSIC_MANY) {
                if (mMusicLayoutType == TYPE_MUSIC_MANY) {
                    fragments.add(mLocalFragment);
                }
                for (int i = 0; i < mMusicApiList.size(); i++) {
                    if (!TextUtils.isEmpty(mSoundTypeUrl)) {
                        //新版云音乐-支持分页
                        CloudSoundFragment fragment = new CloudSoundFragment();
                        //全部加载完毕后再取消等待框
                        fragment.setListener(new CloudSoundFragment.LoadingListener() {
                            @Override
                            public void onComplete() {
                                mDownLoad++;
                                if (mInitLoad && mDownLoad == mMusicApiList.size()) {
                                    SysAlertDialog.cancelLoadingDialog();
                                }
                            }
                        });
                        fragment.setSound(mClassification.get(i), mSoundUrl, ModeDataUtils.TYPE_YUN_CLOUD_MUSIC);
                        fragments.add(fragment);
                    } else {  //需要旧版素材管理  --没有分页
                        CloudMusicFragment fragment = new CloudMusicFragment();
                        fragment.setIMusic(mMusicApiList.get(i));
                        fragments.add(fragment);
                    }
                }
            } else if (mMusicLayoutType == TYPE_MUSIC_SOUND) {
                for (int i = 0; i < mMusicApiList.size(); i++) {
                    CloudSoundFragment fragment = new CloudSoundFragment();
                    //全部加载完毕后再取消等待框
                    fragment.setListener(new CloudSoundFragment.LoadingListener() {
                        @Override
                        public void onComplete() {
                            mDownLoad++;
                            if (mInitLoad && mDownLoad == mMusicApiList.size()) {
                                SysAlertDialog.cancelLoadingDialog();
                            }
                        }
                    });
                    fragment.setSound(mClassification.get(i), mSoundUrl, ModeDataUtils.TYPE_YUN_AUDIO_EFFECT);
                    fragments.add(fragment);
                }
            } else {
                fragments.add(mMyMusicFragment);
                fragments.add(mLocalFragment);
                fragments.add(mHistoryMusicFragment);
            }
        }

        public void setChecked(int lastid, int newId) {
            if (mMusicLayoutType == TYPE_MUSIC_YUN) {
                Fragment yi = fragments.get(lastid);
                if (null != yi) {
                    yi.onPause();
                }
                yi = fragments.get(newId);
                if (null != yi) {
                    yi.onResume();
                }
            } else if (mMusicLayoutType == TYPE_MUSIC_SOUND) {
                Fragment yi = fragments.get(lastid);
                if (null != yi) {
                    yi.onPause();
                }
                yi = fragments.get(newId);
                if (null != yi) {
                    yi.onResume();
                }
            } else if (mMusicLayoutType == TYPE_MUSIC_MANY) {
                Fragment yi = fragments.get(lastid);
                if (null != yi) {
                    yi.onPause();
                }
                yi = fragments.get(newId);
                if (null != yi) {
                    yi.onResume();
                }
            }
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int paramInt) {
            return fragments.get(paramInt);
        }
    }

    private void initView() {
        mTvTitle.setText(R.string.activity_label_select_music);
        if (mMusicLayoutType == TYPE_MUSIC_SOUND) {
            mTvTitle.setText(R.string.activity_label_effect_music);
        }
        mBtnRight.setBackgroundResource(0);
        mBtnRight.setVisibility(View.GONE);
        mBtnLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.public_menu_cancel, 0, 0, 0);

        mBtnLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        mBtnRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBtnSure();
            }
        });


        if (mMusicLayoutType != TYPE_MUSIC_YUN
                && mMusicLayoutType != TYPE_MUSIC_SOUND
                && mMusicLayoutType != TYPE_MUSIC_MANY) {
            if (mCurFragmentItem != 1) {
                initViewPager();
            } else {
                mViewPager.post(new Runnable() {
                    @Override
                    public void run() {
                        initViewPager();
                    }
                });
            }
        }

    }

    private int getListSize() {
        if (null != mMusicApiList) {
            return mMusicApiList.size();
        }
        return 0;
    }

    private void initViewPager() {
        if (mPageAdapter == null) {
            mPageAdapter = new MPageAdapter(getSupportFragmentManager());
        }
        mViewPager.setAdapter(mPageAdapter);
        if (mMusicLayoutType == TYPE_MUSIC_LOCAL) {

        } else if (mMusicLayoutType == TYPE_MUSIC_YUN || mMusicLayoutType == TYPE_MUSIC_SOUND) {
            int len = getListSize();
            if (len > 0) {
                mViewPager.setOffscreenPageLimit(len);
                mRgMusicGroup.check(mMusicApiList.get(0).getMenu().hashCode());
            }
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                public void onPageScrollStateChanged(int paramAnonymousInt) {
                }

                public void onPageScrolled(int paramAnonymousInt1,
                                           float paramAnonymousFloat, int paramAnonymousInt2) {

                }

                public void onPageSelected(int arg0) {

                    if (getListSize() > 0) {
                        int id = mMusicApiList.get(arg0).getMenu().hashCode();
                        mRgMusicGroup.check(id);
                    }
                }
            });
        } else if (mMusicLayoutType == TYPE_MUSIC_MANY) {
            int len = getListSize();
            mViewPager.setOffscreenPageLimit(len + 1);
            if (len > 0) {
                mRgMusicGroup.check(mMusicApiList.get(0).getMenu().hashCode());
            } else {
                mRgMusicGroup.check(localHash);
            }
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                public void onPageScrollStateChanged(int paramAnonymousInt) {
                }

                public void onPageScrolled(int paramAnonymousInt1,
                                           float paramAnonymousFloat, int paramAnonymousInt2) {

                }

                public void onPageSelected(int arg0) {
                    if (arg0 == 0) {
                        mRgMusicGroup.check(localHash);
                    } else {
                        int id = mMusicApiList.get(arg0 - 1).getMenu().hashCode();
                        mRgMusicGroup.check(id);
                    }
                }
            });
        } else {
            mViewPager.setOffscreenPageLimit(3);
            mCurFragmentItem = 1;
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                public void onPageScrollStateChanged(int paramAnonymousInt) {
                }

                public void onPageScrolled(int paramAnonymousInt1,
                                           float paramAnonymousFloat, int paramAnonymousInt2) {

                }

                public void onPageSelected(int paramAnonymousInt) {
                    if (paramAnonymousInt == 0) {
                        mRgMusicGroup.check(R.id.rbMidSide);
                    } else if (paramAnonymousInt == 1) {
                        mRgMusicGroup.check(R.id.rbLocalMusic);
                    } else if (paramAnonymousInt == 2) {
                        mRgMusicGroup.check(R.id.rbHistoryMusic);
                    }

                }
            });
            mViewPager.setCurrentItem(mCurFragmentItem);
            mRgMusicGroup.check(R.id.rbLocalMusic);
        }

    }

    /**
     * 响应选择完成
     */
    private void onBtnSure() {
        Intent intent = new Intent();
        intent.putExtra(MUSIC_INFO, mTempWebMusic);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void clickView(View v) {
        int id = v.getId();
        if (id == R.id.public_menu_cancel) {
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public static final String MUSIC_INFO = "musicinfo.....";
    public static final String ACTION_MUSICINFO_STRING_WEB = "webmusicinfo";
    public static final String CONTENT_STRING = "content_string",
            ACTION_GONE_STRING = "action_gone_string",
            VISIBIBLE_EXTRA = "visibible_extra", ACTION_TITLE = "action_title",
            ACTION_RIGHT = "action_right", ACTION_ADD_LISTENER = "item_add";

    private AudioMusicInfo mTempWebMusic;
    private boolean mLastWebVisible = true;

    private class TitleReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(ACTION_TITLE)) {
                String title = intent.getStringExtra(CONTENT_STRING);
                if (mViewPager.getCurrentItem() == 1) {
                    mTvTitle.setText(title);
                } else {
                    mTvTitle.setText(R.string.activity_label_select_music);
                }
            } else if (action.equals(ACTION_RIGHT)) {

                String data = intent.getStringExtra(CONTENT_STRING);
                mBtnRight.setVisibility(View.VISIBLE);
                mBtnRight.setText(data);
                if (TextUtils.equals(data, getString(R.string.right))) {
                    mBtnRight.setTextColor(getResources().getColor(R.color.edit_rightbtn_textcolor));
                    mBtnRight.setEnabled(true);
                } else {
                    mBtnRight.setTextColor(getResources().getColor(R.color.transparent_white));
                    mBtnRight.setEnabled(false);
                }

            } else if (action.equals(ACTION_MUSICINFO_STRING_WEB)) {
                mTempWebMusic = intent.getParcelableExtra(CONTENT_STRING);

            } else if (action.equals(ACTION_GONE_STRING)) {
                mLastWebVisible = intent.getBooleanExtra(VISIBIBLE_EXTRA, true);
                mRgMusicGroup.setVisibility(mLastWebVisible ? View.VISIBLE : View.GONE);
            } else if (action.equals(ACTION_ADD_LISTENER)) {
                mTempWebMusic = intent.getParcelableExtra(CONTENT_STRING);
                onBtnSure();

            } else {
                LogUtil.i(TAG, "TitleReceiver" + intent.getAction());
            }

        }
    }

    private int localHash;

}
