package cn.com.mobnote.golukmobile.videosuqare;

import cn.com.mobnote.golukmobile.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

@SuppressLint("InflateParams")
public class VideoCategoryView {
	private Context mContext = null;
	private RelativeLayout mRootLayout = null;

	public VideoCategoryView(Context context) {
		mContext = context;
		mRootLayout = (RelativeLayout) LayoutInflater.from(mContext).inflate(
				R.layout.video_category, null);
		init();

	}

	private void init() {

		// 热门直播按钮
		RelativeLayout live_categroy = (RelativeLayout) mRootLayout
				.findViewById(R.id.live_categroy);

		// 曝光台
		ImageButton category_btn_one = (ImageButton) mRootLayout
				.findViewById(R.id.category_btn_one);
		// 碰瓷达人
		ImageButton category_btn_two = (ImageButton) mRootLayout
				.findViewById(R.id.category_btn_two);
		// 路上风景
		ImageButton category_btn_three = (ImageButton) mRootLayout
				.findViewById(R.id.category_btn_three);
		// 随手拍
		ImageButton category_btn_four = (ImageButton) mRootLayout
				.findViewById(R.id.category_btn_four);
		// 事故大爆料
		ImageButton category_btn_five = (ImageButton) mRootLayout
				.findViewById(R.id.category_btn_five);
		// 堵车预警
		ImageButton category_btn_six = (ImageButton) mRootLayout
				.findViewById(R.id.category_btn_six);
		// 惊险十分
		ImageButton category_btn_seven = (ImageButton) mRootLayout
				.findViewById(R.id.category_btn_seven);
		// 疯狂超车
		ImageButton category_btn_eight = (ImageButton) mRootLayout
				.findViewById(R.id.category_btn_eight);
		// 感人瞬间
		ImageButton category_btn_nine = (ImageButton) mRootLayout
				.findViewById(R.id.category_btn_nine);
		// 传递正能量
		ImageButton category_btn_ten = (ImageButton) mRootLayout
				.findViewById(R.id.category_btn_ten);

		live_categroy.setOnClickListener(new click());
		category_btn_one.setOnClickListener(new click());
		category_btn_two.setOnClickListener(new click());
		category_btn_three.setOnClickListener(new click());
		category_btn_four.setOnClickListener(new click());
		category_btn_five.setOnClickListener(new click());
		category_btn_six.setOnClickListener(new click());
		category_btn_seven.setOnClickListener(new click());
		category_btn_eight.setOnClickListener(new click());
		category_btn_nine.setOnClickListener(new click());
		category_btn_ten.setOnClickListener(new click());
		
	}

	class click implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			switch (view.getId()) {
				case R.id.live_categroy:
					startActivity();
					break;
				case R.id.category_btn_one:
					startActivity();
					break;
				case R.id.category_btn_three:
					startActivity();
					break;
				case R.id.category_btn_four:
					startActivity();
					break;
				case R.id.category_btn_five:
					startActivity();
					break;
				case R.id.category_btn_six:
					startActivity();
					break;
				case R.id.category_btn_seven:
					startActivity();
					break;
				case R.id.category_btn_eight:
					startActivity();
					break;
				case R.id.category_btn_nine:
					startActivity();
					break;
				case R.id.category_btn_ten:
					startActivity();
					break;
				case R.id.category_btn_two:
					startActivity();
					break;
				case R.id.back_btn:
					VideoSquarePlayActivity vpa = (VideoSquarePlayActivity) mContext;
					vpa.finish();
					break;
				default:
					break;
			}

		}
	}

	public void startActivity() {
		VideoSquareActivity play = (VideoSquareActivity)mContext;
		Intent intent = new Intent(); 
        intent.setClass(play,VideoSquarePlayActivity.class);
        play.startActivity(intent);
	}

	public View getView() {
		return mRootLayout;
	}
}
