package com.rd.veuisdk;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.vecore.VirtualVideo;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;
import com.rd.veuisdk.adapter.DragMediaAdapter;
import com.rd.veuisdk.adapter.DragMediaAdapter.DragItemListener;
import com.rd.veuisdk.model.ExtPicInfo;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.ui.DraggableGridView;
import com.rd.veuisdk.ui.DraggedTrashLayout;
import com.rd.veuisdk.ui.DraggedView;
import com.rd.veuisdk.ui.DraggedView.ITashScroll;
import com.rd.veuisdk.ui.ExtListItemView;
import com.rd.veuisdk.ui.PriviewLayout;
import com.rd.veuisdk.ui.PriviewLinearLayout;
import com.rd.veuisdk.utils.BitmapUtils;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频调序
 *
 * @author scott
 */
public class SortMediaActivity extends BaseActivity {
    /**
     * 排序
     */
    static void onSortMedia(Context context, ArrayList<Scene> list, int requestCode) {
        Intent intent = new Intent(context, SortMediaActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, list);
        ((Activity) context).startActivityForResult(intent, requestCode);
        ((Activity) context).overridePendingTransition(0, 0);
    }

    private DragMediaAdapter mScenesAdapter;
    private int mIndex;
    private ArrayList<Scene> mSceneList;

    private final int REQUESTCODE_FOR_APPEND = 1;

    private DraggableGridView mSortScenesArray;
    private DraggedView mDraggedView;
    private DraggedTrashLayout mDraggedLayout;
    private PriviewLinearLayout mPriviewLinearLayout;
    private PriviewLayout mParentFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStrActivityPageName = getString(R.string.preview_sort);
        setContentView(R.layout.activity_sort_media);

        mSceneList = getIntent().getParcelableArrayListExtra(
                IntentConstants.INTENT_EXTRA_SCENE);
        mSceneList.remove(null);

        mScenesAdapter = new DragMediaAdapter(this, getLayoutInflater());

        for (Scene scene : mSceneList) {
            mScenesAdapter.addItem(scene);
        }

        mScenesAdapter.addItem(null);
        mScenesAdapter.setDragItemListener(mDragItemListener);
        mScenesAdapter.setCheckId(0);
        mScenesAdapter.sortActivity(true);

        registerReceiver(mReceiver, new IntentFilter(SdkEntry.ALBUM_CUSTOMIZE));
        initViews();

