package cn.com.mobnote.golukmobile;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;

/**
 * 开机动画
 * @author mobnote
 *
 */
public class UserWelcomeActivity extends BaseActivity {

	/**VideoView**/
	private VideoView mVideoView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.user_welcome_layout);
		
		mVideoView = (VideoView) findViewById(R.id.user_welcome_videoview);
		mVideoView.setVideoURI(Uri.parse("android.resource://cn.com.mobnote.golukmobile/" + R.raw.start_video));
		mVideoView.start();

		mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();
				mp.setLooping(true);
			}
		});

		mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				mVideoView.setVideoPath("android.resource://cn.com.mobnote.golukmobile/" + R.raw.start_video);
				mVideoView.start();
			}
		});
	}
	
}
