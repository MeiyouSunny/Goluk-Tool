package com.rd.veuisdk.ae.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.rd.vecore.Music;
import com.rd.vecore.models.AEFragmentInfo;
import com.rd.vecore.models.BlendEffectObject;
import com.rd.vecore.models.MediaObject;
import com.rd.veuisdk.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * AE模板信息
 */
public class AETemplateInfo implements Parcelable {
    private static final String TAG = "AETemplateInfo";

    public void setVer(int ver) {
        this.ver = ver;
    }

    private int ver = 1;

    public AETemplateInfo(   ) {

    }

    protected AETemplateInfo(Parcel in) {
        ver = in.readInt();
        swDecode = in.readByte() == 1;
        frame = in.readInt();
        coverAsp = in.readFloat();
        zipFile = in.readString();
        mListBlendEffectObject = in.createTypedArrayList(BlendEffectObject.CREATOR);
        mAEDataPath = in.readString();
        mAEName = in.readString();
        mListAENoneEditPath = in.createStringArrayList();
        width = in.readInt();
        height = in.readInt();
        iconPath = in.readString();
        videoUrl = in.readString();
        picNum = in.readInt();
        textNum = in.readInt();
        videoNum = in.readInt();
        music = in.readParcelable(Music.class.getClassLoader());
        mListBgMedia = in.createTypedArrayList(BackgroundMedia.CREATOR);
        bUsedHashMap = in.readByte() != 0;
        mMediaObjects = in.createTypedArrayList(MediaObject.CREATOR);
        mListPath = in.createStringArrayList();
        mListDefaultMeida = in.createTypedArrayList(DefaultMedia.CREATOR);
        url = in.readString();
        updatetime = in.readString();
        mAETextLayerInfos = in.createTypedArrayList(AETextLayerInfo.CREATOR);
        mEditLayerNum = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ver);
        dest.writeByte(swDecode ? (byte) 1 : (byte) 0);
        dest.writeInt(frame);
        dest.writeFloat(coverAsp);
        dest.writeString(zipFile);
        dest.writeTypedList(mListBlendEffectObject);
        dest.writeString(mAEDataPath);
        dest.writeString(mAEName);
        dest.writeStringList(mListAENoneEditPath);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(iconPath);
        dest.writeString(videoUrl);
        dest.writeInt(picNum);
        dest.writeInt(textNum);
        dest.writeInt(videoNum);
        dest.writeParcelable(music, flags);
        dest.writeTypedList(mListBgMedia);
        dest.writeByte((byte) (bUsedHashMap ? 1 : 0));
        dest.writeTypedList(mMediaObjects);
        dest.writeStringList(mListPath);
        dest.writeTypedList(mListDefaultMeida);
        dest.writeString(url);
        dest.writeString(updatetime);
        dest.writeTypedList(mAETextLayerInfos);
        dest.writeInt(mEditLayerNum);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AETemplateInfo> CREATOR = new Creator<AETemplateInfo>() {
        @Override
        public AETemplateInfo createFromParcel(Parcel in) {
            return new AETemplateInfo(in);
        }

        @Override
        public AETemplateInfo[] newArray(int size) {
            return new AETemplateInfo[size];
        }
    };

    public float getCoverAsp() {
        return coverAsp;
    }

    public void setCoverAsp(float coverAsp, int width, int height) {
        this.coverAsp = coverAsp;
        nCoverWidth = width;
        nCoverHeight = height;
    }

    public int getCoverWidth() {
        return nCoverWidth;
    }

    public int getCoverHeight() {
        return nCoverHeight;
    }

    private int nCoverWidth, nCoverHeight;

    private float coverAsp = 1f;

    public boolean isSwDecode() {
        return swDecode;
    }

    public void setSwDecode(boolean swDecode) {
        this.swDecode = swDecode;
    }

    //是否需要强制软解
    private boolean swDecode = false;

    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    private int frame = 30;


