package cn.com.mobnote.golukmobile.usercenter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.gridview.HeaderGridView;
import cn.com.mobnote.golukmobile.gridview.PullToRefreshGridView;
import cn.com.tiros.debug.GolukDebugUtils;

public class TestGridViewActivity extends BaseActivity {

	private List<Data> mListData = new ArrayList<Data>();
	private PullToRefreshGridView mGridView = null;
//	private HeaderGridView mGridView = null;
	private TestGridViewAdapter mAdapter = null;
	private View mHeaderView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_testgridview_layout);
		initData();
		initView();
	}

	private void initView() {
		mGridView = (PullToRefreshGridView) findViewById(R.id.gv_testgridview);
		mAdapter = new TestGridViewAdapter(this);
		UserHeader header = new UserHeader(this);
		mHeaderView = header.createHeader();
		mGridView.getRefreshableView().addHeaderView(mHeaderView);
		mGridView.getRefreshableView().setNumColumns(2);
		mAdapter.setData(mListData);
		mGridView.setAdapter(mAdapter);
	}

	private void initData() {
		for (int i = 0; i < 10; i++) {
			Data data = new Data();
			data.name = "张三" + i;
			data.content = "啊又是张三啊张三啊张三";
			mListData.add(data);
		}
	}

	class Data {
		public String name;
		public String content;
	}

	class UserHeader {
		private Context context;
		private ImageView mImageHead;

		public UserHeader(Context context) {
			super();
			this.context = context;
		}
		
		public View createHeader() {
			View view = LayoutInflater.from(context).inflate(R.layout.testgridview_item_header, null);
			mImageHead = (ImageView) view.findViewById(R.id.iv_testgridview_head);
			return view;
		}
	}

}
