/*
 * Talk_1RegisterNotify.cpp
 *
 *  Created on: 2015-3-31
 *  Author: jiayf
 */
 
#include "Module_talk.h"

void CallBack_Talk(void* pvUser, int dwParam1, const char *dwParam2){
	
	JNIEnv * g_env;
	jvm->AttachCurrentThread(&g_env,0);
	
	dbgprintf("jyf-----JNI-----goluk-----CallBack_Talk--11: %d",dwParam1);
	
	dbgprintf("IPCManager-----JNI------CallBack_Talk----11111");
	
	jclass cls =g_env->FindClass("cn/com/mobnote/module/talk/TalkNotifyAdapter");
	jmethodID mId_CallBack = g_env->GetStaticMethodID(cls,"TalkNotifyCallBack","(ILjava/lang/String;)V");
	jstring data = 0;
	if(0 != dwParam2){
		data = charToJstringUTF(g_env, (char *)dwParam2);
	}
	
	dbgprintf("IPCManager-----JNI------CallBack_ipcManager----22222");
	
	g_env->CallStaticVoidMethod(cls,mId_CallBack,dwParam1,data);
	
	dbgprintf("IPCManager-----JNI------CallBack_ipcManager----33333");
	
	// free
	g_env->DeleteLocalRef(cls);
}
