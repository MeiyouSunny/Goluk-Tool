package cn.com.mobnote.golukmobile.profit;

import cn.com.mobnote.golukmobile.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class CustomTextView extends TextView {

	private Paint textPaint;
	private float preferredTextSize;
	private float minTextSize;

	public CustomTextView(Context context) {
		this(context, null);
	}

	public CustomTextView(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.autoScaleTextViewStyle);
	}

	public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		this.textPaint = new Paint();

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutoScaleTextView, defStyle, 0);
		this.minTextSize = a.getDimension(R.styleable.AutoScaleTextView_minTextSize, 10f);
		a.recycle();

		this.preferredTextSize = this.getTextSize();
	}

	/**
	 * 设置TextView的最小字体大小
	 * @param minTextSize
	 * 
	 */
	public void setMinTextSize(float minTextSize) {
		this.minTextSize = minTextSize;
	}

	/**
	 * 适配字体大小和文本宽度
	 * @param text　文字不能是null和“”
	 * @param textWidth  TextView的宽度必须大于０
	 * 
	 */
	private void refitText(String text, int textWidth) {
		if (textWidth <= 0 || text == null || text.length() == 0) {
			return;
		}

		//TextView的可操作宽度区域
		int targetWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();

		final float threshold = 0.5f;

		this.textPaint.set(this.getPaint());

		while ((this.preferredTextSize - this.minTextSize) > threshold) {
			float size = (this.preferredTextSize + this.minTextSize) / 2;
			this.textPaint.setTextSize(size);
			if (this.textPaint.measureText(text) >= targetWidth) {
				//最大字号
				this.preferredTextSize = size;
			} else {
				//最小字号
				this.minTextSize = size;
			}
		}
		/**
		 * getTextSize返回值是以像素(px)为单位的，而setTextSize()是以sp为单位的，
		 * 因此setTextSize(TypedValue.COMPLEX_UNIT_PX, size); 
		 */
		//使用最小的字号，使得文字在TextView里面（如果设置最大字号，文字可能在TextView以外）
		this.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.minTextSize);
	}

	@Override
	protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
		this.refitText(text.toString(), this.getWidth());
	}

	@Override
	protected void onSizeChanged(int width, int height, int oldwidth, int oldheight) {
		if (width != oldwidth) {
			this.refitText(this.getText().toString(), width);
		}
	}
	
}
