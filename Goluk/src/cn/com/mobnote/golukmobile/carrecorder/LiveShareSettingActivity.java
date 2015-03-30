package cn.com.mobnote.golukmobile.carrecorder;

import cn.com.mobnote.golukmobile.R;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

public class LiveShareSettingActivity extends Activity implements
		OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carrecorder_live_share_setting);
		initView();
		setListener();

	}

	private void initView() {
		boolean cartalkbtn = SettingUtils.getInstance().getBoolean(
				"cartalk", true);// 行车对讲开关
		boolean voiceopen = SettingUtils.getInstance().getBoolean("voiceopen",
				true);// 视频声音开关

		if (cartalkbtn) {
			findViewById(R.id.car_talk).setBackgroundResource(
					R.drawable.carrecorder_setup_option_on);// 打开
		} else {
			findViewById(R.id.car_talk).setBackgroundResource(
					R.drawable.carrecorder_setup_option_off);// 关闭
		}

		if (voiceopen) {
			findViewById(R.id.voice_switch_btn).setBackgroundResource(
					R.drawable.carrecorder_setup_option_on);// 打开
		} else {
			findViewById(R.id.voice_switch_btn).setBackgroundResource(
					R.drawable.carrecorder_setup_option_off);// 关闭
		}
	}

	private void setListener() {
		findViewById(R.id.car_talk).setOnClickListener(this);// 行车对讲
		findViewById(R.id.voice_switch_btn).setOnClickListener(this);// 视频声音开关
		findViewById(R.id.progress).setOnClickListener(this);// 直播时间滚动条
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.progress:
			break;
		case R.id.voice_switch_btn:
			boolean voiceopen = SettingUtils.getInstance().getBoolean("voiceopen");
			this.setButtonsBk(voiceopen, R.id.voice_switch_btn, "voiceopen");
			break;
		case R.id.car_talk:
			boolean cartalk = SettingUtils.getInstance().getBoolean("cartalk");
			this.setButtonsBk(cartalk, R.id.car_talk, "cartalk");
			break;
		default:
			break;
		}
	}

	/**
	 * 设置按钮的背景图片并保存属性值
	 * 
	 * @Title: setButtonsBk
	 * @Description: TODO
	 * @param flog
	 * @param id
	 * @param btn
	 *            void
	 * @author 曾浩
	 * @throws
	 */
	private void setButtonsBk(boolean flog, int id, String btn) {
		if (flog) {
			findViewById(id).setBackgroundResource(
					R.drawable.carrecorder_setup_option_off);
			SettingUtils.getInstance().putBoolean(btn, false);
		} else {
			findViewById(id).setBackgroundResource(
					R.drawable.carrecorder_setup_option_on);
			SettingUtils.getInstance().putBoolean(btn, true);
		}
	}

}
