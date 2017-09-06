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
import com.rd.veuisdk.ui.HorizontalListViewCamera;

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

    private byte[] mCameraData;
    private String mCreatedItemId = "";
    private String mCurItemId = "";

    private boolean mFuNotifyPause;
    private int mFrameId;

    private FaceuUIHandler faceHandler;
    private String LOG_TAG = FaceuHandler.this.toString();
    private byte[] pack;
    private FaceuConfig config;
    private int[] itemsArray = new int[3];
    private int mFacebeautyItem = 0, mEffectItem = 0;

    private AssetManager ast;
    private Context context;

    public FaceuHandler(Context _context, RadioGroup filterGroup,
                        HorizontalListViewCamera camareLV, View filter_menu_parent,
                        byte[] _pack, FaceuConfig faceConfig, View fuBeautyLayout,
                        LinearLayout fuLayout, LinearLayout fuLayoutParent, LinearLayout filter_parent_layout, IReloadListener listener) {
        context = _context;
        ast = _context.getAssets();
        config = faceConfig;
        pack = _pack;
        faceHandler = new FaceuUIHandler(filterGroup, camareLV,
                filter_menu_parent, (null != pack && pack.length > 0), config,
                fuBeautyLayout, new FaceuListener() {

            @Override
            public void onFUChanged(String mp3Path, int lastPosition) {
                mCurItemId = mp3Path;
            }
        }, fuLayout, fuLayoutParent, filter_parent_layout, listener);
    }

    private boolean inited = false;
    //初始化faceU成功,防止没有v3.mp3
    private boolean bInitedSuccessed = false;

    public void onInitFaceunity() {
        if (isSupportFace()) {
            inited = false;

            isSwitching = false;
            mFuNotifyPause = true;
            mFrameId = 0;
            mCameraData = null;
            byte[] v3data = null;
            bInitedSuccessed = false;
            try {
                InputStream is = ast.open("faceu/v3.mp3");
                if (null != is) {
                    v3data = new byte[is.available()];
                    is.read(v3data);
                    is.close();
                    bInitedSuccessed = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "IOException: " + e);
            } finally {
                if (bInitedSuccessed) {
                    faceunity.fuSetup(v3data, null, pack);
                    faceunity.fuSetMaxFaces(1);
                }
                inited = true;
                mFacebeautyItem = 0;
                mEffectItem = 0;
                itemsArray[0] = mFacebeautyItem;
                itemsArray[1] = mEffectItem;
            }


        }
        inited = true;
    }

    private int previewW, previewH;

    @Override
    public void onPreviewFrame(byte[] nv21data, int width, int height) {
//        Log.e("onPreviewFrame" + this.toString(), ((null!=nv21data)?nv21data.length:"null")
//                + "..xxxxxxxxxxxx." + width + "*" + height);
        previewW = width;
        previewH = height;
        mCameraData = nv21data;
    }

    private Handler mhandler = new Handler();

    private Runnable readItem = new Runnable() {

        @Override
        public void run() {
            if (!TextUtils.isEmpty(mCurItemId)
                    && !TextUtils.equals(mCurItemId, FaceuAdapter.NONE)) {
                File f = new File(mCurItemId);
                if (null != f && f.exists() && f.length() > 0) {
                    try {
                        InputStream is = new FileInputStream(f);
                        byte[] item_data = new byte[is.available()];
                        is.read(item_data);
                        is.close();
                        mEffectItem = faceunity
                                .fuCreateItemFromPackage(item_data);
                        itemsArray[1] = mEffectItem;
                        // Log.e("ondrawframe", "readItem->" + mCurItemId
                        // + itemsArray[1]);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(LOG_TAG, "IOException: " + e.toString());
                    }
                }
            }
        }
    };
    private Runnable readBeauty = new Runnable() {

        @Override
        public void run() {
            mFacebeautyItem = 0;
            try {
                InputStream is = ast.open("faceu/face_beautification.mp3");
                if (null != is) {
                    byte[] item_data = new byte[is.available()];
                    is.read(item_data);
                    is.close();
                    mFacebeautyItem = faceunity
                            .fuCreateItemFromPackage(item_data);
                }
                // Log.e("beauty....", itemsArray[0] + "");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "IOException: " + e);
            } finally {
                itemsArray[0] = mFacebeautyItem;
            }


        }
    };

    @Override
    public int onDrawFrame(int textureId, float[] transformMatrix, int flags) {
        if (bInitedSuccessed) {
//        Log.e("ondrawframe..." + this.toString(), mFuNotifyPause + "--"
//                + "---isSwitching" + isSwitching + "是否有数据:"
//                + (mCameraData != null) + "---" + previewW + "*" + previewH);
            if ((!mCreatedItemId.equals(mCurItemId)) || mFuNotifyPause
                    || isSwitching) {
                // Log.e("ondrawframe." + mCreatedItemId + "__" + mFuNotifyPause,
                // mCurItemId + "destoryitem--"
                // + Arrays.toString(itemsArray));
                if (itemsArray[1] != 0) {
                    faceunity.fuDestroyItem(itemsArray[1]);
                    itemsArray[1] = 0;
                    mEffectItem = 0;

                }
                mCreatedItemId = mCurItemId;
                mFuNotifyPause = false;
            }
            int newTexId = 0;

            if (null != mCameraData) {
                if (!TextUtils.isEmpty(mCurItemId)) {// 开启人脸道具
                    if (!isSwitching) {

                        if (itemsArray[1] == 0) {
                            if (inited) {
                                mhandler.post(readItem);
                            }
                        }

                    } else {
                        itemsArray[1] = 0;
                    }
                }

                faceunity.fuItemSetParam(mEffectItem, "isAndroid", 1.0);
                if (mFacebeautyItem == 0) {// 开启美白

                    if (inited) {
                        mhandler.post(readBeauty);
                    }
                }


                // long tem = System.currentTimeMillis();
                if (mFacebeautyItem != 0) {
                    faceunity.fuItemSetParam(mFacebeautyItem, "color_level",
                            faceHandler.getColor());// 美白
                    faceunity.fuItemSetParam(mFacebeautyItem, "blur_level",
                            faceHandler.getBlue());// 磨皮
                    faceunity.fuItemSetParam(mFacebeautyItem, "cheek_thinning",
                            faceHandler.getThin()); // 瘦脸(0-2.0f)
                    faceunity.fuItemSetParam(mFacebeautyItem, "eye_enlarging",
                            faceHandler.getEye());

                }
                flags = ((flags == IRecorderTextureCallBack.FLAG_OES_TEXTURE) ? faceunity.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE
                        : 0);
                newTexId = faceunity.fuDualInputToTexture(mCameraData,
                        textureId, flags, previewW, previewH, mFrameId++, itemsArray);
                // Log.e(mCreatedItemId + " facehaoshiwu thin-"
                // + Thread.currentThread().getId(),
                // (System.currentTimeMillis() - tem) + "----/"
                // + Arrays.toString(itemsArray));


            } else {
                //切换正方形长方形时，第一帧返回null，清除当前贴纸效果，防止闪屏
                faceunity.fuOnCameraChange();
                mEffectItem = 0;
                itemsArray[1] = mEffectItem;
                mFuNotifyPause = true;
//            Log.e("null----", "画原始贴图222");
                newTexId = textureId;
            }
            return newTexId;
        }
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

    private boolean isSwitching = false;

    /**
     * 通知切换摄像头
     */
    public void onSwitchCamare(boolean isSwitchIng) {
        if (isSupportFace()) {
            isSwitching = isSwitchIng;
            if (!isSwitchIng) {
                if (bInitedSuccessed) {
                    faceunity.fuOnCameraChange();
                }
            }
            mEffectItem = 0;
            itemsArray[1] = mEffectItem;
            mFuNotifyPause = true;
            mCameraData = null;// 数据重置为null,先画原始贴图
        }
    }

    private boolean isSupportFace() {
        return (null != faceHandler && faceHandler.isbSupportFace());
    }

    public void onPasue() {
        // Log.e("onpas.....", inited + this.toString());
        if (isSupportFace()) {
            mFuNotifyPause = true;
            mEffectItem = 0;
            mFacebeautyItem = 0;
            if (0 != itemsArray[1]) {
                if (bInitedSuccessed) {
                    faceunity.fuDestroyItem(itemsArray[1]);
                }
                itemsArray[1] = mEffectItem;
            }
            if (0 != itemsArray[0]) {
                if (bInitedSuccessed) {
                    faceunity.fuDestroyItem(itemsArray[0]);
                }
                itemsArray[0] = mFacebeautyItem;
            }
            mFrameId = 0;
            // Log.e("clear", this.toString());
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

        itemsArray = null;
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
