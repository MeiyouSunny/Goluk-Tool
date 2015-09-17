package cn.com.mobnote.golukmobile.usercenter;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.BitmapManager;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.golukmobile.carrecorder.util.MD5Utils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.newest.ClickCategoryListener;
import cn.com.mobnote.golukmobile.newest.ClickCommentListener;
import cn.com.mobnote.golukmobile.newest.ClickNewestListener;
import cn.com.mobnote.golukmobile.newest.ClickPraiseListener;
import cn.com.mobnote.golukmobile.newest.CommentDataInfo;
import cn.com.mobnote.golukmobile.newest.NewestAdapter.ViewHolder;
import cn.com.mobnote.golukmobile.special.ClusterCommentListener;
import cn.com.mobnote.golukmobile.special.ClusterInfo;
import cn.com.mobnote.golukmobile.special.ClusterPressListener;
import cn.com.mobnote.golukmobile.special.SpecialCommentListener;
import cn.com.mobnote.golukmobile.special.SpecialInfo;
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;
import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;

@SuppressLint("InflateParams")
public class UserCenterAdapter extends BaseAdapter implements
		VideoSuqareManagerFn, OnTouchListener {
	private Context mContext = null;
	private List<ClusterInfo> videoListData = null;
	private List<PraiseInfo> praisListData = null;

	private List<ClusterInfo> vData = new ArrayList<ClusterInfo>();
	private List<PraiseInfo> pData = new ArrayList<PraiseInfo>();
	private int count = 0;

	private SharePlatformUtil sharePlatform;

	private int width = 0;

	int VIEW_TYPE = 2;

	final int TYPE_1 = 0;
	final int TYPE_2 = 1;
	final int TYPE_3 = 3;

	private int itemtype = TYPE_2;

	/** 滚动中锁标识 */
	private boolean lock = false;

	private UserInfo userinfo;

	private ImageView praiseselect = null;
	private ImageView videoselect = null;
	private TextView fxsptxt = null;
	private TextView fxspnum = null;

	private TextView dztxt = null;
	private TextView dzpnum = null;

	public UserCenterAdapter(Context context, SharePlatformUtil spf) {
		mContext = context;
		videoListData = new ArrayList<ClusterInfo>();
		praisListData = new ArrayList<PraiseInfo>();
		sharePlatform = spf;
		width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		GolukApplication.getInstance().getVideoSquareManager()
				.addVideoSquareManagerListener("videosharehotlist", this);
	}

	/**
	 * 设置视频数据
	 * 
	 * @param data
	 */
	public void setVideoData(List<ClusterInfo> data) {
		videoListData.clear();
		videoListData.addAll(data);

		vData.clear();
		vData.addAll(data);

		count = videoListData.size();
		count++;
	}

	/**
	 * 设置点赞数据
	 * 
	 * @param data
	 */
	public void setPraisData(List<PraiseInfo> data) {
		praisListData.clear();
		praisListData.addAll(data);

		pData.clear();
		pData.addAll(data);
		count = praisListData.size();
		count++;
	}

	/**
	 * 设置用户数据
	 * 
	 * @param head
	 */
	public void setUserData(UserInfo user) {
		userinfo = user;
	}

	// 每个convert view都会调用此方法，获得当前所需要的view样式
	@Override
	public int getItemViewType(int position) {
		int p = position;
		if (p == 0)
			return TYPE_1;
		else
			return itemtype;
	}

	@Override
	public int getViewTypeCount() {
		return 4;
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

	ViewHolder holder;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		int type = getItemViewType(position);

		switch (type) {
		case TYPE_1:
			if (userinfo != null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.user_center_userinfo, null);
				LinearLayout praise = (LinearLayout) convertView
						.findViewById(R.id.praiselayout);
				LinearLayout share = (LinearLayout) convertView
						.findViewById(R.id.sharelayout);
				
				praiseselect = (ImageView) convertView
						.findViewById(R.id.praise_select);
				videoselect = (ImageView) convertView
						.findViewById(R.id.video_select);
				fxsptxt = (TextView) convertView.findViewById(R.id.fxsp_txt);
				fxspnum = (TextView) convertView.findViewById(R.id.fxsp_num);

				dztxt = (TextView) convertView.findViewById(R.id.dz_txt);
				dzpnum = (TextView) convertView.findViewById(R.id.dz_num);

				if (itemtype == TYPE_2) {
					praiseselect.setVisibility(View.INVISIBLE);
					videoselect.setVisibility(View.VISIBLE);
					fxsptxt.setTextColor(Color.rgb(9, 132, 255));
					fxspnum.setTextColor(Color.rgb(9, 132, 255));

					dztxt.setTextColor(Color.rgb(255, 255, 255));
					dzpnum.setTextColor(Color.rgb(255, 255, 255));
				} else {
					videoselect.setVisibility(View.INVISIBLE);
					praiseselect.setVisibility(View.VISIBLE);

					dztxt.setTextColor(Color.rgb(9, 132, 255));
					dzpnum.setTextColor(Color.rgb(9, 132, 255));

					fxsptxt.setTextColor(Color.rgb(255, 255, 255));
					fxspnum.setTextColor(Color.rgb(255, 255, 255));
				}

				share.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub

						itemtype = TYPE_2;
						videoListData.clear();
						videoListData.addAll(vData);

						praisListData.clear();

						count = videoListData.size();
						count++;
						notifyDataSetChanged();
					}
				});

				praise.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub

						itemtype = TYPE_3;
						praisListData.clear();
						praisListData.addAll(pData);

						videoListData.clear();

						count = praisListData.size();
						count++;
						notifyDataSetChanged();
					}
				});
				
			}
			break;
		case TYPE_2:
			int index_v = position;
			index_v--;
			ClusterInfo clusterInfo = videoListData.get(index_v);
			if (convertView == null) {

				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.newest_list_item, null);
				holder.imageLayout = (RelativeLayout) convertView
						.findViewById(R.id.imageLayout);
				holder.headimg = (ImageView) convertView
						.findViewById(R.id.headimg);
				holder.nikename = (TextView) convertView
						.findViewById(R.id.nikename);
				holder.time = (TextView) convertView.findViewById(R.id.time);
				holder.function = (ImageView) convertView
						.findViewById(R.id.function);

				holder.praiseLayout = (LinearLayout) convertView
						.findViewById(R.id.praiseLayout);
				holder.zanIcon = (ImageView) convertView
						.findViewById(R.id.zanIcon);
				holder.zanText = (TextView) convertView
						.findViewById(R.id.zanText);

				holder.commentLayout = (LinearLayout) convertView
						.findViewById(R.id.commentLayout);
				holder.commentIcon = (ImageView) convertView
						.findViewById(R.id.commentIcon);
				holder.commentText = (TextView) convertView
						.findViewById(R.id.commentText);

				holder.shareLayout = (LinearLayout) convertView
						.findViewById(R.id.shareLayout);
				holder.shareIcon = (ImageView) convertView
						.findViewById(R.id.shareIcon);
				holder.shareText = (TextView) convertView
						.findViewById(R.id.shareText);

				holder.zText = (TextView) convertView.findViewById(R.id.zText);
				holder.weiguan = (TextView) convertView
						.findViewById(R.id.weiguan);
				holder.weiguan = (TextView) convertView
						.findViewById(R.id.weiguan);
				holder.totalcomments = (TextView) convertView
						.findViewById(R.id.totalcomments);

				holder.detail = (TextView) convertView
						.findViewById(R.id.detail);
				holder.comment1 = (TextView) convertView
						.findViewById(R.id.comment1);
				holder.comment2 = (TextView) convertView
						.findViewById(R.id.comment2);
				holder.comment3 = (TextView) convertView
						.findViewById(R.id.comment3);

				int height = (int) ((float) width / 1.77f);
				RelativeLayout.LayoutParams mPlayerLayoutParams = new RelativeLayout.LayoutParams(
						width, height);
				mPlayerLayoutParams.addRule(RelativeLayout.BELOW,
						R.id.headlayout);
				holder.imageLayout.setLayoutParams(mPlayerLayoutParams);

				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.nikename.setText(clusterInfo.author);
			holder.time.setText(clusterInfo.sharingtime);
			holder.zText.setText(clusterInfo.praisenumber);
			holder.weiguan.setText(clusterInfo.clicknumber + " 围观");
			holder.detail.setText(clusterInfo.author + "  "
					+ clusterInfo.describe);
			holder.totalcomments.setText("查看所有" + clusterInfo.comments + "条评论");
			holder.zText.setText(clusterInfo.praisenumber + " 赞");
			loadImage(holder.imageLayout, clusterInfo.imagepath);
			initListener(index_v);
			// 没点过
			if ("0".equals(clusterInfo.ispraise)) {
				holder.zanIcon
						.setBackgroundResource(R.drawable.videodetail_like);
			} else {// 点赞过
				holder.zanIcon
						.setBackgroundResource(R.drawable.videodetail_like_press);
			}
			if (clusterInfo.ci1 != null) {
				holder.comment1.setText(clusterInfo.ci1.name + "  "
						+ clusterInfo.ci1.text);
			} else {
				holder.comment1.setVisibility(View.GONE);
			}

			if (clusterInfo.ci2 != null) {
				holder.comment2.setText(clusterInfo.ci2.name + "  "
						+ clusterInfo.ci2.text);
			} else {
				holder.comment2.setVisibility(View.GONE);
			}

			if (clusterInfo.ci3 != null) {
				holder.comment3.setText(clusterInfo.ci3.name + "  "
						+ clusterInfo.ci3.text);
			} else {
				holder.comment3.setVisibility(View.GONE);
			}
			break;
		case TYPE_3:
			int index_p = position;
			index_p--;
			PraiseInfo prais = praisListData.get(index_p);
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.user_center_praise, null);
			}
			break;
		default:
			break;
		}

		return convertView;
	}

	private String mVideoId = null;

	public void setWillShareVideoId(String vid) {
		mVideoId = vid;
	}

	public String getWillShareVideoId() {
		return mVideoId;
	}

	private void initListener(int index) {
		ClusterInfo clusterInfo = videoListData.get(index);

		holder.commentLayout.setOnClickListener(new ClusterCommentListener(
				mContext, clusterInfo, false));
		holder.totalcomments.setOnClickListener(new ClusterCommentListener(
				mContext, clusterInfo, false));
		// holder.praiseLayout.setOnClickListener(new
		// ClusterPressListener(mContext, clusterInfo, this));
		// holder.function.setOnClickListener(new ClusterPressListener(mContext,
		// clusterInfo, this));
		// holder.imageLayout.setOnClickListener(new
		// SpecialCommentListener(mContext,this,clusterInfo.imagepath,clusterInfo.videopath,"suqare",clusterInfo.videotype,clusterInfo.videoid));
		// holder.shareLayout.setOnClickListener(new
		// SpecialCommentListener(mContext,this,
		// clusterInfo.imagepath,clusterInfo.videopath,"suqare",clusterInfo.videotype,clusterInfo.videoid));
	}

	public int getUserHead(String head) {
		return 0;
	}

	public void onBackPressed() {

	}

	public void onResume() {
		GolukApplication.getInstance().getVideoSquareManager()
				.addVideoSquareManagerListener("videosharehotlist", this);
	}

	/**
	 * 点赞
	 * 
	 * @Title: setLikePress
	 * @Description: TODO
	 * @param clusterInfo
	 *            void
	 * @author 曾浩
	 * @throws
	 */
	public void setLikePress(ClusterInfo clusterInfo) {
		for (int i = 0; i < videoListData.size(); i++) {
			ClusterInfo cl = videoListData.get(i);
			if (cl.videoid.equals(clusterInfo.videoid)) {
				videoListData.set(i, clusterInfo);
				break;
			}
		}

		this.notifyDataSetChanged();

	}

	@SuppressLint("SimpleDateFormat")
	public String formatTime(String date) {
		String time = "";
		if (null != date) {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyyMMddHHmmssSSS");

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

	private void loadImage(RelativeLayout layout, String url) {
		layout.removeAllViews();
		SimpleDraweeView view = new SimpleDraweeView(mContext);
		GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(
				mContext.getResources());
		GenericDraweeHierarchy hierarchy = builder.setFadeDuration(300)
		// .setPlaceholderImage(mContext.getResources().getDrawable(R.drawable.tacitly_pic),
		// ScaleType.FIT_XY)
		// .setFailureImage(mContext.getResources().getDrawable(R.drawable.tacitly_pic),
		// ScaleType.FIT_XY)
				.setActualImageScaleType(ScaleType.FIT_XY).build();
		view.setHierarchy(hierarchy);

		if (!lock) {
			view.setImageURI(Uri.parse(url));
		}

		int height = (int) ((float) width / 1.77f);
		RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(
				width, height);
		layout.addView(view, mPreLoadingParams);
		//
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

		TextView comment1;
		TextView comment2;
		TextView comment3;
	}

	public Bitmap getThumbBitmap(String netUrl) {
		String name = MD5Utils.hashKeyForDisk(netUrl) + ".0";
		String path = Environment.getExternalStorageDirectory()
				+ File.separator + "goluk/image_cache";
		File file = new File(path + File.separator + name);
		Bitmap t_bitmap = null;
		if (null == file) {
			return null;
		}
		if (file.exists()) {
			t_bitmap = ImageManager.getBitmapFromCache(file.getAbsolutePath(),
					100, 100);
		}
		return t_bitmap;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		switch (v.getId()) {

		case R.id.share_btn:
			Button sharebtn = (Button) v;
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				Drawable more_down = mContext.getResources().getDrawable(
						R.drawable.share_btn_press);
				sharebtn.setCompoundDrawablesWithIntrinsicBounds(more_down,
						null, null, null);
				sharebtn.setTextColor(Color.rgb(59, 151, 245));
				break;
			case MotionEvent.ACTION_UP:
				Drawable more_up = mContext.getResources().getDrawable(
						R.drawable.share_btn);
				sharebtn.setCompoundDrawablesWithIntrinsicBounds(more_up, null,
						null, null);
				sharebtn.setTextColor(Color.rgb(136, 136, 136));
				break;
			}
			break;
		}
		return false;
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,
			Object param2) {
		// TODO Auto-generated method stub

	}

}
