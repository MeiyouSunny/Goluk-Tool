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
 * Method:    GolukLogicCommGet
 * Signature: (JIILjava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_cn_com_mobnote_logic_GolukLogicJni_GolukLogicCommGet
  (JNIEnv *env, jclass cls, jlong pLogic, jint mId, jint cId, jstring param) {
	  
	dbgprintf("%s,%s,%d,cmd:%d",__FILE__,__func__,__LINE__,cId);
	char * pParam = jstringTochar(env, param);
	const char * result = ((Goluk_LogicEngine *)pLogic)->CommGet(mId, cId, pParam);
	//dbgprintf("%s,%s,%d,result:%s",__FILE__,__func__,__LINE__,result);
	if (NULL == result) {
		free(pParam);
		return NULL;
	}
	free(pParam);
	
	return charToJstringUTF(env, result);
}
