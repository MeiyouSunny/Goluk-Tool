package com.rd.veuisdk.quik;

import android.content.Context;
import android.graphics.RectF;

import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;

import java.util.List;

public class Jolly {
    /**
     * 播放器view的位置
     */
    private static RectF mPlayerRectF;
    private static final String TAG = "Jolly";

    static RectF fixShowRectF(RectF inView) {
        return QuikHandler.fixShowRectF(mPlayerRectF, inView);
    }

    private static String[] layerNameArr = null;

    public static String[] init(float asp) {
        String dir;
        if (asp == QuikHandler.ASP_1) {
            dir = "tantan/";
        } else if (asp == QuikHandler.ASP_169) {
            dir = "tantan16-9/";
        } else {
            dir = "tantan9-16/";
        }

        layerNameArr = new String[]{dir + "1.json"
                , dir + "2.json", dir + "3.json", dir + "4.json", dir + "5.json", dir + "6.json"
        };
        return layerNameArr;
    }

    public static void loadAnimation(Scene scene, Context context, float asp) {
        init(asp);
        List<MediaObject> allMedia = scene.getAllMedia();
        for (int n = 0; n < allMedia.size(); n++) {
            MediaObject mediaObject = allMedia.get(n);
            mediaObject.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
        }
    }


}
