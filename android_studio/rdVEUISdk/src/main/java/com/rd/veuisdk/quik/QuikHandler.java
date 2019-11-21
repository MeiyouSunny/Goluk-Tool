package com.rd.veuisdk.quik;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;

import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.models.AnimationGroup;
import com.rd.vecore.models.AnimationObject;
import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.PermutationMode;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.AETextMediaInfo;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.ReplaceableUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class QuikHandler {

    public static final float ASP_1 = 1f;
    public static final float ASP_916 = 544 / 960.0f;
    public static final float ASP_169 = 544 / 306.0f;
    static final String TAG = "QuikHandler";

    public static class EffectInfo {
        public EffectInfo(QuikTemplate quikTemplate, int icon, String musicPath, String aeZipName, boolean isPiantou) {
            mQuikTemplate = quikTemplate;
            this.icon = icon;
            this.musicPath = musicPath;
            this.aeZipName = aeZipName;
            this.isPiantou = isPiantou;

        }

        public EffectInfo(QuikTemplate quikTemplate, int icon, String musicPath, String blendPath, String aeZipName, boolean isPiantou) {
            this(quikTemplate, icon, musicPath, aeZipName, isPiantou);
            this.blendPath = blendPath;
        }

        public QuikTemplate mQuikTemplate;
        public int icon;
        public String musicPath;
        public String blendPath;
        public String aeZipName;

        @Override
        public String toString() {
            return "EffectInfo{" +
                    "mQuikTemplate=" + mQuikTemplate +
                    ", icon=" + icon +
                    ", musicPath='" + musicPath + '\'' +
                    ", blendPath='" + blendPath + '\'' +
                    ", aeZipName='" + aeZipName + '\'' +
                    ", mAETextMediaList=" + mAETextMediaList +
                    ", isPiantou=" + isPiantou +
                    '}';
        }


        public List<AETextMediaInfo> getAETextMediaList() {
            return mAETextMediaList;
        }

        public void setAETextMediaList(List<AETextMediaInfo> AETextMediaList) {
            mAETextMediaList = AETextMediaList;


        }

        private List<AETextMediaInfo> mAETextMediaList;


        public boolean isPiantou() {
            return isPiantou;
        }


        private boolean isPiantou = false;
    }

    public List<EffectInfo> getList() {
        return mList;
    }

    private List<EffectInfo> mList;


    /**
     * @param effectInfo
     */
    public void updateItem(QuikHandler.EffectInfo effectInfo) {
        if (null != effectInfo && mList != null) {
            int index = -1;
            int len = mList.size();
            for (int i = 0; i < len; i++) {
                if (effectInfo.mQuikTemplate != null && mList.get(i).mQuikTemplate == effectInfo.mQuikTemplate) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                mList.set(index, effectInfo);
            }
        }

    }


    /**
     * @param effectInfo
     * @param asp
     * @return
     */
    public String initAEDir(EffectInfo effectInfo, float asp) {
        if (null != effectInfo) {
            String dir = getTagetDir(effectInfo) + "/";
            if (!TextUtils.isEmpty(dir)) {
                if (asp == QuikHandler.ASP_1) {
                    dir += "1-1/";
                } else if (asp == QuikHandler.ASP_169) {
                    dir += "16-9/";
                } else {
                    dir += "9-16/";
                }
                return dir;
            }
        }
        return null;

    }

    private String getTagetDir(EffectInfo effectInfo) {
        return PathUtils.getRdAssetPath() + "/" + effectInfo.aeZipName;
    }


    public void init(Context context) {
        mList = new ArrayList<>();
        AssetManager assetManager = context.getAssets();
        String unicornSongPath = PathUtils.getAssetFileNameForSdcard("Agnes-The Unicorn Song", ".mp3");
        if (!FileUtils.isExist(unicornSongPath)) {
            CoreUtils.assetRes2File(assetManager, "quik/Agnes-The Unicorn Song.mp3", unicornSongPath);
        }
        String DennyWhiteColors = PathUtils.getAssetFileNameForSdcard("Denny White - Colors", ".mp3");
        if (!FileUtils.isExist(DennyWhiteColors)) {
            CoreUtils.assetRes2File(assetManager, "quik/Denny White - Colors.mp3", DennyWhiteColors);
        }
        String Kalimba = PathUtils.getAssetFileNameForSdcard("Kalimba", ".mp3");
        if (!FileUtils.isExist(Kalimba)) {
            CoreUtils.assetRes2File(assetManager, "quik/Kalimba.mp3", Kalimba);
        }
        String LalehColors = PathUtils.getAssetFileNameForSdcard("Laleh - Colors", ".mp3");
        if (!FileUtils.isExist(LalehColors)) {
            CoreUtils.assetRes2File(assetManager, "quik/Laleh - Colors.mp3", LalehColors);
        }
        String PushimColors = PathUtils.getAssetFileNameForSdcard("Pushim - Colors", ".mp3");
        if (!FileUtils.isExist(PushimColors)) {
            CoreUtils.assetRes2File(assetManager, "quik/Pushim - Colors.mp3", PushimColors);
        }

        String QuintorigoGrigio = PathUtils.getAssetFileNameForSdcard("Quintorigo-Grigio", ".mp3");
        if (!FileUtils.isExist(QuintorigoGrigio)) {
            CoreUtils.assetRes2File(assetManager, "quik/Quintorigo-Grigio.mp3", QuintorigoGrigio);
        }

        String SleepAway = PathUtils.getAssetFileNameForSdcard("Sleep Away", ".mp3");
        if (!FileUtils.isExist(SleepAway)) {
            CoreUtils.assetRes2File(assetManager, "quik/Sleep Away.mp3", SleepAway);
        }
        String LikeABullet = PathUtils.getAssetFileNameForSdcard("Stefanie Heinzmann - Like A Bullet", ".mp3");
        if (!FileUtils.isExist(LikeABullet)) {
            CoreUtils.assetRes2File(assetManager, "quik/Stefanie Heinzmann - Like A Bullet.mp3", LikeABullet);
        }
        String ThePassColors = PathUtils.getAssetFileNameForSdcard("The Pass - Colors", ".mp3");
        if (!FileUtils.isExist(ThePassColors)) {
            CoreUtils.assetRes2File(assetManager, "quik/The Pass - Colors.mp3", ThePassColors);
        }

        String BebeSiempreMe = PathUtils.getAssetFileNameForSdcard("Bebe-Siempre Me Quedará", ".mp3");
        if (!FileUtils.isExist(BebeSiempreMe)) {
            CoreUtils.assetRes2File(assetManager, "quik/Bebe-Siempre Me Quedará.mp3", BebeSiempreMe);
        }
        String ThatBass = PathUtils.getAssetFileNameForSdcard("Glee Cast-All About That Bass", ".mp3");
        if (!FileUtils.isExist(ThatBass)) {
            CoreUtils.assetRes2File(assetManager, "quik/Glee Cast-All About That Bass.mp3", ThatBass);
        }
        String Faraway = PathUtils.getAssetFileNameForSdcard("Gala[欧美]-Faraway", ".mp3");
        if (!FileUtils.isExist(Faraway)) {
            CoreUtils.assetRes2File(assetManager, "quik/Gala[欧美]-Faraway.mp3", Faraway);
        }
        String tantan = PathUtils.getAssetFileNameForSdcard("tantan", ".mp3");
        if (!FileUtils.isExist(tantan)) {
            CoreUtils.assetRes2File(assetManager, "quik/tantan.mp3", tantan);
        }

        String[] arr = new String[]{"Action.zip", "Grammy.zip", "Boxed.zip", "Epic.zip", "Slice.zip", "Sunny.zip", "Raw.zip", "Serene.zip", "Flick.zip", "Lapse2.zip", "Jolly.zip", "Light.lottie.zip"};
        int len = arr.length;
        for (int i = 0; i < len; i++) {
            String name = arr[i];
            if (!TextUtils.isEmpty(name)) {
                String dst = PathUtils.getRdAssetPath() + "/" + name;
                if (!com.rd.lib.utils.FileUtils.isExist(dst)) {
                    CoreUtils.assetRes2File(assetManager, "quik/" + name, dst);
                }
                try {
                    String tmp = FileUtils.unzip(dst, PathUtils.getRdAssetPath());
//                    Log.e(TAG, "init: " + dst + ">>>" + tmp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        String light = new File(PathUtils.getRdAssetPath(), "light.zip").getAbsolutePath();
        if (!FileUtils.isExist(light)) {
            CoreUtils.assetRes2File(assetManager, "quik/light.zip", light);
        }
        String dst = null;
        try {
            File file = new File(light);
            dst = FileUtils.unzip(light, file.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }


//        action,fick,light,jolly,这几个是覆盖的
        mList.add(new EffectInfo(QuikTemplate.Action, R.drawable.qk_action, LikeABullet, "Action", true));
        ReplaceableUtils.loadQuik(mList, ThePassColors, Kalimba, BebeSiempreMe, SleepAway, DennyWhiteColors, ThatBass, Faraway, PushimColors, tantan, dst);
    }


    public static enum QuikTemplate {
        NONE, Action, Grammy, Boxed, Epic, Slice, Sunny, Radical, Seren, Flick, Jolly, Light, Lapse
    }


    /**
     * @param scene
     * @param virtualVideo
     * @param template
     * @param asp
     * @param context
     * @return 返回动画总的运行时长 单位：秒
     */
    public void setQuikTemplate(Scene scene, VirtualVideo virtualVideo, QuikTemplate template, float asp, Context context) {

        if (template.equals(QuikTemplate.Action)) {
            scene.setPermutationMode(PermutationMode.COMBINATION_MODE);
            Action.loadAnimation(scene, asp);
        } else if (template.equals(QuikTemplate.Lapse)) {
            scene.setPermutationMode(PermutationMode.COMBINATION_MODE);
            Lapse.loadAnimation(scene, asp);
        } else if (template.equals(QuikTemplate.Grammy)) {
            scene.setPermutationMode(PermutationMode.LINEAR_MODE);
            Grammy.loadAnimation(scene, virtualVideo, asp);
        } else if (template.equals(QuikTemplate.Boxed)) {
            scene.setPermutationMode(PermutationMode.COMBINATION_MODE);
            Boxed.loadAnimation(scene, asp);
        } else if (template.equals(QuikTemplate.Epic)) {
            scene.setPermutationMode(PermutationMode.COMBINATION_MODE);
            Epic.loadAnimation(scene, asp);
        } else if (template.equals(QuikTemplate.Slice)) {
            scene.setPermutationMode(PermutationMode.COMBINATION_MODE);
            Slice.loadAnimation(scene, asp);
        } else if (template.equals(QuikTemplate.Sunny)) {
            scene.setPermutationMode(PermutationMode.COMBINATION_MODE);
            Sunny.loadAnimation(scene, asp);
        } else if (template.equals(QuikTemplate.Radical)) {
            scene.setPermutationMode(PermutationMode.COMBINATION_MODE);
            Radical.loadAnimation(scene, asp);
        } else if (template.equals(QuikTemplate.Seren)) {
            scene.setPermutationMode(PermutationMode.COMBINATION_MODE);
            Seren.loadAnimation(scene, asp);
        } else if (template.equals(QuikTemplate.Jolly)) {
            scene.setPermutationMode(PermutationMode.LINEAR_MODE);
            Jolly.loadAnimation(scene, context, asp);
        } else if (template.equals(QuikTemplate.Flick)) {
            scene.setPermutationMode(PermutationMode.COMBINATION_MODE);
            Flick.loadAnimation(scene, asp);
        } else if (template.equals(QuikTemplate.Light)) {
            scene.setPermutationMode(PermutationMode.LINEAR_MODE);
            Light.loadAnimation(scene, asp);
        } else {
            scene.setPermutationMode(PermutationMode.LINEAR_MODE);
        }


        if (scene.getPermutationMode() == PermutationMode.COMBINATION_MODE) {
            //修正组合排列的比例
            scene.setDisAspectRatio(asp);
        }
    }

    /**
     * 放大缩小
     *
     * @param showRectF 原始显示位置
     * @param scale     缩放比例
     * @return
     */
    static RectF createRect(RectF showRectF, float scale) {
        return MiscUtils.zoomRectF(showRectF, scale, scale);

    }

    /**
     * 因为所有区域都是基于正方形设定的，需要根据宽高动态调整
     *
     * @param showRectF
     * @param scaleX
     * @param scaleY
     * @return
     */
    static RectF createRect(RectF showRectF, float scaleX, float scaleY) {
        if (scaleX >= 0 || scaleY >= 0) {
            Matrix temp = new Matrix();
            temp.postScale(scaleX, scaleY, showRectF.centerX(), showRectF.centerY());
            RectF dst = new RectF();
            temp.mapRect(dst, showRectF);
            temp = null;
            return dst;
        } else {
            return new RectF(showRectF);
        }

    }

    /**
     * 真实的像素
     *
     * @param showRectF
     * @param scale
     * @return
     */
    static Rect createRect(Rect showRectF, float scale) {
        if (scale >= 0) {
            Matrix temp = new Matrix();
            temp.postScale(scale, scale, showRectF.centerX(), showRectF.centerY());
            RectF dst = new RectF();
            temp.mapRect(dst, new RectF(showRectF));
            temp = null;
            return new Rect((int) dst.left, (int) dst.top, (int) dst.right, (int) dst.bottom);
        } else {
            return new Rect(showRectF);
        }

    }

    /**
     * @param mPlayerRectF
     * @param inView
     * @return
     */
    static RectF fixShowRectF(RectF mPlayerRectF, RectF inView) {
        RectF dst = new RectF();
        float fw = mPlayerRectF.width(), fh = mPlayerRectF.height();
        dst.set(mPlayerRectF.left + (fw * inView.left),
                mPlayerRectF.top + (fh * inView.top),
                mPlayerRectF.left + (fw * inView.right),
                mPlayerRectF.top + (fh * inView.bottom));
        return dst;

    }

    /**
     * 播放器显示位置
     *
     * @param asp
     * @return
     */
    static RectF getShowRectF(float asp) {

        if (asp == 1) {
            return new RectF(0, 0, 1, 1);
        } else if (asp == 16 / 9.0f) {
            //胶片
            float mtop = (16.0f - 9.0f) / 2 / 16;
            return new RectF(0, mtop, 1, 1 - mtop);

        } else {
            //纵向
            float mleft = (16.0f - 9.0f) / 2 / 16;
            return new RectF(mleft, 0, 1 - mleft, 1);

        }
    }

    /**
     * 处理竖屏视频两边羽化
     *
     * @param tmp
     * @param asp
     */
    public static void fixVerVideoFeather(MediaObject tmp, float asp) {
        //竖屏显示时不做羽化
        if (tmp.getMediaType() == MediaType.MEDIA_VIDEO_TYPE && tmp.getWidth() < tmp.getHeight()) {
            if (asp != QuikHandler.ASP_916) {
                Rect rectF = new Rect();
                float tAsp = QuikHandler.ASP_1;
                if (asp == QuikHandler.ASP_169) {
                    tAsp = QuikHandler.ASP_169;
                }
                MiscUtils.fixClipRect(tAsp, tmp.getWidth(), tmp.getHeight(), rectF);
                tmp.setClipRectF(new RectF(rectF));
//                Log.e(TAG, "fixVerVideoFeather: " + rectF);
                tmp.setBlendEnabled(true);
                tmp.setBackgroundFilterType(VisualFilterConfig.FILTER_ID_GAUSSIAN_BLUR, 0.1f);
                try {
                    tmp.changeFilter(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_NORMAL).setFeatherX(0.1f));
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            } else {
                tmp.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);

            }
        }
    }

    /**
     * 最后一个媒体，逐渐变黑
     *
     * @param mediaObject
     * @param mPlayerRectF
     * @param du
     */
    public static void exit(MediaObject mediaObject, RectF mPlayerRectF, float du) {
        List<AnimationObject> listAnimation = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(0);
        animationObject.setRectPosition(new RectF(0, 0, 1, 1));
        int srcW = mediaObject.getWidth();
        int srcH = mediaObject.getHeight();
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(mPlayerRectF, animationObject.getRectPosition()), srcW, srcH, clip);
        animationObject.setClipRect(clip);
        animationObject.setAlpha(1f);
        listAnimation.add(animationObject);


        animationObject = new AnimationObject(du);
        animationObject.setRectPosition(new RectF(0, 0, 1, 1));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(mPlayerRectF, animationObject.getRectPosition()), srcW, srcH, clip);
        animationObject.setClipRect(clip);
        animationObject.setAlpha(0.1f);
        listAnimation.add(animationObject);
        mediaObject.addAnimationGroup(new AnimationGroup(listAnimation));

    }

    /**
     * @param mTimelineFrom
     * @param mTimelineTo
     * @return
     */
    public static float getLineDuration(float mTimelineFrom, float mTimelineTo) {
        float lineDu = mTimelineTo - mTimelineFrom;
        BigDecimal b = new BigDecimal(lineDu);
        //   b.setScale(2,  BigDecimal.ROUND_HALF_UP)  表明四舍五入，保留两位小数
        return b.setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
    }
}
