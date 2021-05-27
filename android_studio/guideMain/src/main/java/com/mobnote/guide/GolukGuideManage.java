package com.mobnote.guide;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserStartActivity;
import com.mobnote.golukmain.carrecorder.util.ImageManager;
import com.mobnote.golukmobile.GuideActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.viewpager.widget.ViewPager;

/**
 * @ 功能描述:引导页管理
 * 
 * @author 陈宣宇
 * 
 */

public class GolukGuideManage {

	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	/** 引导页内容 */
	private ViewPager mPager = null;
	/** Tab页面列表 */
	private ArrayList<View> mListViews = null;
	/** 引导页数据适配器 */
	private GolukGuideAdapter mGolukGuideAdapter = null;
	/** 引导页圆点图标view */
	private LinearLayout mImageCursorLayout = null;
	/** 引导页圆点img */
	private List<ImageView> mCursorList = new ArrayList<ImageView>();
	/** 当前引导页下标 */
	private int mCurrentItem = 0;

	private Bitmap mBGBitmap2 = null;
	private Bitmap mBGBitmap3 = null;
	private Bitmap mBGBitmap4 = null;
	private Bitmap mBGBitmap5 = null;

	public GolukGuideManage(Context context) {
		mContext = context;

		if (this.mPager == null) {
			this.mLayoutInflater = LayoutInflater.from(mContext);
			// 视频轮播布局
			this.mPager = (ViewPager) ((GuideActivity) mContext).findViewById(R.id.vPager);

			// 视频轮播圆点布局
			this.mImageCursorLayout = (LinearLayout) ((GuideActivity) mContext).findViewById(R.id.cursor_layout);
		}
	}

	/**
	 * 在线视频初始化
	 */
	public void initGolukGuide() {
		if (this.mPager != null) {
			// 获取视频数据
			this.getLocalOnLineVideoList();
			this.mGolukGuideAdapter = new GolukGuideAdapter(this.mListViews);
			this.mPager.setAdapter(this.mGolukGuideAdapter);
			this.mPager.setOnPageChangeListener(new GuideImageChangeListener());
		}
	}

	/**
	 * 获取本地缓存在线视频列表
	 * 
	 * @return
	 */
	@SuppressLint("InflateParams")
	public void getLocalOnLineVideoList() {
		mListViews = new ArrayList<View>();
		mBGBitmap2 = ImageManager.getBitmapFromResource(R.drawable.guide_2);
//		mBGBitmap3 = ImageManager.getBitmapFromResource(R.drawable.guide_3);
		if(GolukApplication.getInstance().isMainland()) {
			mBGBitmap4 = ImageManager.getBitmapFromResource(R.drawable.guide_4);
		}
		mBGBitmap5 = ImageManager.getBitmapFromResource(R.drawable.guide_5);
		ArrayList<Bitmap> imgId = new ArrayList<>();
		imgId.add(mBGBitmap2);
//		imgId.add(mBGBitmap3);
//		if(GolukApplication.getInstance().isMainland()) {
//			imgId.add(mBGBitmap4);
//		}
		imgId.add(mBGBitmap5);
		for (int i = 0, len = imgId.size(); i < len; i++) {
			// 最后一个,采用最后一个布局,
			RelativeLayout view = null;
			if (i == (len - 1)) {
				view = (RelativeLayout) mLayoutInflater.inflate(R.layout.guide_item_last, null);

				// 开始goluk按钮
				Button btn = (Button) view.findViewById(R.id.start_goluk_btn);
				btn.setOnClickListener(new StartGolukBtnClickListener());
			} else {
				view = (RelativeLayout) mLayoutInflater.inflate(R.layout.guide_item, null);
			}

			// Rect frame = new Rect();
			// int statusBarHeight = frame.top;
			DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
			int width = dm.widthPixels;
			int height = dm.heightPixels;

			// 设置元素margin
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
			// params.setMargins(0,0,0,0);
			view.setLayoutParams(params);

			mListViews.add(view);
			ImageView img = (ImageView) view.findViewById(R.id.guide_img);
			img.setScaleType(ScaleType.FIT_XY);
			img.setImageBitmap(imgId.get(i));
			
			// 添加圆点图标
			addImageCursor();

			// 注册view事件
			// btn.setOnClickListener(new
			// OnLineVideoPlayBtnClickListener(vurl));
		}
	}

