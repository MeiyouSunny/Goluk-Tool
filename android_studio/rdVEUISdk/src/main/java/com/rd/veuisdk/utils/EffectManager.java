package com.rd.veuisdk.utils;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;

import com.rd.net.JSONObjectEx;
import com.rd.vecore.RdVECore;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.models.EffectInfo;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.VisualCustomFilter;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.veuisdk.database.EffectData;
import com.rd.veuisdk.model.EffectFilterInfo;
import com.rd.veuisdk.model.type.EffectType;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.rd.veuisdk.utils.TransitionManager.NOT_SUPPORT;

/**
 * 特效管理工具
 *
 * @author JIAN
 * @create 2018/12/28
 * @Describe
 */
public class EffectManager {
    private final String TAG = "EffectManager";

    private EffectManager() {
    }

    private volatile static EffectManager instance;

    public static EffectManager getInstance() {
        if (null == instance) {
            synchronized (EffectManager.class) {
                if (null == instance) {
                    instance = new EffectManager();
                }
            }
        }
        return instance;
    }


    private HashMap<String, Integer> registeredData = new HashMap<>();

    public void add(String url, int id) {
        registeredData.put(url, id);
    }


    /**
     * @param url
     * @return
     */
    public int getRegistered(String url) {
        Integer re = registeredData.get(url);
        if (null == re) {
            return 0;
        }
        return re;
    }


    /**
     * 获取滤镜对应的文件滤镜
     *
     * @param nCoreFilterId
     * @return
     */
    public String getCustomFilterPath(int nCoreFilterId) {
        String dst = null;

        Set<Map.Entry<String, Integer>> entrySet = registeredData.entrySet();
        if (null != entrySet) {
            for (Map.Entry<String, Integer> entry : entrySet) {
                if (entry.getValue() == nCoreFilterId) {
                    dst = entry.getKey();
                    break;
                }
            }
        }

        return dst;
    }

    /***
     *
     *
     * 根据绑定的滤镜文件。找到此滤镜文件的 滤镜id( 还原草稿 （每次重启应用程序），需要重新注册滤镜文件RdVECore.registerCustomFilter，需要根据文件重新给媒体设置滤镜id)
     * @param filterPath
     * @return
     */
    public int getFilterId(String filterPath) {
        int nFilterId = VisualFilterConfig.FILTER_ID_NORMAL;
        Set<Map.Entry<String, Integer>> entrySet = registeredData.entrySet();
        if (null != entrySet) {
            for (Map.Entry<String, Integer> entry : entrySet) {
                if (entry.getKey().equals(filterPath)) {
                    Integer integer = entry.getValue();
                    if (null != integer) {
                        nFilterId = integer;
                    }
                    break;
                }
            }
        }

        return nFilterId;
    }

    /**
     * 滤镜遮罩颜色
     *
     * @param filterId
     * @return
     */
    public EffectFilterInfo getRegisterFilterInfo(int filterId) {
        int len = mFilterList.size();
        EffectFilterInfo tmp = null;
        for (int i = 0; i < len; i++) {
            EffectFilterInfo filterInfo = mFilterList.get(i);
            if (filterInfo.getCoreFilterId() == filterId) {
                tmp = filterInfo;
                break;
            }
        }
        return tmp;
    }


    public synchronized void setFilterList(List<EffectFilterInfo> filterList) {
        mFilterList = filterList;
    }

    private List<EffectFilterInfo> mFilterList = new ArrayList<>();

    private void recycle() {
        registeredData.clear();
        mFilterList.clear();
    }

    /**
     * 初始化注册到全局
     *
     * @param context
     */
    public void init(Context context) {
        recycle();
        EffectData.getInstance().initilize(context);
        List<EffectFilterInfo> list = EffectData.getInstance().queryAll();
        if (null != list && list.size() > 0) {
            int len = list.size();
            for (int i = 0; i < len; i++) {
                EffectFilterInfo info = list.get(i);
                init(context, info, null,null);
                add(info.getFile(), info.getCoreFilterId());
            }
            setFilterList(list);
        }
    }

