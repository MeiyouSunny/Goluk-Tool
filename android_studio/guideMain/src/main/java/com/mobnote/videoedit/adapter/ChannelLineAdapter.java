package com.mobnote.videoedit.adapter;

import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.makeramen.dragsortadapter.DragSortAdapter;
import com.makeramen.dragsortadapter.NoForegroundShadowBuilder;
import com.mobnote.golukmain.R;
import com.mobnote.videoedit.AfterEffectActivity;
import com.mobnote.videoedit.bean.ChunkBean;
import com.mobnote.videoedit.bean.DummyFooterBean;
import com.mobnote.videoedit.bean.DummyHeaderBean;
import com.mobnote.videoedit.bean.ProjectItemBean;
import com.mobnote.videoedit.bean.TailBean;
import com.mobnote.videoedit.bean.TransitionBean;
import com.mobnote.videoedit.utils.DeviceUtil;
import com.mobnote.videoedit.utils.VideoEditUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import cn.npnt.ae.AfterEffect;
import cn.npnt.ae.model.Chunk;
import cn.npnt.ae.model.ChunkThumbs;
import cn.npnt.ae.model.VideoThumb;


public class ChannelLineAdapter extends
		DragSortAdapter<ChannelLineAdapter.ProjectItemViewHolder> {

	private final int VIEW_TYPE_HEADER = 0;
	private final int VIEW_TYPE_CHUNK = 1;
	private final int VIEW_TYPE_TRANSITION = 2;
	private final int VIEW_TYPE_CHUNK_TAIL = 3;
	private final int VIEW_TYEE_FOOTER = 4;
	private List<ProjectItemBean> mDataList;
	private Context mContext;
	private int mFooterWidth;
	RecyclerView mRecyclerView;

	private AfterEffectActivity mAEActivity;

	int mEditIndex = -1;
	AfterEffect mAfterEffect;

//	String mVideoPath = VideoEditConstant.VIDEO_PATH_1;
    private void refreshListFocusStatus(int index) {
        if(null == mDataList || mDataList.size() == 0) {
            return;
        }

        if(-1 == index) {
            for(ProjectItemBean bean : mDataList) {
                if(bean instanceof ChunkBean) {
                    ChunkBean chunkBean = (ChunkBean)bean;
                    chunkBean.isEditState = false;
                }
            }
        } else {
            int size = mDataList.size();
            for(int i = 0; i < size; i++) {
                ProjectItemBean bean = mDataList.get(i);
                if(bean instanceof ChunkBean) {
                    ChunkBean chunkBean = (ChunkBean)bean;
                    if(i == index) {
                        chunkBean.isEditState = true;
                    } else {
                        chunkBean.isEditState = false;
                    }
                }
            }
        }
    }

	public int getEditIndex() {
		return mEditIndex;
	}

	public void setEditIndex(int index) {
		mEditIndex = index;
	}

	public static final String TAG = ChannelLineAdapter.class.getSimpleName();

	public void setData(List<ProjectItemBean> src) {
		mDataList = src;
	}

	public ChannelLineAdapter(Context cxt,
			RecyclerView recyclerView, List<ProjectItemBean> dataList, AfterEffect ae) {
		super(recyclerView);
		this.mDataList = dataList;
		this.mContext = cxt;
		this.mAEActivity = (AfterEffectActivity) cxt;
		mRecyclerView = recyclerView;
		mAfterEffect = ae;
		mFooterWidth = DeviceUtil.getScreenWidthSize(mContext) - DeviceUtil.dp2px(mContext, 65);
	}

	public void addChunk(String path) {
		if(mDataList == null) {
			mDataList = new ArrayList<ProjectItemBean>();
		}

//		Toast.makeText(mContext, "add more", Toast.LENGTH_SHORT).show();
		((AfterEffectActivity)mContext).addChunk(path);
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		Object obj = mDataList.get(position);
		if(obj instanceof DummyHeaderBean) {
			return VIEW_TYPE_HEADER;
		} else if(obj instanceof DummyFooterBean) {
			return VIEW_TYEE_FOOTER;
		} else if(obj instanceof ChunkBean) {
			return VIEW_TYPE_CHUNK;
		} else if(obj instanceof TransitionBean) {
			return VIEW_TYPE_TRANSITION;
		} else if(obj instanceof TailBean) {
			return VIEW_TYPE_CHUNK_TAIL;
		}

		return -1;
	}

	@Override
	public ProjectItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

		if(VIEW_TYPE_HEADER == viewType) {
			View view = inflater.inflate(R.layout.ae_data_item_header_layout, viewGroup, false);
			return new HeaderViewHolder(this, view);
		}else if(VIEW_TYEE_FOOTER == viewType) {
			View view = inflater.inflate(R.layout.ae_data_item_footer_layout, viewGroup, false);
			return new FooterViewHolder(this, view, mFooterWidth);
		}else if(VIEW_TYPE_CHUNK_TAIL == viewType) {
			View view = inflater.inflate(R.layout.ae_data_item_tail_layout, viewGroup, false);
			return new ChunkTailViewHolder(this, view, mAEActivity.getTailWidth());
		}else if(VIEW_TYPE_TRANSITION == viewType) {
			View view = inflater.inflate(R.layout.ae_data_item_transition_layout, viewGroup, false);
			return new TransitionViewHolder(this, view);
		}else if(VIEW_TYPE_CHUNK == viewType) {
			View view = inflater.inflate(R.layout.ae_data_item_chunk_layout, viewGroup, false);
			ChunkViewHolder vHolder = new ChunkViewHolder(this, view);
			view.setOnLongClickListener(vHolder);
			return vHolder;
		}

		return null;
	}

	static class ChunkViewHolder extends ProjectItemViewHolder implements
		View.OnClickListener, View.OnLongClickListener {

		LinearLayout nChunkContainerLL;
		View nChunkMaskLL;
		TextView nChunkDurationTV;
		View nChunkFrameFL;

		public ChunkViewHolder(DragSortAdapter<?> dragSortAdapter, View itemView) {
			super(dragSortAdapter, itemView);
			nChunkContainerLL = (LinearLayout)itemView.findViewById(R.id.ll_ae_data_chunk);
			nChunkMaskLL = itemView.findViewById(R.id.v_ae_data_chunk_mask);
			nChunkDurationTV = (TextView)itemView.findViewById(R.id.tv_ae_data_chunk_duration);
			nChunkFrameFL = itemView.findViewById(R.id.fl_ae_data_chunk);
		}

		@Override
		public boolean onLongClick(View v) {
			startDrag();
			return true;
		}

		@Override
		public void onClick(View v) {

		}
	}

	static class TransitionViewHolder extends ProjectItemViewHolder {
		public TransitionViewHolder(DragSortAdapter<?> dragSortAdapter,
				View itemView) {
			super(dragSortAdapter, itemView);
			// TODO Auto-generated constructor stub
		}
	}

	static class HeaderViewHolder extends ProjectItemViewHolder {
		public HeaderViewHolder(DragSortAdapter<?> dragSortAdapter, View itemView) {
			super(dragSortAdapter, itemView);
			// TODO Auto-generated constructor stub
		}
	}

    static class ChunkTailViewHolder extends ProjectItemViewHolder {
        LinearLayout nTailLL;
        public ChunkTailViewHolder(DragSortAdapter<?> dragSortAdapter, View itemView, int tailWidth) {
            super(dragSortAdapter, itemView);
            nTailLL = (LinearLayout)itemView.findViewById(R.id.ll_ae_data_tail);
        }
    }

	static class FooterViewHolder extends ProjectItemViewHolder {
		ImageView nAddChunkIV;
		TextView nChannelTimeIV;

		public FooterViewHolder(DragSortAdapter<?> dragSortAdapter, View itemView, int FooterWidth) {
			super(dragSortAdapter, itemView);
			// TODO Auto-generated constructor stub
			nAddChunkIV = (ImageView)itemView.findViewById(R.id.iv_ae_data_add);
			nChannelTimeIV = (TextView)itemView.findViewById(R.id.tv_ae_data_totaltime);

			ViewGroup.LayoutParams lp =  itemView.getLayoutParams();
			lp.width = FooterWidth;
			itemView.setLayoutParams(lp);
		}
	}

	@Override
	public void onBindViewHolder(final ProjectItemViewHolder holder, final int position) {
		ProjectItemBean bean = mDataList.get(position);

		if(holder instanceof ChunkViewHolder) {
			final ChunkViewHolder viewHolder = (ChunkViewHolder)holder;
			viewHolder.nChunkContainerLL.removeAllViews();
			if(bean instanceof ChunkBean) {
				final ChunkBean chunkBean = (ChunkBean)bean;
				Chunk chunk = chunkBean.chunk;
				if(null != chunk) {
					ChunkThumbs chunkThumbs = chunk.getChunkThumbs();
					List<VideoThumb> videoThumbList = chunkThumbs.getThumbs();

					float delta = chunkThumbs.getLength();

					int bitmapCount = 0;
					if(delta > 0 && delta <= 1) {
						bitmapCount = 1;
					}

					if(delta > 1) {
						if(delta - (int)delta > 0) {
							bitmapCount = (int)delta + 1;
						} else {
							bitmapCount = (int)delta;
						}
					}

					if(null != videoThumbList && videoThumbList.size() > 0) {
						int count = videoThumbList.size();
						for(int i = 0; i < bitmapCount && i < count; i++) {
							VideoThumb videoThumb = videoThumbList.get(i);
							ImageView imageView = new ImageView(mContext);
							// Last, to calc bitmap width
							if(i == bitmapCount - 1) {
                                float lastD = delta - (int)delta;
                                if(lastD == 0f) {// This which means the last should show one complete bitmap
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                            (int)(chunkThumbs.getThumbWidth()),
                                            LayoutParams.MATCH_PARENT);
                                    imageView.setLayoutParams(params);
                                } else {
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                            (int) (chunkThumbs.getThumbWidth() * lastD),
                                            LayoutParams.MATCH_PARENT);
                                    imageView.setLayoutParams(params);
                                }
							} else {
								LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
										chunkThumbs.getThumbWidth(),
										LayoutParams.MATCH_PARENT);
								imageView.setLayoutParams(params);
							}
							imageView.setScaleType(ImageView.ScaleType.FIT_XY);
							imageView.setImageBitmap(videoThumb.getBitmap());
							viewHolder.nChunkContainerLL.addView(imageView);
						}
					}
				}

				int visibility = (getDraggingId() == chunkBean.index_tag ? View.INVISIBLE
						: View.VISIBLE);
