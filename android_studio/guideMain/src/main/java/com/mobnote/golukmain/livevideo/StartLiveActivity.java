package com.mobnote.golukmain.livevideo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.live.LiveSettingBean;
import com.mobnote.util.GolukUtils;

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

    /** 默认直播时长 */
    private final int DEFAULT_SECOND = 30 * 60;
    private final int MAX_SECOND = 30 * 60;

    /** 直播时长 */
    private int mCurrentLiveSecond = DEFAULT_SECOND;

    private LiveSettingBean mLiveSettingBean;
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
        // 时长
        mLiveSettingBean.duration = mCurrentLiveSecond;
        mLiveSettingBean.netCountStr = getCurrentFlow(mCurrentLiveSecond);
        mLiveSettingBean.isEnablePublic = true;
        mLiveSettingBean.isEnableVoice = true;
        mLiveSettingBean.isEnableSaveReplay = false;
    }

    private void setupView() {
        mBackIv.setOnClickListener(this);
        mEnablePublicBtn.setOnClickListener(this);
        mEnableVoiceBtn.setOnClickListener(this);
        mEnableSaveLiveBtn.setOnClickListener(this);
        mStartLiveBtn.setOnClickListener(this);
        mLiveDurationSeekBar.setOnSeekBarChangeListener(this);

        mLiveDurationTv.setText(GolukUtils.secondToString(mCurrentLiveSecond));
        mLiveConsumeFlowTv.setText(getCurrentFlow(mCurrentLiveSecond));
    }

    private void initView() {

        mBackIv = (ImageView) findViewById(R.id.ib_live_back);
        mLiveDurationTv = (TextView) findViewById(R.id.tv_live_duration);
        mLiveConsumeFlowTv = (TextView) findViewById(R.id.tv_live_consumeflow);
        mLiveDurationSeekBar = (SeekBar) findViewById(R.id.seekbar_duration);

        mEnablePublicBtn = (Button) findViewById(R.id.btn_enable_public_live);
        mEnableVoiceBtn = (Button) findViewById(R.id.btn_enable_live_voice);
        mEnableSaveLiveBtn = (Button) findViewById(R.id.btn_enable_save_live);
        mDescriptionEt = (EditText) findViewById(R.id.et_live_description);
        mStartLiveBtn = (Button) findViewById(R.id.btn_start_live);

        if(mLiveSettingBean.isEnablePublic){
            mEnablePublicBtn.setBackgroundResource(R.drawable.set_open_btn);
        }else{
            mEnablePublicBtn.setBackgroundResource(R.drawable.set_close_btn);
        }

        if(mLiveSettingBean.isEnableSaveReplay){
            mEnableSaveLiveBtn.setBackgroundResource(R.drawable.set_open_btn);
        }else{
            mEnableSaveLiveBtn.setBackgroundResource(R.drawable.set_close_btn);
        }

        if(mLiveSettingBean.isEnableVoice){
            mEnableVoiceBtn.setBackgroundResource(R.drawable.set_open_btn);
        }else{
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
        if(vId == R.id.ib_live_back){
            this.finish();
        }else if(vId == R.id.btn_enable_public_live){
            if(mLiveSettingBean.isEnablePublic){
                mEnablePublicBtn.setBackgroundResource(R.drawable.set_close_btn);
            }else{
                mEnablePublicBtn.setBackgroundResource(R.drawable.set_open_btn);
            }
            mLiveSettingBean.isEnablePublic = !mLiveSettingBean.isEnablePublic;
        }else if(vId == R.id.btn_enable_live_voice){
            if(mLiveSettingBean.isEnableVoice){
                mEnableVoiceBtn.setBackgroundResource(R.drawable.set_close_btn);
            }else{
                mEnableVoiceBtn.setBackgroundResource(R.drawable.set_open_btn);
            }
            mLiveSettingBean.isEnableVoice = !mLiveSettingBean.isEnableVoice;
        }else if(vId == R.id.btn_enable_save_live){
            if(mLiveSettingBean.isEnableSaveReplay){
                mEnableSaveLiveBtn.setBackgroundResource(R.drawable.set_close_btn);
            }else{
                mEnableSaveLiveBtn.setBackgroundResource(R.drawable.set_open_btn);
            }
            mLiveSettingBean.isEnableSaveReplay = !mLiveSettingBean.isEnableSaveReplay;
        }else if(vId == R.id.btn_start_live){
            GolukUtils.startPublishOrLookLiveActivity(this,true,false,mLiveSettingBean,null);
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
        mLiveDurationTv.setText(GolukUtils.secondToString(progress));
        mLiveConsumeFlowTv.setText(getCurrentFlow(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
