package com.rd.veuisdk.ae.model;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * json 文件中，ReplaceableText 相关信息
 */
public class AETextLayerInfo implements Parcelable {
    /**
     * 对齐方式
     */
    public enum Alignment {
        right, left
    }


    private String name;
    private int width, height;
    private String textFont;
    private String fontSrc;
    private String textContent;
    private int maxNum, lineNum;
    private String alignment;
    private int textColor;
    private float alpha=1;
    private boolean vertical;
    private Rect padding = new Rect();
    private boolean bold; //加粗
    private boolean italic; //倾斜
    private int strokeColor= Color.TRANSPARENT;;
    private int strokeWidth;
    private float stroleAlpha=1;
    private int shadowColor= Color.TRANSPARENT;;
    private float shadowRadius = 0, shadowDx, shadowDy;
    private int fontSize = 0;
    String ttfPath;

    /**
     * 设置对齐方式 left 左对齐 right 右对齐
     *
     * @param alignment
     */
    public void setAlignment(Alignment alignment) {
        this.alignment = alignment.name();
    }

    /*
     * 设置字体颜色
     */
    public void setTextColor(int color) {
        textColor = color;
    }

    /**
     * 设置透明度（0-1，0是完全透明）
     *
     * @param alpha
     */
    public void setAlpha(float alpha) {
        this.alpha = Math.min(1, Math.max(0, alpha));
    }

    /**
     * 是否纵向显示
     *
     * @param isVertiacl
     */
    public void setVertical(boolean isVertiacl) {
        vertical = isVertiacl;
    }

    /**
     * 设置文字padding
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setPadding(int left, int top, int right, int bottom) {
        padding = new Rect(left, top, right, bottom);
    }

    /**
     * 设置文字加粗
     *
     * @param bold
     */
    public void setBold(boolean bold) {
        this.bold = bold;
    }


    public int getStrokeColor() {
        return strokeColor;
    }

    /**
     * 设置描边颜色
     *
     * @param color
     */
    public void setStrokeColor(int color) {
        strokeColor = color;
    }

    /**
     * 设置描边宽度
     *
     * @param width
     */
    public void setStrokeWidth(int width) {
        strokeWidth = width;
    }

    /**
     * 设置阴影颜色
     *
     * @param color
     */
    public void setShadowColor(int color) {
        shadowColor = color;
    }

    /**
     *  设置文字内容
     * @param textContent
     */
    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    /**
     *  设置斜体
     * @param italic
     */
    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    /**
     *  设置描边透明度
     * @param stroleAlpha
     */
    public void setStroleAlpha(float stroleAlpha) {
        this.stroleAlpha = stroleAlpha;
    }

    /**
     * 设置阴影层
     *
     * @param radius 模糊半径(0为没有阴影)
     * @param dx     横向偏移量
     * @param dy     纵向偏移量
     */
    public void setShadowLayer(float radius, float dx, float dy) {
        shadowRadius = radius;
        shadowDx = dx;
        shadowDy = dy;
    }

