package com.rd.veuisdk.ui;

import android.net.Uri;
import android.text.TextUtils;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.postprocessors.IterativeBoxBlurPostProcessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

public class SimpleDraweeViewUtils {


    /**
     * 设置视频封面
     *
     * @param draweeView
     * @param pathVideoConver
     */
    public static void setCover(SimpleDraweeView draweeView, String pathVideoConver) {
        setCover(draweeView, pathVideoConver, false, 150, 150);
    }


    /***
     *
     * @param view
     * @param drawable
     */
    public static void setCover(SimpleDraweeView view, int drawable) {
        Uri uri = Uri.parse("res://" +
                view.getContext().getPackageName() +
                "/" + drawable);
        setCover(view, uri);
    }

    /**
     *
     */
    public static void setCover(SimpleDraweeView view, Uri uri) {
        DraweeController controller = Fresco.newDraweeControllerBuilder().
                setUri(uri).
                build();
        view.setController(controller);
    }

    /**
     * 边框颜色
     */
    public static void setBorderColor(SimpleDraweeView view, int borderColor) {
        RoundingParams roundingParams = view.getHierarchy().getRoundingParams();
        roundingParams.setBorderColor(borderColor);
        view.getHierarchy().setRoundingParams(roundingParams);

    }

    /**
     * 设置视频封面
     *
     * @param draweeView
     * @param pathVideoConver
     * @param enableBlur      模糊
     */
    public static void setCover(SimpleDraweeView draweeView, String pathVideoConver, boolean enableBlur, int width, int height) {
        if (!TextUtils.isEmpty(pathVideoConver)) {
            ImageRequest requestVideoConver;
            ResizeOptions resizeOptions = null;
            if (!pathVideoConver.endsWith(".webp") && width > 0 && height > 0) {
                resizeOptions = new ResizeOptions(width, height);
            }
            if (enableBlur) {
                requestVideoConver = ImageRequestBuilder.newBuilderWithSource(Uri.parse(pathVideoConver))
                        .setRotationOptions(RotationOptions.autoRotate())
                        .setLocalThumbnailPreviewsEnabled(true)
                        .setImageDecodeOptions(ImageDecodeOptions.newBuilder().build())
                        .setPostprocessor(new IterativeBoxBlurPostProcessor(5, 5)) //TODO:高斯
                        .setResizeOptions(resizeOptions)
                        .build();
            } else {

                String tmp = pathVideoConver;
                if (pathVideoConver.startsWith("/")) {
                    //本地图片
                    tmp = "file://" + pathVideoConver;
                }
                requestVideoConver = ImageRequestBuilder.newBuilderWithSource(Uri.parse(tmp))
                        .setRotationOptions(RotationOptions.autoRotate())
                        .setLocalThumbnailPreviewsEnabled(true)
                        .setImageDecodeOptions(ImageDecodeOptions.newBuilder().build())
                        .setResizeOptions(resizeOptions)
                        .build();
            }

            DraweeController placeHolderDraweeControllerVideoConver = Fresco.newDraweeControllerBuilder()
                    .setOldController(draweeView.getController())
                    .setImageRequest(requestVideoConver)
                    .setAutoPlayAnimations(false)
                    .build();
            draweeView.setController(placeHolderDraweeControllerVideoConver);
        } else {
            draweeView.setImageURI((String) null);
        }
    }
}
