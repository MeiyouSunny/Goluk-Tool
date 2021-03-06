/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class cn_com_tiros_api_SensorDetector */

#ifndef _Included_cn_com_tiros_api_SensorDetector
#define _Included_cn_com_tiros_api_SensorDetector
#ifdef __cplusplus
extern "C" {
#endif
#undef cn_com_tiros_api_SensorDetector_ST_GYROSCOPE
#define cn_com_tiros_api_SensorDetector_ST_GYROSCOPE 1L
#undef cn_com_tiros_api_SensorDetector_ST_LIGHT
#define cn_com_tiros_api_SensorDetector_ST_LIGHT 2L
#undef cn_com_tiros_api_SensorDetector_ST_SHAKE
#define cn_com_tiros_api_SensorDetector_ST_SHAKE 3L
#undef cn_com_tiros_api_SensorDetector_LIGHT_BLOCKED
#define cn_com_tiros_api_SensorDetector_LIGHT_BLOCKED 1L
#undef cn_com_tiros_api_SensorDetector_LIGHT_RESTORE
#define cn_com_tiros_api_SensorDetector_LIGHT_RESTORE 2L
#undef cn_com_tiros_api_SensorDetector_UPDATE_INTERVAL
#define cn_com_tiros_api_SensorDetector_UPDATE_INTERVAL 100L
/*
 * Class:     cn_com_tiros_api_SensorDetector
 * Method:    SensorChanged
 * Signature: (IIIFI)V
 */
JNIEXPORT void JNICALL Java_cn_com_tiros_api_SensorDetector_SensorChanged
  (JNIEnv *, jclass, jint, jint, jint, jfloat, jint);

#ifdef __cplusplus
}
#endif
#endif
