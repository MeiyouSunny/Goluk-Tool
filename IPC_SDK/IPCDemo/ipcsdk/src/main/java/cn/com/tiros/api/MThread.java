package cn.com.tiros.api;

import android.os.Handler;
import android.os.Message;

public class MThread {

	private static final String TAG = MThread.class.getSimpleName();

	//
	// thread.h
	// Thread API
	//
	// Created by Harry on 13-10-31.
	// Copyright (c) 2013年 Harry. All rights reserved.

	// #ifndef __THREAD_H__
	// #define __THREAD_H__

	// #include "./boolean.h"

	// #define ThreadCreate() \
	// tr_ThreadCreate()
	// #define ThreadSetPriority(pthread, priority) \
	// tr_ThreadSetPriority(pthread, priority)
	// #define ThreadSetProcAttr(pthread, proc,notify, pvuser) \
	// tr_ThreadSetProcAttr(pthread, proc,notify, pvuser)
	// #define ThreadStart(pthread) \
	// tr_ThreadStart(pthread)
	// #define ThreadCancel(pthread) \
	// tr_ThreadCancel(pthread)
	// #define ThreadIsExecuting(pthread) \
	// tr_ThreadIsExecuting(pthread)
	// #define ThreadDestory(pthread) \
	// tr_ThreadDestory(pthread)

	// typedef struct _ThreadStr Thread;

	// typedef enum _ThreadPriority
	// {
	private static int EThreadPriority_low = -1;
	private static int EThreadPriority_default = 0;
	private static int EThreadPriority_high = 1;
	// }ThreadPriority;

	// 线程执行体函数原型
	// typedef void (*SYS_ThreadProcFunc)(void* pvuser);
	// 线程执行完后的回调函数原型
	// typedef void (*SYS_ThreadNotifyFunc)(void* pvuser);

	// #ifdef __cplusplus
	// extern "C" {
	// #endif

	private int mThreadHandler;

	/**
	 * 函数执行完成后，是否通知主线程
	 */
	private boolean mNotify = false;

	// /**
	// * 线程启动运行，初始化就启动运行，销毁结束运行
	// */
	// private boolean mRunning = true;

	// /**
	// * 线程执行结构，外部启动变为true，否则为false
	// */
	// private boolean mIsProcess = false;

	private MyThread mThread = null;

	public MThread() {
	}

	// SYS_Thread* sys_ThreadCreate(SYS_ThreadProcFunc proc,
	// SYS_ThreadNotifyFunc notify, void* pvuser);

	// unsigned char sys_ThreadStart(SYS_Thread* pthread);

	// unsigned char sys_ThreadIsExecuting(SYS_Thread* pthread);

	// void sys_ThreadDestory(SYS_Thread* pthread);

	/**
	 * @brief 创建线程句柄
	 * @param level
	 *            - 线程优先级参数
	 * @param proc
	 *            - 线程执行函数地址
	 * @param pvuser
	 *            - 传递给线程执行的参数
	 * @param notify
	 *            - 线程执行完毕之后通知主线程函数地址
	 * @return - 创建成功的线程句柄，NULL为失败
	 */
	public void sys_ThreadCreate(int handler, int bnotify) {
		mThreadHandler = handler;

		// GolukDebugUtils.i(TAG, "TTTTTT tr_ThreadCreate  bnotify = " +
		// bnotify);

		// 是否有回调
		if (bnotify == 0) {
			mNotify = false;
		} else {
			mNotify = true;
		}

		// GolukDebugUtils.i(TAG, "TTTTTT tr_ThreadCreate mThreadHandler = " +
		// mThreadHandler);

	}

	// /**
	// * @brief 设置线程优先级,默认为EThreadPriority_default
	// * @param pthread
	// * - 线程句柄
	// * @param level
	// * - 线程优先级参数
	// * @return - 创建成功的线程句柄，NULL为失败
	// */
	// public void tr_ThreadSetPriority(int priority) {
	// if (priority == EThreadPriority_low) {
	// setPriority(Thread.MIN_PRIORITY);
	// } else if (priority == EThreadPriority_high) {
	// setPriority(Thread.MAX_PRIORITY);
	// } else if (priority == EThreadPriority_default) {
	// setPriority(Thread.NORM_PRIORITY);
	// } else {
	// setPriority(Thread.NORM_PRIORITY);
	// }
	// }

	/**
	 * @brief 启动线程
	 * @param pthread
	 *            - 线程句柄
	 * @return - 成功true/失败false
	 */
	public int sys_ThreadStart() {

		// GolukDebugUtils.i(TAG, "TTTTTT sys_ThreadStart");

		if (mThread != null) {
			mThread.interrupt();
			mThread = null;
		}
		mThread = new MyThread();
		mThread.start();
		return 1;
	}

	// /**
	// * @brief 取消线程
	// * @param pthread
	// * - 线程句柄
	// * @return - 成功true/失败false
	// */
	// public boolean tr_ThreadCancel() {
	// try {
	// interrupt();
	// } catch (Exception e) {
	// return false;
	// }
	// return true;
	// }

	/**
	 * @brief 获取线程是否正在运行
	 * @param pthread
	 *            - 线程句柄
	 * @return - true：正在执行/false：空闲或者完成
	 */
	public int sys_ThreadIsExecuting() {

		// boolean ret = this.isAlive();

		// GolukDebugUtils.i(TAG, "TTTTTT sys_ThreadIsExecuting  mThread = " +
		// mThread);

		if (mThread != null) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * @brief 销毁线程
	 * @param pthread
	 *            - 线程句柄
	 * @return - 成功true/失败false
	 */
	public void sys_ThreadDestory() {

		// GolukDebugUtils.i(TAG, "TTTTTT tr_ThreadDestory");
		if (null == mThread) {
			return;
		}

		try {
			mThread.interrupt();
			mThread = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				// GolukDebugUtils.i(TAG, "TTTTTT handleMessage  1");
				if (mThread != null) {
					mThread = null;
					SYS_ThreadNotifyFunc(mThreadHandler);
				}
				break;
			case 2:
				// GolukDebugUtils.i(TAG, "TTTTTT handleMessage  2");
				if (mThread != null) {
					mThread = null;
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	class MyThread extends Thread {

		/**
		 * 线程执行方法
		 */
		public void run() {
			// GolukDebugUtils.i(TAG, "TTTTTT run");
			SYS_ThreadProcFunc(mThreadHandler);
			if (Thread.interrupted())
				return;
			if (mNotify) {
				synchronized (MThread.this) {
					handler.sendEmptyMessage(1);
				}
			} else {
				synchronized (MThread.this) {
					handler.sendEmptyMessage(2);
				}
			}
		}

	}

	public static native void SYS_ThreadProcFunc(int handler);

	public static native void SYS_ThreadNotifyFunc(int handler);

	// #ifdef __cplusplus
	// }
	// #endif
	//
	// #endif /*__THREAD_H__*/

}