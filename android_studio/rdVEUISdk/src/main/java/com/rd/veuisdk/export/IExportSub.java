package com.rd.veuisdk.export;

import com.rd.vecore.models.SubtitleObject;

import java.util.ArrayList;

/**
 * 多线程导出特效字幕
 *
 * @author JIAN
 */
public interface IExportSub {
    /**
     * 字幕绘制完毕 (字幕特效多线程导出)
     */
    void onSub(ArrayList<SubtitleObject> effects);

}
