package cn.com.mobnote.golukmobile.newest;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import android.app.Dialog; 
import android.content.Context;
import android.content.Intent;
import android.view.View;  
import android.widget.TextView;

public class FunctionDialog extends Dialog implements android.view.View.OnClickListener{ 
	private TextView tuijian;
	private TextView jubao;
	private TextView cancle;
	private Context mContext;
	private VideoSquareInfo mVideoSquareInfo;
	 
	public FunctionDialog(Context context, VideoSquareInfo mVideoSquareInfo) { 
		super(context, R.style.CustomDialog);  
		setContentView(R.layout.function_dialog);
		this.mVideoSquareInfo = mVideoSquareInfo;
		mContext = context;
		initLayout();
	}
	
	private void initLayout(){ 
		this.tuijian = (TextView)findViewById(R.id.tuijian);
		this.jubao = (TextView)findViewById(R.id.jubao);
		this.cancle = (TextView)findViewById(R.id.cancle);
		tuijian.setOnClickListener(this);
		jubao.setOnClickListener(this);
		cancle.setOnClickListener(this);
	}
 
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tuijian:
			dismiss();
			Intent intent = new Intent(mContext, RecomVideoActivity.class);
			
			mContext.startActivity(intent);
			break;
		case R.id.jubao:
			dismiss();

			break;
		case R.id.cancle:
			dismiss();
			break;
		default:
			break;
		}
	} 
	
}

