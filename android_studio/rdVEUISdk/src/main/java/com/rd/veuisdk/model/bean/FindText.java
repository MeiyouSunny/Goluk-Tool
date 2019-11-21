package com.rd.veuisdk.model.bean;

import android.support.annotation.Keep;
import android.text.TextUtils;

import com.rd.veuisdk.demo.zishuo.TextNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @create 2019/8/7
 * @Describe 腾讯云语音识别（提取音频中的文本）
 */
@Keep
public class FindText {


    /**
     * code : 0
     * msg : ok
     * data : {"id":"220","code":"0","message":"成功","requestId":"515085097","appid":"1259660397","projectid":"0","audioUrl":null,"text":"[0:0.140,0:14.860,0]  事业就是啊，李白好像钱您也光疑是地上霜，举头望明月，低头是故乡。\n[0:0.140,0:14.860,1]  是夜市啊，李白好像钱您也光你是不上山，举头望明月，低头是故乡。\n","audioTime":"14.884","createtime":"2019-08-07 16:25:51"}
     */

    private int code;
    private String msg;
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    @Keep
    public static class DataBean {
        /**
         * id : 220
         * code : 0
         * message : 成功
         * requestId : 515085097
         * appid : 1259660397
         * projectid : 0
         * audioUrl : null
         * text : [0:0.140,0:14.860,0]  事业就是啊，李白好像钱您也光疑是地上霜，举头望明月，低头是故乡。
         * [0:0.140,0:14.860,1]  是夜市啊，李白好像钱您也光你是不上山，举头望明月，低头是故乡。
         * <p>
         * audioTime : 14.884
         * createtime : 2019-08-07 16:25:51
         */

        private String id;
        private String code;
        private String message;
        private String requestId;
        private String appid;
        private String projectid;
        private Object audioUrl;
        private String text;
        private String audioTime;
        private String createtime;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getAppid() {
            return appid;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }

        public String getProjectid() {
            return projectid;
        }

        public void setProjectid(String projectid) {
            this.projectid = projectid;
        }

        public Object getAudioUrl() {
            return audioUrl;
        }

        public void setAudioUrl(Object audioUrl) {
            this.audioUrl = audioUrl;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getAudioTime() {
            return audioTime;
        }

        public void setAudioTime(String audioTime) {
            this.audioTime = audioTime;
        }

        public String getCreatetime() {
            return createtime;
        }

        public void setCreatetime(String createtime) {
            this.createtime = createtime;
        }
    }

    @Keep
    public static class TextInfo {

        @Override
        public String toString() {
            return "TextInfo{" +
                    "start=" + start +
                    ", end=" + end +
                    ", text='" + text + '\'' +
                    '}';
        }

        public TextInfo(float start, float end, String text) {
            this.start = start;
            this.end = end;
            this.text = text;
        }

        public float getStart() {
            return start;
        }

        public float getEnd() {
            return end;
        }

        public String getText() {
            return text;
        }

        private float start, end; //单位：秒
        private String text;
    }


