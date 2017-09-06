package com.rd.veuisdk.export;

import java.util.ArrayList;

import com.rd.veuisdk.model.SpecialInfo;

/**
 * 多线程导出特效字幕
 * 
 * @author JIAN
 * 
 */
public interface IExportSpecial {

	/**
	 * 特效
	 * 
	 * @param infos
	 */
	void onSpecial(ArrayList<SpecialInfo> infos);

}
