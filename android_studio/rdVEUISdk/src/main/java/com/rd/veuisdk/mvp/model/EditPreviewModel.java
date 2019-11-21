package com.rd.veuisdk.mvp.model;

import android.graphics.RectF;

import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.veuisdk.model.RCInfo;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.utils.IMediaParamImp;
import com.rd.veuisdk.utils.Utils;

import java.util.List;

/**
 * 片段编辑
 */
public class EditPreviewModel {
    /**
     * 应用调色、滤镜到全部片段 (单个媒体)
     *
     * @param mediaObject
     * @param mediaParamImp
     * @param isApplyFilter true  滤镜统一，false 调色统一
     */
    public void applyMediaParamToAll(MediaObject mediaObject, IMediaParamImp mediaParamImp, boolean isApplyFilter) {
        if (null != mediaParamImp && null != mediaObject) {
            Object obj = mediaObject.getTag();

            VideoOb videoOb = null;
            IMediaParamImp tmp = null;
            if (obj instanceof VideoOb) {
                videoOb = (VideoOb) obj;
                IMediaParamImp old = videoOb.getMediaParamImp();
                if (isApplyFilter) {//滤镜
                    if (null != old) {
                        //保证media绑定的原调色不变，只改变滤镜内容的内容
                        tmp = old.clone();
                        tmp.setCurrentFilterType(mediaParamImp.getCurrentFilterType());
                        tmp.setFilterIndex(mediaParamImp.getFilterIndex());
                        tmp.setLookupConfig(mediaParamImp.getLookupConfig());
                    } else {
                        tmp = mediaParamImp.clone();
                    }
                } else { //调色
                    tmp = mediaParamImp.clone();
                    //保证media绑定的滤镜不变，只改变调色的内容
                    if (null != old) {
                        tmp.setCurrentFilterType(old.getCurrentFilterType());
                        tmp.setFilterIndex(old.getFilterIndex());
                        tmp.setLookupConfig(old.getLookupConfig());
                    }
                }
            } else {
                tmp = mediaParamImp.clone();
                videoOb = new VideoOb(mediaObject);
            }
            videoOb.setMediaParamImp(tmp);
            try {
                List<VisualFilterConfig> list = Utils.getFilterList(tmp);
                mediaObject.changeFilterList(list);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
            mediaObject.setTag(videoOb);
        }
    }


    /**
     * 裁剪、旋转-应用到全部
     *
     * @param param 目标样本
     * @param list  全部媒体
     */
    public void fixAllMediaRC(RCInfo param, List<Scene> list) {
        RectF base = param.getClipRectF();
        for (Scene tmp : list) {
            for (MediaObject mediaObject : tmp.getAllMedia()) {
                mediaObject.setAngle(param.getAngle());
                mediaObject.setFlipType(param.getFlipType());
                RectF rectF = null;
                if (null != base) {
                    VideoConfig vc = new VideoConfig();
                    Utils.fixVideoSize(vc, mediaObject);
                    int tmpW = vc.getVideoWidth();
                    int tmpH = vc.getVideoHeight();
                    rectF = new RectF(base.left * tmpW, base.top * tmpH,
                            base.right * tmpW, base.bottom * tmpH);
                } else {
                    //原始比例
                }
                mediaObject.setClipRectF(rectF);
            }
        }
    }
}
