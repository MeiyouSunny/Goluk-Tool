package com.rd.veuisdk.model;

import com.rd.gallery.IImage;

/**
 * Image项
 * 
 * @author abreal
 * 
 */
public class ImageItem {

    /**
     * constructror
     * 
     * @param img
     */
    public ImageItem(IImage img) {
	this.image = img;
	imageItemKey = img.getDataPath().hashCode();
    }

    public IImage image;
    public boolean selected=false;
    public int imageItemKey;
    public int position;

}
