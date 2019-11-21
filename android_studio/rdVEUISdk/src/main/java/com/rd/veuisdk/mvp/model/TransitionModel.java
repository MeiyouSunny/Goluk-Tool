package com.rd.veuisdk.mvp.model;


import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownListener;
import com.rd.http.MD5;
import com.rd.lib.utils.LogUtil;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SdkEntry;
import com.rd.veuisdk.database.TransitionData;
import com.rd.veuisdk.model.EffectTypeDataInfo;
import com.rd.veuisdk.model.TransitionInfo;
import com.rd.veuisdk.model.bean.AppData;
import com.rd.veuisdk.model.bean.DataBean;
import com.rd.veuisdk.model.bean.TypeBean;
import com.rd.veuisdk.model.bean.TypeData;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.ModeDataUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.TransitionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 执行逻辑（加载网络数据和下载功能的具体实现）
 *
 * @author JIAN
 * @create 2019/2/22
 * @Describe
 */
public class TransitionModel implements ITransitionModel {
    private Context mContext;
    private Handler mHandler;
    private HashMap<Long, DownLoadUtils> mMapDown = null;
    private String TAG = "TransitionModel";

    public TransitionModel(Context context) {
        mContext = context;
        mHandler = new Handler();
    }

    //基础类型
    public static final int BASE_TYPE_ID = "TransitionModel".hashCode();

    /**
     * 初始化基础效果
     */
    private EffectTypeDataInfo initBaseType() {

        TypeBean typeBean = new TypeBean();
        typeBean.setName(mContext.getString(R.string.transition_type_base));
        typeBean.setId(Integer.toString(BASE_TYPE_ID));
        EffectTypeDataInfo info = new EffectTypeDataInfo(typeBean);

        List<TransitionInfo> list = new ArrayList<>();
        int nId = 0;
        list.add(new TransitionInfo(nId++, mContext.getString(R.string.none), "asset:///transition/transition_null_normal.png"));
        initBase(nId, list);
        info.setList(list);
        return info;

    }

    /**
     * 查询数据库列表中指定的项
     *
     * @param dbList
     * @param url
     * @return
     */
    private TransitionInfo getDbItem(List<TransitionInfo> dbList, String url) {
        TransitionInfo tmp = null;
        if (null != dbList) {
            int len = dbList.size();
            TransitionInfo info;
            for (int i = 0; i < len; i++) {
                info = dbList.get(i);
                if (TextUtils.equals(url, info.getUrl())) {
                    tmp = info;
                    break;
                }
            }
        }
        return tmp;
    }

    /**
     * 单个分类下的转场数据
     *
     * @param srcList json数据列表
     * @return 转场对象列表
     */
    private List<TransitionInfo> getChild(List<DataBean> srcList, List<TransitionInfo> dbList) {
        List<TransitionInfo> list = new ArrayList<>();
        int len = srcList.size();
        for (int i = 0; i < len; i++) {
            DataBean dataBean = srcList.get(i);
            String name = dataBean.getName();
            String url = dataBean.getFile();
            String cover = dataBean.getCover();
            long updateTime = dataBean.getUpdatetime();
            if (dbList == null) {
                dbList = TransitionData.getInstance().queryAll();
            }
            TransitionInfo local = getDbItem(dbList, url);
//            TransitionInfo local = TransitionData.getInstance().quweryOne(url);
            boolean result = false;
            if (null != local) {
                //判断服务器是否已经更新 （如果有更新删除旧的文件或文件夹）
                if (updateTime != local.getUpdatetime()) {
                    //服务端已经更新，需要重新下载
                    FileUtils.deleteAll(local.getLocalPath());
                    result = true;
                }
            }

            if (result) {
                //旧的版本，服务端已经更新，需要重新下载
                TransitionInfo info = new TransitionInfo(url, cover, name, "", updateTime);
                list.add(info);
                //更新本地数据库
                TransitionData.getInstance().replace(info);
                dbList = null;
            } else {
                if (null != local) {
                    local.setUpdatetime(updateTime);
                    local.setCover(cover);
                    String localPath = local.getLocalPath();
                    if (FileUtils.isExist(localPath)) {
                        TransitionInfo transitionInfo = new TransitionInfo(url, cover, name, localPath, updateTime);
                        transitionInfo.setCoreFilterId(TransitionManager.getInstance().getFilterId(url));
                        //已下载
                        list.add(transitionInfo);
                    } else {
                        //下载的异常,没有找到本地文件
                        TransitionInfo info = new TransitionInfo(url, cover, name, "", updateTime);
                        TransitionData.getInstance().replace(info);
                        dbList = null;
                        list.add(info);
                    }
                } else {
                    list.add(new TransitionInfo(url, cover, name, "", updateTime));
                }
            }
        }
        return list;
    }


