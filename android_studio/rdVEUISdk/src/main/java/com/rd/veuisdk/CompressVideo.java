package com.rd.veuisdk;

import android.content.Context;

import com.rd.vecore.utils.ExportUtils;
import com.rd.veuisdk.callback.ICompressVideoCallback;
import com.rd.veuisdk.utils.PathUtils;

/**
 * RDVEUISdk压缩视频工具
 */
public class CompressVideo {

    private static ICompressVideoCallback iListener;
    private static String cancel = "";

    public static void compressVideo(final Context context,
                                     final String mediaPath, final ICompressVideoCallback listener) {
        ExportUtils.CompressConfig compressConfig = SdkEntry.getSdkService()
                .getCompressConfig().toCompressConfig();

        iListener = listener;
        cancel = context.getString(R.string.compress_cancel);

        String saveMp4FileName = PathUtils.getDstFilePath(SdkEntry.getSdkService().getCompressConfig().savePath);

        ExportUtils.compressVideo(context, mediaPath, saveMp4FileName, compressConfig, new ExportUtils.CompressVideoListener() {
            @Override
            public void onCompressStart() {
                listener.onCompressStart();
            }

            @Override
            public void onProgress(int progress, int max) {
                listener.onProgress(progress, max);
            }

            @Override
            public void onCompressComplete(String path) {
                listener.onCompressComplete(path);
            }

            @Override
            public void onCompressError(int result) {
                if (result == OPEN_VIDEO_FAILED) {
                    listener.onCompressError(context
                            .getString(R.string.compress_add_error));
                } else {
                    listener.onCompressError(context
                            .getString(R.string.compress_error) + ",error no:" + result);
                }
            }
        });
    }


    /**
     * 取消压缩
     */
    public static void cancelCompress() {
        ExportUtils.cancelCompress();
        if (iListener != null) {
            iListener.onCompressError(cancel);
        }
    }
}
