package com.rd.veuisdk.mix;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.rd.lib.utils.FileUtils;
import com.rd.lib.utils.LogUtil;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.EffectType;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SelectModeActivity;
import com.rd.veuisdk.model.EffectInfo;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.ui.HorizontalProgressDialog;
import com.rd.veuisdk.ui.VideoPreviewLayout;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.RotateUtils;
import com.rd.veuisdk.utils.SysAlertDialog;


/**
 * 演示：编辑单段视频
 * Created by JIAN on 2017/8/29.
 */

public class EditItemHandler {
    private final String TAG = "EditItemHandler";

    private VirtualVideoView player;
    private FrameLayout editParent;
    private SeekBar sbarVolume, sbarChannel;

    private int mVolume = 50;
    private int mChannel = 50; //左右声道核心未完成，后期再处理
    private MixInfo mixInfo;
    private Context context;

    public EditItemHandler(FrameLayout editParent, SeekBar sbarVolume, SeekBar sbarChannel) {
        this.editParent = editParent;
        context = editParent.getContext();
        this.sbarVolume = sbarVolume;
        this.sbarChannel = sbarChannel;
        sbarChannel.setMax(100);
        sbarVolume.setMax(100);


    }

    private void initUIListener() {
        sbarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mVolume = progress;
                if (null != mixInfo && null != mixInfo.getMediaObject()) {
                    //设置单个媒体对象的声音大小
                    mixInfo.getMediaObject().setMixFactor(mVolume);
                }
                if (null != mVirtualVideo) {
                    //编辑播放器实时更改预览声音大小
                    mVirtualVideo.setOriginalMixFactor(mVolume);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbarChannel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                mChannel = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private VirtualVideo mVirtualVideo;
    private IMenuLisenter iMenuLisenter;
    private VideoPreviewLayout itemVideoParent;
    private float previewAspectRatio = 1f;

    /***
     * 初始化编辑播放器
     * @param info
     * @param rectF
     * @param ilistener
     */
    public void initData(MixInfo info, RectF rectF, IMenuLisenter ilistener) {
        mVolume = info.getVolumeFactor();
        mChannel = info.getChannelFactor();
        if (null == mVirtualVideo) {
            mVirtualVideo = new VirtualVideo();
        } else {
            mVirtualVideo.reset();
        }

        sbarChannel.setProgress(info.getChannelFactor());
        sbarVolume.setProgress(info.getVolumeFactor());
        Log.e(TAG, "initData: src " + info.toString());
        mixInfo = info.clone();

        //编辑的部分UI待处理
        editParent.setVisibility(View.VISIBLE);
        Log.e(TAG, "initData: " + mixInfo.toString() + "..." + rectF.toShortString());
        iMenuLisenter = ilistener;
        Context context = editParent.getContext();
        //每次只能new player,不能写在布局中 ，防止openggl 重叠
        this.player = new VirtualVideoView(context, null);
        player.setZOrderOnTop(true);//必须
        player.setZOrderMediaOverlay(true);//必须，防止重叠时当前gl被遮挡，
        //设置播放器的预览比例，必须，
        previewAspectRatio = (480 * rectF.width()) / ((480.0f / SelectModeActivity.ASP_RATION) * rectF.height());

        VideoConfig videoConfig = new VideoConfig();

        MediaObject tmp = mixInfo.getMediaObject();
        if (tmp.getClipRectF() == null || tmp.getClipRectF().isEmpty()) {
            VirtualVideo.getMediaInfo(tmp.getMediaPath(), videoConfig);
            Rect clipRect = new Rect();
            int tmpW = videoConfig.getVideoWidth(), tmpH = videoConfig.getVideoHeight();
            //旋转之后宽高交替
            if (tmp.getAngle() == 270 || tmp.getAngle() == 90) {
                tmpW = videoConfig.getVideoHeight();
                tmpH = videoConfig.getVideoWidth();
            }
            ModeUtils.getClipSrc(tmpW, tmpH, previewAspectRatio, clipRect, EditItemHandler.POFF);

            if (!clipRect.isEmpty()) {
                Log.e(TAG, "oldxxx:" + tmp.getClipRectF().toShortString() + "..." + previewAspectRatio + "reLoad: " + clipRect.toShortString() + "..." + tmp.getAngle());

                RectF mRectVideoClipBound = new RectF(clipRect.left, clipRect.top, clipRect.right, clipRect.bottom);

                RotateUtils.fixRotate(tmpW, tmpH, tmp.getAngle(), tmp.getFlipType(), mRectVideoClipBound);

                Log.e(TAG, "initData: --clip out>>" + mRectVideoClipBound.toShortString());
                tmp.setClipRectF(mRectVideoClipBound);
            }
        } else {
            Log.e(TAG, "initData: clip" + tmp.getClipRectF().toShortString());
        }
        tmp.setShowRectF(new RectF(0, 0, 1f, 1f));
        player.setPreviewAspectRatio(previewAspectRatio);
        itemVideoParent = new VideoPreviewLayout(context, null);
        itemVideoParent.setCheck(true);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        editParent.addView(itemVideoParent, lp);
        //新隐藏，等layout 加载完成再显示，防止UI闪烁
        itemVideoParent.setVisibility(View.INVISIBLE);
        itemVideoParent.setCustomRect(rectF);
        itemVideoParent.addView(player, lp);
        FrameLayout.LayoutParams lpGallery = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpGallery.gravity = (Gravity.BOTTOM | Gravity.LEFT);
        FrameLayout.LayoutParams lpScreen = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpScreen.gravity = (Gravity.BOTTOM | Gravity.RIGHT);
        ImageView del = new ImageView(context, null);
        del.setImageResource(R.drawable.btn_mix_del);
        del.setId(("del").hashCode());
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: del edit");
                if (null != iMenuLisenter) {
                    iMenuLisenter.onEditDelete();
                }


            }
        });
        itemVideoParent.addView(del, lpGallery);