    /***
     * 修正特效的有效时间 (只保留0~maxend段的数据，不修正trimStart<>trimEnd)
     * @param mediaObject
     */
    public static void fixEffect(MediaObject mediaObject, ArrayList<EffectInfo> list) {
        //绑定的特效
        if (null != list && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                EffectInfo info = list.get(i);
                if (info.getFilterId() != EffectInfo.Unknown) {
                    //动感特效铺满整个时间轴
                } else {
                    //时间特效 (完全清理，不做修正)
                    list.remove(info);
                }
            }
            if (list.size() > 0) {
                mediaObject.setEffectInfos(list);
            }
        }
    }

    /**
     * 颜色解析
     *
     * @param jarr
     * @return
     */
    private int initColor(JSONArray jarr) {
        if (null != jarr && jarr.length() == 4) {
            try {
                return Color.argb((int) (jarr.getDouble(3) * 255), (int) (jarr.getDouble(0)), (int) (jarr.getDouble(1)), (int) (jarr.getDouble(2)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return Color.BLUE;
    }


    /**
     * 解析并注册滤镜
     */
    public boolean init(Context context, EffectFilterInfo effectFilterInfo, VirtualVideoView virtualVideoView,String path) {
        String dir = effectFilterInfo.getLocalPath();
        if (FileUtils.isExist(dir)) {
            String config = FileUtils.readTxtFile(new File(dir, "config.json").getAbsolutePath());
            try {
                JSONObjectEx jsonObjectEx = new JSONObjectEx(config);

                if (jsonObjectEx.optInt("minCoreVer", 0) > RdVECore.getVersionCode()) {
                    //当前版本不支持该滤镜特效
                    Log.e(TAG, "init: " + NOT_SUPPORT + " effectFilterInfo:" + effectFilterInfo);
                    return false;
                }
                JSONArray jarr = jsonObjectEx.optJSONArray("color");
                if (null != jarr && jarr.length() == 4) {
                    //颜色遮罩
                    effectFilterInfo.setColor(initColor(jarr));
                }
                String builtIn = jsonObjectEx.optString("builtIn", "");
                if (builtIn.equals("illusion")) {
                    //幻觉 (内置滤镜)
                    effectFilterInfo.setCoreFilterId(VisualFilterConfig.FILTER_ID_ECHO);
                } else {
                    VisualCustomFilter visualCustomFilter = new VisualCustomFilter();

                    int ver = jsonObjectEx.optInt("ver", 0);
                    if (ver >= 2) {
                        // 190131支持fragShader 支持加密
                        String name = jsonObjectEx.optString("name", "");
                        //解密key
                        visualCustomFilter.setName(name);
                    }

                    boolean isDingge = TextUtils.equals(EffectType.DINGGE, effectFilterInfo.getType());
                    if (isDingge && !FileUtils.isExist(path)) { //定格类型的滤镜必须要有图片才允许注册
                        return false;
                    }
                    int duration = jsonObjectEx.optInt("duration", -1);
                    if (-1 != duration) { //最佳效果的持续时长 单位：秒
                        effectFilterInfo.setDuration(duration);
                    }

                    if (jsonObjectEx.has("duration")) {
                        visualCustomFilter.setDuration((float) jsonObjectEx.optDouble("duration", 0));
                    }

                    String fragShader = jsonObjectEx.optString("fragShader");
                    fragShader = new File(dir, fragShader).getAbsolutePath();
                    if (FileUtils.isExist(fragShader)) {
                        visualCustomFilter.setFragmentShader(fragShader);
                    }
                    String vertShader = jsonObjectEx.optString("vertShader");
                    if (!TextUtils.isEmpty(vertShader)) {
                        vertShader = new File(dir, vertShader).getAbsolutePath();
                        if (FileUtils.isExist(vertShader)) {
                            visualCustomFilter.setVertexShader(vertShader);
                        }
                    }

                    //解析参数
                    VisualCustomFilterHelper.parseParams(jsonObjectEx, visualCustomFilter, dir,
                            jsonObjectEx.optInt("nit") == 0, path); //nit为1代表不添加内置纹理

                    if (null != virtualVideoView) {
                        //预览播放器会自动注册到全局
                        effectFilterInfo.setCoreFilterId(virtualVideoView.registerCustomFilter(visualCustomFilter));
                    } else {
                        effectFilterInfo.setCoreFilterId(RdVECore.registerCustomFilter(context, visualCustomFilter));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }


}