    @Override
    public void initData(final String typeUrl, final String url, @NonNull final ICallBack callBack) {
        ThreadPoolUtils.execute(new ThreadPoolUtils.ThreadPoolRunnable() {
            List<EffectTypeDataInfo<TransitionInfo>> mList;

            @Override
            public void onBackground() {
                mList = new ArrayList<>();
                isWebTansition = false;
                //基础类型
                mList.add(initBaseType());

                //高级类型
                if (TextUtils.isEmpty(typeUrl) || TextUtils.isEmpty(url)) {
                    initLocal(mList);
                    //防止同时请求多个http耗时，（优先返回分组信息）
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (null != callBack) {
                                callBack.onSuccess(mList);
                            }
                        }
                    });
                } else {
                    isWebTansition = true;
                    getWebTransition(mList, typeUrl, url, callBack);
                }
            }
        });

    }

    /**
     * 是否请求的是网络转场
     *
     * @return
     */
    @Override
    public boolean isWebTansition() {
        return isWebTansition;
    }


    /**
     * filter的sd 完整路径
     *
     * @param info
     * @return
     */
    private String getFilterFilePath(TransitionInfo info) {

        return PathUtils.getRdTransitionPath() + "/" + MD5.getMD5(info.getUrl()) +
                (info.getFile().toLowerCase().contains("glsl".toLowerCase()) ? ".glsl" : ".jpg");
    }

    @Override
    public void downTransition(Context context, int itemId, final TransitionInfo info, @NonNull final IDownCallBack iDownCallBack) {

        if (null == mMapDown) {
            mMapDown = new HashMap<>();
        }
        if (!mMapDown.containsKey((long) itemId)) {
            /**
             * 支持指定下载文件的存放位置
             */
            final DownLoadUtils download = new DownLoadUtils(context, itemId, info.getUrl(), getFilterFilePath(info));
            download.setConfig(0, 50, 100);
            LogUtil.i(TAG, "downTransition: " + itemId);
            download.DownFile(new IDownListener() {

                @Override
                public void onFailed(long l, int i) {
                    Log.e(TAG, "onFailed: " + l + i);
                    if (null != mMapDown) {
                        mMapDown.remove((Long) l);
                    }
                    iDownCallBack.downFailed((int) l, R.string.download_failed);
                }

                @Override
                public void onProgress(long mid, int progress) {
                }

                @Override
                public void Finished(long mid, String localPath) {
                    LogUtil.i(TAG, "Finished: " + localPath);
                    mMapDown.remove((Long) mid);
                    if (info.getFile().toLowerCase().contains("zip".toLowerCase())) {
                        //需要解压
                        try {
                            localPath = FileUtils.unzip(localPath, PathUtils.getRdFilterPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    info.setLocalPath(localPath);
                    iDownCallBack.downSuccessed((int) mid, info);
                }

                @Override
                public void Canceled(long mid) {
                    Log.e(TAG, "Canceled: " + mid);
                    if (null != mMapDown) {
                        mMapDown.remove((Long) mid);
                    }
                    iDownCallBack.downFailed((int) mid, R.string.download_failed);
                }
            });
            mMapDown.put((long) itemId, download);
        } else {
            Log.e(TAG, "download " + info.getUrl() + "  is mMapDown");
        }
    }

    @Override
    public void onCancel() {
        if (null != mMapDown && mMapDown.size() > 0) {
            Set<Map.Entry<Long, DownLoadUtils>> set = mMapDown.entrySet();
            if (null != set) {
                for (Map.Entry<Long, DownLoadUtils> entry : set) {
                    entry.getValue().setCancel();
                }
            }
            mMapDown.clear();
        }
    }

    /**
     * 获取内置的随机转场效果
     */
    public List<TransitionInfo> initData(@Deprecated String url, @Deprecated boolean isRandom) {
        return initRandom();
    }


    private boolean isWebTansition = false;


    /**
     * 按照分类id获取
     *
     * @param url      单个分类数据
     * @param typeBean 单个分类
     * @return
     */
    private void getItemData(String url, TypeBean typeBean, List<TransitionInfo> dbList, EffectTypeDataInfo info) {
        if (null != typeBean) {
            AppData appData = ModeDataUtils.getEffectAppData(url, ModeDataUtils.TYPE_TRANSITION, typeBean.getId());
            if (null != appData) {
                info.setList(getChild(appData.getData(), dbList));
            }
        }
    }

    /**
     * @param dstList 目标集合
     * @param typeUrl 分类接口
     * @param url     获取单个分类下的数据
     */
    private void getWebTransition(final List<EffectTypeDataInfo<TransitionInfo>> dstList, String typeUrl, String url, final ICallBack callback) {
        String str = ModeDataUtils.getModeData(typeUrl, ModeDataUtils.TYPE_TRANSITION);
        if (!TextUtils.isEmpty(str)) {
            TypeData typeData = JSON.parseObject(str, TypeData.class);
            int lastLen = dstList.size();
            int len = typeData.getData().size();
            for (int i = 0; i < len; i++) {
                EffectTypeDataInfo<TransitionInfo> tmp = new EffectTypeDataInfo(typeData.getData().get(i));
                dstList.add(tmp);
            }
            //防止同时请求多个http耗时，（优先返回分组信息）
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (null != callback) {
                        callback.onSuccess(dstList);
                    }
                }
            });

            List<TransitionInfo> dbList = TransitionData.getInstance().queryAll();
            for (int i = 0; i < len; i++) {
                getItemData(url, typeData.getData().get(i), dbList, dstList.get(lastLen + i));
            }
        } else {
            //防止同时请求多个http耗时，（优先返回分组信息）
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (null != callback) {
                        callback.onSuccess(dstList);
                    }
                }
            });
        }
    }


    private List<TransitionInfo> initRandom() {
        List<TransitionInfo> list = new ArrayList<>();
        list.clear();
        initBase(0, list);
        list.add(new TransitionInfo("glsl", null, "wind", "asset://rand_transition/wind.glsl", 0));
        list.add(new TransitionInfo("glsl", null, "Swirl", "asset://rand_transition/Swirl.glsl", 0));
        list.add(new TransitionInfo("glsl", null, "swap", "asset://rand_transition/swap.glsl", 0));
        list.add(new TransitionInfo("glsl", null, "squareswire", "asset://rand_transition/squareswire.glsl", 0));
        list.add(new TransitionInfo("glsl", null, "ripple", "asset://rand_transition/ripple.glsl", 0));
        list.add(new TransitionInfo("glsl", null, "Mosaic", "asset://rand_transition/Mosaic.glsl", 0));
        list.add(new TransitionInfo("glsl", null, "angular", "asset://rand_transition/angular.glsl", 0));
        list.add(new TransitionInfo("glsl", null, "circleopen", "asset://rand_transition/circleopen.glsl", 0));
        list.add(new TransitionInfo("glsl", null, "CrazyParametricFun", "asset://rand_transition/CrazyParametricFun.glsl", 0));
        list.add(new TransitionInfo("glsl", null, "CrossZoom", "asset://rand_transition/CrossZoom.glsl", 0));
        list.add(new TransitionInfo("glsl", null, "cube", "asset://rand_transition/cube.glsl", 0));
        list.add(new TransitionInfo("glsl", null, "Dreamy", "asset://rand_transition/Dreamy.glsl", 0));
        list.add(new TransitionInfo("glsl", null, "DreamyZoom", "asset://rand_transition/DreamyZoom.glsl", 0));
        list.add(new TransitionInfo("glsl", null, "GlitchMemories", "asset://rand_transition/GlitchMemories.glsl", 0));
        list.add(new TransitionInfo("glsl", null, "GridFlip", "asset://rand_transition/GridFlip.glsl", 0));
        list.add(new TransitionInfo("glsl", null, "heart", "asset://rand_transition/heart.glsl", 0));
        list.add(new TransitionInfo("glsl", null, "kaleidoscope", "asset://rand_transition/kaleidoscope.glsl", 0));
        return list;
    }

    /**
     * 基础转场
     */
    private int initBase(int nId, List<TransitionInfo> list) {
        list.add(new TransitionInfo(nId++, mContext.getString(R.string.show_style_item_recovery), "asset:///transition/transition_recovery_normal.png"));
        list.add(new TransitionInfo(nId++, mContext.getString(R.string.show_style_item_to_up), "asset:///transition/transition_to_up_normal.png"));
        list.add(new TransitionInfo(nId++, mContext.getString(R.string.show_style_item_to_down), "asset:///transition/transition_to_down_normal.png"));
        list.add(new TransitionInfo(nId++, mContext.getString(R.string.show_style_item_to_left), "asset:///transition/transition_to_left_normal.png"));
        list.add(new TransitionInfo(nId++, mContext.getString(R.string.show_style_item_to_right), "asset:///transition/transition_to_right_normal.png"));
        list.add(new TransitionInfo(nId++, mContext.getString(R.string.show_style_item_flash_white), "asset:///transition/transition_flash_white_normal.png"));
        list.add(new TransitionInfo(nId++, mContext.getString(R.string.show_style_item_flash_black), "asset:///transition/transition_flash_black_normal.png"));
        return nId;
    }

    private void initLocal(List<EffectTypeDataInfo<TransitionInfo>> data) {
        int nId = 0;
        if (!SdkEntry.isLite(mContext)) {
            List<TransitionInfo> list = new ArrayList<>();
            list.add(new TransitionInfo(nId++, mContext.getString(R.string.none), "asset:///transition/transition_null_normal.png"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_003.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_004.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_005.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_006.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_007.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_008.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_009.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_012.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_014.JPG"));

            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_015.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_016.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_017.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_018.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_019.JPG"));

            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_020.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_021.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_022.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_023.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_024.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_025.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_026.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_027.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_028.JPG"));

            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_030.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_031.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_032.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_033.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_034.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_035.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_036.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_037.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_038.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_039.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_040.JPG"));

            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_041.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_042.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_043.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_044.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_045.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_047.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_048.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_049.JPG"));

            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_050.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_051.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_052.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_053.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_054.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_055.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_056.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_057.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_058.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_059.JPG"));

            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_060.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_061.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_062.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_063.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_064.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_065.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_066.JPG"));
            list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_067.JPG"));

            TypeBean typeBean = new TypeBean();
            String text = mContext.getString(R.string.transition_type_chachu);
            typeBean.setId(Integer.toString(text.hashCode()));
            typeBean.setName(text);
            typeBean.setType(text);
            EffectTypeDataInfo effectTypeDataInfo = new EffectTypeDataInfo(typeBean);
            effectTypeDataInfo.setList(list);

            data.add(effectTypeDataInfo);
        }
    }


}
