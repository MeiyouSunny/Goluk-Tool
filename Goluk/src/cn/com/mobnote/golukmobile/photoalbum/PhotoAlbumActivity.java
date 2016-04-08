package cn.com.mobnote.golukmobile.photoalbum;

import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class PhotoAlbumActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_album_activity);

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		Bundle bundle = new Bundle();
		bundle.putString("platform", "1");
		FragmentAlbum fa = new FragmentAlbum();
		fa.setArguments(bundle);
		fragmentTransaction.add(R.id.photo_album_fragment, fa);
		fragmentTransaction.commitAllowingStateLoss();
	}
}
