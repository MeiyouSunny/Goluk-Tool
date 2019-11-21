package com.rd.veuisdk.ae;

import android.graphics.RectF;
import android.text.TextUtils;

import com.rd.lib.utils.FileUtils;
import com.rd.vecore.Music;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.models.AECustomTextInfo;
import com.rd.vecore.models.BlendEffectObject;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.ae.model.AETemplateInfo;
import com.rd.veuisdk.ae.model.AETextLayerInfo;
import com.rd.veuisdk.ae.model.BackgroundMedia;
import com.rd.veuisdk.ae.model.DefaultMedia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * AE模板工具类
 */
public class AETemplateUtils {
    private static final String TAG = "AETemplateUtils";
    private static final String CONFIG_JSON = "config.json";

    /***
     * 检测已经存在的AE模板的解压目录是否有效 （已经解压过）
     * @param key  zip路径的hashcode
     * @param file  目标文件夹
     * @return config所在的文件夹 (已经解压的目录)
     */
    private static String checkDir(String key, File file) {
        String dstPath = null;
        if (file.isDirectory()) {
            if (MiscUtils.enableMVDir(key, file.lastModified())) {
                File[] fs = file.listFiles();
                int len = 0;
                if (null != fs && (len = fs.length) >= 1) {
                    if (len == 1 && fs[0].isDirectory()) {
                        if (MiscUtils.enableMVDir(key, fs[0].lastModified())) {
                            //判断目录是否有被修改过，如果有被修改，则删除再重新解压
                            return fs[0].getAbsolutePath();
                        }
                    } else {
                        //zip格式中，没有以模板名字来命名的文件夹  (兼容部分zip)
                        for (int i = 0; i < len; i++) {
                            if (fs[i].getName().endsWith(CONFIG_JSON)) {
                                //如果存在配置文件，直接读取，否则从新解压
                                dstPath = file.getAbsolutePath();
                                break;
                            }
                        }
                    }
                }
            }
        }
        return dstPath;
    }

    /**
     * 解压AE模板
     *
     * @param zipPath ae模板路径 （**.zip）
     * @return 模板信息
     */
    public static AETemplateInfo parseAE(String zipPath) throws JSONException, IOException {
        if (TextUtils.isEmpty(zipPath)) {
            return null;
        }
        String key = Integer.toString(zipPath.hashCode());
        File targetDir = new File(MiscUtils.getAEPath(), key);
        String dirPath = checkDir(key, targetDir);
        if (!TextUtils.isEmpty(dirPath)) {
            //已经解压，并可用的文件夹目录
            return getConfig(dirPath);
        } else {
            FileUtils.deleteAll(targetDir);
            String dstDirPath = com.rd.lib.utils.FileUtils.unzip(zipPath, targetDir.getAbsolutePath());
            if (!TextUtils.isEmpty(dstDirPath)) {
                File file = new File(dstDirPath);
                if (file.isDirectory() && new File(file, CONFIG_JSON).exists()) {
                    MiscUtils.setMVDirModifi(key); //记录时刻，防止目录被修改
                    return getConfig(file.getAbsolutePath());
                } else {
                    for (File dir : targetDir.listFiles()) {
                        if (dir.isDirectory() && !dir.getName().toLowerCase().contains("_mac".toLowerCase())) {
                            //防止有mac系统压缩有多个文件夹（_macos）
                            // 文件路径替换src:/mv/jiaoyou--->/mv/郊游/jiaoyou
                            MiscUtils.setMVDirModifi(key);
                            return getConfig(dir.getAbsolutePath());
                        }
                    }
                }
            }
        }
        return null;
    }

    private static final int VER_TAG = 1;

    private static AETemplateInfo getConfig(String rootPath) throws JSONException {
        File config = new File(rootPath, CONFIG_JSON);
        String content = FileUtils.readTxtFile(config.getAbsolutePath());
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        JSONObject json = new JSONObject(content);
        return getConfigImp(config, json, rootPath, false);
    }


