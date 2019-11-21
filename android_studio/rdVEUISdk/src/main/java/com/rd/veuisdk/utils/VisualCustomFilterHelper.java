package com.rd.veuisdk.utils;

import android.renderscript.Matrix4f;
import android.text.TextUtils;
import android.util.Log;

import com.rd.lib.utils.FileUtils;
import com.rd.vecore.customFilter.TextureResource;
import com.rd.vecore.customFilter.UniformValue;
import com.rd.vecore.models.VisualCustomFilter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析自定义滤镜 （转场、特效）
 */

public class VisualCustomFilterHelper {

    /**
     * 解析参数(textureParams、uniformParams)
     *
     * @param appendDefaultInput true代表需要添加内置纹理参数
     */
    public static void parseParams(JSONObject jsonObject, VisualCustomFilter visualCustomFilter,
                                   String dir, boolean appendDefaultInput, String path) throws JSONException {
        parseTextureParams(jsonObject.optJSONArray("textureParams"), visualCustomFilter, dir, appendDefaultInput, path);
        parseUniformParams(jsonObject.optJSONArray("uniformParams"), visualCustomFilter);
    }

    private static Matrix4f initUniformValueMatrix4x4(JSONArray jarr) throws JSONException {
        float[] arr = new float[16];
        if (null != jarr && jarr.length() == 4) {
            int len = 4;
            for (int i = 0; i < len; i++) {
                JSONArray tmp = jarr.getJSONArray(i);
                int count = tmp.length();
                for (int j = 0; j < count; j++) {
                    arr[j + (i * 4)] = (float) tmp.getDouble(j);
                }
            }
        }
        return new Matrix4f(arr);
    }

    private static float[] initUniformValueFloatArr(JSONArray jarr) throws JSONException {
        float[] arr = null;
        if (null != jarr) {
            int len = jarr.length();
            arr = new float[len];
            for (int i = 0; i < len; i++) {
                arr[i] = (float) jarr.getDouble(i);
            }
        }
        return arr;
    }

    private static int initUniformValueInt(JSONArray jarr) throws JSONException {
        return jarr.getInt(0);
    }

    private static void parseUniformParams(JSONArray jarr, VisualCustomFilter visualCustomFilter) throws JSONException {
        int len = 0;
        if (null != jarr && (len = jarr.length()) > 0) {
            for (int i = 0; i < len; i++) {
                parseItemUniformParams(jarr.getJSONObject(i), visualCustomFilter);
            }
        }
    }

    private static void parseItemUniformParams(JSONObject tmp, VisualCustomFilter visualCustomFilter) throws JSONException {
        JSONArray frameArray = tmp.getJSONArray("frameArray");
        int count = 0;
        if (null != frameArray && (count = frameArray.length()) > 0) {
            UniformValue[] uniformValues = new UniformValue[count];
            int from = 0;
            String type = tmp.optString("type", "");
            for (int i = 0; i < count; i++) {
                JSONObject jFrame = frameArray.getJSONObject(i);
                int ms = (int) ((float) jFrame.getDouble("time") * 1000);
                if ("floatArray".toLowerCase().equals(type.toLowerCase())) {
                    JSONArray arrValue = jFrame.getJSONArray("value");
                    float[] farr = initUniformValueFloatArr(arrValue);
                    if (null != farr) {
                        uniformValues[i] = new UniformValue(from, ms, farr);
                    }
                } else if ("float".toLowerCase().equals(type.toLowerCase())) {
                    JSONArray arrValue = jFrame.optJSONArray("value");
                    float[] farr;
                    if (null != arrValue) {
                        farr = initUniformValueFloatArr(arrValue);
                    } else {
                        double value = jFrame.optDouble("value", 0);
                        farr = new float[]{(float) value};
                    }
                    uniformValues[i] = new UniformValue(from, ms, farr);
                } else if ("Matrix4x4".toLowerCase().equals(type.toLowerCase())) {
                    JSONArray arrValue = jFrame.getJSONArray("value");
                    UniformValue uniformValue = new UniformValue(from, ms);
                    uniformValue.setValue(initUniformValueMatrix4x4(arrValue));
                    uniformValues[i] = uniformValue;
                } else if ("int".toLowerCase().equals(type.toLowerCase())) {
                    JSONArray arrValue = jFrame.optJSONArray("value");
                    if (null != arrValue) {
                        uniformValues[i] = new UniformValue(from, ms, initUniformValueInt(arrValue));
                    } else {
                        //兼容
                        int value = jFrame.optInt("value", 0);
                        float[] farr = new float[]{(float) value};
                        uniformValues[i] = new UniformValue(from, ms, farr);
                    }
                }
                from = ms;
            }

            String paramName = tmp.optString("paramName");
            boolean repeat = tmp.optInt("repeat", 0) == 1;
            visualCustomFilter.setUniform(paramName, uniformValues, repeat);
        }
    }

