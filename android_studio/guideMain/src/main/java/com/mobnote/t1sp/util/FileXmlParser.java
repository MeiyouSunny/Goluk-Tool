package com.mobnote.t1sp.util;

import android.util.Log;
import android.util.Xml;

import com.mobnote.t1sp.bean.FileInfo;

import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Xml文件列表解析
 */
public class FileXmlParser {

    public static List<FileInfo> parse(String xmlData) {
        ByteArrayInputStream ios = new ByteArrayInputStream(xmlData.getBytes(Charset.defaultCharset()));

        List<FileInfo> list = new ArrayList<FileInfo>();
        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(ios, "UTF-8");
            int event = xmlPullParser.getEventType();

            FileInfo fileInfo = null;

            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("file".equals(xmlPullParser.getName())) {
                            fileInfo = new FileInfo();
                        } else if ("name".equals(xmlPullParser.getName())) {
                            fileInfo.name = xmlPullParser.nextText();
                        } else if ("format".equals(xmlPullParser.getName())) {
                            final int attributeCount = xmlPullParser.getAttributeCount();
                            for (int i = 0; i < attributeCount; i++) {
                                String attributeName = xmlPullParser.getAttributeName(i);
                                String attributeValue = xmlPullParser.getAttributeValue(i);
                                if ("size".equals(attributeName)) {
                                    fileInfo.resolution = attributeValue;
                                } else if ("fps".equals(attributeName)) {
                                    fileInfo.fps = attributeValue;
                                } else if ("time".equals(attributeName)) {
                                    fileInfo.videoTime = (int) Float.parseFloat(attributeValue);
                                }
                            }
                            fileInfo.format = xmlPullParser.nextText();
                        } else if ("size".equals(xmlPullParser.getName())) {
                            fileInfo.size = xmlPullParser.nextText();
                        } else if ("attr".equals(xmlPullParser.getName())) {
                            fileInfo.attr = xmlPullParser.nextText();
                        } else if ("time".equals(xmlPullParser.getName())) {
                            fileInfo.time = xmlPullParser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if ("file".equals(xmlPullParser.getName())) {
                            list.add(fileInfo);
                            fileInfo = null;
                        }
                        break;
                }
                event = xmlPullParser.next();
            }
        } catch (Exception e) {
            Log.e("rss", "Parse xml data error：" + e.getMessage());
        }
        return list;
    }

}
