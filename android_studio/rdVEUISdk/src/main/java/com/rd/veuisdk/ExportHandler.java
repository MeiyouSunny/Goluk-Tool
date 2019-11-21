package com.rd.veuisdk;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import android.text.TextUtils;

import com.rd.vecore.VirtualVideo;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.models.Watermark;
import com.rd.veuisdk.manager.ExportConfiguration;
import com.rd.veuisdk.manager.TextWatermarkBuilder;
import com.rd.veuisdk.ui.HorizontalProgressDialog;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.io.File;

/**
 * 导出-返回到之前的界面
 */
public class ExportHandler {
    private VirtualVideo exportVideo;
    private IExport mExport;
    private String mStrSaveMp4FileName;
    private Activity mActivity;
    private String TAG = "ExportHandler";
    private boolean withWatermark = true;
    private ExportConfiguration mExportConfig;
    private float mSrcAsp = 1f;
    private String mStrCustomWatermarkTempPath = null;

    public ExportHandler(@NonNull Activity activity, @NonNull IExport export) {
        mActivity = activity;
        mExport = export;
    }


    /**
     * 导出
     *
     * @param asp           输出比例
     * @param withWatermark 是否加水印，true 加水印；false 屏蔽水印
     */
    public void onExport(float asp, boolean withWatermark) {
        onExport(asp, withWatermark, Color.BLACK);
    }


    /**
     * 导出
     *
     * @param asp             输出比例
     * @param withWatermark   是否加水印，true 加水印；false 屏蔽水印
     * @param backgroundColor 背景色
     */
    public void onExport(float asp, boolean withWatermark, int backgroundColor) {

        VideoConfig videoConfig = getExportConfig(asp);
        //背景色
        videoConfig.setBackgroundColor(backgroundColor);
        onExport(withWatermark, videoConfig);
    }

    /**
     * 构建初始化时配置的输出参数
     */
    public static VideoConfig getExportConfig(float asp) {
        ExportConfiguration config = SdkEntry.getSdkService().getExportConfig();

        VideoConfig videoConfig = new VideoConfig();
        //输出大小
        videoConfig.setAspectRatio(config.getVideoMaxWH(), asp);
        //码率
        videoConfig.setVideoEncodingBitRate(config.getVideoBitratebps());
        //帧率
        videoConfig.setVideoFrameRate(config.exportVideoFrameRate);
        return videoConfig;
    }


    /**
     * @param withWatermark
     * @param videoConfig   导出参数
     */
    public void onExport(boolean withWatermark, VideoConfig videoConfig) {
        this.withWatermark = withWatermark;
        mSrcAsp = videoConfig.getAspectRatio();
        exportVideo = new VirtualVideo();
        mExport.addData(exportVideo);
        mExportConfig = SdkEntry.getSdkService().getExportConfig();
        addWatermark(exportVideo);
        mStrSaveMp4FileName = PathUtils.getDstFilePath(mExportConfig.saveDir);
        exportVideo.export(mActivity, mStrSaveMp4FileName, videoConfig, mExportListener);

    }

    /**
     * 水印
     */
    private void addWatermark(VirtualVideo virtualVideo) {
        if (withWatermark) {
            if (mExportConfig.enableTextWatermark) {  // 自定义view水印
                mStrCustomWatermarkTempPath = PathUtils.getTempFileNameForSdcard(mExportConfig.saveDir, "png");
                TextWatermarkBuilder textWatermarkBuilder = new TextWatermarkBuilder(mActivity, mStrCustomWatermarkTempPath);
                textWatermarkBuilder.setWatermarkContent(mExportConfig.textWatermarkContent);
                textWatermarkBuilder.setTextSize(mExportConfig.textWatermarkSize);
                textWatermarkBuilder.setTextColor(mExportConfig.textWatermarkColor);
                textWatermarkBuilder.setShowRect(mExportConfig.watermarkShowRectF);
                textWatermarkBuilder.setTextShadowColor(mExportConfig.textWatermarkShadowColor);
                virtualVideo.setWatermark(textWatermarkBuilder);
            } else if (com.rd.veuisdk.utils.FileUtils.isExist(mActivity, mExportConfig.watermarkPath)) {  //图片水印
                Watermark watermark = new Watermark(mExportConfig.watermarkPath);
                if (mExportConfig.watermarkShowRectF != null) {
                    watermark.setShowRect(mExportConfig.watermarkShowRectF);
                    watermark.setUseLayoutRect(false);
                }
                if (mSrcAsp > 1) {
                    //横屏使用横屏水印
                    if (mExportConfig.watermarkLandLayoutRectF != null) {
                        watermark.setShowRect(mExportConfig.watermarkLandLayoutRectF);
                        watermark.setUseLayoutRect(true);
                    } else {
                        if (mExportConfig.watermarkPortLayoutRectF != null) {
                            watermark.setShowRect(mExportConfig.watermarkPortLayoutRectF);
                            watermark.setUseLayoutRect(true);
                        }
                    }
                } else {
                    if (mExportConfig.watermarkPortLayoutRectF != null) {
                        watermark.setShowRect(mExportConfig.watermarkPortLayoutRectF);
                        watermark.setUseLayoutRect(true);
                    } else {
                        if (mExportConfig.watermarkLandLayoutRectF != null) {
                            watermark.setShowRect(mExportConfig.watermarkLandLayoutRectF);
                            watermark.setUseLayoutRect(true);
                        }
                    }
                }
                watermark.setShowMode(mExportConfig.watermarkShowMode);
                virtualVideo.setWatermark(watermark);
            }
        }
    }


