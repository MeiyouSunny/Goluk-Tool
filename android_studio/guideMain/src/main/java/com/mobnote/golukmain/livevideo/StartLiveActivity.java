package com.mobnote.golukmain.livevideo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserLoginActivity;
import com.mobnote.golukmain.internation.login.InternationUserLoginActivity;
import com.mobnote.golukmain.live.LiveSettingBean;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;

/**
 * 直播设置页面
 * Created by leege100 on 16/7/13.
 */
public class StartLiveActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private ImageView mBackIv;
    private TextView mLiveDurationTv;
    private TextView mLiveConsumeFlowTv;
    private SeekBar mLiveDurationSeekBar;

    private Button mEnablePublicBtn;
    private Button mEnableVoiceBtn;
    private Button mEnableSaveLiveBtn;

    private EditText mDescriptionEt;
    private Button mStartLiveBtn;

    private TextView mDescWordCount;
    private boolean mIsLive = false;

    /**
     * 默认直播时长
     */
    private final int DEFAULT_SECOND = 30 * 60;
    private final int MAX_SECOND = 30 * 60;

    /**
     * 直播时长
     */
    private int mCurrentLiveSecond = DEFAULT_SECOND;

    private LiveSettingBean mLiveSettingBean;

    public static final String CURR_LON = "curr_lon";
    public static final String CURR_LAT = "curr_lat";
    public static final String SHORT_LOCATION = "short_location";

    private String mDescStr;
    private final int MAX_DESCRIPTION_COUNT = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_live);
        initData();
        initView();
        setupView();
    }

    private void initData() {
        mLiveSettingBean = new LiveSettingBean();
        mLiveSettingBean.vtype = 0;
//        mBaseApp.mIPCControlManager.setT3WifiMode(0);

        // 时长
        mLiveSettingBean.duration = mCurrentLiveSecond;
        mLiveSettingBean.netCountStr = getCurrentFlow(mCurrentLiveSecond);
        mLiveSettingBean.isEnablePublic = true;
        mLiveSettingBean.isEnableVoice = true;
        mLiveSettingBean.isEnableSaveReplay = false;

        Intent intent = getIntent();
        mLiveSettingBean.shortLocation = intent.getStringExtra(SHORT_LOCATION);
        mLiveSettingBean.lat = intent.getDoubleExtra(CURR_LAT, 0.0);
        mLiveSettingBean.lon = intent.getDoubleExtra(CURR_LON, 0.0);
    }

    private void setupView() {
        mBackIv.setOnClickListener(this);
        mEnablePublicBtn.setOnClickListener(this);
        mEnableVoiceBtn.setOnClickListener(this);
        mEnableSaveLiveBtn.setOnClickListener(this);
        mStartLiveBtn.setOnClickListener(this);
        mLiveDurationSeekBar.setOnSeekBarChangeListener(this);
        mDescriptionEt.addTextChangedListener(mTextWatcher);

        mLiveDurationTv.setText(GolukUtils.secondToString(mCurrentLiveSecond));
        mLiveConsumeFlowTv.setText(getCurrentFlow(mCurrentLiveSecond));
    }

    private void initView() {

        mBackIv = (ImageView) findViewById(R.id.ib_live_back);
        mLiveDurationTv = (TextView) findViewById(R.id.tv_live_duration);
        mDescWordCount = (TextView) findViewById(R.id.tv_live_desc_wordcount);
        mLiveConsumeFlowTv = (TextView) findViewById(R.id.tv_live_consumeflow);
        mLiveDurationSeekBar = (SeekBar) findViewById(R.id.seekbar_duration);

        mEnablePublicBtn = (Button) findViewById(R.id.btn_enable_public_live);
        mEnableVoiceBtn = (Button) findViewById(R.id.btn_enable_live_voice);
        mEnableSaveLiveBtn = (Button) findViewById(R.id.btn_enable_save_live);
        mDescriptionEt = (EditText) findViewById(R.id.et_live_description);
        mStartLiveBtn = (Button) findViewById(R.id.btn_start_live);

        if (mLiveSettingBean.isEnablePublic) {
            mEnablePublicBtn.setBackgroundResource(R.drawable.set_open_btn);
        } else {
            mEnablePublicBtn.setBackgroundResource(R.drawable.set_close_btn);
        }

        if (mLiveSettingBean.isEnableSaveReplay) {
            mEnableSaveLiveBtn.setBackgroundResource(R.drawable.set_open_btn);
        } else {
            mEnableSaveLiveBtn.setBackgroundResource(R.drawable.set_close_btn);
        }

        if (mLiveSettingBean.isEnableVoice) {
            mEnableVoiceBtn.setBackgroundResource(R.drawable.set_open_btn);
        } else {
            mEnableVoiceBtn.setBackgroundResource(R.drawable.set_close_btn);
        }

        mLiveDurationSeekBar.setMax(MAX_SECOND);
        mLiveDurationSeekBar.setProgress(DEFAULT_SECOND);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        int vId = view.getId();
        if (vId == R.id.ib_live_back) {
            this.finish();
        } else if (vId == R.id.btn_enable_public_live) {
            if (mLiveSettingBean.isEnablePublic) {
                mEnablePublicBtn.setBackgroundResource(R.drawable.set_close_btn);
            } else {
                mEnablePublicBtn.setBackgroundResource(R.drawable.set_open_btn);
            }
            mLiveSettingBean.isEnablePublic = !mLiveSettingBean.isEnablePublic;
        } else if (vId == R.id.btn_enable_live_voice) {
            if (mLiveSettingBean.isEnableVoice) {
                mEnableVoiceBtn.setBackgroundResource(R.drawable.set_close_btn);
            } else {
                mEnableVoiceBtn.setBackgroundResource(R.drawable.set_open_btn);
            }
            mLiveSettingBean.isEnableVoice = !mLiveSettingBean.isEnableVoice;
        } else if (vId == R.id.btn_enable_save_live) {
            if (mLiveSettingBean.isEnableSaveReplay) {
                mEnableSaveLiveBtn.setBackgroundResource(R.drawable.set_close_btn);
            } else {
                mEnableSaveLiveBtn.setBackgroundResource(R.drawable.set_open_btn);
            }
            mLiveSettingBean.isEnableSaveReplay = !mLiveSettingBean.isEnableSaveReplay;
        } else if (vId == R.id.btn_start_live) {
            if (!mBaseApp.isIpcConnSuccess) {
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.user_dialog_hint_title)
                        .setMessage(R.string.str_disconnect_ipc)
                        .setPositiveButton(R.string.str_button_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                StartLiveActivity.this.finish();
                            }
                        })
                        .setCancelable(false)
                        .create();
                dialog.show();
                return;
            }
            if (!GolukUtils.isNetworkConnected(this)) {
                showToast(R.string.network_error);
                return;
            }
            if (GolukUtils.isFastDoubleClick()) {
                return;
            }
            if (!mBaseApp.isUserLoginSucess) {
                Intent intent = null;
                if (mBaseApp.isMainland() == false) {
                    intent = new Intent(this, InternationUserLoginActivity.class);
                } else {
                    intent = new Intent(this, UserLoginActivity.class);
                }
                intent.putExtra("isInfo", "back");
                startActivity(intent);
                return;
            }
            String liveDescription = null;
            liveDescription = mDescriptionEt.getText().toString();
            if (liveDescription == null) {
                liveDescription = "";
            }
            mLiveSettingBean.desc = liveDescription;
            //直播页面
            ZhugeUtils.eventLive(this, this.getString(R.string.str_zhuge_live_ipc_page));
            mIsLive = true;
            GolukUtils.startPublishOrWatchLiveActivity(this, true, false, null, mLiveSettingBean, null);
            finish();
        }
    }

    /**
     * 计算本次直播所需要的流量
     */
    private String getCurrentFlow(int progress) {
        int size = (int) (mCurrentLiveSecond * 0.1);
        return "" + size + "MB";
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        if (progress < 180) {
            progress = 180;
        }

        mCurrentLiveSecond = progress;
        mLiveSettingBean.duration = mCurrentLiveSecond;
        mLiveDurationTv.setText(GolukUtils.secondToString(progress));
        mLiveConsumeFlowTv.setText(getCurrentFlow(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    TextWatcher mTextWatcher = new TextWatcher() {
        private CharSequence temp;
        private int editStart;
        private int editEnd;

        @Override
        public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
            temp = s;
        }

        @Override
        public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
            if (s == null) {
                mDescStr = null;
            } else {
                mDescStr = s.toString().trim();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            editStart = mDescriptionEt.getSelectionStart();
            editEnd = mDescriptionEt.getSelectionEnd();
            if (temp.length() > MAX_DESCRIPTION_COUNT) {
                s.delete(editStart - 1, editEnd);
                int tempSelection = editStart;
                mDescriptionEt.setText(s);
                mDescriptionEt.setSelection(tempSelection);
            }
            if (s != null) {
                mDescWordCount.setText(s.length() + "/" + MAX_DESCRIPTION_COUNT);
            } else {
                mDescWordCount.setText(0 + "/" + MAX_DESCRIPTION_COUNT);
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBaseApp.isIpcLoginSuccess && !mIsLive) {
            mBaseApp.setIpcDisconnect();
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                wifiManager.disableNetwork(wifiInfo.getNetworkId());
            }
        }
    }
}
