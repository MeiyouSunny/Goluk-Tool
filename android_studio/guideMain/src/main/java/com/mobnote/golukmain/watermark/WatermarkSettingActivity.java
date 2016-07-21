package com.mobnote.golukmain.watermark;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.watermark.bean.BandCarBrandResultBean;
import com.mobnote.golukmain.watermark.bean.CarBrandBean;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukUtils;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;

public class WatermarkSettingActivity extends BaseActivity implements View.OnClickListener, IPCManagerFn, IRequestResultListener {
    public static final int SPECIAL_SETTING_REQUEST = 1;
    public static final String SPECIAL_SETTING_RESULT = "CarBrand";
    private static final String IPC_WATERMARK = "WatermarkSetting";
    private EditText edtName;
    private CarBrandBean bean;
    private ImageView ivLogo;

    private BandCarBrandsRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_special_setting);
        mBaseApp.mIPCControlManager.addIPCManagerListener(IPC_WATERMARK, this);
        initView();
        initViewData();
    }

    private void initViewData() {

    }

    private void initView() {
        ImageButton btnBack = (ImageButton) findViewById(R.id.back_btn);
        Button btnSave = (Button) findViewById(R.id.btn_special_setting_save);
        RelativeLayout rlBrands = (RelativeLayout) findViewById(R.id.rl_special_setting_band_layout);
        edtName = (EditText) findViewById(R.id.edt_special_setting_name);
        ivLogo = (ImageView) findViewById(R.id.iv_special_setting_brand);
        btnBack.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        rlBrands.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_btn) {
            finish();
        } else if (id == R.id.btn_special_setting_save) {
            saveBrand();
        } else if (id == R.id.rl_special_setting_band_layout) {
            Intent specialSetting = new Intent(this, CarBrandsListActivity.class);
            startActivityForResult(specialSetting, SPECIAL_SETTING_REQUEST);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBaseApp.mIPCControlManager.removeIPCManagerListener(IPC_WATERMARK);
    }

    private void saveBrand() {
        if (!mBaseApp.isIpcConnSuccess) {
            GolukUtils.showToast(this, getString(R.string.ipc_disconnect_try_again));
            return;
        }
        String name = edtName.getText().toString();
        String code = bean.logoUrl;
        mBaseApp.mIPCControlManager.setIPCWatermark(code, name);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != SPECIAL_SETTING_REQUEST || resultCode != RESULT_OK) {
            return;
        }
        bean = data.getParcelableExtra(SPECIAL_SETTING_RESULT);
        if (bean == null) {
            return;
        }
        edtName.setText(bean.name);
        Glide.with(this).load(bean.logoUrl).into(ivLogo);
    }


    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
        if (event == ENetTransEvent_IPC_VDCP_CommandResp && msg == IPC_VDCP_Msg_SetIPCLogo) {
            //成功
            if (0 == param1) {
                request = new BandCarBrandsRequest(this);
                request.get(GolukConfig.SERVER_PROTOCOL_V2, bean.brandId, bean.code, edtName.getText().toString(), mBaseApp.mCurrentUId);
                finish();
            } else {
                GolukUtils.showToast(this, getString(R.string.user_personal_save_failed));
            }
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        BandCarBrandResultBean bean = (BandCarBrandResultBean) result;
        if (bean == null || bean.code != GolukConfig.SERVER_RESULT_OK) {
            //下次启动的时候再来上传内容
            request.saveCacheRequest();
        }
    }
}
