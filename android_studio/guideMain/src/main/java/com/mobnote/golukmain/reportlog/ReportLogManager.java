package com.mobnote.golukmain.reportlog;

import java.util.HashMap;

public class ReportLogManager {

	private HashMap<String, ReportLog> mHash = new HashMap<String, ReportLog>();
	private static ReportLogManager mInstance = new ReportLogManager();

	public static ReportLogManager getInstance() {
		return mInstance;
	}

	public ReportLog getReport(String key) {
		if (mHash.containsKey(key)) {
			return mHash.get(key);
		}
		ReportLog log = new ReportLog(key);
		mHash.put(key, log);
		return log;
	}

	public void removeKey(String key) {
		mHash.remove(key);
	}

}
