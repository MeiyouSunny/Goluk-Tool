package cn.com.mobnote.golukmobile;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import cn.com.mobnote.util.GolukAnimal;

public class TestActivity extends BaseActivity implements OnClickListener {

	private ImageView mLoadingImg = null;
	private GolukAnimal mLoadingAnimal = null;
	private Button mStartBtn = null;
	int[] animalRes = { R.drawable.finish_pic_14, R.drawable.finish_pic_13, R.drawable.finish_pic_12,
			R.drawable.finish_pic_11, R.drawable.finish_pic_10, R.drawable.finish_pic_9, R.drawable.finish_pic_8,
			R.drawable.finish_pic_7, R.drawable.finish_pic_6, R.drawable.finish_pic_5, R.drawable.finish_pic_4,
			R.drawable.finish_pic_3, R.drawable.finish_pic_2, R.drawable.finish_pic_1 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		mLoadingAnimal = new GolukAnimal(animalRes);
		init();
	}

	private void init() {
		mStartBtn = (Button) findViewById(R.id.start);
		mStartBtn.setOnClickListener(this);
		mLoadingImg = (ImageView) findViewById(R.id.wifi_link_waitconn_img);
		mLoadingImg.setImageDrawable(mLoadingAnimal.getAnimationDrawable());
	}

	private void free() {
		mLoadingAnimal.free();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		free();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.start) {
			mLoadingAnimal.start();
		}
	}

}
