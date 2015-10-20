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

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.newest.JsonParserUtils;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.ClipImageView;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.SettingImageView;
import cn.com.tiros.api.FileUtils;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ImageClipActivity extends BaseActivity implements OnClickListener,
		IPageNotifyFn {

	private ClipImageView imageView;
	private Button saveHead;
	
	private CustomLoadingDialog mCustomProgressDialog = null;

	private SettingImageView siv = null;

	/** 视频存放外卡文件路径 */
	private static final String APP_FOLDER = android.os.Environment
			.getExternalStorageDirectory().getPath();

	private static final String headCachePatch = APP_FOLDER
			+ "/goluk/head_cache/";
	
	private String cachePath = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.roadbook_crop_pic);
		Uri uri = Uri.parse(getIntent().getStringExtra("imageuri"));
		saveHead = (Button) findViewById(R.id.saveBtn);
		imageView = (ClipImageView) findViewById(R.id.src_pic);
		imageView.setImageURI(uri);
		siv = new SettingImageView(this);
		initListener();

	}

	private void initListener() {
		saveHead.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
		if(mCustomProgressDialog == null){
			mCustomProgressDialog = new CustomLoadingDialog(this, "图片生成中,请稍后!");
			mCustomProgressDialog.show();
		}else{
			mCustomProgressDialog.show();
		}
		Bitmap bitmap = imageView.clip();

		try {
			String request = this.saveBitmap(siv.toRoundBitmap(bitmap));
			if (request != null) {
				boolean flog = this.uploadImageHead(request);
				System.out.println("flog =" + flog);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bitmap.recycle();
	}

	/**
	 * 调用图像上传功能
	 * 
	 * @return
	 */
	public boolean uploadImageHead(String requestStr) {
		return mBaseApp.mGoluk.GolukLogicCommRequest(
				GolukModule.Goluk_Module_HttpPage, PageType_ModifyHeadPic,
				requestStr);
	}

	/**
	 * 保存头像并上传
	 * 
	 * @throws IOException
	 * @throws JSONException
	 */
	public String saveBitmap(Bitmap bm) throws IOException, JSONException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);

		JSONObject requestStr = null;
		byte[] bb = baos.toByteArray();
		System.out.println("bitmap 长度:"+ bb.length);
		String md5key = this.compute32(bb);
		String picname = System.currentTimeMillis() + ".png";
		System.out.println("imagename" + picname);

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
			bm.compress(Bitmap.CompressFormat.PNG, 100, out);

			requestStr = new JSONObject();
			requestStr.put("PicMD5", md5key);
			requestStr.put("PicPath", FileUtils.javaToLibPath(cachePath));
			requestStr.put("channel", "2");

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
			return requestStr.toString();
		}

	}

	public static String compute32(byte[] content) {
		StringBuffer buf = new StringBuffer("");
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			try {
				md.update(content);
			} catch (Exception e) {
				e.printStackTrace();
			}
			byte b[] = md.digest();
			int i;
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return buf.toString();
	}

	public static void makeRootDirectory(String filePath) {
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
		// TODO Auto-generated method stub
		mBaseApp.setContext(this, "imageClipActivity");
		super.onResume();
	}

	@Override
	public void pageNotifyCallBack(int type, int success, Object param1,
			Object param2) {
		// TODO Auto-generated method stub
		if(type == PageType_ModifyHeadPic){
			if(mCustomProgressDialog.isShowing()){
				mCustomProgressDialog.close();
			}
			if(success == 1){
				try {
					JSONObject result = new JSONObject(param2.toString());
					if(result != null){
						Boolean suc = result.getBoolean("success");
						
						if(suc){
							JSONObject data = result.getJSONObject("data");
							String rst = data.getString("result");
							//图片上传成功
							if("0".equals(rst)){
								if(cachePath != null && !"".equals(cachePath)){
									File  file = new File(cachePath);
									if(file.exists()){
										file.delete();
									}
									cachePath = "";
								}
								
								String path = data.getString("customavatar");
								GolukUtils.showToast(ImageClipActivity.this, "图片上传成功");
								
								Intent it = new Intent(ImageClipActivity.this,
										UserPersonalInfoActivity.class);
								it.putExtra("imagepath", path);
								this.setResult(RESULT_OK, it);
								this.finish();
							}
							
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
