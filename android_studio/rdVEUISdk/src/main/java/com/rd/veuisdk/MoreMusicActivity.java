package com.rd.veuisdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.LayoutParams;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.http.MD5;
import com.rd.http.NameValuePair;
import com.rd.lib.ui.ExtButton;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.InputUtls;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.net.RdHttpClient;
import com.rd.veuisdk.database.HistoryMusicCloud;
import com.rd.veuisdk.database.SDMusicData;
import com.rd.veuisdk.database.WebMusicData;
import com.rd.veuisdk.fragment.CloudMusicFragment;
import com.rd.veuisdk.fragment.HistoryMusicFragment;
import com.rd.veuisdk.fragment.LocalMusicFragment;
import com.rd.veuisdk.fragment.MyMusicFragment;
import com.rd.veuisdk.model.AudioMusicInfo;
import com.rd.veuisdk.model.IMusicApi;
import com.rd.veuisdk.model.WebMusicInfo;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.SysAlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 更多音乐
 *
 * @author jian
 */
public class MoreMusicActivity extends BaseActivity {
    public static final String PARAM_TYPE = "menu";
    public static final String PARAM_CLOUDMUSIC = "mCloudMusicUrl";
    public static final int TYPE_MUSIC_1 = 0;// 配乐方式1
    public static final int TYPE_MUSIC_LOCAL = 1;// 配乐方式2 -》本地
    public static final int TYPE_MUSIC_YUN = 2;// 配乐方式2->云音乐

    @BindView(R2.id.vpMusicMain)
    ViewPager mVpMusicMain;
    @BindView(R2.id.rgMusicGroup)
    RadioGroup mRgMusicGroup;
    @BindView(R2.id.hsvMenu)
    HorizontalScrollView hsvMenuScroll;
    @BindView(R2.id.btnRight)
    ExtButton mBtnRight;
    @BindView(R2.id.btnLeft)
    ExtButton mBtnLeft;
    @BindView(R2.id.tvTitle)
    TextView mTvTitle;

    private IntentFilter mIntentFilter;
    private TitleReceiver mReceiver;
    private int mCurFragmentItem = 1;
    private MPageAdapter mPageAdapter;


    private int mMusicLayoutType = TYPE_MUSIC_1;
    private Resources mResources;

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

    // private CloudMusicFragment search;
    private String mCloudMusicUrl = null;
    private ArrayList<IMusicApi> mMusicApiList = new ArrayList<IMusicApi>();
    private File mFile;