//				viewHolder.nChunkContainerLL.setVisibility(visibility);
//				viewHolder.nChunkMaskLL.setVisibility(visibility);
//				viewHolder.nChunkDurationTV.setVisibility(visibility);
				viewHolder.nChunkFrameFL.setVisibility(visibility);

				viewHolder.nChunkContainerLL.postInvalidate();
				if(chunkBean.isEditState) {
					// Set mask layout params
					if(null != chunk) {
						float begin = chunk.getChunkThumbs().getBegin();
						float end = chunk.getChunkThumbs().getLength();

						FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
							(int)(end * chunk.getChunkThumbs().getThumbWidth()),
							FrameLayout.LayoutParams.MATCH_PARENT);

						viewHolder.nChunkMaskLL.setLayoutParams(params);
						viewHolder.nChunkMaskLL.setVisibility(View.VISIBLE);
						viewHolder.nChunkDurationTV.setVisibility(View.VISIBLE);
					}
				} else {
					viewHolder.nChunkMaskLL.setVisibility(View.GONE);
					viewHolder.nChunkDurationTV.setVisibility(View.GONE);
				}

                int duration = (int)(chunk.getDuration() * 10);
                viewHolder.nChunkDurationTV.setText("" + (float)duration / 10 + "\'\'");

                // Chunk click edit, mutual click
                viewHolder.nChunkContainerLL.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(-1 == mEditIndex) { // no selection before
                            mEditIndex = position;
//						chunkBean.isEditState = true;
                            refreshListFocusStatus(mEditIndex);
//						notifyItemChanged(mEditIndex);
                            mAEActivity.showEditController();
                        } else {
                            if(mEditIndex == position) { // tap same item to cancel selection
//								chunkBean.isEditState = false;
//								mEditIndex = -1;
//								notifyItemChanged(position);
//								mAEActivity.showMusicController();
                            } else {
                                ProjectItemBean bean = mDataList.get(mEditIndex);
//							if(bean instanceof ChunkBean) {
//								ChunkBean preBean = (ChunkBean)bean;
//								preBean.isEditState = !preBean.isEditState;
//							}
//							notifyItemChanged(mEditIndex);
///							chunkBean.isEditState = !chunkBean.isEditState;

//							notifyItemChanged(position);
                                mEditIndex = position;
                                refreshListFocusStatus(mEditIndex);
                                mAEActivity.showEditController();
                            }
                        }
                        notifyDataSetChanged();
                        mAEActivity.moveChunk2Gate(mEditIndex);
                        mAEActivity.setEditChunkVolume();
                    }
                });

				viewHolder.nChunkContainerLL.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						viewHolder.startDrag();
						return true;
					}
				});

				viewHolder.nChunkContainerLL.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						mAfterEffect.playPause();
						return false;
					}
				});

