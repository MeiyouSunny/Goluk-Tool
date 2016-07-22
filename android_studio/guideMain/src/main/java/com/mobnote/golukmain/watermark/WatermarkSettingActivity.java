package com.mobnote.golukmain.watermark;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;

public class WatermarkSettingActivity extends BaseActivity implements View.OnClickListener, IPCManagerFn, IRequestResultListener {
    public static final int SPECIAL_SETTING_REQUEST = 1;
    public static final String SPECIAL_SETTING_RESULT = "CarBrand";
    private static final String IPC_WATERMARK = "WatermarkSetting";
    private EditText edtName;
    private CarBrandBean currentBean;
    private ImageView ivLogo;

    private BandCarBrandsRequest request;
    //从缓存里直接读取 , 前置条件必须已经缓存过
    private List<CarBrandBean> mList;

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
        mList = GolukFileUtils.restoreFileToList(GolukFileUtils.CAR_BRAND_OBJECT);
        mBaseApp.mIPCControlManager.getIPCWatermark();
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
        String code = currentBean.code;
        mBaseApp.mIPCControlManager.setIPCWatermark(code, name);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != SPECIAL_SETTING_REQUEST || resultCode != RESULT_OK) {
            return;
        }
        currentBean = (CarBrandBean) data.getSerializableExtra(SPECIAL_SETTING_RESULT);
        if (currentBean == null) {
            return;
        }
        edtName.setText(currentBean.name);
        Glide.with(this).load(currentBean.logoUrl).into(ivLogo);
    }


    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
        if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
            if (msg == IPC_VDCP_Msg_SetIPCLogo) {
                //成功
                if (0 == param1) {
                    request = new BandCarBrandsRequest(this);
                    request.get(GolukConfig.SERVER_PROTOCOL_V2, currentBean.brandId, currentBean.code, edtName.getText().toString(), mBaseApp.mCurrentUId);
                    finish();
                } else {
                    GolukUtils.showToast(this, getString(R.string.user_personal_save_failed));
                }
            } else if (msg == IPC_VDCP_Msg_GetIPCLogo) {
                if (0 == param1) {
                    String watermarkInfo = (String) param2;
                    try {
                        JSONObject jsonObject = new JSONObject(watermarkInfo);
                        String code = jsonObject.getString("code");
                        String name = jsonObject.getString("name");
                        convertToServerBean(code, name);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //如果获取不到什么东西的话，说明没有
            }
        }
    }

    private void convertToServerBean(String code, String name) {
        if (TextUtils.isEmpty(code)) {
            return;
        }
        if (TextUtils.isEmpty(name)) {
            return;
        }
        if (mList == null) {
            return;
        }

        for (CarBrandBean bean : mList) {
            if (bean.code.equals(code)) {
                currentBean = bean;
                break;
            }
        }
        if (currentBean == null) {
            return;
        }
        edtName.setText(name);
        Bitmap logo = GolukFileUtils.reloadThumbnail(currentBean.code + ".jpg");
        ivLogo.setImageBitmap(logo);
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
