LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog

LOCAL_SRC_FILES := \
	   uart_exchange_data.cpp \
	   native_serialport.cpp 

LOCAL_MODULE := libuart_exchange_data
LOCAL_SHARED_LIBRARIES := \
			     
include $(BUILD_SHARED_LIBRARY)