        initPlayerListener(this.player);
        player.reset();
        player.setAspectRatioFitMode(AspectRatioFitMode.IGNORE_ASPECTRATIO);
        reLoad();
        try {
            mVirtualVideo.build(player);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
        initUIListener();
    }


    public String getOutPath() {
        return exportPath;
    }

    public String getSrcVideo() {
        return mixInfo.getMediaObject().getMediaPath();
    }


    public static final float POFF = 0f;

    public void onActivityEditResultOK(float nTrimStart, float nTrimEnd) {
        MediaObject tmp = mixInfo.getMediaObject();
        tmp.setTimeRange(nTrimStart, nTrimEnd);
        VideoOb tOb = (VideoOb) tmp.getTag();
        tOb.rStart = nTrimStart;
        tOb.rEnd = nTrimEnd;
        tOb.nStart = nTrimStart;
        tOb.nEnd = nTrimEnd;
        tmp.setTag(tOb);
        Log.e(TAG, "onActivityEditResultOK: " + nTrimStart + ".《》" + nTrimEnd + "...");

        reLoad();
        try {
            mVirtualVideo.build(player);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    public void onActivityEditResultOKMirror(MediaObject media) {
        mixInfo.setMediaObject(media);

        Log.e(TAG, "onActivityEditResultOKMirror: " + mixInfo.getMediaObject().getFlipType() + "..." + media.getClipRectF().toShortString() + "angle" + media.getAngle());

        reLoad();
        try {
            mVirtualVideo.build(player);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    public MediaObject getEditMedia() {
        return mixInfo.getMediaObject();


    }


    /**
     * 重新加载
     */
    private void reLoad() {
        mVirtualVideo.reset();
        if (mixInfo.isExistsVideo()) {
            Scene scene = VirtualVideo.createScene();
            MediaObject mMediaObject = mixInfo.getMediaObject();


            scene.addMedia(mMediaObject);
            Log.e(TAG, "reLoad: " + "////" + mMediaObject.getTrimStart() + "<>" + mMediaObject.getTrimEnd() + ".....>" + mMediaObject.getDuration() + "view asp:" + previewAspectRatio + "..." + mMediaObject.getClipRectF().toShortString() + ".." + mMediaObject.getFlipType());
            mMediaObject.setMixFactor(mVolume);
            mMediaObject.setAudioMute(false);

            mMediaObject.setAspectRatioFitMode(mixInfo.getAspectRatioFitMode());
            mVirtualVideo.addScene(scene);
            if (mFilterType != 0) {
                //滤镜
                mVirtualVideo.changeFilter(mFilterType);
            }
            try {
                if (null != mEffectInfo) {
                    //添加一个特效
                    mVirtualVideo.addEffect(mEffectInfo.getEffectType(), mEffectInfo.getStartTime(), mEffectInfo.getEndTime());
                }
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "reLoad:  no video");
        }


    }

    //导出的临时视频文件路径
    private String exportPath = null;


    public float getPlayerDuration() {
        return (null != player) ? player.getDuration() : 0.1f;
    }

    public void seekto(float seekto) {
        if (null != player && bPrepared) {
            player.seekTo(seekto);
        }
    }

    /***
     * 编辑->"完成"按钮
     * */
    public interface IExportLisener {
        /**
         * 生成临时文件的回调
         *
         * @param sucess
         */
        public void onExportEnd(boolean sucess);

        /**
         * 不需要生成临时文件的回调
         */
        public void onEnd(MixInfo info);
    }


    public interface IMenuLisenter extends VirtualVideoView.VideoViewListener {


        //编辑->删除
        public void onEditDelete();


    }

    private EffectInfo mEffectInfo;
    private int mFilterType = 0;

    /**
     * 演示->新增滤镜
     */
    public void onAddMixFilter() {
        //随机设置滤镜
        mFilterType = (int) (Math.random() * 10);
        if (null != player) {
            player.stop();
            player.reset();
            player.setAspectRatioFitMode(AspectRatioFitMode.IGNORE_ASPECTRATIO);
            reLoad();
            try {
                mVirtualVideo.build(player);
            } catch (InvalidStateException e) {
                e.printStackTrace();
            }
            start();
        }

    }

    private float duration;

    /**
     * 演示->新增特效
     */
    public void onAddEffect() {


        if (null != player) {
            player.stop();
            mEffectInfo = new EffectInfo();
            mEffectInfo.setEffectType(EffectType.SPOTLIGHT);
            mEffectInfo.setTimeRange(0, duration / 2);
            player.reset();
            player.setAspectRatioFitMode(AspectRatioFitMode.IGNORE_ASPECTRATIO);
            reLoad();
            try {
                mVirtualVideo.build(player);
            } catch (InvalidStateException e) {
                e.printStackTrace();
            }
            start();
        }


    }

    /**
     * 演示：改变声道
     */
    public void onChangeChannel() {
        if (null != player) {
            player.stop();
            player.reset();
            player.setAspectRatioFitMode(AspectRatioFitMode.IGNORE_ASPECTRATIO);
            reLoad();
            try {
                mVirtualVideo.build(player);
            } catch (InvalidStateException e) {
                e.printStackTrace();
            }
            start();
        }
    }

    private float lastPosition = -1f;

    public void onResume() {
        if (bPrepared && -1 != lastPosition) {
            player.seekTo(lastPosition);
        }
    }

    private boolean bPrepared = false;

    public void onPause() {
        if (bPrepared && null != player) {
            if (player.isPlaying()) {
                player.pause();
            }
            lastPosition = player.getCurrentPosition();
        } else {
            lastPosition = -1f;
        }
    }

    /**
     * 播放
     */
    public void startPlay() {
        if (bPrepared && null != player) {
            if (!player.isPlaying()) {
                player.start();
            }
        }
    }


    private IExportLisener mIExportLisener;
    private final int MAXSIZE = 480;

    /**
     * 点击编辑->完成,导出视频
     *
     * @param listener
     */
    public void onExportVideo(IExportLisener listener) {
        player.stop();
        mIExportLisener = listener;
        if (mEffectInfo != null || mFilterType != 0) {
            //参数有变化，生成临时视频文件
            VideoConfig videoConfig = new VideoConfig();


            VirtualVideo.getMediaInfo(mixInfo.getMediaObject().getMediaPath(), videoConfig);

            //设置最大输出视频尺寸
            Rect clipRect = new Rect();
            ModeUtils.getClipSrc(videoConfig.getVideoWidth(), videoConfig.getVideoHeight(), previewAspectRatio, clipRect, EditItemHandler.POFF);
            int targetW = MAXSIZE;
            int targetH = MAXSIZE;

            if (!clipRect.isEmpty()) {
                Log.e(TAG, "onExportVideo: " + clipRect.toShortString());
                //尽可能的使用小尺寸的视频
                if (previewAspectRatio > 1f) {
                    targetW = Math.min(MAXSIZE, clipRect.width());
                    targetH = (int) (targetW / previewAspectRatio);
                } else {
                    targetH = Math.min(MAXSIZE, clipRect.height());
                    targetW = (int) (targetH * previewAspectRatio);
                }
            } else {
                if (previewAspectRatio > 1f) {
                    targetW = MAXSIZE;
                    targetH = (int) (targetW / previewAspectRatio);
                } else {
                    targetH = MAXSIZE;
                    targetW = (int) (targetH * previewAspectRatio);
                }
            }
            Log.e(TAG, "onExportVideo: " + targetW + "*" + targetH + "....." + (targetW / (targetH + 0.0f)) + "..." + previewAspectRatio + "..." + mixInfo.getMediaObject().getClipRectF().toShortString() + "clip:" + mixInfo.getMediaObject().getWidth() + "*" + mixInfo.getMediaObject().getHeight());
            //得到要输出的size
            videoConfig.setVideoSize(targetW, targetH);
            //必须设置比例
            videoConfig.setAspectRatio(previewAspectRatio);
            videoConfig.setVideoEncodingBitRate(Math.min(videoConfig.getVideoEncodingBitRate(), 4000 * 1000));
            videoConfig.setVideoFrameRate(Math.min(24, videoConfig.getVideoFrameRate()));
            reLoad();
            exportPath = PathUtils.getTempFileNameForSdcard(PathUtils.TEMP_MIX_EDIT, "mp4");
            mVirtualVideo.export(context, exportPath, videoConfig, new ExportListener() {
                HorizontalProgressDialog dialog;

                @Override
                public void onExportStart() {
                    dialog = SysAlertDialog.showHoriProgressDialog(
                            context, context.getString(R.string.isloading),
                            false, false, new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {

                                }
                            });
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }

                @Override
                public boolean onExporting(int progress, int max) {
                    dialog.setMax(max);
                    dialog.setProgress(progress);
//                    Log.e(TAG, "onExporting: ." + progress + "..." + max);
                    return true;
                }

                @Override
                public void onExportEnd(int nResult) {
                    Log.e(TAG, "onExportEnd: " + nResult);
                    if (null != dialog) {
                        dialog.cancel();
                        dialog = null;
                    }
                    if (nResult >= VirtualVideo.RESULT_SUCCESS) {
                        mixInfo.setVolumeFactor(50);
                        if (null != mIExportLisener) {
                            mIExportLisener.onExportEnd(true);
                        }
                    } else {
                        LogUtil.e("Export failed,result:" + nResult);
                        if (null != mIExportLisener) {
                            mIExportLisener.onExportEnd(false);
                        }
                        FileUtils.deleteAll(exportPath);//清除失败的临时文件
                    }
                }
            });

        } else {
            if (null != mIExportLisener) {
                //仅更改了音量，不需要生成临时文件
                mixInfo.setVolumeFactor(mVolume);
                mIExportLisener.onEnd(mixInfo);
            }
        }

    }


    /**
     * 移除编辑播放器
     */
    public void removeEditUI() {
        sbarVolume.setOnSeekBarChangeListener(null);
        sbarChannel.setOnSeekBarChangeListener(null);
        exportPath = null;
        mEffectInfo = null;
        mFilterType = 0;
        Log.e(TAG, "removeEditUI: ");
        if (player != null) {
            player.setOnPlaybackListener(null);
            player.cleanUp();
            player = null;
        }
        editParent.removeAllViews();
        if (null != mVirtualVideo) {
            mVirtualVideo.release();//清空当前
            mVirtualVideo = null;
        }
    }

    /**
     * 播放按钮
     */
    public void onStartClick() {
        if (player != null) {
            if (!player.isPlaying()) {
                player.start();
            } else {
                player.pause();
            }
        }
    }

    /**
     * 编辑播放器start()
     */
    private void start() {
        if (player != null && !player.isPlaying()) {
            player.start();
        }
    }

    /**
     * 编辑播放器stop()
     */
    public void stop() {
        if (player != null && player.isPlaying()) {
            player.stop();
        }
    }

    /**
     * 播放器初始化成功
     */
    public void setPlayerPrepared() {
        bPrepared = true;
    }

    /**
     * 注册播放器回调
     *
     * @param player
     */
    private void initPlayerListener(final VirtualVideoView player) {
        player.setOnPlaybackListener((VirtualVideoView.VideoViewListener) iMenuLisenter);
        player.setOnInfoListener(new VirtualVideo.OnInfoListener() {
            @Override
            public boolean onInfo(int what, int extra, Object obj) {
                Log.e(TAG, "-onInfo: " + what + "..." + extra + "..." + obj);
                return true;
            }
        });


    }

}
