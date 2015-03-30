/*
 * IPCManager_1RegisterNetTransNotify.cpp
 *
 *  Created on: 2015-2-11
 *  Author: jiayf
 */
 
#include "Module_ipcManager.h"

void CallBack_ipcManager(void* pvUser, Net_Trans_Event event ,int msg , unsigned long param1, unsigned long param2){
	JNIEnv * g_env;
	jvm->AttachCurrentThread(&g_env,0);
	
	dbgprintf("jyf-----JNI-----goluk-----CallBack_ipcManager--11: %d,%d",event, msg);
	
	dbgprintf("IPCManager-----JNI------CallBack_ipcManager----11111");
	
	jclass cls =g_env->FindClass("cn/com/mobnote/module/ipcmanager/IPCManagerAdapter");
	jmethodID mId_CallBack = g_env->GetStaticMethodID(cls,"IPCManage_CallBack","(IIILjava/lang/Object;)V");
	jstring data = 0;
	if(0 != param2){
		data = (g_env)->NewStringUTF((char *)param2);
	}
	
	dbgprintf("IPCManager-----JNI------CallBack_ipcManager----22222");
	
	g_env->CallStaticVoidMethod(cls,mId_CallBack,event,msg,(jint)param1,data);
	
	dbgprintf("IPCManager-----JNI------CallBack_ipcManager----33333");
	
	// free
	g_env->DeleteLocalRef(cls);
}
