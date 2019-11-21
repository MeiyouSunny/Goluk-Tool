package com.rd.veuisdk.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.rd.vecore.VirtualVideo;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.Transition;
import com.rd.vecore.models.TransitionType;
import com.rd.veuisdk.AEActivity;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SelectMediaActivity;
import com.rd.veuisdk.adapter.DragMediaAdapter;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.ui.DraggableAddGridView;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 片段编辑
 */
public class PartEditFragment extends BaseFragment {

    private DragMediaAdapter mAdapterScene;
    private boolean mIsLongClick;

    private ArrayList<Scene> mSceneList = new ArrayList<>();
    private Scene mCurrentScene;

    private DraggableAddGridView mGridVideosArray;

    private IVideoEditorHandler mHlrVideoEditor;
    private UIConfiguration mUIConfig;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mHlrVideoEditor = (IVideoEditorHandler) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_part_edit, container, false);
        initView();
        mGridVideosArray.setAddItemInfo(mSceneList);
        mGridVideosArray.hideSort(true);

        initListView(mIndex);
        return mRoot;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapterScene = new DragMediaAdapter(getContext(), getLayoutInflater(savedInstanceState));
        mAdapterScene.setDragItemListener(mDragItemListener);
        for (Scene scene : mSceneList) {
            mAdapterScene.addItem(scene);
        }
        reload();
    }

    public void setUIConfig(UIConfiguration uiConfig) {
        mUIConfig = uiConfig;
    }


    private AdapterView.OnItemClickListener mItemListener = new AdapterView.OnItemClickListener() {
        private long lastClickTime;

        @Override
        public void onItemClick(AdapterView<?> parent, final View view,
                                int position, long id) {
            if (SystemClock.uptimeMillis() - lastClickTime < 1000) {
                // 防止频繁调用
                return;
            }
            lastClickTime = SystemClock.uptimeMillis();

            if (!mIsLongClick) {
                onDragItemClick(position);
            }
            mIsLongClick = false;
            seekToPosition(position, false);
        }
    };

    /**
     * 选中当前位置的开始时刻
     *
     * @param position
     * @param isAddItem
     */
    private void seekToPosition(int position, boolean isAddItem) {
        float progress = 0;
        if (isAddItem) {
            position += 1;
        }
        for (int n = 0; n < position; n++) {
            Scene scene = mSceneList.get(n);
            Transition transition = scene.getTransition();
            progress += scene.getDuration();
            if (transition != null) {
                if (checkMediaDuration(n + 1)) {
                    if (transition.getType() != TransitionType.TRANSITION_NULL &&
                            transition.getType() != TransitionType.TRANSITION_BLINK_BLACK &&
                            transition.getType() != TransitionType.TRANSITION_BLINK_WHITE) {
                        progress -= transition.getDuration();
                        if (!isAddItem) {
                            if (n == position - 1) {
                                progress += transition.getDuration();
                            }
                        }
                    } else {
                        if (n == position - 1) {
                            if (isAddItem) {
                                progress -= transition.getDuration();
                            } else {
                                progress += transition.getDuration();
                            }
                        }
                    }
                }
            }
        }
        if (isAddItem) {
            progress -= 0.1f;
        } else {
            progress += 0.1f;
        }
        if (progress < 0) {
            progress = 0;
        }
        if (position == 0) {
            //特别处理，第0个media，开始位置强制为0
            progress = 0;
        }
        playBackSeekTo(progress);
    }

    private void playBackSeekTo(float progress) {
        if (null != mHlrVideoEditor.getEditorVideo()) {
            mHlrVideoEditor.seekTo(Utils.s2ms(progress));
        }
    }

    /***
     * 响应当前选中项
     * @param position
     */
    private void onDragItemClick(int position) {

        mIndex = position;
        mAddItemIndex = -1;
        mGridVideosArray.resetAddItem();
        mAdapterScene.setCheckId(mIndex);
        onListViewItemSelected();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<MediaObject> tempMedias = data.getParcelableArrayListExtra(IntentConstants.EXTRA_MEDIA_LIST);
        int len = tempMedias.size();
        for (int i = 0; i < len; i++) {
            MediaObject mo = tempMedias.get(i);
            int addPosition;
            addPosition = mSceneList.size();
            Scene scene = VirtualVideo.createScene();
            scene.addMedia(mo);
            mAdapterScene.addItem(addPosition, scene);
            mSceneList.add(addPosition, scene);
        }
        mHlrVideoEditor.reload(false);
        mGridVideosArray.post(new Runnable() {

            @Override
            public void run() {
                initListView(mIndex);
            }
        });
    }


    /***
     * 响应被选中项的UI
     */
    private void onListViewItemSelected() {
        mCurrentScene = mAdapterScene.getItem(mIndex);
        if (null == mCurrentScene) {
            Log.e(TAG, "onListViewItemSelected:  mCurrentScene is null");
            return;
        }
    }


    private void initView() {
        mGridVideosArray = $(R.id.gridVideosDstArray);
        mGridVideosArray.setOnItemClickListener(mItemListener);
        mGridVideosArray.setAddItemListener(mAddItemListener);
        // 设置项目大小
        mGridVideosArray.setItemSize(R.dimen.priview_item_width_plus, R.dimen.priview_item_height_plus);
        mGridVideosArray.setHideAddItemWithoutSort(true);
    }


    private int mIndex = 0;// 记录正在编辑的视频的index
    private int mAddItemIndex = -1; // 记录当前加号按钮的index
    private final int REQUESTCODE_FOR_SORT = 13;


    private boolean checkMediaDuration(int addIndex) {
        if (addIndex < 1 || addIndex > (mSceneList.size() - 1)) {
            return false;
        }
        Scene sceneFront = mSceneList.get(addIndex - 1);
        Scene sceneBelow = mSceneList.get(addIndex);
        if (sceneFront.getDuration() < 0.5f || sceneBelow.getDuration() < 0.5f) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 删除视频
     */
    private void deleteVideo() {
        Scene scene = mAdapterScene.getItem(mIndex);
        mAdapterScene.removeItem(scene);
        mSceneList.remove(mIndex);

        int len = mSceneList.size();
        if (mIndex >= len) {
            mIndex = len - 1;
        }
        initListView(mIndex);
        reload();
    }

    protected Dialog onCreateDialog(int id) {

        Dialog dialog = null;
        String strMessage = null;
        if (id == DIALOG_REMOVE_ID) {
            strMessage = getString(R.string.remove_item);
            dialog = SysAlertDialog.showAlertDialog(getContext(), "", strMessage,
                    getString(R.string.no),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }, getString(R.string.yes),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteVideo();
                        }
                    });
        }

        return dialog;
    }

    /**
     * 删除媒体项的提示信息
     */

    private static final int DIALOG_REMOVE_ID = 3;


    /**
     * 设置适配器
     *
     * @param index 默认选中项
     */
    private void initListView(int index) {
        mGridVideosArray.setAdapter(mAdapterScene);
        mCurrentScene = mAdapterScene.getItem(mIndex);
        int len = mSceneList.size();
        if (mAddItemIndex >= len) {
            mAddItemIndex = len - 1;
        }
        if (mAddItemIndex == -1 || mAddItemIndex == 0) {
            onDragItemClick(index);
        } else {
            mGridVideosArray.setAddItemSelect(mAddItemIndex - 1);
            mAdapterScene.setCheckId(-1);
        }
    }

    public void setScene(ArrayList<Scene> scenes) {
        mSceneList = scenes;
    }


    private DragMediaAdapter.DragItemListener mDragItemListener = new DragMediaAdapter.DragItemListener() {

        @Override
        public void onRemove(int position) {
            if (mAdapterScene.getCount() == 1) {
                int msgResId = R.string.just_only_one_scene;
                try {
                    Scene scene = mAdapterScene.getItem(0);
                    if (null != scene) {
                        List<MediaObject> list = scene.getAllMedia();
                        if (null != list && list.size() >= 1) {
                            MediaType mediaType = list.get(0).getMediaType();
                            if (mediaType == MediaType.MEDIA_VIDEO_TYPE) {
                                msgResId = R.string.just_only_one_video;
                            } else if (mediaType == MediaType.MEDIA_IMAGE_TYPE) {
                                msgResId = R.string.just_only_one_image;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                onToast(msgResId);
            } else {
                onCreateDialog(DIALOG_REMOVE_ID).show();
            }

        }

        @Override
        public boolean isExt(int position) {
            if (position >= mSceneList.size()) {
                return false;
            }
            MediaObject mo = mSceneList.get(position).getAllMedia().get(0);
            VideoOb vo = (VideoOb) mo.getTag();
            if (vo == null) {
                return false;
            }
            if (vo.isExtPic == 1) {
                return true;
            } else {
                return false;
            }

        }
    };

    private void reload() {
        mHlrVideoEditor.reload(false);
    }


    private DraggableAddGridView.AddItemOnClickListener mAddItemListener = new DraggableAddGridView.AddItemOnClickListener() {

        @Override
        public void onClick(int index) {
            mAddItemIndex = index + 1;
            mAdapterScene.setCheckId(-1);
            seekToPosition(index, true);
        }

        @Override
        public void addItemClick(int type) {
            if (type == 1) {
                mAddItemIndex = -1;
                int maxCount = getMaxAppend();
                if (maxCount == 0) {
                    onToast(getString(R.string.media_un_exceed_num, mUIConfig.mediaCountLimit));
                } else {
                    SelectMediaActivity.appendMedia(getContext(), true,
                            true, maxCount, AEActivity.REQUESTCODE_FOR_APPEND);
                }
            } else if (type == 2) {
                //排序界面时，禁用当前activity中的广播追加的回调
                Intent intent = new Intent();
                intent.setClass(getContext(), com.rd.veuisdk.SortMediaActivity.class);
                intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, mSceneList);
                startActivityForResult(intent, REQUESTCODE_FOR_SORT);
            }
        }

        @Override
        public void reorderAddItem(ArrayList<Scene> arr, int drag) {
            mSceneList = arr;
            onDragItemClick(drag);
        }
    };

    private int getMaxAppend() {
        return mUIConfig.mediaCountLimit > 0 ? (mUIConfig.mediaCountLimit - mSceneList.size()) : -1;
    }
}
