package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.base.BaseActivity;

public class VideoQualityActivity extends BaseActivity implements OnClickListener{
	private TextView mCloseText=null;
	private TextView mLowText=null;
	private TextView mMiddleText=null;
	private TextView mHighText=null;
	
	private ImageButton mCloseIcon=null;
	private ImageButton mLowIcon=null;
	private ImageButton mMiddleIcon=null;
	private ImageButton mHighIcon=null;
	
	private enum SensitivityType{_1080h, _1080l, _720h, _720l};
	private SensitivityType curType=SensitivityType._1080h;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_video_quality, null)); 
		setTitle("视频质量");
		
		initView();
		updateSensitivity(SensitivityType._1080h);
	}
	
	/**
	 * 初始化控件
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void initView(){
		findViewById(R.id.close).setOnClickListener(this);
		findViewById(R.id.low).setOnClickListener(this);
		findViewById(R.id.middle).setOnClickListener(this);
		findViewById(R.id.high).setOnClickListener(this);
		
		
		mCloseText = (TextView)findViewById(R.id.closeText);
		mLowText = (TextView)findViewById(R.id.lowText);
		mMiddleText = (TextView)findViewById(R.id.middleText);
		mHighText = (TextView)findViewById(R.id.highText);
		mCloseIcon = (ImageButton)findViewById(R.id.cRight);
		mLowIcon = (ImageButton)findViewById(R.id.dRight);
		mMiddleIcon = (ImageButton)findViewById(R.id.zRight);
		mHighIcon = (ImageButton)findViewById(R.id.gRight);
		
	}
	
	/**
	 * 切换视频质量
	 * @param type 视频类型
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void updateSensitivity(SensitivityType type){
		curType = type;
		mCloseText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
		mLowText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
		mMiddleText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
		mHighText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
		mCloseIcon.setVisibility(View.GONE);
		mLowIcon.setVisibility(View.GONE);
		mMiddleIcon.setVisibility(View.GONE);
		mHighIcon.setVisibility(View.GONE);
		
		if(SensitivityType._1080h == curType){
			mCloseIcon.setVisibility(View.VISIBLE);
			mCloseText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
		}else if(SensitivityType._1080l == curType){
			mLowIcon.setVisibility(View.VISIBLE);
			mLowText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
		}else if(SensitivityType._720h == curType){
			mMiddleIcon.setVisibility(View.VISIBLE);
			mMiddleText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
		}else{
			mHighIcon.setVisibility(View.VISIBLE);
			mHighText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
		}
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.close:
				updateSensitivity(SensitivityType._1080h);
				break;
			case R.id.low:
				updateSensitivity(SensitivityType._1080l);
				break;
			case R.id.middle:
				updateSensitivity(SensitivityType._720h);
				break;
			case R.id.high:
				updateSensitivity(SensitivityType._720l);
				break;
	
			default:
				break;
		}
	}
	
}
