package com.makeramen.dragsortadapter.example;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
//import butterknife.ButterKnife;
//import butterknife.InjectView;



import com.goluk.videoedit.R;
import com.makeramen.dragsortadapter.DragSortAdapter;
import com.makeramen.dragsortadapter.NoForegroundShadowBuilder;
import com.makeramen.dragsortadapter.example.util.EnglishNumberToWords;

import java.util.List;

public class ExampleAdapter extends
		DragSortAdapter<ExampleAdapter.MainViewHolder> {

	public static final String TAG = ExampleAdapter.class.getSimpleName();

	private List<Bitmap> data;

	public void setData(List<Bitmap> src) {
		data = src;
	}

	public ExampleAdapter(RecyclerView recyclerView, List<Bitmap> data) {
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
//		int itemId = data.get(position);
		Bitmap bitmap = data.get(position);
//		holder.text.setText(EnglishNumberToWords.convert(itemId));
		holder.img.setImageBitmap(bitmap);
		// NOTE: check for getDraggingId() match to set an "invisible space"
		// while dragging
//		holder.container
//				.setVisibility(getDraggingId() == itemId ? View.INVISIBLE
//						: View.VISIBLE);
		holder.container.postInvalidate();
	}

	@Override
	public long getItemId(int position) {
		return position;//data.get(position);
	}

	@Override
	public int getItemCount() {
		return data.size();
	}

	@Override
	public int getPositionForId(long id) {
		return data.indexOf((int) id);
	}

	@Override
	public boolean move(int fromPosition, int toPosition) {
		data.add(toPosition, data.remove(fromPosition));
		return true;
	}

	static class MainViewHolder extends DragSortAdapter.ViewHolder implements
			View.OnClickListener, View.OnLongClickListener {

		// @InjectView(R.id.container)
		ViewGroup container;
		// @InjectView(R.id.text)
		ImageView img;

		public MainViewHolder(DragSortAdapter adapter, View itemView) {
			super(adapter, itemView);
			container = (ViewGroup) itemView.findViewById(R.id.container);
			img = (ImageView) itemView.findViewById(R.id.img);
			// ButterKnife.inject(this, itemView);
		}

		@Override
		public void onClick(@NonNull View v) {
//			Log.d(TAG, text.getText() + " clicked!");
		}

		@Override
		public boolean onLongClick(@NonNull View v) {
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
