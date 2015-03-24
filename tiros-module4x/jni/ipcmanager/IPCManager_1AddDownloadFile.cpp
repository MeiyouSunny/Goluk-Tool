#include "cn_com_mobnote_tachograph_comm_IPCManagerJNI.h"
#include "../../lib/ipcmanger/IPCManager.h"
#include "../../lib/api/system/debug.h"
#include "../../lib/jnibase/jni_system.h"
#include <android/log.h>


/*
 * Class:     cn_com_mobnote_tachograph_comm_IPCManagerJNI
 * Method:    IPCManager_AddDownloadFile
 * Signature: (ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_cn_com_mobnote_tachograph_comm_IPCManagerJNI_IPCManager_1AddDownloadFile
  (JNIEnv * env, jclass cls, jint pManager, jstring filename,jstring tag, jstring savePath) {
	 
		char * pFileName = jstringTochar(env,filename);
		char * pTag = jstringTochar(env,tag);
		char * pSavePath = jstringTochar(env,savePath);
		
		IPCMgr_VDTP_AddDownloadFile((IPcManager *)pManager, pFileName, pTag, pSavePath);
		
		free(pFileName); 
		free(pTag);
		free(pSavePath);
}
