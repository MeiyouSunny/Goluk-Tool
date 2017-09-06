package com.rd.veuisdk.utils;

import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.SparseArray;

import com.rd.lib.utils.CoreUtils;
import com.rd.net.JSONObjectEx;
import com.rd.veuisdk.model.FilterInfo2;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.model.StyleT;
import com.rd.veuisdk.model.TimeArray;
import com.rd.veuisdk.model.WordInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class CommonStyleUtils {
    // static String STYLEINFO_TXT = "styleInfo.txt";
    public static final String CONFIG_JSON = "config.json";

    private CommonStyleUtils() {

    }

    private static double OUT_WIDTH = 640.0, OUT_HEIGHT = 360.0;
    private static final double PWIDTH = 640.0, PHEIGHT = 360.0;
    public static double asp = -1.0;

    public static void init(double width, double height) {
        OUT_WIDTH = width;
        OUT_HEIGHT = height;
        asp = OUT_WIDTH / OUT_HEIGHT;
    }

    public static void getConfig(File config, StyleInfo info) {
        String content = FileUtils.readTxtFile(config.getAbsolutePath());
        if (!TextUtils.isEmpty(content)) {

            try {
                JSONObjectEx json = new JSONObjectEx(content);

                // info.pid = json.getInt("pid");

                info.fid = json.getInt("fid");
                info.type = json.getInt("type");
                info.pExtend = json.getInt("pExtend");
                info.extendSection = json.getInt("extendSection");
                info.x = json.getDouble("x");
                info.y = json.getDouble("y");
                info.w = json.getDouble("w");
                info.h = json.getDouble("h");
                info.a = json.getDouble("a");
                // new change
                info.lashen = 0;
                info.onlyone = 0;
                info.shadow = 0;
                if (json.has("shadow")) {
                    info.shadow = json.getInt("shadow");
                }
                if (json.has("lashen")) {
                    info.lashen = json.getInt("lashen");
                    if (json.has("onlyone")) {
                        info.onlyone = json.getInt("onlyone");
                    }

                    info.left = json.getDouble("left");
                    if (info.left == 0)
                        info.left = 1.0;
                    info.top = json.getDouble("top");
                    if (info.top == 0)
                        info.top = 1.0;
                    info.right = json.getDouble("right");
                    if (info.right == 0)
                        info.right = 1.0;
                    info.buttom = json.getDouble("buttom");
                    if (info.buttom == 0)
                        info.buttom = 1.0;
                } else {

                    info.fx = json.getDouble("fx");
                    info.fy = json.getDouble("fy");
                    info.fw = json.getDouble("fw");
                    info.fh = json.getDouble("fh");
                }

                if (info.type == 0) {

                    info.tLeft = json.getInt("tLeft");
                    info.tTop = json.getInt("tTop");
                    if (info.lashen == 1) {
                        info.tRight = json.getInt("tRight");
                        info.tButtom = json.getInt("tButtom");
                        info.tWidth = 0;
                        info.tHeight = (int) (info.h - info.tTop - info.tButtom);
                    } else {
                        info.tWidth = json.getInt("tWidth");
                        info.tHeight = json.getInt("tHeight");
                    }

                    info.tFont = json.optString("tFont", "");
                    if (!TextUtils.isEmpty(info.tFont)) {
                        if (json.has("strokeR")) {
                            info.strokeColor = Color.rgb(
                                    json.getInt("strokeR"),
                                    json.getInt("strokeG"),
                                    json.getInt("strokeB"));
                        }
                        info.strokeWidth = json.optInt("strokeWidth", 0);

                    }
                }
                info.rotateAngle = (float) json.optDouble("tAngle", 0.0);
                info.code = config.getParentFile().getName();
                info.pid = info.code.hashCode();
                info.n = json.getString("n");

                info.du = (int) (json.getDouble("du") * 1000);

                JSONObject jtemp = null;
                StyleT tempT = null;
                FilterInfo2 finfo;

                float[] start = new float[2], end = new float[2];
                int halfw = info.tWidth / 2;
                int halfh = info.tHeight / 2;
                if (info.type == 0) {

                    start[0] = (float) ((info.tLeft - halfw + 0.0) / info.w);
                    start[1] = (float) ((info.tTop - halfh + 0.0) / info.h);
                    end[0] = (float) ((info.tLeft + halfw + 0.0) / info.w);
                    end[1] = (float) ((info.tTop + halfh + 0.0) / info.h);
                    String ptext = json.optString("pText");
                    finfo = new FilterInfo2(ptext, Color.rgb(json.getInt("tR"),
                            json.getInt("tG"), json.getInt("tB")), start, end,
                            info.pid);

                } else {
                    start[0] = 0.01f;
                    start[1] = 0.01f;
                    end[0] = 0.99f;
                    end[1] = 0.99f;
                    finfo = new FilterInfo2("", Color.parseColor("#ffffff"),
                            start, end, info.pid);
                    // info.centerxy[0] = (float) (info.x / PWIDTH);
                    // info.centerxy[1] = (float) (info.y / PHEIGHT);
                }
                info.setFilterInfo2(finfo);

                info.centerxy[0] = (float) info.x;
                info.centerxy[1] = (float) info.y;
                // Log.e("info.center....",
                // Arrays.toString(info.centerxy)+"...");

                JSONArray jarr = json.getJSONArray("frameArry");
                String tpictemp = config.getParent() + "/" + info.code;
                // info.icon = config.getParent() + "/icon.png";
                int len = jarr.length();
                for (int i = 0; i < len; i++) {
                    jtemp = jarr.getJSONObject(i);
                    tempT = new StyleT();
                    int ntime = (int) (jtemp.getDouble("time") * 1000);
                    tempT.time = ntime;
                    tempT.pic = tpictemp + jtemp.getInt("pic") + ".png";
                    info.frameArry.put(ntime, tempT);

                }

                jarr = json.getJSONArray("timeArry");

                len = jarr.length();

                for (int i = 0; i < len; i++) {
                    jtemp = jarr.getJSONObject(i);
                    info.timeArrays.add(new TimeArray((int) (jtemp
                            .getDouble("beginTime") * 1000), (int) (jtemp
                            .getDouble("endTime") * 1000)));

                }

                Options op = new Options();
                op.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(info.frameArry.valueAt(0).pic, op);

                // Log.e("sic",
                // OUT_WIDTH+"...."+PWIDTH+".....arr"+Arrays.toString(start)+"...."+Arrays.toString(end));
                info.disf = (float) (info.w / (op.outWidth + 0.0f) * ((OUT_WIDTH + .0) / PWIDTH));

                if (CoreUtils.getMetrics().widthPixels < 720) {
                    info.disf = Math.min(info.disf, 0.8f);// 防止横屏字幕太大。
                }

                // info.disf = (float) (info.w / (op.outWidth + 0.0f) *
                // (CoreUtils
                // .getMetrics().widthPixels / OUT_WIDTH));
                info.isdownloaded = true;
            } catch (JSONException e) {
                FileLog.writeLog(e.getMessage() + info.mlocalpath);
            }
        }
    }

    public static boolean checkStyle(File styleDir, StyleInfo info) {
        if (!styleDir.isDirectory() && styleDir.exists())
            return false;
        info.code = styleDir.getName();
        info.pid = info.code.hashCode();

        File fconfig = new File(styleDir.getAbsolutePath(), CONFIG_JSON);
        if (fconfig.exists() && fconfig.length() > 0) {
            getConfig(fconfig, info);

            return true;
        }
        return false;
    }

    /**
     * @param info
     */
    public static StyleInfo getDefualt(StyleInfo info) {
        info.x = (int) (OUT_WIDTH / 2);
        info.y = (int) (OUT_HEIGHT / 2);

        float[] start = new float[2], end = new float[2];
        start[0] = (float) (10.0 / 400);
        start[1] = (float) (5.0 / 60);
        end[0] = (float) (390.0 / 400);
        end[1] = (float) (55.0 / 60);

        info.type = 0;// only subtitle
        FilterInfo2 finfo = new FilterInfo2("", Color.parseColor("#ffffff"),
                start, end, info.pid);
        info.setFilterInfo2(finfo);
        info.centerxy[0] = (float) (info.x / OUT_WIDTH);
        info.centerxy[1] = (float) (info.y / OUT_HEIGHT);

        return info;
    }

    public static boolean isEquals(ArrayList<WordInfo> templist,
                                   ArrayList<WordInfo> savelist) {

        if (templist.size() != savelist.size()) {
            return false;

        }

        boolean isequals = true;
        WordInfo temp;
        for (int i = 0; i < templist.size(); i++) {
            temp = templist.get(i);
            if (!temp.equals(savelist.get(i))) {
                isequals = false;
                break;
            }

        }
        return isequals;
    }

    /**
     * 默认单个字幕的区域时间为视频的/20
     *
     * @param mDuration
     * @return
     */
    public static int getItemLength(int mDuration) {
        int itemlength = mDuration / 10;
        // if (itemlength > 2000) {
        // itemlength = 2000;
        // } else if (itemlength < 1000) {
        // itemlength = 1000;
        // }
        itemlength = 10;
        return itemlength;
    }

    public static enum STYPE {
        sub, special;

    }

    private static String TAG = CommonStyleUtils.class.getName();

    /**
     * 根据newNum 找出最接近的Frame对应的StyleT
     *
     * @param nearNum    进度单位ims
     * @param arrayList
     * @param timeArrays
     * @param isEdit     当前Item 是否正在编辑  （可以拖动 rect位置）
     * @param spDuration 当前特效的duration 单位：ms
     * @return
     */
    public static StyleT search(int nearNum, SparseArray<StyleT> arrayList, ArrayList<TimeArray> timeArrays, boolean isEdit, int spDuration) {
        StyleT item, resultP = null;
        int len = 0;
        if (null != arrayList && (len = arrayList.size()) > 0) {
            int itemDuration = 100;
            try {
                itemDuration = arrayList.valueAt(1).time - arrayList.valueAt(0).time;
            } catch (Exception e) {
                itemDuration = 100;
            }
            int timeArraySize = timeArrays.size();
            if (timeArraySize == 3) {
                int headCount = timeArrays.get(0).getDuration() / itemDuration;
                int headDuration = timeArrays.get(0).getDuration();

                if (nearNum <= headDuration) {
                    // 差值实始化
                    int diffNum = Math.abs(arrayList.valueAt(0).time - nearNum);
                    for (int i = 0; i < headCount; i++) {
                        item = arrayList.valueAt(i);
                        int diffNumTemp = Math.abs(item.time - nearNum);
//                        Log.e(TAG, i + "search: " + diffNumTemp + "....." + diffNum);
                        if (diffNumTemp <= diffNum) {
                            diffNum = diffNumTemp;
                            resultP = item;
                        }
                    }
                } else {
                    if (isEdit) {
                        TimeArray loopArray = timeArrays.get(1);
                        int loopdu = loopArray.getDuration();
                        int nd = ((nearNum - headDuration) % loopdu) / itemDuration;
                        resultP = arrayList.valueAt((loopArray.getBegin() / itemDuration) + nd);
//                        Log.e(TAG, len + "editsearch: " + "---" + nd);
                    } else {
                        //如果是滑动进度条 获取当前位置的特效图，（已经知道 开始、结束  ）

                        TimeArray lastArray = timeArrays.get(2);
//                        Log.e(TAG, "search: " + lastArray.getDuration() + "..." + spDuration + "....." + nearNum);
                        if (nearNum < (spDuration - lastArray.getDuration())) {
                            //循环中间部分
                            TimeArray loopArray = timeArrays.get(1);
                            int bodyItemDuration = loopArray.getDuration();
                            int nd = ((nearNum - headDuration) % bodyItemDuration) / itemDuration;
                            resultP = arrayList.valueAt((loopArray.getBegin() / itemDuration) + nd);
                        } else {
                            //从末尾Array中找出要绘制的图片
                            if (nearNum <= spDuration) {
                                //距离结束还有多少毫秒
                                int offd = spDuration - nearNum;
                                //是倒数第几帧
                                int lastf = offd / itemDuration;
                                int t = (lastArray.getEnd() / itemDuration) - 1 - lastf;
                                int tdst = Math.min(len - 1, Math.max(0, t));
                                resultP = arrayList.valueAt(tdst);
//                                Log.e(TAG, len + "false末尾" + nearNum + "---->" + spDuration + "...." + arrayList.size() + "..............." + tdst + "-->t:" + t + ".....>" + lastf + "...offd:" + offd + "..." + lastArray.getDuration() + "..result." + resultP.pic);

                            } else {
//                                Log.e(TAG, "search: 异常" + nearNum + "---->" + spDuration);
                                resultP = arrayList.valueAt(len - 1);
                            }
                        }
                    }
                }


            } else {


                if (timeArraySize == 2) {
                    int headDuration = timeArrays.get(0).getDuration();
                    int headCount = headDuration / itemDuration;


                    if (nearNum <= headDuration) {
                        // 差值实始化
                        int diffNum = Math.abs(arrayList.valueAt(0).time - nearNum);
                        for (int i = 0; i < headCount; i++) {
                            item = arrayList.valueAt(i);
                            int diffNumTemp = Math.abs(item.time - nearNum);
//                        Log.e(TAG, i + "search: " + diffNumTemp + "....." + diffNum);
                            if (diffNumTemp <= diffNum) {
                                diffNum = diffNumTemp;
                                resultP = item;
                            }
                        }
                    } else {
                        if (isEdit) {
                            TimeArray loopArray = timeArrays.get(1);
                            int loopdu = loopArray.getDuration();
                            int nd = ((nearNum - headDuration) % loopdu) / itemDuration;
                            int index = (loopArray.getBegin() / itemDuration) + nd; //从循环部分的begin时刻算起
                            int tIndex = Math.max(0, Math.min(index, (arrayList.size() - 1)));
                            resultP = arrayList.valueAt(tIndex);
//                            Log.e(TAG, arrayList.size() + "len>" + len + "editsearch: " + "---" + index + "...." + tIndex + "...." + nd);
                        } else {
                            //如果是滑动进度条 获取当前位置的特效图，（已经知道 开始、结束  ）
                            TimeArray loopArray = timeArrays.get(1);
//                        Log.e(TAG, "search: " + lastArray.getDuration() + "..." + spDuration + "....." + nearNum);

                            if (nearNum < spDuration) {
                                //循环中间部分
                                int loopdu = loopArray.getDuration();
                                int nd = ((nearNum - headDuration) % loopdu) / itemDuration;
                                int index = (loopArray.getBegin() / itemDuration) + nd;
                                int tIndex = Math.max(0, Math.min(index, (arrayList.size() - 1)));
                                resultP = arrayList.valueAt(tIndex);
                            }
                        }
                    }


                } else {

                    // 差值实始化
                    int diffNum = Math.abs(arrayList.valueAt(0).time - nearNum);
                    //只有（header 和last 、header)
                    for (int i = 0; i < len; i++) {
                        item = arrayList.valueAt(i);
                        int diffNumTemp = Math.abs(item.time - nearNum);
//                    Log.e(TAG, diffNumTemp + "<" + diffNum + "...>" + item.time + "-->>" + nearNum);
                        if (diffNumTemp <= diffNum) {
                            diffNum = diffNumTemp;
                            resultP = item;
                        }
                    }
                }

            }
        }
//        Log.e(TAG, isEdit + "search: " + nearNum + "..." + ((resultP != null) ? (resultP.pic + "..." + resultP.time) : "null"));
        return resultP;

    }
}