    private void getMusic() {
        mFile = new File(getCacheDir(), MD5.getMD5("CloudMusicUrl.json"));


        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                addUsed();
                boolean needLoadLocal = true;
                if (CoreUtils.checkNetworkInfo(MoreMusicActivity.this) != CoreUtils.UNCONNECTED) {
                    String result = RdHttpClient.PostJson(mCloudMusicUrl,
                            new NameValuePair("type", "android"));
                    if (!TextUtils.isEmpty(result)) {
                        onParseJson(result);
                        try {
                            String data = URLEncoder.encode(result, "UTF-8");
                            FileUtils.writeText2File(data, mFile.getAbsolutePath());
                            needLoadLocal = false;
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (needLoadLocal && null != mFile && mFile.exists()) {// 加载离线数据
                    String offline = FileUtils.readTxtFile(mFile
                            .getAbsolutePath());
                    try {
                        offline = URLDecoder.decode(offline, "UTF-8");
                        if (!TextUtils.isEmpty(offline)) {
                            onParseJson(offline);
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                }
                mHandler.obtainMessage(MSG_API).sendToTarget();


            }
        });

    }

    /**
     * 添加历史记录
     */
    private void addUsed() {
        // 暂时屏蔽，未作删除
        // ArrayList<IMusic> temp = HistoryMusicCloud.getInstance().queryAll();
        // int len = temp.size();
        // if (len > 0) {
        // IMusic imusic;
        // ArrayList<WebMusicInfo> twebs = new ArrayList<WebMusicInfo>();
        // IMusicApi history = new IMusicApi(getString(R.string.used), twebs);
        // WebMusicInfo mWebMusicInfo;
        // for (int i = 0; i < len; i++) {
        // imusic = temp.get(i);
        // mWebMusicInfo = new WebMusicInfo();
        // mWebMusicInfo.setLocalPath(imusic.getPath());
        // mWebMusicInfo.setMusicName(imusic.getName());
        // mWebMusicInfo.setDuration(imusic.getDuration());
        // twebs.add(mWebMusicInfo);
        // }
        // mMusicApiList.add(history);
        // }
    }

    private void onParseJson(String result) {
        try {
            JSONObject jobj = new JSONObject(result);
            if (jobj.optBoolean("state", false)) {
                jobj = jobj.optJSONObject("result");
                JSONArray jarr = jobj.optJSONArray("bgmusic");
                int len = 0;
                if (null != jarr && (len = jarr.length()) > 0) {
                    for (int i = 0; i < len; i++) {
                        mMusicApiList.add(new IMusicApi(jarr.getJSONObject(i)));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private final int MSG_API = 568;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MSG_API) {

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
                    for (int i = 0; i < len; i++) {
                        createRadioButton(mMusicApiList.get(i).getMenu(), lpItem);
                    }
                    initViewPager();
                    SysAlertDialog.cancelLoadingDialog();
                } else {
                    SysAlertDialog.cancelLoadingDialog();
                    SysAlertDialog.showAutoHideDialog(MoreMusicActivity.this,
                            0, R.string.load_http_failed, Toast.LENGTH_SHORT);
                }
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResources = getResources();
        Intent in = getIntent();
        mMusicLayoutType = in.getIntExtra(PARAM_TYPE, TYPE_MUSIC_1);
        mCloudMusicUrl = in.getStringExtra(PARAM_CLOUDMUSIC);


        mReceiver = new TitleReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ACTION_RIGHT);
        mIntentFilter.addAction(ACTION_TITLE);
        mIntentFilter.addAction(ACTION_MUSICINFO_STRING_WEB);
        mIntentFilter.addAction(ACTION_GONE_STRING);
        mIntentFilter.addAction(ACTION_ADD_LISTENER);

        mStrActivityPageName = getString(R.string.moreaudio);
        SDMusicData.getInstance().initilize(this);
        WebMusicData.getInstance().initilize(this);
        setContentView(R.layout.activity_more_music);
        ButterKnife.bind(this);

        if (mMusicLayoutType == TYPE_MUSIC_YUN) {
            HistoryMusicCloud.getInstance().initilize(this);
            mCurFragmentItem = 0;
            mRgMusicGroup.removeAllViews();
            SysAlertDialog.showLoadingDialog(this, R.string.isloading);
            getMusic();
            initSearch();
            mCloudChangListener = new onCloudCheckedChangeListener();
            mRgMusicGroup.setOnCheckedChangeListener(mCloudChangListener);
        } else if (mMusicLayoutType == TYPE_MUSIC_LOCAL) {
            mRgMusicGroup.setVisibility(View.GONE);

        } else {
            setRadioGroupFill();

            mRgMusicGroup
                    .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(RadioGroup group,
                                                     int checkedId) {
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

    private void initSearch() {
        // editText.setOnEditorActionListener(new OnEditorActionListener() {
        //
        // @Override
        // public boolean onEditorAction(TextView v, int actionId,
        // KeyEvent event) {
        // Log.e("action-->", actionId + "");
        // if (actionId == KeyEvent.ACTION_DOWN) {
        // onSearch(editText.getText().toString(), editText);
        // }
        // return false;
        // }
        // });
        // editText.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // Log.e("setOnClickListener-->", "...");
        //
        // onVpVisible(true);
        // }
        // });
        // editText.setOnFocusChangeListener(new OnFocusChangeListener() {
        //
        // @Override
        // public void onFocusChange(View v, boolean hasFocus) {
        // // int vi = hasFocus ? View.GONE : View.VISIBLE;
        // // mRgMusicGroup.setVisibility(vi);
        // // mVpMusicMain.setVisibility(vi);
        // Log.e("onFocusChange-->",
        // hasFocus + "--" + editText.isFocused());
        // }
        // });

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
                // Log.e("setOnCheckedChangeListener", checkedId
                // + "----" + pIndex);
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
        mRgMusicGroup
                .setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                        w, CoreUtils.dpToPixel(50)));
    }

    /**
     * * 响应搜索
     *
     * @param text
     * @param edit
     */
    void onSearch(String text, EditText edit) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        InputUtls.hideKeyboard(edit);
        // searchLayout.setVisibility(View.VISIBLE);
        // if (null == search) {
        // search = new CloudMusicFragment();
        // }
        // getSupportFragmentManager().beginTransaction()
        // .replace(R.id.searched_layout, search)
        // .commitAllowingStateLoss();

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

    /**
     * @param inputOpen
     */
    private void onVpVisible(boolean inputOpen) {
        int vi = inputOpen ? View.GONE : View.VISIBLE;
        mRgMusicGroup.setVisibility(vi);
        mVpMusicMain.setVisibility(vi);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReceiver = null;
        mRgMusicGroup.setOnCheckedChangeListener(null);
        if (null != mCloudChangListener) {
            mCloudChangListener = null;
        }
        SDMusicData.getInstance().close();
        WebMusicData.getInstance().close();
        if (null != mMusicApiList) {
            for (int i = 0; i < mMusicApiList.size(); i++) {
                ArrayList<WebMusicInfo> tep = mMusicApiList.get(i).getWebs();
                if (null != tep) {
                    tep.clear();
                    tep = null;
                }
                mMusicApiList.remove(i);
            }
            mMusicApiList.clear();
            mMusicApiList = null;
        }
    }

    private void setchecked(int paramInt) {

        switch (paramInt) {
            case 0:
                mTvTitle.setText(R.string.activity_label_select_music);

                mMyMusicFragment.onReLoad();
                mLocalMusicFragment.onPause();
                mHistoryMusicFragment.onPause();
                break;
            case 1:
                mMyMusicFragment.onPause();
                mHistoryMusicFragment.onPause();
                break;
            case 2:
                mMyMusicFragment.onPause();
                mLocalMusicFragment.onPause();
                break;
            default:
                break;

        }
        mCurFragmentItem = paramInt;
        if (mVpMusicMain.getCurrentItem() != paramInt) {
            mVpMusicMain.setCurrentItem(paramInt, true);
        }

    }

    private void setcheckedYUN(int paramInt) {
        // Log.e("setcheckedYUN", mVpMusicMain.getCurrentItem() + "......." + paramInt);
        if (mVpMusicMain.getCurrentItem() != paramInt) {
            mVpMusicMain.setCurrentItem(paramInt, true);
        }
        if (null != mPageAdapter) {
            mPageAdapter.setChecked(mCurFragmentItem, paramInt);
        }
        mCurFragmentItem = paramInt;

    }

    private MyMusicFragment mMyMusicFragment;
    private LocalMusicFragment mLocalMusicFragment;
    private HistoryMusicFragment mHistoryMusicFragment;

    private class MPageAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments = new ArrayList<Fragment>();

        public MPageAdapter(FragmentManager fm) {
            super(fm);
            if (mMusicLayoutType == TYPE_MUSIC_YUN) {

            } else if (mMusicLayoutType == TYPE_MUSIC_LOCAL) {
                mLocalMusicFragment = new LocalMusicFragment();
            } else {
                if (fm.getFragments() != null && fm.getFragments().size() > 0) {
                    for (int n = 0; n < fm.getFragments().size(); n++) {
                        Fragment fragment = fm.getFragments().get(n);
                        if (fragment instanceof MyMusicFragment) {
                            mMyMusicFragment = (MyMusicFragment) fragment;
                        } else if (fragment instanceof LocalMusicFragment) {
                            mLocalMusicFragment = (LocalMusicFragment) fragment;
                        } else if (fragment instanceof HistoryMusicFragment) {
                            mHistoryMusicFragment = (HistoryMusicFragment) fragment;
                        }
                    }
                } else {

                    mMyMusicFragment = new MyMusicFragment();

                    mLocalMusicFragment = new LocalMusicFragment();

                    mHistoryMusicFragment = new HistoryMusicFragment();

                }
            }
            if (mMusicLayoutType == TYPE_MUSIC_LOCAL) {
                this.fragments.add(mLocalMusicFragment);
            } else if (mMusicLayoutType == TYPE_MUSIC_YUN) {
                for (int i = 0; i < mMusicApiList.size(); i++) {
                    CloudMusicFragment yitem = new CloudMusicFragment();
                    yitem.setIMusic(mMusicApiList.get(i));
                    this.fragments.add(yitem);
                }
            } else {
                this.fragments.add(mMyMusicFragment);
                this.fragments.add(mLocalMusicFragment);
                this.fragments.add(mHistoryMusicFragment);
            }
        }

        public void setChecked(int lastid, int newId) {
            CloudMusicFragment yi = (CloudMusicFragment) fragments
                    .get(lastid);
            if (null != yi) {
                yi.onPause();
            }
            yi = (CloudMusicFragment) fragments.get(newId);
            if (null != yi) {
                yi.onResume();
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
        mBtnRight.setBackgroundResource(0);
        mBtnRight.setText(R.string.string_null);
        mBtnRight.setVisibility(View.GONE);
        mBtnLeft.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.public_menu_cancel, 0, 0, 0);

        mBtnLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        mBtnRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onOK();
            }
        });


        if (mMusicLayoutType != TYPE_MUSIC_YUN) {
            if (mCurFragmentItem != 1) {
                initViewPager();
            } else {
                mVpMusicMain.post(new Runnable() {
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
        mVpMusicMain.setAdapter(mPageAdapter);

        if (mMusicLayoutType == TYPE_MUSIC_LOCAL) {

        } else if (mMusicLayoutType == TYPE_MUSIC_YUN) {
            int len = getListSize();
            if (len > 0) {
                mVpMusicMain.setOffscreenPageLimit(len);
                mRgMusicGroup.check(mMusicApiList.get(0).getMenu().hashCode());
            }
            mVpMusicMain.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                public void onPageScrollStateChanged(int paramAnonymousInt) {
                    // Log.e("onPageScrollStateChanged...", "/。。。"
                    // + paramAnonymousInt);
                }

                public void onPageScrolled(int paramAnonymousInt1,
                                           float paramAnonymousFloat, int paramAnonymousInt2) {
                    // Log.e("onPageScrolled...", paramAnonymousInt2 +
                    // "        "
                    // + paramAnonymousInt1);

                }

                public void onPageSelected(int arg0) {

                    if (getListSize() > 0) {
                        int id = mMusicApiList.get(arg0).getMenu().hashCode();
                        mRgMusicGroup.check(id);
                    }
                }
            });
        } else {
            mVpMusicMain.setOffscreenPageLimit(3);
            mCurFragmentItem = 1;
            mVpMusicMain.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                public void onPageScrollStateChanged(int paramAnonymousInt) {
                    // Log.e("onPageScrollStateChanged...", "/。。。" +
                    // paramAnonymousInt);
                }

                public void onPageScrolled(int paramAnonymousInt1,
                                           float paramAnonymousFloat, int paramAnonymousInt2) {
                    // Log.e("onPageScrolled...", paramAnonymousInt2 +
                    // "        "
                    // + paramAnonymousInt1);

                }

                public void onPageSelected(int paramAnonymousInt) {
                    // Log.e("onPageSelected...", paramAnonymousInt +
                    // "   /。。。");
                    if (paramAnonymousInt == 0) {
                        mRgMusicGroup.check(R.id.rbMidSide);
                    } else if (paramAnonymousInt == 1) {
                        mRgMusicGroup.check(R.id.rbLocalMusic);
                    } else if (paramAnonymousInt == 2) {
                        mRgMusicGroup.check(R.id.rbHistoryMusic);
                    }

                }
            });
            mVpMusicMain.setCurrentItem(mCurFragmentItem);
            mRgMusicGroup.check(R.id.rbLocalMusic);
        }

    }

    /**
     * 响应选择完成
     */
    protected void onOK() {

        // AudioMusicInfo info = null;
        // if (mVpMusicMain.getCurrentItem() == 1) {
        // info = mTempWebMusic;
        // } else {
        // info = mMyMusicFragment.getCheckMusicInfo();
        // }
        Intent i = new Intent();
        i.putExtra(MUSIC_INFO, mTempWebMusic);
        setResult(RESULT_OK, i);
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
        // if (mRgMusicGroup.getVisibility() != View.VISIBLE) {
        // if (null != search) {
        // getSupportFragmentManager().beginTransaction().remove(search)
        // .commitAllowingStateLoss();
        // }
        // if (null != searchLayout) {
        // searchLayout.setVisibility(View.GONE);
        // }
        // if (null != editText) {
        // editText.setText("");
        // }
        // onVpVisible(false);
        // return;
        // }
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
            // Log.e("action...", action);
            if (action.equals(ACTION_TITLE)) {
                String mwebTitle = intent.getStringExtra(CONTENT_STRING);
                if (mVpMusicMain.getCurrentItem() == 1) {
                    mTvTitle.setText(mwebTitle);
                } else {
                    mTvTitle.setText(R.string.activity_label_select_music);
                }
            } else if (action.equals(ACTION_RIGHT)) {

                String data = intent.getStringExtra(CONTENT_STRING);
                mBtnRight.setVisibility(View.VISIBLE);
                mBtnRight.setText(data);
                if (TextUtils.equals(data, getString(R.string.right))) {
                    mBtnRight.setTextColor(getResources().getColor(
                            R.color.edit_rightbtn_textcolor));
                    mBtnRight.setEnabled(true);
                } else {
                    mBtnRight.setTextColor(getResources().getColor(
                            R.color.transparent_white));
                    mBtnRight.setEnabled(false);
                }

            } else if (action.equals(ACTION_MUSICINFO_STRING_WEB)) {
                mTempWebMusic = intent.getParcelableExtra(CONTENT_STRING);

            } else if (action.equals(ACTION_GONE_STRING)) {
                mLastWebVisible = intent.getBooleanExtra(VISIBIBLE_EXTRA, true);
                mRgMusicGroup.setVisibility(mLastWebVisible ? View.VISIBLE
                        : View.GONE);
            } else if (action.equals(ACTION_ADD_LISTENER)) {
                mTempWebMusic = intent.getParcelableExtra(CONTENT_STRING);
                onOK();

            } else {
                Log.d("weizhi ...", intent.getAction());
            }

        }
    }
}
