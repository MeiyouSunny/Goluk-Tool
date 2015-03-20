/*
 * IPCManager_1WifiStateChanged.cpp
 *
 *  Created on: 2015-2-11
 *  Author: jiayf
 */
#include "cn_com_mobnote_tachograph_comm_IPCManagerJNI.h"
#include "../../lib/ipcmanger/IPCManager.h"
#include "../../lib/api/system/debug.h"
#include "../../lib/jnibase/jni_system.h"
#include <android/log.h>

/*
 * Class:     cn_com_mobnote_tachograph_comm_IPCManagerJNI
 * Method:    IPCManager_SetMode
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_cn_com_mobnote_tachograph_comm_IPCManagerJNI_IPCManager_1SetMode
  (JNIEnv *env, jclass cls, jint pManager, jint mode) {
	  
	IPCMgr_SetMode((IPcManager *)pManager, (IPCMgrMode)mode);
	  
}
