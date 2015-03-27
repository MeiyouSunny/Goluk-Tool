/*
 * MapNaviCtrlJni_MapNaviCtrl_1Create.cpp
 *
 *  Created on: 2015-3-26
 *      Author: jiayf
 */
#include "Module_page.h"


void PageNotify_CallBack(void* pvUser, PageType type ,int success , unsigned long param1, unsigned long param2){
	
	dbgprintf("jyf-----JNI-----goluk-----PageNotify_CallBack--11: %d,%d",type, success);

	JNIEnv * g_env;
	jvm->AttachCurrentThread(&g_env, 0);
	jclass cls = g_env->FindClass("cn/com/mobnote/module/page/PageNotifyAdapter");
	jmethodID mId = g_env->GetStaticMethodID(cls, "pageNotifyCallBack", "(IILjava/lang/Object;Ljava/lang/Object;)V");
	//dbgprintf("AAAAAAAAAAAAAAA  AppMapNaviSecretaryNotify_CallBack event = %d, message = %s", (int)event, message);
	jclass cls_interger = g_env->FindClass("java/lang/Integer");
	jmethodID mId_de = g_env->GetMethodID(cls_interger,"<init>","(I)V");
	

	jobject obj1 = 0;
	obj1 = g_env->NewObject(cls_interger,mId_de,param1);
	if (success == 0)
	{
		jobject obj2 = g_env->NewObject(cls_interger,mId_de,param2);
		g_env->CallStaticVoidMethod(cls, mId, (jint)type, success, obj1,obj2);
		if(0 != obj2) 
		{
			g_env->DeleteLocalRef(obj2);
		}
	}
	else
	{
		jstring str = 0;
		if(param2) 
		{
			str = g_env->NewStringUTF((const char*)param2);
		}
		g_env->CallStaticVoidMethod(cls, mId, (jint)type, success, obj1,str);
		g_env->DeleteLocalRef(str);
	}
	if(0 != obj1) 
	{
		g_env->DeleteLocalRef(obj1);
	}
	// if (0 == type) 
	// {
	// 	jobject obj1 = 0;
	// 	obj1 = g_env->NewObject(cls_interger,mId_de,param1);
	// 	if (success == 0)
	// 	{
	// 		jobject obj2 = g_env->NewObject(cls_interger,mId_de,param2);
	// 		g_env->CallStaticVoidMethod(cls, mId, (jint)type, success, obj1,obj2);
	// 		if(0 != obj2) 
	// 		{
	// 			g_env->DeleteLocalRef(obj2);
	// 		}
	// 	}
	// 	else
	// 	{
	// 		jstring str = 0;
	// 		if(param2) 
	// 		{
	// 			str = g_env->NewStringUTF((const char*)param2);
	// 		}
	// 		g_env->CallStaticVoidMethod(cls, mId, (jint)type, success, obj1,str);
	// 		g_env->DeleteLocalRef(str);
	// 	}
	// 	if(0 != obj1) 
	// 	{
	// 		g_env->DeleteLocalRef(obj1);
	// 	}
	// } else if(1 == type) {
	// 	jobject obj2 = 0;
	// 	jstring str = 0;
	// 	if(param1) {
	// 		str = g_env->NewStringUTF((const char*)param1);
	// 	}
		
	// 	obj2 = g_env->NewObject(cls_interger,mId_de,param2);
		
	// 	g_env->CallStaticVoidMethod(cls, mId, (jint)type, success, str,obj2);
	// 	g_env->DeleteLocalRef(str);
		
		
	// } else if (2 == type) {
	// 	jstring str2 = 0;
	// 	jobject obj2 = 0;
	// 	if(param1) {
	// 		str2 = g_env->NewStringUTF((const char*)param1);
	// 	}
	// 	obj2 = g_env->NewObject(cls_interger,mId_de,param2);
		
	// 	g_env->CallStaticVoidMethod(cls, mId, (jint)type, success, str2,obj2);
	// 	g_env->DeleteLocalRef(str2);
	// }

    g_env->DeleteLocalRef(cls);  
}























