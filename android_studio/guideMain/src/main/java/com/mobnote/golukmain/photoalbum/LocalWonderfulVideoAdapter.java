package com.mobnote.golukmain.photoalbum;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.com.tiros.debug.GolukDebugUtils;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.entity.DoubleVideoInfo;
import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.util.GlideUtils;

public class LocalWonderfulVideoAdapter extends BaseAdapter implements StickyListHeadersAdapter {
    private FragmentAlbum mFragment = null;
    private Context mContext = null;
    private LayoutInflater inflater = null;
    private List<DoubleVideoInfo> mDataList = null;
    private List<String> mGroupNameList = null;
    private float density = 1;
    private int screenWidth = 0;
    private IListViewItemClickColumn onListViewItemClickColumnListener;
    /**
     * 滚动中锁标识
     */
//	private boolean lock = false;
    private String from = null;
    private int type = 0;

    public LocalWonderfulVideoAdapter(Context c, FragmentAlbum fragment, int type, String from, IListViewItemClickColumn itemClickColumnListener) {
        this.from = from;
        this.type = type;
        this.mFragment = fragment;
        this.mContext = c;
        this.inflater = LayoutInflater.from(c);
        this.density = SoundUtils.getInstance().getDisplayMetrics().density;
        this.screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
        this.mDataList = new ArrayList<>();
        this.mGroupNameList = new ArrayList<>();
        this.onListViewItemClickColumnListener = itemClickColumnListener;
    }

