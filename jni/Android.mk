LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := br_com_bott_droidsshd_system_NativeTask.c 

#LOCAL_SHARED_LIBRARIES := libcutils
LOCAL_LDLIBS += -llog

LOCAL_C_INCLUDES += $(JNI_H_INCLUDE)

LOCAL_C_INCLUDES += /home/mestre/android/original_source/system/core/include

LOCAL_MODULE := libNativeTask

include $(BUILD_SHARED_LIBRARY)