//				viewHolder.nChunkContainerLL.setOnGenericMotionListener(new OnGenericMotionListener() {
//					@Override
//					public boolean onGenericMotion(View v, MotionEvent event) {
//						if(event.getAction() == MotionEvent.ACTION_MOVE) {
//							float x = event.getX();
//							float y = event.getY();
//						}
//						return true;
//					}
//				});
//
//				viewHolder.nChunkContainerLL.setOnTouchListener(new OnTouchListener() {
//					@Override
//					public boolean onTouch(View v, MotionEvent event) {
//						chunkBean.isEditState = !chunkBean.isEditState;
//						notifyItemChanged(position);
//						return false;
//					}
//				});
			}
		} else if(holder instanceof FooterViewHolder) {
			FooterViewHolder viewHolder = (FooterViewHolder)holder;
			viewHolder.nAddChunkIV.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mAEActivity.goToChooseVideo();
				}
			});

			viewHolder.nAddChunkIV.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					mAfterEffect.playPause();
					return false;
				}
			});

			float duration = ((AfterEffectActivity)mContext).getChannelDuration();
			int trimDuration = (int)(duration * 10);
			viewHolder.nChannelTimeIV.setText("" + (float)trimDuration / 10 + "s");
		} else if(holder instanceof ChunkTailViewHolder) {
//            ChunkTailViewHolder viewHolder = (ChunkTailViewHolder)holder;
//            ViewGroup.LayoutParams lp = viewHolder.nTailLL.getLayoutParams();
//            lp.width = (int)(VideoEditConstant.VIDEO_TAIL_TIME_DURATION /
//                    VideoEditConstant.BITMAP_TIME_INTERVAL * mAEActivity.getTailWidth());
//            viewHolder.nTailLL.setLayoutParams(lp);
        }
	}

	@Override
	public long getItemId(int position) {
		return mDataList.get(position).index_tag;
	}

	@Override
	public int getItemCount() {
		return mDataList.size();
	}

	@Override
	public int getPositionForId(long id) {
		for (int i = 0; i < mDataList.size(); i++) {
			ProjectItemBean bean = mDataList.get(i);
			if (bean.index_tag == (int) id) {
				return i;
			}
		}
		return -1;
	}

	private void swapChunk(int fromPosition, int toPosition) {
//		mDataList.add(toPosition, mDataList.remove(fromPosition));
		ProjectItemBean fromBean = mDataList.get(fromPosition);
		ProjectItemBean toBean = mDataList.get(toPosition);
		mDataList.set(toPosition, fromBean);
		mDataList.set(fromPosition, toBean);
		mAfterEffect.editExchangeChunk(VideoEditUtils.mapI2CIndex(toPosition), VideoEditUtils.mapI2CIndex(fromPosition));
		// Continue process transition
//		mDataList.add(toPosition + 1, mDataList.remove(fromPosition + 1));
		if(mEditIndex == toPosition) {
			mEditIndex = fromPosition;
		}

		if(mEditIndex == fromPosition) {
			mEditIndex = toPosition;
		}
	}

	@Override
	public boolean move(int fromPosition, int toPosition) {
		ProjectItemBean bean = mDataList.get(toPosition);
		if(bean instanceof ChunkBean) {
//			mDataList.add(toPosition, mDataList.remove(fromPosition));
			swapChunk(fromPosition, toPosition);
			return true;
		} else {
			return false;
		}
	}

	static class ProjectItemViewHolder extends DragSortAdapter.ViewHolder implements
			View.OnClickListener, View.OnLongClickListener {

		public ProjectItemViewHolder(DragSortAdapter<?> adapter, View itemView) {
			super(adapter, itemView);
		}

		@Override
		public void onClick(View v) {

		}

		@Override
		public boolean onLongClick(View v) {
//			startDrag();
			return false;
		}

		@Override
		public View.DragShadowBuilder getShadowBuilder(View itemView,
				Point touchPoint) {
			return new NoForegroundShadowBuilder(itemView, touchPoint);
		}
	}

}
