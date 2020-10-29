LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_CERTIFICATE := platform
LOCAL_PACKAGE_NAME := hybrid_gtm
LOCAL_SRC_FILES := $(call all-java-files-under, src)

include $(BUILD_PACKAGE)
