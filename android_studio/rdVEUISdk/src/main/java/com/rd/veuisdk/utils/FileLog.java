package com.rd.veuisdk.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 文件日志
 * 
 * @author abreal
 * 
 */
public class FileLog {
    private final static SimpleDateFormat sdfLogFileName = new SimpleDateFormat(
	    "yyyy-MM-dd", Locale.getDefault());
    public final static SimpleDateFormat sdfLogMsgPrefix = new SimpleDateFormat(
	    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private static String g_strLogPath;

    /**
     * 设置日志目录
     * 
     * @param strLogPath
     */
    public static void setLogPath(String strLogPath) {
	g_strLogPath = strLogPath;
    }

    /**
     * 写入日志
     * 
     * @param strMsg
     */
    public static void writeLog(String strMsg) {
	writeLog(false, strMsg);
    }

    /**
     * 写入异常日志
     * 
     * @param err
     */
    public static void writeThrowableLog(Throwable err) {
	String strLogFileName = String.format("%s.log",
		sdfLogFileName.format(new Date()));
	PrintStream ps;
	try {
	    ps = new PrintStream(strLogFileName);
	    err.printStackTrace(ps);
	    ps.close();
	} catch (FileNotFoundException e) {
	}
    }

    /**
     * 写入日志
     * 
     * @param strMsg
     */
    public static void writeLog(boolean noPrefix, String strMsg) {
	FileWriter fwLogger = null;
	String strLogFileName = String.format("%s.log",
		sdfLogFileName.format(new Date()));
	try {
	    File logFile = new File(g_strLogPath, strLogFileName);
	    if (!logFile.exists()) {
		logFile.createNewFile();
	    }
	    fwLogger = new FileWriter(logFile, true);
	    if (noPrefix) {
		fwLogger.write(strMsg + "\r\n");
	    } else {
		fwLogger.write(String.format("[%s]%s\r\n",
			sdfLogMsgPrefix.format(new Date()), strMsg));
	    }
	    // fwLogger.flush();
	    // fwLogger.close();
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    if (null != fwLogger) {
		try {
		    fwLogger.flush();
		    fwLogger.close();
		    fwLogger = null;
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}
    }
}
