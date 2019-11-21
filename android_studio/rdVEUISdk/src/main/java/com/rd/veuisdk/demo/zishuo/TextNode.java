package com.rd.veuisdk.demo.zishuo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 文字节点 开始、结束、持续时间、文字、字体、描边、阴影、颜色
 *      默认动画持续时间0.3f, 字体颜色白色
 */
public class TextNode  implements Parcelable {
    private float begin;//开始时间
    private float end;//结束时间
    private float continued = 0.3f;//默认持续时间
    private String text;//文字
    private String font = null;//字体
    private float strokeWidth = 0;//描边宽度
    private String strokeColor = "#ffffff";//描边颜色
    private float shadowAlpha = 0;//阴影 0表示透明度100%  0.001f表示透明度0% 越大越模糊 0消失不见
    private String color = "#ffffff";//字体颜色

    public TextNode clone() {
        TextNode textNode = new TextNode();
        textNode.begin = begin;
        textNode.end = end;
        textNode.continued = continued;
        textNode.text = text;
        textNode.font = font;
        textNode.strokeWidth = strokeWidth;
        textNode.strokeColor = strokeColor;
        textNode.shadowAlpha = shadowAlpha;
        textNode.color = color;
        return textNode;
    }

    public float getBegin() {
        return begin;
    }

    public void setBegin(float begin) {
        this.begin = begin;
    }

    public float getEnd() {
        return end;
    }

    public void setEnd(float end) {
        this.end = end;
    }

    public float getContinued() {
        return continued;
    }

    public void setContinued(float continued) {
        this.continued = continued;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public String getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
    }

    public float getShadowAlpha() {
        return shadowAlpha;
    }

    public void setShadowAlpha(float shadowAlpha) {
        this.shadowAlpha = shadowAlpha;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.begin);
        dest.writeFloat(this.end);
        dest.writeFloat(this.continued);
        dest.writeString(this.text);
        dest.writeString(this.font);
        dest.writeFloat(this.strokeWidth);
        dest.writeString(this.strokeColor);
        dest.writeFloat(this.shadowAlpha);
        dest.writeString(this.color);
    }

    public TextNode() {
    }

    public TextNode(float start, float end, String text) {
        this.begin = start;
        this.end = end;
        this.text = text;
    }

    protected TextNode(Parcel in) {
        this.begin = in.readFloat();
        this.end = in.readFloat();
        this.continued = in.readFloat();
        this.text = in.readString();
        this.font = in.readString();
        this.strokeWidth = in.readFloat();
        this.strokeColor = in.readString();
        this.shadowAlpha = in.readFloat();
        this.color = in.readString();
    }

    public static final Creator<TextNode> CREATOR = new Creator<TextNode>() {
        @Override
        public TextNode createFromParcel(Parcel source) {
            return new TextNode(source);
        }

        @Override
        public TextNode[] newArray(int size) {
            return new TextNode[size];
        }
    };
}
