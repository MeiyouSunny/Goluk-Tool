package com.mobnote.golukmain.watermark;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.watermark.bean.CarBrandBean;
import com.mobnote.util.GolukFileUtils;
import com.mobnote.view.SideBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CarBrandsListActivity extends BaseActivity implements View.OnClickListener, SideBar.OnTouchingLetterChangedListener {
    public static final String CURRENT_SELECTED_CAR_BRAND_CODE = "currentSelected";

    private StickyListHeadersListView mLvCar;
    private CarBrandsAdapter mAdapter;
    private List<CarBrandBean> mList;
    private String mCurrentSelectedCarCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_car_brands_list);
        initView();
        initData();
    }

    private void initView() {
        ImageButton btnBack = (ImageButton) findViewById(R.id.back_btn);
        EditText mEdtSearch = (EditText) findViewById(R.id.et_search_content);
        mLvCar = (StickyListHeadersListView) findViewById(R.id.lv_car_brands);
        SideBar mSideBar = (SideBar) findViewById(R.id.side_bar);
        mAdapter = new CarBrandsAdapter(this);
        mLvCar.setAdapter(mAdapter);
        btnBack.setOnClickListener(this);
        mSideBar.setOnTouchingLetterChangedListener(this);
        mLvCar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CarBrandBean bean = (CarBrandBean) mAdapter.getItem(position);
                Intent data = new Intent();
                if (!bean.code.equals(mCurrentSelectedCarCode)) {
                    data.putExtra(WatermarkSettingActivity.SPECIAL_SETTING_RESULT, bean);
                }
                setResult(RESULT_OK, data);
                finish();
            }
        });
        mEdtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initData() {
//        CarBrandsRequest request = new CarBrandsRequest(this);
//        request.setCache(true);
//        request.get(GolukConfig.SERVER_PROTOCOL_V2, mBaseApp.mCurrentUId);
        Intent data = getIntent();
        if (data != null) {
            mCurrentSelectedCarCode = data.getStringExtra(CURRENT_SELECTED_CAR_BRAND_CODE);
            mAdapter.setCurrentSelected(mCurrentSelectedCarCode);
        }
        mList = GolukFileUtils.restoreFileToList(GolukFileUtils.CAR_BRAND_OBJECT);
        Collections.sort(mList, new PinyinComparator());
        mAdapter.setList(mList);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_btn) {
            finish();
        }
    }

    @Override
    public void onTouchingLetterChanged(String s) {
        int position = mAdapter.getPositionForSection(s.charAt(0));
        if (position != -1) {
            mLvCar.setSelection(position);
        }
    }

    private void filterData(String filterStr) {
        if (mList == null) {
            return;
        }
        List<CarBrandBean> filterDateList = new ArrayList<>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = mList;
        } else {
            filterDateList.clear();
            for (CarBrandBean bean : mList) {
                String name = bean.name;
                String alpha = bean.alphaName;
                if (name.contains(filterStr) || alpha.contains(filterStr)) {
                    filterDateList.add(bean);
                }
            }
        }

        mAdapter.setList(filterDateList);
    }

    public class PinyinComparator implements Comparator<CarBrandBean> {

        public int compare(CarBrandBean o1, CarBrandBean o2) {
            return o1.alphaName.toUpperCase().compareTo(o2.alphaName.toUpperCase());
        }

    }
}
