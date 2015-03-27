/*
 * IPCManager_1WifiStateChanged.cpp
 *
 *  Created on: 2015-2-11
 *  Author: jiayf
 */
#include "cn_com_mobnote_logic_GolukLogicJni.h"
#include "../../lib/logic/Goluk_Logic.h"
#include "../../lib/api/system/debug.h"
#include "../../lib/jnibase/jni_system.h"

#include <android/log.h>

/*
 * Class:     cn_com_mobnote_logic_GolukLogicJni
 * Method:    GolukLogicCommRequest
 * Signature: (JIILjava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_com_mobnote_logic_GolukLogicJni_GolukLogicCommRequest
  (JNIEnv *env, jclass cls, jlong pLogic, jint mId, jint cmd, jstring param) {

	const char * pParam = env->GetStringUTFChars(param, 0);
	
	dbgprintf("jyf-----JNI-----goluk-----GolukLogicCommRequest--11: %d,%d,%s",mId, cmd, pParam);
	
	jboolean isSucess = ((Goluk_LogicEngine *)pLogic)->CommRequest(mId, cmd, pParam);
	
	env->ReleaseStringUTFChars(param, pParam);
	
	return isSucess;
	  	  
}