    @Override
    public String toString() {
        return "AETemplateInfo{" +
                "coverAsp=" + coverAsp +
                ", zipFile='" + zipFile + '\'' +
                ", mAEFragmentInfo=" + mAEFragmentInfo +
                ", mListBlendEffectObject=" + mListBlendEffectObject +
                ", mAEDataPath='" + mAEDataPath + '\'' +
                ", mAEName='" + mAEName + '\'' +
                ", mListAENoneEditPath=" + mListAENoneEditPath +
                ", width=" + width +
                ", height=" + height +
                ", iconPath='" + iconPath + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", music=" + music +
                ", mListBgMedia=" + mListBgMedia +
                ", bUsedHashMap=" + bUsedHashMap +
                ", mMediaObjects=" + mMediaObjects +
                ", mHashMapMediaObject=" + mHashMapMediaObject +
                ", mListPath=" + mListPath +
                ", mListDefaultMeida=" + mListDefaultMeida +
                ", url='" + url + '\'' +
                ", updatetime='" + updatetime + '\'' +
                ", mEditLayerNum='" + mEditLayerNum + '\'' +
                ", mAETextLayerInfos=" + mAETextLayerInfos +
                '}';
    }


    public String getZipFile() {
        return zipFile;
    }

    public void setZipFile(String zipFile) {
        this.zipFile = zipFile;
    }

    private String zipFile;


    private AEFragmentInfo mAEFragmentInfo;

    private ArrayList<BlendEffectObject> mListBlendEffectObject = new ArrayList<>();

    private String mAEDataPath;

    private String mAEName;
    // 模板图片背景 (ver>=2不再需要此参数）
    @Deprecated
    private ArrayList<String> mListAENoneEditPath = new ArrayList<>();

    private int width, height;

    private String iconPath;

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    private String videoUrl;

    public int getPicNum() {
        return picNum;
    }

    public int getTextNum() {
        return textNum;
    }

    public int getVideoNum() {
        return videoNum;
    }

    private int picNum, textNum, videoNum;

    /**
     * 模板需要的媒体数目
     *
     * @param pic   图片数目
     * @param text  文本数目
     * @param video 视频数目
     */
    public void setMediaNum(int pic, int text, int video) {
        this.picNum = pic;
        this.textNum = text;
        this.videoNum = video;
    }

    private Music music;
    // 模板视频背景
    private ArrayList<BackgroundMedia> mListBgMedia = new ArrayList<>();
    // 用户选择替换媒体路径列表
    private boolean bUsedHashMap = false;
    private List<MediaObject> mMediaObjects = new ArrayList<>();
    //媒体列表    key:layer.getName();
    private HashMap<String, MediaObject> mHashMapMediaObject = new HashMap<>();

    //用户选择的资源
    private List<String> mListPath = new ArrayList<>();
    // 模板默认媒体资源路径列表
    private List<DefaultMedia> mListDefaultMeida = new ArrayList<>();

    private String url;

    public int getEditLayerNum() {
        return mEditLayerNum;
    }

    public void setEditLayerNum(int editLayerNum) {
        mEditLayerNum = editLayerNum;
    }

    //可编辑的layer数目
    private int mEditLayerNum = 0;

