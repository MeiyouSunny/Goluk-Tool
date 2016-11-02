package com.mobnote.golukmain.carrecorder.settings;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.carrecorder.base.CarRecordBaseActivity;
import com.mobnote.golukmain.carrecorder.entity.VideoConfigState;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 视频质量设置页面
 * <p>
 * 2015年4月7日
 *
 * @author xuhw
 */
@SuppressLint("InflateParams")
public class VideoQualityActivity extends CarRecordBaseActivity implements OnClickListener {
    /**
     * 视频类型文字显示
     */
    private TextView mCloseText = null;
    private TextView mLowText = null;
    private TextView mMiddleText = null;
    private TextView mHighText = null;
    /**
     * 视频类型选中高亮
     */
    private ImageButton mCloseIcon = null;
    private ImageButton mLowIcon = null;
    private ImageButton mMiddleIcon = null;
    private ImageButton mHighIcon = null;

    /**
     * 视频质量类型　1080高 1080低 720高 720低
     */
    public static enum SensitivityType {
        _1080h, _1080l, _720h, _720l
    }

    ;

    /**
     * 音视频配置信息
     */
    private VideoConfigState mVideoConfigState = null;
    /**
     * UI显示
     **/
    private String[] mArrayText = null;
    /**
     * 文字显示
     **/
    private TextView[] mText = null;
    /**
     * 按钮显示
     **/
    private ImageButton[] mImageIcon = null;
    private String selectType = "";
    private String[] resolutionArray = null;
    private String[] bitrateArray = null;
    /**
     * ipc设备型号
     **/
    private String mIPCName = "";
    private int mSelect = 0;

    private RelativeLayout mLayoutHigh720, mLayoutMiddle720;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_video_quality, null));
        setTitle(this.getResources().getString(R.string.spzl_title));

        mIPCName = GolukApplication.getInstance().mIPCControlManager.mProduceName;
        initView();
        setListener();

        mVideoConfigState = GolukApplication.getInstance().getVideoConfigState();
        setData2UI();
    }

    /**
     * 初始化控件
     *
     * @author xuhw
     * @date 2015年4月6日
     */
    private void initView() {
        mCloseText = (TextView) findViewById(R.id.closeText);
        mLowText = (TextView) findViewById(R.id.lowText);
        mMiddleText = (TextView) findViewById(R.id.middleText);
        mHighText = (TextView) findViewById(R.id.highText);
        mCloseIcon = (ImageButton) findViewById(R.id.cRight);
        mLowIcon = (ImageButton) findViewById(R.id.dRight);
        mMiddleIcon = (ImageButton) findViewById(R.id.zRight);
        mHighIcon = (ImageButton) findViewById(R.id.gRight);
        mLayoutHigh720 = (RelativeLayout) findViewById(R.id.rl_video_quality_high720);
        mLayoutMiddle720 = (RelativeLayout) findViewById(R.id.rl_video_quality_middle720);

        mArrayText = getResources().getStringArray(R.array.list_quality_ui);
        mText = new TextView[]{mCloseText, mLowText, mMiddleText, mHighText};
        mImageIcon = new ImageButton[]{mCloseIcon, mLowIcon, mMiddleIcon, mHighIcon};
        resolutionArray = SettingsUtil.returnResolution(this, mIPCName);
        bitrateArray = SettingsUtil.returnBitrate(this, mIPCName);

        getArrays();

        if (IPCControlManager.T1_SIGN.equals(GolukApplication.getInstance().mIPCControlManager.mProduceName)
                || IPCControlManager.T2_SIGN.equals(GolukApplication.getInstance().mIPCControlManager.mProduceName)) {
            mLayoutHigh720.setVisibility(View.GONE);
            mLayoutMiddle720.setVisibility(View.GONE);
        } else {
            mLayoutHigh720.setVisibility(View.VISIBLE);
            mLayoutMiddle720.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置控件监听事件
     *
     * @author xuhw
     * @date 2015年4月7日
     */
    private void setListener() {
        findViewById(R.id.close).setOnClickListener(this);
        findViewById(R.id.low).setOnClickListener(this);
        findViewById(R.id.rl_video_quality_high720).setOnClickListener(this);
        findViewById(R.id.rl_video_quality_middle720).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.back_btn) {
            exit();
        } else if (id == R.id.close) {
            setArrayUI(mArrayText[0]);
        } else if (id == R.id.low) {
            setArrayUI(mArrayText[1]);
        } else if (id == R.id.rl_video_quality_high720) {
            setArrayUI(mArrayText[2]);
        } else if (id == R.id.rl_video_quality_middle720) {
            setArrayUI(mArrayText[3]);
        } else {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        GolukApplication.getInstance().setContext(this, "videoquality");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void exit() {
        if (GolukApplication.getInstance().getIpcIsLogin()) {
            setArrayData();

            GolukApplication.getInstance().setVideoConfigState(mVideoConfigState);
            boolean flag = GolukApplication.getInstance().getIPCControlManager().setVideoEncodeCfg(mVideoConfigState);
            GolukDebugUtils.e("xuhw", "YYY==========curType=========flag=" + flag);
        }

        finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    // 为UI赋值
    private void getArrays() {
        if (null != mArrayText) {
            int length = mArrayText.length;
            for (int i = 0; i < length; i++) {
                for (int j = i; j < mText.length; j++) {
                    mText[j].setText(mArrayText[i]);
                }
            }
        }
    }

    // 点击选择视频质量
    private void setArrayUI(String type) {
        if (null != mArrayText) {
            int length = mArrayText.length;
            for (int i = 0; i < length; i++) {
                mImageIcon[i].setVisibility(View.GONE);
                mText[i].setTextColor(getResources().getColor(R.color.setting_text_color_nor));
                if (type.equals(mArrayText[i])) {
                    mImageIcon[i].setVisibility(View.VISIBLE);
                    mText[i].setTextColor(getResources().getColor(R.color.setting_text_color_sel));
                    selectType = mArrayText[i];
                    mSelect = i;
                }
            }
        }
    }

    // 保存选择的视频质量类型
    private void setArrayData() {
        if (null == mVideoConfigState) {
            return;
        }
        if (selectType.equals(mArrayText[mSelect])) {
            mVideoConfigState.resolution = resolutionArray[mSelect];
            mVideoConfigState.bitrate = Integer.parseInt(bitrateArray[mSelect]);
        }
    }

    // 遍历分辨率，区分码率，改变UI
    private void setData2UI() {
        if (null != mVideoConfigState && null != resolutionArray) {
            for (int i = 0; i < resolutionArray.length; i++) {
                mImageIcon[i].setVisibility(View.GONE);
                mText[i].setTextColor(getResources().getColor(R.color.setting_text_color_nor));
                if (mVideoConfigState.resolution.equals(resolutionArray[i])) {
                    if (null != bitrateArray) {
                        if (String.valueOf(mVideoConfigState.bitrate).equals(bitrateArray[i])) {
                            mImageIcon[i].setVisibility(View.VISIBLE);
                            mText[i].setTextColor(getResources().getColor(R.color.setting_text_color_sel));
                            selectType = mArrayText[i];
                            mSelect = i;
                        }
                    }
                }
            }
        }
    }

}
