package com.rd.veuisdk;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.Camera;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.LinearLayout;

import com.rd.recorder.api.RecorderCore;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.adapter.CameraLocalAcvFilterAdapter;
import com.rd.veuisdk.adapter.CameraLocalAcvFilterAdapter.FilterItem;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.utils.ReplaceableUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 摄像头特效滤镜handler
 *
 * @author abreal
 */
class CameraEffectHandler {
    private Context mContext;
    private IFilterCheck mIFilterCheck;

    private boolean enableAcv = false;

    /**
     * @param filterUrl
     * @param IFilterCheck
     */
    public CameraEffectHandler(Context context, String filterUrl, IFilterCheck IFilterCheck) {
        mIFilterCheck = IFilterCheck;
        this.mContext = context;
        if (!TextUtils.isEmpty(filterUrl)) {
            cameraLookupHandler = new CameraLookupHandler(this.mContext, filterUrl, mIFilterCheck);
        } else {
            if (enableAcv) {
                //启用acv滤镜
                mCameraLocalAcvFilterAdapter = new CameraLocalAcvFilterAdapter(mContext);
            } else {
                //本地lookup滤镜
                cameraLookupHandler = new CameraLookupHandler(this.mContext, null, mIFilterCheck);
            }
        }
    }

    void recycle() {
        if (null != cameraLookupHandler) {
            cameraLookupHandler.recycle();
            cameraLookupHandler = null;
        }
    }


    class Str2IntComparator implements Comparator<String> {
        private boolean reverseOrder; // 是否倒序

        public Str2IntComparator(boolean reverseOrder) {
            this.reverseOrder = reverseOrder;
        }

        public int compare(String arg0, String arg1) {
            if (reverseOrder)
                return Integer.parseInt(arg1) - Integer.parseInt(arg0);
            else
                return Integer.parseInt(arg0) - Integer.parseInt(arg1);
        }
    }

    private CameraLookupHandler cameraLookupHandler;

