package cn.com.mobnote.golukmobile.videosuqare;

import java.util.List;
import cn.com.mobnote.golukmobile.R;
import android.content.Context;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class VideoSquareOnClickListener implements OnClickListener {
	
	Context mcontext;
	List<VideoSquareInfo> mVideoSquareListData;
	VideoSquareInfo mVideoSquareInfo;
	public VideoSquareOnClickListener(Context context, List<VideoSquareInfo> videoSquareListData,VideoSquareInfo videoSquareInfo){
		mcontext = context;
		mVideoSquareListData = videoSquareListData;
		mVideoSquareInfo = videoSquareInfo;
	}
	
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.share_btn:
			break;
		case R.id.like_btn:
			Button dz = (Button) view;
			int likenumber = Integer.parseInt(dz.getText().toString());
			dz.setText((likenumber+1)+"");
			String videoid = mVideoSquareInfo.mVideoEntity.videoid;
			for(int i = 0;i<mVideoSquareListData.size();i++){
				VideoSquareInfo vsi =  mVideoSquareListData.get(i);
				if(vsi.mVideoEntity.videoid.equals(videoid)){
					mVideoSquareInfo.mVideoEntity.praisenumber = (likenumber + 1)+"";
					mVideoSquareListData.get(i).mVideoEntity.praisenumber = (likenumber + 1)+"";
					break;
				}
			}

			if(null != VideoSquareListView.mHandler){
				Message msg = new Message();
				msg.what = 1;
				msg.obj = mVideoSquareInfo;
				VideoSquareListView.mHandler.sendMessage(msg);
			}
			
			//dz.setBackgroundResource(R.drawable.livestreaming_heart_btn_down);
			break;
		default:
			break;
		}
	}
	
	
}