    private static AETemplateInfo getConfigImp(File config, JSONObject json, String rootPath, boolean ignoreText) throws JSONException {

        int ver = json.optInt("ver", 0);

        AETemplateInfo aeTemplateInfo = new AETemplateInfo();
        aeTemplateInfo.setVer(ver);
        //帧率
        aeTemplateInfo.setFrame(json.optInt("fr", 30));
        aeTemplateInfo.setDataPath(config.getAbsolutePath());
        parseAssets(aeTemplateInfo, rootPath, json.optJSONArray("assets"));


        JSONObject textimg = json.optJSONObject("textimg");
        if (null != textimg) {
            JSONArray jarr = textimg.optJSONArray("text");
            List<AETextLayerInfo> aeTextLayerInfos = new ArrayList<>();
            int len = jarr.length();
            for (int i = 0; i < len; i++) {
                AETextLayerInfo tmp = new AETextLayerInfo(jarr.getJSONObject(i), ignoreText);
                if (config.getAbsolutePath().toLowerCase().contains("Boxed".toLowerCase())) {
                    //demo 特别处理   （兼容boxed 9-16 ->ReplaceableText11 ）
                    if (tmp.getName().equals("ReplaceableText1.png") || tmp.getName().equals("ReplaceableText11.png")) {
                        tmp.setTextContent("世界那么大");
                    } else if (tmp.getName().equals("ReplaceableText2.png") || tmp.getName().equals("ReplaceableText12.png")) {
                        tmp.setTextContent("因为有你而与众不同");
                    } else {
                        tmp.setTextContent("");
                    }

                }

                if (!TextUtils.isEmpty(tmp.getFontSrc())) {
                    tmp.setTtfPath(config.getParent() + "/" + tmp.getFontSrc());
                }
                aeTextLayerInfos.add(tmp);
            }
            aeTemplateInfo.setAETextLayerInfos(aeTextLayerInfos);
        }


        JSONObject rdsetting = json.optJSONObject("rdsetting");
        if (rdsetting != null) {
            aeTemplateInfo.setSwDecode(rdsetting.optInt("swDecode", 0) == 1);
            aeTemplateInfo.setName(rdsetting.optString("theme"));
            aeTemplateInfo.setIconPath(rdsetting.optString("icon"));
            aeTemplateInfo.setWidth(rdsetting.optInt("w"));
            aeTemplateInfo.setHeight(rdsetting.optInt("h"));
            if (ver < 2) { //新版不再保留“aeNoneEdit”
                JSONArray jsonArray = rdsetting.optJSONArray(ver >= VER_TAG ? "aeNoneEdit" : "aene");
                if (jsonArray != null) {
                    ArrayList<String> listPath = new ArrayList<>();
                    for (int n = 0; n < jsonArray.length(); n++) {
                        listPath.add(new File(rootPath, jsonArray.optString(n)).getAbsolutePath());
                    }
                    aeTemplateInfo.setAENoneEditPath(listPath);
                }
            }
            parseMusic(aeTemplateInfo, rootPath, rdsetting.optJSONObject("music"), ver);
            parseBackground(aeTemplateInfo, rootPath, rdsetting.optJSONArray(ver >= VER_TAG ? "background" : "bg"), ver);
            parseEffect(aeTemplateInfo, rootPath, rdsetting.optJSONArray("effects"), ver);
            return aeTemplateInfo;
        }


        JSONObject settings = json.optJSONObject("settings");
        if (null != settings) {
            String mask = settings.optString("maskVideo");
            if (!TextUtils.isEmpty(mask)) {
                File file = new File(rootPath, mask);
                String backgroundVideo = settings.optString("backgroundVideo");
                String mediaPath = null;
                if (!TextUtils.isEmpty(backgroundVideo)) {
                    File bgVideo = new File(rootPath, backgroundVideo);
                    mediaPath = bgVideo.getAbsolutePath();
                    BackgroundMedia backgroundMedia = new BackgroundMedia(mediaPath);
                    backgroundMedia.setDuration((float) (settings.optDouble("duration") / 1000));
                    backgroundMedia.setBegintime(0);
                    backgroundMedia.setType("video");
                    ArrayList<BackgroundMedia> listBackground = new ArrayList<>();
                    listBackground.add(backgroundMedia);
                    aeTemplateInfo.setBackground(listBackground);
                }
                BlendEffectObject effectObject = new BlendEffectObject(mediaPath, BlendEffectObject.EffectObjectType.MASK);
                effectObject.setMaskMediaPath(file.getAbsolutePath());
                effectObject.setStartTime(0);
                effectObject.setFilterType("video");
                effectObject.setWidth((float) settings.optDouble("width"));
                effectObject.setHeight((float) settings.optDouble("height"));
                effectObject.setFPS(settings.optInt("fps"));
                effectObject.setEndTime((float) (settings.optDouble("duration") / 1000));
                ArrayList<BlendEffectObject> blendEffectObjects = new ArrayList<>();
                blendEffectObjects.add(effectObject);
                aeTemplateInfo.setBlendEffectObject(blendEffectObjects);
            }
            return aeTemplateInfo;
        }
        return aeTemplateInfo;
    }

