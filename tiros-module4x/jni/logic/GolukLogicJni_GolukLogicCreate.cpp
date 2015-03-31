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
 * Method:    GolukLogicCreate
 * Signature: ()I
 */
JNIEXPORT jlong JNICALL Java_cn_com_mobnote_logic_GolukLogicJni_GolukLogicCreate
  (JNIEnv *env, jclass cls) {
	  
	Goluk_LogicEngine *pLogic = new Goluk_LogicEngine();
	return (jlong)pLogic;
}
