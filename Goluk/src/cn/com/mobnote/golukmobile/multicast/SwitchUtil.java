package cn.com.mobnote.golukmobile.multicast;

public class SwitchUtil {

	public static String S10To116(int value) {
		int realValue = value > 0 ? value : 256 - value;
		return Integer.toHexString(realValue);
	}

}
