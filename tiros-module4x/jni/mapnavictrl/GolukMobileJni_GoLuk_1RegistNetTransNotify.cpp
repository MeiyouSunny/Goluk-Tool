/*
 * MapNaviCtrlJni_MapNaviCtrl_1Destroy.cpp
 *
 *  Created on: 2012-2-13
 *      Author: shizy
 */
#include "cn_com_mobonote_golukmobile_comm_GolukMobileJni.h"
#include "../../lib/logic/Logic.h"
#include "../../lib/api/system/debug.h"
#include "../../lib/jnibase/jni_system.h"
#include "../../lib/api/system/debug.h"

void Net_Trans_NotifyCallBack (void* pvUser, Net_Trans_Event event ,int msg , unsigned long param1, unsigned long param2){
	JNIEnv * g_env;
	jvm->AttachCurrentThread(&g_env, 0);
	jclass cls = g_env->FindClass("cn/com/mobonote/golukmobile/comm/NetTransNotifyAdapter");
	jmethodID mId = g_env->GetStaticMethodID(cls, "netTransNotifyCallBack", "(IILjava/lang/Object;Ljava/lang/Object;)V");
	
	jclass cls_interger = g_env->FindClass("java/lang/Integer");
	jmethodID mId_de = g_env->GetMethodID(cls_interger,"<init>","(I)V");
	jobject obj1 = 0;
	jobject obj2 = 0;
	if (0 == event) {
		// param1 = param2 = int
		obj1 = g_env->NewObject(cls_interger,mId_de,param1);
		obj2 = g_env->NewObject(cls_interger,mId_de,param2);
		g_env->CallStaticVoidMethod(cls,mId,event,msg,obj1,obj2);	
	
	} else {
        if ( 0 == msg) {
            //空闲TransmissionStateMsg_Idle
            obj1 = g_env->NewObject(cls_interger,mId_de,param1);
            obj2 = g_env->NewObject(cls_interger,mId_de,param2);
            g_env->CallStaticVoidMethod(cls,mId,event,msg,obj1,obj2);
        }
		else if ( 1 == msg) {
            //文件列表校验消息TransmissionStateMsg_CheckList
            // param1 = int param2 == char
            dbgprintf("JNI__Net_Trans_NotifyCallBack__CheckList result:%s", (const char*)param2);
            obj1 = g_env->NewObject(cls_interger,mId_de,param1);
			jstring ms2 = 0;
			if(param2) {
                dbgprintf("JNI__Net_Trans_NotifyCallBack__CheckList result_____2");
				ms2 = g_env->NewStringUTF((const char *)param2);
			}
			g_env->CallStaticVoidMethod(cls,mId,event,msg,obj1,ms2);
			if(0 != ms2) {
				g_env->DeleteLocalRef(ms2);
			}
		}
        else if ( 2 == msg) {
            //文件传输消息TransmissionStateMsg_File
				// param1 = param2 = int
			obj1 = g_env->NewObject(cls_interger,mId_de,param1);
			jstring ms2 = 0;
            if(param2) {
                ms2 = g_env->NewStringUTF((const char *)param2);
            }
			g_env->CallStaticVoidMethod(cls,mId,event,msg,obj1,ms2);
            if(0 != ms2) {
                g_env->DeleteLocalRef(ms2);
            }				
		}
	}
	
	g_env->DeleteLocalRef(cls);
	g_env->DeleteLocalRef(cls_interger);
	if(0 != obj1) {
		g_env->DeleteLocalRef(obj1);
	}
	if (0 != obj2) {
		g_env->DeleteLocalRef(obj2);
	}
	
}

/*
 * Class:     cn_com_mobonote_golukmobile_comm_GolukMobileJni
 * Method:    GoLuk_RegistNetTransNotify
 * Signature: (Lcn/com/mobonote/golukmobile/comm/INetTransNotifyFn;)V
 */
JNIEXPORT void JNICALL Java_cn_com_mobonote_golukmobile_comm_GolukMobileJni_GoLuk_1RegistNetTransNotify
  (JNIEnv *env, jclass cls, jint plogic,jobject pvUser) {
	  
	LogicEngine *pL = (LogicEngine*)plogic;
	pL->RegistNetTransNotify((Net_Trans_Notify)Net_Trans_NotifyCallBack,0); 
	  
}
