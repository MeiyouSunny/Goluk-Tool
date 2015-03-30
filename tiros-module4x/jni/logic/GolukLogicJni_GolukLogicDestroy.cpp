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
 * Method:    GolukLogicDestroy
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_cn_com_mobnote_logic_GolukLogicJni_GolukLogicDestroy
  (JNIEnv *, jclass, jlong pLogic) {
		
	delete((Goluk_LogicEngine *)pLogic);
	  
}
