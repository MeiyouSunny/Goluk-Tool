package com.rd.veuisdk;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.AECustomTextInfo;
import com.rd.vecore.models.AEFragmentInfo;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.utils.AEFragmentUtils;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.ae.AETemplateUtils;
import com.rd.veuisdk.ae.model.AETemplateInfo;
import com.rd.veuisdk.ae.model.AETextLayerInfo;
import com.rd.veuisdk.database.TTFData;
import com.rd.veuisdk.model.AETextMediaInfo;
import com.rd.veuisdk.model.ZiShuoInfo;
import com.rd.veuisdk.net.TTFUtils;
import com.rd.veuisdk.quik.QuikHandler;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 字说
 */
public class ZishuoActivity extends BaseActivity {
    private String TAG = "ZishuoActivity";
    private PreviewFrameLayout mPreviewFrame;
    private ExtButton mBtnNext, mBtnLeft;
    private TextView mTvTitle;
    private VirtualVideoView player;
    private ImageView mIvVideoPlayState;
    private RdSeekBar mRdSeekBar;
    private TextView currentTv;
    private TextView totalTv;
    private float mCurProportion = 1f;
    private VirtualVideo mVirtualVideo;
    private List<AECustomTextInfo> mAECustomTextInfos;

    private String aeConfigText;

    private String zishuoText = "zishuotest";
    private String zipName = "zishuo";

    //30行
//    private String zishuoText = "zishuotest1";
//    private String zipName = "zishuo1";

