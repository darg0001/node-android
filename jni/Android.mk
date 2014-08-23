# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

### prebuilt libuvpp.so
include $(CLEAR_VARS)
LOCAL_MODULE := libuvpp-prebuilt
LOCAL_SRC_FILES := libuvpp/libuvpp.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/libuvpp
include $(PREBUILT_SHARED_LIBRARY)

### prebuilt libjnidispatch.so
include $(CLEAR_VARS)
LOCAL_MODULE := jna-prebuilt
LOCAL_SRC_FILES := jna/libjnidispatch.so
include $(PREBUILT_SHARED_LIBRARY)

### libuvpp-jni.so
include $(CLEAR_VARS)
LOCAL_MODULE           := libuvpp-jni

LOCAL_SRC_FILES        := libuvpp.c

LOCAL_SHARED_LIBRARIES := libuvpp-prebuilt jna-prebuilt
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
