package cn.com.mobnote.golukmobile.videodetail;

import cn.com.mobnote.golukmobile.videodetail.VideoDetailAdapter.ViewHolder;
import android.content.Context;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SeekBarListener implements OnSeekBarChangeListener {

	private Context mContext ;
	private ViewHolder mHolder ;
	
	public SeekBarListener(Context context ,ViewHolder holder ){
		this.mContext = context;
		this.mHolder = holder;
	}
	int originProgress = 0;
	
	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		mHolder.mSeekBar.setProgress(originProgress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		originProgress = mHolder.mSeekBar.getProgress();
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {

	}

}
