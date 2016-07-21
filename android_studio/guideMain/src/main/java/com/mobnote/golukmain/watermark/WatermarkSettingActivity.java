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
import com.mobnote.golukmain.watermark.bean.CarBrandBean;

public class WatermarkSettingActivity extends BaseActivity implements View.OnClickListener {
    public static final int SPECIAL_SETTING_REQUEST = 1;
    public static final String SPECIAL_SETTING_RESULT = "CarBrand";
    private EditText edtName;
    private CarBrandBean bean;
    private ImageView ivLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_special_setting);
        initView();
        initViewData();
    }

    private void initViewData() {
        if (bean == null) {
            return;
        }
        edtName.setText(bean.name);
        Glide.with(this).load(bean.logoUrl).into(ivLogo);
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
    protected void onResume() {
        super.onResume();
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

    private void saveBrand() {
        String name = edtName.getText().toString();
        String code = bean.code;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != SPECIAL_SETTING_REQUEST || resultCode != RESULT_OK) {
            return;
        }
        bean = data.getParcelableExtra(SPECIAL_SETTING_RESULT);
        initViewData();
    }
}
