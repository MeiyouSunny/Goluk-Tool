package com.mobnote.t1sp.util;

import android.util.Xml;

import com.mobnote.golukmain.carrecorder.entity.VideoInfo;

import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import cn.com.tiros.debug.GolukDebugUtils;

/**
 * Xml文件列表解析
 */
public class FileXmlParser {

    public static List<VideoInfo> parse(String xmlData) {
        ByteArrayInputStream ios = new ByteArrayInputStream(xmlData.getBytes(Charset.defaultCharset()));

        List<VideoInfo> list = new ArrayList<VideoInfo>();
        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(ios, "UTF-8");
            int event = xmlPullParser.getEventType();

            //FileInfo fileInfo = null;

            VideoInfo videoInfo = null;

            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("file".equals(xmlPullParser.getName())) {
                            //fileInfo = new FileInfo();
                            videoInfo = new VideoInfo();
                        } else if ("name".equals(xmlPullParser.getName())) {
                            //fileInfo.path = xmlPullParser.nextText();
                            //fileInfo.name = fileInfo.path.substring(fileInfo.path.lastIndexOf("/"));
                            videoInfo.videoPath = xmlPullParser.nextText();
                            videoInfo.filename = videoInfo.videoPath.substring(videoInfo.videoPath.lastIndexOf("/") + 1);
                            videoInfo.videoPath = FileUtil.getVideoUrlByPath(videoInfo.videoPath);
                        } else if ("format".equals(xmlPullParser.getName())) {
                            final int attributeCount = xmlPullParser.getAttributeCount();
                            for (int i = 0; i < attributeCount; i++) {
                                String attributeName = xmlPullParser.getAttributeName(i);
                                String attributeValue = xmlPullParser.getAttributeValue(i);
                                if ("size".equals(attributeName)) {
                                    //fileInfo.resolution = attributeValue;
                                } else if ("fps".equals(attributeName)) {
                                    //fileInfo.fps = attributeValue;
                                } else if ("time".equals(attributeName)) {
                                    //fileInfo.videoTime = (int) Float.parseFloat(attributeValue);
                                    videoInfo.countTime = attributeValue;
                                    videoInfo.videoHP = "1080p";
                                }
                            }
                            //fileInfo.format = xmlPullParser.nextText();
                        } else if ("size".equals(xmlPullParser.getName())) {
                            //fileInfo.size = xmlPullParser.nextText();
                            videoInfo.videoSize = Integer.valueOf(xmlPullParser.nextText()) / 1024 / 1024 + "M";
                        } else if ("attr".equals(xmlPullParser.getName())) {
                            //fileInfo.attr = xmlPullParser.nextText();
                        } else if ("time".equals(xmlPullParser.getName())) {
                            //fileInfo.time = xmlPullParser.nextText();
                            videoInfo.videoCreateDate = xmlPullParser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if ("file".equals(xmlPullParser.getName())) {
                            list.add(videoInfo);
                        }
                        break;
                }
                event = xmlPullParser.next();
            }
        } catch (Exception e) {
            GolukDebugUtils.e(Const.LOG_TAG, "Parse remote xml data error：" + e.getMessage());
        }
        return list;
    }

}
