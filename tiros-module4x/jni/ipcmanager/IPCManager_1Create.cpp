/*
 * IPCManager_1Create.cpp
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
 * Method:    IPCManager_Create
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_cn_com_mobnote_tachograph_comm_IPCManagerJNI_IPCManager_1Create
  (JNIEnv * env, jclass cls){
	 dbgprintf("IPCManager-----JNI------Create");
	 IPcManager * pManager =  IPCMgr_Create();
	 return (jint)pManager;
}


