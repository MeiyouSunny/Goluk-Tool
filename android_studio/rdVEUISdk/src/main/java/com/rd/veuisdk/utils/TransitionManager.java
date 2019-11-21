package com.rd.veuisdk.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.rd.net.JSONObjectEx;
import com.rd.vecore.RdVECore;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.customFilter.TextureResource;
import com.rd.vecore.models.Transition;
import com.rd.vecore.models.VisualCustomFilter;
import com.rd.veuisdk.database.TransitionData;
import com.rd.veuisdk.model.TransitionInfo;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author JIAN
 * @create 2019/2/20
 * @Describe 自定义转场管理工具
 */
public class TransitionManager {
    private final String TAG = "TransitionManager";
    public static final String NOT_SUPPORT = "Current version not supported !";

    private TransitionManager() {
    }

    private volatile static TransitionManager instance;

    public static TransitionManager getInstance() {
        if (null == instance) {
            synchronized (TransitionManager.class) {
                if (null == instance) {
                    instance = new TransitionManager();
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
        int nFilterId = Transition.Unknown;
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


    public synchronized void setFilterList(List<TransitionInfo> filterList) {
        mFilterList = filterList;
    }

    public List<TransitionInfo> getFilterList() {
        return mFilterList;
    }

    private List<TransitionInfo> mFilterList = new ArrayList<>();

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
        TransitionData.getInstance().initilize(context);
        List<TransitionInfo> list = TransitionData.getInstance().queryAll();
        List<TransitionInfo> tmp = new ArrayList<>();
        if (null != list && list.size() > 0) {
            int len = list.size();
            for (int i = 0; i < len; i++) {
                TransitionInfo info = list.get(i);
                if (isGlsl(info)) {
                    //只记录*.glsl 转场滤镜  (普通灰度图转场不需要注册)
                    if (init(context, info, null)) {
                        tmp.add(info);
                        add(info.getFile(), info.getCoreFilterId());
                    }
                }
            }
        }
        setFilterList(tmp);
    }

    /**
     * 是否是glsl 转场
     *
     * @param info
     * @return
     */
    public boolean isGlsl(TransitionInfo info) {
        String file = info.getFile().toLowerCase();
        return file.contains("glsl".toLowerCase()) || file.contains("zip".toLowerCase());
    }


    /**
     * 解析并注册滤镜
     */
    public boolean init(Context context, TransitionInfo transitionInfo, VirtualVideoView virtualVideoView) {
        String dir = transitionInfo.getLocalPath();
        if (FileUtils.isExist(context, dir)) {
            VisualCustomFilter visualCustomFilter = new VisualCustomFilter();
            File file = new File(dir);
            if (file.isDirectory()) {
                String config = FileUtils.readTxtFile(new File(dir, "config.json").getAbsolutePath());
                try {
                    JSONObjectEx jsonObjectEx = new JSONObjectEx(config);
                    int ver = jsonObjectEx.optInt("ver", 0);
                    if (ver >= 1) {
                        // 190226支持fragShader 支持加密
                        //解密key
                        visualCustomFilter.setName(jsonObjectEx.optString("name", ""));
                    }
                    if (jsonObjectEx.optInt("minCoreVer", 0) > RdVECore.getVersionCode()) {
                        //当前版本不支持该转场效果
                        Log.e(TAG, "init: " + NOT_SUPPORT + " transitionInfo:" + transitionInfo);
                        return false;
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
                    VisualCustomFilterHelper.parseParams(jsonObjectEx, visualCustomFilter, dir, false,null);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                boolean gotInternalTexture = false;
                TextureResource[] textureResources = visualCustomFilter.getTextureResources();
                if (textureResources != null) {
                    for (TextureResource tr : textureResources) {
                        if (tr.getResourceType() == TextureResource.TEXTURE_RES_TYPE_INTERNAL) {
                            gotInternalTexture = true;
                            break;
                        }
                    }
                }

                if (!gotInternalTexture) {
                    List<TextureResource> setTextureResources = new ArrayList<>();
                    setTextureResources.add(new TextureResource("from"));
                    setTextureResources.add(new TextureResource("to"));
                    if (null != textureResources) {
                        Collections.addAll(setTextureResources, textureResources);
                    }
                    textureResources = new TextureResource[setTextureResources.size()];
                    setTextureResources.toArray(textureResources);
                    visualCustomFilter.setTextureResources(textureResources);
                }
            } else {
                //单文件
                visualCustomFilter.setFragmentShader(FileUtils.readTxtFile(context, dir));
                visualCustomFilter.setTextureResources(new TextureResource[]
                        {new TextureResource("from"),
                                new TextureResource("to")});
            }

            if (null != virtualVideoView) {
                //预览播放器会自动注册到全局
                transitionInfo.setCoreFilterId(virtualVideoView.registerCustomFilter(visualCustomFilter));
            } else {
                transitionInfo.setCoreFilterId(RdVECore.registerCustomFilter(context, visualCustomFilter));
            }
            return true;
        }
        return false;

    }
}
