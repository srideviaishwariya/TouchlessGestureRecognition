LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
include /Users/sridevi/OpenCV-2.4.4-android-sdk/sdk/native/jni/OpenCV.mk
LOCAL_LDLIBS +=  -llog -ldl
LOCAL_MODULE    := TouchlessGestureRecognition
LOCAL_SRC_FILES := hand.cpp
include $(BUILD_SHARED_LIBRARY)
