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
#include "../../lib/logic/Goluk_HttpPage_def.h"
#include "../../lib/logic/Goluk_IPCManager_def.h"
#include "../../lib/logic/Goluk_Location_def.h"
#include "../../lib/logic/Goluk_Module_def.h"


#include "module_videosquare/Module_videosquare.h"
#include "module_ipcmanage/Module_ipcManager.h"
#include "module_page/Module_page.h"
#include "module_talk/Module_talk.h"

#include <android/log.h>

/*
 * Class:     cn_com_mobnote_logic_GolukLogicJni
 * Method:    GolukLogicRegisterNotify
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_cn_com_mobnote_logic_GolukLogicJni_GolukLogicRegisterNotify
  (JNIEnv *env, jclass cls, jlong pLogic, jint mId) {
	  
	switch(mId) {
	case Goluk_Module_HttpPage:
		return ((Goluk_LogicEngine *)pLogic)->RegistNotify(mId, (unsigned long)PageNotify_CallBack, 0);
	case Goluk_Module_Talk:
		return ((Goluk_LogicEngine *)pLogic)->RegistNotify(mId, (unsigned long)CallBack_Talk, 0);
	case Goluk_Module_IPCManager:
		return ((Goluk_LogicEngine *)pLogic)->RegistNotify(mId, (unsigned long)CallBack_ipcManager, 0);
	case Goluk_Module_Square:
		return ((Goluk_LogicEngine *)pLogic)->RegistNotify(mId, (unsigned long)CallBack_videosquare, 0);
	default:
		break;	
	}
	
	return 0;
}
