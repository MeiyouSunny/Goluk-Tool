package com.goluk.videoedit.view;

import cn.npnt.ae.model.Chunk;

import com.goluk.videoedit.bean.AEDataBean;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ChunkView extends LinearLayout {

	private Chunk mChunk;

    public ChunkView(Context context) {
        super(context);
    }

    public ChunkView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setData(Chunk chunk){
        this.mChunk = chunk;
    }
}
