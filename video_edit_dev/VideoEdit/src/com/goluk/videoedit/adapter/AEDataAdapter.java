package com.goluk.videoedit.adapter;

import java.util.ArrayList;
import java.util.List;

import utils.DeviceUtil;

import com.goluk.videoedit.R;
import com.goluk.videoedit.bean.AEDataBean;
import com.makeramen.dragsortadapter.DragSortAdapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class AEDataAdapter extends DragSortAdapter<DragSortAdapter.ViewHolder>{

	/** 空白头 */
	private final int VIEW_TYPE_HEADER = 0;
	/** 段落 */
	private final int VIEW_TYPE_SECTION = 1;
	/** 转场 */
	private final int VIEW_TYPE_TRANSFER = 2;
	/** 片尾 */
	private final int VIEW_TYPE_SECTION_TAIL = 3;
	/** 尾部 */
	private final int VIEW_TYEE_FOOTER = 4;

	/** 列表 */
	private List<AEDataBean> mDataList;

	private int mItemCount;

	private Context mContext;

	public AEDataAdapter(Context cxt,RecyclerView recyclerView,List<AEDataBean> dataList) {
		super(recyclerView);
		this.mDataList = dataList;
		this.mContext = cxt;
	}

	public void addSection(){

		if(mDataList == null){
			mDataList = new ArrayList<AEDataBean>();
		}

		mDataList.add(new AEDataBean(1));
		this.notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position) {
		//return data.get(position);
		return position;
	}

	@Override
	public int getPositionForId(long id) {
		//return data.indexOf((int) id);
		return (int) id;
	}

	@Override
	public boolean move(int fromPosition, int toPosition) {
		mDataList.add(toPosition/2, mDataList.remove(fromPosition/2));
		return true;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub

		if(position == 0){
			return VIEW_TYPE_HEADER;
		}
		if(position == mItemCount - 1){
			return VIEW_TYEE_FOOTER;
		}

		if(isTailItem(position)){
			return VIEW_TYPE_SECTION_TAIL;
		}

		if(mDataList != null && mDataList.size()*2 + 1> position){

			if(position%2 == 1){
				return VIEW_TYPE_SECTION;
			}else{
				return VIEW_TYPE_TRANSFER;
			}
		}
		return -1;
	}

	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		// 列表长度 = 空头(1) + 段落长度 + 转场长度 + 片尾长度(0/1) + 添加按钮长度(1)
		int dataCount = 0;
		if(mDataList != null){
			dataCount = mDataList.size() * 2;
		}
		mItemCount = dataCount + 3;

		return mItemCount;
	}

	@Override
	public void onBindViewHolder(ViewHolder vHolder, int position) {
		// TODO Auto-generated method stub

	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		// TODO Auto-generated method stub

		LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

		if(VIEW_TYPE_HEADER == viewType){
			View view = inflater.inflate(R.layout.ae_data_header, viewGroup, false);
			return new HeaderViewHolder(this,view);
		}else if(VIEW_TYEE_FOOTER == viewType){
			View view = inflater.inflate(R.layout.ae_data_footer, viewGroup, false);
			return new FooterViewHolder(this,view);
		}else if(VIEW_TYPE_SECTION_TAIL == viewType){
			View view = inflater.inflate(R.layout.ae_data_section_tail, viewGroup, false);
			return new SectionTailViewHolder(this,view);
		}else if(VIEW_TYPE_TRANSFER == viewType){
			View view = inflater.inflate(R.layout.ae_data_transfer, viewGroup, false);
			return new TransferViewHolder(this,view);
		}else if(VIEW_TYPE_SECTION == viewType){
			View view = inflater.inflate(R.layout.ae_data_section, viewGroup, false);
			SectionViewHolder vHolder = new SectionViewHolder(this,view);
			view.setOnLongClickListener(vHolder);
			return vHolder;
		}

		return null;
	}

	private boolean isTailItem(int position){

		if(position == this.getItemCount() -2 ){
			return true;
		}	
		return false;
	}

	/**
	 * 段落viewholder
	 * @author uestc
	 *
	 */
	class SectionViewHolder extends DragSortAdapter.ViewHolder implements
	View.OnClickListener, View.OnLongClickListener{

		public SectionViewHolder(DragSortAdapter<?> dragSortAdapter,
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
	class TransferViewHolder extends DragSortAdapter.ViewHolder{

		public TransferViewHolder(DragSortAdapter<?> dragSortAdapter,
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
	class HeaderViewHolder extends DragSortAdapter.ViewHolder{

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
	class SectionTailViewHolder extends DragSortAdapter.ViewHolder{

		public SectionTailViewHolder(DragSortAdapter<?> dragSortAdapter, View itemView) {
			super(dragSortAdapter, itemView);
			// TODO Auto-generated constructor stub
		}
	}
	/**
	 * 尾部viewholder
	 * @author uestc
	 *
	 */
	class FooterViewHolder extends ViewHolder{

		ImageView mAddIv;
		public FooterViewHolder(DragSortAdapter<?> dragSortAdapter, View itemView) {
			super(dragSortAdapter, itemView);
			// TODO Auto-generated constructor stub
			mAddIv = (ImageView) itemView.findViewById(R.id.iv_ae_data_add);
			mAddIv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					addSection();
				}
			});
			
			ViewGroup.LayoutParams lp =  itemView.getLayoutParams();
			lp.width = DeviceUtil.getScreenWidthSize(mContext) - DeviceUtil.dp2px(mContext, 65);
			itemView.setLayoutParams(lp);
		}

	}

}
