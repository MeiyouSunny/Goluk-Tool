package cn.com.mobnote.golukmobile.videosuqare;

import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

@SuppressLint("InflateParams")
public class VideoCategoryView{
	private Context mContext=null;
	private RelativeLayout mRootLayout=null;
	private int screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
	private float density = SoundUtils.getInstance().getDisplayMetrics().density;
	
	public VideoCategoryView(Context context){
		mContext=context;
		mRootLayout = (RelativeLayout)LayoutInflater.from(mContext).inflate(R.layout.video_category, null); 
		
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
		
		int width = (int)((screenWidth - 15*density)/2);
		int height = (int)(width/1.969);
		LinearLayout.LayoutParams oneParams = new LinearLayout.LayoutParams(width, height);
		LinearLayout.LayoutParams twoParams = new LinearLayout.LayoutParams(width, height);
		LinearLayout.LayoutParams threeParams = new LinearLayout.LayoutParams(width, height);
		LinearLayout.LayoutParams fourParams = new LinearLayout.LayoutParams(width, height);
		LinearLayout.LayoutParams fiveParams = new LinearLayout.LayoutParams(width, height);
		LinearLayout.LayoutParams sixParams = new LinearLayout.LayoutParams(width, height);
		LinearLayout.LayoutParams sevenParams = new LinearLayout.LayoutParams(width, height);
		LinearLayout.LayoutParams eightParams = new LinearLayout.LayoutParams(width, height);
		LinearLayout.LayoutParams nineParams = new LinearLayout.LayoutParams(width, height);
		LinearLayout.LayoutParams tenParams = new LinearLayout.LayoutParams(width, height);
		
		oneParams.setMargins(0, 0, (int)(5*density), 0);
		threeParams.setMargins(0, 0, (int)(5*density), 0);
		fiveParams.setMargins(0, 0, (int)(5*density), 0);
		sevenParams.setMargins(0, 0, (int)(5*density), 0);
		nineParams.setMargins(0, 0, (int)(5*density), 0);

		category_btn_one.setLayoutParams(oneParams);
		category_btn_two.setLayoutParams(twoParams);
		category_btn_three.setLayoutParams(threeParams);
		category_btn_four.setLayoutParams(fourParams);
		category_btn_five.setLayoutParams(fiveParams);
		category_btn_six.setLayoutParams(sixParams);
		category_btn_seven.setLayoutParams(sevenParams);
		category_btn_eight.setLayoutParams(eightParams);
		category_btn_nine.setLayoutParams(nineParams);
		category_btn_ten.setLayoutParams(tenParams);

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
	
	public void onDestroy(){
		
	}

	class click implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			switch (view.getId()) {
				case R.id.live_categroy:
					startActivity("1","0");
					break;
				case R.id.category_btn_one:
					startActivity("2","1");
					break;
				case R.id.category_btn_two:
					startActivity("2","2");
					break;
				case R.id.category_btn_three:
					startActivity("2","3");
					break;
				case R.id.category_btn_four:
					startActivity("2","4");
					break;
				case R.id.category_btn_five:
					startActivity("2","5");
					break;
				case R.id.category_btn_six:
					startActivity("2","6");
					break;
				case R.id.category_btn_seven:
					startActivity("2","7");
					break;
				case R.id.category_btn_eight:
					startActivity("2","8");
					break;
				case R.id.category_btn_nine:
					startActivity("2","9");
					break;
				case R.id.category_btn_ten:
					startActivity("2","10");
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

	public void startActivity(String type,String attribute) {
		MainActivity play = (MainActivity)mContext;
		Intent intent = new Intent(); 
		intent.putExtra("type", type);
		intent.putExtra("attribute", attribute);
        intent.setClass(play,VideoSquarePlayActivity.class);
		mContext.startActivity(intent);
	}

	public View getView() {
		return mRootLayout;
	}
}
