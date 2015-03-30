//
//  Module_ipcManager.h
//  Goluk通用逻辑模块声明头文件
//
//  Created by jiayf
//  Copyright (c) 2015年
//

#ifndef __MODULE_IPCMANAGER_DEF_H__
#define __MODULE_IPCMANAGER_DEF_H__

#include "../../../lib/api/system/debug.h"
#include "../../../lib/jnibase/jni_system.h"
#include "../../../lib/logic/Goluk_IPCManager_def.h"
#include <android/log.h>


void CallBack_ipcManager(void* pvUser, Net_Trans_Event event ,int msg , unsigned long param1, unsigned long param2);


#endif /*__MODULE_IPCMANAGER_DEF_H__*/