    private void initEffects(boolean isVer, List<String> supportedColorEffects) {
        ArrayList<FilterItem> list = new ArrayList<FilterItem>();
        Collections.sort(supportedColorEffects, new Str2IntComparator(false));

        Resources res = mContext.getResources();
        list.add(new FilterItem(R.drawable.camera_effect_0, res
                .getString(R.string.camera_effect_0), supportedColorEffects
                .get(0)));
        try {
            list.add(new FilterItem(R.drawable.camera_effect_5, res
                    .getString(R.string.camera_effect_5), supportedColorEffects
                    .get(5)));
            list.add(new FilterItem(R.drawable.camera_effect_6, res
                    .getString(R.string.camera_effect_6), supportedColorEffects
                    .get(6)));
            list.add(new FilterItem(R.drawable.camera_effect_7, res
                    .getString(R.string.camera_effect_7), supportedColorEffects
                    .get(7)));
            list.add(new FilterItem(R.drawable.camera_effect_8, res
                    .getString(R.string.camera_effect_8), supportedColorEffects
                    .get(8)));
            list.add(new FilterItem(R.drawable.camera_effect_9, res
                    .getString(R.string.camera_effect_9), supportedColorEffects
                    .get(9)));
            list.add(new FilterItem(R.drawable.camera_effect_10, res
                    .getString(R.string.camera_effect_10), supportedColorEffects
                    .get(10)));

            if (!SdkEntry.isLite(mContext)) {
                //完整版（acv）
                ReplaceableUtils.initCameraFilterAcv(list, res, supportedColorEffects);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCameraLocalAcvFilterAdapter.addAll(isVer, list);
    }

    private CameraLocalAcvFilterAdapter mCameraLocalAcvFilterAdapter;

    private String TAG = "CameraEffectHandler";

    public interface IFilterCheck {

        void onSelected(int nItemId, boolean user);
    }


    private void initListener() {
        mCameraLocalAcvFilterAdapter.setOnItemClickListener(new OnItemClickListener<Object>() {
            @Override
            public void onItemClick(int position, Object item) {
                if (com.rd.veuisdk.utils.Utils
                        .getSupportExpandEffects()) {
                    Log.e(TAG,
                            mContext.getString(R.string.livecamera_record_switch_filter_failed));
                } else {
                    if (null != mIFilterCheck) {
                        mIFilterCheck.onSelected(position, true);
                    }
                }
            }
        });


    }

    /**
     * 刷新
     */
    public void notifyDataSetChanged(boolean isVer) {
        if (isLookup()) {
            cameraLookupHandler.notifyDataSetChanged(isVer);
        } else {
            if (null != mCameraLocalAcvFilterAdapter) {
                mCameraLocalAcvFilterAdapter.setOrientation(isVer);
                mCameraLocalAcvFilterAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * * 初始并刷新所有特效滤镜
     *
     * @param recyclerView
     * @param supportedColorEffects
     * @param checkItemId           默认选中项
     */
    public void initAllEffects(boolean isVer, RecyclerView recyclerView, LinearLayout strengthLayout,
                               List<String> supportedColorEffects, int checkItemId) {


        if (isLookup()) {
            Log.i(TAG, "initAllEffects: lookup");
            //lookup
            cameraLookupHandler.initView(recyclerView, strengthLayout);
            cameraLookupHandler.onResume();
            cameraLookupHandler.init(isVer, checkItemId);

        } else {
//acv
            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false));
//设置添加或删除item时的动画，这里使用默认动画
            recyclerView.setItemAnimator(new DefaultItemAnimator());
//设置适配器
            recyclerView.setAdapter(mCameraLocalAcvFilterAdapter);

            if (RecorderCore.isSupportBeautify()) {
                initEffects(isVer, supportedColorEffects);
                initListener();
            } else {
                //sdk 支持api18+
                List<FilterItem> tmp = new ArrayList<>();
                tmp.add(new FilterItem(R.drawable.camera_filter_, mContext.getString(R.string.camare_filter_0), Camera.Parameters.EFFECT_NONE));
                initListener();
                mCameraLocalAcvFilterAdapter.addAll(isVer, tmp);
            }
            mCameraLocalAcvFilterAdapter.onItemChecked(checkItemId);
        }
        Log.i(TAG, "initAllEffects-------supported filter--finish  test----------");
    }

    /**
     * 是否是lookup滤镜方式
     *
     * @return
     */

    public boolean isLookup() {
        return (null != cameraLookupHandler);
    }

    /**
     * 获取系统内置特效滤镜
     *
     * @param nItemIndex
     * @return
     */
    public String getInternalColorEffectByItemId(int nItemIndex) {

        if (isLookup()) {
            //lookup 返回文件路径
            if (nItemIndex >= 0 && nItemIndex < cameraLookupHandler.size()) {
                return cameraLookupHandler.get(nItemIndex);
            } else {
                return Camera.Parameters.EFFECT_NONE;
            }
        } else {
            //返回acv 滤镜id
            if (nItemIndex >= 0 && nItemIndex < mCameraLocalAcvFilterAdapter.getItemCount()) {
                FilterItem info = mCameraLocalAcvFilterAdapter.getItem(nItemIndex);
                if (null != info) {
                    return info.effect;
                }
            }
            return Camera.Parameters.EFFECT_NONE;
        }
    }


    /**
     * @return
     */
    public int getEffectCount() {
        if (isLookup()) {
            return cameraLookupHandler.size();
        } else {
            return mCameraLocalAcvFilterAdapter.getItemCount();
        }
    }

    /**
     * @param nItemId
     */
    public void selectListItem(int nItemId) {
        if (isLookup()) {
            cameraLookupHandler.selectListItem(nItemId);
        } else {
            mCameraLocalAcvFilterAdapter.onItemChecked(nItemId);
        }
    }

    /**
     * @return
     */
    public int getCurrentItemId() {
        if (isLookup()) {
            return cameraLookupHandler.getCurrentItemId();
        } else {
            return (null != mCameraLocalAcvFilterAdapter) ? mCameraLocalAcvFilterAdapter.getCurrentId() : 0;
        }
    }
}
