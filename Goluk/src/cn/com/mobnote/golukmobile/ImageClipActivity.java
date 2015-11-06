package cn.com.mobnote.golukmobile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.ClipImageView;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.SettingImageView;
import cn.com.tiros.api.FileUtils;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ImageClipActivity extends BaseActivity implements OnClickListener, IPageNotifyFn {

	private ClipImageView imageView;

	private Button saveHead;

	private Button cancelBtn;

	private CustomLoadingDialog mCustomProgressDialog = null;

//	private SettingImageView siv = null;

	private boolean isSave = true;

	/** 视频存放外卡文件路径 */
	private static final String APP_FOLDER = android.os.Environment.getExternalStorageDirectory().getPath();

	private static final String headCachePatch = APP_FOLDER + "/goluk/head_cache/";

	private String cachePath = "";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.roadbook_crop_pic);
		
		mCustomProgressDialog = new CustomLoadingDialog(ImageClipActivity.this, "正在保存头像,请稍候!");
		saveHead = (Button) findViewById(R.id.saveBtn);
		cancelBtn = (Button) findViewById(R.id.cancelBtn);
		imageView = (ClipImageView) findViewById(R.id.src_pic);

		try {
			String uriStr = getIntent().getStringExtra("imageuri");
			Uri  uri = null;
			Bitmap bitmap = null;
			if(uriStr != null  && !"".equals(uriStr)){
				uri = Uri.parse(getIntent().getStringExtra("imageuri"));
				BitmapFactory.Options options = new BitmapFactory.Options();
//				options.inJustDecodeBounds = true;
//				BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
				options.inJustDecodeBounds = false;
				options.inSampleSize = 4;// 图片宽高都为原来的4分之一，即图片为原来的8分之一
				bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
			}else{
				bitmap =  getIntent().getParcelableExtra("imagebitmap");
			}
			
			if(bitmap == null){
				GolukUtils.showToast(ImageClipActivity.this, "文件格式不正确");
				this.finish();
			}else{
				if (bitmap.getHeight() < bitmap.getWidth()) {
					Bitmap bp = bitmap;
					bitmap = this.rotaingImageView(90, bp);
					bp.recycle();
				}
				imageView.setImageBitmap(bitmap);
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}

//		siv = new SettingImageView(this);
		initListener();

	}

	/*
	 * 旋转图片
	 * 
	 * @param angle
	 * 
	 * @param bitmap
	 * 
	 * @return Bitmap
	 */
	public  Bitmap rotaingImageView(int angle, Bitmap bitmap) {
		// 旋转图片 动作
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		System.out.println("angle2=" + angle);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}

	private void initListener() {
		saveHead.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.saveBtn:
			if (isSave) {
				isSave = false;
				if (mCustomProgressDialog != null) {
					mCustomProgressDialog.show();
				}
				Bitmap bitmap = imageView.clip();

				if (bitmap == null) {
					isSave = true;
					return;
				}
				try {
					String request = this.saveBitmap(SettingImageView.toRoundBitmap(bitmap));
					if (request != null) {
						boolean flog = this.uploadImageHead(request);
						System.out.println("flog =" + flog);
					}
				} catch (IOException e) {
					isSave = true;
					e.printStackTrace();
				} catch (JSONException e) {
					isSave = true;
					e.printStackTrace();
				}
				bitmap.recycle();
			}

			break;
		case R.id.cancelBtn: {
			this.finish();
			break;
		}
		default:
			break;
		}

	}

	/**
	 * 调用图像上传功能
	 * 
	 * @return
	 */
	public boolean uploadImageHead(String requestStr) {
		return mBaseApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, PageType_ModifyHeadPic,
				requestStr);
	}

	/**
	 * 保存头像并上传
	 * 
	 * @throws IOException
	 * @throws JSONException
	 */
	@SuppressWarnings("finally")
	public String saveBitmap(Bitmap bm) throws IOException, JSONException {


		JSONObject requestStr = null;

		String md5key = "";

		String picname = System.currentTimeMillis() + ".png";

		this.makeRootDirectory(headCachePatch);
		cachePath = headCachePatch + picname;

		File f = new File(cachePath);
		if (f.exists()) {
			f.delete();
		}
		FileOutputStream out = null;
		try {

			f.createNewFile();
			out = new FileOutputStream(f);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Bitmap cacheBitmap = this.compress(bm);

			cacheBitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
			byte[] bb = baos.toByteArray();
			
			md5key = GolukUtils.compute32(bb);
			baos.writeTo(out);
			requestStr = new JSONObject();
			requestStr.put("PicMD5", md5key);
			requestStr.put("PicPath", FileUtils.javaToLibPath(cachePath));
			requestStr.put("channel", "2");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			} else {
			}
			return requestStr.toString();
		}

	}

	/**
	 * 把图片压缩到100k 之下
	 * 
	 * @param image
	 * @return
	 */
	public Bitmap compress(Bitmap image) {
		// 图片允许最大空间 单位：KB
		double maxSize = 50.00;
		// 将bitmap放至数组中，意在bitmap的大小（与实际读取的原文件要大）
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] b = baos.toByteArray();
		// 将字节换成KB
		double mid = b.length / 1024;
		// 判断bitmap占用空间是否大于允许最大空间 如果大于则压缩 小于则不压缩
		if (mid > maxSize) {
			// 获取bitmap大小 是允许最大大小的多少倍
			double i = mid / maxSize;
			// 开始压缩 此处用到平方根 将宽带和高度压缩掉对应的平方根倍
			// （1.保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小）
			image = zoomImage(image, image.getWidth() / Math.sqrt(i), image.getHeight() / Math.sqrt(i));
		}

		return image;

	}

	/***
	 * 图片的缩放方法
	 * 
	 * @param bgimage
	 *            ：源图片资源
	 * @param newWidth
	 *            ：缩放后宽度
	 * @param newHeight
	 *            ：缩放后高度
	 * @return
	 */
	public static Bitmap zoomImage(Bitmap bgimage, double newWidth, double newHeight) {
		// 获取这个图片的宽和高
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算宽高缩放率
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width, (int) height, matrix, true);
		return bitmap;
	}

	public void makeRootDirectory(String filePath) {
		File file = null;
		try {
			file = new File(filePath);
			if (!file.exists()) {
				file.mkdir();
			}
		} catch (Exception e) {

		}
	}

	@Override
	protected void onResume() {
		mBaseApp.setContext(this, "imageClipActivity");
		super.onResume();
	}

	@Override
	public void pageNotifyCallBack(int type, int success, Object param1, Object param2) {
		if (type == PageType_ModifyHeadPic) {
			isSave = true;
			if (mCustomProgressDialog.isShowing()) {
				mCustomProgressDialog.close();
			}
			if (success == 1) {
				try {
					JSONObject result = new JSONObject(param2.toString());
					Boolean suc = result.getBoolean("success");

					if (suc) {
						JSONObject data = result.getJSONObject("data");
						String rst = data.getString("result");
						// 图片上传成功
						if ("0".equals(rst)) {
							if (cachePath != null && !"".equals(cachePath)) {
								File file = new File(cachePath);
								if (file.exists()) {
									file.delete();
								}
								cachePath = "";
							}

							String path = data.getString("customavatar");
							GolukUtils.showToast(ImageClipActivity.this, "图片保存成功");

							Intent it = new Intent(ImageClipActivity.this, UserPersonalInfoActivity.class);
							it.putExtra("imagepath", path);
							this.setResult(RESULT_OK, it);
							this.finish();
						} else {
							GolukUtils.showToast(ImageClipActivity.this, "头像保存失败，请重试");
						}

					} else {
						GolukUtils.showToast(ImageClipActivity.this, "头像保存失败，请重试");
					}
				} catch (JSONException e) {
					GolukUtils.showToast(ImageClipActivity.this, "头像保存失败，请重试");
					e.printStackTrace();
				}
			} else {

				GolukUtils.showToast(ImageClipActivity.this, "网络不给力");
			}
		}
	}

}
