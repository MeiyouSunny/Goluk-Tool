package cn.com.mobnote.golukmobile.newest;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.BitmapManager;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.videosuqare.CategoryListView;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;

import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.core.BitmapSize;

@SuppressLint("InflateParams")
public class NewestAdapter extends BaseAdapter {
	private Context mContext = null;
	private NewestListHeadDataInfo mHeadDataInfo = null;
	private List<VideoSquareInfo> mDataList = null;
	private int count = 0;
	private int width = 0;
	private float density = 0;
	/** 滚动中锁标识 */
	private boolean lock = false;
	private NewestListView mNewestListView = null;
	private CategoryListView mCategoryListView = null;
	private final int FIRST_TYPE = 0;
	private final int OTHERS_TYPE = 1;
	private boolean clickLock = false;
	
	public NewestAdapter(Context context) {
		mContext = context;
		mDataList = new ArrayList<VideoSquareInfo>();
		width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		density = SoundUtils.getInstance().getDisplayMetrics().density;
	}

	public void setData(NewestListHeadDataInfo headata, List<VideoSquareInfo> data) {
		mHeadView = null;
		mHeadDataInfo = headata;
		mDataList.clear();
		mDataList.addAll(data);
		if (null == mHeadDataInfo) {
			count = mDataList.size();
		} else {
			count = mDataList.size() + 1;
		}
		this.notifyDataSetChanged();
	}
	
	public void loadData(List<VideoSquareInfo> data) {
		mDataList.clear();
		mDataList.addAll(data);
		if (null == mHeadDataInfo) {
			count = mDataList.size();
		} else {
			count = mDataList.size() + 1;
		}
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}
	
	@Override
	public int getViewTypeCount() {
		if (null == mHeadDataInfo) {
			return 1;
		} else {
			return 2;
		}
	};

	@Override
	public int getItemViewType(int position) {
		if (null == mHeadDataInfo) {
			return OTHERS_TYPE;
		} else {
			if (position == 0) {
				return FIRST_TYPE;
			} else {
				return OTHERS_TYPE;
			}
		}
	};

	ViewHolder holder;
	@Override
	public View getView(int arg0, View convertView, ViewGroup parent) {
		int type = getItemViewType(arg0);
		if (FIRST_TYPE == type) {
			convertView = getHeadView();
		} else {
			convertView = loadLayout(convertView, arg0);
		}

		return convertView;
	}
	
	private View loadLayout(View convertView, int arg0) {
		if (null == convertView) {
			convertView = initLayout();
		} else {
			holder = (ViewHolder) convertView.getTag();
			if (null == holder) {
				convertView = initLayout();
			}
		}
		
		int index = arg0;
		if (null != mHeadDataInfo) {
			index = arg0 - 1;
		}
		initView(index);
		initListener(index);
		
		return convertView;
	}

	private View initLayout() {
		holder = new ViewHolder();
		View convertView = LayoutInflater.from(mContext).inflate(R.layout.newest_list_item, null);
		holder.imageLayout = (RelativeLayout) convertView.findViewById(R.id.imageLayout);
		holder.liveImg = (ImageView) convertView.findViewById(R.id.newlist_item_liveicon);
		holder.headimg = (ImageView) convertView.findViewById(R.id.headimg);
		holder.nikename = (TextView) convertView.findViewById(R.id.nikename);
		holder.time = (TextView) convertView.findViewById(R.id.time);
		holder.function = (ImageView) convertView.findViewById(R.id.function);

		holder.praiseLayout = (LinearLayout) convertView.findViewById(R.id.praiseLayout);
		holder.zanIcon = (ImageView) convertView.findViewById(R.id.zanIcon);
		holder.zanText = (TextView) convertView.findViewById(R.id.zanText);

		holder.commentLayout = (LinearLayout) convertView.findViewById(R.id.commentLayout);
		holder.commentIcon = (ImageView) convertView.findViewById(R.id.commentIcon);
		holder.commentText = (TextView) convertView.findViewById(R.id.commentText);

		holder.shareLayout = (LinearLayout) convertView.findViewById(R.id.shareLayout);
		holder.shareIcon = (ImageView) convertView.findViewById(R.id.shareIcon);
		holder.shareText = (TextView) convertView.findViewById(R.id.shareText);

		holder.zText = (TextView) convertView.findViewById(R.id.zText);
		holder.weiguan = (TextView) convertView.findViewById(R.id.weiguan);
		holder.totalcomments = (TextView) convertView.findViewById(R.id.totalcomments);
		holder.detail = (TextView) convertView.findViewById(R.id.detail);

		holder.totlaCommentLayout = (LinearLayout) convertView.findViewById(R.id.totlaCommentLayout);
		holder.comment1 = (TextView) convertView.findViewById(R.id.comment1);
		holder.comment2 = (TextView) convertView.findViewById(R.id.comment2);
		holder.comment3 = (TextView) convertView.findViewById(R.id.comment3);

		int height = (int) ((float) width / 1.77f);
		RelativeLayout.LayoutParams mPlayerLayoutParams = new RelativeLayout.LayoutParams(width, height);
		mPlayerLayoutParams.addRule(RelativeLayout.BELOW, R.id.headlayout);
		holder.imageLayout.setLayoutParams(mPlayerLayoutParams);
		convertView.setTag(holder);

		return convertView;
	}

