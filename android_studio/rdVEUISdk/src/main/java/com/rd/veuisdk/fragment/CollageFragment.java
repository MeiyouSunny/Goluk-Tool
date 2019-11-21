package com.rd.veuisdk.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.FlipType;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SelectMediaActivity2;
import com.rd.veuisdk.TempVideoParams;
import com.rd.veuisdk.demo.VideoEditAloneActivity;
import com.rd.veuisdk.model.CollageInfo;
import com.rd.veuisdk.model.ImageItem;
import com.rd.veuisdk.ui.DragView;
import com.rd.veuisdk.ui.SubInfo;
import com.rd.veuisdk.utils.CollageManager;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * 画中画
 */
public class CollageFragment extends CollageBaseFragment {
    private boolean isAddItemIng = false; //正在添加单个画中画

    public void setCallBack(CollageFragment.CallBack callBack) {
        mCallBack = callBack;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "CollageFragment";
    }

    public static CollageFragment newInstance() {
        Bundle args = new Bundle();
        CollageFragment fragment = new CollageFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isAddItemIng = false;
        isDraging = false;
        isMixItemFirstForLine = false;
    }

    /**
     * 拖拽组件容器
     *
     * @param linearWords
     */
    public void setLinearWords(FrameLayout linearWords) {
        mLinearWords = linearWords;
    }