    public void setDataPath(String dataPath) {
        mAEDataPath = dataPath;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public ArrayList<BlendEffectObject> getBlendEffectObject() {
        return mListBlendEffectObject;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    private String updatetime;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public AEFragmentInfo getAEFragmentInfo() {
        return mAEFragmentInfo;
    }


    /**
     * @param useAllScene    true 内部用场景中的媒体替换zip中的img_*.png   ;false layer中的媒体由UISDK层指定  （预览时 ，如：AEDetailActivity ）
     * @param aeFragmentInfo
     * @return
     */
    public AETemplateInfo setAEFragmentInfo(boolean useAllScene, AEFragmentInfo aeFragmentInfo) {
        aeFragmentInfo.setUseAllScene(useAllScene);
        if (useAllScene) {
            //layer的媒体由core内部获取场景中的媒体资源
            setMediaObjects(null);
            setListPath(null);
            setMapMediaObjects(null);
        }
        mAEFragmentInfo = aeFragmentInfo;
        setLayerInfo(mAEFragmentInfo.getLayers());
        return this;
    }

    /**
     * 解析layer，处理各种类型的layer
     *
     * @param layerInfos
     */
    private void setLayerInfo(List<AEFragmentInfo.LayerInfo> layerInfos) {
        List<AEFragmentInfo.LayerInfo> backgroundLayers = new ArrayList<>();
        List<AEFragmentInfo.LayerInfo> editableLayers = new ArrayList<>();
        for (AEFragmentInfo.LayerInfo layer : layerInfos) {
            //可修改固定资源
            if (layer.getLayerType() == AEFragmentInfo.LayerType.NONE_EDIT) { //背景资源
                backgroundLayers.add(layer);
            } else {   //可替换资源
                editableLayers.add(layer);
            }
        }
        //根据开始时间排序
        Collections.sort(editableLayers, new Comparator<AEFragmentInfo.LayerInfo>() {
            @Override
            public int compare(AEFragmentInfo.LayerInfo lhs, AEFragmentInfo.LayerInfo rhs) {
                return new Float(lhs.getStartTime()).compareTo(rhs.getStartTime());
            }
        });
        int len = 0;
        boolean bBindMediaObject = false; // true  绑定的是媒体；false 绑定的是文件路径
        if (bUsedHashMap) {
            int mapSize = mHashMapMediaObject.size();
            if (mapSize > 0) {
                bBindMediaObject = true;
                len = mapSize;
            } else {
                bBindMediaObject = false;
                len = mListPath.size();
            }
        } else {

            int size = mMediaObjects.size();
            if (size > 0) {
                bBindMediaObject = true;
                len = size;
            } else {
                bBindMediaObject = false;
                len = mListPath.size();
            }
        }

        // 可替换资源赋值媒体路径
        for (int n = 0; n < editableLayers.size(); n++) {
            AEFragmentInfo.LayerInfo layerInfo = editableLayers.get(n);
            if (n < len) {
                if (bBindMediaObject) {
                    //图片视频媒体 （支持裁剪、旋转）
                    MediaObject tmp = bUsedHashMap ? mHashMapMediaObject.get(layerInfo.getName()) : mMediaObjects.get(n);
                    if (tmp == null) {
                        //没有媒体时，默认的图片资源占位
                        setLayerDefaultFile(layerInfo);
                    } else {
                        //有效的媒体资源
                        layerInfo.setMediaObject(tmp);
                    }
                } else { //图片资源
                    String path = mListPath.get(n);
                    if (!TextUtils.isEmpty(path)) {
                        layerInfo.setPath(path);
                    } else {
                        setLayerDefaultFile(layerInfo);
                    }
                }
            } else {  // 选择媒体不够使用模板默认媒体填充
                setLayerDefaultFile(layerInfo);
            }
        }
        // 不可替换资源赋值媒体路径
        if (ver >= 2) {
            len = backgroundLayers.size();
            for (int n = 0; n < len; n++) {
                AEFragmentInfo.LayerInfo layerInfo = backgroundLayers.get(n);
                setLayerDefaultFile(layerInfo);
            }
        } else {
            len = Math.min(backgroundLayers.size(), mListAENoneEditPath.size());
            for (int n = 0; n < len; n++) {
                AEFragmentInfo.LayerInfo layerInfo = backgroundLayers.get(n);
                layerInfo.setPath(getTargetNoEditPath(layerInfo.getName()));
            }
        }


    }

    /**
     * 默认文件资源
     *
     * @param layerInfo
     */
    private void setLayerDefaultFile(AEFragmentInfo.LayerInfo layerInfo) {
        String dirPath = new File(mAEDataPath).getParent();
        for (DefaultMedia defaultMedia : mListDefaultMeida) {
            if (layerInfo.getRefId().equals(defaultMedia.getRefId())) {
                String path = defaultMedia.getPath();
                if (!FileUtils.isExist(path) && path.contains("images/")) {  //部分模板没有遵循 “images/”目录结构
                    path = path.replace("images/", "");
                }
                if (FileUtils.isExist(path)) {
                    layerInfo.setPath(path);
                } else {
                    //****如果默认的资源不存在，则需要修正检测 (模板数据有问题，部分layer中的图片未遵循原生lottie的定义 例如：shoushigongke ...)
                    layerInfo.setPath(new File(dirPath, layerInfo.getName()).getAbsolutePath());
                }
                break;
            }
        }
    }

    /**
     * 指定的不可编辑的图片路径
     *
     * @param name background4.png
     * @return
     */
    @Deprecated
    private String getTargetNoEditPath(String name) {
        String dst = null;
        int len = mListAENoneEditPath.size();
        for (int i = 0; i < len; i++) {
            String file = mListAENoneEditPath.get(i);
            if (file.contains(name)) {
                dst = file;
                break;
            }
        }
        return dst;
    }

    /**
     * 设置mask screen视频对象
     *
     * @param blendEffectObject
     */
    public void setBlendEffectObject(ArrayList<BlendEffectObject> blendEffectObject) {
        mListBlendEffectObject.clear();
        if (null != blendEffectObject && blendEffectObject.size() > 0) {
            mListBlendEffectObject.addAll(blendEffectObject);
        }
    }

    public void setBackground(ArrayList<BackgroundMedia> videoBg) {
        mListBgMedia.clear();
        if (null != videoBg && videoBg.size() > 0) {
            mListBgMedia.addAll(videoBg);
        }
    }


    /**
     * 设置背景资源路径
     */
    @Deprecated
    public void setAENoneEditPath(ArrayList<String> bgPath) {
        mListAENoneEditPath.clear();
        if (null != bgPath && bgPath.size() > 0) {
            mListAENoneEditPath.addAll(bgPath);
        }
    }

    public void setMusic(Music music) {
        this.music = music;
    }


    public String getName() {
        return mAEName;
    }

    public void setName(String name) {
        this.mAEName = name;
    }


    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    /**
     * 用户选择的媒体资源列表 (setListPath setMapMediaObjects 只能二选一）
     *
     * @param mediaObjects 这种方式，无法媒体和RefId一对一对应
     */
    public void setMediaObjects(List<MediaObject> mediaObjects) {
        bUsedHashMap = false;
        mMediaObjects.clear();
        if (null != mediaObjects && mediaObjects.size() > 0) {
            mMediaObjects.addAll(mediaObjects);
        }
    }

    /**
     * 用户选择的媒体资源列表 (setListPath setMapMediaObjects 只能二选一）
     * RefId 和媒体一对一对应
     *
     * @param mediaObjects
     */
    public void setMapMediaObjects(HashMap<String, MediaObject> mediaObjects) {
        bUsedHashMap = true;
        mHashMapMediaObject.clear();
        if (null != mediaObjects) {
            mHashMapMediaObject.putAll(mediaObjects);
        }
    }


    /**
     * 用户选择的资源路径 (setListPath 与setMediaObjects 只能二选一）
     *
     * @param list
     */
    public void setListPath(List<String> list) {
        mListPath.clear();
        if (null != list && list.size() > 0) {
            mListPath.addAll(list);
        }
    }

    public void setDefaultMeida(List<DefaultMedia> defaultMeida) {
        mListDefaultMeida.clear();
        mListDefaultMeida.addAll(defaultMeida);
    }


    public String getVersion() {
        return null;
    }

    public int getId() {
        if (TextUtils.isEmpty(mAEDataPath)) {
            return -1;
        }
        return mAEDataPath.hashCode();
    }


    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getDataPath() {
        return mAEDataPath;
    }

    public Music getMusic() {
        return music;
    }

    public List<BackgroundMedia> getBackground() {
        return mListBgMedia;
    }


    public List<AETextLayerInfo> getAETextLayerInfos() {
        return mAETextLayerInfos;
    }

    public void setAETextLayerInfos(List<AETextLayerInfo> AETextLayerInfos) {
        mAETextLayerInfos = AETextLayerInfos;
    }

    /**
     * @param name
     * @return
     */
    public AETextLayerInfo getTargetAETextLayer(String name) {
        AETextLayerInfo info = null;
        if (!TextUtils.isEmpty(name)) {
            if (null != mAETextLayerInfos && mAETextLayerInfos.size() > 0) {
                int len = mAETextLayerInfos.size();
                for (int i = 0; i < len; i++) {
                    AETextLayerInfo textLayerInfo = mAETextLayerInfos.get(i);
                    if (null != textLayerInfo && textLayerInfo.getName().equals(name)) {
                        info = textLayerInfo;
                        break;
                    }
                }
                return info;
            }
        }
        return null;
    }

    private List<AETextLayerInfo> mAETextLayerInfos;


}
