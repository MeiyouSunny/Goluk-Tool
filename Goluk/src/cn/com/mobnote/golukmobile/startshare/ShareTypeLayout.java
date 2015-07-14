package cn.com.mobnote.golukmobile.startshare;

import java.util.ArrayList;

import android.content.Context;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.util.GolukUtils;

public class ShareTypeLayout implements OnItemClickListener, OnClickListener {

	private final int TYPE_BG = 0;
	private final int TYPE_SG = 1;
	private final int TYPE_ML = 2;
	private final int TYPE_SSP = 3;

	public final String[] typeArray = { "曝光台", "事故大爆料", "美丽风景", "随手拍" };
	private final String[] bgArray = { "曝光台1", "曝光台2", "曝光台3", "曝光台4", "曝光台5", "曝光台6" };
	private final String[] sgArray = { "事故大爆料1", "事故大爆料2", "事故大爆料3", "事故大爆料4" };
	private final String[] mlArray = { "美丽风景1", "美丽风景2", "美丽风景3" };
	private final String[] sspArray = { "随手拍1", "随手拍2" };

	private Context mContext = null;
	private LayoutInflater mLayoutFlater = null;
	private RelativeLayout mRootLayout = null;
	private ListView mListView = null;
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

	public ShareTypeLayout(Context context) {
		mContext = context;
		mLayoutFlater = LayoutInflater.from(mContext);
		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.shareselecttype, null);
		loadRes();
		initView();

		switchTypeUI(TYPE_BG);
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
		mListView = (ListView) mRootLayout.findViewById(R.id.shortshare_listview);
		mListView.setOnItemClickListener(this);
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
		mListView.setAdapter(mAdapter);

		switchOpenAndClose(mIsOpenShare);
	}

	public boolean isOpenShare() {
		return mIsOpenShare;
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
		final int length = typeViewArray.length;
		for (int i = 0; i < length; i++) {
			if (select == i) {
				typeViewArray[i].setBackgroundResource(R.drawable.share_bg_icon);
				typeViewArray[i].setTextColor(resTypeSelectColor);
			} else {
				typeViewArray[i].setBackgroundDrawable(null);
				typeViewArray[i].setTextColor(resTypeUnSelectColor);
			}
		}
	}

	public void setEditContent(boolean isCancel, String content) {
		if (isCancel) {
			mEditText.setText("");
		} else {
			mEditText.setText(content);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.share_type_bg:
			mAdapter.setData(getData(TYPE_BG));
			switchTypeUI(TYPE_BG);
			break;
		case R.id.share_type_sg:
			mAdapter.setData(getData(TYPE_SG));
			switchTypeUI(TYPE_SG);
			break;
		case R.id.share_type_ml:
			mAdapter.setData(getData(TYPE_ML));
			switchTypeUI(TYPE_ML);
			break;
		case R.id.share_type_ssp:
			mAdapter.setData(getData(TYPE_SSP));
			switchTypeUI(TYPE_SSP);
			break;
		case R.id.share_open_layout:
			switchOpenAndClose(!mIsOpenShare);
			break;
		case R.id.share_sayother:
			GolukUtils.showToast(mContext, "显示输入");
			click_input();
			break;
		default:
			break;
		}
	}

	private void click_input() {
		if (null != mContext && mContext instanceof VideoEditActivity) {
			((VideoEditActivity) mContext).mInputLayout.show();
		}
	}
}
