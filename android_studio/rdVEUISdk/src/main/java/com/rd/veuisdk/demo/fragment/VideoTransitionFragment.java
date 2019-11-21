package com.rd.veuisdk.demo.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;
import com.rd.veuisdk.ExtPhotoActivity;
import com.rd.veuisdk.IPlayer;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SelectMediaActivity;
import com.rd.veuisdk.adapter.DragMediaAdapter;
import com.rd.veuisdk.fragment.BaseFragment;
import com.rd.veuisdk.model.ExtPicInfo;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.ui.DraggableAddGridView;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 转场
 */
public class VideoTransitionFragment extends BaseFragment {

    public static VideoTransitionFragment newInstance() {

        Bundle args = new Bundle();

        VideoTransitionFragment fragment = new VideoTransitionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static interface IVideoTransition {
        void build();
    }

    public int getCurrentIndex() {
        return nCurrentIndex;
    }


    private int nCurrentIndex = 0;

    public void onResetUI() {

        setViewVisibility(R.id.sceneLayout, true);
        setViewVisibility(R.id.llParteditAdd, false);
    }

    public void addItem(MediaObject mo, int addPosition, Scene scene, int isTxtPic, ExtPicInfo info) {
        mMediaAdapter.addItem(addPosition, scene);
        if (isTxtPic == 1) {
            addVideoObToMedia(mo, isTxtPic, info);
        } else {
            addVideoObToMedia(mo, isTxtPic, null);
        }
    }

    public void onCheck(final int mIndex) {
        mGridVideosArray.post(new Runnable() {

            @Override
            public void run() {
                initListView(mIndex);
            }
        });
    }

    public static interface IData {

        ArrayList<Scene> getSceneList();

        void onTransition(int index);
    }

    private IVideoTransition mIVideoTransition;
    private IData mData;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mData = (IData) context;
        mIPlayer = (IPlayer) context;
        mIVideoTransition = (IVideoTransition) context;
    }

    private DraggableAddGridView mGridVideosArray;
    private IPlayer mIPlayer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_video_transition_layout, container, false);
        initView();
        return mRoot;
    }

    private void initView() {
        mGridVideosArray = $(R.id.gridVideosDstArray);
        mGridVideosArray.setOnItemClickListener(mItemListener);
        mGridVideosArray.setAddItemListener(mAddItemListener);
        // 设置项目大小
        mGridVideosArray.setItemSize(R.dimen.priview_item_width_plus, R.dimen.priview_item_height_plus);
        mGridVideosArray.setHideAddItemWithoutSort(false);
        mGridVideosArray.hideSort(true);


        mGridVideosArray.setAddItemInfo(mData.getSceneList());
        mMediaAdapter = new DragMediaAdapter(getContext(), LayoutInflater.from(getContext()));
        mMediaAdapter.setDragItemListener(mDragItemListener);
        for (Scene scene : mData.getSceneList()) {
            addVideoObToMedia(scene.getAllMedia().get(0), 0, null);
            mMediaAdapter.addItem(scene);
        }

        initListView(mIndex);


        $(R.id.tvAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMedia(false);
            }
        });
        $(R.id.tvAddVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMedia(true);
            }
        });
        $(R.id.tvAddText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onText();
            }
        });
        $(R.id.tvAddTransition).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddTransition();
            }
        });

    }

    /**
     * 转场
     */
    private void onAddTransition() {
        mIPlayer.pause();
        mData.onTransition(mIndex);
    }

    private DragMediaAdapter mMediaAdapter;


    /**
     * 设置适配器
     *
     * @param index 默认选中项
     */
    private void initListView(int index) {
        mGridVideosArray.setAdapter(mMediaAdapter);
        onDragItemClick(index);
        nCurrentIndex = index;
    }

    private DragMediaAdapter.DragItemListener mDragItemListener = new DragMediaAdapter.DragItemListener() {
        @Override
        public void onRemove(int position) {
            if (mMediaAdapter.getCount() == 1) {
                int msgResId = R.string.just_only_one_scene;
                try {
                    Scene scene = mMediaAdapter.getItem(0);
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
                mIndex = position;
                onCreateDialog(DIALOG_REMOVE_ID);
            }

        }

        @Override
        public boolean isExt(int position) {
            if (position >= mData.getSceneList().size()) {
                return false;
            }
            MediaObject mo = mData.getSceneList().get(position).getAllMedia().get(0);
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
            onDragItemClick(position);
            mIPlayer.pause();
        }
    };
    private DraggableAddGridView.AddItemOnClickListener mAddItemListener = new DraggableAddGridView.AddItemOnClickListener() {

        @Override
        public void onClick(int index) {
            mIndex = index;
            mData.onTransition(index);
        }

        @Override
        public void addItemClick(int type) {

        }

        @Override
        public void reorderAddItem(ArrayList<Scene> arr, int drag) {

        }
    };

    private int mIndex;

    /***
     * 响应当前选中项
     * @param position
     */
    private void onDragItemClick(int position) {
        mIndex = position;
        mGridVideosArray.resetAddItem();
        mMediaAdapter.setCheckId(mIndex);
    }


    /**
     * 添加媒体
     *
     * @param isVideo
     */
    private void addMedia(boolean isVideo) {
        if (isVideo) {
            SelectMediaActivity.appendMedia(getContext(), false, 0, REQUESTCODE_FOR_APPEND);
        } else {
            SelectMediaActivity.appendMedia(getContext(), true, false, 0, REQUESTCODE_FOR_APPEND);
        }
    }

    public static final int REQUESTCODE_FOR_APPEND = 800;

    /**
     * 进入文字界面
     */
    private void onText() {
        ExtPhotoActivity.onTextPic(getContext(), REQUESTCODE_FOR_APPEND);
    }


    /**
     * 添加到扩展类
     */
    private void addVideoObToMedia(MediaObject media, int isextpic, ExtPicInfo info) {
        if (media.getTag() == null) {
            media.setTag(new VideoOb(media.getTrimStart(), media.getTrimEnd(), media
                    .getTrimStart(), media.getTrimEnd(), media.getTrimStart(),
                    media.getTrimEnd(), isextpic, info, VideoOb.DEFAULT_CROP));
        }
    }

    private final int DIALOG_REMOVE_ID = 455;

    private void onCreateDialog(int id) {

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
            dialog.show();
        }
    }

    private void deleteVideo() {
        Scene scene = mMediaAdapter.getItem(mIndex);
        mIPlayer.pause();
        mMediaAdapter.removeItem(scene);
        mData.getSceneList().remove(mIndex);
        int len = mData.getSceneList().size();
        if (mIndex >= len) {
            mIndex = len - 1;
        }
        initListView(mIndex);
        mIVideoTransition.build();

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMediaAdapter.onDestroy();
    }
}
