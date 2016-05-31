package cn.com.tiros.api;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Mutex implements Lock {

	public Mutex() {
	}

	// SYS_Mutex* sys_MutexCreate();
	//
	// void sys_MutexLock(SYS_Mutex* pmutex);
	//
	// void sys_MutexUnlock(SYS_Mutex* pmutex);
	//
	// void sys_MutexDestory(SYS_Mutex* pmutex);

	@Override
	public void lock() {
		// TODO Auto-generated method stub
		// this.lock();

		// GolukDebugUtils.i(TAG, "TTTTTT lock");

	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public Condition newCondition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean tryLock() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void unlock() {
		// TODO Auto-generated method stub
		// this.unlock();

		// GolukDebugUtils.i(TAG, "TTTTTT unlock");

	}

}
