package com.mobnote.golukmain.wifimanage;

 
import java.io.UnsupportedEncodingException;

public class ByteUtil {
	/**整数转字节
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
	/**短整转字节
	 * @param number
	 * @return
	 */
	public static byte[] short2Bytes(short number)
	{
		byte[] result = new byte[2];
		for (int i = 0; i < result.length; i++)
		{
			result[i] = (byte) ((number >>> (8 * i)) & 0xff);
		}
		return result;
	}
 
	
	/**长整转字节
	 * @param number
	 * @return
	 */
	public static byte[] long2Bytes(long number)
	{
		byte[] result = new byte[8];
		for (int i = 0; i < result.length; i++)
		{
			result[i] = (byte) ((number >>> (8 * i)) & 0xff);
		}
		return result;
	}
	
	
	
	/**字节转数组
	 * @param str
	 * @param charsetName
	 * @return
	 */
	public static byte[] String2Bytes(String str, String charsetName)
	{
		try
		{
			byte[] strBytes = null;
			strBytes = str.getBytes(charsetName);
			int len = strBytes.length;
			byte[] result = new byte[len + 1];
			result[0] = (byte) len;
			if (len > 0)
			{
				for (int i = 1; i < result.length; i++)
				{
					result[i] = strBytes[i - 1];
				}
			}
			return result;
		} catch (UnsupportedEncodingException uee)
		{
			uee.printStackTrace();
		}
		return new byte[] { 0 };
	}
	
	// 字节数组转换成int 
	public static int bytes2Int(byte byteArray[], int x, int y)
	{
		int ti = 0;
		for (int i = (y - 1); i >= x; i--)
		{
			ti <<= 8;  
			ti |= byteArray[i] & 0xff;  
		}
		return ti;
	}
	
	public static String byte2String(byte byteArray[], int offset, int length,
			String charsetName)
	{
		try
		{
			return new String(byteArray, offset, length, charsetName);
		} catch (UnsupportedEncodingException uee)
		{
			uee.printStackTrace();
		}
		return null;
	}
	public byte[] toBytes(String str, String charsetName)
	{
		try
		{
			byte[] strBytes = null;
			strBytes = str.getBytes(charsetName);
			int len = strBytes.length;
			byte[] result = new byte[len + 1];
			result[0] = (byte) len;
			if (len > 0)
			{
				for (int i = 1; i < result.length; i++)
				{
					result[i] = strBytes[i - 1];
				}
			}
			return result;
		} catch (UnsupportedEncodingException uee)
		{
			uee.printStackTrace();
		}
		return new byte[] { 0 };
	}
	
}
