package com.goluk.crazy.panda.common.http;

/**
 * Created by leege100 on 2016/8/22.
 */
public class ApiException extends RuntimeException {

    public static final int NET_ERROR = -1;
    public static final int TOKEN_NOT_AUTH_OR_EXPIRED = 10001;
    public static final int TOKEN_INVALID = 10002;
    public static final int TOKEN_DEVICE_CHANGED = 10003;

    public ApiException(int resultCode) {
        this(getApiExceptionMessage(resultCode));
    }

    public ApiException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * 由于服务器传递过来的错误信息直接给用户看的话，用户未必能够理解
     * 需要根据错误码对错误信息进行一个转换，在显示给用户
     * @param code
     * @return
     */
    private static String getApiExceptionMessage(int code) {
        String message = "";
        switch (code) {
            case NET_ERROR:
                message = "网络错误";
                break;
            case TOKEN_NOT_AUTH_OR_EXPIRED:
                message = "未授权";
                break;
            case TOKEN_INVALID:
                message = "token无效";
                break;
            case TOKEN_DEVICE_CHANGED:
                message = "设备更换，请重新登录";
                break;
            default:
                message = "";

        }
        return message;
    }
}
