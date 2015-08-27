package cn.com.mobnote.golukmobile.startshare;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.startshare.MVManage.MVEditData;
import cn.com.mobnote.view.MyGridView;

public class ShareFilterLayout {
	private Context mContext = null;
	/** mv滤镜appter */
	public MVListAdapter mMVListAdapter = null;
	private ScrollView mRootLayout = null;
	private LinearLayout mMVListLayout = null;
	private LayoutInflater mLayoutFlater = null;

	public ShareFilterLayout(Context context) {
		mContext = context;
		mLayoutFlater = LayoutInflater.from(mContext);

		mRootLayout = (ScrollView) mLayoutFlater.inflate(R.layout.share_filter_select, null);
		mMVListLayout = (LinearLayout) mRootLayout.findViewById(R.id.mvlistlayout);

		initVideoEditList();
	}

	public ViewGroup getRootLayout() {
		return mRootLayout;
	}

	public void setExit() {
		mMVListAdapter.setExit();
	}

	/**
	 * 初始化滤镜布局
	 */
	private void initVideoEditList() {
		MyGridView gridView = createMVGridView();
		MVManage mvManage = new MVManage(mContext);
		ArrayList<MVEditData> list = mvManage.getLocalVideoList();
		mMVListAdapter = new MVListAdapter(mContext, list, this);
		gridView.setAdapter(mMVListAdapter);
		mMVListLayout.addView(gridView);
	}

	/**
	 * 创建本地滤镜列表布局
	 * 
	 * @return
	 */
	private MyGridView createMVGridView() {
		MyGridView gridLayout = new MyGridView(mContext, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		gridLayout.setLayoutParams(lp);
		gridLayout.setBackgroundResource(R.color.color_bg_comm);
		gridLayout.setNumColumns(4);
		gridLayout.setPadding(16, 30, 16, 30);
		gridLayout.setVerticalSpacing(37);
		gridLayout.setHorizontalSpacing(16);
		return gridLayout;
	}
}
