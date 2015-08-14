package cn.com.mobnote.golukmobile.newest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.videosuqare.VideoCategoryActivity;

@SuppressLint("ClickableViewAccessibility")
public class ClickCategoryListener implements OnTouchListener {
	private CategoryDataInfo mCategoryDataInfo = null;
	private Context mContext = null;
	private NewestAdapter mNewestAdapter = null;
	
	public ClickCategoryListener(Context context, CategoryDataInfo info, NewestAdapter adapter) {
		this.mCategoryDataInfo = info;
		this.mContext = context;
		this.mNewestAdapter = adapter;
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		switch (arg1.getAction()) {
		case MotionEvent.ACTION_DOWN:
			hideAnimation(arg0);
			break;
		case MotionEvent.ACTION_UP:
			showAnimation(arg0);
			jump();
			
			final ImageView mengban = (ImageView)arg0.findViewById(R.id.mengban);
			final TextView mTitleName = (TextView) arg0.findViewById(R.id.mTitleName);
			final TextView mUpdateTime = (TextView) arg0.findViewById(R.id.mUpdateTime);
			
			mengban.postDelayed(new Runnable() {
				@Override
				public void run() {
					mengban.setVisibility(View.VISIBLE);
					mTitleName.setVisibility(View.VISIBLE);
					mUpdateTime.setVisibility(View.VISIBLE);
				}
			}, 300);
			break;
		case MotionEvent.ACTION_CANCEL:
			showAnimation(arg0);
			break;

		default:
			break;
		}
		
		return true;
	}
	
	private void jump() {
		if(mNewestAdapter.getClickLock()) {
			return;
		}
		
		// 跳转到点播
		mNewestAdapter.setClickLock(true);
		Intent intent = new Intent(mContext, VideoCategoryActivity.class);
		intent.putExtra(VideoCategoryActivity.KEY_VIDEO_CATEGORY_TYPE, VideoCategoryActivity.CATEGORY_TYPE_DB);
		intent.putExtra(VideoCategoryActivity.KEY_VIDEO_CATEGORY_ATTRIBUTE, mCategoryDataInfo.id);
		intent.putExtra(VideoCategoryActivity.KEY_VIDEO_CATEGORY_TITLE, mCategoryDataInfo.name);
		mContext.startActivity(intent);
	}
	
	private void showAnimation(final View view) {
		final ImageView mengban = (ImageView)view.findViewById(R.id.mengban);
		final TextView mTitleName = (TextView) view.findViewById(R.id.mTitleName);
		final TextView mUpdateTime = (TextView) view.findViewById(R.id.mUpdateTime);
		
		AlphaAnimation show = new AlphaAnimation(0f, 1.0f);  
		show.setDuration(300);
		show.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {}
			@Override
			public void onAnimationRepeat(Animation arg0) {}
			@Override
			public void onAnimationEnd(Animation arg0) {
				mengban.clearAnimation();
				mTitleName.clearAnimation();
				mUpdateTime.clearAnimation();
				
				mengban.setVisibility(View.VISIBLE);
				mTitleName.setVisibility(View.VISIBLE);
				mUpdateTime.setVisibility(View.VISIBLE);
			}
		});
		
		mengban.startAnimation(show);
		mTitleName.startAnimation(show);
		mUpdateTime.startAnimation(show);
	}
	
	private void hideAnimation(final View view) {
		final ImageView mengban = (ImageView)view.findViewById(R.id.mengban);
		final TextView mTitleName = (TextView) view.findViewById(R.id.mTitleName);
		final TextView mUpdateTime = (TextView) view.findViewById(R.id.mUpdateTime);
		
		AlphaAnimation hide = new AlphaAnimation(1.0f, 0f);  
		hide.setDuration(300);
		hide.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {}
			@Override
			public void onAnimationRepeat(Animation arg0) {}
			@Override
			public void onAnimationEnd(Animation arg0) {
				mengban.clearAnimation();
				mTitleName.clearAnimation();
				mUpdateTime.clearAnimation();
				
				mengban.setVisibility(View.GONE);
				mTitleName.setVisibility(View.GONE);
				mUpdateTime.setVisibility(View.GONE);
			}
		});
		
		mengban.startAnimation(hide);
		mTitleName.startAnimation(hide);
		mUpdateTime.startAnimation(hide);
	}

}