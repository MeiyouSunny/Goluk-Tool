package cn.com.mobnote.golukmobile.videosuqare;

import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

@SuppressLint("InflateParams")
public class VideoCategoryView{
	private Context mContext=null;
	private RelativeLayout mRootLayout=null;
	private boolean onlyOnClick = true;
	
	public VideoCategoryView(Context context){
		mContext=context;
		mRootLayout = (RelativeLayout)LayoutInflater.from(mContext).inflate(R.layout.video_category, null); 
		
		init();
	}

	private void init() {

		// 曝光台
		ImageButton category_btn_one = (ImageButton) mRootLayout
				.findViewById(R.id.category_btn_one);
		//  事故大爆料
		ImageButton category_btn_two = (ImageButton) mRootLayout
				.findViewById(R.id.category_btn_two);
		// 美丽风景
		ImageButton category_btn_three = (ImageButton) mRootLayout
				.findViewById(R.id.category_btn_three);
		// 随手拍
		ImageButton category_btn_four = (ImageButton) mRootLayout
				.findViewById(R.id.category_btn_four);
		
		category_btn_one.setOnClickListener(new click());
		category_btn_two.setOnClickListener(new click());
		category_btn_three.setOnClickListener(new click());
		category_btn_four.setOnClickListener(new click());
	}
	
	public void onDestroy(){
		
	}

	class click implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			switch (view.getId()) {
					
				case R.id.category_btn_one:
					
					if(onlyOnClick == false){
						return;
					}else{
						onlyOnClick = false;
						startActivity("2","1");
						break;
					}
					
				case R.id.category_btn_two:
					if(onlyOnClick == false){
						return;
					}else{
						onlyOnClick = false;
						startActivity("2","2");
						break;
					}
					
				case R.id.category_btn_three:
					if(onlyOnClick == false){
						return;
					}else{
						onlyOnClick = false;
						startActivity("2","3");
						break;
					}
					
				case R.id.category_btn_four:
					if(onlyOnClick == false){
						return;
					}else{
						onlyOnClick = false;
						startActivity("2","4");
						break;
					}
			
				case R.id.back_btn:
					VideoSquarePlayActivity vpa = (VideoSquarePlayActivity) mContext;
					vpa.finish();
					break;
				default:
					break;
			}

		}
	}
	
	public void onResume(){
		onlyOnClick = true;
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
