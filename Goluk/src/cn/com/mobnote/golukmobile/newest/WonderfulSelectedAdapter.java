package cn.com.mobnote.golukmobile.newest;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.Utils;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.view.SlideShowView;


public class WonderfulSelectedAdapter extends BaseAdapter {
	private Context mContext = null;
//	private List<JXListItemDataInfo> mDataList = null;
	private List<Object> mDataList = null;
	private int count = 0;
	private int width = 0;
	private Typeface mTypeface = null;
	private final static int BANNER_ITEM = 0;
	private final static int VIDEO_ITEM = 1;
	private BannerDataModel mBannerData = null;
	private final static String FAKE_CONTENT = "fake";

	public WonderfulSelectedAdapter(Context context) {
		mContext = context;
		mDataList = new ArrayList<Object>();
		width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		mTypeface = Typeface.createFromAsset(context.getAssets(), "AdobeHebrew-Bold.otf");
	}

	public void setBannerData(BannerDataModel model) {
		mBannerData = model;
		if(mDataList.size() == 0) {
			mDataList.add(model);
		} else {
			mDataList.set(0, model);
		}
		notifyDataSetChanged();
	}

	// add fake banner data
	public void setData(List<JXListItemDataInfo> data) {
		mDataList.clear();

		if(null != mBannerData) {
			mDataList.add(mBannerData);
		} else {
			BannerDataModel model = new BannerDataModel();
			model.setResult(FAKE_CONTENT);
			mDataList.add(new BannerDataModel());
		}
		mDataList.addAll(data);
		count = mDataList.size();
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public Object getItem(int position) {
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override

	public int getItemViewType(int position) {
		if (position == 0) {
			return BANNER_ITEM;
		} else {
			return VIDEO_ITEM;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		ViewHolderBanner bannerHolder = null;
		int height = (int) ((float) width / 1.78f);

		if (convertView == null) {
			if(getItemViewType(position) == VIDEO_ITEM) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.wonderful_selected_item, null);
				holder = new ViewHolder();
				holder.main = (RelativeLayout) convertView
						.findViewById(R.id.main);
				holder.mDate = (TextView) convertView.findViewById(R.id.mDate);
				holder.videoImg = (ImageView) convertView
						.findViewById(R.id.simpledrawee);
				holder.icon = (ImageView) convertView
						.findViewById(R.id.wonderful_icon);
				holder.mTitleName = (TextView) convertView
						.findViewById(R.id.mTitleName);
				holder.mTagName = (TextView) convertView
						.findViewById(R.id.mTagName);
				holder.mVideoLayout = (LinearLayout) convertView
						.findViewById(R.id.mVideoLayout);
//				holder.mLookLayout = (LinearLayout) convertView
//						.findViewById(R.id.mLookLayout);
				holder.mVideoNum = (TextView) convertView
						.findViewById(R.id.mVideoNum);
				holder.mLookNum = (TextView) convertView
						.findViewById(R.id.mLookNum);

				RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(
						width, height);
				holder.videoImg.setLayoutParams(mPreLoadingParams);
				convertView.setTag(holder);
			} else {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.wonderful_banner_item, null);
				bannerHolder = new ViewHolderBanner();
				bannerHolder.mBannerSlide = (SlideShowView)convertView.findViewById(R.id.ssv_wonderful_banner);
				LinearLayout.LayoutParams mPreLoadingParams = new LinearLayout.LayoutParams(
						width, height);
				bannerHolder.mBannerSlide.setLayoutParams(mPreLoadingParams);
				convertView.setTag(bannerHolder);
			}
		} else {
			if(getItemViewType(position) == VIDEO_ITEM) {
				holder = (ViewHolder) convertView.getTag();
			} else {
				bannerHolder = (ViewHolderBanner)convertView.getTag();
			}
		}

		if(getItemViewType(position) == VIDEO_ITEM) {
			JXListItemDataInfo info = (JXListItemDataInfo)mDataList.get(position);
			holder.mTitleName.setText(getTitleString(info.ztitle));
			holder.mTagName.setVisibility(View.GONE);
			if ("-1".equals(info.clicknumber)) {
				holder.mVideoLayout.setVisibility(View.GONE);
			} else {
				holder.mVideoNum.setText(GolukUtils.getFormatNumber(info.clicknumber));
				holder.mVideoLayout.setVisibility(View.VISIBLE);
			}

//			if ("-1".equals(info.videonumber)) {
//				holder.mLookLayout.setVisibility(View.GONE);
//			} else {
//				holder.mLookNum.setText(GolukUtils.getFormatNumber(info.videonumber));
//				holder.mLookLayout.setVisibility(View.VISIBLE);
//			}

			if (!TextUtils.isEmpty(info.jxdate)) {
				if (0 == position) {
					holder.mDate.setVisibility(View.GONE);
				} else {
					String phoneDate = Utils.getDateStr(System.currentTimeMillis());
					if(phoneDate.trim().equals(info.jxdate.trim())) {
						holder.mDate.setTypeface(mTypeface);
						holder.mDate.setText(mContext.getString(R.string.str_jx_today));
						holder.mDate.setVisibility(View.VISIBLE);
					} else {
						if(position != 1) {
							holder.mDate.setTypeface(mTypeface);
							holder.mDate.setText(GolukUtils.getTime(info.jxdate));
							holder.mDate.setVisibility(View.VISIBLE);
						} else {
							holder.mDate.setTypeface(mTypeface);
							holder.mDate.setText(mContext.getString(R.string.str_jx_other_day));
							holder.mDate.setVisibility(View.VISIBLE);
						}
					}
				}
			} else {
				holder.mDate.setVisibility(View.GONE);
			}
			holder.main.setOnTouchListener(new ClickWonderfulSelectedListener(mContext, info, this));
			loadImage(holder.videoImg, holder.icon, info.jximg, info.jtypeimg);
		} else {
            BannerDataModel model = (BannerDataModel)mDataList.get(position);
            if(null == model || null == model.getSlides()) {
                showDefaultImage(bannerHolder.mBannerSlide);
            } else {
                if(null != model && "0".equals(model.getResult())) {
                    List<BannerSlideBody> slidesList = model.getSlides();
                    if(slidesList != null) {
                        // No exceed 10 images
                        if(model.getSlides().size() > 10) {
                            int size = model.getSlides().size();
                            for(int i = 10; i < size; i++) {
                                slidesList.remove(i);
                            }
                        }
                        bannerHolder.mBannerSlide.clearImages();
                        bannerHolder.mBannerSlide.setImageDataList(slidesList);
                    } else {
                        showDefaultImage(bannerHolder.mBannerSlide);
                    }
                } else {
                    //default pic
                    showDefaultImage(bannerHolder.mBannerSlide);
                }
            }
		}
		return convertView;
	}