    private ExportListener mExportListener = new ExportListener() {
        private HorizontalProgressDialog epdExport = null;
        private Dialog dialog = null;
        private boolean cancelExport = false;

        @Override
        public boolean onExporting(int nProgress, int nMax) {
            if (null != epdExport) {
                epdExport.setProgress(nProgress);
                epdExport.setMax(nMax);
            }
            if (cancelExport) {
                return false;
            }
            return true;
        }

        @Override
        public void onExportStart() {
            cancelExport = false;
            if (epdExport == null) {
                epdExport = SysAlertDialog.showHoriProgressDialog(
                        mActivity, getString(R.string.exporting),
                        false, true, new DialogInterface.OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                cancelExport = true;

                            }
                        });
                epdExport.setCanceledOnTouchOutside(false);
                epdExport.setOnCancelClickListener(new HorizontalProgressDialog.onCancelClickListener() {

                    @Override
                    public void onCancel() {
                        dialog = SysAlertDialog.showAlertDialog(
                                mActivity, "",
                                getString(R.string.cancel_export),
                                getString(R.string.no),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {

                                    }

                                }, getString(R.string.yes),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        if (epdExport != null) {
                                            epdExport.cancel();
                                        }
                                    }
                                });
                    }
                });
            }
            mActivity.getWindow()
                    .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        @Override
        public void onExportEnd(int nResult) {

            if (null != exportVideo) {
                exportVideo.release();
                exportVideo = null;
            }
            mActivity.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            if (!mActivity.isFinishing()) {
                if (epdExport != null) {
                    epdExport.dismiss();
                    epdExport = null;
                }
                if (dialog != null) {
                    dialog.dismiss();
                    dialog.cancel();
                    dialog = null;
                }
            }

            // 删除自定义水印临时文件
            if (!TextUtils.isEmpty(mStrCustomWatermarkTempPath)) {
                try {
                    new File(mStrCustomWatermarkTempPath).delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mStrCustomWatermarkTempPath = null;
            }

            if (nResult >= VirtualVideo.RESULT_SUCCESS) {
                gotoNext(mActivity, mStrSaveMp4FileName);
            } else {
                new File(mStrSaveMp4FileName).delete();
                if (nResult != VirtualVideo.RESULT_EXPORT_CANCEL) {
                    if (nResult == VirtualVideo.RESULT_APPVERIFY_ERROR) {
                        String strMessage = getString(R.string.export_failed);
                        SysAlertDialog.showAutoHideDialog(mActivity, "", strMessage, Toast.LENGTH_SHORT);
                        strMessage = getString(R.string.export_failed_by_appverify);
                        Log.e(TAG, "onExportEnd:" + strMessage + ",result:" + nResult);
                    } else {
                        String strMessage = getString(R.string.export_failed);
                        if (nResult == VirtualVideo.RESULT_CORE_ERROR_LOW_DISK) {
                            strMessage = getString(R.string.export_failed_no_free_space);
                        }
                        SysAlertDialog.showAutoHideDialog(mActivity, "", strMessage, Toast.LENGTH_SHORT);
                        Log.e(TAG, strMessage + ",result:" + nResult);
                    }
                } else {
                    if (mExport instanceof ExportCallBack) {
                        ((ExportCallBack) mExport).onCancel();
                    }
                }
            }
        }
    };

    private String getString(@StringRes int resId) {
        return mActivity.getString(resId);
    }

    /**
     * 返回数据
     */
    private void gotoNext(Activity activity, String outpath) {
        SdkEntryHandler.getInstance().onExport(activity, outpath);
        Intent intent = new Intent();
        intent.putExtra(SdkEntry.EDIT_RESULT, outpath);
        mActivity.setResult(Activity.RESULT_OK, intent);
        mActivity.finish();
    }

    /**
     * 导出回调 (用来添加资源)
     */
    public static interface IExport {

        /**
         * 加载媒体
         */
        void addData(VirtualVideo virtualVideo);

    }

    /**
     * 导出回调
     */
    public static interface ExportCallBack extends IExport {

        /**
         * 取消导出
         */
        void onCancel();

    }
}
