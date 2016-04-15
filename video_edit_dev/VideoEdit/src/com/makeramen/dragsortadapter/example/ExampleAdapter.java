package com.makeramen.dragsortadapter.example;

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
import com.makeramen.dragsortadapter.DragSortAdapter;
import com.makeramen.dragsortadapter.NoForegroundShadowBuilder;

import java.util.List;

public class ExampleAdapter extends
		DragSortAdapter<ExampleAdapter.MainViewHolder> {

	public static final String TAG = ExampleAdapter.class.getSimpleName();

	private List<BitmapWrapper> data;

	public void setData(List<BitmapWrapper> src) {
		data = src;
	}

	public ExampleAdapter(RecyclerView recyclerView, List<BitmapWrapper> data) {
		super(recyclerView);
		this.data = data;
	}

	@Override
	public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(R.layout.item_rv_chunk_layout, parent, false);
		MainViewHolder holder = new MainViewHolder(this, view);
		view.setOnClickListener(holder);
		view.setOnLongClickListener(holder);
		return holder;
	}

	@Override
	public void onBindViewHolder(final MainViewHolder holder, final int position) {
		BitmapWrapper wrapper = data.get(position);
		Bitmap bitmap = data.get(position).bitmap;
		holder.img.setImageBitmap(bitmap);
		holder.text.setText(wrapper.index + "");
		// NOTE: check for getDraggingId() match to set an "invisible space"
		// while dragging
		holder.container
				.setVisibility(getDraggingId() == wrapper.index ? View.INVISIBLE
						: View.VISIBLE);
		holder.container.postInvalidate();
	}

	@Override
	public long getItemId(int position) {
		return data.get(position).index;
	}

	@Override
	public int getItemCount() {
		return data.size();
	}

	@Override
	public int getPositionForId(long id) {
		for (int i = 0; i < data.size(); i++) {
			BitmapWrapper wrapper = data.get(i);
			if (wrapper.index == (int) id) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean move(int fromPosition, int toPosition) {
		data.add(toPosition, data.remove(fromPosition));
		return true;
	}

	static class MainViewHolder extends DragSortAdapter.ViewHolder implements
			View.OnClickListener, View.OnLongClickListener {
		ViewGroup container;
		ImageView img;
		TextView text;

		public MainViewHolder(DragSortAdapter adapter, View itemView) {
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
