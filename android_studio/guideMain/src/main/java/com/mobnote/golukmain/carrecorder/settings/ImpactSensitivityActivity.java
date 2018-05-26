package com.mobnote.golukmain.carrecorder.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.base.CarRecordBaseActivity;
import com.mobnote.golukmain.carrecorder.util.IpcSettingUtil;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 碰撞感应灵敏度控制
 * <p>
 * 2015年4月6日
 *
 * @author xuhw
 */
@SuppressLint("InflateParams")
public class ImpactSensitivityActivity extends CarRecordBaseActivity implements OnClickListener, IPCManagerFn {
    private TextView mCloseText, mLowText, mMiddleText, mHighText, mLowerText, mHigherText;
    private ImageButton mCloseIcon, mLowIcon, mMiddleIcon, mHighIcon, mLowerIcon, mHigherIcon;

    private int policy = 0;

    // 是否是T系列
    private boolean mIsT3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_impact_sensitivity, null));
        setTitle(this.getResources().getString(R.string.pzgy_title));

        initView();

        mIsT3 = mBaseApp.getIPCControlManager().isT3Relative();

        GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("sensitivity", this);
        if (isSupportTimeslapse()) {
            boolean flag = GolukApplication.getInstance().getIPCControlManager().getGSensorMoreValueCfg();
            GolukDebugUtils.e("xuhw", "YYYYY===getIPCControlManager============getGSensorMoreValueCfg======flag=" + flag);
        } else {
            boolean flag = GolukApplication.getInstance().getIPCControlManager().getGSensorControlCfg();
            GolukDebugUtils.e("xuhw", "YYYYY===getIPCControlManager============getGSensorControlCfg======flag=" + flag);
        }
    }

    /**
     * 初始化控件
     *
     * @author xuhw
     * @date 2015年4月6日
     */
    private void initView() {
        findViewById(R.id.rl_1080p_high).setOnClickListener(this);
        findViewById(R.id.rl_1080p_middle).setOnClickListener(this);
        findViewById(R.id.middle).setOnClickListener(this);
        findViewById(R.id.high).setOnClickListener(this);
        findViewById(R.id.layout_lower).setOnClickListener(this);
        findViewById(R.id.layout_highter).setOnClickListener(this);

        mCloseText = (TextView) findViewById(R.id.tv_1080p_high);
        mLowText = (TextView) findViewById(R.id.tv_1080p_middle);
        mMiddleText = (TextView) findViewById(R.id.middleText);
        mHighText = (TextView) findViewById(R.id.highText);
        mLowerText = (TextView) findViewById(R.id.tv_lower);
        mHigherText = (TextView) findViewById(R.id.tv_highter);
        mCloseIcon = (ImageButton) findViewById(R.id.cRight);
        mLowIcon = (ImageButton) findViewById(R.id.dRight);
        mMiddleIcon = (ImageButton) findViewById(R.id.zRight);
        mHighIcon = (ImageButton) findViewById(R.id.gRight);
        mLowerIcon = (ImageButton) findViewById(R.id.lower_check_icon);
        mHigherIcon = (ImageButton) findViewById(R.id.higher_check_icon);

        if (!isSupportTimeslapse()) {
            findViewById(R.id.layout_lower).setVisibility(View.GONE);
            findViewById(R.id.layout_highter).setVisibility(View.GONE);
        }
    }

    private boolean isSupportTimeslapse() {
        return GolukApplication.getInstance().mIPCControlManager.isSupportTimeslapse();
    }

    /**
     * 切换碰撞灵敏度
     *
     * @param _policy 灵敏度类型
     * @author xuhw
     * @date 2015年4月6日
     */
    private void updateSensitivity(int _policy) {
        policy = _policy;
        mCloseText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
        mLowText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
        mMiddleText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
        mHighText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
        mCloseIcon.setVisibility(View.GONE);
        mLowIcon.setVisibility(View.GONE);
        mMiddleIcon.setVisibility(View.GONE);
        mHighIcon.setVisibility(View.GONE);
        if (isSupportTimeslapse()) {
            mLowerText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
            mHigherText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
            mLowerIcon.setVisibility(View.GONE);
            mHigherIcon.setVisibility(View.GONE);
        }

        if (0 == policy || policy == IpcSettingUtil.COLLISION_OFF || policy == IpcSettingUtil.COLLISION_OFF_T3) {
            mCloseIcon.setVisibility(View.VISIBLE);
            mCloseText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
        } else if (1 == policy || policy == IpcSettingUtil.COLLISION_LOW || policy == IpcSettingUtil.COLLISION_LOW_T3) {
            mLowIcon.setVisibility(View.VISIBLE);
            mLowText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
        } else if (2 == policy || policy == IpcSettingUtil.COLLISION_MIDDLE || policy == IpcSettingUtil.COLLISION_MIDDLE_T3) {
            mMiddleIcon.setVisibility(View.VISIBLE);
            mMiddleText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
        } else if (3 == policy || policy == IpcSettingUtil.COLLISION_HIGHT || policy == IpcSettingUtil.COLLISION_HIGHT_T3) {
            mHighIcon.setVisibility(View.VISIBLE);
            mHighText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
        } else if (policy == IpcSettingUtil.COLLISION_LOWER || policy == IpcSettingUtil.COLLISION_LOWER_T3) {
            mLowerIcon.setVisibility(View.VISIBLE);
            mLowerText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
        } else if (policy == IpcSettingUtil.COLLISION_HIGHTER || policy == IpcSettingUtil.COLLISION_HIGHTER_T3) {
            mHigherIcon.setVisibility(View.VISIBLE);
            mHigherText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.back_btn) {
            exit();
        } else if (id == R.id.rl_1080p_high) {
            int value = 0;
            if (isSupportTimeslapse())
                value = mIsT3 ? IpcSettingUtil.COLLISION_OFF_T3 : IpcSettingUtil.COLLISION_OFF;
            updateSensitivity(value);
        } else if (id == R.id.rl_1080p_middle) {
            int value = 1;
            if (isSupportTimeslapse())
                value = mIsT3 ? IpcSettingUtil.COLLISION_LOW_T3 : IpcSettingUtil.COLLISION_LOW;
            updateSensitivity(value);
        } else if (id == R.id.middle) {
            int value = 2;
            if (isSupportTimeslapse())
                value = mIsT3 ? IpcSettingUtil.COLLISION_MIDDLE_T3 : IpcSettingUtil.COLLISION_MIDDLE;
            updateSensitivity(value);
        } else if (id == R.id.high) {
            int value = 3;
            if (isSupportTimeslapse())
                value = mIsT3 ? IpcSettingUtil.COLLISION_HIGHT_T3 : IpcSettingUtil.COLLISION_HIGHT;
            updateSensitivity(value);
        } else if (id == R.id.layout_lower) {
            updateSensitivity(mIsT3 ? IpcSettingUtil.COLLISION_LOWER_T3 : IpcSettingUtil.COLLISION_LOWER);
        } else if (id == R.id.layout_highter) {
            updateSensitivity(mIsT3 ? IpcSettingUtil.COLLISION_HIGHTER_T3 : IpcSettingUtil.COLLISION_HIGHTER);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        GolukApplication.getInstance().setContext(this, "impactsensitivity");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
        if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
            GolukDebugUtils.e("xuhw", "YYYYY===GSensor====event=" + event + "==msg=" + msg + "===param1=" + param1
                    + "==param2=" + param2);
            if (msg == IPC_VDCP_Msg_GetGSensorControlCfg) {
                if (param1 == RESULE_SUCESS) {
                    try {
                        JSONObject json = new JSONObject((String) param2);
                        policy = json.optInt("policy");
                        updateSensitivity(policy);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (msg == IPC_VDCP_Msg_GetCollisionValueConf) {
                if (param1 == RESULE_SUCESS) {
                    try {
                        JSONObject json = new JSONObject((String) param2);
                        policy = json.optInt("collisionValue");
                        updateSensitivity(policy);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void exit() {
        if (isSupportTimeslapse()) {
            boolean flag = GolukApplication.getInstance().getIPCControlManager().setGSensorMoreValueCfg(policy);
            GolukDebugUtils.e("xuhw", "YYYYY====setGSensorMoreValueCfg===policy=" + policy + "==flag=" + flag);
        } else {
            boolean flag = GolukApplication.getInstance().getIPCControlManager().setGSensorControlCfg(policy);
            GolukDebugUtils.e("xuhw", "YYYYY====setGSensorControlCfg===policy=" + policy + "==flag=" + flag);
        }
        GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("sensitivity");
        finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

}
