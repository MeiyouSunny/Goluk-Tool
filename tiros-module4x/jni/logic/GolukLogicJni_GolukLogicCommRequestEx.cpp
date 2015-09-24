/*
 * IPCManager_1WifiStateChanged.cpp
 *
 *  Created on: 2015-9-24
 *  Author: jiayf
 */
#include "cn_com_mobnote_logic_GolukLogicJni.h"
#include "../../lib/logic/Goluk_Logic.h"
#include "../../lib/api/system/debug.h"
#include "../../lib/jnibase/jni_system.h"

#include <android/log.h>
  
  /*
 * Class:     cn_com_mobnote_logic_GolukLogicJni
 * Method:    CommRequestEx
 * Signature: (JIILjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_cn_com_mobnote_logic_GolukLogicJni_CommRequestEx
  (JNIEnv *env, jclass cls, jlong pLogic, jint mId, jint cmd, jstring param) {

	const char * pParam = env->GetStringUTFChars(param, 0);
	
	dbgprintf("jyf-----JNI-----goluk-----GolukLogicCommRequestEx-11: %d,%d,%s",mId, cmd, pParam);
	
	jlong sessionId =(jlong) ((Goluk_LogicEngine *)pLogic)->CommRequestEx(mId, cmd, pParam);
	
	env->ReleaseStringUTFChars(param, pParam);
	
	return sessionId;
	  	  
}
