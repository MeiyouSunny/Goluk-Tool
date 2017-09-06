package com.rd.veuisdk;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * @author JIAN
 * @date 2016-12-8 上午9:40:39
 */
public class SdkEntryHandler {

    private Object isdk;
    private static SdkEntryHandler instance;

    static SdkEntryHandler getInstance() {
        if (null == instance) {
            instance = new SdkEntryHandler();
        }
        return instance;
    }


    void setICallBack(com.rd.veuisdk.callback.ISdkCallBack iback) {
        isdk = iback;
    }

    /**
     * 简单录制
     *
     * @param context
     * @param path
     */
    void onExportRecorder(Context context, String path) {
        if (null != isdk) {
            Message msg = mhandler.obtainMessage(MSG_CAMERA_EXPORT);
            msg.obj = new IData(context, path);
            msg.sendToTarget();
        }
    }

    /**
     * 录制并编辑
     *
     * @param context
     * @param path
     */
    void onExportRecorderEdit(Context context, String path) {
        if (null != isdk) {
            Message msg = mhandler.obtainMessage(MSG_CAMERA_EDIT_EXPORT);
            msg.obj = new IData(context, path);
            msg.sendToTarget();
        }
    }

    /**
     * 编辑导出
     *
     * @param context
     * @param path
     */
    void onExport(Context context, String path) {
        if (null != isdk) {
            Message msg = mhandler.obtainMessage(MSG_EDIT_EXPORT);
            msg.obj = new IData(context, path);
            msg.sendToTarget();
        }
    }

    /**
     * 普通截取导出视频
     *
     * @param context
     * @param path
     */
    void onTrimExport(Context context, String path) {
        if (null != isdk) {
            Message msg = mhandler.obtainMessage(MSG_TRIM_VIDEO_EXPORT);
            msg.obj = new IData(context, path);
            msg.sendToTarget();
        }
    }

    /**
     * 定长截取导出视频
     *
     * @param context
     * @param path
     */
    void onTrimDurationExport(Context context, String path) {
        if (null != isdk) {
            Message msg = mhandler
                    .obtainMessage(MSG_TRIM_VIDEO_DURATION_EXPORT);
            msg.obj = new IData(context, path);
            msg.sendToTarget();
        }
    }

    /**
     * 普通截取导出时间
     *
     * @param context
     * @param startTime
     * @param endTime
     */
    void onInterceptVideo(Context context, int startTime, int endTime) {
        if (null != isdk) {
            Message msg = mhandler.obtainMessage(MSG_INTERCEP_TVIDEO);
            msg.obj = context;
            msg.arg1 = startTime;
            msg.arg2 = endTime;
            msg.sendToTarget();
        }
    }

    /**
     * 截取定长导出时间
     *
     * @param context
     * @param startTime
     * @param endTime
     */
    void onInterceptVideoDuration(Context context, int startTime, int endTime) {
        if (null != isdk) {
            Message msg = mhandler.obtainMessage(MSG_TRIM_DURATION_VIDEO);
            msg.obj = context;
            msg.arg1 = startTime;
            msg.arg2 = endTime;
            msg.sendToTarget();
        }
    }

    void onTrimDialog(Context context, int exportType) {
        if (null != isdk) {
            Message msg = mhandler.obtainMessage(MSG_TRIM_COMMIT);
            msg.obj = context;
            msg.arg1 = exportType;
            msg.sendToTarget();
        }
    }

    void onSelectImage(Context context) {
        if (null != isdk) {
            Message msg = mhandler.obtainMessage(MSG_SELECT_IMAGE);
            msg.obj = context;
            msg.sendToTarget();
        }
    }

    void onSelectVideo(Context context) {
        if (null != isdk) {
            Message msg = mhandler.obtainMessage(MSG_SELECT_VIDEO);
            msg.obj = context;
            msg.sendToTarget();
        }
    }

    private final int MSG_EDIT_EXPORT = 1; // 编辑导出视频路径
    private final int MSG_CAMERA_EXPORT = 2; // 简单录制
    private final int MSG_INTERCEP_TVIDEO = 3; // 普通截取导出时间
    private final int MSG_TRIM_COMMIT = 4;
    private final int MSG_SELECT_IMAGE = 5; // 打开自定义相册
    private final int MSG_SELECT_VIDEO = 6;
    private final int MSG_TRIM_VIDEO_EXPORT = 7; // 普通截取导出视频
    private final int MSG_TRIM_VIDEO_DURATION_EXPORT = 8; // 定长截取导出视频
    private final int MSG_CAMERA_EDIT_EXPORT = 9; // 摄像头录制并编辑
    private final int MSG_TRIM_DURATION_VIDEO = 10; // 定长截取导出时间

