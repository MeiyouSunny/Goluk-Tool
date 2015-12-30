package cn.com.mobnote.golukmobile.adas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.alibaba.fastjson.JSON;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.eventbus.EventAdasConfigStatus;
import cn.com.mobnote.eventbus.EventConfig;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.util.GolukFileUtils;
import cn.com.mobnote.util.GolukUtils;
import de.greenrobot.event.EventBus;

public class AdasSeletedVehicleTypeActivity extends BaseActivity implements OnClickListener, OnItemClickListener {
	private static final String TAG = "AdasSeletedVehicleTypeActivity";
	private static final String CONFIG_FILE_NAME = "vehicleConfig";
	public static final String FROM = "from";
	public static final int REQUEST_CODE_VEHICLE_CONFIG = 0;
	private GolukApplication mApp = null;
	private ImageButton mBackBtn = null;
	private ListView mListView = null;
	private CarTypeAdapter mCarTypeAdapter = null;
	private int mFromType = 0;
	/** 向导跳转：0， 配置页跳转：1 **/
	private Button mNextButton;
	private AdasConfigParamterBean mAdasConfigParamter = null;
	private ArrayList<VehicleParamterBean> mCustomVehicleList = new ArrayList<VehicleParamterBean>(3);
	private int mPosition = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adasselected_vehicle_type);
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, TAG);
		if (savedInstanceState == null) {
			Intent intent = getIntent();
			mFromType = intent.getIntExtra(FROM, 0);
			mAdasConfigParamter = (AdasConfigParamterBean) intent
					.getSerializableExtra(AdasVerificationActivity.ADASCONFIGDATA);
		} else {
			mFromType = savedInstanceState.getInt(FROM);
			mAdasConfigParamter = (AdasConfigParamterBean) savedInstanceState
					.getSerializable(AdasVerificationActivity.ADASCONFIGDATA);
		}
		EventBus.getDefault().register(this);
		initView();
		loadData();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putInt(FROM, mFromType);
		if (mAdasConfigParamter != null) {
			outState.putSerializable(AdasVerificationActivity.ADASCONFIGDATA, mAdasConfigParamter);
		}
		super.onSaveInstanceState(outState);
	}

	private void initView() {
		mBackBtn = (ImageButton) findViewById(R.id.imagebutton_back);
		mBackBtn.setOnClickListener(this);
		mNextButton = (Button) findViewById(R.id.button_selected_complete);
		mNextButton.setOnClickListener(this);
		if (mFromType != 0) {
			mNextButton.setText(R.string.short_input_ok);
		}
		mListView = (ListView) findViewById(R.id.listview_car_type);
		mCarTypeAdapter = new CarTypeAdapter(this);
		mListView.setAdapter(mCarTypeAdapter);
		mListView.setOnItemClickListener(this);
	}

	private void loadData() {
		ArrayList<VehicleParamterBean> data = null;
		String listStr = getDataFromAssets(CONFIG_FILE_NAME);
		if (listStr != null) {
			data = (ArrayList<VehicleParamterBean>) JSON.parseArray(listStr, VehicleParamterBean.class);
		}
		String customListStr = GolukFileUtils.loadString(GolukFileUtils.ADAS_CUSTOM_VEHICLE, null);
		if (customListStr != null) {
			mCustomVehicleList = (ArrayList<VehicleParamterBean>) JSON.parseArray(customListStr,
					VehicleParamterBean.class);
		} else {
			String name = getString(R.string.str_custom);
			for (int i = 1; i < 4; i++) {
				VehicleParamterBean item = new VehicleParamterBean();
				item.name = name + i;
				mCustomVehicleList.add(item);
			}
		}

		if (data != null) {
			data.addAll(mCustomVehicleList);
		} else {
			data = mCustomVehicleList;
		}
		int len = data.size();

		for (int i = 0; i < len; i++) {
			VehicleParamterBean item = data.get(i);

			if (item.head_offset == mAdasConfigParamter.head_offset
					&& item.height_offset == mAdasConfigParamter.height_offset
					&& item.left_offset == mAdasConfigParamter.left_offset
					&& item.right_offset == mAdasConfigParamter.right_offset
					&& item.wheel_offset == mAdasConfigParamter.wheel_offset) {
				mPosition = i;
				break;
			}
		}
		mCarTypeAdapter.setData(data);
		mCarTypeAdapter.setSelectedId(mPosition);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
		case R.id.imagebutton_back:
			// 返回
			if (GolukUtils.isFastDoubleClick()) {
				return;
			}
			finish();
			break;
		case R.id.button_selected_complete:
			if (GolukUtils.isFastDoubleClick()) {
				return;
			}
			int position = mCarTypeAdapter.getSelectedId();
			VehicleParamterBean selectedItem = (VehicleParamterBean) mCarTypeAdapter.getItem(position);
			mAdasConfigParamter.head_offset = selectedItem.head_offset;
			mAdasConfigParamter.height_offset = selectedItem.height_offset;
			mAdasConfigParamter.left_offset = selectedItem.left_offset;
			mAdasConfigParamter.right_offset = selectedItem.right_offset;
			mAdasConfigParamter.wheel_offset = selectedItem.wheel_offset;
			if (mFromType == 0) {
				Intent intent = new Intent(AdasSeletedVehicleTypeActivity.this, AdasVerificationActivity.class);
				intent.putExtra(AdasVerificationActivity.FROM, 0);
				intent.putExtra(AdasVerificationActivity.ADASCONFIGDATA, mAdasConfigParamter);
				startActivity(intent);
			} else {
				EventAdasConfigStatus eventAdasConfigStatus = new EventAdasConfigStatus(EventConfig.IPC_ADAS_CONFIG_FROM_MODIFY);
				eventAdasConfigStatus.setData(mAdasConfigParamter);
				EventBus.getDefault().post(eventAdasConfigStatus);
				finish();
			}
			break;
		default:
			Log.e(TAG, "id = " + id);
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// TODO Auto-generated method stub
		mPosition = position;
		int len = mCarTypeAdapter.getCount();
		if (position > len - 4) {
			Intent intent = new Intent(AdasSeletedVehicleTypeActivity.this, AdasVehicleConfigActivity.class);
			intent.putExtra(AdasVehicleConfigActivity.CUSTOMDATA, mCustomVehicleList);
			intent.putExtra(AdasVehicleConfigActivity.CUSTOMINDEX, mCustomVehicleList.size() - len + position);
			startActivityForResult(intent, REQUEST_CODE_VEHICLE_CONFIG);
		} else {
			mCarTypeAdapter.setSelectedId(position);
		}
	}

	public String getDataFromAssets(String fileName) {
		InputStreamReader inputReader = null;
		try {
			inputReader = new InputStreamReader(getAssets().open(fileName));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			String result = "";
			while ((line = bufReader.readLine()) != null) {
				result += line;
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inputReader != null) {
				try {
					inputReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		return null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_VEHICLE_CONFIG) {
			VehicleParamterBean result = (VehicleParamterBean) data.getSerializableExtra(AdasVehicleConfigActivity.CUSTOMDATA);
			if (result == null) {
				return;
			}
			VehicleParamterBean selectedItem = (VehicleParamterBean) mCarTypeAdapter.getItem(mPosition);
			if (selectedItem == null) {
				return;
			}
			selectedItem.head_offset = result.head_offset;
			selectedItem.height_offset = result.height_offset;
			selectedItem.left_offset = result.left_offset;
			selectedItem.name = result.name;
			selectedItem.wheel_offset = result.wheel_offset;
			selectedItem.right_offset = result.right_offset;
			mCarTypeAdapter.setSelectedId(mPosition);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	public void onEventMainThread(EventAdasConfigStatus event) {
		finish();
	}
}
