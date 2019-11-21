package com.rd.veuisdk.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.Music;
import com.rd.vecore.RdVECore;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.DewatermarkObject;
import com.rd.vecore.models.EffectInfo;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MusicFilterType;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.Trailer;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.vecore.models.Watermark;
import com.rd.vecore.models.caption.CaptionLiteObject;
import com.rd.vecore.models.caption.CaptionObject;
import com.rd.veuisdk.IShortVideoInfo;
import com.rd.veuisdk.R;
import com.rd.veuisdk.fragment.AudioInfo;
import com.rd.veuisdk.manager.ExportConfiguration;
import com.rd.veuisdk.manager.TextWatermarkBuilder;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.CollageInfo;
import com.rd.veuisdk.model.GraffitiInfo;
import com.rd.veuisdk.model.ShortVideoInfoImp;
import com.rd.veuisdk.model.SoundInfo;
import com.rd.veuisdk.model.VideoOb;

import java.util.ArrayList;
import java.util.List;

/**
 * 导出辅助(草稿箱和VideoEditActivity共用)
 *
 * @author JIAN
 * @create 2018/11/9
 * @Describe
 */
public class ExportHandler {
    private VirtualVideo mVirtualVideoSave;
    private Context mContext;

    public boolean isHWCodecEnabled() {
        return mHWCodecEnabled;
    }

    /**
     * 硬编失败，VideoEditActivity重试，软编
     *
     * @param HWCodecEnabled
     */
    public void setHWCodecEnabled(boolean HWCodecEnabled) {
        mHWCodecEnabled = HWCodecEnabled;
    }

    private boolean mHWCodecEnabled = true;

    /**
     *
     */
    public ExportHandler(Context context) {
        this.mContext = context;
        mHWCodecEnabled = CoreUtils.hasJELLY_BEAN_MR2();
    }


    /**
     * @param virtualVideo
     * @param music           配乐
     * @param audioInfoList   配音
     * @param isRemoveMVMusic 移除mv中的声音
     * @param paramData       音频相关参数
     */
    public static void addMusic(VirtualVideo virtualVideo, Music music, List<Music> audioInfoList, boolean isRemoveMVMusic, IShortParamData paramData) {
        if (music != null) {
            try {
                virtualVideo.addMusic(music);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
        if (null != audioInfoList) {
            int len = audioInfoList.size();
            for (int i = 0; i < len; i++) {
                try {
                    virtualVideo.addMusic(audioInfoList.get(i));
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
        virtualVideo.removeMVMusic(isRemoveMVMusic);
        if (paramData.getSoundEffectId() != MusicFilterType.MUSIC_FILTER_NORMAL.ordinal()) {
            MusicFilterType filterType = MusicFilterType.valueOf(paramData.getSoundEffectId());
            if (null != filterType) {
                //变声||音效
                virtualVideo.setMusicFilter(filterType, filterType == MusicFilterType.MUSIC_FILTER_CUSTOM ? paramData.getMusicPitch() : 0);
            }
        }
    }

    public interface ExportVideoSizeListener {
        void onCancel();

        /**
         * 是否需要保存为草稿
         *
         * @param saveToDraft true需要存为草稿 ；false 不用保存
         */
        void onContinue(boolean saveToDraft);
    }

    private static int mExportMaxVideoSize;

    public static void showExportVideoSizeDialog(Context context, final ExportVideoSizeListener listener, boolean hasDraft) {
        final Dialog customizeDialog = new Dialog(context, R.style.dialog);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.video_size_dialog, null);
        customizeDialog.setContentView(dialogView);
        customizeDialog.show();
        final CheckBox cbSaveDraft = dialogView.findViewById(R.id.cbSaveDraft);
        if (hasDraft) {
            cbSaveDraft.setText(R.string.draft_still_saved);
        } else {
            cbSaveDraft.setText(R.string.export_with_draft);
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.tv480p) {
                    mExportMaxVideoSize = 480;
                } else if (id == R.id.tv720p) {
                    mExportMaxVideoSize = 720;
                } else if (id == R.id.tv1080p) {
                    mExportMaxVideoSize = 1080;
                }
                listener.onContinue(cbSaveDraft.isChecked());
                customizeDialog.cancel();
            }
        };
        dialogView.findViewById(R.id.tv480p).setOnClickListener(clickListener);
        dialogView.findViewById(R.id.tv720p).setOnClickListener(clickListener);
        dialogView.findViewById(R.id.tv1080p).setOnClickListener(clickListener);

    }

    private String mStrCustomWatermarkTempPath;


    /**
     * 草稿箱视频导出
     *
     * @return 视频路径
     */
    public String export(VirtualVideo virtualVideo, IShortVideoInfo info, final ExportListener exportListener, boolean withWatermark) throws InvalidArgumentException {
        ShortVideoInfoImp imp = (ShortVideoInfoImp) info;
        if (!imp.isExit()) {
            throw new InvalidArgumentException("MediaObject is deleted...");
        }

        List<CaptionObject> mListCaptions = null;
        if (null != imp.getWordInfoList()) {
            int len = imp.getWordInfoList().size();
            mListCaptions = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                mListCaptions.add(imp.getWordInfoList().get(i).getCaptionObject());
            }
        }
        List<CaptionLiteObject> mTempSpecials = null;
        if (null != imp.getRSpecialInfos()) {
            int len = imp.getRSpecialInfos().size();
            mTempSpecials = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                ArrayList<CaptionLiteObject> tmp = imp.getRSpecialInfos().get(i).getList();
                if (null != tmp) {
                    mTempSpecials.addAll(tmp);
                }
            }
        }

        //去水印|马赛克
        List<DewatermarkObject> markList = null;
        if (null != imp.getMOInfos()) {
            int len = imp.getMOInfos().size();
            markList = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                markList.add(imp.getMOInfos().get(i).getObject());
            }
        }

