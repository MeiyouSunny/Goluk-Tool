package com.mobnote.t1sp.gps;

import java.io.UnsupportedEncodingException;

public class HexConvertTools {
    private static final String DEF_CHARSET_NAME = "iso8859-1";

    /**
     * 将字节数组转为Int
     *
     * @param bytes
     * @param offset
     * @return
     */
    public static int bytesToInt(byte[] bytes, int offset) {
        if (bytes == null) {
            return -1;
        }
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }

    public static long bytesToLong(byte[] bytes) {
        if (bytes == null) {
            return -1;
        }
        long value = 0;
        for (int i = 0; i < 8; i++) {
            int shift = (8 - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;
        }
        return value;
    }

    /**
     * 将字节转换为Int
     *
     * @param bytes
     * @return
     */
    public static int bytesToInt(byte[] bytes) {
        return bytesToInt(bytes, 0);
    }

    /**
     * 字节数组转为16进制
     *
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes != null) {
            StringBuilder stringBuilder = new StringBuilder("");
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }
                stringBuilder.append(hex.toUpperCase());
            }
            return stringBuilder.toString();
        }
        return null;
    }

    public static String stringToHex(String str) {
        try {
            return bytesToHex(str.getBytes(DEF_CHARSET_NAME));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 字节串转iso8859-1编码的字符串
     *
     * @param bytes
     * @return
     */
    public static String bytesToStr(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            return new String(bytes, DEF_CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 16进制的字符串转成字节数组
     *
     * @param hexString 16进制格式的字符串
     * @param fillSize  如果不满，则填充0
     * @return 转换后的字节数组
     **/
    public static byte[] hexStrToBytes(String hexString, int fillSize) {
        if (hexString == null) {
            return null;
        }
        hexString = addNullToStr(hexString, fillSize);
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
            byteArray[i] = (byte) (high << 4 | low);
            k += 2;
        }
        return byteArray;
    }

    /**
     * 16进制字符串转换为字节数组
     *
     * @param hexString
     * @return
     */
    public static byte[] hexStrToBytes(String hexString) {
        return hexStrToBytes(hexString, 8);
    }

    public static String hexToUtf8Str(String hexString) {
        if (hexString == null) {
            return null;
        }
        byte[] hexBytes = hexStrToBytes(hexString);
        if (hexBytes != null) {
            return bytesToStr(hexBytes);
        }
        return null;
    }

    public static int hexToInt(String hexString) {
        if (hexString == null) {
            return -1;
        }
        byte[] hexBytes = hexStrToBytes(hexString);
        if (hexBytes != null) {
            return bytesToInt(hexBytes);
        }
        return 0;
    }

    /**
     * 在指定字符串面前，添�?"0"，填充够指定位数
     *
     * @param hexString �?要被填充的字符串
     * @param num       �?要填充后达到的长�?
     * @return
     */
    public static String addNullToStr(String hexString, int num) {
        if (hexString == null) {
            return null;
        }
        for (int i = 0; i < num; i++) {
            hexString = "0" + hexString;
            if (hexString.length() == num)
                return hexString;
        }
        return hexString;
    }

    public static byte[] intToByteArray(final int integer) {
        int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer : integer)) / 8;
        byte[] byteArray = new byte[4];

        for (int n = 0; n < byteNum; n++)
            byteArray[3 - n] = (byte) (integer >>> (n * 8));

        return (byteArray);
    }

    public static byte[] bswap32(int x) {
        return new byte[]{
                (byte) (((x << 24) & 0xff000000) >> 24),
                (byte) (((x << 8) & 0x00ff0000) >> 16),
                (byte) (((x >> 8) & 0x0000ff00) >> 8),
                (byte) (((x >> 24) & 0x000000ff))
        };
    }

    private static final byte[] bswap64(long x) {
        return new byte[]{
                (byte) (((x << 56) & 0xff00000000000000L) >> 56),
                (byte) (((x << 40) & 0x00ff000000000000L) >> 48),
                (byte) (((x << 24) & 0x0000ff0000000000L) >> 40),
                (byte) (((x << 8) & 0x000000ff00000000L) >> 32),
                (byte) (((x >> 8) & 0x00000000ff000000L) >> 24),
                (byte) (((x >> 24) & 0x0000000000ff0000L) >> 16),
                (byte) (((x >> 40) & 0x000000000000ff00L) >> 8),
                (byte) (((x >> 56) & 0x00000000000000ffL))
        };
    }

    /**
     * 将低字节数组转换为int
     *
     * @param b byte[]
     * @return int
     */
    public static int lBytesToInt(byte[] b) {
        int s = 0;
        for (int i = 0; i < 3; i++) {
            if (b[3 - i] >= 0) {
                s = s + b[3 - i];
            } else {
                s = s + 256 + b[3 - i];
            }
            s = s * 256;
        }
        if (b[0] >= 0) {
            s = s + b[0];
        } else {
            s = s + 256 + b[0];
        }
        return s;
    }

    /**
     * 低字节数组转换为float
     *
     * @param b byte[]
     * @return float
     */
    public static float lBytesToFloat(byte[] b) {
        int i = 0;
        Float F = new Float(0.0);
        i = ((((b[3] & 0xff) << 8 | (b[2] & 0xff)) << 8) | (b[1] & 0xff)) << 8 | (b[0] & 0xff);
        return F.intBitsToFloat(i);
    }

}
