package cn.com.mobnote.golukmobile.startshare;

import java.util.ArrayList;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.tiros.debug.GolukDebugUtils;

public class ShareTypeLayout implements OnItemClickListener, OnClickListener {

	private final int TYPE_BG = 0;
	private final int TYPE_SG = 1;
	private final int TYPE_ML = 2;
	private final int TYPE_SSP = 3;

	private int mCurrentType = TYPE_BG;

	public final String[] typeArray = { "曝光台", "事故大爆料", "美丽风景", "随手拍" };

	private final String[] bgArray = { "小手一抖，抢录精彩一幕", "看见这样扔垃圾的也是醉了！", "打个转向灯真的那么难么？" };
	private final String[] sgArray = { "偶遇一场事故，惊出一身冷汗！", "又一起事故，大家都应该慢点啊！", "车祸现场，极其惨烈......" };
	private final String[] mlArray = { "世上美景无灱，不如车外沿途小路！", "这样风景如画的地方，心情也格外舒畅！", "这景色，美呆了！" };
	private final String[] sspArray = { "灵光一闪，拒绝无聊时光！", "在路上，遇到最美的惊喜！", "拍拍回家的那条路！" };

	private Context mContext = null;
	private LayoutInflater mLayoutFlater = null;
	private RelativeLayout mRootLayout = null;
//	private ListView mListView = null;
	private EditText mEditText = null;

	private SelectTypeAdapter mAdapter = null;
	/** 曝光台 */
	private TextView mBgBtn = null;
	private TextView mSgBtn = null;
	private TextView mMlBtn = null;
	private TextView mSspBtn = null;

	private LinearLayout mShareOpenLayout = null;
	private ImageView mShareOpenImg = null;
	private TextView mShareOpenText = null;
	private String resShareOpen = null;
	private String resShareClose = null;
	/** 是否分享到视频广场 */
	private boolean mIsOpenShare = true;

	private int resListSelectColor = 0;
	private int resListUnSelectColor = 0;

	/** 曝光台,事故大爆炸,美丽风景, 随手拍　背景颜色 */
	private int resTypeSelectColor = 0;
	private int resTypeUnSelectColor = 0;

	private TextView[] typeViewArray = new TextView[4];
	private SparseIntArray mTypeArray = new SparseIntArray();

	public ShareTypeLayout(Context context) {
		mContext = context;
		mLayoutFlater = LayoutInflater.from(mContext);
		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.shareselecttype, null);
		loadRes();
		initView();
		initData();

