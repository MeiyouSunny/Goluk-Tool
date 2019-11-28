#ubuntu系统下tiros-module库makefile文件

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDFLAGS := $(LOCAL_PATH)/../lib/logic/lib/liblogic.a \
                 $(LOCAL_PATH)/../lib/ipcmanger/lib/liblogictachograph.a \
                 $(LOCAL_PATH)/../lib/api/md5/lib/libmd5.a \
                 $(LOCAL_PATH)/../lib/api/jansson/lib/libjansson.a \
                 $(LOCAL_PATH)/../lib/api/zlib/lib/libzlib.a \
                 $(LOCAL_PATH)/../lib/net-plugin/lib/libnet-plugin.a \
                 $(LOCAL_PATH)/../lib/api/system/lib/libsystem_api.a \
                 $(LOCAL_PATH)/../lib/api/universal/lib/libuniversal_api.a
LOCAL_MODULE    := golukmobile

LOCAL_CFLAGS =-D__LITTLE_ENDIAN -D_UNSUPPORT_STDARG -DTIROS_ANDROID_PLATFORM -mfloat-abi=softfp -mfpu=neon

MY_SRC_DIRS := $(LOCAL_PATH) \
		$(LOCAL_PATH)/../../../jnibase/src/base \

$(call __ndk_info, Start traversal source files!)
MY_SRC_C := $(foreach dir, $(MY_SRC_DIRS),\
  						$(shell find $(dir) -name *.c)\
						)

MY_SRC_CPP := $(foreach dir, $(MY_SRC_DIRS),\
  						$(shell find $(dir) -name *.cpp)\
						)

$(call __ndk_info, Traversal source files over!)

MY_SRCS := $(MY_SRC_C) $(MY_SRC_CPP)

$(call __ndk_info, Start replace $(LOCAL_PATH)/!)
LOCAL_SRC_FILES := $(foreach src, $(MY_SRCS),\
  	$(subst $(LOCAL_PATH)/,,$(src)) \
)
$(call __ndk_info, Replace $(LOCAL_PATH)/ over!)

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog -lz -lGLESv2 -ljnigraphics

include $(BUILD_SHARED_LIBRARY)
