//
//  Module_location.h
//  Goluk通用逻辑模块声明头文件
//
//  Created by jiayf
//  Copyright (c) 2015年4/14
//

#ifndef __MODULE_LOCATION_DEF_H__
#define __MODULE_LOCATION_DEF_H__

#include "../../../lib/api/system/debug.h"
#include "../../../lib/jnibase/jni_system.h"
#include "../../../lib/logic/Goluk_Location_def.h"
#include <android/log.h>


void LocationNotify_CallBack(const char *positionJson,Position position,void *pvuser);


#endif /*__MODULE_LOCATION_DEF_H__*/
