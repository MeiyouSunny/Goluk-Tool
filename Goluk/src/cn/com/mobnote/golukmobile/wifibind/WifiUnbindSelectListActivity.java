package cn.com.mobnote.golukmobile.wifibind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class WifiUnbindSelectListActivity extends BaseActivity implements OnClickListener {

	/** 关闭按钮 **/
	private ImageView mCloseBtn;

	/** 数据列表 **/
	private ListView mListView;

	/** 没有数据时的默认布局 **/
	private RelativeLayout mEmptyLayout;

	/** 编辑按钮 **/
	private Button mEditBtn;

	private WifiUnbindSelectListAdapter mListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unbind_connection_list);

		initView();
		initLisenner();
		initData();
	}

	/**
	 * 初始化view
	 */
	private void initView() {
		mListView = (ListView) findViewById(R.id.listView);
		mEmptyLayout = (RelativeLayout) findViewById(R.id.emptyLayout);
		mCloseBtn = (ImageView) findViewById(R.id.close_btn);
		mEditBtn = (Button) findViewById(R.id.edit_btn);

		findViewById(R.id.addMoblieBtn).setOnClickListener(this);
	}

	/**
	 * 初始化view的监听
	 */
	private void initLisenner() {
		mCloseBtn.setOnClickListener(this);
		mEditBtn.setOnClickListener(this);
	}

	JSONArray jsons = new JSONArray();

	/** 初始化数据 **/
	private void initData() {
		mListView.setEmptyView(mEmptyLayout);
		mListAdapter = new WifiUnbindSelectListAdapter(this);
		mListView.setAdapter(mListAdapter);

		JSONObject json = new JSONObject();
		json.put("type", "T1");
		json.put("edit", 0);
		json.put("name", "goluk12345678");
		jsons.add(json);

		json = new JSONObject();
		json.put("type", "G2");
		json.put("edit", 0);
		json.put("name", "goluk87654321");
		jsons.add(json);

		json = new JSONObject();
		json.put("type", "G1");
		json.put("edit", 0);
		json.put("name", "goluk00000000");
		jsons.add(json);

		mListAdapter.setData(jsons);
		mListAdapter.notifyDataSetChanged();
	}

	/**
	 * 添加头部
	 * 
	 * @param view
	 */
	public void addListViewHead(View view) {
		mListView.addHeaderView(view);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.close_btn:
			this.finish();
			break;
		case R.id.edit_btn:
			if (mListAdapter.mEditState) {
				mListAdapter.mEditState = false;
				mEditBtn.setText(this.getResources().getString(R.string.edit_text));// 编辑
			} else {
				mListAdapter.mEditState = true;
				mEditBtn.setText(this.getResources().getString(R.string.user_personal_title_right));// 保存
			}
			mListAdapter.notifyDataSetChanged();
			break;
		case R.id.addMoblieBtn:
			click_AddIpc();
			break;
		default:
			break;
		}
	}

	private void click_AddIpc() {
		Intent intent = new Intent(this, WifiUnbindSelectTypeActivity.class);
		startActivity(intent);
	}

}
