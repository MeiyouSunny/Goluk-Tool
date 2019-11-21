package com.rd.veuisdk.utils;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.CameraLocalAcvFilterAdapter;
import com.rd.veuisdk.adapter.CameraLocalAcvFilterAdapter.FilterItem;
import com.rd.veuisdk.fragment.FilterFragment;
import com.rd.veuisdk.quik.QuikHandler;
import com.rd.veuisdk.quik.QuikHandler.QuikTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 可替换的工具类，用来切换完整版和精简版
 */
public class ReplaceableUtils {

    /**
     * 加载本地有效的acv滤镜
     */
    public static void initCameraFilterAcv(ArrayList<FilterItem> list, Resources res, List<String> supportedColorEffects) {
        list.add(new CameraLocalAcvFilterAdapter.FilterItem(R.drawable.camera_effect_11, res
                .getString(R.string.camera_effect_11), supportedColorEffects
                .get(11)));
        list.add(new CameraLocalAcvFilterAdapter.FilterItem(R.drawable.camera_effect_12, res
                .getString(R.string.camera_effect_12), supportedColorEffects
                .get(12)));
        list.add(new FilterItem(R.drawable.camera_effect_13, res
                .getString(R.string.camera_effect_13), supportedColorEffects
                .get(13)));
        list.add(new FilterItem(R.drawable.camera_effect_14, res
                .getString(R.string.camera_effect_14), supportedColorEffects
                .get(14)));

        list.add(new FilterItem(R.drawable.camera_effect_15, res
                .getString(R.string.camera_effect_15), supportedColorEffects
                .get(15)));
        list.add(new FilterItem(R.drawable.camera_effect_16, res
                .getString(R.string.camera_effect_16), supportedColorEffects
                .get(16)));
        list.add(new FilterItem(R.drawable.camera_effect_17, res
                .getString(R.string.camera_effect_17), supportedColorEffects
                .get(17)));
        list.add(new FilterItem(R.drawable.camera_effect_18, res
                .getString(R.string.camera_effect_18), supportedColorEffects
                .get(18)));
        list.add(new FilterItem(R.drawable.camera_effect_19, res
                .getString(R.string.camera_effect_19), supportedColorEffects
                .get(19)));
        list.add(new FilterItem(R.drawable.camera_effect_20, res
                .getString(R.string.camera_effect_20), supportedColorEffects
                .get(20)));
        list.add(new FilterItem(R.drawable.camera_effect_21, res
                .getString(R.string.camera_effect_21), supportedColorEffects
                .get(21)));

        list.add(new FilterItem(R.drawable.camera_effect_22, res
                .getString(R.string.camera_effect_22), supportedColorEffects
                .get(22)));
        list.add(new FilterItem(R.drawable.camera_effect_23, res
                .getString(R.string.camera_effect_23), supportedColorEffects
                .get(23)));
        list.add(new FilterItem(R.drawable.camera_effect_24, res
                .getString(R.string.camera_effect_24), supportedColorEffects
                .get(24)));
        list.add(new FilterItem(R.drawable.camera_effect_25, res
                .getString(R.string.camera_effect_25), supportedColorEffects
                .get(25)));
        list.add(new FilterItem(R.drawable.camera_effect_26, res
                .getString(R.string.camera_effect_26), supportedColorEffects
                .get(26)));

        list.add(new FilterItem(R.drawable.camera_effect_27, res
                .getString(R.string.camera_effect_27), supportedColorEffects
                .get(27)));
        list.add(new FilterItem(R.drawable.camera_effect_28, res
                .getString(R.string.camera_effect_28), supportedColorEffects
                .get(28)));
        list.add(new FilterItem(R.drawable.camera_effect_29, res
                .getString(R.string.camera_effect_29), supportedColorEffects
                .get(29)));
        list.add(new FilterItem(R.drawable.camera_effect_30, res
                .getString(R.string.camera_effect_30), supportedColorEffects
                .get(30)));
        list.add(new FilterItem(R.drawable.camera_effect_31, res
                .getString(R.string.camera_effect_31), supportedColorEffects
                .get(31)));
        list.add(new FilterItem(R.drawable.camera_effect_32, res
                .getString(R.string.camera_effect_32), supportedColorEffects
                .get(32)));

        list.add(new FilterItem(R.drawable.camera_effect_33, res
                .getString(R.string.camera_effect_33), supportedColorEffects
                .get(33)));
        list.add(new FilterItem(R.drawable.camera_effect_34, res
                .getString(R.string.camera_effect_34), supportedColorEffects
                .get(34)));
        list.add(new FilterItem(R.drawable.camera_effect_35, res
                .getString(R.string.camera_effect_35), supportedColorEffects
                .get(35)));
        list.add(new FilterItem(R.drawable.camera_effect_36, res
                .getString(R.string.camera_effect_36), supportedColorEffects
                .get(36)));
        list.add(new FilterItem(R.drawable.camera_effect_37, res
                .getString(R.string.camera_effect_37), supportedColorEffects
                .get(37)));
        list.add(new FilterItem(R.drawable.camera_effect_38, res
                .getString(R.string.camera_effect_38), supportedColorEffects
                .get(38)));
    }


