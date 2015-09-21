/*
 * MapNaviCtrlJni_MapNaviCtrl_1Create.cpp
 *
 *  Created on: 2015-4-14
 *      Author: jiayf
 */
#include "Module_location.h"


void LocationNotify_CallBack(const char *positionJson, Position position, void *pvuser){
	
	//dbgprintf("jyf-----JNI-----goluk-----LocationNotify_CallBack--11: %s",positionJson);

	JNIEnv * g_env;
	jvm->AttachCurrentThread(&g_env, 0);
	if (!positionJson) {
		return;
	}
	//dbgprintf("jyf-----JNI-----goluk-----LocationNotify_CallBack--22");
	jclass cls = g_env->FindClass("cn/com/mobnote/module/location/LocationNotifyAdapter");
	jmethodID mId = g_env->GetStaticMethodID(cls, "LocationCallBack", "(Ljava/lang/String;)V");
	
	//dbgprintf("jyf-----JNI-----goluk-----LocationNotify_CallBack--333");
	jstring locationStr = charToJstringUTF(g_env, positionJson);
	g_env->CallStaticVoidMethod(cls, mId, locationStr);
	
	//dbgprintf("jyf-----JNI-----goluk-----LocationNotify_CallBack--444");
	
	g_env->DeleteLocalRef(locationStr);
	g_env->DeleteLocalRef(cls);
	
	//dbgprintf("jyf-----JNI-----goluk-----LocationNotify_CallBack--5555");
	
}























