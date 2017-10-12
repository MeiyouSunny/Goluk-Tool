package com.rd.veuisdk;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.ui.RotateImageView;
import com.rd.lib.ui.RotateRelativeLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.FileUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.recorder.ResultConstants;
import com.rd.recorder.api.IRecorderCallBack;
import com.rd.recorder.api.RecorderConfig;
import com.rd.recorder.api.RecorderCore;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.PermutationMode;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.mix.EditItemHandler;
import com.rd.veuisdk.mix.MixInfo;
import com.rd.veuisdk.mix.MixItemHolder;
import com.rd.veuisdk.mix.ModeUtils;
import com.rd.veuisdk.mix.RecordInfo;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.ui.ExtProgressBar;
import com.rd.veuisdk.ui.HorizontalProgressDialog;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.ui.VideoPreviewLayout;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.rd.vecore.VirtualVideo.getMediaInfo;


/**
 * 预览画中画(选择媒体、录制)
 * <p>
 * 说明：界面中包含两个播放器（预览mix播放器、编辑单个视频时的播放器)
 */
public class MixRecordActivity extends Activity {


    private SeekBar sBarVolume, sBarChannel;
    private String TAG = "MixRecordActivity";
    private static final String SAVE_MIXDATA = "save_mix_data";
    private static final String SAVE_MIX_CAMERA = "save_mix_camera";
    private static final String SAVE_CURRENT_MIXDATA = "save_current_mix_data";
    public static final String PARAM_MODE = "param_mode";
    public static final String PARAM_ASSET_BG = "param_asset_bg";

    private static final int REQUEST_MIX_GALLERY = 1 << 2;//打开图库选折视频
    private static final int THUMB_OVER = 1 << 3;
    private static final int REQUEST_TRIM_CROP = 1 << 4;//裁剪视频
    private static final int REQUEST_EDIT_TRIM = 1 << 5;//编辑截取
    private static final int REQUEST_EDIT_MIRROR = 1 << 6;//编辑编辑
    private static final int CANCEL_EXPORT = 1 << 7;//取消导出


    private FrameLayout mEditPlayerParent;
    private PreviewFrameLayout mPreviewFrame;
    private ExtButton btnMixEditFilter;
    private ExtButton btnMixEditEffect;
    private ExtButton btnMixEditSound;
    private ExtButton btnMixEditTrim;
    private ExtButton btnMixEditEdit;
    private LinearLayout mixEditLayout;
    private ExtButton mBtnNext;
    private TextView mTvTitle;
    private VirtualVideoView player;
    private FrameLayout playerParent;
    private LinearLayout mixMenuLayout;

    private RdSeekBar mRdSeekBar;
    private TextView currentTv, totalTv;
    private RotateRelativeLayout mProgressLayout;
    private String recordVideoPath;
    private ArrayList<RectF> list = new ArrayList<RectF>();
    private String assetBg = "";
    private ArrayList<VideoPreviewLayout> listPreview = new ArrayList<VideoPreviewLayout>();

    private final int DEFAULT_CAMERA_INDEX = -1;
    //记录摄像头画框所在的位置下标 （0-8）
    private int usedCameraIndex = DEFAULT_CAMERA_INDEX;
    private VideoPreviewLayout lastChecked = null;
    private ArrayList<MixItemHolder> holderList = new ArrayList<MixItemHolder>();
    private MixInfo currentMix = null;
    private List<RecordInfo> recordingList = null;
    private RotateImageView delRecordItem;
    private ExtProgressBar recordBar;