    /**
     * 加载quik 菜单
     */
    public static void loadQuik(List<QuikHandler.EffectInfo> mList, String ThePassColors, String Kalimba,
                                String BebeSiempreMe, String SleepAway, String DennyWhiteColors, String ThatBass, String Faraway, String PushimColors, String tantan, String dst) {
//        action,fick,light,jolly,这几个是覆盖的
        mList.add(new QuikHandler.EffectInfo(QuikTemplate.Grammy, R.drawable.qk_grammy, ThePassColors, "Grammy", true));
        mList.add(new QuikHandler.EffectInfo(QuikTemplate.Boxed, R.drawable.qk_boxed, Kalimba, "Boxed", true));
        mList.add(new QuikHandler.EffectInfo(QuikTemplate.Epic, R.drawable.qk_action, BebeSiempreMe, "Epic", true));
        mList.add(new QuikHandler.EffectInfo(QuikTemplate.Slice, R.drawable.qk_slice, SleepAway, "Slice", true));
        mList.add(new QuikHandler.EffectInfo(QuikTemplate.Sunny, R.drawable.qk_sunny, DennyWhiteColors, "Sunny", true));
        mList.add(new QuikHandler.EffectInfo(QuikTemplate.Radical, R.drawable.qk_action, ThatBass, "Raw", false));
        mList.add(new QuikHandler.EffectInfo(QuikTemplate.Seren, R.drawable.qk_serene, Faraway, "Serene", true));
        mList.add(new QuikHandler.EffectInfo(QuikTemplate.Flick, R.drawable.qk_flick, Kalimba, "Flick", false));
        mList.add(new QuikHandler.EffectInfo(QuikTemplate.Lapse, R.drawable.qk_lapse, PushimColors, "Lapse2", true));
        mList.add(new QuikHandler.EffectInfo(QuikTemplate.Jolly, R.drawable.qk_jolly, tantan, "Jolly", false));
        if (!TextUtils.isEmpty(dst)) {
            mList.add(new QuikHandler.EffectInfo(QuikTemplate.Light, R.drawable.qk_light, dst + "music.mp3", dst + "screen.mp4", "Light.lottie", false));
        }
    }