	/**
	 * 添加下标点图片
	 * 
	 * @author cxy
	 * @date 2014-6-12
	 */
	private void addImageCursor() {
		// 获取图标view
		if (this.mImageCursorLayout != null) {
			// 设置元素margin
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.setMargins(0, 0, 20, 0);

			ImageView img = new ImageView(mContext);
			img.setBackgroundResource(R.drawable.icon_guide_cursor1);
			img.setLayoutParams(params);

			// 保存图标对象
			this.mCursorList.add(img);
			// 添加图标
			this.mImageCursorLayout.addView(img);
			// 刷新view
			this.mImageCursorLayout.invalidate();

			// 如果当前下标为0,更新圆点高亮和类别图片
			if (this.mCurrentItem == 0) {
				ImageView firstImg = this.mCursorList.get(0);
				firstImg.setBackgroundResource(R.drawable.icon_guide_cursor_curr1);
			}
		}
	}

	/**
	 * 修改当前高亮圆点
	 * 
	 * @param current
	 * @author cxy
	 * @date 2014-11-10
	 */
	public void changeCurrentCursor(int pre, int current) {
		try {
			// 获取当前正在显示的高亮图标
			ImageView preImg = this.mCursorList.get(pre);
			preImg.setBackgroundResource(R.drawable.icon_guide_cursor1);
			// 将要高亮显示的图标
			ImageView currentImg = this.mCursorList.get(current);
			currentImg.setBackgroundResource(R.drawable.icon_guide_cursor_curr1);
		} catch (Exception ex) {
		}
	}

	/**
	 * 视频切换事件监听
	 */
	protected class GuideImageChangeListener implements ViewPager.OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			// arg0是表示你当前选中的页面，这事件是在你页面跳转完毕的时候调用的。
			// 修改高亮圆点
			changeCurrentCursor(mCurrentItem, arg0);
			// 保存当前下标
			mCurrentItem = arg0;
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// arg0 ==1的时候表示正在滑动，arg0==2的时候表示滑动完毕了，arg0==0的时候表示什么都没做，就是停在那。
			if (1 == arg0) {
				// 停止自动轮播
				// handler.removeMessages(1);
			} else if (0 == arg0) {
				// 走完2之后,完全停下来之后就会走0,所以在最后重新启动轮播
				// 重新启动自动轮播
				// startAutoChangeMessage();
			}
		}
	}

	public void destoryImage() {
		if (mBGBitmap2 != null) {
			if (!mBGBitmap2.isRecycled()) {
				mBGBitmap2.recycle();
				mBGBitmap2 = null;
			}
		}
		if (mBGBitmap3 != null) {
			if (!mBGBitmap3.isRecycled()) {
				mBGBitmap3.recycle();
				mBGBitmap3 = null;
			}
		}
		if (mBGBitmap4 != null) {
			if (!mBGBitmap4.isRecycled()) {
				mBGBitmap4.recycle();
				mBGBitmap4 = null;
			}
		}
		if (mBGBitmap5 != null) {
			if (!mBGBitmap5.isRecycled()) {
				mBGBitmap5.recycle();
				mBGBitmap5 = null;
			}
		}
	}

	/**
	 * 点击开始按钮click事件内部类
	 * 
	 * @author cxy
	 */
	protected class StartGolukBtnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			SharedPreferences preferences = mContext.getSharedPreferences("golukmark", Context.MODE_PRIVATE);
			Editor editor = preferences.edit();
			editor.putBoolean("isfirst", false);
			// 提交修改
			editor.commit();
			// 启动个人中心的起始页
			Intent userStart = new Intent(mContext, UserStartActivity.class);
			userStart.putExtra("judgeVideo", false);
			if (null != mContext && mContext instanceof GuideActivity) {
				((GuideActivity) mContext).addWebStartData(userStart);
			}
			mContext.startActivity(userStart);
			((GuideActivity) mContext).finish();
		}
	}
}
