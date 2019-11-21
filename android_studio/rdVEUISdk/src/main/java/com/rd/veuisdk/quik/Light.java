package com.rd.veuisdk.quik;

import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;

import java.util.List;

class Light {


    /**
     * @param scene
     * @param asp
     */
    public static void loadAnimation(Scene scene, float asp) {
        List<MediaObject> allMedia = scene.getAllMedia();
        float[] itemDus = {1f, 6f, 3.7f};
        float lineStart = 0;
        for (int n = 0; n < allMedia.size(); n++) {
            MediaObject mediaObject = allMedia.get(n);
            float du = itemDus[n % itemDus.length];
            if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                //视频以视频的duration为准
                du = Math.min(du, mediaObject.getIntrinsicDuration());
            } else {
                mediaObject.setIntrinsicDuration(du);
            }

            float tLineTo = lineStart + du;
            mediaObject.setTimelineRange(lineStart, tLineTo);

            if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                mediaObject.setTimeRange(0, Math.min(mediaObject.getLineDuration(), mediaObject.getIntrinsicDuration()));
                if (mediaObject.getWidth() < mediaObject.getHeight()) {
                    QuikHandler.fixVerVideoFeather(mediaObject, asp);
                } else {
                    //静态播放
                    mediaObject.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
                }
            } else {
                mediaObject.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
            }


            lineStart = tLineTo;
        }
    }
}
