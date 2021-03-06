package cn.com.tiros.api;

import java.io.File;
import java.util.Vector;

public class EnumFile {
	
	private int mFileIndex = 0;
	
	private Vector<File> mFiles = new Vector<File>(); 

	/**
	 * ��ʼö���ļ�Ŀ¼
	 * @param FileDir, isAll �Ƿ�����ö����Ŀ¼��false��ʾֻö���ļ�,true��ʾֻö����Ŀ¼
	 * @return Vector
	 */
	public void sys_fenumstart(String FileDir, boolean bDirs){	
		String filePath = FileUtils.libToJavaPath(FileDir);
		File file = new File(filePath);
		filePath = null;
		
		File[] listFiles = file.listFiles();
		
		if(listFiles == null){
			return;
		}
		
		if(bDirs){
			 for (int j = 0; j < listFiles.length; j++) {
				 if(listFiles[j].isDirectory()){
					 mFiles.add(listFiles[j]);
				 }
			 }
		}else{
			for (int i = 0; i < listFiles.length; i++) {
				if(listFiles[i].isFile()){
					mFiles.add(listFiles[i]);
				}
			}
		}
		mFileIndex = 0;
	}
	
	/**
	 * ö���ļ�Ŀ¼
	 * @param bDirs
	 * @return
	 */
	public String sys_fenumnext() {
		if(mFiles.size() < 1){
			return null;
		}		
		if (mFileIndex >= mFiles.size()){
			return null;
		}
		
		String fileName = mFiles.get(mFileIndex).getName();	
		mFileIndex++;
		return fileName;
	}
	
	/**
	 * �����ļ�ö��
	 * @param bDirs
	 * @return
	 */
	public void sys_fenumend() {
		mFiles.removeAllElements();
		mFiles = null;
		mFileIndex = 0;
	}
}
