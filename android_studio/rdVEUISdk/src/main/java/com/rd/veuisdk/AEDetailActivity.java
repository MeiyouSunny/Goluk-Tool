package com.rd.veuisdk;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Switch;
import android.widget.TextView;

import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownListener;
import com.rd.http.MD5;
import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.models.MediaObject;
import com.rd.veuisdk.ae.AETemplateUtils;
import com.rd.veuisdk.ae.model.AETemplateInfo;
import com.rd.veuisdk.ui.ExtProgressDialog;
import com.rd.veuisdk.ui.exoplayer.RdExoPlayerView;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 展示AE详情
 */
public class AEDetailActivity extends BaseActivity {
    private PreviewFrameLayout mPreviewFrame;
    private ExtButton mBtnNext, mBtnLeft;
    private TextView mTvTitle;
    private Switch mSwBox;
    private static final String PARAM_AE = "param_ae_info";

    /**
     * 进入AE详情
     *
     * @param context
     * @param aeTemplateInfo
     * @param requestCode
     */
    public static void gotoAEDetail(Context context, AETemplateInfo aeTemplateInfo, int requestCode) {
        Intent intent = new Intent(context, AEDetailActivity.class);
        intent.putExtra(PARAM_AE, aeTemplateInfo);
        ((Activity) context).startActivityForResult(intent, requestCode);

    }

    private TextView tvAETitle, tvAEMedia;
    private AETemplateInfo mAETemplateInfo;

    private void initExoPlayer(String url) {
        mExoPlayerView.setUrl(url);
        mExoPlayerView.startPlayer();

    }