    public List<TextInfo> getList() {
        if (null == data || TextUtils.isEmpty(data.text)) {
            return null;
        }

        List<TextInfo> list = new ArrayList<>();
        String text = new String(data.text);
        if (text.contains("[")) {
            String startChr = "<";
            String endChr = ">";
            text = text.replace("[", startChr);
            text = text.replace("]", endChr);
            String[] arr = text.split(startChr);
//            Log.e(TAG, "getList: " + text + " arr:" + Arrays.toString(arr));
            if (null != arr) {
                int len = arr.length;
                if (len > 0) {
                    for (int i = 1; i < len; i++) {
                        String line = arr[i];
                        String timeLine = line.substring(0, line.indexOf(endChr));
//                        Log.e(TAG, "getList: " + i + "/" + len + "   >" + timeLine);
                        String arrTimeLine[] = timeLine.split(",");
                        if (null != arrTimeLine && arrTimeLine.length >= 2) {
                            String strStartTime[] = arrTimeLine[0].split(":");
                            String strEndTime[] = arrTimeLine[1].split(":");
                            if (arrTimeLine.length == 3) {
                                //双声道
                                if (TextUtils.equals(arrTimeLine[2], Integer.toString(0))) {
                                    //只保留一个声道的数据
                                    //[0:0.160,0:14.800,1]  事业是啊，李白好像钱您也光你是不上山，举头望明月，低头思故乡。
                                    float startTime = Float.parseFloat(strStartTime[0]) * 60 + Float.parseFloat(strStartTime[1]);
                                    float endTime = Float.parseFloat(strEndTime[0]) * 60 + Float.parseFloat(strEndTime[1]);
                                    list.add(new TextInfo(startTime, endTime, line.substring(line.indexOf(endChr) + 1).trim()));
                                }
                            } else {
                                //[0:0.160,0:14.800,1]  事业是啊，李白好像钱您也光你是不上山，举头望明月，低头思故乡。
                                float startTime = Float.parseFloat(strStartTime[0]) * 60 + Float.parseFloat(strStartTime[1]);
                                float endTime = Float.parseFloat(strEndTime[0]) * 60 + Float.parseFloat(strEndTime[1]);
                                list.add(new TextInfo(startTime, endTime, line.substring(line.indexOf(endChr) + 1).trim()));
                            }
                        }
                    }
                }
            }
        }

        return list;


    }

    /**
     * 字说自绘语音转文字
     * @return
     */
    public List<TextNode> getTextNode() {
        if (null == data || TextUtils.isEmpty(data.text)) {
            return null;
        }

        List<TextNode> list = new ArrayList<>();
        String text = new String(data.text);
        if (text.contains("[")) {
            String startChr = "<";
            String endChr = ">";
            text = text.replace("[", startChr);
            text = text.replace("]", endChr);
            String[] arr = text.split(startChr);
//            Log.e(TAG, "getList: " + text + " arr:" + Arrays.toString(arr));
            if (null != arr) {
                int len = arr.length;
                if (len > 0) {
                    for (int i = 1; i < len; i++) {
                        String line = arr[i];
                        String timeLine = line.substring(0, line.indexOf(endChr));
//                        Log.e(TAG, "getList: " + i + "/" + len + "   >" + timeLine);
                        String arrTimeLine[] = timeLine.split(",");
                        if (null != arrTimeLine && arrTimeLine.length >= 2) {
                            String strStartTime[] = arrTimeLine[0].split(":");
                            String strEndTime[] = arrTimeLine[1].split(":");
                            if (arrTimeLine.length == 3) {
                                //双声道
                                if (TextUtils.equals(arrTimeLine[2], Integer.toString(0))) {
                                    //只保留一个声道的数据
                                    //[0:0.160,0:14.800,1]  事业是啊，李白好像钱您也光你是不上山，举头望明月，低头思故乡。
                                    float startTime = Float.parseFloat(strStartTime[0]) * 60 + Float.parseFloat(strStartTime[1]);
                                    float endTime = Float.parseFloat(strEndTime[0]) * 60 + Float.parseFloat(strEndTime[1]);

                                    list.add(new TextNode(startTime, endTime, line.substring(line.indexOf(endChr) + 1).trim()));
                                }
                            } else {
                                //[0:0.160,0:14.800,1]  事业是啊，李白好像钱您也光你是不上山，举头望明月，低头思故乡。
                                float startTime = Float.parseFloat(strStartTime[0]) * 60 + Float.parseFloat(strStartTime[1]);
                                float endTime = Float.parseFloat(strEndTime[0]) * 60 + Float.parseFloat(strEndTime[1]);
                                list.add(new TextNode(startTime, endTime, line.substring(line.indexOf(endChr) + 1).trim()));
                            }
                        }
                    }
                }
            }
        }
        return list;
    }

}