    public void setData(List<String> groupname, List<DoubleVideoInfo> data) {
        mDataList.clear();
        mDataList.addAll(data);
        mGroupNameList.clear();
        mGroupNameList.addAll(groupname);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return mDataList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        int width = (int) (screenWidth - 95 * density) / 2;
        int height = (int) ((float) width / 1.77f);
        if (view == null) {
            holder = new ViewHolder();
            final View convertView = inflater.inflate(R.layout.video_list_item, parent, false);
            holder.mVideoLayout1 = (RelativeLayout) convertView.findViewById(R.id.mVideoLayout1);
            holder.mVideoLayout2 = (RelativeLayout) convertView.findViewById(R.id.mVideoLayout2);
            holder.mTMLayout1 = (RelativeLayout) convertView.findViewById(R.id.mTMLayout1);
            holder.mTMLayout2 = (RelativeLayout) convertView.findViewById(R.id.mTMLayout2);
            holder.image1 = (ImageView) convertView.findViewById(R.id.video_first_needle1);
            holder.image2 = (ImageView) convertView.findViewById(R.id.video_first_needle2);
            //holder.mVideoCountTime1 = (TextView) convertView.findViewById(R.id.video_countTime1);
            //holder.mVideoCountTime2 = (TextView) convertView.findViewById(R.id.video_countTime2);
            holder.mVideoQuality1 = (TextView) convertView.findViewById(R.id.video_quality1);
            holder.mVideoQuality2 = (TextView) convertView.findViewById(R.id.video_quality2);
            holder.mVideoCreateTime1 = (TextView) convertView.findViewById(R.id.video_createtime1);
            holder.mVideoCreateTime2 = (TextView) convertView.findViewById(R.id.video_createtime2);
//			holder.mVideoSize1 = (TextView) convertView.findViewById(R.id.video_size1);
//			holder.mVideoSize2 = (TextView) convertView.findViewById(R.id.video_size2);
            holder.line = convertView.findViewById(R.id.line);
            holder.mVide1Type = (Button) convertView.findViewById(R.id.video1_type);
            holder.mVide2Type = (Button) convertView.findViewById(R.id.video2_type);
            holder.mPreView1 = (ImageView) convertView.findViewById(R.id.mPreView1);
            holder.mPreView2 = (ImageView) convertView.findViewById(R.id.mPreView2);

            RelativeLayout.LayoutParams lineParams = new RelativeLayout.LayoutParams((int) (2 * density),
                    (int) (height + 4 * density));
            lineParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            if (density == 1.5) {
                lineParams.setMargins((int) (29.5 * density), 0, (int) (12 * density), 0);
            } else {
                lineParams.setMargins((int) (29 * density), 0, (int) (12 * density), 0);
            }

            holder.line.setLayoutParams(lineParams);

            int marginTop = 0;
            // if(0 != position) {
            marginTop = (int) (4 * density);
            // }

            RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(width, height);
            layoutParams1.addRule(RelativeLayout.RIGHT_OF, R.id.line);
            layoutParams1.setMargins((int) (4 * density), marginTop, (int) (4 * density), 0);
            holder.mVideoLayout1.setLayoutParams(layoutParams1);

            RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(width, height);
            layoutParams2.setMargins(0, marginTop, 0, 0);
            layoutParams2.addRule(RelativeLayout.RIGHT_OF, R.id.mVideoLayout1);
            holder.mVideoLayout2.setLayoutParams(layoutParams2);
            holder.mVideoLayout1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onListViewItemClickColumnListener.onItemClicked(convertView, (DoubleVideoInfo) v.getTag(), IListViewItemClickColumn.COLUMN_FIRST);
                }
            });
            holder.mVideoLayout2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onListViewItemClickColumnListener.onItemClicked(convertView, (DoubleVideoInfo) v.getTag(), IListViewItemClickColumn.COLUMN_SECOND);
                }
            });
            convertView.setTag(holder);
            view = convertView;
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if (mFragment.parentViewIsMainActivity) {
            holder.mPreView1.setVisibility(View.GONE);
            holder.mPreView2.setVisibility(View.GONE);
            //holder.mVideoQuality1.setVisibility(View.GONE);
            //holder.mVideoQuality2.setVisibility(View.GONE);
        } else {
            //holder.mVideoQuality1.setVisibility(View.VISIBLE);
            //holder.mVideoQuality2.setVisibility(View.VISIBLE);
            holder.mPreView1.setVisibility(View.VISIBLE);
            holder.mPreView2.setVisibility(View.VISIBLE);
        }


        holder.image1.setImageResource(R.drawable.tacitly_pic);
        holder.image2.setImageResource(R.drawable.tacitly_pic);

        if (position > this.getCount() - 1) {
            return view;
        }

        holder.mVideoLayout2.setVisibility(View.GONE);
        VideoInfo mVideoInfo1 = mDataList.get(position).getVideoInfo1();
        VideoInfo mVideoInfo2 = mDataList.get(position).getVideoInfo2();
        holder.mTMLayout1.setTag(mVideoInfo1.videoPath);
        holder.mTMLayout2.setTag("");
        //holder.mVideoCountTime1.setText(mVideoInfo1.countTime);
        holder.mVideoCreateTime1.setText(mVideoInfo1.videoCreateDate.length()>11?mVideoInfo1.videoCreateDate.substring(11):"");
