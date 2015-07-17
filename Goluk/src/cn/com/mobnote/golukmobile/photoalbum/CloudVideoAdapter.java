package cn.com.mobnote.golukmobile.photoalbum;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CloudVideoAdapter extends PagerAdapter{
	private Context mContext=null;
	
	public CloudVideoAdapter(Context c) {
		this.mContext=c;
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		if(0 == position){
			TextView v1 = new TextView(mContext);
			v1.setText("第一个列表");
			container.addView(v1);
			return v1;
		}else if(1 == position){
			TextView v1 = new TextView(mContext);
			v1.setText("第二个列表");
			container.addView(v1);
			return v1;
		}else {		
			TextView v1 = new TextView(mContext);
			v1.setText("第三个列表");
			container.addView(v1);
			return v1;
		}
	}
	
	@Override
	public int getCount() {
		return 3;
	}
	
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
	
	public void onDestroy() {
		
	}

}