    private RdExoPlayerView mExoPlayerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics metrics = CoreUtils.getMetrics();
        if (metrics.widthPixels / (metrics.heightPixels + 0.0f) >= (9 / 16.0f) && CoreUtils.checkDeviceVirtualBar(this)) {
            //9:16 且有虚拟导航时
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_aedetail_layout);
        $(R.id.titlebar_layout).setBackgroundColor(getResources().getColor(R.color.transparent));
        mAETemplateInfo = getIntent().getParcelableExtra(PARAM_AE);
        if (null == mAETemplateInfo) {
            Log.e(TAG, "onCreate:  mAETemplateInfo is null ");
            finish();
            return;
        }
        mSwBox = $(R.id.swAERepeat);
        String path = getAEFilePath(mAETemplateInfo);
        if (FileUtils.isExist(path)) {
            mAETemplateInfo = initNextAETemp(path);
        }
        initView();
        initInfo();
        mExoPlayerView = $(R.id.exoPlayer);
        initExoPlayer(mAETemplateInfo.getVideoUrl());
    }

    /**
     * 通过解析压缩包，获取信息
     *
     * @param path
     * @return
     */
    private AETemplateInfo initNextAETemp(String path) {
        AETemplateInfo tmp = null;
        try {
            tmp = AETemplateUtils.parseAE(path);
            tmp.setName(mAETemplateInfo.getName());
            tmp.setIconPath(mAETemplateInfo.getIconPath());
            tmp.setZipFile(path);
            tmp.setCoverAsp(mAETemplateInfo.getCoverAsp(), 0, 0);
            tmp.setMediaNum(mAETemplateInfo.getPicNum(), mAETemplateInfo.getTextNum(), mAETemplateInfo.getVideoNum());
            tmp.setVideoUrl(mAETemplateInfo.getVideoUrl());
            tmp.setUrl(mAETemplateInfo.getUrl());
            tmp.setUpdatetime(mAETemplateInfo.getUpdatetime());
            return tmp;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    private void initInfo() {
        int index = 1;
        StringBuffer stringBuffer = new StringBuffer();

        if (mAETemplateInfo.getTextNum() > 0) {
            stringBuffer.append(index + ". " + getString(R.string.ae_media_word, mAETemplateInfo.getTextNum()) + " \n");
            index++;
        }
        if (mAETemplateInfo.getPicNum() > 0) {
            stringBuffer.append(index + ". " + getString(R.string.ae_media_image, mAETemplateInfo.getPicNum()) + " \n");
            index++;
        }
        if (mAETemplateInfo.getVideoNum() > 0) {
            stringBuffer.append(index + ". " + getString(R.string.ae_mode_media, mAETemplateInfo.getVideoNum()) + " \n");
            index++;
        }

        tvAEMedia.setText(stringBuffer.toString());

        mSwBox.setVisibility(isEnableRepeat() ? View.VISIBLE : View.GONE);

    }

    /**
     * 是否允许重复 规则：只有全图片时才允许重复
     */
    private boolean isEnableRepeat() {
        return (mAETemplateInfo.getVideoNum() == 0 && mAETemplateInfo.getTextNum() == 0);
    }


    private ExtProgressDialog mProgressDialog;
    private DownLoadUtils downLoadUtils;

    /**
     * 本地ae模板路径
     *
     * @param tmp
     * @return
     */
    static String getAEFilePath(AETemplateInfo tmp) {
        return PathUtils.getRdAEPath() + "/" + MD5.getMD5(tmp.getUrl()) + tmp.getUpdatetime() + ".zip";
    }

    /**
     * 是否用户主动取消
     */
    private boolean bCancelByUser = false;

    private void onSelectedImp(final AETemplateInfo info) {
        if (null == info) {
            onToast(R.string.ae_template_error);
            return;
        }
        if (TextUtils.isEmpty(info.getDataPath())) {
            //未下载
            if (CoreUtils.checkNetworkInfo(this) == CoreUtils.UNCONNECTED) {
                onToast(getString(R.string.please_check_network));
            } else {
                String localPath = getAEFilePath(info);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                bCancelByUser = false;
                downLoadUtils = new DownLoadUtils(this, info.getUrl().hashCode(), info.getUrl(), localPath);
                downLoadUtils.setInterval(1);
                downLoadUtils.setItemTime(100);
                mProgressDialog = SysAlertDialog.showProgressDialog(this, R.string.dialog_download_ing, false, true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        bCancelByUser = true;
                        if (null != downLoadUtils) {
                            downLoadUtils.setCancel();
                            downLoadUtils = null;
                        }
                        mProgressDialog = null;
                    }
                });
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.setMax(100);
                mProgressDialog.setProgress(1);
                downLoadUtils.DownFile(new IDownListener() {
                    @Override
                    public void onFailed(long l, int i) {
                        if (null != mProgressDialog) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        downLoadUtils = null;
                        if (i == DownLoadUtils.RESULT_NET_UNCONNECTED) {
                            onToast(getString(R.string.please_check_network));
                        } else {
                            if (isRunning && !bCancelByUser) {
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                onToast(getString(R.string.download_failed));
                            }
                        }
                    }

                    @Override
                    public void Canceled(long l) {
                        if (null != mProgressDialog) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        downLoadUtils = null;
                        if (isRunning && !bCancelByUser) {
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                            onToast(getString(R.string.download_failed));
                        }
                    }

                    @Override
                    public void onProgress(long l, int i) {
                        if (null != mProgressDialog) {
                            mProgressDialog.setProgress(i);
                        }
                    }

                    @Override
                    public void Finished(long l, String s) {
                        if (null != mProgressDialog) {
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                            mProgressDialog.setProgress(100);
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        downLoadUtils = null;
                        File zip = new File(getAEFilePath(info));
                        if (zip.exists()) {
                            mAETemplateInfo = initNextAETemp(zip.getAbsolutePath());
                            if (isRunning) {
                                onSelectedImp(mAETemplateInfo);
                            }
                        }
                    }
                });
            }
        } else {
            //资源已下载
            if (info.getPicNum() == 0 && info.getVideoNum() == 0) {
                //只能添加文字板
                AEPreviewActivity.gotoAEPreview(this, info, null, isEnableRepeat() && mSwBox.isChecked(), REQUEST_FOR_PREVIEW_CODE);
            } else {
                SelectMediaActivity.onAEMedia(this, info.getPicNum(), info.getVideoNum(),
                        REQUEST_FOR_SELECT_MEDIA, isEnableRepeat() && mSwBox.isChecked());
            }
        }

    }

    private void initView() {
        tvAETitle = $(R.id.aeTvTitle);
        tvAETitle.setText(mAETemplateInfo.getName());
        tvAEMedia = $(R.id.aeMedia);
        mPreviewFrame = $(R.id.previewFrame);
        ((PreviewFrameLayout) $(R.id.contentFrame)).setAspectRatio(1.0f);
        mPreviewFrame.setAspectRatio(mAETemplateInfo.getCoverAsp());
        mBtnNext = $(R.id.btnRight);
        mBtnLeft = $(R.id.btnLeft);
        mBtnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mTvTitle = $(R.id.tvTitle);
        mTvTitle.setText(R.string.mode_detail);
        mBtnNext.setVisibility(View.GONE);
        $(R.id.btnPreview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectedImp(mAETemplateInfo);
            }
        });


    }

    private final int REQUEST_FOR_PREVIEW_CODE = 600;
    private final int REQUEST_FOR_SELECT_MEDIA = 601;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_FOR_SELECT_MEDIA: {
                if (resultCode == RESULT_OK) {
                    ArrayList<MediaObject> mediaObjects = data.getParcelableArrayListExtra(IntentConstants.EXTRA_MEDIA_LIST);
                    //媒体
                    AEPreviewActivity.gotoAEPreview(this, mAETemplateInfo, mediaObjects,
                            isEnableRepeat() && mSwBox.isChecked(), REQUEST_FOR_PREVIEW_CODE);
                }
            }
            break;
            case REQUEST_FOR_PREVIEW_CODE: {
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra(SdkEntry.EDIT_RESULT);
                    Intent intent = new Intent();
                    intent.putExtra(SdkEntry.EDIT_RESULT, path);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
            break;
            default:
                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != mExoPlayerView) {
            mExoPlayerView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mExoPlayerView) {
            mExoPlayerView.onPause();
        }
        if (null != downLoadUtils) {
            downLoadUtils.setCancel();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mExoPlayerView) {
            mExoPlayerView.releasePlayers();
            mExoPlayerView = null;
        }
    }
}
