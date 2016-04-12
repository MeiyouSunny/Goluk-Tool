package com.mobnote.golukmain.internation.login;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import cn.com.tiros.debug.GolukDebugUtils;
import cn.smssdk.EventHandler;
import cn.smssdk.OnSendMessageHandler;
import cn.smssdk.SMSSDK;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventRegister;
import com.mobnote.golukmain.R;

import de.greenrobot.event.EventBus;

public class GolukMobUtils {

	public static String MOB_APPKEY = "fceba22a3706";
	public static String MOB_APPSECRET = "f9de10007342e4592077422fd0426318";

	public static final int[] countryArray = { R.array.smssdk_country_group_a, R.array.smssdk_country_group_b,
			R.array.smssdk_country_group_c, R.array.smssdk_country_group_d, R.array.smssdk_country_group_e,
			R.array.smssdk_country_group_f, R.array.smssdk_country_group_g, R.array.smssdk_country_group_h,
			R.array.smssdk_country_group_i, R.array.smssdk_country_group_j, R.array.smssdk_country_group_k,
			R.array.smssdk_country_group_l, R.array.smssdk_country_group_m, R.array.smssdk_country_group_n,
			R.array.smssdk_country_group_o, R.array.smssdk_country_group_p, R.array.smssdk_country_group_q,
			R.array.smssdk_country_group_r, R.array.smssdk_country_group_s, R.array.smssdk_country_group_t,
			R.array.smssdk_country_group_u, R.array.smssdk_country_group_v, R.array.smssdk_country_group_w,
			R.array.smssdk_country_group_x, R.array.smssdk_country_group_y, R.array.smssdk_country_group_z, };

	private static OnSendMessageHandler messageCallBack = new OnSendMessageHandler() {

		@Override
		public boolean onSendMessage(String arg0, String arg1) {
			GolukDebugUtils.e("", "mob---msg:   onSendMessage arg0: " + arg0 + "   arg1:" + arg1);
			return false;
		}
	};

	private static EventHandler eh = new EventHandler() {

		@Override
		public void afterEvent(int event, int result, Object data) {

			GolukDebugUtils.e("", "mob---msg:  EventHandler: afterEvent event :  " + event + "   result:" + result
					+ "  data:" + data);

			EventBus.getDefault().post(new EventRegister(EventConfig.EVENT_REGISTER_CODE, event, result, data));

			if (SMSSDK.RESULT_COMPLETE == result) {
				// 回调完成
				switch (event) {
				case SMSSDK.EVENT_GET_VERIFICATION_CODE:
					// 获取验证码成功
					break;
				case SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE:
					// 提交验证码成功
					break;
				case SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES:
					// 得到支持的国家列表
					break;
				}
			} else {

			}

			super.afterEvent(event, result, data);
		}

		@Override
		public void beforeEvent(int arg0, Object arg1) {
			super.beforeEvent(arg0, arg1);
		}

	};

	public static ArrayList<CountryBean> getCountryList() {
		final ArrayList<CountryBean> list = new ArrayList<CountryBean>();
		final Resources resources = GolukApplication.getInstance().getContext().getResources();
		final int length = countryArray.length;
		for (int i = 0; i < length; i++) {
			try {
				String[] itemArray = resources.getStringArray(countryArray[i]);
				if (null != itemArray && itemArray.length > 0) {
					final int itemLength = itemArray.length;
					for (int j = 0; j < itemLength; j++) {
						CountryBean bean = getCountryBean(itemArray[j]);
						if (null != bean) {
							list.add(bean);
						}
					}
				}
			} catch (Exception e) {
			}
		}
		return list;
	}

	public static CountryBean getCountryBean(String str) {
		try {
			int nameIndex = str.indexOf(",");
			String name = str.substring(0, nameIndex);
			int areaIndex = str.indexOf(",", nameIndex + 1);
			String area = str.substring(nameIndex + 1, areaIndex);
			int codeIndex = str.indexOf(",", areaIndex + 1);
			String code = str.substring(areaIndex + 1, codeIndex);
			CountryBean bean = new CountryBean();
			bean.name = name;
			bean.code = code;
			bean.area = area;

			return bean;
		} catch (Exception e) {

		}
		return null;
	}

	// 初始化及添加监听
	public static void initMob(Context context) {
		SMSSDK.initSDK(context, MOB_APPKEY, MOB_APPSECRET);
		SMSSDK.registerEventHandler(eh);
	}

	// 取消监听
	public static void destroy() {
		SMSSDK.unregisterEventHandler(eh);
	}

	// 得到验证码
	public static void sendSms(String country, String phone) {
		SMSSDK.getVerificationCode(country, phone, messageCallBack);
	}
}