    /**
     * 仿quik专用
     *
     * @param rootPath
     * @return
     * @throws JSONException
     */
    public static AETemplateInfo getConfig2(String rootPath) throws JSONException {
        File config = new File(rootPath, "data.json");
        String content = FileUtils.readTxtFile(config.getAbsolutePath());
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        JSONObject json = new JSONObject(content);
        return getConfigImp(config, json, rootPath, true);


    }


    public static String getTextConfig(String path, List<AECustomTextInfo> listCustomTextInfo) throws JSONException {
        File file = new File(path);
        String context = "";
        try {
            InputStream instream = new FileInputStream(file);
            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);

                String line;
                StringBuffer stringBuffer = new StringBuffer("");
                //分行读取  
                while ((line = buffreader.readLine()) != null) {
                    if (line.contains("]")) {
                        stringBuffer.append(line);
                        stringBuffer.append("\n");
                        String text = line.substring(line.indexOf("]") + 1);
                        String strTimeLine[] = line.substring(line.indexOf("[") + 1, line.indexOf("]")).split(",");
                        String strStartTime[] = strTimeLine[0].split(":");
                        String strEndTime[] = strTimeLine[1].split(":");
                        float startTime = Float.parseFloat(strStartTime[0]) * 60 + Float.parseFloat(strStartTime[1]);
                        float endTime = Float.parseFloat(strEndTime[0]) * 60 + Float.parseFloat(strEndTime[1]);
                        AECustomTextInfo aeCustomTextInfo = new AECustomTextInfo(text, startTime, endTime);
                        listCustomTextInfo.add(aeCustomTextInfo);
                    }
                }
                context = stringBuffer.toString();
                instream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return context;
    }


    /**
     * 解析音乐
     *
     * @param aeTemplateInfo
     * @param rootPath
     * @param joMusic
     * @throws JSONException
     */
    private static void parseMusic(AETemplateInfo aeTemplateInfo, String rootPath, JSONObject joMusic, int ver) throws JSONException {
        if (null != joMusic) {
            String musicPath = joMusic.optString(ver >= VER_TAG ? "fileName" : "src");
            if (!TextUtils.isEmpty(musicPath)) {
                Music music = VirtualVideo.createMusic(new File(rootPath, musicPath).getAbsolutePath());
                int startTime = (int) (joMusic.optDouble("begintime") * 1000);
                int endTime = startTime + (int) (joMusic.optDouble("duration") * 1000);
                music.setTimelineRange(startTime, endTime);
                aeTemplateInfo.setMusic(music);
            }
        }
    }