    private String bgVideoPath;
    private String mZiShuoTextPath;
    private ZiShuoInfo mZiShuoInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserTextList.clear();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_zishuo_layout);
        SysAlertDialog.showLoadingDialog(ZishuoActivity.this, R.string.isloading);
        bgVideoPath = PathUtils.getAssetFileNameForSdcard("zishuo", ".mp4");
        ThreadPoolUtils.executeEx(new ThreadPoolUtils.ThreadPoolRunnable() {
            @Override
            public void onBackground() {
                //  字说配置文件
                AssetManager assetManager = getAssets();
                mZiShuoTextPath = PathUtils.getAssetFileNameForSdcard(zishuoText, ".txt");

                if (!com.rd.veuisdk.utils.FileUtils.isExist(bgVideoPath)) {
                    CoreUtils.assetRes2File(assetManager, "quik/zishuo.mp4", bgVideoPath);
                }
                if (!com.rd.veuisdk.utils.FileUtils.isExist(mZiShuoTextPath)) {
                    CoreUtils.assetRes2File(assetManager, "quik/" + zishuoText + ".txt", mZiShuoTextPath);
                }
                try {
                    mAECustomTextInfos = new ArrayList<>();
                    aeConfigText = AETemplateUtils.getTextConfig(mZiShuoTextPath, mAECustomTextInfos);
                    for (AECustomTextInfo aeCustomTextInfo : mAECustomTextInfos) {
                        mUserTextList.add(aeCustomTextInfo.getText());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String zip = zipName + ".zip";
                String dst = PathUtils.getRdAssetPath() + "/" + zip;
                if (!com.rd.lib.utils.FileUtils.isExist(dst)) {
                    CoreUtils.assetRes2File(assetManager, "quik/" + zip, dst);
                }
                String ziShuoDir = null;
                try {
                    ziShuoDir = com.rd.veuisdk.utils.FileUtils.unzip(dst, PathUtils.getRdAssetPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mZiShuoInfo = new ZiShuoInfo("zishuo", ziShuoDir);
            }

            @Override
            public void onEnd() {
                super.onEnd();
                if (!ZishuoActivity.this.isDestroyed()) {
                    initZiShuoAE(mZiShuoInfo);
                    mScene = initAEFragment();
                    build(false);
                }
            }
        });

        TTFData.getInstance().initilize(this);//字体初始化数据库
        initView();
        mTvTitle.setText(R.string.zishuo);
        mCurProportion = QuikHandler.ASP_916;
        mPreviewFrame.setAspectRatio(mCurProportion);
        player.setPreviewAspectRatio(mCurProportion);
        player.setAutoRepeat(true);
        mVirtualVideo = new VirtualVideo();
        mVirtualVideo.setIsZishuo(true);

        player.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isPlayingORecording) {
                    pause();
                } else {
                    start();
                }
            }
        });
        initPlayerListener(player);
    }


    private VirtualVideo mSnapshotEditor;


    public void start() {
        isPlayingORecording = true;
        player.start();
        mIvVideoPlayState.setImageResource(R.drawable.btn_pause);
        ViewUtils.fadeOut(ZishuoActivity.this, mIvVideoPlayState);
    }

    /**
     * 暂停播放
     */
    public void pause() {
        player.pause();
        mIvVideoPlayState.clearAnimation();
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
        isPlayingORecording = false;
    }

    public void seekTo(int msec) {
        player.seekTo(Utils.ms2s(msec));
        onSeekTo(msec);
    }

    public void stop() {
        player.stop();
        onSeekTo(0);
        mIvVideoPlayState.clearAnimation();
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public int getDuration() {
        return Utils.s2ms(player.getDuration());
    }

    public int getCurrentPosition() {
        return Utils.s2ms(player.getCurrentPosition());
    }


    private void initView() {
        mRdSeekBar = findViewById(R.id.sbEditor);
        currentTv = findViewById(R.id.tvCurTime);
        totalTv = findViewById(R.id.tvTotalTime);
        mPreviewFrame = findViewById(R.id.previewFrame);
        mBtnNext = findViewById(R.id.btnRight);
        mBtnLeft = findViewById(R.id.btnLeft);
        mTvTitle = findViewById(R.id.tvTitle);
        player = findViewById(R.id.player);
        mIvVideoPlayState = findViewById(R.id.ivPlayerState);
        mBtnNext.setVisibility(View.VISIBLE);
        mBtnNext.setTextColor(getResources().getColor(R.color.main_orange));
        mBtnNext.setText(R.string.export);

        ExtButton mBtnEdit = $(R.id.btnDraft);
        mBtnEdit.setVisibility(View.VISIBLE);
        mBtnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEditTextLayer();
            }
        });
        mBtnEdit.setText(R.string.edit);

        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRightButtonClick();
            }
        });

        mBtnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mRdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    float p = Utils.ms2s(progress);
                    player.seekTo(p);
                    currentTv.setText(getFormatTime(progress));
                }
            }

            private boolean isPlaying = false;

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                if ((isPlaying = player.isPlaying())) {
                    isPlaying = true;
                    player.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isPlaying) {
                    player.start();
                }
            }
        });

    }


    //管控所有的模板，（只对应一份文本）
    private ArrayList<String> mUserTextList = new ArrayList<>();


    /**
     * 获取自定义的layer文本
     *
     * @param aeTemplateInfo
     */
    private void initAETextMediaList(AETemplateInfo aeTemplateInfo) {
        AEFragmentInfo info = aeTemplateInfo.getAEFragmentInfo();
        if (null != info) {
            List<AETextMediaInfo> aeTextMediaInfos = new ArrayList<>();
            int len = info.getLayers().size();
            int replaceableIndex = 0;
            for (int i = 0; i < len; i++) {
                AEFragmentInfo.LayerInfo layerInfo = info.getLayers().get(i);
                if (!layerInfo.getName().toLowerCase().contains("ReplaceableText".toLowerCase())) {
                    continue;
                }
                String key = layerInfo.getName();
                AETextMediaInfo textMediaInfo = new AETextMediaInfo();
                AETextLayerInfo tmp = aeTemplateInfo.getTargetAETextLayer(key);
                textMediaInfo.setAETextLayerInfo(tmp, layerInfo);
                if (mAECustomTextInfos != null) {
                    if (replaceableIndex < mAECustomTextInfos.size()) {
                        textMediaInfo.setText(mAECustomTextInfos.get(replaceableIndex).getText());
                    } else {
                        textMediaInfo.setText(null);
                    }
                } else if (null != tmp) {
                    textMediaInfo.setText(tmp.getTextContent());
                } else {
                    textMediaInfo.setText(null);
                }
                aeTextMediaInfos.add(textMediaInfo);
                replaceableIndex++;
            }
            mZiShuoInfo.setAETextMediaList(aeTextMediaInfos);
        }
    }

    /**
     * @param ziShuoInfo
     */
    private void initZiShuoAE(ZiShuoInfo ziShuoInfo) {
        mAETemplateInfo = null;
        if (null != ziShuoInfo) {
            AETemplateInfo aeTemplateInfo = readAE(ziShuoInfo);
            if (null != aeTemplateInfo) {
                initAETextMediaList(aeTemplateInfo);
            }
            List<AETextMediaInfo> textMediaInfos = ziShuoInfo.getAETextMediaList();
            if (null != textMediaInfos) {
                initCustomAEText(textMediaInfos);
            }
            mAETemplateInfo = aeTemplateInfo;
        }
    }

    /**
     * 替换AELayer中的文本   ****更换文本内容为 ：“静夜思”......
     *
     * @param textMediaInfos
     */
    private void initCustomAEText(List<AETextMediaInfo> textMediaInfos) {
        int len = textMediaInfos.size();
        int j = 0;
        for (int i = 0; i < len; i++) {
            AETextMediaInfo info = textMediaInfos.get(i);
            //有文字版
            AETextLayerInfo aeTextLayerInfo = info.getAETextLayerInfo();
            if (null != aeTextLayerInfo) {
                String text = null;
                if (mUserTextList.size() > 1) {
                    //多行文字
                    if (i < mUserTextList.size()) {
                        text = mUserTextList.get(i);
                    }
                } else if (mUserTextList.size() == 1) {
                    String[] arr = mUserTextList.get(0).split("\n");
                    if (null != arr) {
                        if (j < arr.length) {
                            text = arr[j];
                        }
                    }
                    j++;
                } else {
                    text = info.getText();
                }
                if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(info.getText()) && !info.getText().equals(text)) {
                    //已经输入了文本内容，切换模板时，需更加新的内容和容器，生成新的图片
                    info.setTextMediaObj(null);
                } else {
                    //没有输入文本，填充默认的文字
                    text = info.getText();
                }

                if (info.getTextMediaObj() == null) {
                    if (!TextUtils.isEmpty(text)) {
                        //文本转图片
                        String file = AETextActivity.fixAEText(aeTextLayerInfo, text, aeTextLayerInfo.getTtfPath(), info.getLayerInfo());
                        try {
                            info.setTextMediaObj(new MediaObject(file));
                        } catch (InvalidArgumentException e) {
                            e.printStackTrace();
                        }
                    } else {
                        info.setTextMediaObj(null);
                    }
                }
            } else {
                info.setTextMediaObj(null);
            }
            textMediaInfos.set(i, info);
        }
    }


    /**
     * 读取ae模板
     *
     * @param mEffectInfo
     * @return
     */
    private AETemplateInfo readAE(ZiShuoInfo mEffectInfo) {
        String dir = mEffectInfo.getPath();
        AETemplateInfo templateInfo = null;
        if (!TextUtils.isEmpty(dir)) {
            try {
                templateInfo = AETemplateUtils.getConfig2(dir);
                if (null != templateInfo) {
                    //获取默认的layer、信息
                    AEFragmentInfo aeFragmentInfo = AEFragmentUtils.loadSync(templateInfo.getDataPath(), mAECustomTextInfos, 0);
                    if (null != aeFragmentInfo) {
                        templateInfo.setListPath(null);
                        templateInfo.setMapMediaObjects(null);
                        templateInfo.setAEFragmentInfo(false, aeFragmentInfo);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }

        }
        return templateInfo;
    }

    private AETemplateInfo mAETemplateInfo;

    /**
     * 准备重新加载
     *
     * @param needDialog
     */
    private void build(boolean needDialog) {
        if (needDialog) {
            SysAlertDialog.showLoadingDialog(this, R.string.isloading);
        }
        if (player.isPlaying()) {
            pause();
        }
        player.reset();
        player.setPreviewAspectRatio(mCurProportion);
        mVirtualVideo.reset();

        //重新加载
        reload(mVirtualVideo);
        try {
            mVirtualVideo.build(player);
            onSeekTo(0);
            start();
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }


    }


    private boolean isPlayingORecording = false;


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
                SysAlertDialog.cancelLoadingDialog();
                int ms = Utils.s2ms(player.getDuration());
                mRdSeekBar.setMax(ms);
                totalTv.setText(getFormatTime(ms));
                onSeekTo(0);
                player.setBackgroundColor(Color.BLACK);

            }

            @Override
            public boolean onPlayerError(int what, int extra) {
                Log.e(TAG, "onPlayerError: " + what + "..." + extra);
                if (extra == -14) {
                    build(false);
                }
                return false;
            }

            @Override
            public void onPlayerCompletion() {
                Log.i(TAG, "onPlayerCompletion:  播放完毕-->" + player.getDuration());

            }

            @Override
            public void onGetCurrentPosition(float position) {
                onSeekTo(Utils.s2ms(position));
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

    /**
     *
     */
    private void onSeekTo(int progress) {
        currentTv.setText(getFormatTime(progress));
        mRdSeekBar.setProgress(progress);
    }

    private final int REQUESTCODE_FOR_AETEXT = 265;

    /**
     * 编辑layer中的文本
     */
    private void onEditTextLayer() {
        pause();
        if (null != mAETemplateInfo) {
            List<AETextLayerInfo> tmp = mAETemplateInfo.getAETextLayerInfos();
            if (null != tmp && tmp.size() >= 1) {
                //编辑ae模板文字
                AEFragmentInfo info = mAETemplateInfo.getAEFragmentInfo();
                List<AEFragmentInfo.LayerInfo> layerInfos = info.getLayers();

                int len = layerInfos.size();
                ArrayList<AETextMediaInfo> aeTextMediaInfos = new ArrayList<>();

                for (int i = 0; i < len - 1; i++) {
                    AETextMediaInfo item = mZiShuoInfo.getAETextMediaList().get(i);
                    AETextLayerInfo aeTextLayerInfo = item.getAETextLayerInfo();
                    if (null != aeTextLayerInfo) {
                        aeTextMediaInfos.add(item);
                    }
                }
                if ((aeTextMediaInfos.size() > 0)) {
                    //例如Boxed 一个输入框，支持多个可替换文字的layer
                    ArrayList<String> txtList = new ArrayList<>();
                    if (mUserTextList.size() > 1) {
                        //多行文字
                        txtList.addAll(mUserTextList);
                    } else if (mUserTextList.size() == 1) {
                        //当行文字（在非boxed模板中编辑的文字（可能出现一个layer对应多行文本），到了boxed时，得拆分成一行对应一个layer）
                        String[] arr = mUserTextList.get(0).split("\n");
                        if (null != arr) {
                            int count = arr.length;
                            for (int i = 0; i < count; i++) {
                                txtList.add(arr[i]);
                            }
                        }
                    }
                    AETextActivity.onAEText(this, aeTextMediaInfos, txtList, aeConfigText, REQUESTCODE_FOR_AETEXT);
                } else {
                    //未知情况
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        lastProgress = -1;
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_FOR_AETEXT) {
                mUserTextList.clear();
                try {
                    mAECustomTextInfos.clear();
                    aeConfigText = AETemplateUtils.getTextConfig(mZiShuoTextPath, mAECustomTextInfos);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                initAETextMediaList(mAETemplateInfo);

                List<AETextActivity.AEText> aeTextList = AETextActivity.getAETextList(data);
                if (null != aeTextList) {
                    List<AETextMediaInfo> list = mZiShuoInfo.getAETextMediaList();
                    int len = 0;
                    if (null != list && (len = list.size()) > 0) {
                        for (int i = 0; i < len; i++) {
                            AETextMediaInfo mAETextMediaInfo = list.get(i);
                            AETextLayerInfo aeTextLayerInfo = mAETextMediaInfo.getAETextLayerInfo();
                            AETextActivity.AEText aeText = aeTextList.get(i);
                            String text = "";
                            if (i < mAECustomTextInfos.size()) {
                                text = mAECustomTextInfos.get(i).getText();
                            }
                            if (aeText == null) {
                                //写字板的内容不足， 不再显示没有内容的layer
                                if (null != aeTextLayerInfo) {
                                    mAETextMediaInfo.setTtf("", 0);
                                    mAETextMediaInfo.setTextMediaObj(null);
                                }
                            } else {
                                mUserTextList.add(text);
                                if (null != aeTextLayerInfo) {
                                    mAETextMediaInfo.setTtf(aeText.getTtf(), aeText.getTtfIndex());
                                    String file = AETextActivity.fixAEText(aeTextLayerInfo, text, mAETextMediaInfo.getTtf());
                                    try {
                                        mAETextMediaInfo.setTextMediaObj(new MediaObject(file));
                                    } catch (InvalidArgumentException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        }
                    }
                }
                mScene = initAEFragment();
                build(true);
            }
        } else {
            build(true);
        }
    }


    @Override
    protected void onDestroy() {
        SysAlertDialog.cancelLoadingDialog();
        TTFUtils.recycle();
        TTFData.getInstance().close();
        if (null != player) {
            player.stop();
            player.cleanUp();
            player = null;
        }
        if (mSnapshotEditor != null) {
            mSnapshotEditor.release();
            mSnapshotEditor = null;
        }
        //清理
        TempVideoParams.getInstance().recycle();
        super.onDestroy();
    }


    private float lastProgress = -1f;

    @Override
    protected void onStart() {
        super.onStart();
        isPlayingORecording = false;

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lastProgress != -1) {
            player.seekTo(lastProgress);
            onSeekTo(Utils.s2ms(lastProgress));
            start();
            lastProgress = -1f;
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        lastProgress = -1;
        if (null != player) {
            if (player.isPlaying()) {
                //暂停
                player.pause();
            }
            //记录播放器位置
            lastProgress = player.getCurrentPosition();
        }
    }


    @Override
    public void onBackPressed() {
        String strMessage = getString(R.string.quit_edit);
        SysAlertDialog.showAlertDialog(this, "", strMessage,
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }, getString(R.string.sure),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (null != player) {
                            player.stop();
                        }
                        finish();
                    }
                });
    }

    private Scene mScene;

    /**
     * 处理片头的ae模板
     */
    private Scene initAEFragment() {
        String dir = mZiShuoInfo.getPath();
        if (!TextUtils.isEmpty(dir)) {
            try {
                if (null != mAETemplateInfo) {
                    MediaObject mediaObject = new MediaObject(bgVideoPath);
                    AEFragmentInfo aeFragmentInfo = AEFragmentUtils.loadSync(mAETemplateInfo.getDataPath(), mAECustomTextInfos, mediaObject.getDuration());
                    if (null != aeFragmentInfo) {
                        HashMap<String, MediaObject> data = new HashMap<>();
                        float maxTrim = aeFragmentInfo.getDuration();//依据每个layer的时间线，,只保留有效的layer
                        List<AETextMediaInfo> list = mZiShuoInfo.getAETextMediaList();
                        if (null != list) {
                            int len = list.size();
                            for (int i = 0; i < len; i++) {
                                AETextMediaInfo item = list.get(i);
                                MediaObject textMedia = item.getTextMediaObj();
                                List<AEFragmentInfo.TimeLine> timeLineList = item.getLayerInfo().getTimeLine();
                                String key = item.getLayerInfo().getName();
                                if (null != textMedia) {
                                    data.put(key, textMedia);
                                } else {
                                    data.put(key, null);
                                    maxTrim = getMaxDuration(maxTrim, timeLineList);
                                }
                            }
                        }
                        if (zipName.equals("zishuo")) {
                            //quik/zishuo.zip->data.json 此模板时替换文件
//                            data.put("未标题-1.jpg", mediaObject);
                        }
                        mAETemplateInfo.setMapMediaObjects(data);
                        mAETemplateInfo.setAEFragmentInfo(false, aeFragmentInfo);
                        //190621 兼容字说 （解决：如果指定的layer 的最大lineEnd，小于默认的AE模板duration，ae模板绑定的部分layer上的视频不绘制(把媒体当成主媒体处理不受AE模板的duration影响)）
                        Scene scene = VirtualVideo.createScene();
                        scene.addMedia(mediaObject);
                        mAEFragmentInfo = aeFragmentInfo;
                        mMaxTrim = maxTrim;
                        return scene;
                    }
                }
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }


        }
        return null;

    }

    private AEFragmentInfo mAEFragmentInfo;
    private float mMaxTrim;

    /**
     * @param maxTrim
     * @param timeLineList
     * @return
     */
    private float getMaxDuration(float maxTrim, List<AEFragmentInfo.TimeLine> timeLineList) {
        int count = timeLineList.size();
        for (int n = 0; n < count; n++) {
            AEFragmentInfo.TimeLine line = timeLineList.get(n);
            maxTrim = Math.min(maxTrim, line.getStartTime());
        }
        return maxTrim;
    }

    /**
     * 加载视频资源
     */
    private void reload(VirtualVideo virtualVideo) {
        if (null != mScene) {
            virtualVideo.addScene(mScene);
        }
        if (null != mAEFragmentInfo) {
            virtualVideo.addAEFragment(mAEFragmentInfo, 0, mMaxTrim);
        }
    }


    /**
     * 响应确定与导出
     */
    private void onRightButtonClick() {
        pause();
        ExportHandler mExportHandler = new ExportHandler(this, new ExportHandler.IExport() {
            @Override
            public void addData(VirtualVideo virtualVideo) {
                reload(virtualVideo);
            }
        });
        mExportHandler.onExport(mCurProportion, false);
    }

}
