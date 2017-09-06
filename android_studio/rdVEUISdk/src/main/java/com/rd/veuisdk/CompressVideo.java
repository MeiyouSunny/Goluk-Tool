package com.rd.veuisdk;

import android.content.Context;
import android.text.TextUtils;

import com.rd.vecore.utils.ExportUtils;
import com.rd.veuisdk.callback.ICompressVideoCallback;
import com.rd.veuisdk.utils.PathUtils;

import java.io.File;

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
        String saveMp4FileName;
        iListener = listener;
        cancel = context.getString(R.string.compress_cancel);

        if (!TextUtils.isEmpty(SdkEntry.getSdkService()
                .getCompressConfig().savePath)) {
            File path = new File(SdkEntry.getSdkService()
                    .getCompressConfig().savePath);
            PathUtils.checkPath(path);
            saveMp4FileName = PathUtils.getTempFileNameForSdcard(
                    SdkEntry.getSdkService().getCompressConfig().savePath, "VIDEO", "mp4");
        } else {
            saveMp4FileName = PathUtils.getMp4FileNameForSdcard();
        }

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
