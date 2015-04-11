/*
 * InitJniFuntion.c
 *
 *  Created on: 2014-5-20
 *  
 */
#include "../../lib/jnibase/jni_system.h"
#include "../../lib/jnibase/jni_global.h"
#include "cn_com_tiros_api_Gps.h"
#include "cn_com_tiros_api_LLocation.h"
#include "cn_com_tiros_api_SensorDetector.h"
#include "cn_com_tiros_airtalkee_TaxiAirTalkee.h"
#include "cn_com_tiros_baidu_BaiduLocation.h"


static JNINativeMethod gps_gMethods[] = {
		{
				"sys_gpsChange",
				"(DDDD)V",
				(void *) Java_cn_com_tiros_api_Gps_sys_1gpsChange
		}
};

static JNINativeMethod llocation_gMethods[] = {
		{
				"sys_llocationNotify",
				"(DDD)V",
				(void *) Java_cn_com_tiros_api_LLocation_sys_1llocationNotify
		}
};

static JNINativeMethod sensor_gMethods[] = {
		{
				"SensorChanged",
				"(IIIFI)V",
				(void *) Java_cn_com_tiros_api_SensorDetector_SensorChanged
		}
};


static JNINativeMethod airtalkee_gMethods[] = {
		{
				"sys_AirTalkeeEvent",
				"(IILjava/lang/String;II)V",
				(void *) Java_cn_com_tiros_airtalkee_TaxiAirTalkee_sys_1AirTalkeeEvent
		}
};

static JNINativeMethod baiduLocation_gMethods[] = {
		{
				"sys_baiduLocationChange",
				"(IDDDDD)V",
				(void *) Java_cn_com_tiros_baidu_BaiduLocation_sys_1baiduLocationChange
		}
};

static jint register_cn_com_tiros_airtalkee(JNIEnv *env) {
	jclass cls = (*env)->FindClass(env, "cn/com/tiros/airtalkee/TaxiAirTalkee");
	jint result = (*env)->RegisterNatives(env, cls, airtalkee_gMethods, 1);
	(*env)->DeleteLocalRef(env, cls);
	return result;
}

static jint unregister_cn_com_tiros_airtalkee(JNIEnv * env) {
	jclass cls = (*env)->FindClass(env, "cn/com/tiros/airtalkee/TaxiAirTalkee");
	jint result = (*env)->UnregisterNatives(env, cls);
	(*env)->DeleteLocalRef(env, cls);
	return result;
}

static jint register_cn_com_tiros_baidu_BaiduLocation(JNIEnv *env) {
	jclass cls = (*env)->FindClass(env, "cn/com/tiros/baidu/BaiduLocation");
	jint result = (*env)->RegisterNatives(env, cls, baiduLocation_gMethods, 1);

	(*env)->DeleteLocalRef(env, cls);
	return result;
}



static jint unregister_cn_com_tiros_baidu_BaiduLocation(JNIEnv * env) {
	jclass cls = (*env)->FindClass(env, "cn/com/tiros/baidu/BaiduLocation");

	jint result = (*env)->UnregisterNatives(env, cls);
	(*env)->DeleteLocalRef(env, cls);
	return result;
}

static jint register_cn_com_tiros_api_Gps(JNIEnv *env) {
	jclass cls = (*env)->FindClass(env, "cn/com/tiros/api/Gps");
	jint result = (*env)->RegisterNatives(env, cls, gps_gMethods, 1);
	(*env)->DeleteLocalRef(env, cls);
	return result;
}

static jint unregister_cn_com_tiros_api_Gps(JNIEnv * env) {
	jclass cls = (*env)->FindClass(env, "cn/com/tiros/api/Gps");
	jint result = (*env)->UnregisterNatives(env, cls);
	(*env)->DeleteLocalRef(env, cls);
	return result;
}

static jint register_cn_com_tiros_api_llocation(JNIEnv *env) {
	jclass cls = (*env)->FindClass(env, "cn/com/tiros/api/LLocation");
	jint result = (*env)->RegisterNatives(env, cls, llocation_gMethods, 1);
	(*env)->DeleteLocalRef(env, cls);
	return result;
}

static jint unregister_cn_com_tiros_api_llocation(JNIEnv * env) {
	jclass cls = (*env)->FindClass(env, "cn/com/tiros/api/LLocation");
	jint result = (*env)->UnregisterNatives(env, cls);
	(*env)->DeleteLocalRef(env, cls);
	return result;
}

static jint register_cn_com_tiros_api_sensor(JNIEnv *env) {
	jclass cls = (*env)->FindClass(env, "cn/com/tiros/api/SensorDetector");
	jint result = (*env)->RegisterNatives(env, cls, sensor_gMethods, 1);
	(*env)->DeleteLocalRef(env, cls);
	return result;
}

static jint unregister_cn_com_tiros_api_sensor(JNIEnv * env) {
	jclass cls = (*env)->FindClass(env, "cn/com/tiros/api/SensorDetector");
	jint result = (*env)->UnregisterNatives(env, cls);
	(*env)->DeleteLocalRef(env, cls);
	return result;
}

jint JNI_Sub_Register(JNIEnv * g_env) {
	if (register_cn_com_tiros_api_Gps(g_env) < 0) {
		return -1;
	}

	if (register_cn_com_tiros_api_llocation(g_env) < 0) {
		return -1;
	}
	if (register_cn_com_tiros_api_sensor(g_env) < 0) {
		return -1;
	}

	if (register_cn_com_tiros_airtalkee(g_env) < 0) {
		return -1;
	}
	if (register_cn_com_tiros_api_Gps(g_env) < 0) {
		return -1;
	}
	if (register_cn_com_tiros_baidu_BaiduLocation(g_env) < 0) {
		return -1;
	}
	return 0;
}

void JNI_Sub_UnRegister(JNIEnv * g_env) {
	unregister_cn_com_tiros_api_Gps(g_env);
	unregister_cn_com_tiros_api_llocation(g_env);
	unregister_cn_com_tiros_api_sensor(g_env);
	unregister_cn_com_tiros_airtalkee(g_env);
	unregister_cn_com_tiros_baidu_BaiduLocation(g_env);

}

void InitRegisterFuntion(RegisterFuntionInfo registerFuntion){
	registerFuntion.subRegister = JNI_Sub_Register;
	registerFuntion.subUnRegister = JNI_Sub_UnRegister;
}


