LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -lbluetooth -llog
LOCAL_DEFAULT_CPP_EXTENSION := cpp

LOCAL_MODULE    := bluescan
LOCAL_SRC_FILES := bluescan.cpp rfcomm-client.cpp rfcomm-server.cpp

include $(BUILD_SHARED_LIBRARY)