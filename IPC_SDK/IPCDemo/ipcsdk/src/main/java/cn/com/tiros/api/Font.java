package cn.com.tiros.api;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;

public class Font {

	public static final int SYS_FS_MAP_1 = 100;
	public static final int SYS_FS_MAP_2 = 101;
	public static final int SYS_FS_MAP_3 = 102;
	public static final int SYS_FS_MAP_4 = 103;
	public static final int SYS_FS_MAP_5 = 104;
	public static final int SYS_FS_MAP_6 = 105;
	public static final int SYS_FS_MAP_7 = 106;
	public static final int SYS_FS_MAP_8 = 107;
	public static final int SYS_FS_MAP_9 = 108;
	public static final int SYS_FS_MAP_10 = 109;
	public static final int SYS_FS_MAP_11 = 110;

	public static final int SYS_FS_NIGHT_1 = 200;
	public static final int SYS_FS_NIGHT_2 = 201;
	public static final int SYS_FS_NIGHT_3 = 202;
	public static final int SYS_FS_NIGHT_4 = 203;
	public static final int SYS_FS_NIGHT_5 = 204;
	public static final int SYS_FS_NIGHT_6 = 205;
	public static final int SYS_FS_NIGHT_7 = 206;
	public static final int SYS_FS_NIGHT_8 = 207;
	public static final int SYS_FS_NIGHT_9 = 208;
	public static final int SYS_FS_NIGHT_10 = 209;
	public static final int SYS_FS_NIGHT_11 = 210;

	/** 获取屏幕密度 */
	public static float DISPLAY_HEIGHT = 1.5f;

	private Paint mPaint = null;
	private int mPnFits;
	private Image mImg = null;

	private int mStyle;