    /**
     * 构造msg.obj
     *
     * @author JIAN
     */
    private class IData {

        Context context;
        String path;

        public IData(Context context, String path) {
            this.context = context;
            this.path = path;
        }

    }

    private final Handler mhandler = new Handler(Looper.getMainLooper()) {

        @SuppressWarnings("deprecation")
        @Override
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case MSG_EDIT_EXPORT:
                    if (null != isdk) {
                        IData idata = (IData) msg.obj;
                        if (isdk instanceof com.rd.veuisdk.callback.ISdkCallBack) {
                            ((com.rd.veuisdk.callback.ISdkCallBack) isdk)
                                    .onGetVideoPath(idata.context,
                                            SdkEntry.EDIT_EXPORT, idata.path);
                        }
                    }
                    break;
                case MSG_CAMERA_EXPORT:
                    if (null != isdk) {
                        IData idata = (IData) msg.obj;
                        if (isdk instanceof com.rd.veuisdk.callback.ISdkCallBack) {
                            ((com.rd.veuisdk.callback.ISdkCallBack) isdk)
                                    .onGetVideoPath(idata.context,
                                            SdkEntry.CAMERA_EXPORT, idata.path);
                        }
                    }
                    break;
                case MSG_CAMERA_EDIT_EXPORT:
                    if (null != isdk) {
                        IData idata = (IData) msg.obj;
                        if (isdk instanceof com.rd.veuisdk.callback.ISdkCallBack) {
                            ((com.rd.veuisdk.callback.ISdkCallBack) isdk)
                                    .onGetVideoPath(idata.context,
                                            SdkEntry.CAMERA_EDIT_EXPORT,
                                            idata.path);
                        }
                    }
                    break;
                case MSG_INTERCEP_TVIDEO:
                    if (null != isdk) {
                        if (isdk instanceof com.rd.veuisdk.callback.ISdkCallBack) {
                            ((com.rd.veuisdk.callback.ISdkCallBack) isdk)
                                    .onGetVideoTrimTime((Context) msg.obj,
                                            SdkEntry.TRIMVIDEO_EXPORT, msg.arg1,
                                            msg.arg2);
                        }
                    }
                    break;
                case MSG_TRIM_DURATION_VIDEO:
                    if (null != isdk) {
                        if (isdk instanceof com.rd.veuisdk.callback.ISdkCallBack) {
                            ((com.rd.veuisdk.callback.ISdkCallBack) isdk)
                                    .onGetVideoTrimTime((Context) msg.obj,
                                            SdkEntry.TRIMVIDEO_DURATION_EXPORT,
                                            msg.arg1, msg.arg2);
                        }
                    }
                    break;
                case MSG_TRIM_COMMIT:
                    if (null != isdk) {
                        if (isdk instanceof com.rd.veuisdk.callback.ISdkCallBack) {
                            ((com.rd.veuisdk.callback.ISdkCallBack) isdk)
                                    .onGetVideoTrim((Context) msg.obj, msg.arg1);
                        }
                    }
                    break;
                case MSG_SELECT_IMAGE:
                    if (null != isdk) {
                        if (isdk instanceof com.rd.veuisdk.callback.ISdkCallBack) {
                            ((com.rd.veuisdk.callback.ISdkCallBack) isdk)
                                    .onGetPhoto((Context) msg.obj);
                        }
                    }
                    break;
                case MSG_SELECT_VIDEO:
                    if (null != isdk) {
                        if (isdk instanceof com.rd.veuisdk.callback.ISdkCallBack) {
                            ((com.rd.veuisdk.callback.ISdkCallBack) isdk)
                                    .onGetVideo((Context) msg.obj);
                        }
                    }
                    break;
                case MSG_TRIM_VIDEO_EXPORT:
                    if (null != isdk) {
                        IData idata = (IData) msg.obj;
                        if (isdk instanceof com.rd.veuisdk.callback.ISdkCallBack) {
                            ((com.rd.veuisdk.callback.ISdkCallBack) isdk)
                                    .onGetVideoPath(idata.context,
                                            SdkEntry.TRIMVIDEO_EXPORT, idata.path);
                        }
                    }
                    break;
                case MSG_TRIM_VIDEO_DURATION_EXPORT:
                    if (null != isdk) {
                        IData idata = (IData) msg.obj;
                        if (isdk instanceof com.rd.veuisdk.callback.ISdkCallBack) {
                            ((com.rd.veuisdk.callback.ISdkCallBack) isdk)
                                    .onGetVideoPath(idata.context,
                                            SdkEntry.TRIMVIDEO_DURATION_EXPORT,
                                            idata.path);
                        }
                    }
                    break;

                default:
                    break;
            }
            msg.obj = null;
        }

        ;
    };

}
