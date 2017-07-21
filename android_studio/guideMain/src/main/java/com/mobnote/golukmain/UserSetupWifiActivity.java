package com.mobnote.golukmain;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.live.LiveDialogManager;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;

import org.json.JSONObject;

import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 功能：设置页中修改极路客WIFI密码
 *
 * @author mobnote
 */
public class UserSetupWifiActivity extends BaseActivity implements OnClickListener {

    /**
     * title部分
     **/
    private ImageButton mBtnBack = null;// 返回
    private TextView mTextTitle = null;// title
    private TextView mTvName = null;// title
    private Button mBtnSave = null;// 保存
    /**
     * body
     **/
    private EditText mEditText = null;
    private EditText mEditText2 = null;

    private GolukApplication mApp = null;

    private String mGolukSSID = "";
    private String mGolukPWD = "";
    private String mApSSID = "";
    private String mApPWD = "";

    // 写死ip,网关
    private final String ip = "192.168.1.103";
    private final String way = "192.168.1.103";

    private ImageView mImageView1, mImageView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        setContentView(R.layout.user_setup_password);
        mApp = (GolukApplication) getApplication();
        Intent intent = getIntent();
        mApp.setContext(this, "changewifi");

        mGolukSSID = intent.getStringExtra("golukssid");
        mGolukPWD = intent.getStringExtra("golukpwd");
        mApSSID = intent.getStringExtra("apssid");
        mApPWD = intent.getStringExtra("appwd");

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mApp.setContext(this, "changewifi");

    }

    // 初始化
    public void initView() {
        mBtnBack = (ImageButton) findViewById(R.id.back_btn);
        mBtnSave = (Button) findViewById(R.id.user_title_right);
        mEditText = (EditText) findViewById(R.id.changewifi_password_editText);
        mEditText2 = (EditText) findViewById(R.id.changewifi_password_editText_2);
        mTextTitle = (TextView) findViewById(R.id.user_title_text);
        mTvName = (TextView) findViewById(R.id.changewifi_password_name_2);
        mImageView1 = (ImageView) findViewById(R.id.imageView1);
        mImageView2 = (ImageView) findViewById(R.id.imageView2);

        mTextTitle.setText(this.getResources().getString(R.string.str_wifi_title));

        /**
         * 获取摄像头管理页面传来的WIFI密码
         */
        String customName = parseGolukSSid(mApSSID);
        String prefix = getPrefixGolukSSid(mApSSID);
        if (!"".equals(mApSSID)) {
            mEditText2.setText(customName);
            mTvName.setText(prefix);
            mEditText2.setSelection(customName.length());
        } else {
            mEditText2.setText("");
        }
        if (IPCControlManager.T1_SIGN.equals(mApp.mIPCControlManager.mProduceName)
                || IPCControlManager.T1s_SIGN.equals(mApp.mIPCControlManager.mProduceName)
                || IPCControlManager.T2_SIGN.equals(mApp.mIPCControlManager.mProduceName)
                || IPCControlManager.T3_SIGN.equals(mApp.mIPCControlManager.mProduceName)
                || IPCControlManager.T3U_SIGN.equals(mApp.mIPCControlManager.mProduceName)) {
            mImageView1.setImageResource(R.drawable.ipcbind_t_direct_gif_3);
            mImageView2.setVisibility(View.INVISIBLE);
        } else {
            mImageView1.setImageResource(R.drawable.ipcbind_g_direct_bg);
            mImageView2.setVisibility(View.VISIBLE);
        }

        // 监听
        mBtnBack.setOnClickListener(this);
        mBtnSave.setOnClickListener(this);
    }

    private String getPrefixGolukSSid(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            return "";
        }
        String part_ssid = "";
        if (mBaseApp.getIPCControlManager().isT1RelativeWifiName()) {
            int sp = ssid.lastIndexOf("_") + 1;
            part_ssid = ssid.substring(0, sp);
        } else {
            part_ssid = ssid.substring(0, 5);
        }
        return part_ssid;
    }

    public String parseGolukSSid(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            return "";
        }
        String part_ssid = "";
        if (mBaseApp.getIPCControlManager().isT1RelativeWifiName()) {
            part_ssid = ssid.substring(ssid.lastIndexOf("_") + 1);
        } else {
            part_ssid = ssid.substring(5);
        }
        return part_ssid;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.back_btn) {
            UserUtils.hideSoftMethod(UserSetupWifiActivity.this);
            this.finish();
        } else if (id == R.id.user_title_right) {
            // 点击保存按钮隐藏软件盘
            UserUtils.hideSoftMethod(UserSetupWifiActivity.this);
            savePassword();
        } else {
        }
    }

    /**
     * 设置IPC信息成功回调
     */
    public void setIpcLinkWiFiCallBack(int state) {
        LiveDialogManager.getManagerInstance().dissmissCustomDialog();
        if (0 == state) {
            GolukUtils.showToast(this, this.getResources().getString(R.string.str_wifi_success));
            this.setResult(11);
            this.finish();
        } else {
            GolukUtils.showToast(this, this.getResources().getString(R.string.str_wifi_name_fail));
        }
    }

    private void savePassword() {
        final String newPwd = mEditText2.getText().toString();
        if (newPwd.length() < 1 || newPwd.length() > 10) {
            GolukUtils.showToast(this, this.getResources().getString(R.string.str_wifi_limit));
            mEditText2.requestFocus();
            return;
        }
//        final String newPwdConfirm = mEditText2.getText().toString();
//        if (newPwdConfirm.length() < 8 || newPwdConfirm.length() > 15) {
//            GolukUtils.showToast(this, this.getResources().getString(R.string.confirm_password));
//            mEditText2.requestFocus();
//            return;
//        }

        String json = getSetIPCJson();
        mApp.stopDownloadList();
        boolean b = mApp.mIPCControlManager.setIpcLinkPhoneHot(json);
//		if (!b) {
//			GolukUtils.showToast(this, this.getResources().getString(R.string.str_disconnect_ipc));
//		} else {
        LiveDialogManager.getManagerInstance().showCustomDialog(this,
                this.getResources().getString(R.string.str_wait));
//		}
    }

    private String getSetIPCJson() {
        // 连接ipc热点wifi---调用ipc接口
        GolukDebugUtils.e("", "通知ipc连接手机热点--setIpcLinkPhoneHot---1");
        final String newName = mTvName.getText().toString() + mEditText2.getText().toString();
        String json = getIPCJson(mGolukSSID, mGolukPWD, newName, mApPWD);
        return json;
    }

    private String getIPCJson(String golukSSID, String golukPWD, String apSSID, String apPWD) {
        String json = null;
        try {
            JSONObject obj = new JSONObject();
            obj.put("AP_SSID", apSSID);
            obj.put("AP_PWD", apPWD);
            // obj.put("GolukSSID", golukSSID);
            // obj.put("GolukPWD", golukPWD);
            // obj.put("GolukIP", ip);
            // obj.put("GolukGateway", way);

            json = obj.toString();
        } catch (Exception e) {

        }
        return json;
    }

}