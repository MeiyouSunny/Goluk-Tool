/*
 * IPCManager_1RegisterNetTransNotify.cpp
 *
 *  Created on: 2015-2-11
 *  Author: jiayf
 */
 
#include "Module_videosquare.h"

void CallBack_videosquare(void* pvUser, int event, int msg, unsigned long param1, unsigned long param2){
	JNIEnv * g_env;
	jvm->AttachCurrentThread(&g_env,0);
	
	dbgprintf("xuhw-----JNI-----goluk-----CallBack_videosquare--11: %d,%d",event, msg);
	
	dbgprintf("xuhw-----JNI------CallBack_videosquare----11111");
	
	jclass cls =g_env->FindClass("cn/com/mobnote/module/videosquare/VideoSquareManagerAdapter");
	jmethodID mId_CallBack = g_env->GetStaticMethodID(cls,"VideoSquare_CallBack","(IIILjava/lang/Object;)V");
	jstring data = 0;
	if(0 != param2){
		data = (g_env)->NewStringUTF((char *)param2);
	}
	
	dbgprintf("xuhw------videosquare-----JNI------CallBack_ipcManager----22222==%s",(char *)param2);
	
	g_env->CallStaticVoidMethod(cls,mId_CallBack,event,msg,(jint)param1,data);
	
	dbgprintf("xuhw-----videosquare-----JNI------CallBack_ipcManager----33333");
	
	// free
	g_env->DeleteLocalRef(cls);
}
