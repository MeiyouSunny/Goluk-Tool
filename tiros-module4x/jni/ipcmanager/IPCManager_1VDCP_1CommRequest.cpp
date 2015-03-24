/*
 * IPCManager_1SetVDCPSessionID.cpp
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
 * Method:    IPCManager_VDCP_CommRequest
 * Signature: (IILjava/lang/String;)V
 */
JNIEXPORT jboolean JNICALL Java_cn_com_mobnote_tachograph_comm_IPCManagerJNI_IPCManager_1VDCP_1CommRequest
  (JNIEnv *env, jclass cls, jint pManager, jint cmd, jstring param) {
	  
	char * pParam = jstringTochar(env,param);
	dbgprintf("IPCManager----------------JNI-----------CommRequest:----1111 %s ,%d: " , pParam,cmd);
	jboolean result = (jboolean)IPCMgr_VDCP_CommRequest((IPcManager *)pManager,(IPC_VDCP_Command)cmd,pParam);
	
	dbgprintf("IPCManager----------------JNI-----------CommRequest:----22222");
	free(pParam);
	
	return result;  
}


