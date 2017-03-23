package com.mobnote.golukmain.watermark;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.watermark.bean.BandCarBrandResultBean;
import com.mobnote.golukmain.watermark.bean.CarBrandBean;
import com.mobnote.golukmain.wifibind.WiFiInfo;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        InputFilter[] filters = {new NameLengthFilter(20)};
        edtName.setFilters(filters);
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
            if (currentBean != null) {
                specialSetting.putExtra(CarBrandsListActivity.CURRENT_SELECTED_CAR_BRAND_CODE, currentBean.code);
            }
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
        name = GolukUtils.toUtf8(name);
        String code = currentBean == null ? "" : currentBean.code;
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
            ivLogo.setImageDrawable(null);
            return;
        }
        GolukFileUtils.reloadThumbnail(currentBean.code + ".jpg");
        Bitmap logo = GolukFileUtils.reloadThumbnail(currentBean.code + ".jpg");
        ivLogo.setImageBitmap(logo);
    }


    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
        if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
            if (msg == IPC_VDCP_Msg_SetIPCLogo) {
                //成功
                if (0 == param1) {
                    if (currentBean != null) {
                        request = new BandCarBrandsRequest(this);
                        request.post(GolukConfig.SERVER_PROTOCOL_V2, currentBean.brandId, currentBean.code, edtName.getText().toString(), mBaseApp.mCurrentUId, WiFiInfo.MOBILE_SSID);
                    }
                    finish();
                } else {
                    GolukUtils.showToast(this, getString(R.string.user_personal_save_failed));
                }
            } else if (msg == IPC_VDCP_Msg_GetIPCLogo) {
                if (0 == param1) {
                    String watermarkInfo = (String) param2;
                    try {
                        JSONObject jsonObject = new JSONObject(watermarkInfo);
                        String code = jsonObject.getString("logo");
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
        if (mList == null) {
            return;
        }

        if (!TextUtils.isEmpty(code)) {
            for (CarBrandBean bean : mList) {
                if (bean.code.equals(code)) {
                    currentBean = bean;
                    break;
                }
            }
        }
        edtName.setText(name);
        if (currentBean == null) {
            return;
        }
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


    private class NameLengthFilter implements InputFilter {
        int MAX_EN;// 最大英文/数字长度 一个汉字算两个字母
        String regEx = "[\\u4e00-\\u9fa5]"; // unicode编码，判断是否为汉字

        public NameLengthFilter(int mAX_EN) {
            super();
            MAX_EN = mAX_EN;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            int destCount = dest.toString().length()
                    + getChineseCount(dest.toString());
            int sourceCount = source.toString().length()
                    + getChineseCount(source.toString());
            if (destCount + sourceCount > MAX_EN) {
                return "";

            } else {
                return source;
            }
        }

        private int getChineseCount(String str) {
            int count = 0;
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(str);
            while (m.find()) {
                for (int i = 0; i <= m.groupCount(); i++) {
                    count = count + 1;
                }
            }
            return count;
        }
    }

}
