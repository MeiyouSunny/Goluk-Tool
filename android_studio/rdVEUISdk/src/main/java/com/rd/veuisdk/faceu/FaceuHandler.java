package com.rd.veuisdk.faceu;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.faceunity.wrapper.faceunity;
import com.rd.recorder.api.IRecorderPreivewCallBack;
import com.rd.recorder.api.IRecorderTextureCallBack;
import com.rd.recorder.api.RecorderCore;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.SdkEntry;
import com.rd.veuisdk.adapter.FaceuAdapter;
import com.rd.veuisdk.manager.FaceuConfig;
import com.rd.veuisdk.manager.FaceuInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * FaceUnity辅助
 *
 * @author JIAN
 * @date 2017-3-14 下午4:07:52
 */
public class FaceuHandler implements IRecorderTextureCallBack,
        IRecorderPreivewCallBack {
    // 句柄索引
    private static final int ITEM_ARRAYS_FACE_BEAUTY_INDEX = 0;
    private static final int ITEM_ARRAYS_EFFECT_INDEX = 1;
    private static final int ITEM_ARRAYS_LIGHT_MAKEUP_INDEX = 2;
    private static final int ITEM_ARRAYS_EFFECT_ABIMOJI_3D_INDEX = 3;
    private static final int ITEM_ARRAYS_EFFECT_HAIR_NORMAL_INDEX = 4;
    private static final int ITEM_ARRAYS_EFFECT_HAIR_GRADIENT_INDEX = 5;
    private static final int ITEM_ARRAYS_CHANGE_FACE_INDEX = 6;
    private static final int ITEM_ARRAYS_FUZZYTOON_FILTER_INDEX = 7;
    private static final int ITEM_ARRAYS_LIVE_PHOTO_INDEX = 8;
    private static final int ITEM_ARRAYS_FACE_MAKEUP_INDEX = 9;
    private static final int ITEM_ARRAYS_AVATAR_BACKGROUND = 10;
    private static final int ITEM_ARRAYS_AVATAR_HAIR = 11;
    private static final int ITEM_ARRAYS_NEW_FACE_TRACKER = 12;

    // 句柄数量
    private static final int ITEM_ARRAYS_COUNT = 13;

    private static final String BUNDLE_V3 = "faceu/v3.bundle";
    private static final String BUNDLE_FACE_BEAUTIFICATION = "faceu/face_beautification.bundle";
    private static final String ASSETS = "assets:///";
//    private static final String FACEU_ITEM_BUNDLE = ASSETS + "faceu/BeagleDog.mp3";


    private byte[] mCameraData;
    private String mCreatedItemId = "";
    private String mCurItemId = "";

    private boolean mFuNotifyPause;
    private int mFrameId;

    private FaceuUIHandler faceHandler;
    private final String TAG = "FaceuHandler";
    private byte[] pack;
    private FaceuConfig config;


    //美颜和其他道具的handle数组
    private final int[] itemsArray = new int[ITEM_ARRAYS_COUNT];

    private AssetManager mAssetManager;

    public boolean isCurrentIsVer() {
        return faceHandler.isCurrentIsVer();
    }


    /**
     * @param _context
     * @param filterGroup
     * @param filter_menu_parent
     * @param _pack
     * @param faceConfig
     * @param fuLayout
     * @param fuLayoutParent
     * @param filter_parent_layout
     * @param listener
     */
    public FaceuHandler(Context _context, RadioGroup filterGroup,
                        View filter_menu_parent, View rgMenuParent,
                        byte[] _pack, FaceuConfig faceConfig,
                        LinearLayout fuLayout, LinearLayout fuLayoutParent, LinearLayout filter_parent_layout, IReloadListener listener) {
        //重置数组
        int len = itemsArray.length;
        for (int i = 0; i < len; i++) {
            itemsArray[i] = 0;
        }
        mAssetManager = _context.getAssets();
        config = faceConfig;
        pack = _pack;
        faceHandler = new FaceuUIHandler(filterGroup,
                filter_menu_parent, rgMenuParent, (null != pack && pack.length > 0), config,
                new FaceuListener() {
                    @Override
                    public void onFUChanged(String filePath, int lastPosition) {
                        mCurItemId = filePath;
//                        mCurItemId = FACEU_ITEM_BUNDLE;
                    }
                }, fuLayout, fuLayoutParent, filter_parent_layout, listener);

//        mCurItemId = FACEU_ITEM_BUNDLE;
    }

    private boolean inited = false;
    //初始化faceU成功,防止没有v3.mp3
    private boolean bInitedSuccessed = false;

    public void onInitFaceunity() {
        if (isSupportFace()) {
            inited = false;
            mFuNotifyPause = true;
            mFrameId = 0;
            mCameraData = null;
            bInitedSuccessed = false;
            byte[] data = readAssetFaceu(BUNDLE_V3);
            if (null != data) {
                faceunity.fuSetup(data, null, pack);
                faceunity.fuSetMaxFaces(1);
            }
            itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] = 0;
            itemsArray[ITEM_ARRAYS_EFFECT_INDEX] = 0;
            bInitedSuccessed = true;
        }
        inited = true;
    }


    private int previewW, previewH;

    @Override
    public void onPreviewFrame(byte[] nv21data, int width, int height) {
//        Log.e(TAG, "onPreviewFrame" + ((null != nv21data) ? nv21data.length : "null")
//                + "..xxxxxxxxxxxx." + width + "*" + height);
        previewW = width;
        previewH = height;
        mCameraData = nv21data;
    }

    private Handler mhandler = new Handler();

    private Runnable readItem = new Runnable() {

        @Override
        public void run() {
            if (!TextUtils.isEmpty(mCurItemId) && !TextUtils.equals(mCurItemId, FaceuAdapter.NONE)) {
                if (mCurItemId.startsWith(ASSETS)) {
                    //内置资源
                    byte[] data = readAssetFaceu(mCurItemId.replace(ASSETS, ""));
                    if (null != data) {
                        itemsArray[ITEM_ARRAYS_EFFECT_INDEX] = faceunity.fuCreateItemFromPackage(data);
                    }
                } else {
                    File f = new File(mCurItemId);
                    if (null != f && f.exists() && f.length() > 0) {
                        try {
                            InputStream is = new FileInputStream(f);
                            byte[] item_data = new byte[is.available()];
                            is.read(item_data);
                            is.close();
                            itemsArray[ITEM_ARRAYS_EFFECT_INDEX] = faceunity.fuCreateItemFromPackage(item_data);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "IOException: " + e.toString());
                        }
                    }
                }


            }
        }
    };

    /**
     * 读取faceu资源
     *
     * @param assetName
     */
    private byte[] readAssetFaceu(String assetName) {
        byte[] data = null;
        try {
            InputStream is = mAssetManager.open(assetName);
            if (null != is) {
                data = new byte[is.available()];
                is.read(data);
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException: " + e);
        }
        return data;
    }

    private Runnable readBeauty = new Runnable() {

        @Override
        public void run() {
            byte[] data = readAssetFaceu(BUNDLE_FACE_BEAUTIFICATION);
            if (null != data) {
                itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] = faceunity.fuCreateItemFromPackage(data);
            }
        }
    };

    @Override
    public int onDrawFrame(int textureId, float[] transformMatrix, int flags) {
        if (bInitedSuccessed) {
//            Log.e(TAG, "onDrawFrame>" + mFuNotifyPause + "--"
//                    + "是否有数据:"
//                    + (mCameraData != null) + "---" + previewW + "*" + previewH);

            if (mFuNotifyPause) {
                //暂停绘制道具 (防止道具在界面上闪烁)
                return textureId;
            }
            int effectItem = itemsArray[ITEM_ARRAYS_EFFECT_INDEX];
//            android.util.Log.e(TAG, "onDrawFrame: " + mCreatedItemId + " >>" + mCurItemId);
            if ((!mCreatedItemId.equals(mCurItemId))) {
                if (effectItem != 0) {
                    faceunity.fuDestroyItem(effectItem);
                    itemsArray[ITEM_ARRAYS_EFFECT_INDEX] = effectItem = 0;
                }
                mCreatedItemId = mCurItemId;
            }
            int newTexId = textureId;

            if (null != mCameraData) {
                if (!TextUtils.isEmpty(mCurItemId)) {// 开启人脸道具
                    if (itemsArray[ITEM_ARRAYS_EFFECT_INDEX] == 0) {
                        if (inited) {
                            mhandler.post(readItem);
                        }
                    }
                }

                faceunity.fuItemSetParam(effectItem, "isAndroid", 1.0);
                int beauty = itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX];
                if (beauty == 0) {// 开启美白
                    if (inited) {
                        mhandler.post(readBeauty);
                    }
                } else {
                    faceunity.fuItemSetParam(beauty, "color_level",
                            faceHandler.getColor());// 美白
                    faceunity.fuItemSetParam(beauty, "blur_level",
                            faceHandler.getBlue());// 磨皮
                    faceunity.fuItemSetParam(beauty, "cheek_thinning",
                            faceHandler.getThin()); // 瘦脸(0-2.0f)
                    faceunity.fuItemSetParam(beauty, "eye_enlarging",
                            faceHandler.getEye());

                }
                flags = ((flags == IRecorderTextureCallBack.FLAG_OES_TEXTURE) ? faceunity.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE
                        : 0);
                newTexId = faceunity.fuDualInputToTexture(mCameraData,
                        textureId, flags, previewW, previewH, mFrameId++, itemsArray);
//                Log.e(TAG, mCreatedItemId + " thread:"
//                        + Thread.currentThread().getId() + " 耗时：" +
//                        (System.currentTimeMillis() - tem));


            } else {
//                Log.e(TAG, "画原始贴图222");
                newTexId = textureId;
            }
            return newTexId;
        }
