/*
 * MapNaviCtrlJni_MapNaviCtrl_1Create.cpp
 *
 *  Created on: 2012-2-13
 *      Author: shizy
 *      Modify: caoyp 2012-07-21 添加POI显示的回调接口
 *      Modify: caoyp 2012-08-29 
 *      Modify: caoyp 2012-11-06
 */
#include "cn_com_mobonote_golukmobile_comm_GolukMobileJni.h"
#include "../../lib/logic/Logic.h"
#include "../../lib/api/system/debug.h"
#include "../../lib/jnibase/jni_system.h"

#include <android/log.h>

/*
 * Class:     cn_com_mobonote_golukmobile_comm_GolukMobileJni
 * Method:    GoLuk_CommonGetPage
 * Signature: (ILjava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_com_mobonote_golukmobile_comm_GolukMobileJni_GoLuk_1CommonGetPage
  (JNIEnv * env, jclass cls,jint plogic, jint type, jstring params) {
	jboolean rev = false;
	LogicEngine *pL = (LogicEngine*)plogic;
	char *pParams = jstringTochar(env,params);
	rev = pL->CommonGetPage((PageType)type,pParams);
	free(pParams);	  
	return rev;
}