		switchTypeUI(TYPE_BG);
	}

	private void initData() {
		mTypeArray.put(TYPE_BG, 1);
		mTypeArray.put(TYPE_SG, 5);
		mTypeArray.put(TYPE_ML, 3);
		mTypeArray.put(TYPE_SSP, 4);
	}

	private void loadRes() {
		resShareOpen = mContext.getResources().getString(R.string.share_str_open);
		resShareClose = mContext.getResources().getString(R.string.share_str_close);

		resListSelectColor = mContext.getResources().getColor(R.color.share_list_select);
		resListUnSelectColor = mContext.getResources().getColor(R.color.share_list_unselect);

		resTypeSelectColor = mContext.getResources().getColor(R.color.share_type_shigu_select);
		resTypeUnSelectColor = mContext.getResources().getColor(R.color.share_type_shigu_unselect);
	}

	public ViewGroup getRootLayout() {
		return mRootLayout;
	}

	public void show() {
		if (null != mAdapter) {
			mAdapter.notifyDataSetChanged();
		}
	}

	private void initView() {
//		mListView = (ListView) mRootLayout.findViewById(R.id.shortshare_listview);
//		mListView.setOnItemClickListener(this);
		mEditText = (EditText) mRootLayout.findViewById(R.id.share_sayother);
		mEditText.setOnClickListener(this);
		mEditText.setKeyListener(null);

		mBgBtn = (TextView) mRootLayout.findViewById(R.id.share_type_bg);
		mSgBtn = (TextView) mRootLayout.findViewById(R.id.share_type_sg);
		mMlBtn = (TextView) mRootLayout.findViewById(R.id.share_type_ml);
		mSspBtn = (TextView) mRootLayout.findViewById(R.id.share_type_ssp);

		typeViewArray[0] = mBgBtn;
		typeViewArray[1] = mSgBtn;
		typeViewArray[2] = mMlBtn;
		typeViewArray[3] = mSspBtn;

		mShareOpenLayout = (LinearLayout) mRootLayout.findViewById(R.id.share_open_layout);
		mShareOpenImg = (ImageView) mRootLayout.findViewById(R.id.share_open_img);
		mShareOpenText = (TextView) mRootLayout.findViewById(R.id.share_open_txt);

		mBgBtn.setOnClickListener(this);
		mSgBtn.setOnClickListener(this);
		mMlBtn.setOnClickListener(this);
		mSspBtn.setOnClickListener(this);
		mShareOpenLayout.setOnClickListener(this);

		mAdapter = new SelectTypeAdapter(getData(TYPE_BG));
//		mListView.setAdapter(mAdapter);

		switchOpenAndClose(mIsOpenShare);
	}

	public boolean isOpenShare() {
		return mIsOpenShare;
	}

	public int getCurrentSelectType() {
		return mTypeArray.get(mCurrentType);
	}

	// 返回当前的描述文字
	public String getCurrentDesc() {
		String inputStr = mEditText.getText().toString().trim();
		if (null != inputStr && !inputStr.equals("")) {
			return inputStr;
		}
		return "啥也不想说，快看视频吧...";
	}

	private void switchOpenAndClose(boolean isOpen) {
		mIsOpenShare = isOpen;
		if (mIsOpenShare) {
			mShareOpenImg.setBackgroundResource(R.drawable.share_open_icon);
			mShareOpenText.setText(resShareOpen);
		} else {
			mShareOpenImg.setBackgroundResource(R.drawable.share_close_icon);
			mShareOpenText.setText(resShareClose);
		}
	}

	private ArrayList<DataBean> getData(int index) {
		if (TYPE_BG == index) {
			final int length = bgArray.length;
			ArrayList<DataBean> array = new ArrayList<DataBean>(length);
			for (int i = 0; i < length; i++) {
				DataBean data = new DataBean();
				data.content = bgArray[i];
				data.isSelect = false;
				if (i == 0) {
					data.isSelect = true;
				}

				array.add(data);
			}
			return array;
		} else if (TYPE_SG == index) {
			final int length = sgArray.length;
			ArrayList<DataBean> array = new ArrayList<DataBean>(length);
			for (int i = 0; i < length; i++) {
				DataBean data = new DataBean();
				data.content = sgArray[i];
				data.isSelect = false;
				if (i == 0) {
					data.isSelect = true;
				}
				array.add(data);
			}
			return array;
		} else if (TYPE_ML == index) {
			final int length = mlArray.length;
			ArrayList<DataBean> array = new ArrayList<DataBean>(length);
			for (int i = 0; i < length; i++) {
				DataBean data = new DataBean();
				data.content = mlArray[i];
				data.isSelect = false;
				if (i == 0) {
					data.isSelect = true;
				}
				array.add(data);
			}
			return array;
		} else if (TYPE_SSP == index) {
			final int length = sspArray.length;
			ArrayList<DataBean> array = new ArrayList<DataBean>(length);
			for (int i = 0; i < length; i++) {
				DataBean data = new DataBean();
				data.content = sspArray[i];
				data.isSelect = false;
				if (i == 0) {
					data.isSelect = true;
				}
				array.add(data);
			}
			return array;
		}

		return null;
	}

	class DataBean {
		String content;
		boolean isSelect;
	}

	class SelectTypeAdapter extends BaseAdapter {
		private ArrayList<DataBean> mDataList = null;
		private LayoutInflater mLayoutInflater = null;
		/** 记录滤镜上一次点击的id */
		private int resIndex = 0;

		public SelectTypeAdapter(ArrayList<DataBean> data) {
			mLayoutInflater = LayoutInflater.from(mContext);
			mDataList = data;
		}

		public void setData(ArrayList<DataBean> dd) {
			mDataList.clear();
			mDataList = dd;
			this.notifyDataSetChanged();
		}

		public ArrayList<DataBean> getCurrentData() {
			return mDataList;
		}

		public String getCurrentSelectData() {
			if (null == mDataList) {
				return "";
			}
			final int size = mDataList.size();
			for (int i = 0; i < size; i++) {
				if (mDataList.get(i).isSelect) {
					return mDataList.get(i).content;
				}
			}

			return "";
		}

		public int getCurrentResIndex() {
			return resIndex;
		}

		@Override
		public int getCount() {
			return mDataList == null ? 0 : mDataList.size();
		}

		@Override
		public Object getItem(int position) {
			return mDataList == null ? null : mDataList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (null == convertView) {
				holder = new ViewHolder();
				convertView = mLayoutInflater.inflate(R.layout.share_list_item, null);
				holder.contentTv = (TextView) convertView.findViewById(R.id.list_item_content);
				holder.selectImg = (ImageView) convertView.findViewById(R.id.list_item_select);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final DataBean dataBean = (DataBean) mDataList.get(position);
			holder.contentTv.setText(dataBean.content);

			if (dataBean.isSelect) {
				holder.selectImg.setVisibility(View.VISIBLE);
				holder.contentTv.setTextColor(resListSelectColor);
			} else {
				holder.contentTv.setTextColor(resListUnSelectColor);
				holder.selectImg.setVisibility(View.GONE);
			}
			return convertView;
		}

		class ViewHolder {
			TextView contentTv = null;
			ImageView selectImg = null;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		ArrayList<DataBean> beans = mAdapter.getCurrentData();
		final int size = beans.size();
		for (int i = 0; i < size; i++) {
			if (i == arg2) {
				beans.get(i).isSelect = true;
			} else {
				beans.get(i).isSelect = false;
			}
		}
		mAdapter.notifyDataSetChanged();
	}

	private void switchTypeUI(final int select) {
		mCurrentType = select;
		final int length = typeViewArray.length;
		for (int i = 0; i < length; i++) {
			if (select == i) {
				typeViewArray[i].setBackgroundResource(R.drawable.share_type_bg);
				typeViewArray[i].setTextColor(resTypeSelectColor);
			} else {
				typeViewArray[i].setBackgroundDrawable(null);
				typeViewArray[i].setTextColor(resTypeUnSelectColor);
			}
		}
	}

	public void setEditContent(boolean isCancel, String content) {
		if (isCancel) {
			// mEditText.setText("");
		} else {
			mEditText.setText(content);
		}
	}

	private boolean mIsExit = false;

	public void setExit() {
		mIsExit = true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.share_type_bg:
			if (mIsExit) {
				return;
			}
//			mAdapter.setData(getData(TYPE_BG));
			mEditText.setHint("视频描述裸奔中，快来给他披大衣...");
			switchTypeUI(TYPE_BG);
			break;
		case R.id.share_type_sg:
			if (mIsExit) {
				return;
			}
//			mAdapter.setData(getData(TYPE_SG));
			mEditText.setHint("吓死宝宝了，前面啥情况？说说呗...");
			switchTypeUI(TYPE_SG);
			break;
		case R.id.share_type_ml:
			if (mIsExit) {
				return;
			}
//			mAdapter.setData(getData(TYPE_ML));
			mEditText.setHint("任何伟大的创意都需要一个漂亮的描述...");
			switchTypeUI(TYPE_ML);
			break;
		case R.id.share_type_ssp:
			if (mIsExit) {
				return;
			}
//			mAdapter.setData(getData(TYPE_SSP));
			mEditText.setHint("导演，配个文字再加场吻戏吧！");
			switchTypeUI(TYPE_SSP);
			break;
		case R.id.share_open_layout:
			if (mIsExit) {
				return;
			}
			switchOpenAndClose(!mIsOpenShare);
			break;
		case R.id.share_sayother:
			GolukDebugUtils.e("", "------------点击输入框-----------------mIsExit："+mIsExit);
			if (mIsExit) {
				return;
			}
			click_input();
			break;
		default:
			break;
		}
	}

	// 显示输入界面
	private void click_input() {
		if (null != mContext && mContext instanceof VideoEditActivity) {
			((VideoEditActivity) mContext).mInputLayout.show(mEditText.getText().toString().trim());
		}
	}
}
