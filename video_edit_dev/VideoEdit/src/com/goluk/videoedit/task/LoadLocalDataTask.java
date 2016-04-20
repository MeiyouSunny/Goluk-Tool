package com.goluk.videoedit.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.AsyncTask;
public class LoadLocalDataTask extends AsyncTask<String, String, String> {
	private VidLoadCallBack mDataCallBack = null;
	private String mFilePath = null;
	List<String> files = null;

	public LoadLocalDataTask(VidLoadCallBack callBack){
		this.mDataCallBack = callBack;
		this.mFilePath = android.os.Environment.getExternalStorageDirectory().getPath() + "/goluk/video/";
		files = new ArrayList<String>();
	}

	@Override
	protected String doInBackground(String... arg0) {
		String[] videoPaths = {"","loop/", "urgent/", "", "wonderful/" };
		files.addAll(getFileNames(mFilePath + videoPaths[1], "(.+?mp4)"));
		files.addAll(getFileNames(mFilePath + videoPaths[2], "(.+?mp4)"));
		files.addAll(getFileNames(mFilePath + videoPaths[4], "(.+?mp4)"));
		
		if (null == files || files.size() <= 0) {
			return null;
		}

		//Collections.sort(files, new SortByDate());

		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		mDataCallBack.OnLoadSucced(files);
	}

	public interface VidLoadCallBack{
		public void OnLoadSucced(List<String> list);
	}

	public ArrayList<String> getFileNames(final String folder, final String fileNameFilterPattern) {
		ArrayList<String> myData = new ArrayList<String>();
		File fileDir = new File(folder);
		if (!fileDir.exists() || !fileDir.isDirectory()) {
			return myData;
		}

		String[] files = fileDir.list();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (fileNameFilterPattern == null || files[i].matches(fileNameFilterPattern))
					myData.add(files[i]);
			}
		}

		return myData;
	}
}

