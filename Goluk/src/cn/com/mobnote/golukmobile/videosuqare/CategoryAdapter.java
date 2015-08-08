package cn.com.mobnote.golukmobile.videosuqare;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
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
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.newest.ClickCommentListener;
import cn.com.mobnote.golukmobile.newest.ClickNewestListener;
import cn.com.mobnote.golukmobile.newest.ClickPraiseListener;
import cn.com.mobnote.golukmobile.newest.CommentDataInfo;
import cn.com.tiros.debug.GolukDebugUtils;

import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;

@SuppressLint("InflateParams")
public class CategoryAdapter extends BaseAdapter {
	private Context mContext = null;
	private List<VideoSquareInfo> mDataList = null;
	private int count = 0;
	private int width = 0;
	private float density = 0;
	/** 滚动中锁标识 */
	private boolean lock = false;
	private final int FIRST_TYPE = 0;
	private final int OTHERS_TYPE = 1;

	public CategoryAdapter(Context context) {
		mContext = context;
		mDataList = new ArrayList<VideoSquareInfo>();
		width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		density = SoundUtils.getInstance().getDisplayMetrics().density;
	}

	public void setData(List<VideoSquareInfo> data) {
		mDataList.clear();
		mDataList.addAll(data);
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		GolukDebugUtils.e("","jyf----Category------Adapter------------------getCount: " +  mDataList.size());
		return mDataList.size();
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

		return 1;

	}

	@Override
	public int getItemViewType(int position) {
		return OTHERS_TYPE;
	};

	ViewHolder holder;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		GolukDebugUtils.e("","jyf-----CategoryListView------------------getView: " +  position);
		if (null == convertView) {
			convertView = loadLayout();
		} else {
			holder = (ViewHolder) convertView.getTag();
			if (null == holder) {
				convertView = loadLayout();
			}
		}
		initView(position);
		initListener(position);
		return convertView;
	}

	private View loadLayout() {
		holder = new ViewHolder();
		View convertView = LayoutInflater.from(mContext).inflate(R.layout.newest_list_item, null);
		holder.imageLayout = (RelativeLayout) convertView.findViewById(R.id.imageLayout);
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
		VideoSquareInfo mVideoSquareInfo = mDataList.get(index);

		holder.commentLayout.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo, true));
		holder.imageLayout.setOnClickListener(new ClickNewestListener(mContext, mVideoSquareInfo));
		// holder.function.setOnClickListener(new
		// ClickCategoryListener(mVideoSquareInfo.mVideoEntity.videoid));
		holder.praiseLayout.setOnClickListener(new ClickPraiseListener(mVideoSquareInfo.mVideoEntity.videoid));

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
		GolukDebugUtils.e("", "TTTTTTT=========holder==" + holder);
		VideoSquareInfo mVideoSquareInfo = mDataList.get(index);
		GolukDebugUtils.e("", "TTTTTTT=========holder.imageLayout=" + holder.imageLayout + "===mVideoSquareInfo="
				+ mVideoSquareInfo + "==mVideoSquareInfo.mVideoEntity==" + mVideoSquareInfo.mVideoEntity);
		loadImage(holder.imageLayout, mVideoSquareInfo.mVideoEntity.picture);

		showHead(holder.headimg, mVideoSquareInfo.mUserEntity.headportrait);

		holder.nikename.setText(mVideoSquareInfo.mUserEntity.nickname);
		holder.time.setText(formatTime(mVideoSquareInfo.mVideoEntity.sharingtime));

		if ("0".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
			holder.zanIcon.setBackgroundResource(R.drawable.videodetail_like);
		} else {
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
			holder.weiguan.setText(getFormatNumber(mVideoSquareInfo.mVideoEntity.clicknumber) + "围观");
		}

		if (TextUtils.isEmpty(mVideoSquareInfo.mVideoEntity.describe)) {
			holder.detail.setVisibility(View.GONE);
		} else {
			holder.detail.setVisibility(View.VISIBLE);
			showText(holder.detail, mVideoSquareInfo.mUserEntity.nickname, mVideoSquareInfo.mVideoEntity.describe);
		}

		if ("1".equals(mVideoSquareInfo.mVideoEntity.iscomment)) {
			List<CommentDataInfo> comments = mVideoSquareInfo.mVideoEntity.commentList;
			if (comments.size() > 0) {
				holder.totalcomments.setText("查看所有" + getFormatNumber(mVideoSquareInfo.mVideoEntity.comcount) + "条评论");
				holder.totalcomments.setVisibility(View.VISIBLE);
				holder.totlaCommentLayout.setVisibility(View.VISIBLE);
				holder.totalcomments.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo, false));
				holder.totlaCommentLayout
						.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo, false));
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

	private void showText(TextView view, String nikename, String text) {
		String t_str = nikename + " " + text;
		SpannableStringBuilder style = new SpannableStringBuilder(t_str);
		style.setSpan(new ForegroundColorSpan(Color.rgb(0x11, 0x63, 0xa2)), 0, nikename.length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		view.setText(style);
	}

	private void loadImage(RelativeLayout layout, String url) {
		layout.removeAllViews();
		SimpleDraweeView view = new SimpleDraweeView(mContext);
		GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(mContext.getResources());
		GenericDraweeHierarchy hierarchy = builder.setFadeDuration(300)
				.setPlaceholderImage(mContext.getResources().getDrawable(R.drawable.tacitly_pic), ScaleType.FIT_XY)
				.setFailureImage(mContext.getResources().getDrawable(R.drawable.tacitly_pic), ScaleType.FIT_XY)
				.setActualImageScaleType(ScaleType.FIT_XY).build();
		view.setHierarchy(hierarchy);

		if (!lock) {
			view.setImageURI(Uri.parse(url));
		}

		int height = (int) ((float) width / 1.77f);
		RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(width, height);
		layout.addView(view, mPreLoadingParams);
	}

	public static class ViewHolder {
		RelativeLayout imageLayout;
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
		this.notifyDataSetChanged();
	}

	public void onResume() {

	}

	public void onStop() {

	}

	public void onDestroy() {

	}

	@SuppressLint("SimpleDateFormat")
	public String formatTime(String date) {
		String time = "";
		if (null != date) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");

			try {
				Date strtodate = formatter.parse(date);
				if (null != strtodate) {
					formatter = new SimpleDateFormat("MM月dd日 HH时mm分");
					if (null != formatter) {
						time = formatter.format(strtodate);
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return time;
	}

}