//		holder.mVideoSize1.setText(mVideoInfo1.videoSize);
//		holder.image1.setTag("image:" + mVideoInfo1.filename);
        displayVideoQuality(mVideoInfo1.videoHP, holder.mVideoQuality1);
        loadImage(mVideoInfo1.filename, holder.image1);
        int type = getVideoType(mVideoInfo1.filename);
        if (type == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
            holder.mVide1Type.setText(mContext.getResources().getString(R.string.str_wonderful_title));
            holder.mVide1Type.setBackgroundColor(mContext.getResources().getColor(R.color.photoalbum_wonderful_txt_color));
            holder.mPreView1.setImageResource(R.drawable.photo_share_icon);
        } else if (type == PhotoAlbumConfig.PHOTO_BUM_IPC_URG) {
            holder.mVide1Type.setText(mContext.getResources().getString(R.string.str_urgent_title));
            holder.mVide1Type.setBackgroundColor(mContext.getResources().getColor(R.color.photoalbum_urgent_txt_color));
            holder.mPreView1.setImageResource(R.drawable.photo_share_icon);
        } else if(type == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP){
            holder.mPreView1.setImageResource(R.drawable.photo_preview_icon);
            holder.mVide1Type.setText(mContext.getResources().getString(R.string.str_loop_title));
            holder.mVide1Type.setBackgroundColor(mContext.getResources().getColor(R.color.photoalbum_loop_txt_color));
            holder.mPreView1.setVisibility(View.GONE);
        } else if(type == PhotoAlbumConfig.PHOTO_BUM_IPC_TIMESLAPSE) {
            holder.mPreView1.setImageResource(R.drawable.photo_preview_icon);
            holder.mVide1Type.setText(mContext.getResources().getString(R.string.reduce));
            holder.mVide1Type.setBackgroundColor(mContext.getResources().getColor(R.color.photoalbum_reduce_txt_color));
            holder.mPreView1.setVisibility(View.GONE);
        }

        if (null != mVideoInfo2) {
            holder.mTMLayout2.setTag(mVideoInfo2.videoPath);
            holder.mVideoLayout2.setVisibility(View.VISIBLE);
            //holder.mVideoCountTime2.setText(mVideoInfo2.countTime);
            holder.mVideoCreateTime2.setText(mVideoInfo2.videoCreateDate.substring(11));
//			holder.mVideoSize2.setText(mVideoInfo2.videoSize);
//			holder.image2.setTag("image:" + mVideoInfo2.filename);
            displayVideoQuality(mVideoInfo2.videoHP, holder.mVideoQuality2);
            loadImage(mVideoInfo2.filename, holder.image2);

//			if (mVideoInfo2.isNew) {
//				holder.mNewIcon2.setVisibility(View.VISIBLE);
//			} else {
//				holder.mNewIcon2.setVisibility(View.GONE);
//			}

            int type2 = getVideoType(mVideoInfo2.filename);
            if (type2 == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
                holder.mPreView2.setImageResource(R.drawable.photo_share_icon);
                holder.mVide2Type.setText(mContext.getResources().getString(R.string.str_wonderful_title));
                holder.mVide2Type.setBackgroundColor(mContext.getResources().getColor(R.color.photoalbum_wonderful_txt_color));
            } else if (type2 == PhotoAlbumConfig.PHOTO_BUM_IPC_URG) {
                holder.mPreView2.setImageResource(R.drawable.photo_share_icon);
                holder.mVide2Type.setText(mContext.getResources().getString(R.string.str_urgent_title));
                holder.mVide2Type.setBackgroundColor(mContext.getResources().getColor(R.color.photoalbum_urgent_txt_color));
            } else if (type2 == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP){
                holder.mPreView2.setImageResource(R.drawable.photo_preview_icon);
                holder.mVide2Type.setText(mContext.getResources().getString(R.string.str_loop_title));
                holder.mVide2Type.setBackgroundColor(mContext.getResources().getColor(R.color.photoalbum_loop_txt_color));
                holder.mPreView2.setVisibility(View.GONE);
            } else if(type2 == PhotoAlbumConfig.PHOTO_BUM_IPC_TIMESLAPSE) {
                holder.mPreView2.setImageResource(R.drawable.photo_preview_icon);
                holder.mVide2Type.setText(mContext.getResources().getString(R.string.reduce));
                holder.mVide2Type.setBackgroundColor(mContext.getResources().getColor(R.color.photoalbum_reduce_txt_color));
                holder.mPreView2.setVisibility(View.GONE);
            }
        }
        holder.mVideoLayout1.setTag(mDataList.get(position));
        holder.mVideoLayout2.setTag(mDataList.get(position));
        updateEditState(mDataList.get(position), holder.mTMLayout1, holder.mTMLayout2);

        return view;
    }

    /**
     * 更新编辑状态
     *
     * @param mDoubleVideoInfo 视频数据信息
     * @param mTMLayout1       列表左侧编辑布局
     * @param mTMLayout2       列表右侧编辑布局
     * @author xuhw
     * @date 2015年6月8日
     */
    private void updateEditState(DoubleVideoInfo mDoubleVideoInfo, RelativeLayout mTMLayout1, RelativeLayout mTMLayout2) {
        VideoInfo mVideoInfo1 = mDoubleVideoInfo.getVideoInfo1();
        VideoInfo mVideoInfo2 = mDoubleVideoInfo.getVideoInfo2();
        List<String> selectedData = mFragment.getSelectedList();
        if (mFragment.getEditState()) {
            if (selectedData.contains(mVideoInfo1.videoPath)) {
                mTMLayout1.setVisibility(View.VISIBLE);
            } else {
                mTMLayout1.setVisibility(View.GONE);
            }

            if (null == mVideoInfo2) {
                return;
            }

            if (selectedData.contains(mVideoInfo2.videoPath)) {
                mTMLayout2.setVisibility(View.VISIBLE);
            } else {
                mTMLayout2.setVisibility(View.GONE);
            }
        } else {
            mTMLayout1.setVisibility(View.GONE);
            mTMLayout2.setVisibility(View.GONE);
        }

    }

    /**
     * 加载并显示预览图片
     *
     * @param filename 图片名称
     * @param image    显示控件
     * @author xuhw
     * @date 2015年6月8日
     */
    private void loadImage(String filename, ImageView image) {
        filename = filename.replace(".mp4", ".jpg");
        String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
//		GlideUtils.loadLocalImage(mActivity, image, filePath + File.separator + filename, R.drawable.album_default_img);
        GlideUtils.loadImage(mContext, image, filePath + File.separator + filename, R.drawable.album_default_img);
//		Bitmap mBitmap = mActivity.getBitmap(filename);
//		if (null != mBitmap) {
//			image.setImageBitmap(mBitmap);
//		} else {
//			image.setImageResource(R.drawable.album_default_img);
//			// if (lock) {
//			// return;
//			// }
//
//			String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
//
//			ImageAsyncTask.getBitmapForCache(filePath + File.separator + filename, new ICallBack() {
//				@Override
//				public void SuccessCallback(String url, Bitmap mBitmap) {
//					String filename = url.substring(url.lastIndexOf("/") + 1);
//					if (null == mBitmap) {
//						return;
//					}
//
//					Bitmap b = mActivity.getBitmap(filename);
//					if (null == b) {
//						b = mBitmap;
//						mActivity.putBitmap(filename, mBitmap);
//					} else {
//						if (null != mBitmap) {
//							if (!mBitmap.isRecycled()) {
//								mBitmap.recycle();
//								mBitmap = null;
//							}
//						}
//					}
//
//					String imagefilename = filename.replace(".jpg", ".mp4");
//					ImageView image = (ImageView) mListView.findViewWithTag("image:" + imagefilename);
//					if (null != image) {
//						image.setImageBitmap(b);
//					}
//				}
//			});
//		}
    }

    private int getVideoType(String name) {
        return PhotoAlbumConfig.getVideoTypeByName(name);
    }

    /**
     * 显示视频质量
     *
     * @param videoName 视频名称
     * @param videoHP   视频分辨率
     * @param image     显示控件
     * @author xuhw
     * @date 2015年6月8日
     */
    private void displayVideoQuality(String videoHP, TextView text) {
        if ("1080p".equals(videoHP) || "1080P".equals(videoHP)) {
            text.setText(mContext.getResources().getString(R.string.str_album_video_1080));
        } else if ("720p".equals(videoHP) || "720P".equals(videoHP)) {
            text.setText(mContext.getResources().getString(R.string.str_album_video_720));
        } else if ("480p".equals(videoHP) || "480P".equals(videoHP)) {
            text.setText(mContext.getResources().getString(R.string.str_album_video_480));
        }
    }

    @Override
    public long getHeaderId(int position) {
        long id = 0;
        String groupname = "";
        for (int i = 0; i < mGroupNameList.size(); i++) {
            groupname = mGroupNameList.get(i);
            String path1 = mDataList.get(position).getVideoInfo1().videoCreateDate;
            if (path1.contains(groupname)) {
                GolukDebugUtils.e("", "BBBBBBB=====#####====groupname=" + groupname);
                id = i;
                break;
            }
        }

        GolukDebugUtils.e("", "BBBBBBB=====#####====position=" + position + "==id=" + id);
        return id;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.video_list_groupname, parent, false);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.mTopLine = (ImageView) convertView.findViewById(R.id.mTopLine);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        if (0 == position) {
            holder.mTopLine.setVisibility(View.GONE);
        } else {
            holder.mTopLine.setVisibility(View.VISIBLE);
        }

        if (position > getCount() - 1) {
            return convertView;
        }

        String headerText = "";
        for (int i = 0; i < mGroupNameList.size(); i++) {
            if (mDataList.get(position).getVideoInfo1().videoCreateDate.contains(mGroupNameList.get(i))) {
                headerText = mGroupNameList.get(i);
                break;
            }
        }

        try {
            String time[] = headerText.split("-");
            if (3 == time.length) {
                Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH) + 1;
                int day = c.get(Calendar.DAY_OF_MONTH);
                int o_year = Integer.parseInt(time[0]);
                if (year == o_year) {
                    int o_month = Integer.parseInt(time[1]);
                    int o_day = Integer.parseInt(time[2]);

                    if (month == o_month) {
                        if (day == o_day) {
                            holder.date.setText(mContext.getString(R.string.str_today));
                        } else if (day == (o_day + 1)) {
                            holder.date.setText(mContext.getString(R.string.str_yestoday));
                        } else {
                            holder.date.setText(time[1] + "/" + time[2]);
                        }
                    } else {
                        holder.date.setText(time[1] + "/" + time[2]);
                    }

                } else {
                    String t_str = time[0] + "\n" + time[1] + "/" + time[2];

                    SpannableStringBuilder style = new SpannableStringBuilder(t_str);
                    style.setSpan(new ForegroundColorSpan(Color.rgb(0x11, 0x63, 0xa2)), 0, 4,
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    style.setSpan(new AbsoluteSizeSpan(16, true), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.date.setText(style);
                }
            } else {
                holder.date.setText(headerText);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    static class HeaderViewHolder {
        TextView year;
        TextView date;
        ImageView mTopLine;
    }

    static class ViewHolder {
        RelativeLayout mVideoLayout1;
        RelativeLayout mVideoLayout2;
        RelativeLayout mTMLayout1;
        RelativeLayout mTMLayout2;
        ImageView image1;
        ImageView image2;
        //TextView mVideoCountTime1;
        //TextView mVideoCountTime2;
        TextView mVideoQuality1;
        TextView mVideoQuality2;
        TextView mVideoCreateTime1;
        TextView mVideoCreateTime2;
        //TextView mVideoSize1;
        //TextView mVideoSize2;
        View line;
        /*ImageView mNewIcon1;
        ImageView mNewIcon2;*/
        Button mVide1Type;
        Button mVide2Type;
        ImageView mPreView1;
        ImageView mPreView2;
    }

    /**
     * 锁住后滚动时禁止下载图片
     *
     * @author xuhw
     * @date 2015年6月8日
     */
//	public void lock() {
//		lock = true;
//	}

    /**
     * 解锁后恢复下载图片功能
     *
     * @author xuhw
     * @date 2015年6月8日
     */
//	public void unlock() {
//		lock = false;
//		// this.notifyDataSetChanged();
//	}

    public interface IListViewItemClickColumn {
        int COLUMN_FIRST = 1;
        int COLUMN_SECOND = 2;

        void onItemClicked(View parent, DoubleVideoInfo videoInfo, int columnIndex);
    }
}
