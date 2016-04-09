package com.mobnote.wifibind;

import android.annotation.SuppressLint;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@SuppressLint("DefaultLocale")
public class ThreeDES {
	private static final byte[] KEYBYTES = { 0x11, 0x22, 0x4F, 0x47, (byte) 0x88,
			0x10, 0x40, 0x38, 0x28, 0x25, 0x79, 0x23, (byte) 0xCB, (byte) 0xDD,
			0x55, 0x66, 0x71, 0x23, 0x51, (byte) 0x98, 0x30, 0x40, 0x36,
			(byte) 0xE2 };  

	private static final String Algorithm = "DESede";  
														 

 
	public static byte[] encryptMode( byte[] src) {
		try {
		 
			SecretKey deskey = new SecretKeySpec(KEYBYTES, Algorithm);
 
			Cipher c1 = Cipher.getInstance(Algorithm);
			c1.init(Cipher.ENCRYPT_MODE, deskey);
			return c1.doFinal(src);
		} catch (java.security.NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (javax.crypto.NoSuchPaddingException e2) {
			e2.printStackTrace();
		} catch (java.lang.Exception e3) {
			e3.printStackTrace();
		}
		return null;
	}

 
	public static byte[] decryptMode( byte[] src) {
		try {
		 
			SecretKey deskey = new SecretKeySpec(KEYBYTES, Algorithm);

			 
			Cipher c1 = Cipher.getInstance(Algorithm);
			c1.init(Cipher.DECRYPT_MODE, deskey);
			return c1.doFinal(src);
		} catch (java.security.NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (javax.crypto.NoSuchPaddingException e2) {
			e2.printStackTrace();
		} catch (java.lang.Exception e3) {
			e3.printStackTrace();
		}
		return null;
	}

 
	@SuppressLint("DefaultLocale")
	public static String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";

		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
			if (n < b.length - 1)
				hs = hs + ":";
		}
		return hs.toUpperCase();
	}

 

}