	private void initListener(int index) {
		if(index < 0 || index >= mDataList.size()) {
			return;
		}
		
		VideoSquareInfo mVideoSquareInfo = mDataList.get(index);
		// 分享监听
		ClickShareListener tempShareListener = new ClickShareListener(mContext, mVideoSquareInfo, mNewestListView);
		tempShareListener.setCategoryListView(mCategoryListView);
		holder.shareLayout.setOnClickListener(tempShareListener);
		// 举报监听
		holder.function.setOnClickListener(new ClickFunctionListener(mContext, mVideoSquareInfo));
		// 评论监听
		holder.commentLayout.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo, true));
		// 播放区域监听
		holder.imageLayout.setOnClickListener(new ClickNewestListener(mContext,  mVideoSquareInfo,mNewestListView));
		// 点赞
		ClickPraiseListener tempPraiseListener = new ClickPraiseListener(mContext, mVideoSquareInfo, mNewestListView);
		tempPraiseListener.setCategoryListView(mCategoryListView);
		holder.praiseLayout.setOnClickListener(tempPraiseListener);
		// 评论总数监听
		List<CommentDataInfo> comments = mVideoSquareInfo.mVideoEntity.commentList;
		if (comments.size() > 0) {
			holder.totalcomments.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo, false));
			holder.totlaCommentLayout.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo, false));
		}
	}

	private String getFormatNumber(String fmtnumber) {
		String number;

		int wg = Integer.parseInt(fmtnumber);

		if (wg < 100000) {
			DecimalFormat df = new DecimalFormat("#,###");
			number = df.format(wg);
		} else {
			number = "100,000+";
		}
		return number;
	}

	private void initView(int index) {
		if(index < 0 || index >= mDataList.size()) {
			return;
		}
		
		VideoSquareInfo mVideoSquareInfo = mDataList.get(index);
		loadImage(holder.imageLayout, mVideoSquareInfo.mVideoEntity.picture);

		showHead(holder.headimg, mVideoSquareInfo.mUserEntity.headportrait);

		holder.nikename.setText(mVideoSquareInfo.mUserEntity.nickname);
		holder.time.setText(formatTime(mVideoSquareInfo.mVideoEntity.sharingtime));

		if ("0".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
			holder.zanText.setTextColor(Color.rgb(0x88, 0x88, 0x88));
			holder.zanIcon.setBackgroundResource(R.drawable.videodetail_like);
		} else {
			holder.zanText.setTextColor(Color.rgb(0x11, 0x63, 0xa2));
			holder.zanIcon.setBackgroundResource(R.drawable.videodetail_like_press);
		}

		if ("-1".equals(mVideoSquareInfo.mVideoEntity.praisenumber)) {
			holder.zText.setText("");
		} else {
			holder.zText.setText(getFormatNumber(mVideoSquareInfo.mVideoEntity.praisenumber) + "赞");
		}

		if ("-1".equals(mVideoSquareInfo.mVideoEntity.clicknumber)) {
			holder.weiguan.setText("");
		} else {
			holder.weiguan.setText(getFormatNumber(mVideoSquareInfo.mVideoEntity.clicknumber) + " 围观");
		}

		if (TextUtils.isEmpty(mVideoSquareInfo.mVideoEntity.describe)) {
			holder.detail.setVisibility(View.GONE);
		} else {
			holder.detail.setVisibility(View.VISIBLE);
			showText(holder.detail, mVideoSquareInfo.mUserEntity.nickname, mVideoSquareInfo.mVideoEntity.describe);
		}

		if (isLive(mVideoSquareInfo)) {
			// 直播
			holder.liveImg.setVisibility(View.VISIBLE);
			holder.commentLayout.setVisibility(View.GONE);
		} else {
			// 点播
			holder.liveImg.setVisibility(View.GONE);
			holder.commentLayout.setVisibility(View.VISIBLE);
		}

		if ("1".equals(mVideoSquareInfo.mVideoEntity.iscomment)) {
			List<CommentDataInfo> comments = mVideoSquareInfo.mVideoEntity.commentList;
			if (null != comments && comments.size() > 0) {
				if (isLive(mVideoSquareInfo)) {
					// 直播不显示评论
					holder.totalcomments.setVisibility(View.GONE);
					holder.totlaCommentLayout.setVisibility(View.GONE);
				} else {
					int comcount = Integer.parseInt(mVideoSquareInfo.mVideoEntity.comcount);
					if (comcount <= 3) {
						holder.totalcomments.setVisibility(View.GONE);
					}else {
						holder.totalcomments.setVisibility(View.VISIBLE);
						holder.totalcomments.setText("查看所有" + getFormatNumber(mVideoSquareInfo.mVideoEntity.comcount)
								+ "条评论");
					}
					
					holder.totlaCommentLayout.setVisibility(View.VISIBLE);
					holder.totalcomments
							.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo, false));
					holder.totlaCommentLayout.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo,
							false));
					holder.comment1.setVisibility(View.VISIBLE);
					holder.comment2.setVisibility(View.VISIBLE);
					holder.comment3.setVisibility(View.VISIBLE);
					if (1 == comments.size()) {
						showText(holder.comment1, comments.get(0).name, comments.get(0).text);
						holder.comment2.setVisibility(View.GONE);
						holder.comment3.setVisibility(View.GONE);
					} else if (2 == comments.size()) {
						showText(holder.comment1, comments.get(0).name, comments.get(0).text);
						showText(holder.comment2, comments.get(1).name, comments.get(1).text);
						holder.comment3.setVisibility(View.GONE);
					} else if (3 == comments.size()) {
						showText(holder.comment1, comments.get(0).name, comments.get(0).text);
						showText(holder.comment2, comments.get(1).name, comments.get(1).text);
						showText(holder.comment3, comments.get(2).name, comments.get(2).text);
					}
				}

			} else {
				holder.totalcomments.setVisibility(View.GONE);
				holder.totlaCommentLayout.setVisibility(View.GONE);
			}
		} else {
			holder.totalcomments.setVisibility(View.GONE);
			holder.totlaCommentLayout.setVisibility(View.GONE);
		}

	}

	private void showHead(ImageView view, String headportrait) {
		if ("1".equals(headportrait)) {
			view.setBackgroundResource(R.drawable.editor_head_boy1);
		} else if ("2".equals(headportrait)) {
			view.setBackgroundResource(R.drawable.editor_head_boy2);
		} else if ("3".equals(headportrait)) {
			view.setBackgroundResource(R.drawable.editor_head_boy3);
		} else if ("4".equals(headportrait)) {
			view.setBackgroundResource(R.drawable.editor_head_girl4);
		} else if ("5".equals(headportrait)) {
			view.setBackgroundResource(R.drawable.editor_head_girl5);
		} else if ("6".equals(headportrait)) {
			view.setBackgroundResource(R.drawable.editor_head_girl6);
		} else if ("7".equals(headportrait)) {
			view.setBackgroundResource(R.drawable.editor_head_feault7);
		} else {
			view.setBackgroundResource(R.drawable.editor_head_feault7);
		}
	}

	private boolean isLive(VideoSquareInfo mVideoSquareInfo) {
		return "1".equals(mVideoSquareInfo.mVideoEntity.type);
	}

	private void showText(TextView view, String nikename, String text) {
		String t_str = nikename + " " + text;
		SpannableStringBuilder style = new SpannableStringBuilder(t_str);
		style.setSpan(new ForegroundColorSpan(Color.rgb(0x11, 0x63, 0xa2)), 0, nikename.length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		view.setText(style);
	}

	RelativeLayout mHeadView;

	private View getHeadView() {
		int imagewidth = (int) ((width - 10 * density) / 2);
		int imageheight = (int) (imagewidth * 0.56);
		if (null == mHeadView) {
			mHeadView = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.category_layout, null);
//		}
		RelativeLayout main = (RelativeLayout) mHeadView.findViewById(R.id.main);
		RelativeLayout liveLayout = (RelativeLayout) mHeadView.findViewById(R.id.liveLayout);
		liveLayout.setOnClickListener(new ClickLiveListener(mContext));

		LiveInfo mLiveInfo = mHeadDataInfo.mLiveDataInfo;
		if (null != mLiveInfo) {
			int number = Integer.parseInt(mLiveInfo.number);
			if (number > 0) {
				liveLayout.setVisibility(View.VISIBLE);
			}else {
				liveLayout.setVisibility(View.GONE);
			}
			
			int height = (int) ((float) width / 1.77f);
			RelativeLayout.LayoutParams liveLayoutParams = new RelativeLayout.LayoutParams(width, height);
			liveLayoutParams.addRule(RelativeLayout.BELOW, R.id.main);
			liveLayout.setLayoutParams(liveLayoutParams);

			ImageView mImageView =  (ImageView) mHeadView.findViewById(R.id.mImageView);
			RelativeLayout.LayoutParams dvParams = new RelativeLayout.LayoutParams(width, height);
			mImageView.setLayoutParams(dvParams);
			loadHeadImage(mImageView, mLiveInfo.pic, width, height);

			LinearLayout mLookLayout = (LinearLayout) mHeadView.findViewById(R.id.mLookLayout);
			TextView mLookNum = (TextView) mHeadView.findViewById(R.id.mLookNum);

			if ("-1".equals(mLiveInfo.number)) {
				mLookLayout.setVisibility(View.GONE);
			} else {
				mLookLayout.setVisibility(View.VISIBLE);
				mLookNum.setText(mLiveInfo.number);
			}

		} else {
			liveLayout.setVisibility(View.GONE);
		}

		for (int i = 0; i < mHeadDataInfo.categoryList.size(); i++) {
			CategoryDataInfo mCategoryDataInfo = mHeadDataInfo.categoryList.get(i);
			RelativeLayout item = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.category_item, null);
			main.setPadding(0, (int) (10 * density), 0, 0);
			int iid = i + 1111;
			item.setId(iid);

			item.setOnTouchListener(new ClickCategoryListener(mContext, mCategoryDataInfo, this));
			TextView mTitleName = (TextView) item.findViewById(R.id.mTitleName);
			TextView mUpdateTime = (TextView) item.findViewById(R.id.mUpdateTime);

			RelativeLayout.LayoutParams itemparams = new RelativeLayout.LayoutParams(imagewidth, imageheight);
			mTitleName.setText(mCategoryDataInfo.name);
			mUpdateTime.setText(getTime(mCategoryDataInfo.time));

			ImageView mImageView = (ImageView)item.findViewById(R.id.mImageView);
			RelativeLayout.LayoutParams dvParams = new RelativeLayout.LayoutParams(imagewidth, imageheight);
			mImageView.setLayoutParams(dvParams);
			loadHeadImage(mImageView, mCategoryDataInfo.coverurl, imagewidth, imageheight);
			
			int id = i + 1111 - 2;
			if (i % 2 == 0) {
				itemparams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				itemparams.setMargins(0, 0, (int) (10 * density), (int) (10 * density));
				itemparams.addRule(RelativeLayout.BELOW, id);
			} else {
				itemparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				itemparams.setMargins(0, 0, 0, (int) (10 * density));
				itemparams.addRule(RelativeLayout.BELOW, id);
			}

			main.addView(item, itemparams);

		}
		}

		return mHeadView;

	}
	
	private void loadHeadImage(final ImageView image, String url, int width, int height) {
		BitmapDisplayConfig config = new BitmapDisplayConfig();
		config.setBitmapMaxSize(new BitmapSize(width, height));
		Bitmap bitmap = BitmapManager.getInstance().mBitmapUtils.getBitmapFromMemCache(url, config);
		if (null == bitmap) {
			image.setImageResource(R.drawable.tacitly_pic);
			
			BitmapManager.getInstance().mBitmapUtils.display(image, url);
		}else {
			image.setImageBitmap(bitmap);
		}
	}

	private void loadImage(RelativeLayout layout, String url) {
		final int id = 3123;
		SimpleDraweeView view;
		int count = layout.getChildCount();
		if (0 == count) {
			layout.removeAllViews();
			view = new SimpleDraweeView(mContext);
			view.setId(id);
			int height = (int) ((float) width / 1.77f);
			RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(width, height);
			layout.addView(view, mPreLoadingParams);
		}else {
			view = (SimpleDraweeView)layout.findViewById(id);
		}
		
		GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(mContext.getResources());
		GenericDraweeHierarchy mGenericDraweeHierarchy = builder.setFadeDuration(300)
				.setPlaceholderImage(mContext.getResources().getDrawable(R.drawable.tacitly_pic), ScaleType.FIT_XY)
				.setFailureImage(mContext.getResources().getDrawable(R.drawable.tacitly_pic), ScaleType.FIT_XY)
				.setActualImageScaleType(ScaleType.FIT_XY).build();
		view.setHierarchy(mGenericDraweeHierarchy);
		view.setImageURI(Uri.parse(url));
	}

	public static class ViewHolder {
		RelativeLayout imageLayout;
		ImageView liveImg;
		ImageView headimg;
		TextView nikename;
		TextView time;
		ImageView function;

		LinearLayout praiseLayout;
		ImageView zanIcon;
		TextView zanText;

		LinearLayout commentLayout;
		ImageView commentIcon;
		TextView commentText;

		LinearLayout shareLayout;
		ImageView shareIcon;
		TextView shareText;

		TextView zText;
		TextView weiguan;
		TextView detail;
		TextView totalcomments;

		LinearLayout totlaCommentLayout;
		TextView comment1;
		TextView comment2;
		TextView comment3;

	}

	/**
	 * 锁住后滚动时禁止下载图片
	 * 
	 * @author xuhw
	 * @date 2015年6月8日
	 */
	public void lock() {
		lock = true;
	}

	/**
	 * 解锁后恢复下载图片功能
	 * 
	 * @author xuhw
	 * @date 2015年6月8日
	 */
	public void unlock() {
		lock = false;
//		this.notifyDataSetChanged();
	}

	@SuppressLint("SimpleDateFormat")
	private String formatTime(String date) {
		String time = null;
		try {
			long curTime = System.currentTimeMillis();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			Date strtodate = formatter.parse(date);
			
			Date curDate = new Date(curTime);
			int curYear = curDate.getYear();
			int history = strtodate.getYear();
			int hisDay = strtodate.getDay();
			int curDay = curDate.getDay();
			
			if (curYear == history) {
				if(hisDay == curDay) {
					SimpleDateFormat jn = new SimpleDateFormat("HH:mm");
					String timestr =  jn.format(strtodate);
					return "今天 " + timestr;
				}else if((hisDay + 1) == curDay) {
					SimpleDateFormat jn = new SimpleDateFormat("HH:mm");
					String timestr =  jn.format(strtodate);
					return "昨天 " + timestr;
				}else {
					SimpleDateFormat jn = new SimpleDateFormat("MM-dd HH:mm");
					return jn.format(strtodate);// 今年内：月日更新
				}
			} else {
				SimpleDateFormat jn = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				return jn.format(strtodate);// 非今年：年月日更新
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return time;
	}
	
	@SuppressLint("SimpleDateFormat")
	private String getTime(String date) {
		final long MINTUE = 60*1000;
		final long HOUR = 60*MINTUE;
		final long DAY = 24*HOUR;
		final long WEEK = 7*DAY;
		
		String time = null;
		try {
			long curTime = System.currentTimeMillis();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			Date strtodate = formatter.parse(date);
			long historytime = strtodate.getTime();

			Date curDate = new Date(curTime);
			int curYear = curDate.getYear();
			int history = strtodate.getYear();
			
			long diff = Math.abs(historytime - curTime);// 时间差
			if (curYear == history) {
				 if (diff <= WEEK && diff > DAY) {
					 return time = diff / DAY + "天前更新";// 天前更新
				 }else if (diff <= DAY && diff > HOUR) {
					 return time = diff / HOUR + "小时前更新";// 小时前更新
				 }else if (diff <= HOUR) {
					 int min = (int)(diff / MINTUE);
					 if(min < 1) {
						 min = 1;
					 }
					 return time = min + "分钟前更新";// 分钟前更新
				 }else {
					 SimpleDateFormat jn = new SimpleDateFormat("MM.dd更新");
					 return jn.format(strtodate);// 今年内：月日更新
				 }
			}else {
				SimpleDateFormat jn = new SimpleDateFormat("yyyy.MM.dd更新");
				return jn.format(strtodate);// 非今年：年月日更新
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return time;
	}

	public void setNewestLiseView(NewestListView view) {
		this.mNewestListView = view;
	}

	public void setCategoryListView(CategoryListView view) {
		mCategoryListView = view;
	}

	public void updateClickPraiseNumber(VideoSquareInfo info) {
		for (int i = 0; i < mDataList.size(); i++) {
			VideoSquareInfo vs = mDataList.get(i);
			if (vs.id.equals(info.id)) {
				mDataList.get(i).mVideoEntity.praisenumber = info.mVideoEntity.praisenumber;
				mDataList.get(i).mVideoEntity.ispraise = info.mVideoEntity.ispraise;
				this.notifyDataSetChanged();
				break;
			}
		}

	}
	
	public synchronized boolean getClickLock() {
		return clickLock;
	}
	
	public synchronized void setClickLock(boolean lock) {
		clickLock = lock;
	}
	
	public void onResume() {
		setClickLock(false);
	}

}