    /**
     * 解析纹理参数
     *
     * @param appendDefaultInputTexture true代表需要添加内置纹理参数
     */
    private static void parseTextureParams(JSONArray jarr, VisualCustomFilter visualCustomFilter, String dir,
                                           boolean appendDefaultInputTexture, String path) throws JSONException {
        List<TextureResource> textureResources = new ArrayList<>();
        for (int i = 0; jarr != null && i < jarr.length(); i++) {
            parseItemTextureParams(jarr.getJSONObject(i), textureResources, dir, path);
        }
        if (textureResources.size() > 0) {
            boolean gotInternal = false;
            for (TextureResource tr : textureResources) {
                if (tr.getResourceType() == TextureResource.TEXTURE_RES_TYPE_INTERNAL) {
                    gotInternal = true;
                    break;
                }
            }
            if (!gotInternal && appendDefaultInputTexture) {
                textureResources.add(0, new TextureResource("inputImageTexture"));
            }
            TextureResource[] trArray = new TextureResource[textureResources.size()];
            textureResources.toArray(trArray);
            visualCustomFilter.setTextureResources(trArray);
        }
    }

    private static final String KEY_PARAM_NAME = "paramName";

    private static void parseItemTextureParams(JSONObject textureParam, List<TextureResource> textureResources, String dir, String path) {
        //            "textureParams":[{//纹理参数
//                "paramName": "noiseTexture",
//                        "warpMode": "Repeat",//纹理模式
// 1､ClampToEdge:位于纹理边缘或者靠近纹理边缘的纹理单元将用于纹理计算，但不使用纹理边框上的纹理单元,默认使用该模式
// 2､Repeat：纹理边界重复 3､MirroredRepeat:超出纹理范围的坐标整数部分被忽略，但当整数部分为奇数时进行取反，形成镜像效果
//                        "source": "image1.png"//纹理图片或者视频名称
//            }
        String paramName = textureParam.optString(KEY_PARAM_NAME, "");
        if (textureParam.has("source")) {
            String sourceName = textureParam.optString("source", "");
            String source = new File(dir, sourceName).getAbsolutePath();
            if (FileUtils.isExist(source)) {
                String wrapMode = textureParam.optString("wrapMode", "");
                if (TextUtils.isEmpty(wrapMode)) {
                    wrapMode = textureParam.optString("warpMode", "");
                }
                TextureResource.TextureWarpMode mode = TextureResource.TextureWarpMode.Repeat;
                if ("ClampToEdge".toLowerCase().equals(wrapMode.toLowerCase())) {
                    mode = TextureResource.TextureWarpMode.ClampToEdge;
                } else if ("MirroredRepeat".toLowerCase().equals(wrapMode.toLowerCase())) {
                    mode = TextureResource.TextureWarpMode.MirroredRepeat;
                }
                textureResources.add(new TextureResource(paramName,
                        TextureResource.TEXTURE_RES_TYPE_PATH, source, mode));
            }
        } else if (textureParam.has(KEY_PARAM_NAME)) {
            if (paramName.equals("currentFrameTexture")) {
                TextureResource.TextureWarpMode mode = TextureResource.TextureWarpMode.Repeat;
                if (com.rd.veuisdk.utils.FileUtils.isExist(path)) {
                    textureResources.add(new TextureResource(paramName,
                            TextureResource.TEXTURE_RES_TYPE_PATH, path, mode));
                } else {
                    Log.e("VisualFilterHelper", "parseItemTextureParams: path is null ");
                }
            } else {
                textureResources.add(new TextureResource(paramName));
            }
        }
    }

}
