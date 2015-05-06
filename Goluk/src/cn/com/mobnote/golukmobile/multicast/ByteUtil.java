package cn.com.mobnote.golukmobile.multicast;



import java.io.UnsupportedEncodingException;

public class ByteUtil {
	/**
	 * 整数转字节
	 * 
	 * @param number
	 * @return
	 */
	public static byte[] int2Bytes(int number) {
		byte[] result = new byte[4];
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) ((number >>> (8 * i)) & 0xff);
		}
		return result;
	}

	/**
	 * 短整转字节
	 * 
	 * @param number
	 * @return
	 */
	public static byte[] short2Bytes(short number) {
		byte[] result = new byte[2];
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) ((number >>> (8 * i)) & 0xff);
		}
		return result;
	}

	/**
	 * 长整转字节
	 * 
	 * @param number
	 * @return
	 */
	public static byte[] long2Bytes(long number) {
		byte[] result = new byte[8];
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) ((number >>> (8 * i)) & 0xff);
		}
		return result;
	}
	/**
	 * 字节转数组
	 * 
	 * @param str
	 * @param charsetName
	 * @return
	 */
	public static byte[] string2Bytes(String str,int length) {
		 byte[] bt=str.getBytes();
		
		if(bt.length<length){
			byte[] rs=new byte[length];
			System.arraycopy(bt, 0, rs, 0, bt.length);
		return rs;
		}
		return bt;
	}
	
 
	/**
	 * 字节转数组
	 * 
	 * @param str
	 * @param charsetName
	 * @return
	 */
	public static byte[] string2Bytes(String str, String charsetName) {
		try {
			byte[] strBytes = null;
			if(null==charsetName){
				strBytes = str.getBytes();
			}else{
				strBytes = str.getBytes(charsetName);
			}
			
			int len = strBytes.length;
			byte[] result = new byte[len + 1];
			result[0] = (byte) len;
			if (len > 0) {
				for (int i = 1; i < result.length; i++) {
					result[i] = strBytes[i - 1];
				}
			}
			return result;
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}
		return new byte[] { 0 };
	}

	// 字节数组转换成int
	public static int bytes2Int(byte byteArray[], int x, int y) {
		int ti = 0;
		for (int i = (y - 1); i >= x; i--) {
			ti <<= 8;
			ti |= byteArray[i] & 0xff;
		}
		return ti;
	}

	public static String byte2String(byte byteArray[], int offset, int length,
			String charsetName) {
		try {
			return new String(byteArray, offset, length, charsetName);
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}
		return null;
	}

	public static String byte2String(byte byteArray[], int offset, int length
		 ) {
		 
			return new String(byteArray, offset, length);
		 
		 
	}
	public byte[] toBytes(String str, String charsetName) {
		try {
			byte[] strBytes = null;
			strBytes = str.getBytes(charsetName);
			int len = strBytes.length;
			byte[] result = new byte[len + 1];
			result[0] = (byte) len;
			if (len > 0) {
				for (int i = 1; i < result.length; i++) {
					result[i] = strBytes[i - 1];
				}
			}
			return result;
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}
		return new byte[] { 0 };
	}
	/** helper method to convert 2 bytes to a short */
	public static short bytes2Short(byte byteArray[], int x, int y)
	{
		short ti = 0;
		for (int i = (y - 1); i >= x; i--)
		{
			ti <<= 8;
			ti |= byteArray[i] & 0xff;
		}
		return ti;
	}

	public short toShort(byte byteArray[], int x, int y)
	{
		short ti = 0;
		for (int i = (y - 1); i >= x; i--)
		{
			ti <<= 8;
			ti |= byteArray[i] & 0xff;
		}
		return ti;
	}

	/**
	 * char转ascii
	 * 
	 * @param c
	 * @return
	 */
	public static int char2ASCII(char c) {
		return (int) c;
	}

	public static char ascii2Char(int ascii) {
		return (char) ascii;
	}

	/**
	 * 字符串转换为ASCII码
	 * 
	 * @param s
	 * @return
	 */
	public static int[] string2ASCII(String s) {
		if (s == null || "".equals(s)) {
			return null;
		}

		char[] chars = s.toCharArray();
		int[] asciiArray = new int[chars.length];

		for (int i = 0; i < chars.length; i++) {
			asciiArray[i] = char2ASCII(chars[i]);
		}
		return asciiArray;
	}

	public static String getIntArrayString(int[] intArray) {
		return getIntArrayString(intArray, ",");
	}

	public static String getIntArrayString(int[] intArray, String delimiter) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < intArray.length; i++) {
			sb.append(intArray[i]).append(delimiter);
		}
		return sb.toString();
	}

	public static String getStringAcsii(String s) {
		return getIntArrayString(string2ASCII(s),"");

	}
	public static long bytes2Long(byte byteArray[], int x, int y)
	{
		long ti = 0;
		for (int i = (y - 1); i >= x; i--)
		{
			ti <<= 8;
			ti |= byteArray[i] & 0xff;
		}
		return ti;
	}

	public long toLong(byte byteArray[], int x, int y)
	{
		long ti = 0;
		for (int i = (y - 1); i >= x; i--)
		{
			ti <<= 8;
			ti |= byteArray[i] & 0xff;
		}
		return ti;
	}
}
