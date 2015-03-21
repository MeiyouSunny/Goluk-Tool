/*
  @AppName: FeiMaoBiJia
  @Version: 3.00
  @Date: 2013-07-18
  @Author: Beijing MoZhi ShiJi Information Technology Co., Ltd.
*/
package cn.com.mobnote.golukmobile.carrecorder;

import java.io.File;

import cn.com.mobnote.application.GolukApplication;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageManager {
	
	public static Bitmap getBitmapFromCache(String fileName){ 
		return BitmapFactory.decodeFile(fileName); 
	}
	
	public static Bitmap getBitmapFromCache(String filename, int width, int height){
		File file = new File(filename);
		if(file.exists()){
			BitmapFactory.Options opts = new BitmapFactory.Options();
	        opts.inJustDecodeBounds = true;
	        BitmapFactory .decodeFile(filename, opts); 
	        int inSampleSize=1;
	        if(opts.outWidth > width){   
	        	inSampleSize = opts.outWidth/width;  
	        
	        	opts.inSampleSize=inSampleSize;
		    	opts.inScaled = false;
		        opts.inJustDecodeBounds = false;
		        return BitmapFactory.decodeFile(filename, opts); 
	        }else{
	        	return BitmapFactory.decodeFile(filename); 
	        } 
		}else{
			return null;
		}  
	} 
	
	public static Bitmap getBitmapFromResource(int id){ 
		return BitmapFactory.decodeResource(GolukApplication.getInstance().getResources(), id); 
	}
	
	public static Bitmap getBitmapFromResource(int id, int width, int height){
//		if(opts.outWidth*opts.outHeight*4 > ){ //��������ڴ����޼���ж�
//			return null;
//		}else{
			BitmapFactory.Options opts = new BitmapFactory.Options();
	        opts.inJustDecodeBounds = true;
	        BitmapFactory.decodeResource(GolukApplication.getInstance().getResources(), id, opts); 
	        int inSampleSize=1;
	        if(opts.outWidth > width){   
	        	inSampleSize = opts.outWidth/width;  
	        
	        	opts.inSampleSize=inSampleSize;
		    	opts.inScaled = false;
		        opts.inJustDecodeBounds = false;
		        return BitmapFactory .decodeResource(GolukApplication.getInstance().getResources(), id, opts); 
	        }else{
	        	return BitmapFactory .decodeResource(GolukApplication.getInstance().getResources(), id); 
	        }   
//		} 
	}
//	
//	public static Bitmap createReflectedImage(Bitmap originalImage) {  
//		if(originalImage != null){ 
//			int reflectionGap = 4; 
//			int width = originalImage.getWidth();
//			int height = originalImage.getHeight(); 
//			Matrix matrix = new Matrix();
//			matrix.preScale(1, -1); 
//			Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, height/2, width, height/2, matrix, false);
//			Bitmap bitmapWithReflection = Bitmap.createBitmap(width,(height + height/7), Config.ARGB_8888);
//			Canvas canvas = new Canvas(bitmapWithReflection); 
//			Rect rect = new Rect(0, 0, originalImage.getWidth(), originalImage.getHeight());
//			canvas.drawBitmap(originalImage, rect, rect, null);  
//			Paint defaultPaint = new Paint();
//			defaultPaint.setColor(Color.TRANSPARENT);
//			canvas.drawRect(0, height, width, height + reflectionGap, defaultPaint);  
//			canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null); 
//			Paint paint = new Paint();
//			LinearGradient shader = new LinearGradient(0,originalImage.getHeight(), 0, bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff, TileMode.CLAMP);
//			paint.setShader(shader);
//			paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
//			canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);
//
//			return bitmapWithReflection;
//		}else{
//			return null;
//		} 
//	}
// 
//	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int corner){ 
//		if(bitmap != null){
//			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap    
//					.getHeight(), Config.ARGB_8888);  
//			Canvas canvas = new Canvas(output);    
//			int color = 0xffcccccc;    
//			Paint paint = new Paint();    
//			Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
//			Rect rect2 = new Rect(1, 1, bitmap.getWidth()-1, bitmap.getHeight()-1); 
//			RectF rectF = new RectF(rect2);    
//			paint.setAntiAlias(true);    
//			canvas.drawARGB(0, 0, 0, 0);    
//			paint.setColor(color);    
//			canvas.drawRoundRect(rectF, corner*density, corner*density, paint);    
//			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
//			canvas.drawBitmap(bitmap, rect, rect, paint);    
//			
//			bitmap.recycle();
//			return output; 
//		}else{
//			return null;
//		} 
//	}
//	
//	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap){ 
//		if(bitmap != null){
//			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap    
//					.getHeight(), Config.ARGB_8888);  
//			Canvas canvas = new Canvas(output);    
//			int color = 0xffcccccc;    
//			Paint paint = new Paint();    
//			Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
//			Rect rect2 = new Rect(1, 1, bitmap.getWidth()-1, bitmap.getHeight()-1); 
//			RectF rectF = new RectF(rect2);    
//			paint.setAntiAlias(true);    
//			canvas.drawARGB(0, 0, 0, 0);    
//			paint.setColor(color);    
//			canvas.drawRoundRect(rectF, 6*density, 6*density, paint);    
//			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
//			canvas.drawBitmap(bitmap, rect, rect, paint);    
//			
//			bitmap.recycle();
//			return output; 
//		}else{
//			return null;
//		} 
//	}
//	
//	public static Bitmap getZoomRotateBitmap(Bitmap bmpOrg, int zoomWidth, int zoomHeight) {
//		int width = bmpOrg.getWidth();
//		int height = bmpOrg.getHeight();
//    	     
//		int newWidth = zoomWidth;
//		int newheight = zoomHeight;
//		float sw = ((float) newWidth) / width;
//		float sh = ((float) newheight) / height;
//		android.graphics.Matrix matrix = new android.graphics.Matrix();
//		matrix.postScale(sw, sh);
//		matrix.postRotate(0);
//		Bitmap resizeBitmap = Bitmap.createBitmap(bmpOrg, 0, 0, width, height, matrix, true);
//		bmpOrg.recycle();
//		return resizeBitmap;
//    }
  
  	
}