//        Log.e(TAG, "画原始贴图33333333333333");
        return textureId;

    }

    /**
     * 返回美颜参数
     *
     * @param intent
     */
    public void saveFaceU(Intent intent) {
        FaceuInfo fu = new FaceuInfo();
        if (null != faceHandler) {
            fu.setBlur_level(faceHandler.getBlue());
            fu.setColor_level(faceHandler.getColor());
            fu.setEye_enlarging(faceHandler.getEye());
            fu.setCheek_thinning(faceHandler.getThin());
            intent.putExtra(SdkEntry.INTENT_KEY_FACEU, fu);
        }
    }


    /**
     * 通知切换摄像头前最后一帧不绘制面具美颜相关
     */
    public void onSwitchCamareBefore() {
        mFuNotifyPause = true;
    }

    /***
     * 切换摄像头后第一帧绘制原始贴图(不绘制面具美颜相关)
     */
    public void onSwitchCamareAfter() {
        if (bInitedSuccessed) {
            faceunity.fuOnCameraChange();
        }
        mFrameId = 0;
        mFuNotifyPause = false;
    }

    private boolean isSupportFace() {
        return (null != faceHandler && faceHandler.isbSupportFace());
    }

    /***
     * 是否暂停绘制faceu道具
     * @param fuNotifyPause
     */
    public void setFuNotifyPause(boolean fuNotifyPause) {
        mFuNotifyPause = fuNotifyPause;
        if (!mFuNotifyPause) {
            mFrameId = 0;
        }
    }


    public void onPasue() {
        if (isSupportFace()) {
            mFuNotifyPause = true;
            int tmp = itemsArray[ITEM_ARRAYS_EFFECT_INDEX];
            if (0 != tmp) {
                faceunity.fuDestroyItem(tmp);
                itemsArray[ITEM_ARRAYS_EFFECT_INDEX] = 0;
            }
            tmp = itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX];
            if (0 != tmp) {
                faceunity.fuDestroyItem(tmp);
                itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] = 0;
            }
            mFrameId = 0;
            if (bInitedSuccessed) {
                faceunity.fuOnDeviceLost();
            }
        }
    }

    /**
     * Activity ondestory() 清除内存
     */
    public void onDestory() {
        if (isSupportFace()) {
            mFuNotifyPause = true;
        }
        if (null != faceHandler) {
            faceHandler.onFinishView();
            faceHandler = null;
        }
        mCameraData = null;

    }

    /**
     * 注册人脸回调
     */
    public void registerCallBack() {
        if (isSupportFace()) {
            RecorderCore.setPreviewCallBack(this);
            RecorderCore.setTextureCallBack(this);
        } else {
            unRegister();
        }
    }

    /**
     * 清除faceu回调
     */
    public void unRegister() {
        RecorderCore.setPreviewCallBack(null);
        RecorderCore.setTextureCallBack(null);
    }

    private boolean enableBeautify = false;

    public boolean isEnabledBeautify() {
        return enableBeautify;
    }

    /**
     * 是否打开faceu美颜
     *
     * @param enable
     */
    public void enableBeautify(boolean enable) {
        if (enableBeautify != enable) {
            enableBeautify = enable;
            if (isSupportFace()) {
                faceHandler.enableBeauty(enableBeautify);
            }
        }
    }

    /**
     * 设置横竖屏旋转方向
     *
     * @param nOrientation 角度0-360
     */
    public void setOrientation(int nOrientation) {
        faceHandler.setOrientation(nOrientation);
    }
}