        mSortScenesArray.setAddItemInfo(mSceneList);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                SortMediaActivity.this.finish();
                SortMediaActivity.this.overridePendingTransition(0, 0);
            }
        });

    }

    private void initViews() {
        mSortScenesArray = (DraggableGridView) findViewById(R.id.gridSceneDstArray);
        mDraggedView = (DraggedView) findViewById(R.id.draggedView);
        mDraggedLayout = (DraggedTrashLayout) findViewById(R.id.thelinearDraggedLayout);
        mPriviewLinearLayout = (PriviewLinearLayout) findViewById(R.id.priviewLayoutContent);
        mParentFrame = (PriviewLayout) findViewById(R.id.rootPriviewLayout);

        findViewById(R.id.titlebar_layout).setBackgroundResource(R.color.sub_menu_bgcolor);
        findViewById(R.id.btnLeft).setVisibility(View.INVISIBLE);
        ((TextView) findViewById(R.id.tvTitle)).setText(mStrActivityPageName);

        mSortScenesArray.setLongLisenter(onDragLongListener);
        mSortScenesArray.setOnItemClickListener(mItemListener);
        // 设置项目大小
        mSortScenesArray.setItemSize(R.dimen.priview_item_height_plus,
                R.dimen.priview_item_height_plus);
        mSortScenesArray.setAdapter(mScenesAdapter);
        mSortScenesArray.setOrientation(1);
        mSortScenesArray.setAddItem(true);
    }

    @Override
    public void clickView(View v) {
        int id = v.getId();

        if (id == R.id.public_menu_cancel) {
            setResult(RESULT_CANCELED);
            onBackPressed();
        } else if (id == R.id.public_menu_sure) {
            Intent intent = new Intent();

            ArrayList<Scene> scenes = mScenesAdapter.getMediaList();
            scenes.remove(scenes.size() - 1);
            intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scenes);
            setResult(RESULT_OK, intent);
            onBackPressed();
        }
    }

    private OnItemClickListener mItemListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, final View view,
                                int position, long id) {
            if (position == mScenesAdapter.getCount() - 1) {
                if (SdkEntry.getSdkService().getUIConfig().useCustomAlbum) {
                    SdkEntryHandler.getInstance().onSelectVideo(
                            SortMediaActivity.this);
                } else {
                    int mediaLimit = SdkEntry.getSdkService().getUIConfig().mediaCountLimit;
                    int max = mediaLimit > 0 ? mediaLimit - mSceneList.size() : -1;
                    if (max == 0) {
                        onToast(getString(R.string.media_un_exceed_num, mediaLimit));
                    } else {
                        SelectMediaActivity.appendMedia(SortMediaActivity.this, false, max, REQUESTCODE_FOR_APPEND);
                    }
                }

            } else {
                mIndex = position;
                mScenesAdapter.setCheckId(position);
            }
        }
    };

    private DragItemListener mDragItemListener = new DragItemListener() {

        @Override
        public void onRemove(int position) {
            if (mScenesAdapter.getCount() == 2) {
                Scene scene = mScenesAdapter.getItem(mIndex);
                int msgResId = R.string.just_only_one_image;
                List<MediaObject> list = scene.getAllMedia();
                if (null != list && list.size() >= 0 && list.get(0).getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                    msgResId = R.string.just_only_one_video;
                }
                SysAlertDialog.showAutoHideDialog(SortMediaActivity.this, null,
                        getString(msgResId), Toast.LENGTH_SHORT);
            } else {
                deleteVideo();
            }

        }

        @Override
        public boolean isExt(int position) {
            VideoOb vo = (VideoOb) mScenesAdapter.getItem(position).getAllMedia().get(0).getTag();
            if (vo == null || vo.isExtPic == 0) {
                return false;
            } else {
                return true;
            }
        }
    };

    private final int DIALOG_REMOVE_ID = 1;

    @Override
    protected Dialog onCreateDialog(int id) {

        Dialog dialog = null;

        if (id == DIALOG_REMOVE_ID) {
            dialog = SysAlertDialog.showAlertDialog(this, "",
                    getString(R.string.isdelete), getString(R.string.no),
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

    private DraggableGridView.onLonglistener onDragLongListener = new DraggableGridView.onLonglistener() {

        @Override
        public void onLong(int index, final View chid) {
            if (index == mScenesAdapter.getCount() - 1) {
                return;
            }
            mDraggedView.setTrashListener(null);
            mDraggedView.setScollListener(null);

            ExtListItemView item = (ExtListItemView) chid.findViewById(R.id.ivItemExt);

            if (null != item) {
                final Bitmap bmp = BitmapUtils.copyBmp(item.getBmpCache(),
                        item.getWidth() + 16, item.getHeight() + 9);

                mPriviewLinearLayout.setEnableTouch(false);
                mParentFrame.setForceToTarget(true);
                // step 2 :setdata到dragview;且高亮显示,回调方法
                if (null != bmp) {
                    mDraggedView.setTrashListener(mTashListener);
                    mDraggedLayout.setVisibility(View.VISIBLE);
                    final int[] location = new int[2];
                    chid.getLocationOnScreen(location);
                    int[] top = new int[2];
                    mParentFrame.getLocationOnScreen(top);

                    final int mtop = location[1] - top[1];

                    mDraggedView.postDelayed(new Runnable() {

                        @Override
                        public void run() { // 计算中心点

                            mDraggedView.initTrashRect(0);
                            mDraggedView.setData(bmp, location[0], mtop,
                                    location[0] + bmp.getWidth(),
                                    mtop + bmp.getHeight());

                        }
                    }, 50);

                    mDraggedView.setScollListener(new ITashScroll() {

                        @Override
                        public void onTouchMove(int x, int y) {
                            mSortScenesArray.doActionMove(x, y);
                        }
                    });
                }
                mScenesAdapter.setCheckId(index);
            }
        }

        @Override
        public void onCancel() {
            mDraggedView.onCancel();

        }

    };

    private DraggedView.onTrashListener mTashListener = new DraggedView.onTrashListener() {

        @Override
        public void onDelete() {
        }

        @Override
        public void onCancel() {
            if (mDraggedLayout.getVisibility() == View.VISIBLE) {
                mDraggedLayout.setVisibility(View.GONE);
            }
            mParentFrame.setForceToTarget(false);
            mPriviewLinearLayout.setEnableTouch(true);

            mParentFrame.post(new Runnable() {

                @Override
                public void run() {
                    mSortScenesArray.reset();
                }
            });
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_FOR_APPEND) {
                ArrayList<MediaObject> tempMedias = data.getParcelableArrayListExtra(IntentConstants.EXTRA_MEDIA_LIST);
                int isextPic = data.getIntExtra(IntentConstants.EXTRA_EXT_ISEXTPIC, 0);
                int len = tempMedias.size();
                for (int i = 0; i < len; i++) {
                    MediaObject mo = tempMedias.get(i);
                    Scene scene = VirtualVideo.createScene();
                    scene.addMedia(mo);
                    mSceneList.add(i + mIndex + 1, scene);
                    mScenesAdapter.addItem(i + mIndex + 1, scene);
                    if (isextPic == 1) {
                        addVideoObToMedia(mo, isextPic,
                                (ExtPicInfo) data.getParcelableExtra(IntentConstants.EXTRA_EXT_PIC_INFO));
                    } else {
                        addVideoObToMedia(mo, isextPic, null);
                    }
                }
                mSortScenesArray.post(new Runnable() {
                    @Override
                    public void run() {
                        mSortScenesArray.setAdapter(mScenesAdapter);
                        mScenesAdapter.setCheckId(mIndex);
                    }
                });
            }
        }
    }


    /**
     * 添加到扩展类
     */
    private void addVideoObToMedia(MediaObject media, int isextpic, ExtPicInfo info) {
        media.setTag(new VideoOb(media.getTrimStart(), media.getTrimEnd(), media
                .getTrimStart(), media.getTrimEnd(), media.getTrimStart(),
                media.getTrimEnd(), isextpic, info, VideoOb.DEFAULT_CROP));
    }

    /**
     * 删除视频
     */
    private void deleteVideo() {
        Scene scene = mScenesAdapter.getItem(mIndex);
        mScenesAdapter.removeItem(scene);
        mSceneList.remove(mIndex);
        mSortScenesArray.setAdapter(mScenesAdapter);
        int len = mScenesAdapter.getCount();
        if (mIndex >= len - 1) {
            mIndex = len - 2;
        }
        mScenesAdapter.setCheckId(mIndex);
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, SdkEntry.ALBUM_CUSTOMIZE)) {
                final ArrayList<String> arrPath = intent
                        .getStringArrayListExtra(SdkEntry.MEDIA_PATH_LIST);
                final ArrayList<MediaObject> alMedias = new ArrayList<MediaObject>();
                mSortScenesArray.post(new Runnable() {

                    @Override
                    public void run() {
                        for (String nMediaKey : arrPath) {
                            if (!TextUtils.isEmpty(nMediaKey)) {
                                Scene scene = VirtualVideo.createScene();
                                MediaObject mo = null;
                                try {
                                    mo = scene.addMedia(nMediaKey);
                                    if (mo != null) {
                                        mo.setTag(VideoOb.createVideoOb(mo.getMediaPath()));
                                        alMedias.add(mo);
                                    }
                                } catch (InvalidArgumentException e) {
                                    e.printStackTrace();
                                    onToast(getString(R.string.media_exception));
                                }
                            }
                        }
                        if (alMedias.size() == 0) {
                            return;
                        }
                        int len = alMedias.size();
                        for (int i = 0; i < len; i++) {
                            MediaObject mo = alMedias.get(i);
                            Scene scene = VirtualVideo.createScene();
                            scene.addMedia(mo);
                            mSceneList.add(i + mIndex + 1, scene);
                            mScenesAdapter.addItem(i + mIndex + 1, scene);
                        }

                        mSortScenesArray.setAdapter(mScenesAdapter);
                        mScenesAdapter.setCheckId(mIndex);
                    }
                });
            }
        }
    };
}
