/*
 * MapNaviCtrlJni_MapNaviCtrl_1Create.cpp
 *
 *  Created on: 2014-11-13
 *      Author: jiayf
 */
#include "cn_com_mobonote_golukmobile_comm_GolukMobileJni.h"
#include "../../lib/logic/Logic.h"
#include "../../lib/api/system/debug.h"
#include "../../lib/jnibase/jni_system.h"

#include <android/log.h>

/*
 * Class:     cn_com_mobonote_golukmobile_comm_GolukMobileJni
 * Method:    GoLuk_WifiStateChanged
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_cn_com_mobonote_golukmobile_comm_GolukMobileJni_GoLuk_1WifiStateChanged
  (JNIEnv * env, jclass cls, jint plogic,jboolean isSucess) {
	  
	LogicEngine *pL = (LogicEngine*)plogic;
	pL->WifiStateChanged(isSucess);
	    
}