    /**
     * 解析 mask screen
     *
     * @param effects
     * @throws JSONException
     */
    private static void parseEffect(AETemplateInfo aeTemplateInfo, String rootPath, JSONArray effects, int ver)
            throws JSONException {
        ArrayList<BlendEffectObject> listBlendEffectObject = new ArrayList<>();
        for (int i = 0; effects != null && i < effects.length(); i++) {
            JSONObject joEffect = effects.getJSONObject(i);
            String strFilter = joEffect.getString("filter");
            if (TextUtils.isEmpty(strFilter))
                continue;
            String strPath = joEffect.optString(ver >= VER_TAG ? "fileName" : "src");
            BlendEffectObject.EffectObjectType objectType = null;
            if (strFilter.equalsIgnoreCase("mask")) {
                objectType = BlendEffectObject.EffectObjectType.MASK;
            } else if (strFilter.equalsIgnoreCase("screen")) {
                objectType = BlendEffectObject.EffectObjectType.SCREEN;
            }
            BlendEffectObject effectObject = new BlendEffectObject(new File(rootPath, strPath).getAbsolutePath(), objectType);
            effectObject.setStartTime((float) (joEffect.optDouble("begintime")));
            effectObject.setEndTime(effectObject.getStartTime() + (float) (joEffect.optDouble("duration")));
            effectObject.setRepeat(joEffect.optInt("repeat") == 1);
            effectObject.setFilterType("video");

            String srcPath = joEffect.optString("srcmask");
            if (!TextUtils.isEmpty(srcPath)) {
                File f = new File(rootPath, srcPath);
                effectObject.setMaskMediaPath(f.getAbsolutePath());
            }
            listBlendEffectObject.add(effectObject);

        }
        aeTemplateInfo.setBlendEffectObject(listBlendEffectObject);
    }

    /**
     * 解析背景视频
     *
     * @param backgrounds
     * @throws JSONException
     */
    private static void parseBackground(AETemplateInfo aeTemplateInfo, String rootPath, JSONArray backgrounds, int ver)
            throws JSONException {
        ArrayList<BackgroundMedia> listBackground = new ArrayList<>();
        for (int i = 0; backgrounds != null && i < backgrounds.length(); i++) {

            JSONObject joBg = backgrounds.getJSONObject(i);
            String strPath = joBg.optString(ver >= VER_TAG ? "fileName" : "src");
            BackgroundMedia backgroundMedia = new BackgroundMedia((new File(rootPath, strPath).getAbsolutePath()));

            backgroundMedia.setBegintime((float) (joBg.optDouble("begintime")));
            backgroundMedia.setDuration((float) (joBg.optDouble("duration")));
            backgroundMedia.setType(joBg.optString("type"));
            backgroundMedia.setMusic(joBg.optString("music"));

            JSONObject crop = joBg.optJSONObject("crop");
            if (crop != null) {
                backgroundMedia.setCropRect(new RectF((float) crop.optDouble("l"), (float) crop.optDouble("t"),
                        (float) crop.optDouble("r"), (float) crop.optDouble("b")));
            }
            listBackground.add(backgroundMedia);
        }
        aeTemplateInfo.setBackground(listBackground);
    }


    /**
     * 解析默认资源
     *
     * @param aeTemplateInfo
     * @param rootPath
     * @param assets
     */
    private static void parseAssets(AETemplateInfo aeTemplateInfo, String rootPath, JSONArray assets)
            throws JSONException {
        List<DefaultMedia> listDefaultMedia = new ArrayList<>();

        for (int i = 0; assets != null && i < assets.length(); i++) {
            JSONObject joAssets = assets.getJSONObject(i);

            DefaultMedia defaultMedia = new DefaultMedia(joAssets.optString("id"), new File(rootPath, joAssets.optString("u") + joAssets.optString("p")).getAbsolutePath());

            defaultMedia.setHeight(joAssets.optInt("h"));
            defaultMedia.setWidth(joAssets.optInt("w"));
            listDefaultMedia.add(defaultMedia);
        }
        aeTemplateInfo.setDefaultMeida(listDefaultMedia);
    }
}