    /**
     * 设置字体大小(0为自适应)
     *
     * @param fontSize
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getTextContent() {
        return textContent;
    }

    public String getTextFont() {
        return textFont;
    }

    public String getFontSrc() {
        return fontSrc;
    }

    public int getMaxNum() {
        return maxNum;
    }

    public int getLineNum() {
        return lineNum;
    }

    public String getAlignment() {
        return alignment;
    }


    public int getTextColor() {
        return textColor;
    }


    public float getAlpha() {
        return alpha;
    }

    public boolean isVertical() {
        return vertical;
    }


    public Rect getPadding() {
        return padding;
    }


    public boolean isBold() {
        return bold;
    }

    /**
     * 是否倾斜
     *
     * @return
     */
    public boolean isItalic() {
        return italic;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public int getShadowColor() {
        return shadowColor;
    }

    public float getStroleAlpha() {
        return stroleAlpha;
    }

    public float getShadowRadius() {
        return shadowRadius;
    }

    public float getShadowDx() {
        return shadowDx;
    }

    public float getShadowDy() {
        return shadowDy;
    }

    /**
     * 是否根据文字数调整字体大小
     * >0 时，有效 ； 其他  自适应
     *
     * @return
     */
    public int getFontSize() {
        return fontSize;
    }

    public String getTtfPath() {
        return ttfPath;
    }

    public void setTtfPath(String ttfPath) {
        this.ttfPath = ttfPath;
    }

    /**
     * @param tmp config.json中  "textimg"->"text" ->jsonObject
     * @throws JSONException
     */
    public AETextLayerInfo(JSONObject tmp) throws JSONException {
        this(tmp, false);
    }

    /**
     * @param tmp
     * @param ignoreText 是否忽略TextContent
     * @throws JSONException
     */
    public AETextLayerInfo(JSONObject tmp, boolean ignoreText) throws JSONException {
        name = tmp.optString("name");
        width = tmp.optInt("width");
        height = tmp.optInt("height");
        if (ignoreText) {
            textContent = "世界那么大\n因为有你而与众不同";
        } else {
            if (tmp.has("textContent")) {
                textContent = tmp.optString("textContent");
            } else {
                textContent = tmp.optString("suggestion");
            }
        }
        textFont = tmp.optString("textFont");
        fontSrc = tmp.optString("fontSrc");
        maxNum = tmp.optInt("maxNum", 200);
        lineNum = tmp.getInt("lineNum");
        alignment = tmp.optString("alignment");
        JSONArray colorArr = tmp.optJSONArray("textColor");
        if (null != colorArr && colorArr.length() == 3) {
            textColor = Color.rgb(colorArr.getInt(0), colorArr.getInt(1), colorArr.getInt(2));
        }
        alpha = (float) tmp.optDouble("alpha", 1.0);
        vertical = tmp.optInt("vertical") == 1;


        JSONArray textPadding = tmp.optJSONArray("textPadding");
        if (null != textPadding && textPadding.length() == 4) {
            padding.set(textPadding.getInt(0), textPadding.getInt(1), textPadding.getInt(2), textPadding.getInt(3));
        }

        bold = tmp.optInt("bold") == 1;
        italic = tmp.optInt("italic") == 1;


        JSONArray strokeColorArr = tmp.optJSONArray("strokeColor");
        if (null != strokeColorArr && strokeColorArr.length() == 3) {
            strokeColor = Color.rgb(strokeColorArr.getInt(0), strokeColorArr.getInt(1), strokeColorArr.getInt(2));
        }

        strokeWidth = tmp.optInt("strokeWidth");
        JSONArray shadowColorArr = tmp.optJSONArray("shadowColor");
        if (null != shadowColorArr && shadowColorArr.length() == 3) {
            shadowColor = Color.rgb(shadowColorArr.getInt(0), shadowColorArr.getInt(1), shadowColorArr.getInt(2));
        }
        if (ignoreText) {
            fontSize = 0;
        } else {
            fontSize = tmp.optInt("fontSize", 0);
        }
    }


    public AETextLayerInfo(int width, int height, Rect padding, String text, int textColor, AETextLayerInfo.Alignment alignment) {
        this.width = width;
        this.height = height;
        this.padding = padding;
        this.textContent = text;
        this.textColor = textColor;
        this.alignment = alignment.name();
    }

    @Override
    public String toString() {
        return "AETextLayerInfo{" +
                "name='" + name + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", textFont='" + textFont + '\'' +
                ", fontSrc='" + fontSrc + '\'' +
                ", textContent='" + textContent + '\'' +
                ", maxNum=" + maxNum +
                ", lineNum=" + lineNum +
                ", alignment='" + alignment + '\'' +
                ", textColor=" + textColor +
                ", alpha=" + alpha +
                ", vertical=" + vertical +
                ", padding=" + padding +
                ", bold=" + bold +
                ", italic=" + italic +
                ", strokeColor=" + strokeColor +
                ", strokeWidth=" + strokeWidth +
                ", stroleAlpha=" + stroleAlpha +
                ", shadowColor=" + shadowColor +
                ", shadowRadius=" + shadowRadius +
                ", shadowDx=" + shadowDx +
                ", shadowDy=" + shadowDy +
                ", fontSize=" + fontSize +
                ", ttfPath='" + ttfPath + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeString(this.textFont);
        dest.writeString(this.fontSrc);
        dest.writeString(this.textContent);
        dest.writeInt(this.maxNum);
        dest.writeInt(this.lineNum);
        dest.writeString(this.alignment);
        dest.writeInt(this.textColor);
        dest.writeFloat(this.alpha);
        dest.writeByte(this.vertical ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.padding, flags);
        dest.writeByte(this.bold ? (byte) 1 : (byte) 0);
        dest.writeByte(this.italic ? (byte) 1 : (byte) 0);
        dest.writeInt(this.strokeColor);
        dest.writeInt(this.strokeWidth);
        dest.writeFloat(this.stroleAlpha);
        dest.writeInt(this.shadowColor);
        dest.writeFloat(this.shadowRadius);
        dest.writeFloat(this.shadowDx);
        dest.writeFloat(this.shadowDy);
        dest.writeInt(this.fontSize);
        dest.writeString(this.ttfPath);
    }

    protected AETextLayerInfo(Parcel in) {
        this.name = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
        this.textFont = in.readString();
        this.fontSrc = in.readString();
        this.textContent = in.readString();
        this.maxNum = in.readInt();
        this.lineNum = in.readInt();
        this.alignment = in.readString();
        this.textColor = in.readInt();
        this.alpha = in.readFloat();
        this.vertical = in.readByte() != 0;
        this.padding = in.readParcelable(Rect.class.getClassLoader());
        this.bold = in.readByte() != 0;
        this.italic = in.readByte() != 0;
        this.strokeColor = in.readInt();
        this.strokeWidth = in.readInt();
        this.stroleAlpha = in.readFloat();
        this.shadowColor = in.readInt();
        this.shadowRadius = in.readFloat();
        this.shadowDx = in.readFloat();
        this.shadowDy = in.readFloat();
        this.fontSize = in.readInt();
        this.ttfPath = in.readString();
    }

    public static final Creator<AETextLayerInfo> CREATOR = new Creator<AETextLayerInfo>() {
        @Override
        public AETextLayerInfo createFromParcel(Parcel source) {
            return new AETextLayerInfo(source);
        }

        @Override
        public AETextLayerInfo[] newArray(int size) {
            return new AETextLayerInfo[size];
        }
    };
}
