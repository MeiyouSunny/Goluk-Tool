package cn.com.mobnote.golukmobile.startshare;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;

public class ShareTypeLayout implements OnClickListener {

	private final int TYPE_BG = 0;
	private final int TYPE_SG = 1;
	private final int TYPE_ML = 2;
	private final int TYPE_SSP = 3;

	private int mCurrentType = TYPE_BG;

	private final String[] hintArray = { "视频描述裸奔中，快来给他披大衣...", "吓死宝宝了，前面啥情况？说说呗...", "任何伟大的创意都需要一个漂亮的描述...",
			"导演，配个文字再加场吻戏吧！" };

	private Context mContext = null;
	private LayoutInflater mLayoutFlater = null;
	private RelativeLayout mRootLayout = null;
	private TextView mTextView = null;

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

		resTypeSelectColor = mContext.getResources().getColor(R.color.share_type_shigu_select);
		resTypeUnSelectColor = mContext.getResources().getColor(R.color.share_type_shigu_unselect);
	}

	public ViewGroup getRootLayout() {
		return mRootLayout;
	}

	private void initView() {
		mTextView = (TextView) mRootLayout.findViewById(R.id.share_sayother);
		mTextView.setOnClickListener(this);

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
		String inputStr = mTextView.getText().toString().trim();
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

	private void switchTypeUI(final int select) {
		mCurrentType = select;
		final int length = typeViewArray.length;
		for (int i = 0; i < length; i++) {
			if (select == i) {
				typeViewArray[i].setBackgroundResource(R.drawable.share_type_bg);
				typeViewArray[i].setTextColor(resTypeSelectColor);
				mTextView.setHint(hintArray[i]);
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
			mTextView.setText(content);
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
			switchTypeUI(TYPE_BG);
			break;
		case R.id.share_type_sg:
			if (mIsExit) {
				return;
			}
			switchTypeUI(TYPE_SG);
			break;
		case R.id.share_type_ml:
			if (mIsExit) {
				return;
			}
			switchTypeUI(TYPE_ML);
			break;
		case R.id.share_type_ssp:
			if (mIsExit) {
				return;
			}
			switchTypeUI(TYPE_SSP);
			break;
		case R.id.share_open_layout:
			if (mIsExit) {
				return;
			}
			switchOpenAndClose(!mIsOpenShare);
			break;
		case R.id.share_sayother:
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
			((VideoEditActivity) mContext).mInputLayout.show(mTextView.getText().toString().trim());
		}
	}
}