	public void sys_ftcreate() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setTextAlign(Align.LEFT);
		sys_ftsetstyle(SYS_FS_MAP_6);
	}

	public void sys_ftdestroy() {
		mPaint = null;
	}

	/**
	 * 绘制POI点图标及字体 共绘制五次
	 * 
	 * @author 钱伟
	 * @date 2012/09/19
	 */
	public void sys_ftdrawtext(Image img, int x, int y, String pwszText, int r,
			int g, int b) {

		// long starttime = System.currentTimeMillis();

		int color = Color.rgb(r, g, b);
		img.mCanvas.setBitmap(img.mBitmap);

		if (color != Color.WHITE && mStyle < SYS_FS_NIGHT_1) {
			mPaint.setColor(Color.WHITE);
			img.mCanvas.drawText(pwszText, x + 1, y
					- mPaint.getFontMetrics().ascent + 1, mPaint);
			img.mCanvas.drawText(pwszText, x - 1, y
					- mPaint.getFontMetrics().ascent - 1, mPaint);
			img.mCanvas.drawText(pwszText, x + 1, y
					- mPaint.getFontMetrics().ascent - 1, mPaint);
			img.mCanvas.drawText(pwszText, x - 1, y
					- mPaint.getFontMetrics().ascent + 1, mPaint);
		}
		mPaint.setColor(color);
		img.mCanvas.drawText(pwszText, x, y - mPaint.getFontMetrics().ascent,
				mPaint);

	}

	public int sys_ftmeasuretext(String pwszText, int nChars, int nMaxWidth) {
		if (pwszText == null) {
			return 0;
		}
		float textWidth = mPaint.measureText(pwszText, 0, nChars);
		if (nMaxWidth != 0 && textWidth > nMaxWidth) {
			textWidth = nMaxWidth;
			float len = 0;
			for (int i = 1; i <= pwszText.length(); i++) {
				len = mPaint.measureText(pwszText, 0, i);
				if (len > nMaxWidth) {
					mPnFits = i - 1;
					break;
				}
			}
		} else {
			mPnFits = pwszText.length();
		}
		return (int) textWidth;
	}

	public int sys_ftgetPnFits() {
		return mPnFits;
	}

	/**
	 * 为描边扩展位置
	 * 
	 * @author 钱伟
	 * @date 2012/09/19
	 */
	public int sys_ftgetfontsize() {
		return (int) mPaint.getTextSize() + 2;
	}

	/**
	 * 有屏幕密度控制 设置字体高度
	 * 
	 * @author 钱伟
	 * @date 2012/09/19
	 */
	public int sys_ftgetheight() {
		int textSize = 0;
		if (DISPLAY_HEIGHT == 2.0) {
			textSize = (int) mPaint.getTextSize() + 4;
		} else {
			textSize = (int) mPaint.getTextSize() + 3;
		}
		return textSize;
	}

	// float[] map1FontSize = { 36f, 30f, 34f, 44f };
	// float[] map1FontSize = { 36f, 30f, 34f, 44f };
	// float[] map1FontSize = { 36f, 30f, 34f, 44f };
	// float[] map1FontSize = { 36f, 30f, 34f, 44f };
	// float[] map1FontSize = { 36f, 30f, 34f, 44f };
	// float[] map1FontSize = { 36f, 30f, 34f, 44f };
	// float[] map1FontSize = { 36f, 30f, 34f, 44f };
	// float[] map1FontSize = { 36f, 30f, 34f, 44f };
	// float[] map1FontSize = { 36f, 30f, 34f, 44f };

	/**
	 * 根据手机屏幕密度 设置字体风格及字体大小
	 * 
	 * @author 钱伟
	 * @date 2012/09/19
	 */
	public void sys_ftsetstyle(int style) {
		mStyle = style;
		mPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
		if (SYS_FS_MAP_1 == style || SYS_FS_NIGHT_1 == style) {
			if (DISPLAY_HEIGHT == 2.0) {
				mPaint.setTextSize(38f);
			} else if (DISPLAY_HEIGHT == 1.5) {
				mPaint.setTextSize(34f);
			} else if (DISPLAY_HEIGHT == 1.0) {
				mPaint.setTextSize(40f);
			} else if (DISPLAY_HEIGHT == 3.0) {
				mPaint.setTextSize(48f);
			}
		} else if (style == SYS_FS_MAP_2 || SYS_FS_NIGHT_2 == style) {
			if (DISPLAY_HEIGHT == 2.0) {
				mPaint.setTextSize(36f);
			} else if (DISPLAY_HEIGHT == 1.5) {
				mPaint.setTextSize(32f);
			} else if (DISPLAY_HEIGHT == 1.0) {
				mPaint.setTextSize(34f);
			} else if (DISPLAY_HEIGHT == 3.0) {
				mPaint.setTextSize(46f);
			}
		} else if (style == SYS_FS_MAP_3 || SYS_FS_NIGHT_3 == style) {
			if (DISPLAY_HEIGHT == 2.0) {
				mPaint.setTextSize(34f);
			} else if (DISPLAY_HEIGHT == 1.5) {
				mPaint.setTextSize(30f);
			} else if (DISPLAY_HEIGHT == 1.0) {
				mPaint.setTextSize(32f);
			} else if (DISPLAY_HEIGHT == 3.0) {
				mPaint.setTextSize(44f);
			}
		} else if (style == SYS_FS_MAP_4 || SYS_FS_NIGHT_4 == style) {
			if (DISPLAY_HEIGHT == 2.0) {
				mPaint.setTextSize(32f);
			} else if (DISPLAY_HEIGHT == 1.5) {
				mPaint.setTextSize(26f);
			} else if (DISPLAY_HEIGHT == 1.0) {
				mPaint.setTextSize(30f);
			} else if (DISPLAY_HEIGHT == 3.0) {
				mPaint.setTextSize(44f);
			}
		} else if (style == SYS_FS_MAP_5 || SYS_FS_NIGHT_5 == style) {
			if (DISPLAY_HEIGHT == 2.0) {
				mPaint.setTextSize(30f);
			} else if (DISPLAY_HEIGHT == 1.5) {
				mPaint.setTextSize(26f);
			} else if (DISPLAY_HEIGHT == 1.0) {
				mPaint.setTextSize(30f);
			} else if (DISPLAY_HEIGHT == 3.0) {
				mPaint.setTextSize(42f);
			}
		} else if (style == SYS_FS_MAP_6 || SYS_FS_NIGHT_6 == style) {
			if (DISPLAY_HEIGHT == 2.0) {
				mPaint.setTextSize(30f);
			} else if (DISPLAY_HEIGHT == 1.5) {
				mPaint.setTextSize(24f);
			} else if (DISPLAY_HEIGHT == 1.0) {
				mPaint.setTextSize(30f);
			} else if (DISPLAY_HEIGHT == 3.0) {
				mPaint.setTextSize(42f);
			}
		} else if (style == SYS_FS_MAP_7 || SYS_FS_NIGHT_7 == style) {
			if (DISPLAY_HEIGHT == 2.0) {
				mPaint.setTextSize(30f);
			} else if (DISPLAY_HEIGHT == 1.5) {
				mPaint.setTextSize(22f);
			} else if (DISPLAY_HEIGHT == 1.0) {
				mPaint.setTextSize(28f);
			} else if (DISPLAY_HEIGHT == 3.0) {
				mPaint.setTextSize(40f);
			}
		} else if (style == SYS_FS_MAP_8 || SYS_FS_NIGHT_8 == style) {
			if (DISPLAY_HEIGHT == 2.0) {
				mPaint.setTextSize(28f);
			} else if (DISPLAY_HEIGHT == 1.5) {
				mPaint.setTextSize(22f);
			} else if (DISPLAY_HEIGHT == 1.0) {
				mPaint.setTextSize(28f);
			} else if (DISPLAY_HEIGHT == 3.0) {
				mPaint.setTextSize(40f);
			}
		} else if (style == SYS_FS_MAP_9 || SYS_FS_NIGHT_9 == style) {
			if (DISPLAY_HEIGHT == 2.0) {
				mPaint.setTextSize(24f);
			} else if (DISPLAY_HEIGHT == 1.5) {
				mPaint.setTextSize(20f);
			} else if (DISPLAY_HEIGHT == 1.0) {
				mPaint.setTextSize(26f);
			} else if (DISPLAY_HEIGHT == 3.0) {
				mPaint.setTextSize(38f);
			}
		} else if (style == SYS_FS_MAP_10 || SYS_FS_NIGHT_10 == style) {
			if (DISPLAY_HEIGHT == 2.0) {
				mPaint.setTextSize(22f);
			} else if (DISPLAY_HEIGHT == 1.5) {
				mPaint.setTextSize(18f);
			} else if (DISPLAY_HEIGHT == 1.0) {
				mPaint.setTextSize(24f);
			} else if (DISPLAY_HEIGHT == 3.0) {
				mPaint.setTextSize(36f);
			}
		} else if (SYS_FS_MAP_11 == style || SYS_FS_NIGHT_11 == style) {
			setMap11Size();
		}
	}

	// 排列顺序为 {1.0密度,1.5密度,2.0密度,3.0密度}
	float[] map11FontSize = { 44f, 50f, 44f, 52f };

	private void setMap11Size() {
		if (DISPLAY_HEIGHT == 1.0) {
			mPaint.setTextSize(map11FontSize[0]);
		} else if (DISPLAY_HEIGHT == 1.5) {
			mPaint.setTextSize(map11FontSize[1]);
		} else if (DISPLAY_HEIGHT == 2.0) {
			mPaint.setTextSize(map11FontSize[2]);
		} else if (DISPLAY_HEIGHT == 3.0) {
			mPaint.setTextSize(map11FontSize[3]);
		}
	}

	public void sys_ftdrawbegin(Image pimg) {
		mImg = pimg;
	}

	public void sys_ftdrawtext2(int x, int y, String pwszText, int r, int g,
			int b) {

		sys_ftdrawtext(mImg, x, y, pwszText, r, g, b);

	}

	public void sys_ftdrawend() {
	}

}
