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
	jclass cls =g_env->FindClass("cn/com/mobnote/module/videosquare/VideoSquareManagerAdapter");
	jmethodID mId_CallBack = g_env->GetStaticMethodID(cls,"VideoSquare_CallBack","(IIJLjava/lang/Object;)V");
	
	if (0 == msg) {
		g_env->CallStaticVoidMethod(cls,mId_CallBack,event,msg, (jlong)param1, 0);
	} else {
		jstring data = 0;
		if(0 != param2){
			dbgprintf("xuhw-----JNI------CallBack_videosquare----22222222");
			data = charToJstringUTF(g_env, (char *)param2);
			dbgprintf("xuhw-----JNI------CallBack_videosquare----333333");
		}
		g_env->CallStaticVoidMethod(cls,mId_CallBack,event,msg,(jlong)param1,data);
		if (0 != data) {
			g_env->DeleteLocalRef(data);
		}
		dbgprintf("xuhw-----JNI------CallBack_videosquare----44444");
	}
	dbgprintf("xuhw-----JNI------CallBack_videosquare----55555");
	// free
	g_env->DeleteLocalRef(cls);
}
