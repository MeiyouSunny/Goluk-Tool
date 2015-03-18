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

void PageNotify_CallBack(void* pvUser, PageType type ,int success , unsigned long param1, unsigned long param2){

	JNIEnv * g_env;
	jvm->AttachCurrentThread(&g_env, 0);
	jclass cls = g_env->FindClass("cn/com/mobonote/golukmobile/comm/PageNotifyAdapter");
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


/*
 * Class:     cn_com_mobonote_golukmobile_comm_GolukMobileJni
 * Method:    GoLuk_RegistPageNotify
 * Signature: (Lcn/com/mobonote/golukmobile/comm/IPageNotifyFn;)V
 */
JNIEXPORT void JNICALL Java_cn_com_mobonote_golukmobile_comm_GolukMobileJni_GoLuk_1RegistPageNotify
  (JNIEnv *env, jclass cls,jint plogic,jobject pvuser) {
	LogicEngine *pL = (LogicEngine*)plogic;
	pL->RegistPageNotify((Page_Notify)PageNotify_CallBack,0);    
}























