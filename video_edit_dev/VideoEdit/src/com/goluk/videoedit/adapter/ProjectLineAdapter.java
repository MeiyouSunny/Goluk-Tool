package com.goluk.videoedit.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import cn.npnt.ae.model.Chunk;
import cn.npnt.ae.model.ChunkThumbs;
import cn.npnt.ae.model.VideoThumb;

import com.goluk.videoedit.R;
import com.goluk.videoedit.bean.ChunkBean;
import com.goluk.videoedit.bean.DummyFooterBean;
import com.goluk.videoedit.bean.DummyHeaderBean;
import com.goluk.videoedit.bean.ProjectItemBean;
import com.goluk.videoedit.bean.TailBean;
import com.goluk.videoedit.bean.TransitionBean;
import com.makeramen.dragsortadapter.DragSortAdapter;
import com.makeramen.dragsortadapter.NoForegroundShadowBuilder;
import com.makeramen.dragsortadapter.DragSortAdapter.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import utils.DeviceUtil;



public class ProjectLineAdapter extends
		DragSortAdapter<ProjectLineAdapter.ProjectItemViewHolder> {

	private final int VIEW_TYPE_HEADER = 0;
	private final int VIEW_TYPE_CHUNK = 1;
	private final int VIEW_TYPE_TRANSITION = 2;
	private final int VIEW_TYPE_CHUNK_TAIL = 3;
	private final int VIEW_TYEE_FOOTER = 4;
	private List<ProjectItemBean> mDataList;
	private Context mContext;
	private int mFooterWidth;

	public static final String TAG = ProjectLineAdapter.class.getSimpleName();

//	private List<BitmapWrapper> data;

	public void setData(List<ProjectItemBean> src) {
		mDataList = src;
	}

//	public ExampleAdapter(RecyclerView recyclerView, List<BitmapWrapper> data) {
//		super(recyclerView);
//		this.data = data;
//	}
	public ProjectLineAdapter(Context cxt,
			RecyclerView recyclerView, List<ProjectItemBean> dataList) {
		super(recyclerView);
		this.mDataList = dataList;
		this.mContext = cxt;
		mFooterWidth = DeviceUtil.getScreenWidthSize(mContext) - DeviceUtil.dp2px(mContext, 65);
	}

	//TODO: TBD
	public void addSection(){
		if(mDataList == null){
			mDataList = new ArrayList<ProjectItemBean>();
		}

//		mDataList.add(new AEDataBean(1));
		this.notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub

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
//		if(position == 0){
//			return VIEW_TYPE_HEADER;
//		}
//		if(position == mItemCount - 1){
//			return VIEW_TYEE_FOOTER;
//		}
//
//		if(isTailItem(position)){
//			return VIEW_TYPE_CHUNK_TAIL;
//		}
//
//		if(mDataList != null && mDataList.size()*2 + 1> position){
//
//			if(position%2 == 1){
//				return VIEW_TYPE_CHUNK;
//			}else{
//				return VIEW_TYPE_TRANSITION;
//			}
//		}
		return -1;
	}
	


//	@Override
//	public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
//		View view = inflater.inflate(R.layout.item_rv_chunk_layout, parent, false);
//		MainViewHolder holder = new MainViewHolder(this, view);
//		view.setOnClickListener(holder);
//		view.setOnLongClickListener(holder);
//		return holder;
//	}

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
			return new ChunkTailViewHolder(this, view);
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

//	private boolean isTailItem(int position){
//
//		if(position == this.getItemCount() -2 ){
//			return true;
//		}	
//		return false;
//	}

	static class ChunkViewHolder extends ProjectItemViewHolder implements
		View.OnClickListener, View.OnLongClickListener {

		LinearLayout nChunkContainerLL;

		public ChunkViewHolder(DragSortAdapter<?> dragSortAdapter, View itemView) {
			super(dragSortAdapter, itemView);
			nChunkContainerLL = (LinearLayout)itemView.findViewById(R.id.ll_ae_data_chunk);
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
		public ChunkTailViewHolder(DragSortAdapter<?> dragSortAdapter, View itemView) {
			super(dragSortAdapter, itemView);
			// TODO Auto-generated constructor stub
		}
	}

	static class FooterViewHolder extends ProjectItemViewHolder {
		ImageView nAddIv;

		public FooterViewHolder(DragSortAdapter<?> dragSortAdapter, View itemView, int FooterWidth) {
			super(dragSortAdapter, itemView);
			// TODO Auto-generated constructor stub
			nAddIv = (ImageView) itemView.findViewById(R.id.iv_ae_data_add);
			nAddIv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
//					addSection();
				}
			});

			ViewGroup.LayoutParams lp =  itemView.getLayoutParams();
			lp.width = FooterWidth;
			itemView.setLayoutParams(lp);
		}
	}

	@Override
	public void onBindViewHolder(final ProjectItemViewHolder holder, final int position) {
		ProjectItemBean bean = mDataList.get(position);

		if(holder instanceof ChunkViewHolder) {
			ChunkViewHolder viewHolder = (ChunkViewHolder)holder;
			viewHolder.nChunkContainerLL.removeAllViews();
			if(bean instanceof ChunkBean) {
				ChunkBean chunkBean = (ChunkBean)bean;
				Chunk chunk = chunkBean.chunk;
				if(null != chunk) {
					ChunkThumbs chunkThumbList = chunk.getChunkThumbs();
					List<VideoThumb> videoThumbList = chunkThumbList.getThumbs();
					if(null != videoThumbList && videoThumbList.size() > 0) {
						int count = videoThumbList.size();
						for(int i = 0; i < count; i++) {
							ImageView imageView = new ImageView(mContext);
							LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
									DeviceUtil.dp2px(mContext, 45),
									LayoutParams.MATCH_PARENT);
							imageView.setLayoutParams(params);
							imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
							imageView.setImageBitmap(videoThumbList.get(i).getBitmap());
							viewHolder.nChunkContainerLL.addView(imageView);
						}
					}
				}
				viewHolder.nChunkContainerLL.setVisibility(getDraggingId() == chunkBean.index_tag ? View.INVISIBLE
						: View.VISIBLE);
				viewHolder.nChunkContainerLL.postInvalidate();
			}
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

	@Override
	public boolean move(int fromPosition, int toPosition) {
		ProjectItemBean bean = mDataList.get(toPosition);
		if(bean instanceof ChunkBean) {
			mDataList.add(toPosition, mDataList.remove(fromPosition));
			return true;
		} else {
			return false;
		}
	}

	static class ProjectItemViewHolder extends DragSortAdapter.ViewHolder implements
			View.OnClickListener, View.OnLongClickListener {
//		ViewGroup container;
//		ImageView img;
//		TextView text;

		public ProjectItemViewHolder(DragSortAdapter<?> adapter, View itemView) {
			super(adapter, itemView);
//			container = (ViewGroup) itemView.findViewById(R.id.container);
//			img = (ImageView) itemView.findViewById(R.id.img);
//			text = (TextView) itemView.findViewById(R.id.text);
		}

		@Override
		public void onClick(View v) {
//			Log.d(TAG, text.getText() + " clicked!");
		}

		@Override
		public boolean onLongClick(View v) {
			startDrag();
			return true;
		}

		@Override
		public View.DragShadowBuilder getShadowBuilder(View itemView,
				Point touchPoint) {
			return new NoForegroundShadowBuilder(itemView, touchPoint);
		}
	}

}