    public static final int REQUESTCODE_FOR_ADD_MEDIA = 300;


    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_THUMB: {
                    onEditDrag();
                }
                break;
                default: {
                }
                break;
            }
        }
    };

    private final int MSG_THUMB = 120;
    private boolean isDoing = false; //防止频繁点击

    //新增单个画中画
    private void onMixItemAdd(final MediaObject mediaObject) {
        //step 1: 构建缩略图，方便拖拽
        ThreadPoolUtils.executeEx(new Runnable() {
            @Override
            public void run() {
                String thumb = Utils.fixThumb(getContext(), mediaObject);
                if (isRunning && !TextUtils.isEmpty(thumb)) {
                    //防止获取大视频的封面太耗时 (已经onDestoryView)
                    RectF rectF = new RectF(0, 0, 0.5f, 0.5f);
                    rectF.bottom = rectF.width() / (mediaObject.getWidth() / (mediaObject.getHeight() + 0.0f));
                    rectF.offset(0, (float) (Math.random() * (1 - rectF.bottom)));
                    mediaObject.setShowRectF(rectF);
                    float duration = Utils.ms2s(mEditorHandler.getDuration());

                    mediaObject.setTimeRange(0, Math.min(mediaObject.getIntrinsicDuration(), duration));
                    mediaObject.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
                    if (mediaObject.getMediaType() == MediaType.MEDIA_IMAGE_TYPE) {
                        mediaObject.setClearImageDefaultAnimation(true);//清除图片默认的放大动画， 否则指定的区域无效
                    }
                    mediaObject.setTimelineRange(Utils.ms2s(mEditorHandler.getCurrentPosition()), duration);
                    if (null == mCurrentCollageInfo) {
                        mCurrentCollageInfo = new CollageInfo(mediaObject, thumb, new SubInfo(Utils.s2ms(mediaObject.getTimelineFrom()), Utils.s2ms(mediaObject.getTimelineTo()), mediaObject.hashCode()));
                    } else {
                        mCurrentCollageInfo.setMedia(mediaObject, thumb);
                    }
                    isDoing = false;
                    if (isRunning) {
                        mHandler.sendEmptyMessage(MSG_THUMB);
                    } else {
                        mCurrentCollageInfo = null;
                    }
                } else {
                    isDoing = false;
                    Log.e(TAG, "onMixItemAdd->run: " + thumb + " isRunning:" + isRunning);
                    mCurrentCollageInfo = null;
                }
            }
        });


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_FOR_ADD_MEDIA) {
                String mediaPath = data.getStringExtra(IntentConstants.EXTRA_MEDIA_LIST);
                if (!TextUtils.isEmpty(mediaPath)) {
                    onMixAdd(mediaPath);
                }
            }
        }
    }

    @Override
    public void onLeftClick() {
        if (isGalleryLayout()) {
            onExitMixItem();
            isAddItemIng = false;
            isItemEditing = false;
        } else {
            if (isItemEditing) {
                checkEidtSave(true, 0);
            } else {
                if (onDeleteOCancelMix(dragView)) {
                    return;
                } else {
                    if (!mModel.isEquals(TempVideoParams.getInstance().getCollageDurationChecked())) {
                        //不一致。放弃修改
                        onShowAlert();
                    } else {
                        mCallBack.onLeftClick();
                    }
                }
            }
        }
    }


    /**
     * 播放时、切换画中画编辑时、新增画中画、onRightClick 3种情况下需要保存
     *
     * @param reload
     */
    @Override
    void checkEidtSave(boolean reload, int end) {
        if (isItemEditing) {
            onSaveItemImp(reload, end);
            isItemEditing = false;
        }
    }

    @Override
    void onRightClick() {
//        Log.e(TAG, "onRightClick: " + isAddItemIng + ">>" + isMixItemFirstForLine + "  isItemEditing:" + isItemEditing);
        if (isAddItemIng) {
            onAddStep1Save();
            isAddItemIng = false;
        } else {
            int end = 0;
            if (isMixItemFirstForLine) {
                //是否处于编辑单个画中画流程，比如：调整时间线，调整位置
                pauseVideo();
                isItemEditing = true;
                end = mEditorHandler.getCurrentPosition();
            }
            if (isItemEditing) {
                //保存当前编辑的状态
                checkTitleBarVisible();
                checkEidtSave(true, end);
            } else {
                if (isGalleryLayout()) {
                    //退出选择资源
                    onExitMixItem();
                } else {
                    mCallBack.onRightClick(mModel.getList());
                }
            }
        }
    }


    private void onShowAlert() {
        SysAlertDialog.createAlertDialog(mContext,
                mContext.getString(R.string.dialog_tips),
                mContext.getString(R.string.cancel_all_changed),
                mContext.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }, mContext.getString(R.string.sure),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (mExitListener != null) {
                            mExitListener.exit(1);
                        }
                        mCallBack.onLeftClick();

                    }
                }, false, null).show();
    }

    @Override
    public int onBackPressed() {
        return super.onBackPressed();
    }

    /**
     * 编辑单个画中画的位置
     *
     * @param collageInfo
     */
    private void onEditMixRectImp(CollageInfo collageInfo) {

        //编辑已添加的片段
        if (null != collageInfo) {
            pauseVideo();
            isAddItemIng = false;
            isDraging = true;
            initDrag(collageInfo);
            //显示当前时间线轴
            mThumbNailLine.showCurrent(collageInfo.getId());
        }


    }


    /**
     * 更新时间线
     *
     * @param collageInfo
     * @param end
     */
    private void updateMixTimeLine(CollageInfo collageInfo, int end) {
        if (null != collageInfo) {
            //保存时间线
            float begin = collageInfo.getMediaObject().getTimelineFrom();
            int start = Utils.s2ms(begin);
            if (end <= start) {
                end = start + 5;
            }
//            Log.e(TAG, "updateMixTimeLine: " + begin + "<>" + end);
            SubInfo info = mThumbNailLine.update(collageInfo.getSubInfo().getId(), start, end);
            if (null != info) {
                collageInfo.setSubInfo(info.clone());
            }
            collageInfo.fixMediaLine(begin, Utils.ms2s(end));

        }
    }


    /**
     * 保存当前
     *
     * @param reload 是否已经重新加载
     */
    private void onSaveItemImp(boolean reload, int end) {

        //更改当前片段的时间线
        CollageInfo collageInfo = mCurrentCollageInfo;
//        Log.e(TAG, "onSaveItemImp: " + collageInfo + " isMixItemFirstForLine:" + isMixItemFirstForLine + " end:" + end);
        if (null != collageInfo) {
            if (isMixItemFirstForLine) {
                if (end == 0) {
                    end = Utils.s2ms(mCurrentCollageInfo.getMediaObject().getTimelineTo());
                }
                //保存时间线
                updateMixTimeLine(collageInfo, end);
            } else {
                mThumbNailLine.setShowCurrentFalse();
            }
            //保存位置
            updateMixRect(collageInfo);

            isAddItemIng = false;

            //判断是否需要添加到集合
            if (!mModel.getList().contains(collageInfo)) {
                //adapter->编辑后
                mModel.add(collageInfo);
            }
            if (reload) {
                mCollageAdapter.addAll(mModel.getList(), -1);
                //重新reload (如果是添加到末尾，则relaod seekto（0）)
                if (end == mDuration) {
                    nScrollProgress = 0;
                } else {
                    nScrollProgress = mEditorHandler.getCurrentPosition();
                }
                CollageManager.udpate(collageInfo);
                mEditorHandler.seekTo(nScrollProgress);
            }

        } else {
            isAddItemIng = false;
        }
        isMixItemFirstForLine = false;
        //状态调整为可以添加画中画的状态
        onResetAddState();
        mCurrentCollageInfo = null;
    }


    /**
     * 保存当前拖拽的位置
     *
     * @return
     */
    private boolean updateMixRect(CollageInfo collageInfo) {
        if (null != dragView) {
            int angle = dragView.getRotateAngle();
            if (null != collageInfo) {
                MediaObject src = collageInfo.getMediaObject();
                src.setShowRectF(dragView.getSrcRectF());
                src.setAngle(-angle);
                src.setFlipType(dragView.getFlipType());
                collageInfo.setDisf(dragView.getDisf());
            }
            mLinearWords.removeView(dragView);
            dragView.recycle();
            dragView = null;
            isDraging = false;
            return true;
        }
        isDraging = false;
        return false;

    }

    /**
     * 准备编辑显示位置
     */
    private void onEditDrag() {
        isAddItemIng = true;
        isMixItemFirstForLine = false;
        if (null != mCurrentCollageInfo) {
            setDragState(true);
            initDrag(mCurrentCollageInfo);
        }
    }


    /**
     * 构造拖拽组件的数据
     *
     * @param collageInfo
     * @return
     */
    private void initDrag(CollageInfo collageInfo) {
        MediaObject mediaObject = collageInfo.getMediaObject();
        int[] size = new int[]{mLinearWords.getWidth(), mLinearWords.getHeight()};

        RectF showRectF = mediaObject.getShowRectF();

        if (null == showRectF || showRectF.isEmpty()) {
            showRectF = new RectF(0, 0, 0.5f, 0.5f);
        }
        if (null != dragView) {
            //替换背景图片
            dragView.setFlipType(mediaObject.getFlipType());
            dragView.setImageStyle(collageInfo.getThumbPath(), true);
        } else {
            dragView = new DragView(mLinearWords.getContext(), -mediaObject.getAngle(), collageInfo.getDisf(), size, new PointF(showRectF.centerX(), showRectF.centerY()), collageInfo.getThumbPath(), mediaObject.getFlipType());
            dragView.setControl(true);
            dragView.setDelListener(new DragView.onDelListener() {
                @Override
                public void onDelete(DragView single) {
                    onDeleteOCancelMix(single);
                    if (isGalleryLayout()) {
                        removeGalleryFragment();
                    }
                }
            });
            dragView.setMirrorListener(new DragView.onMirrorListener() {
                @Override
                public void onMirror(DragView single, FlipType flipType) {
                    if (null != mCurrentCollageInfo) {
                        mCurrentCollageInfo.getMediaObject().setFlipType(flipType);
                    }
                }
            });
            dragView.setId(collageInfo.getSubInfo().getId());
            mLinearWords.addView(dragView);
        }


    }

    @Override
    void onPlayStateClicked() {
        if (mEditorHandler.isPlaying()) {
            pauseVideo();
            if (isMixItemFirstForLine) {
                onSaveEditMix();
            }
        } else {
            mThumbNailLine.setShowCurrentFalse();
            //先保存当前编辑的项
            checkEidtSave(true, 0);
            playVideo();
        }
    }


    @Override
    boolean onDeleteOCancelMix(DragView view) {
        boolean result = false;
        if (null != view) {
            mLinearWords.removeView(view);
            view.recycle();
        }

        if (null != mCurrentCollageInfo) {
            mModel.remove(mCurrentCollageInfo);
            mThumbNailLine.removeById(mCurrentCollageInfo.getSubInfo().getId());
            CollageManager.remove(mCurrentCollageInfo);
            mCurrentCollageInfo = null;
            result = true;
        }
        nScrollProgress = mEditorHandler.getCurrentPosition();
        mCollageAdapter.addAll(mModel.getList(), -1);
        //删除当前缩率图轴上的时间线
        mThumbNailLine.setShowCurrentFalse();

        setDragState(false);
        dragView = null;
        isItemEditing = false;
        isAddItemIng = false;
        onResetMenuUI();


        return result;
    }


    @Override
    void onAddStep1Save() {
        //正在执行添加单个画中画的流程

        int currentProgress = mEditorHandler.getCurrentPosition();
        //保存当期角度、时间线、位置
        updateMixRect(mCurrentCollageInfo);

        if (null != mCurrentCollageInfo) {
            mCurrentCollageInfo.fixMediaLine(Utils.ms2s(currentProgress), Utils.ms2s(mDuration));

            //加载进度条上的UI
            SubInfo subInfo = new SubInfo(currentProgress, currentProgress + 5, mCurrentCollageInfo.getId());
            mCurrentCollageInfo.setSubInfo(subInfo);

            //reload
            mModel.add(mCurrentCollageInfo);
            nScrollProgress = mCurrentCollageInfo.getSubInfo().getTimelinefrom();
            CollageManager.insertCollage(mCurrentCollageInfo);
            //加载进度条上的UI
            mThumbNailLine.addRect(subInfo.getStart(), subInfo.getEnd(), "", subInfo.getId());
            //UI 进度条选中、 可取消、新增
            onItemStep1SaveMenuUI(subInfo.getId());
            isMixItemFirstForLine = true;
        }
        //移除图库fragment
        removeGalleryFragment();
        //已添加的画中画
        mCollageAdapter.addAll(mModel.getList(), -1);
        //seekto并start
        mEditorHandler.seekTo(currentProgress);
        playVideo();

        isAddItemIng = false;
    }


    /**
     * 保存编辑后的参数、需要判断集合中是否存在 （）
     */
    @Override
    void onSaveEditMix() {


        int currentProgress = mEditorHandler.getCurrentPosition();

        if (null != mCurrentCollageInfo) {
            MediaObject mediaObject = mCurrentCollageInfo.getMediaObject();

            mCurrentCollageInfo.fixMediaLine(mediaObject.getTimelineFrom(), Utils.ms2s(Math.min(currentProgress, mDuration)));

            //加载进度条上的UI
            SubInfo subInfo = new SubInfo(Utils.s2ms(mediaObject.getTimelineFrom()), Utils.s2ms(mediaObject.getTimelineTo()), mCurrentCollageInfo.getId());

            mCurrentCollageInfo.setSubInfo(subInfo);

            if (!mModel.checkExit(mCurrentCollageInfo.getId())) {
                //场景：Adapter.setOnItemclick(item) ,可拖拽的状态时，清除了集合中的元素
                mModel.add(mCurrentCollageInfo);
            }
            nScrollProgress = subInfo.getTimelineTo();

            CollageManager.udpate(mCurrentCollageInfo);

            //已添加的画中画
            mCollageAdapter.addAll(mModel.getList(), -1);


            //加载进度条上的UI
            mThumbNailLine.update(subInfo.getId(), subInfo.getTimelinefrom(), subInfo.getTimelineTo());
        }
        //隐藏可调控按钮
        mThumbNailLine.setShowCurrentFalse();

        isMixItemFirstForLine = false;

        //seekto并start
        mEditorHandler.seekTo(currentProgress);
        onSaveItemCompeletedUI();
        mCurrentCollageInfo = null;
        checkTitleBarVisible();
    }

    @Override
    void onEditMixClicked(int mixId) {

        //再编辑新的画中画
        pauseVideo();

        //先保存当前编辑的项
        checkEidtSave(false, 0);

        CollageInfo collageInfo = mModel.getMixInfo(mixId);

        if (null != collageInfo) {
            if (mModel.remove(collageInfo)) {
                //从集合移除当前
                CollageManager.remove(collageInfo);

                mEditorHandler.seekTo(Utils.s2ms(collageInfo.getMediaObject().getTimelineFrom()));
            }
            mCurrentCollageInfo = collageInfo;
            isItemEditing = true;
            //调整UI状态
            checkItemMixMenuUI(mCurrentCollageInfo.getSubInfo().getId());
            nScrollProgress = collageInfo.getSubInfo().getTimelinefrom();

            onEditMixRectImp(collageInfo);
            //seek到起始位置
            mEditorHandler.seekTo(Utils.s2ms(collageInfo.getMediaObject().getTimelineFrom()));
            onScrollProgress(collageInfo.getSubInfo().getTimelinefrom());
        }
    }

    @Override
    GalleryFragment.ICallBack getVideoCallBack() {
        return mVideoCallBack;
    }

    GalleryFragment.ICallBack getPhotoCallBack() {
        return photoCallBack;
    }

    private GalleryFragment.ICallBack mVideoCallBack = new GalleryFragment.ICallBack() {
        @Override
        public void onItem(ImageItem item) {
            if (null == item) {
                SelectMediaActivity2.onMixMedia(getContext(), true, REQUESTCODE_FOR_ADD_MEDIA);
            } else {
                mScrollLayout.setEnableFullParent(false);
                onMixAdd(item.image.getDataPath());
            }
        }
    };
    private GalleryFragment.ICallBack photoCallBack = new GalleryFragment.ICallBack() {
        @Override
        public void onItem(ImageItem item) {
            if (null == item) {
                SelectMediaActivity2.onMixMedia(getContext(), false, REQUESTCODE_FOR_ADD_MEDIA);
            } else {
                mScrollLayout.setEnableFullParent(false);
                onMixAdd(item.image.getDataPath());
            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbNailLine.recycle(true);
        mThumbNailLine = null;
        mModel.recycle();
        mModel = null;


    }

    /**
     * @param path 媒体路径
     */
    private void onMixAdd(@NonNull String path) {
        try {
            MediaObject mediaObject = new MediaObject(path);
            mediaObject.setAudioMute(true);
            if (!isDoing) {
                isDoing = true;
                onMixItemAdd(mediaObject);
            }
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }


    public interface CallBack {

        /**
         *
         */
        void onLeftClick();

        /**
         * @param mixList
         */
        void onRightClick(List<CollageInfo> mixList);
    }

    private VideoEditAloneActivity.ExitListener mExitListener;

    public void setExitListener(VideoEditAloneActivity.ExitListener exitListener) {
        this.mExitListener = exitListener;
    }

    //隐藏编辑框
    public void setHideEdit() {
        if (mThumbNailLine != null) {
            mThumbNailLine.setHideCurrent();
            mCollageAdapter.addAll(mModel.getList(), -1);
        }
    }

}