    /**
     * 编辑界面，acv滤镜
     */
    public static void initFilterAcv(Context context, ArrayList<ArrayList<FilterFragment.FliterItem>> mArrFliterBucket, int id) {

        /**
         * 哥特
         */
        ArrayList<FilterFragment.FliterItem> arrFliter2 = new ArrayList<>();
        arrFliter2.add(new FilterFragment.FliterItem(R.drawable.camera_effect_0,
                getString(context, R.string.filter_1_0), 0));
        arrFliter2.add(new FilterFragment.FliterItem(R.drawable.camera_effect_11,
                getString(context, R.string.filter_1_11), id++));
        arrFliter2.add(new FilterFragment.FliterItem(R.drawable.camera_effect_12,
                getString(context, R.string.filter_1_12), id++));
        arrFliter2.add(new FilterFragment.FliterItem(R.drawable.camera_effect_13,
                getString(context, R.string.filter_1_13), id++));
        arrFliter2.add(new FilterFragment.FliterItem(R.drawable.camera_effect_14,
                getString(context, R.string.filter_1_14), id++));
        mArrFliterBucket.add(arrFliter2);

        /**
         * lemo
         */
        ArrayList<FilterFragment.FliterItem> arrFliter3 = new ArrayList<>();
        arrFliter3.add(new FilterFragment.FliterItem(R.drawable.camera_effect_0,
                getString(context, R.string.filter_1_0), 0));
        arrFliter3.add(new FilterFragment.FliterItem(R.drawable.camera_effect_15,
                getString(context, R.string.filter_1_15), id++));
        arrFliter3.add(new FilterFragment.FliterItem(R.drawable.camera_effect_16,
                getString(context, R.string.filter_1_16), id++));
        arrFliter3.add(new FilterFragment.FliterItem(R.drawable.camera_effect_17,
                getString(context, R.string.filter_1_17), id++));
        arrFliter3.add(new FilterFragment.FliterItem(R.drawable.camera_effect_18,
                getString(context, R.string.filter_1_18), id++));
        arrFliter3.add(new FilterFragment.FliterItem(R.drawable.camera_effect_19,
                getString(context, R.string.filter_1_19), id++));
        arrFliter3.add(new FilterFragment.FliterItem(R.drawable.camera_effect_20,
                getString(context, R.string.filter_1_20), id++));
        arrFliter3.add(new FilterFragment.FliterItem(R.drawable.camera_effect_21,
                getString(context, R.string.filter_1_21), id++));
        mArrFliterBucket.add(arrFliter3);

        /**
         * 冷调
         */
        ArrayList<FilterFragment.FliterItem> arrFliter4 = new ArrayList<>();
        arrFliter4.add(new FilterFragment.FliterItem(R.drawable.camera_effect_0,
                getString(context, R.string.filter_1_0), 0));
        arrFliter4.add(new FilterFragment.FliterItem(R.drawable.camera_effect_22,
                getString(context, R.string.filter_1_22), id++));
        arrFliter4.add(new FilterFragment.FliterItem(R.drawable.camera_effect_23,
                getString(context, R.string.filter_1_23), id++));
        arrFliter4.add(new FilterFragment.FliterItem(R.drawable.camera_effect_24,
                getString(context, R.string.filter_1_24), id++));
        arrFliter4.add(new FilterFragment.FliterItem(R.drawable.camera_effect_25,
                getString(context, R.string.filter_1_25), id++));
        arrFliter4.add(new FilterFragment.FliterItem(R.drawable.camera_effect_26,
                getString(context, R.string.filter_1_26), id++));
        mArrFliterBucket.add(arrFliter4);

        /**
         * 薄暮
         */
        ArrayList<FilterFragment.FliterItem> arrFliter5 = new ArrayList<>();
        arrFliter5.add(new FilterFragment.FliterItem(R.drawable.camera_effect_0,
                getString(context, R.string.filter_1_0), 0));
        arrFliter5.add(new FilterFragment.FliterItem(R.drawable.camera_effect_27,
                getString(context, R.string.filter_1_27), id++));
        arrFliter5.add(new FilterFragment.FliterItem(R.drawable.camera_effect_28,
                getString(context, R.string.filter_1_28), id++));
        arrFliter5.add(new FilterFragment.FliterItem(R.drawable.camera_effect_29,
                getString(context, R.string.filter_1_29), id++));
        arrFliter5.add(new FilterFragment.FliterItem(R.drawable.camera_effect_30,
                getString(context, R.string.filter_1_30), id++));
        arrFliter5.add(new FilterFragment.FliterItem(R.drawable.camera_effect_31,
                getString(context, R.string.filter_1_31), id++));
        arrFliter5.add(new FilterFragment.FliterItem(R.drawable.camera_effect_32,
                getString(context, R.string.filter_1_32), id++));
        mArrFliterBucket.add(arrFliter5);

        /**
         * 夜色
         */
        ArrayList<FilterFragment.FliterItem> arrFliter6 = new ArrayList<>();
        arrFliter6.add(new FilterFragment.FliterItem(R.drawable.camera_effect_0,
                getString(context, R.string.filter_1_0), 0));
        arrFliter6.add(new FilterFragment.FliterItem(R.drawable.camera_effect_33,
                getString(context, R.string.filter_1_33), id++));
        arrFliter6.add(new FilterFragment.FliterItem(R.drawable.camera_effect_34,
                getString(context, R.string.filter_1_34), id++));
        arrFliter6.add(new FilterFragment.FliterItem(R.drawable.camera_effect_35,
                getString(context, R.string.filter_1_35), id++));
        arrFliter6.add(new FilterFragment.FliterItem(R.drawable.camera_effect_36,
                getString(context, R.string.filter_1_36), id++));
        arrFliter6.add(new FilterFragment.FliterItem(R.drawable.camera_effect_37,
                getString(context, R.string.filter_1_37), id++));
        mArrFliterBucket.add(arrFliter6);

        /**
         * 怀旧
         */
        ArrayList<FilterFragment.FliterItem> arrFliter7 = new ArrayList<>();
        arrFliter7.add(new FilterFragment.FliterItem(R.drawable.camera_effect_0,
                getString(context, R.string.filter_1_0), 0));
        arrFliter7.add(new FilterFragment.FliterItem(R.drawable.camera_effect_38,
                getString(context, R.string.filter_1_38), id++));
        mArrFliterBucket.add(arrFliter7);
    }

    private static String getString(Context context, @StringRes int strId) {
        return context.getString(strId);
    }
}