    private void showDefaultImage(SlideShowView slideView) {
        slideView.clearImages();
        BannerSlideBody body = new BannerSlideBody();
        body.setPicture(FAKE_CONTENT);
        List<BannerSlideBody> bodyList = new ArrayList<BannerSlideBody>();
        bodyList.add(body);
        slideView.setImageDataList(bodyList);
    }

	private String getTitleString(String title) {
		String name = "";
		int len = title.length();
		if (len > 15) {
			int size = len / 15 + 1;
			for (int i = 0; i < size; i++) {
				int index = 15 * (i + 1);
				if (index < len) {
					name += title.substring(15 * i, index) + "\n";
				} else {
					name += title.substring(15 * i);
				}
			}
		} else {
			name = title;
		}

		return name;
	}

	private void loadImage(ImageView mPlayerLayout, ImageView iconView, String url, String iconUrl) {
		GlideUtils.loadImage(mContext, mPlayerLayout, url, R.drawable.tacitly_pic);
		if (TextUtils.isEmpty(iconUrl)) {
			iconView.setVisibility(View.GONE);
		} else {
			iconView.setVisibility(View.VISIBLE);
			GlideUtils.loadImage(mContext, iconView, iconUrl, -1);
		}
	}

	public static class ViewHolder {
		RelativeLayout main;
		ImageView videoImg;
		ImageView icon;
		TextView mTitleName;
		TextView mTagName;
		LinearLayout mVideoLayout;
//		LinearLayout mLookLayout;
		TextView mVideoNum;
		TextView mLookNum;
		TextView mDate;
	}

	static class ViewHolderBanner {
		SlideShowView mBannerSlide;
	}
}