        mVirtualVideoSave = virtualVideo;

        return export(virtualVideo, imp.getSceneList(), imp.getMVId(), imp.isRemoveMVMusic(), imp.getUIConfiguration(), mListCaptions, mTempSpecials, imp.getMusic()
                , imp.getAudioInfos(), imp.getSoundInfos(), imp.getMusicInfos(), imp.getExportConfiguration(), imp.getLookupConfig(), imp.getFilterId(), imp.getCurProportion(), new ExportListener() {
                    @Override
                    public void onExportStart() {
                        exportListener.onExportStart();
                    }

                    @Override
                    public boolean onExporting(int progress, int max) {
                        return exportListener.onExporting(progress, max);
                    }

                    @Override
                    public void onExportEnd(int result) {
                        exportListener.onExportEnd(result);
                    }
                }, withWatermark, imp.getEffectInfos(),
                markList, imp.getCollageInfos(), -1, imp);

    }


    /**
     * @return 视频的完整路径
     */
    public String export(ArrayList<Scene> alReloadScenes, IParamDataImp iMenuImp, UIConfiguration mUIConfig, List<CaptionObject> mListCaptions, List<CaptionLiteObject> specailList,
                         Music music, List<AudioInfo> audioInfos, List<SoundInfo> soundInfos, List<SoundInfo> musicInfos, ExportConfiguration exportConfig
            , float mCurProportion, ExportListener exportListener, boolean withWatermark, ArrayList<EffectInfo> effectInfos, List<DewatermarkObject> markList, List<CollageInfo> collageInfos,
                         int backgroundColor, IShortParamData paramData) {

        mVirtualVideoSave = new VirtualVideo();
        return export(mVirtualVideoSave, alReloadScenes, iMenuImp.getMVId(), iMenuImp.isRemoveMVMusic(), mUIConfig, mListCaptions, specailList,
                music, audioInfos, soundInfos, musicInfos, exportConfig, iMenuImp.getLookupConfig()
                , iMenuImp.getCurrentFilterType(), mCurProportion, exportListener, withWatermark, effectInfos, markList, collageInfos,
                backgroundColor, paramData);


    }


    /**
     * build || 导出 || 增强模式截图 时  (添加可见的资源，不包含声音)
     */
    public static void addDataSouce(VirtualVideo virtualVideo, List<Scene> alReloadScenes, ArrayList<EffectInfo> mEffectInfos, int mvId, boolean enableTitlingAndSpecialEffectOuter,
                                    List<CaptionObject> mListCaptions, List<CaptionLiteObject> specialList, List<DewatermarkObject> markList, VisualFilterConfig lookupConfig,
                                    int filterId, List<CollageInfo> collageInfos, IShortParamData paramData) {
        for (Scene scene : alReloadScenes) {
            virtualVideo.addScene(scene);
        }
        //特效
        if (mEffectInfos != null && mEffectInfos.size() > 0) {
            ExportHandler.updateEffects(virtualVideo, mEffectInfos);
        }
        //mv必须在字幕\贴纸之前加载
        if (mvId != RdVECore.DEFAULT_MV_ID) {
            virtualVideo.setMV(mvId);
        }
        virtualVideo.setEnableTitlingAndSpEffectOuter(enableTitlingAndSpecialEffectOuter);


        //字幕
        if (null != mListCaptions) {
            //字幕编辑时，直接操作的是CaptionObject
            for (CaptionObject captionObject : mListCaptions) {
                virtualVideo.addCaption(captionObject);
            }
        }

        if (null != specialList) {
            //!贴纸编辑中
            for (CaptionLiteObject liteObject : specialList) {
                virtualVideo.addSubtitle(liteObject);
            }
        }
        if (null != paramData.getGraffitiList()) {
            //涂鸦
            for (GraffitiInfo graffitiInfo : paramData.getGraffitiList()) {
                virtualVideo.addSubtitle(graffitiInfo.getLiteObject());
            }
        }
        //马赛克|去水印
        if (null != markList) {
            //马赛克编辑时，直接操作的是DewatermarkObject
            int len = markList.size();
            for (int i = 0; i < len; i++) {
                virtualVideo.addDewatermark(markList.get(i));

            }
        }

        //设置整体滤镜
        if (null != lookupConfig) {
            try {
                virtualVideo.changeFilter(lookupConfig);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        } else {
            virtualVideo.changeFilter(filterId);
        }
        //退出滤镜编辑时，恢复片段编辑中设置的滤镜
        ExportHandler.restoreMediaFilter(alReloadScenes);

        //画中画
        ExportHandler.loadMix(virtualVideo, collageInfos, Utils.s2ms(virtualVideo.getDuration()));

    }

    /**
     * @return 目标导出文件的路径
     */
    private String export(VirtualVideo virtualVideo, ArrayList<Scene> alReloadScenes, int mvId, boolean mIsRemoveMVMusic, UIConfiguration mUIConfig, List<CaptionObject> mListCaptions,
                          List<CaptionLiteObject> mTempSpecials,
                          Music music, List<AudioInfo> audioInfos, List<SoundInfo> soundInfos, List<SoundInfo> musicInfos, ExportConfiguration mExportConfig,
                          VisualFilterConfig lookupConfig, int mCurrentFilterType, float mCurProportion, final ExportListener exportListener,
                          boolean withWatermark, ArrayList<EffectInfo> effectInfos,
                          List<DewatermarkObject> markList, List<CollageInfo> collageInfos, int backgroudColor, IShortParamData paramData) {

        //可见的媒体
        addDataSouce(virtualVideo, alReloadScenes, effectInfos, mvId, mUIConfig.enableTitlingAndSpecialEffectOuter,
                mListCaptions, mTempSpecials, markList, lookupConfig, mCurrentFilterType, collageInfos, paramData);
        //封面
        if (null != paramData.getCoverCaption()) {
            virtualVideo.addSubtitle(paramData.getCoverCaption());
        }
        //声音
        List<Music> musicList = null;
        if (null != audioInfos) {
            musicList = new ArrayList<>();
            int len = audioInfos.size();
            for (int i = 0; i < len; i++) {
                musicList.add(audioInfos.get(i).getAudio());
            }
        }
        if (soundInfos != null) {
            if (musicList == null) {
                musicList = new ArrayList<>();
            }
            for (SoundInfo s : soundInfos) {
                musicList.add(s.getmMusic());
            }
        }
        if (musicInfos != null) {
            if (musicList == null) {
                musicList = new ArrayList<>();
            }
            for (SoundInfo s : musicInfos) {
                musicList.add(s.getmMusic());
            }
        }
        addMusic(virtualVideo, music, musicList, mIsRemoveMVMusic, paramData);

        //水印
        if (withWatermark) {
            if (mExportConfig.enableTextWatermark) {  // 自定义view水印
                mStrCustomWatermarkTempPath = PathUtils.getTempFileNameForSdcard(mExportConfig.saveDir, "png");
                TextWatermarkBuilder textWatermarkBuilder = new TextWatermarkBuilder(mContext, mStrCustomWatermarkTempPath);
                textWatermarkBuilder.setWatermarkContent(mExportConfig.textWatermarkContent);
                textWatermarkBuilder.setTextSize(mExportConfig.textWatermarkSize);
                textWatermarkBuilder.setTextColor(mExportConfig.textWatermarkColor);
                textWatermarkBuilder.setShowRect(mExportConfig.watermarkShowRectF);
                textWatermarkBuilder.setTextShadowColor(mExportConfig.textWatermarkShadowColor);
                virtualVideo.setWatermark(textWatermarkBuilder);
            } else if (FileUtils.isExist(mContext, mExportConfig.watermarkPath)) {  //图片水印
                Watermark watermark = new Watermark(mExportConfig.watermarkPath);
                if (mExportConfig.watermarkShowRectF != null) {
                    watermark.setShowRect(mExportConfig.watermarkShowRectF);
                    watermark.setUseLayoutRect(false);
                }
                if (mCurProportion > 1) {  //横屏使用横屏水印
                    if (mExportConfig.watermarkLandLayoutRectF != null) {
                        watermark.setShowRect(mExportConfig.watermarkLandLayoutRectF);
                        watermark.setUseLayoutRect(true);
                    } else {
                        if (mExportConfig.watermarkPortLayoutRectF != null) {
                            watermark.setShowRect(mExportConfig.watermarkPortLayoutRectF);
                            watermark.setUseLayoutRect(true);
                        }
                    }
                } else {
                    if (mExportConfig.watermarkPortLayoutRectF != null) {
                        watermark.setShowRect(mExportConfig.watermarkPortLayoutRectF);
                        watermark.setUseLayoutRect(true);
                    } else {
                        if (mExportConfig.watermarkLandLayoutRectF != null) {
                            watermark.setShowRect(mExportConfig.watermarkLandLayoutRectF);
                            watermark.setUseLayoutRect(true);
                        }
                    }
                }
                watermark.setShowMode(mExportConfig.watermarkShowMode);
                virtualVideo.setWatermark(watermark);
            }
        }

        if (FileUtils.isExist(mExportConfig.trailerPath)) {
            //片尾图片
            Trailer trailer = new Trailer(mExportConfig.trailerPath, mExportConfig.trailerDuration, mExportConfig.trailerFadeDuration);
            virtualVideo.setTrailer(trailer);
        }


        VideoConfig vc = new VideoConfig();
        vc.setVideoEncodingBitRate(mExportConfig.getVideoBitratebps());
        vc.setVideoFrameRate(mExportConfig.exportVideoFrameRate);
        vc.enableHWEncoder(mHWCodecEnabled);
        vc.enableHWDecoder(mHWCodecEnabled);
        if (backgroudColor != -1) {
            vc.setBackgroundColor(backgroudColor);
        }

        if (mExportMaxVideoSize == 480 || mExportMaxVideoSize == 720 || mExportMaxVideoSize == 1080) {
            if (mCurProportion > 1) {
                vc.setVideoSize((int) (mExportMaxVideoSize * mCurProportion), mExportMaxVideoSize);
            } else {
                vc.setVideoSize(mExportMaxVideoSize, (int) (mExportMaxVideoSize / mCurProportion));
            }
        } else {
            vc.setAspectRatio(mExportConfig.getVideoMaxWH(), mCurProportion);
        }
        String mStrSaveMp4FileName = PathUtils.getDstFilePath(mExportConfig.saveDir);
        if (mExportConfig.exportVideoDuration != 0) {
            virtualVideo.setExportDuration(mExportConfig.exportVideoDuration);
        }
        virtualVideo.export(mContext, mStrSaveMp4FileName, vc, new ExportListener() {
            @Override
            public void onExportStart() {

                exportListener.onExportStart();
            }

            @Override
            public boolean onExporting(int progress, int max) {
                return exportListener.onExporting(progress, max);
            }

            @Override
            public void onExportEnd(int result) {
                release();
                mExportMaxVideoSize = 0;
                // 删除自定义水印临时文件
                FileUtils.deleteAll(mStrCustomWatermarkTempPath);
                mStrCustomWatermarkTempPath = null;
                exportListener.onExportEnd(result);
            }
        });
        return mStrSaveMp4FileName;
    }

    /**
     * 恢复片段编辑中设置的滤镜
     *
     * @param alReloadScenes
     */
    private static void restoreMediaFilter(List<Scene> alReloadScenes) {
        for (Scene scene : alReloadScenes) {
            List<MediaObject> list = scene.getAllMedia();
            if (null != list) {
                int len = list.size();
                for (int i = 0; i < len; i++) {
                    MediaObject mediaObject = list.get(i);
                    Object tag = mediaObject.getTag();
                    if (null != tag && tag instanceof VideoOb) {
                        VideoOb videoOb = (VideoOb) tag;
                        if (null != videoOb) {
                            IMediaParamImp mediaParamImp = videoOb.getMediaParamImp();
                            if (null != mediaParamImp) {
                                try {
                                    mediaObject.changeFilterList(Utils.getFilterList(mediaParamImp));
                                } catch (InvalidArgumentException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * 导出完毕，响应清理
     */
    private void release() {
        if (null != mVirtualVideoSave) {
            mVirtualVideoSave.release();
            mVirtualVideoSave = null;
        }
    }


    /***
     * 添加特效到虚拟视频
     * 滤镜特效支持实时预览；时间特效必须reload才能生效（反复、慢放、）
     *
     * @param virtualVideo
     * @param list
     */
    public static void updateEffects(VirtualVideo virtualVideo, ArrayList<EffectInfo> list) {
        if (null != list && list.size() > 0) {
            try {
                for (EffectInfo effectInfo : list) {
                    virtualVideo.addEffect(effectInfo);
                }
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }

    }

    private static final String TAG = "ExportHandler";

    /***
     * 加载画中画
     * @param virtualVideo
     * @param mCollageInfos   画中画内容
     * @param duration   主媒体时长 单位：ms
     */
    public static void loadMix(VirtualVideo virtualVideo, final List<CollageInfo> mCollageInfos, int duration) {

        CollageManager.loadMix(virtualVideo, mCollageInfos, duration);
    }


}