    private void initView() {
        recordBar = (ExtProgressBar) findViewById(R.id.record_bar);
        recordBar.setDuration(Math.max(60000, (int) (maxDuration * 1000)));
        recordBar.setInterval(3000, recordBar.getDuration());


        mContent = findViewById(android.R.id.content);
        mProgressLayout = (RotateRelativeLayout) findViewById(R.id.rlPlayerBottomMenu);
        mRdSeekBar = (RdSeekBar) findViewById(R.id.sbEditor);
        currentTv = (TextView) findViewById(R.id.tvCurTime);
        totalTv = (TextView) findViewById(R.id.tvTotalTime);
        sBarVolume = (SeekBar) findViewById(R.id.sb_volume);
        sBarChannel = (SeekBar) findViewById(R.id.sb_channel);
        mEditPlayerParent = (FrameLayout) findViewById(R.id.editPlayerParent);
        mPreviewFrame = (PreviewFrameLayout) findViewById(R.id.previewFrame);
        btnMixEditFilter = (ExtButton) findViewById(R.id.btn_mix_edit_filter);
        btnMixEditEffect = (ExtButton) findViewById(R.id.btn_mix_edit_effect);
        btnMixEditSound = (ExtButton) findViewById(R.id.btn_mix_edit_sound);
        btnMixEditTrim = (ExtButton) findViewById(R.id.btn_mix_edit_trim);
        btnMixEditEdit = (ExtButton) findViewById(R.id.btn_mix_edit_edit);
        mixEditLayout = (LinearLayout) findViewById(R.id.mix_edit_layout);
        mBtnNext = (ExtButton) findViewById(R.id.btnNext);
        mTvTitle = (TextView) findViewById(R.id.tvTitle);
        player = (VirtualVideoView) findViewById(R.id.palyer);
        playerParent = (FrameLayout) findViewById(R.id.playerParent);
        mixMenuLayout = (LinearLayout) findViewById(R.id.mix_menu_layout);
        delRecordItem = (RotateImageView) findViewById(R.id.del);
        delRecordItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (null != recordingList) {
                    int len = recordingList.size();
                    if (len > 0) {
                        recordingList.remove(len - 1);
                        recordBar.removeLastItem();
                        len = recordingList.size();
                        if (len > 0) {
                            delRecordItem.setVisibility(View.VISIBLE);
                        } else {
                            delRecordItem.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
        btnMixEditFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnEditFilterClicked();
            }
        });
        btnMixEditEffect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnEditEffectClicked();
            }
        });
        btnMixEditSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnEditSoundClicked();
            }
        });
        btnMixEditTrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnEditTrim();
            }
        });
        btnMixEditEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnEditEdit();
            }
        });


        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMBtnBackClicked();
            }
        });
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMBtnNextClicked();
            }
        });


        mRdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    float p = Utils.ms2s(progress);
                    if (isEditIng) {
                        editHandler.seekto(p);
                    } else {
                        player.seekTo(p);
                    }
                    currentTv.setText(getFormatTime(progress));
                }
            }

            private boolean isPlaying = false;

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                if (isEditIng) {
                    editHandler.onPause();
                } else {
                    if ((isPlaying = player.isPlaying())) {
                        isPlaying = true;
                        player.pause();
                    }
                }
                forceStopRecord();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isEditIng) {
                    editHandler.startPlay();
                } else {
                    if (isPlaying) {
                        player.start();
                        onResetPlayState(true);
                    }
                }
            }
        });
    }

    //强制停止录制
    private void forceStopRecord() {
        if (RecorderCore.isRecording() || RecorderCore.isPreparing()) {
            RecorderCore.stopRecord();
        }
    }


    private int getAddBtnId(int index) {
        return ("video" + index).hashCode();
    }

    /***
     * 初始化多个画框
     */
    private void initVideoUI() {
        listPreview.clear();
        holderList.clear();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        FrameLayout.LayoutParams lpGallery = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpGallery.gravity = (Gravity.BOTTOM | Gravity.LEFT);
        FrameLayout.LayoutParams lpScreen = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpScreen.gravity = (Gravity.BOTTOM | Gravity.RIGHT);
        FrameLayout.LayoutParams lpAdd = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpAdd.gravity = (Gravity.CENTER);

        //初始化一个播放器
        int len = list.size();
        //每一个画框占一层Frame，且在单个画框中设置新增、图库、全屏按钮,画框的位置依据单个rectF指定的区域（VideoPreviewLayout.setTargetSize()）

        for (int i = 0; i < len; i++) {
            final RectF temp = list.get(i);

            MixItemHolder holder = new MixItemHolder();
            holder.setMixId(ModeUtils.getRect2Id(temp));
            MixInfo info = new MixInfo(temp);
            info.setState(MixInfo.OTHER_VIDEO);
            holder.setBindMix(info);

            final VideoPreviewLayout itemVideoParent = new VideoPreviewLayout(playerParent.getContext(), null);
            itemVideoParent.setHolder(holder);//绑定holder
            itemVideoParent.setClearBorderLine(true);


            itemVideoParent.setId(i);
            playerParent.addView(itemVideoParent, lp);
            final ImageView add = new ImageView(this, null);
            add.setImageResource(R.drawable.btn_mix_add);
            add.setId(getAddBtnId(i));
            holder.setBtnAdd(add);

            final int tIndex = i;
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onRemoveEditUI();
                    int mixId = ModeUtils.getRect2Id(temp);
                    MixInfo info = getMixData(mixId);
                    onPlayerStop();
                    if (null != info && info.isExistsVideo()) {
                        //已经绑定一个视频， 点击响应编辑视频
                        lastChecked = itemVideoParent;
                        currentMix = info;
                        onEditItemData();


                    } else {
                        MixItemHolder tempHolder = getMixHolder(ModeUtils.getRect2Id(temp));
                        recordingList = new ArrayList<RecordInfo>();
                        //初始化相机
                        initCameraLayout(itemVideoParent);
                        lastChecked = itemVideoParent;
                        info.setState(MixInfo.RECORD_VIDEO);
                        currentMix = info;
                        if (null != tempHolder) {
                            tempHolder.setBindMix(currentMix);
                            tempHolder.getBtnFullScreen().setVisibility(View.VISIBLE);
                        }
                        // 只能添加单个相机  ，其他mix画框中的add按钮隐藏
                        onResetAddState(mixId, true);
                        usedCameraIndex = tIndex;
                    }


                }
            });

            itemVideoParent.setCustomRect(temp);
            itemVideoParent.addView(add, lpAdd);
            ImageView gallery = new ImageView(this, null);
            gallery.setImageResource(R.drawable.btn_mix_gallery);
            gallery.setId(("galleryOdel" + i).hashCode());
            onGalleryODelLisenter ondel = new onGalleryODelLisenter(itemVideoParent, temp);
            gallery.setOnClickListener(ondel);
            itemVideoParent.addView(gallery, lpGallery);

            ImageView screen = new ImageView(this, null);
            screen.setImageResource(R.drawable.btn_mix_fullscreen);
            screen.setId(("screen" + i).hashCode());
            screen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lastChecked = itemVideoParent;
                    if (null != lastChecked) {
                        onPlayerStop();
                        startfull();
                    }
                }
            });
            screen.setVisibility(View.GONE);
            holder.setBtnFullScreen(screen);
            itemVideoParent.addView(screen, lpScreen);

            listPreview.add(itemVideoParent);
            itemVideoParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (null != lastChecked && lastChecked.getId() != view.getId()) {
                        lastChecked.setCheck(false);
                    }
                    VideoPreviewLayout current = (VideoPreviewLayout) view;
                    current.setCheck(true);
                    lastChecked = current;
                }

            });

            holder.setBtnGallery(gallery);
            holderList.add(holder);
            if (usedCameraIndex == i) {//Activity销毁之前是否已打开摄像头，是否需要还原摄像头画面
                initCameraLayout(itemVideoParent);
            }
        }


    }

    /**
     * 如果正在编辑中，恢复预览状态
     * 其他画框新增单个视频会触发
     */
    private void onRemoveEditUI() {
        if (isVoicing) {
            onVoiceOut();
        }
        if (isEditIng) {
            editHandler.removeEditUI();
            removeEditUI(true);
        }
    }

    //停止预览
    private void onPlayerStop() {
        if (null != player && player.isPlaying()) {
            player.stop();
        }
    }


    /**
     * *  隐藏其他未选折视频的画框中的add按钮
     *
     * @param mixId
     * @param isAddOdel true初始化摄像头，后隐藏其他画框中的add按钮，flase 删除当前中的元素
     */
    private void onResetAddState(int mixId, boolean isAddOdel) {
        int len = holderList.size();
        MixItemHolder holder;
        for (int i = 0; i < len; i++) {
            holder = holderList.get(i);
            ImageView add = holder.getBtnAdd();
            if (holder.getMixId() != mixId) {

                if (holder.getBindMix().isExistsVideo()) {
                    //图库视频->编辑
                    add.setImageResource(R.drawable.btn_mix_edit);
                    add.setVisibility(View.VISIBLE);
                } else {
                    //其他画框均可选择图库视频
                    add.setImageResource(R.drawable.btn_mix_add);
                    add.setVisibility(View.VISIBLE); //摄像头已录制视频
                }
                holder.getBtnFullScreen().setVisibility(View.GONE);
                holder.getBtnGallery().setImageResource(R.drawable.btn_mix_gallery);
                holder.getBtnGallery().setVisibility(View.VISIBLE);
            } else {
                if (isAddOdel) {
                    add.setVisibility(View.GONE); //已绑定摄像头
                } else {
                    add.setImageResource(R.drawable.btn_mix_add);
                    add.setVisibility(View.VISIBLE); //摄像头已录制视频
                }
            }
        }

    }

    /**
     * 开始播放和结束播放（UI响应按钮状态）
     *
     * @param isPlaying
     */
    private void onResetPlayState(boolean isPlaying) {
        int len = holderList.size();
        MixItemHolder holder;
        for (int i = 0; i < len; i++) {
            holder = holderList.get(i);
            if (isPlaying) {
                //播放时，全部按钮隐藏
                holder.getBtnAdd().setVisibility(View.GONE);
                holder.getBtnGallery().setVisibility(View.GONE);
            } else {
                if (holder.getBindMix().isExistsVideo()) {
                    holder.getBtnAdd().setImageResource(R.drawable.btn_mix_edit);
                    holder.getBtnAdd().setVisibility(View.VISIBLE);
                    holder.getBtnGallery().setImageResource(R.drawable.btn_mix_del);
                    holder.getBtnGallery().setVisibility(View.GONE);
                } else {
                    if (holder.getBindMix().getState() == MixInfo.RECORD_VIDEO) {
                        holder.getBtnAdd().setVisibility(View.GONE);
                        holder.getBtnGallery().setImageResource(R.drawable.btn_mix_del);
                        holder.getBtnGallery().setVisibility(View.VISIBLE);
                        holder.getBtnFullScreen().setImageResource(R.drawable.btn_mix_fullscreen);
                        holder.getBtnFullScreen().setVisibility(View.VISIBLE);
                    } else {
                        holder.getBtnGallery().setImageResource(R.drawable.btn_mix_gallery);
                        holder.getBtnGallery().setVisibility(View.VISIBLE);
                        holder.getBtnFullScreen().setVisibility(View.GONE);
                        holder.getBtnAdd().setImageResource(R.drawable.btn_mix_add);
                        holder.getBtnAdd().setVisibility(View.VISIBLE);
                    }
                }
            }
        }

    }


    /**
     * 绑定和初始化摄像头
     *
     * @param cameraParent
     */
    private void initCameraLayout(final VideoPreviewLayout cameraParent) {
        mBtnNext.setText(R.string.complete);
        m_rlPreviewLayout = cameraParent.createCameraView();
        mProgressLayout.setVisibility(View.GONE);
        recordBar.setVisibility(View.VISIBLE);
        onInitializeRecorder(cameraParent);
        currentCameraLayoutId = cameraParent.getId();
        final MixItemHolder holder = cameraParent.getHolder();

        if (null != holder) {
            //必须移除当前画框中的组件，防止被新增的摄像头frame遮挡
            //全屏按钮
            ImageView full = holder.getBtnFullScreen();
            if (null != full) {
                cameraParent.removeView(full);
                holder.setBtnFullScreen(null);
            }

            //删除按钮
            ImageView del = holder.getBtnGallery();
            if (null != full) {
                cameraParent.removeView(del);
                holder.setBtnGallery(null);
            }

        }


        //创建一个全屏按钮
        FrameLayout.LayoutParams lpScreen = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpScreen.gravity = (Gravity.BOTTOM | Gravity.RIGHT);

        ImageView screen = new ImageView(this, null);
        screen.setImageResource(R.drawable.btn_mix_fullscreen);
        screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastChecked = cameraParent;
                if (null != lastChecked) {
                    onPlayerStop();
                    startfull();
                }
            }
        });
        screen.setVisibility(View.GONE);
        holder.setBtnFullScreen(screen);
        cameraParent.addView(screen, lpScreen);


        //删除按钮
        FrameLayout.LayoutParams lpDel = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpDel.gravity = (Gravity.BOTTOM | Gravity.LEFT);
        ImageView del = new ImageView(this, null);
        del.setImageResource(R.drawable.btn_mix_del);
        onGalleryODelLisenter ondel = new onGalleryODelLisenter(cameraParent, holder.getBindMix().getMixRect());
        del.setOnClickListener(ondel);
        holder.setBtnGallery(del);
        cameraParent.addView(del, lpDel);


    }

    private void exportRecordVideos(VideoPreviewLayout cameraLayout) {


        final VirtualVideo recordVideo = new VirtualVideo();
        Scene scene = VirtualVideo.createScene();
        int len = recordingList.size();
        for (int i = 0; i < len; i++) {
            MediaObject media = null;
            try {
                media = new MediaObject(recordingList.get(i).getPath());
                media.setShowRectF(new RectF(0, 0, 1f, 1f));
                media.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
                scene.addMedia(media);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }

        }
        recordVideo.addScene(scene);
        final String outRecord = PathUtils.getTempFileNameForSdcard(PathUtils.TEMP_RECORDVIDEO, "mp4");


        VideoConfig vc = new VideoConfig();
        getMediaInfo(recordingList.get(0).getPath(), vc);
        vc.setAspectRatio(cameraLayout.getAspectRatio());

        recordVideo.export(this, outRecord, vc, new ExportListener() {

            @Override
            public void onExportStart() {
                SysAlertDialog.showLoadingDialog(MixRecordActivity.this, R.string.isloading);
            }

            @Override
            public boolean onExporting(int progress, int max) {
                android.util.Log.e(TAG, "onExporting: " + progress + "xxxx" + max);
                return true;
            }

            @Override
            public void onExportEnd(int result) {

                recordBar.clearAll();
                recordBar.setVisibility(View.GONE);
                recordVideo.release();
                recordingList.clear();
                if (result >= VirtualVideo.RESULT_SUCCESS) {
                    onRecordEndUI(outRecord);
                } else {

                }

            }
        });


    }

    /**
     * 删除和图库
     */
    private class onGalleryODelLisenter implements View.OnClickListener {
        private VideoPreviewLayout cameraParent;
        private RectF mixRect;

        public onGalleryODelLisenter(VideoPreviewLayout _cameraParent,
                                     RectF _mixRect) {
            this.cameraParent = _cameraParent;
            this.mixRect = _mixRect;
        }

        @Override
        public void onClick(View v) {
            onRemoveEditUI();

            lastChecked = cameraParent;
            int mixId = ModeUtils.getRect2Id(mixRect);
            MixInfo info = getMixData(mixId);
            onPlayerStop();
            if (info.isExistsVideo() || info.isRecord()) {
                //删除摄像头视频或gallery视频
                onDeleteItemData(cameraParent, mixId);
            } else {
                //从图库选折一个视频
                currentMix = new MixInfo(mixRect);
                currentAspectRatio = lastChecked.getAspectRatio();
                ModeUtils.openGallery(MixRecordActivity.this, REQUEST_MIX_GALLERY);


            }
        }
    }

    /**
     * 获取mix组件集合的下标
     *
     * @param mId
     * @return
     */
    private int getMixHolderIndex(int mId) {
        int index = -1;
        if (mId == 0) {
            return index;
        }
        int len = holderList.size();
        for (int i = 0; i < len; i++) {
            if (holderList.get(i).getMixId() == mId) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 通过mixId 获取单个画框中绑定的视频
     *
     * @param mixId
     * @return
     */
    private MixInfo getMixData(int mixId) {
        MixItemHolder holder = getMixHolder(mixId);
        return null != holder ? holder.getBindMix() : null;

    }

    /**
     * 获取指定的holder
     *
     * @param mixId
     * @return
     */
    private MixItemHolder getMixHolder(int mixId) {
        int index = getMixHolderIndex(mixId);
        if (-1 != index) {
            return holderList.get(index);
        }
        return null;
    }

    private Dialog mCancelLoading;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case THUMB_OVER: {
                    if (null != msg.obj) {
                        String thumb = (String) msg.obj;
                    }
                    initPlayerData(null);
                }
                break;
                case CANCEL_EXPORT: {
                    mCancelLoading = SysAlertDialog.showLoadingDialog(
                            MixRecordActivity.this, R.string.canceling, false,
                            new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    if (null != exportVideo) {
                                        exportVideo = null;
                                    }
                                    mCancelLoading = null;
                                }
                            });
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (null != mCancelLoading)
                                mCancelLoading.setCancelable(true);
                        }
                    }, 5000);
                }
                break;
                default: {
                }
                break;
            }
        }
    };

    /**
     * 获取视频尾帧画面
     *
     * @param info
     */
    private void asyncLoadThumb(final MixInfo info) {

        ThreadPoolUtils.executeEx(new Runnable() {
            @Override
            public void run() {

                String thumb = null;
                if (null != info) {
                    MediaObject tmp = info.getMediaObject();
                    thumb = ModeUtils.getLastThumb(tmp.getMediaPath(), tmp.getTrimEnd());
                    info.setThumbPath(thumb);
//                    Log.e(TAG, "run:  thumb" + thumb);
                }
                initLastThumb();
                mHandler.obtainMessage(THUMB_OVER, thumb).sendToTarget();

            }
        });
    }

    /**
     * 选择视频，或录制成功，单个画框更改UI为可编辑状态
     *
     * @param holder
     */
    private void onAddedVideoUI(MixItemHolder holder) {
        if (null != holder) {
            holder.getBtnAdd().setImageResource(R.drawable.btn_mix_edit);
            holder.getBtnAdd().setVisibility(View.VISIBLE);
            holder.getBtnGallery().setVisibility(View.GONE);
            holder.getBtnGallery().setImageResource(R.drawable.btn_mix_del);
            holder.getBtnFullScreen().setVisibility(View.GONE);
        }
    }

    private final int REQUEST_CODE_PERMISSIONS = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSIONS: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        if (permissions[i] == Manifest.permission.CAMERA) {
                            onToast(getString(R.string.permission_camera_error));
                        } else {
                            onToast(getString(R.string.permission_audio_error));
                        }
                        finish();
                        return;
                    }
                }
            }
            break;
            default:
                break;
        }
    }


    //当前画框的比例
    private float currentAspectRatio = 1.0f;
    private final int MAX_SIZE = 368 * 640;
    private final int MAX_FRAME = 28;
    private final int MAX_BIT = 4000 * 1000;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_OK: {
                if (requestCode == REQUEST_MIX_GALLERY) {

                    ArrayList<String> tempMedias = data
                            .getStringArrayListExtra(SdkEntry.ALBUM_RESULT);

                    if (null != tempMedias && tempMedias.size() > 0) {
                        String filePath = tempMedias.get(0);

                        VideoConfig videoConfig = new VideoConfig();
                        if (getMediaInfo(filePath, videoConfig) > 0) {
//                            Log.e(TAG, "onActivityResult: " + filePath + "---" + videoConfig.getVideoWidth() + "*" + videoConfig.getVideoHeight() + "--" + videoConfig.getVideoFrameRate() + "...." + videoConfig.getVideoEncodingBitRate());
                            if (videoConfig.getVideoWidth() * videoConfig.getVideoHeight() > MAX_SIZE || videoConfig.getVideoFrameRate() > MAX_FRAME || videoConfig.getVideoEncodingBitRate() > MAX_BIT) {
                                //高质量视频，需要压缩裁剪
                                ModeUtils.gotoTrim(this, filePath, REQUEST_TRIM_CROP, currentAspectRatio, false);
                            } else {
                                onActivityResultVideo(filePath);
                            }
                        } else {
                            ModeUtils.gotoTrim(this, filePath, REQUEST_TRIM_CROP, currentAspectRatio, false);
                        }
                    }
                } else if (requestCode == REQUEST_TRIM_CROP) {
                    onActivityResultVideo(data.getStringExtra(SdkEntry.INTENT_KEY_VIDEO_PATH));
                } else if (requestCode == REQUEST_EDIT_TRIM) {
                    //截取单个视频，返回截取起止时间。
                    Scene scene = data.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
                    MediaObject temp = scene.getAllMedia().get(0);
                    editHandler.onActivityEditResultOK(temp.getTrimStart(), temp.getTrimEnd());
                } else if (requestCode == REQUEST_EDIT_MIRROR) {
                    //编辑编辑镜像...
                    Scene scene = data.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
                    MediaObject temp = scene.getAllMedia().get(0);
                    editHandler.onActivityEditResultOKMirror(temp);


                }


            }
            break;
            default:
                break;
        }

    }

    /**
     * 单个视频符合要求，加载数据
     *
     * @param filePath
     */
    private void onActivityResultVideo(String filePath) {
        if (null != currentMix && !TextUtils.isEmpty(filePath)) {

            MediaObject media = null;
            try {
                media = new MediaObject(filePath);
                float duration = ModeUtils.getDuration(filePath);
                media.setTimeRange(0, duration);
                VideoOb videoOb = new VideoOb(0, duration, 0, duration, 0, duration, 0, null, 0);
                media.setTag(videoOb);
                currentMix.setMediaObject(media);
                currentMix.setState(MixInfo.GALLERY_VIDEO);
                currentMix.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
                MixItemHolder holder = holderList.get(getMixHolderIndex(currentMix.getId()));
                if (null != holder) {
                    holder.setBindMix(currentMix);
                    //状态改为编辑
                    onAddedVideoUI(holder);
                    asyncLoadThumb(currentMix);
                } else {
                    initPlayerData(null);
                }
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
                initPlayerData(null);
            }
        } else {
            initPlayerData(null);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_mix_record_layout);
        Log.e(TAG, "onCreate: " + this.toString());
        initView();
        listPreview.clear();
        list = getIntent().getParcelableArrayListExtra(PARAM_MODE);
        assetBg = getIntent().getStringExtra(PARAM_ASSET_BG);
        mTvTitle.setText(R.string.preview);
        mPreviewFrame.setAspectRatio(SelectModeActivity.ASP_RATION);
        player.setPreviewAspectRatio(SelectModeActivity.ASP_RATION);
        mVirtualVideo = new VirtualVideo();
        editHandler = new EditItemHandler(mEditPlayerParent, sBarVolume, sBarChannel);
        recodtime = (TextView) findViewById(R.id.recoderInfo);
        ArrayList<MixInfo> mixData = new ArrayList<>();
        if (null != savedInstanceState) {
            mixData = savedInstanceState.getParcelableArrayList(SAVE_MIXDATA);
            currentMix = savedInstanceState.getParcelable(SAVE_CURRENT_MIXDATA);
            usedCameraIndex = savedInstanceState.getInt(SAVE_MIX_CAMERA);
        }

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!bInterceptRepeat) {
                    //防止重复点击
                    bInterceptRepeat = true;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bInterceptRepeat = false;
                        }
                    }, 800);

                    if (isEditIng) {
                        editHandler.onStartClick();
                    } else {
                        if (isPlayingORecording) {

                            //暂停播放
                            player.pause();
                            onResetPlayState(false);
                            //停止录制
                            forceStopRecord();
                            isPlayingORecording = false;
                        } else {
                            isPlayingORecording = true;
                            player.start();
                            onResetPlayState(true);
                            if (-1 != currentCameraLayoutId) {

                                if (getRecordVideoDuration() >= (recordBar.getDuration() - 50)) {
                                    onToast("已超过最大录制时长");
                                } else {
                                    try {
                                        mProgressLayout.setVisibility(View.GONE);
                                        recordVideoPath = PathUtils.getTempFileNameForSdcard(PathUtils.TEMP_MIX_RECORD, "mp4");
                                        RecorderCore.startRecord(recordVideoPath);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }


            }
        });
        initPlayerListener(player);
        initVideoUI();
        if (null != mixData && mixData.size() > 0) {
            copyDataToHolder(mixData);
        }
        initPlayerData(null);


        //先确保相机录音权限,再初始化摄像头
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasReadPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);

            List<String> permissions = new ArrayList<String>();
            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            hasReadPermission = checkSelfPermission(Manifest.permission.CAMERA);
            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (!permissions.isEmpty()) {
                requestPermissions(
                        permissions.toArray(new String[permissions.size()]),
                        REQUEST_CODE_PERMISSIONS);
            }
        }

    }

    private boolean isPlayingORecording = false;


    private boolean bInterceptRepeat = false;

    /**
     * 拷贝Activity销毁之前保存的数据->holderList集合并绑定
     *
     * @param mixData
     */
    private void copyDataToHolder(ArrayList<MixInfo> mixData) {
        if (null != mixData) {
            int len = mixData.size();
            for (int i = 0; i < len; i++) {
                MixInfo temp = mixData.get(i);
                MixItemHolder holder = getMixHolder(temp.getId());
                if (null != holder) {
                    holder.setBindMix(temp);
                }
            }
        }


    }


    private boolean isFullScreen = false;


    TextView recodtime;
    RelativeLayout m_rlPreviewLayout;
    private int currentCameraLayoutId = -1;
    private View mContent;

    /**
     * 是否全屏
     */
    private void startfull() {
//        Log.e(TAG, "startfull: " + isFullScreen);
        if ((!isFullScreen)) {
            isFullScreen = true;
            if (CoreUtils.hasIceCreamSandwich()) {
                // 全屏时，隐藏虚拟键区
                mContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
            int len = listPreview.size();
            RectF targetRect = null;
            for (int i = 0; i < len; i++) {
                VideoPreviewLayout videoTemp = listPreview.get(i);
                if (videoTemp.getId() != lastChecked.getId()) {
                    //其他画框全部隐藏
                    if (videoTemp.getId() == currentCameraLayoutId) {
                        RecorderCore.recycleCameraView();//释放摄像头画面。因为：player ，与摄像头的gl有重叠，所以释放摄像头
                    }
                    videoTemp.setVisibility(View.GONE);
                    Log.e(TAG, "startfull: " + lastChecked.getId() + "...." + videoTemp.getId());
                } else {

                    //要全屏的画框
                    targetRect = lastChecked.getVideoRectF();
                    lastChecked.setClearBorderLine(true);
                    MixItemHolder holder = videoTemp.getHolder();

                    if (null != holder) {
                        holder.getBtnAdd().setVisibility(View.GONE);
                        holder.getBtnGallery().setImageResource(R.drawable.btn_mix_del);
                        holder.getBtnGallery().setVisibility(View.GONE);
                    }
                    lastChecked.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    lastChecked.requestLayout();
                }
            }
            MixInfo info = getMixData(ModeUtils.getRect2Id(targetRect));
            if (null != info) {
                initPlayerData(info);
            }
        } else {
            if (CoreUtils.hasIceCreamSandwich()) {
                mContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
            isFullScreen = false;
            int len = listPreview.size();
            for (int i = 0; i < len; i++) {
                VideoPreviewLayout videoTemp = listPreview.get(i);
                if (videoTemp.getId() != lastChecked.getId()) {
                    videoTemp.setVisibility(View.VISIBLE);
                    if (videoTemp.getId() == currentCameraLayoutId) {
                        onInitializeRecorder(videoTemp);//恢复摄像头画面
                    } else {
                        MixItemHolder holder = videoTemp.getHolder();

                        if (null != holder) {
                            if (holder.getBindMix() != null && holder.getBindMix().isExistsVideo()) {
                                holder.getBtnAdd().setImageResource(R.drawable.btn_mix_edit);
                                holder.getBtnAdd().setVisibility(View.VISIBLE);
                                holder.getBtnGallery().setImageResource(R.drawable.btn_mix_del);
                                holder.getBtnGallery().setVisibility(View.GONE);
                            } else {
                                holder.getBtnAdd().setImageResource(R.drawable.btn_mix_add);
                                holder.getBtnAdd().setVisibility(View.VISIBLE);
                                holder.getBtnGallery().setImageResource(R.drawable.btn_mix_gallery);
                                holder.getBtnGallery().setVisibility(View.VISIBLE);
                            }
                        }
                    }


                } else {
                    lastChecked.setClearBorderLine(true);
                    lastChecked.resetChildSize(lastChecked.getVideoRectF());

                    MixItemHolder holder = videoTemp.getHolder();

                    if (null != holder) {
                        if (holder.getBindMix() != null && holder.getBindMix().isRecord()) {
                            holder.getBtnAdd().setImageResource(R.drawable.btn_mix_edit);
                            holder.getBtnAdd().setVisibility(View.VISIBLE);
                            holder.getBtnGallery().setVisibility(View.GONE);
                        } else {
                            holder.getBtnAdd().setImageResource(R.drawable.btn_mix_add);
                            holder.getBtnAdd().setVisibility(View.VISIBLE);
                            holder.getBtnGallery().setImageResource(R.drawable.btn_mix_gallery);
                            holder.getBtnGallery().setVisibility(View.VISIBLE);
                        }
                    }

                }
            }
            initPlayerData(null);
        }
    }

    private String getFormatTime(int msec) {
        return DateTimeUtils.stringForMillisecondTime(msec, true, true);
    }

    /**
     * 注册播放器回调
     *
     * @param player
     */
    private void initPlayerListener(final VirtualVideoView player) {

        player.setOnPlaybackListener(new VirtualVideoView.VideoViewListener() {
            @Override
            public void onPlayerPrepared() {

                float dura = player.getDuration();
                if (!isEditIng) {
                    SysAlertDialog.cancelLoadingDialog();
                    int ms = Utils.s2ms(dura);
                    mRdSeekBar.setMax(ms);
                    totalTv.setText(getFormatTime(ms));
                    onSeekTo(0);
                    mProgressLayout.setVisibility(View.VISIBLE);
                    onResetPlayState(false);
                }
//                Log.e(TAG , mPreviewFrame.getWidth() + "*" + mPreviewFrame.getHeight() + "..screen:" + CoreUtils.getMetrics().widthPixels + "onPlayerPrepared: " + player.getDuration() + "。。。。" + player.getWidth() + "*" + player.getHeight() + "......>" + player.getVideoWidth() + "*" + player.getVideoHeight());

            }

            @Override
            public boolean onPlayerError(int what, int extra) {
                Log.e(TAG, "mute-onPlayerError: " + what + "..." + extra);
                if (!isEditIng) {
                    onSeekTo(0);
                }
                return false;
            }

            @Override
            public void onPlayerCompletion() {
                Log.i(TAG, "onPlayerCompletion:  播放完毕-->" + player.getDuration());
                if (!isEditIng) {
                    onResetPlayState(false);
                }
            }

            @Override
            public void onGetCurrentPosition(float position) {
                if (!isEditIng) {
                    onSeekTo(position);

                }
            }
        });


        player.setOnInfoListener(new VirtualVideo.OnInfoListener() {
            @Override
            public boolean onInfo(int what, int extra, Object obj) {
                Log.i(TAG, "onInfo: " + what + "..." + extra + "..." + obj);
                return true;
            }
        });


    }

    private VirtualVideo mVirtualVideo;

    /***
     * 初始化播放器媒体资源
     * @param fullScreen  全屏时的单个媒体资源;   默认传null
     */
    private void initPlayerData(MixInfo fullScreen) {
        mVirtualVideo.reset();
        player.reset();
        try {
            player.setAspectRatioFitMode(AspectRatioFitMode.IGNORE_ASPECTRATIO);
            if (null != fullScreen) {
                //全屏时，加载单个视频
                if (fullScreen.isExistsVideo()) {
                    Scene scene = mVirtualVideo.createScene();
//                    Log.i(TAG, "initPlayerData: fullScreen" + fullScreen.toString());
                    MediaObject media = scene.addMedia(fullScreen.getMediaObject().getMediaPath());
                    media.setShowRectF(fullScreen.getMixRect());
                    mVirtualVideo.addScene(scene);
                    mVirtualVideo.build(player);
                }
            } else {
                int len = holderList.size();
                if (len > 0) {
                    //加载全部视频
                    boolean hasVideo = reload(mVirtualVideo);
                    if (hasVideo) {
                        mVirtualVideo.build(player);
                    } else {
                        Log.e(TAG, "initPlayerData: 没有视频!");
                    }
                }
            }
        } catch (InvalidStateException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        onSeekTo(0);
    }


    private void onSeekTo(float progress) {
        int tp = Utils.s2ms(progress);
        currentTv.setText(getFormatTime(tp));
        mRdSeekBar.setProgress(tp);
    }

    @Override
    protected void onDestroy() {

        //清除片尾帧中的temp文件
        if (null != holderList) {
            int len = holderList.size();
            for (int i = 0; i < len; i++) {
                MixItemHolder holder = holderList.get(i);
                if (null != holder) {
                    FileUtils.deleteAll(holder.getBindMix().getThumbPath());
                }
            }
        }
        if (null != player) {
            player.setOnPlaybackListener(null);
            player.stop();
            player.cleanUp();
        }
        mVirtualVideo.release();
        super.onDestroy();
        RecorderCore.unRegisterReceiver();
        RecorderCore.onExit(this);
        holderList.clear();
    }


    /**
     * 开启预览
     */
    private void onInitializeRecorder(VideoPreviewLayout cameraParent) {
        //清除之前的摄像头预览界面
        RecorderCore.recycleCameraView();
        m_rlPreviewLayout.removeAllViews();
        if (RecorderCore.isRegistedOsd()) {
            RecorderCore.registerOSD(null);
        }

        float vAspRatio = cameraParent.getAspectRatio();
        VideoConfig vc = new VideoConfig();
        //得到等比的尺寸
        ModeUtils.fixRecordOutSize(vc, vAspRatio);
        int targetW = vc.getVideoWidth(), targetH = vc.getVideoHeight();
        //给当前尺寸匹配码流
        int bite = ModeUtils.getRecordBit(targetW, targetH);
//        Log.i(TAG, "onInitializeRecorder: " + targetW + "*" + targetH + "....." + (targetW / (targetH + 0.0f)) + "......" + vAspRatio + "...bite:" + bite);
        //录制推荐配置
        RecorderConfig config = new RecorderConfig()
                .setVideoSize(targetW, targetH)
                //输出帧率
                .setVideoFrameRate(24)
                //输出码率 (需要倒序时设置4M)
                .setVideoBitrate(bite)
                //关键帧间隔  (‘0’代表:每帧都是关键帧，倒序更快)
                .setKeyFrameTime(0)
                //前置摄像头
                .setEnableFront(true)
                //开启美颜
                .setEnableBeautify(true)
                .setBeauitifyLevel(5)
                //前置输出视频镜像
                .setEnableFrontMirror(true)
                //GL置顶
                .setZOrderOnTop(true).setZOrderMediaOverlay(true)
                //设置自动对焦
                .setEnableAutoFocus(true)
                //录制过程也对焦
                .setEnableAutoFocusRecording(true);
        RecorderCore.setEncoderConfig(config);
        RecorderCore.onPrepare(m_rlPreviewLayout, iListener);
    }

    /**
     * 录制回调
     */
    private IRecorderCallBack iListener = new IRecorderCallBack() {

        @Override
        public void onRecordFailed(int nResult, String strResultInfo) {
//录制失败
            onToast("onRecordFailed:" + nResult + strResultInfo);
        }

        @Override
        public void onRecordEnd(int nResult, String strResultInfo) {
            if (nResult >= ResultConstants.SUCCESS) {
                VideoConfig vc = new VideoConfig();
                float fdu = VirtualVideo.getMediaInfo(recordVideoPath, vc);
                int du = Utils.s2ms(fdu);
                recordingList.add(new RecordInfo(recordVideoPath, du));
                recordBar.addItemLine(du + lastRecordDuration);
                //录制成功结束
                VideoPreviewLayout cameraParent = listPreview.get(usedCameraIndex);
                if (null != cameraParent) {
                    if (null != recordingState) {
                        //移除录制中的标识
                        cameraParent.removeView(recordingState);
                    }
                    //录制完成，删除摄像头组件，换成录制的视频


                    MixItemHolder holder = cameraParent.getHolder();
                    android.util.Log.e(TAG, "onRecordEnd: " + (null != holder));

                    if (null != holder) {//录制结束，绑定录制模画框里面的内容
//                        holder.getBindMix().setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
//                        holder.getBindMix().setState(MixInfo.RECORDED_VIDEO);//摄像头视频
                        holder.getBtnAdd().setImageResource(R.drawable.bottom_menu_sure);
                        holder.getBtnAdd().setVisibility(View.VISIBLE);
                        delRecordItem.setVisibility(View.VISIBLE);

//                        MediaObject tmp = new MediaObject(recordVideoPath);
//                        float duration = ModeUtils.getDuration(recordVideoPath);
//                        tmp.setTimeRange(0, duration);
//
//                        VideoOb tOb = new VideoOb(0, duration, 0, duration, 0, duration, 0, null, 0);
//                        tmp.setTag(tOb);
//
//                        holder.getBindMix().setMediaObject(tmp);
////                        holder.getBindMix().setVideoInfo(recordVideoPath, 0, ModeUtils.getDuration(recordVideoPath));//绑定视频文件
//
//                        //画框改为可编辑状态
//                        onAddedVideoUI(holder);

                    }
//                    //重新加载数据
//                    initPlayerData(null);
//                    //恢复状态
//                    usedCameraIndex = DEFAULT_CAMERA_INDEX;
                }
            } else {
                onToast("onRecordEnd:" + nResult + strResultInfo);
            }


        }


        private ImageView recordingState;

        private int lastRecordDuration;

        @Override
        public void onRecordBegin(int nResult, String strResultInfo) {
            recordingState = null;
            if (nResult == ResultConstants.SUCCESS) {
                //开始录制
                if (usedCameraIndex != DEFAULT_CAMERA_INDEX) {
                    VideoPreviewLayout cameraParent = listPreview.get(usedCameraIndex);

                    //增加一个录制中的标识
                    recordingState = new ImageView(cameraParent.getContext(), null);
                    recordingState.setImageResource(R.drawable.recording_state);
                    FrameLayout.LayoutParams lpRecording = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lpRecording.gravity = (Gravity.TOP | Gravity.RIGHT);
                    lpRecording.topMargin = CoreUtils.dpToPixel(5);
                    lpRecording.rightMargin = CoreUtils.dpToPixel(5);
                    cameraParent.addView(recordingState, lpRecording);
                    //已录制的视频时长
                    lastRecordDuration = getRecordVideoDuration();
                }

            } else {
                //录制失败
                onToast("录制失败" + strResultInfo);
            }

        }

        @Override
        public void onPrepared(int nResult, String strResultInfo) {
            if (nResult == ResultConstants.SUCCESS) {
                //录制摄像头界面打开成功
                //设置相机滤镜
                RecorderCore.setColorEffect(Integer.toString(RecorderCore.BASE_FILTER_ID_COLD));
            } else {
                //异常
            }
        }

        @Override
        public void onPermissionFailed(int nResult, String strResultInfo) {
            //target 23 授权失败
        }


        @Override
        public void onGetRecordStatus(int nPosition, int nRecordFPS, int delayed) {

            if (null != recordBar) {
                int temp = lastRecordDuration + nPosition;

                if (temp >= (recordBar.getDuration() - 50)) {
                    RecorderCore.stopRecord();
                } else {
                    recordBar.setProgress(lastRecordDuration + nPosition);
                }
            }
        }

        @Override
        public void onCamera(int nResult, String strResultInfo) {
            //打开相机失败
        }
    };


    private int getRecordVideoDuration() {
        int len = recordingList.size();
        int duration = 0;
        for (int i = 0; i < len; i++) {
            duration += recordingList.get(i).getDuration();
        }
        return duration;
    }

    public void onRecordEndUI(String outPath) {

        mBtnNext.setText(R.string.next);

        VideoPreviewLayout cameraParent = listPreview.get(usedCameraIndex);
        if (null != cameraParent) {
//                if (null != recordingState) {
//                    //移除录制中的标识
//                    cameraParent.removeView(recordingState);
//                }
            //录制完成，删除摄像头组件，换成录制的视频
            RecorderCore.recycleCameraView();
            cameraParent.removeCameraView();

        }

        MixItemHolder holder = getMixHolder(ModeUtils.getRect2Id(list.get(usedCameraIndex)));
        if (null != holder) {//录制结束，绑定录制模画框里面的内容
            holder.getBindMix().setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
            holder.getBindMix().setState(MixInfo.RECORDED_VIDEO);//摄像头视频


            MediaObject tmp = null;
            try {
                tmp = new MediaObject(outPath);
                float duration = ModeUtils.getDuration(outPath);
                tmp.setTimeRange(0, duration);
                VideoOb tOb = new VideoOb(0, duration, 0, duration, 0, duration, 0, null, 0);
                tmp.setTag(tOb);

                holder.getBindMix().setMediaObject(tmp);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }


            //画框改为可编辑状态
            onAddedVideoUI(holder);

        }

        //恢复状态
        usedCameraIndex = DEFAULT_CAMERA_INDEX;

        //创建thumb
        asyncLoadThumb(holder.getBindMix());


    }

    //保存当前正在编辑的多段视频，防止选择图库时，多个视频被回收
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        ArrayList<MixInfo> mixData = new ArrayList<>();
        int len = holderList.size();
        for (int i = 0; i < len; i++) {
            MixItemHolder holder = holderList.get(i);
            if (holder.getBindMix() != null && holder.getBindMix().isExistsVideo()) {
                mixData.add(holder.getBindMix());
            }
        }
        outState.putParcelableArrayList(SAVE_MIXDATA, mixData);
        outState.putParcelable(SAVE_CURRENT_MIXDATA, currentMix);
        outState.putInt(SAVE_MIX_CAMERA, usedCameraIndex);
        super.onSaveInstanceState(outState);
    }

    private float lastProgress = -1f;
    private boolean bPlayerPrepared = false;

    @Override
    protected void onStart() {
        super.onStart();
        isPlayingORecording = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bPlayerPrepared) {
            if (isEditIng) {
                //编辑播放器的状态
                editHandler.onResume();
            } else {
                if (lastProgress != -1f) {
                    //*****如果播放器在切到后台时，已经stop(),需要重新build(),再预览
                    //还原播放器位置，恢复缩略图
                    player.seekTo(lastProgress);
                    onSeekTo(lastProgress);
                }
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bPlayerPrepared) {
            lastProgress = -1f;
            if (isEditIng) {
                editHandler.onPause();
            } else {

                if (null != player) {
                    if (player.isPlaying()) {
                        //暂停
                        player.pause();
                        onResetPlayState(false);
                    }
                    //记录播放器位置
                    lastProgress = player.getCurrentPosition();
                }
            }

        }

    }

    private void onToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private EditItemHandler editHandler;
    private boolean isEditIng = false;


    /**
     * 响应编辑单段视频
     */
    private void onEditItemData() {

        isEditIng = true;
        //编辑时，清除录制，防止gl重叠 ，界面显示异常
//        RecorderCore.recycleCameraView();
        playerParent.setVisibility(View.VISIBLE);
        mEditPlayerParent.setVisibility(View.VISIBLE);
        mixMenuLayout.setVisibility(View.VISIBLE);
        mixEditLayout.setVisibility(View.VISIBLE);
        mProgressLayout.setVisibility(View.INVISIBLE);
        mBtnNext.setText(R.string.complete);
        mTvTitle.setText(R.string.mixEdit);
        MixItemHolder holder = holderList.get(getMixHolderIndex(currentMix.getId()));
        if (null != holder) {
            MixInfo info = null;
            if (null != holder && (info = holder.getBindMix()) != null && info.isExistsVideo()) {
                editHandler.initData(info, currentMix.getMixRect(), new EditItemHandler.IMenuLisenter() {
                    float dura = 0;
                    String TAG = "edit";

                    @Override
                    public void onPlayerPrepared() {
                        onSeekTo(0);
                        editHandler.setPlayerPrepared();
//                        Log.e(TAG, "onPlayerPrepared: " + player.getDuration() + "。。。。" + player.getWidth() + "*" + player.getHeight() + "......>" + player.getVideoWidth() + "*" + player.getVideoHeight());
                        dura = editHandler.getPlayerDuration();
                        int ms = Utils.s2ms(dura);
                        mRdSeekBar.setMax(ms);
                        totalTv.setText(getFormatTime(ms));
                        mProgressLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public boolean onPlayerError(int what, int extra) {
                        onSeekTo(0);
                        return false;
                    }

                    @Override
                    public void onPlayerCompletion() {

                        Log.i(TAG, "onPlayerCompletion:  edit");
                    }

                    @Override
                    public void onGetCurrentPosition(float position) {
                        onSeekTo(position);
                    }


                    @Override
                    public void onEditDelete() {
                        //编辑时，删除当前video
                        //第一步:移除编辑部分的UI
                        if (isVoicing) {
                            onVoiceOut();
                        }
                        editHandler.removeEditUI();
                        removeEditUI(true);
                        //第二步:删除预览集合中的数据
                        int mixId = ModeUtils.getRect2Id(currentMix.getMixRect());
                        onDeleteItemData(lastChecked, mixId);
                        //last:重新加载预览播放器
                        initPlayerData(null);
                    }
                });


            }
            //状态改为编辑
            holder.getBtnAdd().setVisibility(View.GONE);
            holder.getBtnGallery().setImageResource(R.drawable.btn_mix_del);
        }

    }

    /**
     * 删除单个视频
     */
    private void onDeleteItemData(VideoPreviewLayout itemPreview, int mixId) {
        onToast("删除成功");
        //恢复其他画框状态->可添加摄像头
        onResetAddState(mixId, false);
        MixItemHolder holder = holderList.get(getMixHolderIndex(mixId));
        if (null != holder) {
            MixInfo mixInfo;
            if ((mixInfo = holder.getBindMix()) != null && mixInfo.isRecord()) {
                //移除摄像头view
                RecorderCore.recycleCameraView();
                if (null != itemPreview) {
                    itemPreview.removeCameraView();
                }

            }
            if (null != holder.getBindMix()) {
                holder.getBindMix().reset();
            }
            //状态改为可新增视频或录制
            holder.getBtnAdd().setImageResource(R.drawable.btn_mix_add);
            holder.getBtnAdd().setVisibility(View.VISIBLE);
            holder.getBtnGallery().setImageResource(R.drawable.btn_mix_gallery);
            holder.getBtnGallery().setVisibility(View.VISIBLE);
            holder.getBtnFullScreen().setVisibility(View.GONE);
        }

        if (isFullScreen) {
            //退出全屏
            startfull();
            initPlayerData(null);
        }
    }


    /**
     * 清除编辑布局->回到预览
     *
     * @param reLoadData true 重新加载数据，flase 不加载数据
     */
    private void removeEditUI(boolean reLoadData) {
        isEditIng = false;
        mTvTitle.setText(R.string.preview);
        mBtnNext.setText(R.string.next);

        mEditPlayerParent.setVisibility(View.GONE);
        playerParent.setVisibility(View.VISIBLE);
        mixMenuLayout.setVisibility(View.VISIBLE);
        mixEditLayout.setVisibility(View.GONE);
        if (usedCameraIndex != -1) {
            //恢复摄像头
            initCameraLayout(listPreview.get(usedCameraIndex));

        }
        //恢复add按钮的状态
        onResetAddState(0, false);
        if (reLoadData) {
            initPlayerData(null);
        }
    }

    /**
     * 返回编辑->音量的上一页
     */
    private void onVoiceOut() {
        findViewById(R.id.mix_edit_voice_layout).setVisibility(View.GONE);
        findViewById(R.id.mix_edit_layout).setVisibility(View.VISIBLE);
        isVoicing = false;
    }


    @Override
    public void onBackPressed() {

        if (isVoicing) {
            onVoiceOut();
        } else if (isEditIng) {
            editHandler.removeEditUI();
            removeEditUI(true);
        } else {
            String strMessage = getString(R.string.quit_edit);
            Dialog dialog = SysAlertDialog.showAlertDialog(this, "", strMessage,
                    getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            dialog = null;
                        }
                    }, getString(R.string.sure),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            forceStopRecord();
                            if (null != player) {
                                player.stop();
                            }
                            finish();

                        }
                    });


        }
    }

    private void onBtnEditFilterClicked() {
        editHandler.stop();
        editHandler.onAddMixFilter();
    }

    private void onBtnEditEffectClicked() {

        editHandler.stop();
        editHandler.onAddEffect();
    }

    private boolean isVoicing = false;

    private void onBtnEditSoundClicked() {
        editHandler.stop();
        findViewById(R.id.mix_edit_layout).setVisibility(View.GONE);


        isVoicing = true;
        findViewById(R.id.mix_edit_voice_layout).setVisibility(View.VISIBLE);

        editHandler.onChangeChannel();
    }

    /**
     * 截取
     */
    private void onBtnEditTrim() {
        editHandler.stop();

        Scene scene = VirtualVideo.createScene();
        scene.addMedia(editHandler.getEditMedia());

        ModeUtils.gotoTrim(this, scene, REQUEST_EDIT_TRIM, currentAspectRatio, true);

    }

    /**
     * 编辑
     */
    private void onBtnEditEdit() {
        editHandler.stop();
        Scene scene = VirtualVideo.createScene();
        MediaObject temp = editHandler.getEditMedia();
//        Log.i(TAG, "onBtnEditEdit: " + temp.getClipRectF().toShortString() + "..." + temp.getAngle() + "...." + temp.getFlipType());
        VideoOb videoOb = new VideoOb(temp.getTrimStart(), temp.getTrimEnd(), temp
                .getTrimStart(), temp.getTrimEnd(), temp.getTrimStart(),
                temp.getTrimEnd(), 0, null, 0);
        temp.setTag(videoOb);
        scene.addMedia(temp);
        Intent intent = new Intent();
        intent.setClass(this, CropRotateMirrorActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        intent.putExtra(CropRotateMirrorActivity.SHOW_CROP, false);
        startActivityForResult(intent, REQUEST_EDIT_MIRROR);
        overridePendingTransition(0, 0);
    }

    private void onMBtnBackClicked() {
        onBackPressed();
    }

    private void onMBtnNextClicked() {
        if (isVoicing) {//返回编辑页
            onVoiceOut();
        } else {
            if (isEditIng) {
                //响应编辑完成
                editHandler.onExportVideo(new EditItemHandler.IExportLisener() {
                    @Override
                    public void onExportEnd(boolean sucess) {
                        isEditIng = false;
                        MixInfo info = null;
                        if (sucess) {
                            info = getMixData(ModeUtils.getRect2Id(lastChecked.getVideoRectF()));
                            if (info != null) {
                                float duration = ModeUtils.getDuration(editHandler.getOutPath());
                                MediaObject mediaObject = null;
                                try {
                                    mediaObject = new MediaObject(editHandler.getOutPath());
                                    mediaObject.setTimeRange(0, duration);
                                    mediaObject.setTimelineRange(0, duration);
                                    VideoOb vob = new VideoOb(0, duration, 0, duration, 0, duration, 0, null, 0);
                                    mediaObject.setTag(vob);
                                    info.setMediaObject(mediaObject);
                                } catch (InvalidArgumentException e) {
                                    e.printStackTrace();
                                }


                            }
                        }
                        //重新调整该区域的末尾帧的时间线
                        MixRecordActivity.this.asyncLoadThumb(info);
                        removeEditUI(false);
                        editHandler.removeEditUI();

                    }

                    @Override
                    public void onEnd(MixInfo info) {
                        isEditIng = false;
                        int id = ModeUtils.getRect2Id(lastChecked.getVideoRectF());
                        MixItemHolder holder = getMixHolder(id);
                        holder.setBindMix(info);
                        //重新调整该区域的末尾帧的时间线
                        MixRecordActivity.this.asyncLoadThumb(info);
                        //防止编辑界面上下颠倒，造成闪烁
                        removeEditUI(false);
                        //遮罩
                        editHandler.removeEditUI();
                    }
                });
            } else {
                //停止预览播放和录制

                player.stop();

                if (null != recordingList && recordingList.size() > 1) {
                    exportRecordVideos(listPreview.get(usedCameraIndex));

                } else {

                    onResetPlayState(false);
                    if (hasVideo) {
//                    Bitmap tmp = Bitmap.createBitmap(player.getVideoWidth(), player.getVideoHeight(), Bitmap.Config.ARGB_8888);
//                    VirtualVideo tmpVideo = new VirtualVideo();
//                    reload(tmpVideo);
//                    try {
//                        tmpVideo.build(this);
//                    } catch (InvalidStateException e) {
//                        e.printStackTrace();
//                    }
//
//                    boolean re = tmpVideo.getSnapshot(player.getDuration()/2, tmp);
//                    tmpVideo.release();
//                    android.util.Log.e(TAG, "onMBtnNextClicked: " + re);
//                    try {
//                        BitmapUtils.saveBitmapToFile(tmp, 80, PathUtils.getTempFileNameForSdcard("mix_thumb", "jpg"));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }

                        onExport();
                    } else {
                        onToast(getString(R.string.album_no_video));

                    }
                }

            }
        }

    }

    private boolean hasVideo = false;

    /**
     * 加载视频资源
     *
     * @param virtualVideo
     */
    private boolean reload(VirtualVideo virtualVideo) {
        int len = listPreview.size();
        MixInfo temp = null;
        Scene scene = VirtualVideo.createScene();
        boolean canBuild = false;
        hasVideo = false;
        if (!TextUtils.isEmpty(assetBg)) {
//            Log.e(TAG, "reload: assetbg:" + assetBg);
            MediaObject mediabg = null;
            try {
                mediabg = new MediaObject(this, assetBg);
                mediabg.setShowRectF(new RectF(0, 0, 1, 1));//显示区域
                if (len > 0) {
                    mediabg.setTimelineRange(0, maxDuration);//设置背景图的时长
                }
                scene.addMedia(mediabg);
                canBuild = true;
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }

        }

        for (int i = 0; i < len; i++) {
            VideoPreviewLayout item = listPreview.get(i);
            MixItemHolder holder = item.getHolder();
            temp = holder.getBindMix();
            if (null != temp) {
//                Log.e(TAG, "initPlayerData: " + i + "..." + temp.toString());
                if (null != temp.getMediaObject()) {
                    hasVideo = true;
                    canBuild = true;
                    MediaObject media = temp.getMediaObject().clone();
                    media.setShowRectF(temp.getMixRect());
                    media.setMixFactor(temp.getVolumeFactor());
//                    RectF rect = media.getClipRectF();
//                    Log.e(TAG, "reload: clip:" + ((null != rect) ? rect.toShortString() : "null") + "....");
                    media.setAspectRatioFitMode(temp.getAspectRatioFitMode());


//                    if (true) {
//                        //演示：自定义裁剪区域，
//                        Rect clipRect = new Rect();
//                        ModeUtils.getClipSrc(media.getWidth(), media.getHeight(), item.getAspectRatio(), clipRect, EditItemHandler.POFF);
//                        if (!clipRect.isEmpty()) {
//                            media.setClipRectF(new RectF(clipRect.left, clipRect.top, clipRect.right, clipRect.bottom));
//                        }
//                    }
                    scene.addMedia(media);
                    MediaObject thumb = temp.getThumbObject();
                    if (null != thumb) {
                        //单个视频的片尾
//                        Log.e(TAG, "reload: " + thumb.getTimelineFrom() + "<>" + thumb.getTimelineTo() + "..." + thumb.getMediaPath());
                        //跟视频的方向、角度一致
                        thumb.setFlipType(media.getFlipType());
                        thumb.setAngle(media.getAngle());
                        thumb.setShowRectF(temp.getMixRect());

//                        thumb.setAspectRatioFitMode(temp.getAspectRatioFitMode());
//                        if (null != rect && !rect.isEmpty()) {
//                            thumb.setClipRectF(rect);
//                        }

                        scene.addMedia(thumb);
                    }
                }
            }
        }
        if (canBuild) {
            scene.setPermutationMode(PermutationMode.COMBINATION_MODE);
            virtualVideo.addScene(scene);
        }
        return canBuild;
    }

    private VirtualVideo exportVideo;
    private Dialog cancelAlertDialog;
    private HorizontalProgressDialog vExpDialog;


    /**
     * 点击下一步->导出
     */
    private void onExport() {
        exportVideo = new VirtualVideo();
        if (reload(exportVideo)) {
            //可以导出有资源
            final String outpath = PathUtils.getMp4FileNameForSdcard();
            VideoConfig videoConfig = new VideoConfig();
            int outWidth = 480;
            videoConfig.setVideoSize(outWidth, (int) (outWidth / SelectModeActivity.ASP_RATION));
            videoConfig.setAspectRatio(SelectModeActivity.ASP_RATION);
            videoConfig.setVideoEncodingBitRate(4000 * 1000);
            videoConfig.setKeyFrameTime(0);
            final long t1 = System.currentTimeMillis();
            exportVideo.export(this, outpath, videoConfig, new ExportListener() {
                long t4;

                @Override
                public void onExportStart() {
                    t4 = System.currentTimeMillis();
                    vExpDialog = SysAlertDialog.showHoriProgressDialog(
                            MixRecordActivity.this, getString(R.string.exporting),
                            false, false, new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {

                                }
                            });
                    vExpDialog.setCanceledOnTouchOutside(false);
                    vExpDialog.setOnCancelClickListener(new HorizontalProgressDialog.onCancelClickListener() {

                        @Override
                        public void onCancel() {
                            cancelAlertDialog = SysAlertDialog.showAlertDialog(
                                    MixRecordActivity.this,
                                    "",
                                    getString(R.string.cancel_export_video),
                                    getString(R.string.no),
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(
                                                DialogInterface dialog, int which) {
                                        }
                                    },
                                    getString(R.string.yes),
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            vExpDialog.cancel();
                                            vExpDialog.dismiss();
                                            exportVideo.cancelExport();
                                            mHandler.obtainMessage(CANCEL_EXPORT)
                                                    .sendToTarget();
                                        }
                                    });
                        }
                    });
                }

                @Override
                public boolean onExporting(int progress, int max) {
                    if (null != vExpDialog) {
                        vExpDialog.setMax(max);
                        vExpDialog.setProgress(progress);
                    }
                    return true;
                }

                @Override
                public void onExportEnd(int nResult) {
                    long re = System.currentTimeMillis() - t1;
                    exportVideo.release();
                    if (null != vExpDialog) {
                        vExpDialog.cancel();
                        vExpDialog = null;
                    }
                    SysAlertDialog.cancelLoadingDialog();
                    if (nResult >= VirtualVideo.RESULT_SUCCESS) {
                        exportVideo = null;
                        VideoConfig tmp = new VideoConfig();
                        float du = getMediaInfo(outpath, tmp);
                        Log.i(TAG, "onExportEnd: " + du + "--->倍速：" + (Utils.s2ms(du) / (re + 0.0f)) + "....->" + tmp.getVideoWidth() + "*" + tmp.getVideoHeight());
                        gotoNext(outpath);
                    } else {
                        if (null != mCancelLoading) {
                            mCancelLoading.cancel();
                            mCancelLoading.dismiss();
                            mCancelLoading = null;
                        }
                        if (nResult == VirtualVideo.RESULT_EXPORT_CANCEL) {
                            onToast(getString(R.string.export_canceled));
                        } else {
                            onToast(getString(R.string.export_failed));
                        }
                        FileUtils.deleteAll(outpath);//清除失败的临时文件
                    }
                }
            });
        } else {
            exportVideo.release();
            onToast(getResources().getString(R.string.album_no_video));
        }


    }


    /**
     * 返回数据
     *
     * @param outpath
     */
    private void gotoNext(String outpath) {

        Intent intent = new Intent();
        intent.putExtra(SdkEntry.INTENT_KEY_VIDEO_PATH, outpath);
        setResult(RESULT_OK, intent);
        finish();

    }

    private float maxDuration = 0;

    /**
     * 每次选折完视频，需重新修正单个画框中的视频片尾
     */
    private void initLastThumb() {
        int len = holderList.size();
        maxDuration = 0;
        //第一步：找出视频时长的最长的那段视频
        int index = -1;
        for (int i = 0; i < len; i++) {
            MixItemHolder holder = holderList.get(i);
            if (holder.getBindMix().isExistsVideo()) {
                float tduration = holder.getBindMix().getMediaObject().getDuration();
                if (maxDuration < tduration) {
                    index = i;
                    maxDuration = tduration;
                }
            }
        }

        recordBar.setDuration(Math.max(60000, (int) (maxDuration * 1000)));

        //第二步:给其他视频端末尾设置片尾，(当前最长视频段如果有片尾，清除当前段的片尾）
        if (index != -1) {
            for (int i = 0; i < len; i++) {
                MixItemHolder holder = holderList.get(i);
                MixInfo info = holder.getBindMix();
                if (info.isExistsVideo()) {
                    MediaObject thumb = null;
                    if (i != index) {
                        if (FileUtils.isExist(info.getThumbPath())) {
                            thumb = info.getThumbObject();
                            if (null == thumb) {
                                try {
                                    thumb = new MediaObject(info.getThumbPath());
                                    thumb.setTimelineRange(info.getMediaObject().getDuration(), maxDuration);
                                } catch (InvalidArgumentException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
                    holder.getBindMix().setThumbObject(thumb);
                }
            }
        }
    }
}
