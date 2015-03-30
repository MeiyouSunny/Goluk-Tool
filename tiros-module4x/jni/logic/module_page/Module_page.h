//
//  Module_ipcManager.h
//  Goluk通用逻辑模块声明头文件
//
//  Created by jiayf
//  Copyright (c) 2015年
//

#ifndef __MODULE_PAGE_DEF_H__
#define __MODULE_PAGE_DEF_H__

#include "../../../lib/api/system/debug.h"
#include "../../../lib/jnibase/jni_system.h"
#include "../../../lib/logic/Goluk_HttpPage_def.h"
#include <android/log.h>


void PageNotify_CallBack(void* pvUser, PageType type ,int success , unsigned long param1, unsigned long param2);


#endif /*__MODULE_PAGE_DEF_H__*/
