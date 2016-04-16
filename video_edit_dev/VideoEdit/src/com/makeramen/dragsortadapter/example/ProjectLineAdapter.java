package com.makeramen.dragsortadapter.example;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.goluk.videoedit.BitmapWrapper;
import com.goluk.videoedit.R;
import com.goluk.videoedit.bean.AEDataBean;
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

	/** 空白头 */
	private final int VIEW_TYPE_HEADER = 0;
	/** 段落 */
	private final int VIEW_TYPE_CHUNK = 1;
	/** 转场 */
	private final int VIEW_TYPE_TRANSITION = 2;
	/** 片尾 */
	private final int VIEW_TYPE_CHUNK_TAIL = 3;
	/** 尾部 */
	private final int VIEW_TYEE_FOOTER = 4;

	/** 列表 */
	private List<ProjectItemBean> mDataList;

	private int mItemCount;

	private Context mContext;

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

		if(VIEW_TYPE_HEADER == viewType){
			View view = inflater.inflate(R.layout.ae_data_header, viewGroup, false);
			return new HeaderViewHolder(this,view);
		}else if(VIEW_TYEE_FOOTER == viewType){
			View view = inflater.inflate(R.layout.ae_data_footer, viewGroup, false);
			return new FooterViewHolder(this,view);
		}else if(VIEW_TYPE_CHUNK_TAIL == viewType){
			View view = inflater.inflate(R.layout.ae_data_section_tail, viewGroup, false);
			return new ChunkTailViewHolder(this,view);
		}else if(VIEW_TYPE_TRANSITION == viewType){
			View view = inflater.inflate(R.layout.ae_data_transfer, viewGroup, false);
			return new TransitionViewHolder(this,view);
		}else if(VIEW_TYPE_CHUNK == viewType){
			View view = inflater.inflate(R.layout.ae_data_section, viewGroup, false);
			ChunkViewHolder vHolder = new ChunkViewHolder(this,view);
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

	/**
	 * 段落viewholder
	 * @author uestc
	 *
	 */
	static class ChunkViewHolder extends ProjectItemViewHolder implements
		View.OnClickListener, View.OnLongClickListener{

		public ChunkViewHolder(DragSortAdapter<?> dragSortAdapter,
				View itemView) {
			super(dragSortAdapter, itemView);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean onLongClick(View v) {
			// TODO Auto-generated method stub
			startDrag();
			return true;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}

	}

	/**
	 * 转场viewholder
	 * @author uestc
	 *
	 */
	static class TransitionViewHolder extends ProjectItemViewHolder {
		public TransitionViewHolder(DragSortAdapter<?> dragSortAdapter,
				View itemView) {
			super(dragSortAdapter, itemView);
			// TODO Auto-generated constructor stub
		}

	}

	/**
	 * 片头viewholder
	 * @author uestc
	 *
	 */
	static class HeaderViewHolder extends ProjectItemViewHolder {
		public HeaderViewHolder(DragSortAdapter<?> dragSortAdapter, View itemView) {
			super(dragSortAdapter, itemView);
			// TODO Auto-generated constructor stub
		}
	}

	/**
	 * 片尾viewholder
	 * @author uestc
	 *
	 */
	static class ChunkTailViewHolder extends ProjectItemViewHolder {
		public ChunkTailViewHolder(DragSortAdapter<?> dragSortAdapter, View itemView) {
			super(dragSortAdapter, itemView);
			// TODO Auto-generated constructor stub
		}
	}

	/**
	 * 尾部viewholder
	 * @author uestc
	 *
	 */
	static class FooterViewHolder extends ProjectItemViewHolder {
		ImageView mAddIv;
		public FooterViewHolder(DragSortAdapter<?> dragSortAdapter, View itemView) {
			super(dragSortAdapter, itemView);
			// TODO Auto-generated constructor stub
			mAddIv = (ImageView) itemView.findViewById(R.id.iv_ae_data_add);
			mAddIv.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
//					addSection();
				}
			});

			ViewGroup.LayoutParams lp =  itemView.getLayoutParams();
			lp.width = 100;//DeviceUtil.getScreenWidthSize(mContext) - DeviceUtil.dp2px(mContext, 65);
			itemView.setLayoutParams(lp);
		}

	}

	@Override
	public void onBindViewHolder(final ProjectItemViewHolder holder, final int position) {
		ProjectItemBean bean = mDataList.get(position);
//		Bitmap bitmap = data.get(position).bitmap;
//		holder.img.setImageBitmap(bitmap);
//		holder.text.setText(wrapper.index + "");
//		// NOTE: check for getDraggingId() match to set an "invisible space"
//		// while dragging
//		holder.container
//				.setVisibility(getDraggingId() == wrapper.index ? View.INVISIBLE
//						: View.VISIBLE);
//		holder.container.postInvalidate();
	}

	@Override
	public long getItemId(int position) {
		return mDataList.get(position).index_tag;
	}

	@Override
	public int getItemCount() {
		return mDataList.size();
	}

//	@Override
//	public int getItemCount() {
//		// TODO Auto-generated method stub
//		// 列表长度 = 空头(1) + 段落长度 + 转场长度 + 片尾长度(0/1) + 添加按钮长度(1)
//		int dataCount = 0;
//		if(mDataList != null){
//			dataCount = mDataList.size() * 2;
//		}
//		mItemCount = dataCount + 3;
//
//		return mItemCount;
//	}

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
		mDataList.add(toPosition, mDataList.remove(fromPosition));
		return true;
	}

	static class ProjectItemViewHolder extends DragSortAdapter.ViewHolder implements
			View.OnClickListener, View.OnLongClickListener {
		ViewGroup container;
		ImageView img;
		TextView text;

		public ProjectItemViewHolder(DragSortAdapter adapter, View itemView) {
			super(adapter, itemView);
			container = (ViewGroup) itemView.findViewById(R.id.container);
			img = (ImageView) itemView.findViewById(R.id.img);
			text = (TextView) itemView.findViewById(R.id.text);
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
