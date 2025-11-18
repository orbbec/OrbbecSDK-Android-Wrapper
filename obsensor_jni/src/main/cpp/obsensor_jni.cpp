// License: Apache 2.0. See LICENSE file in root directory.
// Copyright(c) 2020  Orbbec Corporation. All Rights Reserved.

/**
 * \file obsensor_jni.cpp
 * \brief libobsensor jni
 * \author chaijingjing@orbbec.com
 */

#include "obsensor_jni.h"

#include <android/log.h>
#include <cinttypes>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <memory>
#include <mutex>
#include <string>
#include <unistd.h>

#include "jdatatype/DataBundle.h"
#include "jdatatype/DepthWorkMode.h"
#include "jdatatype/DeviceInfo.h"
#include "jdatatype/MultiDeviceSyncConfig.h"
#include "jdatatype/NetworkConfig.h"
#include "jdatatype/TimestampResetConfig.h"
#include "libobsensor/h/MultipleDevices.h"
#include "libobsensor/internal/Extension.h"
#include "utils/LocalUtils.h"

#define LOG_TAG "obsensor_jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

JavaVM *gJVM;
std::vector<std::pair<jlong, jobject>> gListCallback_;
std::mutex mutex_;

static inline std::string getStdString(JNIEnv *env, jstring jText,
                                       const char *functionName,
                                       const char *paramName) {
  if (!jText) {
    std::string strParamName = (paramName ? std::string(paramName) : "");
    std::string errMsg = "Invalid argument, " + strParamName + " is null";
    ob_throw_error(env, functionName, errMsg.c_str());
  }
  const char *szText = env->GetStringUTFChars(jText, JNI_FALSE);
  if (!szText) {
    std::string strParamName = (paramName ? std::string(paramName) : "");
    std::string errMsg =
        "Invalid argument, " + strParamName + " GetStringUTFChars return null";
    ob_throw_error(env, functionName, errMsg.c_str());
  }
  if (strlen(szText) <= 0) {
    env->ReleaseStringUTFChars(jText, szText);

    std::string strParamName = (paramName ? std::string(paramName) : "");
    std::string errMsg =
        "Invalid argument, " + strParamName + " string is empty";
    ob_throw_error(env, functionName, errMsg.c_str());
  }

  std::string strText(szText);
  env->ReleaseStringUTFChars(jText, szText);
  return strText;
}

int8_t ensure_utf8(const char *string) {
  if (!string)
    return 0;

  const auto *bytes = (const unsigned char *)string;
  while (*bytes) {
    if (( // ASCII
          // use bytes[0] <= 0x7F to allow ASCII control characters
            bytes[0] == 0x09 || bytes[0] == 0x0A || bytes[0] == 0x0D ||
            (0x20 <= bytes[0] && bytes[0] <= 0x7E))) {
      bytes += 1;
      continue;
    }

    if (( // non-overlong 2-byte
            (0xC2 <= bytes[0] && bytes[0] <= 0xDF) &&
            (0x80 <= bytes[1] && bytes[1] <= 0xBF))) {
      bytes += 2;
      continue;
    }

    if (( // excluding overlongs
            bytes[0] == 0xE0 && (0xA0 <= bytes[1] && bytes[1] <= 0xBF) &&
            (0x80 <= bytes[2] && bytes[2] <= 0xBF)) ||
        ( // straight 3-byte
            ((0xE1 <= bytes[0] && bytes[0] <= 0xEC) || bytes[0] == 0xEE ||
             bytes[0] == 0xEF) &&
            (0x80 <= bytes[1] && bytes[1] <= 0xBF) &&
            (0x80 <= bytes[2] && bytes[2] <= 0xBF)) ||
        ( // excluding surrogates
            bytes[0] == 0xED && (0x80 <= bytes[1] && bytes[1] <= 0x9F) &&
            (0x80 <= bytes[2] && bytes[2] <= 0xBF))) {
      bytes += 3;
      continue;
    }

    if (( // planes 1-3
            bytes[0] == 0xF0 && (0x90 <= bytes[1] && bytes[1] <= 0xBF) &&
            (0x80 <= bytes[2] && bytes[2] <= 0xBF) &&
            (0x80 <= bytes[3] && bytes[3] <= 0xBF)) ||
        ( // planes 4-15
            (0xF1 <= bytes[0] && bytes[0] <= 0xF3) &&
            (0x80 <= bytes[1] && bytes[1] <= 0xBF) &&
            (0x80 <= bytes[2] && bytes[2] <= 0xBF) &&
            (0x80 <= bytes[3] && bytes[3] <= 0xBF)) ||
        ( // plane 16
            bytes[0] == 0xF4 && (0x80 <= bytes[1] && bytes[1] <= 0x8F) &&
            (0x80 <= bytes[2] && bytes[2] <= 0xBF) &&
            (0x80 <= bytes[3] && bytes[3] <= 0xBF))) {
      bytes += 4;
      continue;
    }

    return 0;
  }

  return 1;
}

void n2jPropertyRangeB(JNIEnv *env, ob_bool_property_range nPropertyRange,
                       jobject jPropertyRangeB) {
  auto def = nPropertyRange.def;
  auto max = nPropertyRange.max;
  auto min = nPropertyRange.min;
  auto step = nPropertyRange.step;

  jclass propertyRange = env->GetObjectClass(jPropertyRangeB);
  if (!propertyRange) {
    LOGE("Not found class OptionRange");
    return;
  }

  jfieldID minField = env->GetFieldID(propertyRange, "min", "Z");
  jfieldID maxField = env->GetFieldID(propertyRange, "max", "Z");
  jfieldID stepField = env->GetFieldID(propertyRange, "step", "Z");
  jfieldID defField = env->GetFieldID(propertyRange, "def", "Z");

  env->SetBooleanField(jPropertyRangeB, minField, min);
  env->SetBooleanField(jPropertyRangeB, maxField, max);
  env->SetBooleanField(jPropertyRangeB, stepField, step);
  env->SetBooleanField(jPropertyRangeB, defField, def);
}

void n2jPropertyRangeI(JNIEnv *env, ob_int_property_range nPropertyRange,
                       jobject jPropertyRangeI) {
  auto def = nPropertyRange.def;
  auto max = nPropertyRange.max;
  auto min = nPropertyRange.min;
  auto step = nPropertyRange.step;

  jclass propertyRange = env->GetObjectClass(jPropertyRangeI);
  if (!propertyRange) {
    LOGE("Not found class OptionRange");
    return;
  }

  jfieldID minField = env->GetFieldID(propertyRange, "min", "I");
  jfieldID maxField = env->GetFieldID(propertyRange, "max", "I");
  jfieldID stepField = env->GetFieldID(propertyRange, "step", "I");
  jfieldID defField = env->GetFieldID(propertyRange, "def", "I");

  env->SetIntField(jPropertyRangeI, minField, min);
  env->SetIntField(jPropertyRangeI, maxField, max);
  env->SetIntField(jPropertyRangeI, stepField, step);
  env->SetIntField(jPropertyRangeI, defField, def);
}

void n2jPropertyRangeF(JNIEnv *env, ob_float_property_range nPropertyRange,
                       jobject jPropertyRangeF) {
  auto def = nPropertyRange.def;
  auto max = nPropertyRange.max;
  auto min = nPropertyRange.min;
  auto step = nPropertyRange.step;

  jclass propertyRange = env->GetObjectClass(jPropertyRangeF);
  if (!propertyRange) {
    LOGE("Not found class OptionRange");
    return;
  }

  jfieldID minField = env->GetFieldID(propertyRange, "min", "F");
  jfieldID maxField = env->GetFieldID(propertyRange, "max", "F");
  jfieldID stepField = env->GetFieldID(propertyRange, "step", "F");
  jfieldID defField = env->GetFieldID(propertyRange, "def", "F");

  env->SetFloatField(jPropertyRangeF, minField, min);
  env->SetFloatField(jPropertyRangeF, maxField, max);
  env->SetFloatField(jPropertyRangeF, stepField, step);
  env->SetFloatField(jPropertyRangeF, defField, def);
}

bool isInGListCallback(void *callback) {
  for (const auto &item : gListCallback_) {
    if (item.second == callback) {
      return true;
    }
  }
  return false;
}

void onPlaybackCallback(ob_frame *frame, void *pCallback) {
  ob_error *error = NULL;
  JNIEnv *env;
  int envStatus = gJVM->GetEnv((void **)&env, JNI_VERSION_1_6);
  bool needDetach = false;
  if (envStatus == JNI_EDETACHED) {
    if (gJVM->AttachCurrentThread(&env, NULL) != 0) {
      LOGE("onPlaybackCallback JNI error attach current thread!");
      ob_delete_frame(frame, &error);
      ob_handle_error(env, error);
      return;
    }
    needDetach = true;
  }

  if (!pCallback) {
    LOGW("onPlaybackCallback JNI callback is null...");
    ob_delete_frame(frame, &error);
    ob_handle_error(env, error);
    if (needDetach) {
      gJVM->DetachCurrentThread();
    }
    return;
  }

  if (!isInGListCallback(pCallback)) {
    LOGI("onPlaybackCallback Global ref pCallback had already been deleted !");
    ob_delete_frame(frame, &error);
    ob_handle_error(env, error);
    if (needDetach) {
      gJVM->DetachCurrentThread();
    }
    return;
  }

  auto jCallback = reinterpret_cast<jobject>(pCallback);
  jclass clsPlaybackCallback = env->GetObjectClass(jCallback);
  if (!clsPlaybackCallback) {
    LOGE("onPlaybackCallback not found");
    if (needDetach) {
      gJVM->DetachCurrentThread();
    }
    ob_delete_frame(frame, &error);
    ob_handle_error(env, error);
    return;
  }
  jmethodID methodCallback =
      env->GetMethodID(clsPlaybackCallback, "onPlayback", "(J)V");
  if (!methodCallback) {
    LOGE("onPlaybackCallback GetMethodID not found");
    if (needDetach) {
      gJVM->DetachCurrentThread();
    }
    ob_delete_frame(frame, &error);
    ob_handle_error(env, error);
    return;
  }

  env->CallVoidMethod(jCallback, methodCallback, (jlong)frame);
  if (needDetach) {
    gJVM->DetachCurrentThread();
  }
}

void onFrameCallback(ob_frame *frame, void *pCallback) {
  ob_error *error = NULL;
  JNIEnv *env;
  int envStatus = gJVM->GetEnv((void **)&env, JNI_VERSION_1_6);
  bool needDetach = false;
  if (envStatus == JNI_EDETACHED) {
    needDetach = true;
    if (gJVM->AttachCurrentThread(&env, NULL) != 0) {
      LOGE("JNI error attach current thread");
      ob_delete_frame(frame, &error);
      ob_handle_error(env, error);
      return;
    }
  }

  if (!pCallback) {
    LOGD("JNI callback is null...");
    ob_delete_frame(frame, &error);
    ob_handle_error(env, error);
    if (needDetach) {
      gJVM->DetachCurrentThread();
    }
    return;
  }

  if (!isInGListCallback(pCallback)) {
    LOGD("onFrameCallback Global ref pCallback had already been deleted !");
    ob_delete_frame(frame, &error);
    ob_handle_error(env, error);
    if (needDetach) {
      gJVM->DetachCurrentThread();
    }
    return;
  }

  auto jCallback = reinterpret_cast<jobject>(pCallback);
  jclass clsFrameCallback = env->GetObjectClass(jCallback);
  if (!clsFrameCallback) {
    LOGE("onFrameCallback not found");
    if (needDetach) {
      gJVM->DetachCurrentThread();
    }
    LOGD("onFrameCallback DetachCurrentThread");
    ob_delete_frame(frame, &error);
    ob_handle_error(env, error);
    return;
  }

  jmethodID methodCallback =
      env->GetMethodID(clsFrameCallback, "onFrame", "(J)V");
  if (!methodCallback) {
    LOGE("onFrameCallback GetMethodID not found");
    if (needDetach) {
      gJVM->DetachCurrentThread();
    }
    LOGD("onFrameCallback DetachCurrentThread");
    ob_delete_frame(frame, &error);
    ob_handle_error(env, error);
    return;
  }

  env->CallVoidMethod(jCallback, methodCallback, (jlong)frame);
  if (needDetach) {
    gJVM->DetachCurrentThread();
  }
}

void onFrameSetCallback(ob_frame *frame, void *pCallback) {
  ob_error *error = NULL;
  JNIEnv *env;
  int envStatus = gJVM->GetEnv((void **)&env, JNI_VERSION_1_6);
  bool needDetach = false;
  if (envStatus == JNI_EDETACHED) {
    needDetach = true;
    if (gJVM->AttachCurrentThread(&env, NULL) != 0) {
      LOGE("JNI error attach current thread");
      ob_delete_frame(frame, &error);
      ob_handle_error(env, error);
      return;
    }
  }

  if (!pCallback) {
    LOGD("JNI callback is null...");
    ob_delete_frame(frame, &error);
    ob_handle_error(env, error);
    return;
  }

  if (!isInGListCallback(pCallback)) {
    LOGD("onFrameSetCallback Global ref pCallback had already been deleted !");
    ob_delete_frame(frame, &error);
    ob_handle_error(env, error);
    return;
  }

  auto jCallback = reinterpret_cast<jobject>(pCallback);
  jclass clsFrameCallback = env->GetObjectClass(jCallback);
  if (!clsFrameCallback) {
    LOGE("onFrameSetCallback not found");
    if (needDetach) {
      gJVM->DetachCurrentThread();
    }
    LOGD("onFrameSetCallback DetachCurrentThread");
    ob_delete_frame(frame, &error);
    ob_handle_error(env, error);
    return;
  }

  jmethodID methodCallback =
      env->GetMethodID(clsFrameCallback, "onFrameSet", "(J)V");
  if (!methodCallback) {
    LOGE("onFrameSetCallback GetMethodID not found");
    if (needDetach) {
      gJVM->DetachCurrentThread();
    }
    LOGD("onFrameSetCallback DetachCurrentThread");
    ob_delete_frame(frame, &error);
    ob_handle_error(env, error);
    return;
  }

  env->CallVoidMethod(jCallback, methodCallback, (jlong)frame);
  if (needDetach) {
    gJVM->DetachCurrentThread();
  }
}

void onFilterCallback(ob_frame *frame, void *pCallback) {
  ob_error *error = NULL;
  JNIEnv *env;
  int envStatus = gJVM->GetEnv((void **)&env, JNI_VERSION_1_6);
  bool needDetach = false;
  if (envStatus == JNI_EDETACHED) {
    needDetach = true;
    if (gJVM->AttachCurrentThread(&env, NULL) != 0) {
      LOGE("JNI error attach current thread");
      ob_delete_frame(frame, &error);
      ob_handle_error(env, error);
      return;
    }
  }

  if (!pCallback) {
    LOGD("JNI callback is null...");
    ob_delete_frame(frame, &error);
    ob_handle_error(env, error);
    return;
  }

  if (!isInGListCallback(pCallback)) {
    LOGD("onFilterCallback Global ref pCallback had already been deleted !");
    ob_delete_frame(frame, &error);
    ob_handle_error(env, error);
    return;
  }

  auto jCallback = reinterpret_cast<jobject>(pCallback);
  jclass clsFrameCallback = env->GetObjectClass(jCallback);
  if (!clsFrameCallback) {
    LOGE("onFilterCallback not found");
    if (needDetach) {
      gJVM->DetachCurrentThread();
    }
    LOGD("onFilterCallback DetachCurrentThread");
    ob_delete_frame(frame, &error);
    ob_handle_error(env, error);
    return;
  }

  jmethodID methodCallback =
      env->GetMethodID(clsFrameCallback, "onFrame", "(J)V");
  if (!methodCallback) {
    LOGE("onFilterCallback GetMethodID not found");
    if (needDetach) {
      gJVM->DetachCurrentThread();
    }
    LOGD("onFilterCallback DetachCurrentThread");
    ob_delete_frame(frame, &error);
    ob_handle_error(env, error);
    return;
  }

  env->CallVoidMethod(jCallback, methodCallback, (jlong)frame);
  if (needDetach) {
    gJVM->DetachCurrentThread();
  }
}

void onStateChangeCallback(OBDeviceState state, const char *message,
                           void *pCallback) {
  if (!pCallback) {
    LOGD("JNI callback is null...");
    return;
  }

  if (!isInGListCallback(pCallback)) {
    LOGD("onStateChangeCallback Global ref pCallback had already been deleted "
         "!");
    return;
  }

  JNIEnv *env;
  int envStatus = gJVM->GetEnv((void **)&env, JNI_VERSION_1_6);
  bool needDetach = false;
  if (envStatus == JNI_EDETACHED) {
    needDetach = true;
    if (gJVM->AttachCurrentThread(&env, NULL) != 0) {
      LOGE("JNI error attach current thread");
      return;
    }
  }

  auto jCallback = reinterpret_cast<jobject>(pCallback);
  jclass clsCallback = env->GetObjectClass(jCallback);
  if (!clsCallback) {
    LOGE("onStateCallback not found");
    if (needDetach) {
      gJVM->DetachCurrentThread();
    }
    LOGD("onStateCallback DetachCurrentThread");
    return;
  }

  jmethodID methodCallback =
      env->GetMethodID(clsCallback, "onStateChange", "(ILjava/lang/String;)V");
  if (!methodCallback) {
    LOGE("onStateChangeCallback GetMethodID not found");
    if (needDetach) {
      gJVM->DetachCurrentThread();
    }
    return;
  }

  jstring msg;
  uint8_t ret = ensure_utf8(message);
  if (ret) {
    msg = env->NewStringUTF(reinterpret_cast<const char *>(message));
  } else {
    msg = env->NewStringUTF("null");
  }

  env->CallVoidMethod(jCallback, methodCallback, state, msg);
  if (needDetach) {
    gJVM->DetachCurrentThread();
  }
}

void onFileSendCallback(OBFileTranState state, const char *message,
                        uint8_t percent, void *pCallback) {
  if (!pCallback) {
    LOGD("JNI onFileSendCallback pCallback is null...");
    return;
  }

  if (!isInGListCallback(pCallback)) {
    LOGD("onFileSendCallback Global ref pCallback had already been deleted !");
    return;
  }

  JNIEnv *env;
  bool isNeedDetach = false;
  int envStatus = gJVM->GetEnv((void **)&env, JNI_VERSION_1_6);
  if (envStatus == JNI_EDETACHED) {
    isNeedDetach = true;
    if (gJVM->AttachCurrentThread(&env, NULL) != 0) {
      LOGE("JNI error attach current thread");
      return;
    }
  }

  auto jCallback = reinterpret_cast<jobject>(pCallback);
  jclass clsCallback = env->GetObjectClass(jCallback);
  if (!clsCallback) {
    LOGE("onCallback not found");
    if (isNeedDetach) {
      gJVM->DetachCurrentThread();
    }
    LOGD("onCallback DetachCurrentThread");
    return;
  }

  jmethodID methodCallback =
      env->GetMethodID(clsCallback, "onCallback", "(SSLjava/lang/String;)V");
  if (!methodCallback) {
    LOGE("onCallback GetMethodID not found");
    if (isNeedDetach) {
      gJVM->DetachCurrentThread();
    }
    LOGD("onCallback DetachCurrentThread");
    return;
  }

  jstring msg;
  uint8_t ret = ensure_utf8(message);
  if (ret) {
    msg = env->NewStringUTF(message);
  } else {
    msg = env->NewStringUTF("null");
  }

  env->CallVoidMethod(jCallback, methodCallback, state, percent, msg);

  if (isNeedDetach) {
    gJVM->DetachCurrentThread();
  }
}

void onUpgradeCallback(OBUpgradeState state, const char *message,
                       uint8_t percent, void *pCallback) {
  if (!pCallback) {
    LOGD("JNI callback is null...");
    return;
  }

  if (!isInGListCallback(pCallback)) {
    LOGD("onUpgradeCallback Global ref pCallback had already been deleted !");
    return;
  }

  JNIEnv *env;
  bool isNeedDetach = false;
  int envStatus = gJVM->GetEnv((void **)&env, JNI_VERSION_1_6);
  if (envStatus == JNI_EDETACHED) {
    //        LOGE("JNI envStatus = JNI_EDETACHED");
    isNeedDetach = true;
    if (gJVM->AttachCurrentThread(&env, NULL) != 0) {
      LOGE("JNI error attach current thread");
      return;
    }
  }
  //    LOGD("onCallback AttachCurrentThread");
  auto jCallback = reinterpret_cast<jobject>(pCallback);
  jclass clsCallback = env->GetObjectClass(jCallback);
  if (!clsCallback) {
    LOGE("onCallback not found");
    if (isNeedDetach) {
      gJVM->DetachCurrentThread();
    }
    LOGD("onCallback DetachCurrentThread");
    return;
  }
  //    LOGD("onCallback GetObjectClass...");
  jmethodID methodCallback =
      env->GetMethodID(clsCallback, "onCallback", "(SSLjava/lang/String;)V");
  if (!methodCallback) {
    LOGE("onCallback GetMethodID not found");
    if (isNeedDetach) {
      gJVM->DetachCurrentThread();
    }
    LOGD("onCallback DetachCurrentThread");
    return;
  }
  //    LOGD("onCallback GetMethodID...");
  jstring msg;
  int ret = ensure_utf8(message);
  if (ret) {
    msg = env->NewStringUTF(message);
  } else {
    msg = env->NewStringUTF("null");
  }

  env->CallVoidMethod(jCallback, methodCallback, state, percent, msg);
  //    LOGD("onCallback CallVoidMethod...");
  if (isNeedDetach) {
    gJVM->DetachCurrentThread();
  }
}

void onDeviceChangedCallback(ob_device_list *removed, ob_device_list *added,
                             void *pCallback) {
  JNIEnv *env;
  ob_error *error = NULL;
  int envStatus = gJVM->GetEnv((void **)&env, JNI_VERSION_1_6);

  if (!pCallback) {
    LOGW("onDeviceChangedCallback JNI callback is null...");
    ob_delete_device_list(removed, &error);
    ob_delete_device_list(added, &error);
    ob_handle_error(env, error);
    return;
  }

  if (!isInGListCallback(pCallback)) {
    LOGD("onDeviceChangedCallback Global ref pCallback had already been "
         "deleted !");
    ob_delete_device_list(removed, &error);
    ob_delete_device_list(added, &error);
    ob_handle_error(env, error);
    return;
  }
  bool isNeedDetach = false;
  if (envStatus == JNI_EDETACHED) {
    //        LOGE("JNI envStatus = JNI_EDETACHED");
    isNeedDetach = true;
    if (gJVM->AttachCurrentThread(&env, NULL) != 0) {
      LOGE("JNI error attach current thread");
      ob_delete_device_list(removed, &error);
      ob_delete_device_list(added, &error);
      ob_handle_error(env, error);
      return;
    }
  }
  //    LOGD("onCallback AttachCurrentThread");
  auto jCallback = reinterpret_cast<jobject>(pCallback);
  jclass clsCallback = env->GetObjectClass(jCallback);
  if (!clsCallback) {
    LOGE("onCallback not found");
    if (isNeedDetach) {
      gJVM->DetachCurrentThread();
    }
    LOGD("onCallback DetachCurrentThread");
    ob_delete_device_list(removed, &error);
    ob_delete_device_list(added, &error);
    ob_handle_error(env, error);
    return;
  }
  //    LOGD("onCallback GetObjectClass...");
  jmethodID methodCallback1 =
      env->GetMethodID(clsCallback, "onDeviceAttach", "(J)V");
  jmethodID methodCallback2 =
      env->GetMethodID(clsCallback, "onDeviceDetach", "(J)V");
  if (!methodCallback1 || !methodCallback2) {
    LOGE("onCallback GetMethodID not found");
    if (isNeedDetach) {
      gJVM->DetachCurrentThread();
    }
    LOGD("onCallback DetachCurrentThread");
    ob_delete_device_list(removed, &error);
    ob_delete_device_list(added, &error);
    ob_handle_error(env, error);
    return;
  }
  if (ob_device_list_device_count(added, &error) > 0) {
    env->CallVoidMethod(jCallback, methodCallback1, (jlong)added);
  } else {
    ob_delete_device_list(added, &error);
  }
  ob_handle_error(env, error);
  if (ob_device_list_device_count(removed, &error) > 0) {
    env->CallVoidMethod(jCallback, methodCallback2, (jlong)removed);
  } else {
    ob_delete_device_list(removed, &error);
  }
  ob_handle_error(env, error);
  if (isNeedDetach) {
    gJVM->DetachCurrentThread();
  }
}

void onMediaStateCallback(ob_media_state state, void *pCallback) {
  JNIEnv *env;
  ob_error *error = NULL;
  int envStatus = gJVM->GetEnv((void **)&env, JNI_VERSION_1_6);

  if (!pCallback) {
    LOGW("onMediaStateCallback JNI callback is null...");
    return;
  }

  if (!isInGListCallback(pCallback)) {
    LOGD(
        "onMediaStateCallback Global ref pCallback had already been deleted !");
    return;
  }
  bool isNeedDetach = false;
  if (envStatus == JNI_EDETACHED) {
    isNeedDetach = true;
    if (gJVM->AttachCurrentThread(&env, NULL) != 0) {
      LOGE("JNI error attach current thread");
      return;
    }
  }
  auto jCallback = reinterpret_cast<jobject>(pCallback);
  jclass clsCallback = env->GetObjectClass(jCallback);
  if (!clsCallback) {
    LOGE("onCallback not found");
    if (isNeedDetach) {
      gJVM->DetachCurrentThread();
    }
    LOGD("onCallback DetachCurrentThread");
    return;
  }

  jmethodID methodCallback = env->GetMethodID(clsCallback, "onState", "(I)V");
  if (!methodCallback) {
    LOGE("onCallback GetMethodID not found");
    if (isNeedDetach) {
      gJVM->DetachCurrentThread();
    }
    LOGD("onCallback DetachCurrentThread");
    return;
  }
  env->CallVoidMethod(jCallback, methodCallback, state);
  if (isNeedDetach) {
    gJVM->DetachCurrentThread();
  }
}

/**
 * Context
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_OBContext_nCreate(JNIEnv *env, jclass typeOBContext) {
  LOGI("JNI OBContext create");
  ob_error *error = NULL;
  auto context = ob_create_context(&error);
  ob_handle_error(env, error);
  return (jlong)context;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_OBContext_nCreateWithConfig(JNIEnv *env,
                                                     jclass typeOBContext,
                                                     jstring configPath) {
  LOGI("JNI OBContext create with config");
  ob_error *error = NULL;
  std::string strConfigPath(getStdString(
      env, configPath, "OBContext#nCreateWithConfig", "configPath"));
  auto context = ob_create_context_with_config(strConfigPath.c_str(), &error);
  ob_handle_error(env, error);
  return (jlong)context;
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_OBContext_nDelete(
    JNIEnv *env, jclass typeOBContext, jlong handle) {
  LOGI("JNI OBContext delete");
  ob_error *error = NULL;
  auto context = reinterpret_cast<ob_context *>(handle);
  std::vector<std::pair<jlong, jobject>>::iterator callbackIt;
  std::lock_guard<std::mutex> lk(mutex_);
  for (callbackIt = gListCallback_.begin();
       callbackIt != gListCallback_.end();) {
    if (handle == callbackIt->first) {
      env->DeleteGlobalRef(callbackIt->second);
      callbackIt = gListCallback_.erase(callbackIt);
    } else {
      callbackIt++;
    }
  }
  ob_delete_context(context, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_OBContext_nGetStageVersion(JNIEnv *env,
                                                    jclass typeOBContext) {
  return env->NewStringUTF(ob_get_stage_version());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_OBContext_nGetVersionName(JNIEnv *env,
                                                   jclass typeOBContext) {
  std::string strVersion = std::to_string(ob_get_major_version()) + "." +
                           std::to_string(ob_get_minor_version()) + "." +
                           std::to_string(ob_get_patch_version());
  uint8_t ret = ensure_utf8(strVersion.c_str());
  if (ret) {
    return env->NewStringUTF(strVersion.c_str());
  }
  return env->NewStringUTF("null");
}

extern "C" JNIEXPORT int JNICALL
Java_com_orbbec_obsensor_OBContext_nGetVersionCode(JNIEnv *env,
                                                   jclass typeOBContext) {
  return ob_get_version();
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_OBContext_nQueryDevices(JNIEnv *env,
                                                 jclass typeOBContext,
                                                 jlong handle) {
  ob_error *error = NULL;
  auto context = reinterpret_cast<ob_context *>(handle);
  auto deviceInfoList = ob_query_device_list(context, &error);
  ob_handle_error(env, error);
  return (jlong)deviceInfoList;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_OBContext_nSetDeviceChangedCallback(
    JNIEnv *env, jclass typeOBContext, jlong handle, jobject callback) {
  ob_error *error = NULL;
  auto context = reinterpret_cast<ob_context *>(handle);
  void *cookie = nullptr;
  std::lock_guard<std::mutex> lk(mutex_);
  if (callback) {
    jobject gCallback = env->NewGlobalRef(callback);
    cookie = gCallback;
    gListCallback_.push_back(std::pair<jlong, jobject>(handle, gCallback));
  }
  ob_set_device_changed_callback(context, onDeviceChangedCallback, cookie,
                                 &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_OBContext_nEnableDeviceClockSync(
    JNIEnv *env, jclass typeOBContext, jlong handle, jlong repeatInterval) {
  ob_error *error = NULL;
  auto context = reinterpret_cast<ob_context *>(handle);
  ob_enable_device_clock_sync(context, repeatInterval, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_OBContext_nSetLoggerSeverity(JNIEnv *env,
                                                      jclass typeOBContext,
                                                      jint logSeverity) {
  ob_error *error = NULL;
  ob_set_logger_severity(static_cast<ob_log_severity>(logSeverity), &error);
  ob_handle_error(env, error);
}

/*
 * Class:     com_orbbec_obsensor_OBContext
 * Method:    nSetLoggerToFile
 * Signature: (ILjava/lang/String;)V
 */
extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_OBContext_nSetLoggerToFile__ILjava_lang_String_2(
    JNIEnv *env, jclass typeOBContext, jint logSeverity, jstring directory) {
  ob_error *error = NULL;
  std::string strFilePath(getStdString(
      env, directory, "nSetLoggerToFile__ILjava_lang_String_2", "directory"));
  ob_set_logger_to_file(static_cast<ob_log_severity>(logSeverity),
                        strFilePath.c_str(), &error);
  ob_handle_error(env, error);
}

/*
 * Class:     com_orbbec_obsensor_OBContext
 * Method:    nSetLoggerToFile
 * Signature: (ILjava/lang/String;JJ)V
 */
extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_OBContext_nSetLoggerToFile__ILjava_lang_String_2JJ(
    JNIEnv *env, jclass typeOBContext, jint logSeverity, jstring directory,
    jlong maxFileSize, jlong maxFileNum) {

  ob_error *error = NULL;
  std::string strFilePath(getStdString(
      env, directory, "nSetLoggerToFile__ILjava_lang_String_2JJ", "directory"));
  LOGI("SetLoggerToFile with file size. directory: %s, maxFileSize: %lld, "
       "maxFileNum: %lld",
       strFilePath.c_str(), maxFileSize, maxFileNum);
  ob_set_logger_to_rotating_file(static_cast<ob_log_severity>(logSeverity),
                                 strFilePath.c_str(), (uint32_t)maxFileSize,
                                 (uint32_t)maxFileNum, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_OBContext_nSetLoggerToConsole(JNIEnv *env,
                                                       jclass typeOBContext,
                                                       jint logSeverity) {
  ob_error *error = NULL;
  ob_set_logger_to_console(static_cast<ob_log_severity>(logSeverity), &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_OBContext_nLoadLicense(JNIEnv *env,
                                                jclass typeOBContext,
                                                jstring licenseFilePath,
                                                jstring key) {
  ob_error *error = NULL;
  std::string strFilePath(getStdString(
      env, licenseFilePath, "OBContext#nConfigLicensePath", "licenseFilePath"));
  if (key) {
    std::string strKey(
        getStdString(env, key, "OBContext#nConfigLicensePath", "key"));
    ob_load_license(strFilePath.c_str(), strKey.c_str(), &error);
  } else {
    ob_load_license(strFilePath.c_str(), nullptr, &error);
  }
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_OBContext_nCreateNetDevice(JNIEnv *env,
                                                    jclass typeOBContext,
                                                    jlong handle,
                                                    jstring address,
                                                    jint port) {
  ob_error *error = NULL;
  auto context = reinterpret_cast<ob_context *>(handle);
  std::string szAddress(
      getStdString(env, address, "OBContext#nCreateNetDevice", "address"));
  auto device = ob_create_net_device(context, szAddress.c_str(), port, &error);
  ob_handle_error(env, error);
  return reinterpret_cast<jlong>(device);
}

/*
 * Class:     com_orbbec_obsensor_OBContext
 * Method:    nEnableNetDeviceEnumeration
 * Signature: (JZ)V
 */
extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_OBContext_nEnableNetDeviceEnumeration(
    JNIEnv *env, jclass typeContext, jlong handle, jboolean enable) {
  ob_error *error = NULL;
  auto context = reinterpret_cast<ob_context *>(handle);
  ob_enable_net_device_enumeration(context, enable, &error);
  ob_handle_error(env, error);
}

/*
 * Class:     com_orbbec_obsensor_OBContext
 * Method:    nIsNetDeviceEnumerationEnable
 * Signature: (J)Z
 */
extern "C" JNIEXPORT jboolean JNICALL
Java_com_orbbec_obsensor_OBContext_nIsNetDeviceEnumerationEnable(
    JNIEnv *env, jclass typeContext, jlong handle) {
  ob_error *error = NULL;
  auto context = reinterpret_cast<ob_context *>(handle);
  bool ret = ob_context_is_net_device_enumeration_enable(context, &error);
  ob_handle_error(env, error);
  return ret;
}

/**
 * DeviceList
 */
extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_DeviceList_nGetDeviceCount(JNIEnv *env,
                                                    jclass typeDeviceList,
                                                    jlong handle) {
  ob_error *error = NULL;
  auto deviceInfoList = reinterpret_cast<ob_device_list *>(handle);
  auto count = ob_device_list_device_count(deviceInfoList, &error);
  ob_handle_error(env, error);
  return count;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_DeviceList_nGetDevice(JNIEnv *env,
                                               jclass typeDeviceList,
                                               jlong handle, jint index) {
  ob_error *error = NULL;
  auto deviceList = reinterpret_cast<ob_device_list *>(handle);
  auto device = ob_device_list_get_device(deviceList, index, &error);
  ob_handle_error(env, error);
  return (jlong)device;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_DeviceList_nGetDeviceBySerialNumber(
    JNIEnv *env, jclass typeDevicList, jlong handle, jstring serialNum) {
  ob_error *error = NULL;
  auto deviceList = reinterpret_cast<ob_device_list *>(handle);
  std::string strSerialNum(getStdString(
      env, serialNum, "DeviceList#nGetDeviceBySerialNumber", "serialNum"));
  auto device = ob_device_list_get_device_by_serial_number(
      deviceList, strSerialNum.c_str(), &error);
  ob_handle_error(env, error);
  return (jlong)device;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_DeviceList_nGetDeviceByUid(JNIEnv *env,
                                                    jclass typeDeviceList,
                                                    jlong handle, jstring uid) {
  ob_error *error = NULL;
  auto deviceList = reinterpret_cast<ob_device_list *>(handle);
  std::string strDeviceUid(
      getStdString(env, uid, "DeviceList#nGetDeviceByUid", "uid"));
  auto device = ob_device_list_get_device_by_uid(deviceList,
                                                 strDeviceUid.c_str(), &error);
  ob_handle_error(env, error);
  return (jlong)device;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_DeviceList_nGetName(JNIEnv *env, jclass typeDeviceList,
                                             jlong handle, jint index) {
  ob_error *error = NULL;
  auto deviceInfoList = reinterpret_cast<ob_device_list *>(handle);
  const char *name =
      ob_device_list_get_device_name(deviceInfoList, index, &error);
  ob_handle_error(env, error);
  uint8_t ret = ensure_utf8(name);
  if (ret) {
    return env->NewStringUTF(name);
  }
  return env->NewStringUTF("null");
}

extern "C" JNIEXPORT jint JNICALL Java_com_orbbec_obsensor_DeviceList_nGetPid(
    JNIEnv *env, jclass typeDeviceList, jlong handle, jint index) {
  ob_error *error = NULL;
  auto deviceInfoList = reinterpret_cast<ob_device_list *>(handle);
  auto pid = ob_device_list_get_device_pid(deviceInfoList, index, &error);
  ob_handle_error(env, error);
  return pid;
}

extern "C" JNIEXPORT jint JNICALL Java_com_orbbec_obsensor_DeviceList_nGetVid(
    JNIEnv *env, jclass typeDeviceList, jlong handle, jint index) {
  ob_error *error = NULL;
  auto deviceInfoList = reinterpret_cast<ob_device_list *>(handle);
  auto vid = ob_device_list_get_device_vid(deviceInfoList, index, &error);
  ob_handle_error(env, error);
  return vid;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_DeviceList_nGetUid(JNIEnv *env, jclass typeDeviceList,
                                            jlong handle, jint index) {
  ob_error *error = NULL;
  auto deviceInfoList = reinterpret_cast<ob_device_list *>(handle);
  const char *uid =
      ob_device_list_get_device_uid(deviceInfoList, index, &error);
  ob_handle_error(env, error);
  uint8_t ret = ensure_utf8(uid);
  if (ret) {
    return env->NewStringUTF(uid);
  }
  return env->NewStringUTF("null");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_DeviceList_nGetDeviceSerialNumber(
    JNIEnv *env, jclass typeDeviceList, jlong handle, jint index) {
  ob_error *error = NULL;
  auto deviceList = reinterpret_cast<ob_device_list *>(handle);
  const char *serialNum =
      ob_device_list_get_device_serial_number(deviceList, index, &error);
  ob_handle_error(env, error);
  uint8_t ret = ensure_utf8(serialNum);
  if (ret) {
    return env->NewStringUTF(serialNum);
  }
  return env->NewStringUTF("null");
}

/*
 * Class:     com_orbbec_obsensor_DeviceList
 * Method:    nGetConnectionType
 * Signature: (JI)Ljava/lang/String;
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_DeviceList_nGetConnectionType(JNIEnv *env,
                                                       jclass typeDevice,
                                                       jlong handle,
                                                       jint index) {
  ob_error *error = NULL;
  auto deviceInfoList = reinterpret_cast<ob_device_list *>(handle);
  const char *connectType =
      ob_device_list_get_device_connection_type(deviceInfoList, index, &error);
  ob_handle_error(env, error);
  auto ret = ensure_utf8(connectType);
  if (ret) {
    return env->NewStringUTF(connectType);
  }
  return env->NewStringUTF("null");
}

/*
 * Class:     com_orbbec_obsensor_DeviceList
 * Method:    nGetIpAddress
 * Signature: (JI)Ljava/lang/String;
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_DeviceList_nGetIpAddress(JNIEnv *env,
                                                  jclass typeDeviceList,
                                                  jlong handle, jint index) {
  ob_error *error = NULL;
  auto deviceInfoList = reinterpret_cast<ob_device_list *>(handle);
  const char *ipAddress =
      ob_device_list_get_device_ip_address(deviceInfoList, index, &error);
  ob_handle_error(env, error);
  auto ret = ensure_utf8(ipAddress);
  if (ret) {
    return env->NewStringUTF(ipAddress);
  }
  return nullptr;
}

/*
 * Class:     com_orbbec_obsensor_DeviceList
 * Method:    nGetExtensionInfo
 * Signature: (JI)Ljava/lang/String;
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_DeviceList_nGetExtensionInfo(JNIEnv *env,
                                                      jclass typeDeviceList,
                                                      jlong handle,
                                                      jint index) {
  ob_error *error = NULL;
  auto deviceInfoList = reinterpret_cast<ob_device_list *>(handle);
  const char *extensionInfo =
      ob_device_list_get_extension_info(deviceInfoList, index, &error);
  ob_handle_error(env, error);
  if (extensionInfo && strlen(extensionInfo) > 0) {
    return env->NewStringUTF(extensionInfo);
  }
  return env->NewStringUTF("");
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_DeviceList_nDelete(
    JNIEnv *env, jclass typeDeviceList, jlong handle) {
  ob_error *error = NULL;
  auto deviceInfoList = reinterpret_cast<ob_device_list *>(handle);
  ob_delete_device_list(deviceInfoList, &error);
  ob_handle_error(env, error);
}

/**
 * DeviceInfo
 */
extern "C" JNIEXPORT jobject JNICALL
Java_com_orbbec_obsensor_Device_nGetDeviceInfo(JNIEnv *env,
                                               jclass typeDeviceInfo,
                                               jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto deviceInfo = ob_device_get_device_info(device, &error);
  ob_handle_error(env, error);
  if (error) {
    LOGE("nGetDeviceInfo failed!");
    return NULL;
  }
  jobject jobjDeviceInfo = obandroid::convert_j_DeviceInfo(env, deviceInfo);
  ob_delete_device_info(deviceInfo, &error);
  return jobjDeviceInfo;
}

/**
 * Device
 */
extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Device_nDelete(
    JNIEnv *env, jclass typeDevice, jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  std::vector<std::pair<jlong, jobject>>::iterator callbackIt;
  std::lock_guard<std::mutex> lk(mutex_);
  for (callbackIt = gListCallback_.begin();
       callbackIt != gListCallback_.end();) {
    if (handle == callbackIt->first) {
      env->DeleteGlobalRef(callbackIt->second);
      callbackIt = gListCallback_.erase(callbackIt);
    } else {
      callbackIt++;
    }
  }
  ob_delete_device(device, &error);
  ob_handle_error(env, error);
}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nGetCurrentDepthWorkMode
 * Signature: (J)Lcom/orbbec/obsensor/DepthWorkMode;
 */
extern "C" JNIEXPORT jobject JNICALL
Java_com_orbbec_obsensor_Device_nGetCurrentDepthWorkMode(JNIEnv *env,
                                                         jclass typeDevice,
                                                         jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  ob_depth_work_mode depthWorkMode =
      ob_device_get_current_depth_work_mode(device, &error);
  ob_handle_error(env, error);
  if (error) {
    LOGE("nGetCurrentDepthWorkMode failed!");
    return NULL;
  }
  return obandroid::convert_j_DepthWorkMode(env, depthWorkMode);
}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nSwitchDepthWorkMode
 * Signature: (JLjava/lang/String;)V
 */
extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nSwitchDepthWorkMode(JNIEnv *env,
                                                     jclass typeDevice,
                                                     jlong handle,
                                                     jstring modeName) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  std::string strModeName(
      getStdString(env, modeName, "Device#nSwitchDepthWorkMode", "modeName"));
  ob_device_switch_depth_work_mode_by_name(device, strModeName.c_str(), &error);
  ob_handle_error(env, error);
}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nGetDepthWorkModeList
 * Signature: (J)Ljava/util/List;
 */
extern "C" JNIEXPORT jobject JNICALL
Java_com_orbbec_obsensor_Device_nGetDepthWorkModeList(JNIEnv *env,
                                                      jclass typeDevice,
                                                      jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto workModeList = ob_device_get_depth_work_mode_list(device, &error);
  ob_handle_error(env, error);

  jclass clsArrayList = env->FindClass("java/util/ArrayList");
  jmethodID methodListConstructor =
      env->GetMethodID(clsArrayList, "<init>", "()V");
  jmethodID methodListAdd =
      env->GetMethodID(clsArrayList, "add", "(Ljava/lang/Object;)Z");
  jobject modeArrayList = env->NewObject(clsArrayList, methodListConstructor);
  auto modeCount = ob_depth_work_mode_list_count(workModeList, &error);
  if (error) {
    ob_error *error_1 = NULL;
    ob_delete_depth_work_mode_list(workModeList, &error_1);
    ob_handle_error(env, error);
    return NULL;
  }

  for (int i = 0; i < modeCount; i++) {
    auto workMode = ob_depth_work_mode_list_get_item(workModeList, i, &error);
    ob_handle_error(env, error);
    if (error) {
      LOGE("nGetCurrentDepthWorkMode failed!");
      return NULL;
    }
    auto jWorkMode = obandroid::convert_j_DepthWorkMode(env, workMode);
    if (jWorkMode) {
      env->CallBooleanMethod(modeArrayList, methodListAdd, jWorkMode);
    }
  }
  ob_delete_depth_work_mode_list(workModeList, &error);
  ob_handle_error(env, error);

  return modeArrayList;
}

extern "C" JNIEXPORT jintArray JNICALL
Java_com_orbbec_obsensor_Device_nGetSupportDepthPrecisionLevelList(
    JNIEnv *env, jclass typeDevice, jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  if (!ob_device_is_property_supported(device,
                                       OB_STRUCT_DEPTH_PRECISION_SUPPORT_LIST,
                                       OB_PERMISSION_READ, &error)) {
    LOGI("Device not support property OB_STRUCT_DEPTH_PRECISION_SUPPORT_LIST");
    return NULL;
  }
  ob_handle_error(env, error);

  ob_data_bundle *dataBundle = ob_device_get_structured_data_ext(
      device, OB_STRUCT_DEPTH_PRECISION_SUPPORT_LIST, &error);
  ob_handle_error(env, error);
  if (dataBundle->data == NULL || dataBundle->dataSize <= 0) {
    LOGI("Device get property OB_STRUCT_DEPTH_PRECISION_SUPPORT_LIST return "
         "zero data");
    return NULL;
  }

  auto arraySize = dataBundle->dataSize / sizeof(uint16_t);
  int *corePrecisionArray = new int[arraySize];
  for (size_t i = 0; i < arraySize; i++) {
    uint16_t value = 0;
    memcpy(&value, (uint8_t *)dataBundle->data + i * 2, 2);
    *(corePrecisionArray + i) = value;
  }

  jintArray precisionArray = env->NewIntArray(arraySize);
  if (NULL == precisionArray) {
    delete[] corePrecisionArray;
    LOGI("Device Get support depth precision level list failed. newIntArray "
         "failed.");
    return NULL;
  }
  env->SetIntArrayRegion(precisionArray, 0, arraySize, corePrecisionArray);
  delete[] corePrecisionArray;
  corePrecisionArray = NULL;

  return precisionArray;
}

extern "C" JNIEXPORT jlongArray JNICALL
Java_com_orbbec_obsensor_Device_nQuerySensor(JNIEnv *env, jclass typeDevice,
                                             jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  uint32_t size = 0;
  auto sensors = ob_device_get_sensor_list(device, &error);
  size = ob_sensor_list_get_sensor_count(sensors, &error);
  ob_handle_error(env, error);
  std::vector<jlong> sensorList;
  for (int i = 0; i < size; i++) {
    ob_sensor *sensor = ob_sensor_list_get_sensor(sensors, i, &error);
    sensorList.push_back((jlong)sensor);
  }
  jlongArray jSensors = env->NewLongArray(size);
  env->SetLongArrayRegion(jSensors, 0, size, sensorList.data());
  ob_delete_sensor_list(sensors, &error);
  return jSensors;
}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nHasSensor
 * Signature: (JI)Z
 */
extern "C" JNIEXPORT jboolean JNICALL
Java_com_orbbec_obsensor_Device_nHasSensor(JNIEnv *env, jclass typeDevice,
                                           jlong handle, jint iSensorType) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  uint32_t size = 0;
  auto sensor_list = ob_device_get_sensor_list(device, &error);
  size = ob_sensor_list_get_sensor_count(sensor_list, &error);
  ob_handle_error(env, error);
  bool find = false;
  for (uint32_t i = 0; i < size; i++) {
    ob_sensor_type type =
        ob_sensor_list_get_sensor_type(sensor_list, i, &error);
    ob_handle_error(env, error);
    if ((int)type == iSensorType) {
      find = true;
      break;
    }
  }
  ob_delete_sensor_list(sensor_list, &error);
  return find;
}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nQuerySensorTypes
 * Signature: (J)[I
 */
extern "C" JNIEXPORT jintArray JNICALL
Java_com_orbbec_obsensor_Device_nQuerySensorTypes(JNIEnv *env,
                                                  jclass typeDevice,
                                                  jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  uint32_t size = 0;
  auto sensor_list = ob_device_get_sensor_list(device, &error);
  size = ob_sensor_list_get_sensor_count(sensor_list, &error);
  ob_handle_error(env, error);
  jintArray iarray = env->NewIntArray(size);
  std::shared_ptr<int> ibuf(new int[size]{}, std::default_delete<int[]>());
  for (uint32_t i = 0; i < size; i++) {
    ob_sensor_type type =
        ob_sensor_list_get_sensor_type(sensor_list, i, &error);
    ob_handle_error(env, error);
    *(ibuf.get() + i) = type;
  }
  if (size > 0) {
    env->SetIntArrayRegion(iarray, 0, size, ibuf.get());
  }
  ob_delete_sensor_list(sensor_list, &error);
  return iarray;
}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nGetSensor
 * Signature: (JI)J
 */
extern "C" JNIEXPORT jlong JNICALL Java_com_orbbec_obsensor_Device_nGetSensor(
    JNIEnv *env, jclass typeDevice, jlong handle, jint iSensorType) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  ob_sensor *sensor =
      ob_device_get_sensor(device, (ob_sensor_type)iSensorType, &error);
  ob_handle_error(env, error);
  return (jlong)sensor;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_orbbec_obsensor_Device_nIsPropertySupported(JNIEnv *env,
                                                     jclass typeDevice,
                                                     jlong handle,
                                                     jint propertyId,
                                                     jint permission) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto isSupported = ob_device_is_property_supported(
      device, static_cast<ob_property_id>(propertyId),
      static_cast<ob_permission_type>(permission), &error);
  ob_handle_error(env, error);
  return isSupported;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nGetPropertyRangeB(JNIEnv *env,
                                                   jclass typeDevice,
                                                   jlong handle,
                                                   jint propertyId,
                                                   jobject propertyRange) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto range = ob_device_get_bool_property_range(
      device, static_cast<ob_property_id>(propertyId), &error);
  ob_handle_error(env, error);
  if (error) {
    return;
  }
  n2jPropertyRangeB(env, range, propertyRange);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nGetPropertyRangeI(JNIEnv *env,
                                                   jclass typeDevice,
                                                   jlong handle,
                                                   jint propertyId,
                                                   jobject propertyRange) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto range = ob_device_get_int_property_range(
      device, static_cast<ob_property_id>(propertyId), &error);
  ob_handle_error(env, error);
  if (error) {
    return;
  }
  n2jPropertyRangeI(env, range, propertyRange);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nGetPropertyRangeF(JNIEnv *env,
                                                   jclass typeDevice,
                                                   jlong handle,
                                                   jint propertyId,
                                                   jobject propertyRange) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto range = ob_device_get_float_property_range(
      device, static_cast<ob_property_id>(propertyId), &error);
  ob_handle_error(env, error);
  if (error) {
    return;
  }
  n2jPropertyRangeF(env, range, propertyRange);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nSetPropertyValueB(JNIEnv *env,
                                                   jclass typeDevice,
                                                   jlong handle,
                                                   jint propertyId,
                                                   jboolean value) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  ob_device_set_bool_property(device, static_cast<ob_property_id>(propertyId),
                              value, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nSetPropertyValueI(
    JNIEnv *env, jclass typeDevice, jlong handle, jint propertyId, jint value) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  ob_device_set_int_property(device, static_cast<ob_property_id>(propertyId),
                             value, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nSetPropertyValueF(JNIEnv *env,
                                                   jclass typeDevice,
                                                   jlong handle,
                                                   jint propertyId,
                                                   jfloat value) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  ob_device_set_float_property(device, static_cast<ob_property_id>(propertyId),
                               value, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nSetPropertyValueDataType(JNIEnv *env,
                                                          jclass typeDevice,
                                                          jlong handle,
                                                          jint propertyId,
                                                          jbyteArray data) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  jbyte *bytes = env->GetByteArrayElements(data, JNI_FALSE);
  uint32_t size = env->GetArrayLength(data);
  ob_device_set_structured_data(device, static_cast<ob_property_id>(propertyId),
                                (const void *)bytes, size, &error);
  env->ReleaseByteArrayElements(data, bytes, 0);
  ob_handle_error(env, error);
}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nGetPropertyValueDataTypeExt
 * Signature: (JILcom/orbbec/obsensor/datatype/OBDataBundle;)V
 */
extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nSetPropertyValueDataTypeExt__JILcom_orbbec_obsensor_datatype_OBDataBundle_2(
    JNIEnv *env, jclass typeDevice, jlong handle, jint propertyId,
    jobject dataBundle) {
  // TODO lumiaozi
  //    bool isSuccess = false;
  //    ob_error *error = NULL;
  //    auto device = reinterpret_cast<ob_device *>(handle);
  //    ob_data_bundle *cdata_bundle = obandroid::convert_c_DataBundle(env,
  //    dataBundle); ob_device_set_structured_data_ext(device,
  //    (ob_property_id)propertyId, cdata_bundle,
  //                                      &func_callback, &error);
  //    if (!isSuccess) {
  //
  //    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_orbbec_obsensor_Device_nGetPropertyValueB(JNIEnv *env,
                                                   jclass typeDevice,
                                                   jlong handle,
                                                   jint propertyId) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto value = ob_device_get_bool_property(
      device, static_cast<ob_property_id>(propertyId), &error);
  ob_handle_error(env, error);
  return value;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_Device_nGetPropertyValueI(JNIEnv *env,
                                                   jclass typeDevice,
                                                   jlong handle,
                                                   jint propertyId) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto value = ob_device_get_int_property(
      device, static_cast<ob_property_id>(propertyId), &error);
  ob_handle_error(env, error);
  return value;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_orbbec_obsensor_Device_nGetPropertyValueF(JNIEnv *env,
                                                   jclass typeDevice,
                                                   jlong handle,
                                                   jint propertyId) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto value = ob_device_get_float_property(
      device, static_cast<ob_property_id>(propertyId), &error);
  ob_handle_error(env, error);
  return value;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nGetPropertyValueDataType(JNIEnv *env,
                                                          jclass typeDevice,
                                                          jlong handle,
                                                          jint propertyId,
                                                          jbyteArray data) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  jbyte *bytes = env->GetByteArrayElements(data, JNI_FALSE);
  uint32_t size = 0;
  ob_device_get_structured_data(device, static_cast<ob_property_id>(propertyId),
                                (void *)bytes, &size, &error);
  env->SetByteArrayRegion(data, 0, size, bytes);
  env->ReleaseByteArrayElements(data, bytes, 0);
  ob_handle_error(env, error);
}

//extern "C" JNIEXPORT jbyteArray JNICALL
//Java_com_orbbec_obsensor_Device_nGetPropertyItem(JNIEnv *env,
//                                                          jclass typeDevice,
//                                                          jlong handle,
//                                                          jint propertyId) {
//  ob_error *error = NULL;
//  auto device = reinterpret_cast<ob_device *>(handle);
//  jbyte *bytes = env->GetByteArrayElements(data, JNI_FALSE);
//  uint32_t size = 0;
//  ob_device_get_structured_data(device, static_cast<ob_property_id>(propertyId),
//                                (void *) bytes, &size, &error);
//  env->SetByteArrayRegion(data, 0, size, bytes);
//  env->ReleaseByteArrayElements(data, bytes, 0);
//  ob_handle_error(env, error);
//}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nGetPropertyValueDataTypeExt
 * Signature: (JI)Lcom/orbbec/obsensor/datatype/OBDataBundle;
 */
extern "C" JNIEXPORT jobject JNICALL
Java_com_orbbec_obsensor_Device_nGetPropertyValueDataTypeExt__JI(
    JNIEnv *env, jclass typeDevice, jlong handle, jint propertyId) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto cdata_bundle = ob_device_get_structured_data_ext(
      device, (ob_property_id)propertyId, &error);
  ob_handle_error(env, error);
  if (error) {
    LOGE("nGetPropertyValueDataTypeExt__JI failed!");
    return NULL;
  }
  if (cdata_bundle) {
    jobject ret = obandroid::convert_j_DataBundle(env, cdata_bundle);
    ob_delete_data_bundle(cdata_bundle, &error);
    ob_handle_error(env, error);
    return ret;
  }
  ob_throw_error(env, "nGetPropertyValueDataTypeExt",
                 "Invoke get_structured_data_ext return NULL");
  return NULL;
}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nUpgrade
 * Signature: (JLjava/lang/String;Lcom/orbbec/obsensor/UpgradeCallback;)V
 */
extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nUpgrade__JLjava_lang_String_2Lcom_orbbec_obsensor_UpgradeCallback_2(
    JNIEnv *env, jclass typeDevice, jlong handle, jstring fileName,
    jobject callback) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  std::string strFileName(
      getStdString(env, fileName, "Device#nUpgrade(fileName)", "fileName"));
  void *cookie = NULL;
  if (callback) {
    std::lock_guard<std::mutex> lk(mutex_);
    jobject gCallback = env->NewGlobalRef(callback);
    cookie = gCallback;
    gListCallback_.push_back(std::pair<jlong, jobject>(handle, gCallback));
  }
  ob_device_upgrade(device, strFileName.c_str(), onUpgradeCallback, false,
                    cookie, &error);
  if (cookie) {
    std::lock_guard<std::mutex> lk(mutex_);
    for (auto callbackIt = gListCallback_.begin();
         callbackIt != gListCallback_.end();) {
      if (handle == callbackIt->first && callbackIt->second == cookie) {
        env->DeleteGlobalRef(callbackIt->second);
        callbackIt = gListCallback_.erase(callbackIt);
      } else {
        callbackIt++;
      }
    }
  }
  ob_handle_error(env, error);
}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nUpgrade
 * Signature: (J[BLcom/orbbec/obsensor/UpgradeCallback;)V
 */
extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nUpgrade__JLjava_nio_ByteBuffer_2Lcom_orbbec_obsensor_UpgradeCallback_2(
    JNIEnv *env, jclass typeDevice, jlong handle, jobject byteBuffer,
    jobject callback) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);

  jlong capacity = env->GetDirectBufferCapacity(byteBuffer);
  if (capacity <= 0) {
    LOGW("nUpgrade(ByteBuffer) failed. capacity < size");
    ob_throw_error(env, "nUpgradeCallback(ByteBuffer)",
                   "upgrade failed. capacity < size");
  }

  auto address =
      reinterpret_cast<char *>(env->GetDirectBufferAddress(byteBuffer));
  if (nullptr == address) {
    LOGW("nUpgrade(ByteBuffer) failed. DirectBufferAddress is null");
    ob_throw_error(env, "nUpgradeCallback(ByteBuffer)",
                   "upgrade failed. DirectBufferAddress is null");
  }

  void *cookie = NULL;
  std::lock_guard<std::mutex> lk(mutex_);
  if (callback) {
    jobject gCallback = env->NewGlobalRef(callback);
    cookie = gCallback;
    gListCallback_.push_back(std::pair<jlong, jobject>(handle, gCallback));
  }
  ob_device_upgrade_from_data(device, address, capacity, onUpgradeCallback,
                              false, cookie, &error);
  if (cookie) {
    std::lock_guard<std::mutex> lk(mutex_);
    for (auto callbackIt = gListCallback_.begin();
         callbackIt != gListCallback_.end();) {
      if (handle == callbackIt->first && callbackIt->second == cookie) {
        env->DeleteGlobalRef(callbackIt->second);
        callbackIt = gListCallback_.erase(callbackIt);
      } else {
        callbackIt++;
      }
    }
  }
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nSendFileToDestination(
    JNIEnv *env, jclass typeDevice, jlong handle, jstring filePath,
    jstring dstPath, jobject callback) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  std::string strSrcFile(
      getStdString(env, filePath, "Device#nSendFileToDestination", "filePath"));
  std::string strDstFile(
      getStdString(env, dstPath, "Device#nSendFileToDestination", "dstPath"));
  void *cookie = NULL;
  std::lock_guard<std::mutex> lk(mutex_);
  if (callback) {
    jobject gCallback = env->NewGlobalRef(callback);
    cookie = gCallback;
    gListCallback_.push_back(std::pair<jlong, jobject>(handle, gCallback));
  }
  ob_device_send_file_to_destination(device, strSrcFile.c_str(),
                                     strDstFile.c_str(), onFileSendCallback,
                                     false, cookie, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_orbbec_obsensor_Device_nActivateAuthorization(JNIEnv *env,
                                                       jclass typeDevice,
                                                       jlong handle,
                                                       jstring authCode) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  std::string strAuthCode(
      getStdString(env, authCode, "Device#nActivateAuthorization", "authCode"));
  auto activate =
      ob_device_activate_authorization(device, strAuthCode.c_str(), &error);
  ob_handle_error(env, error);
  return activate;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nSetStateChangeListener(JNIEnv *env,
                                                        jclass typeDevice,
                                                        jlong handle,
                                                        jobject callback) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  void *cookie = NULL;
  std::lock_guard<std::mutex> lk(mutex_);
  if (callback) {
    jobject gCallback = env->NewGlobalRef(callback);
    cookie = gCallback;
    gListCallback_.push_back(std::pair<jlong, jobject>(handle, gCallback));
  }
  ob_device_state_changed(device, onStateChangeCallback, cookie, &error);
  ob_handle_error(env, error);
}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nTriggerCapture
 * Signature: (J)V
 */
extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nTriggerCapture(JNIEnv *env, jclass typeDevice,
                                                jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  ob_device_trigger_capture(device, &error);
  ob_handle_error(env, error);
}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nSetTimestampResetConfig
 * Signature: (JLcom/orbbec/obsensor/TimestampResetConfig;)V
 */
extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nSetTimestampResetConfig(JNIEnv *env,
                                                         jclass typeDevice,
                                                         jlong handle,
                                                         jobject jconfig) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto config = obandroid::convert_c_TimestampResetConfig(env, jconfig);
  ob_device_set_timestamp_reset_config(device, &config, &error);
  ob_handle_error(env, error);
}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nGetTimestampResetConfig
 * Signature: (J)Lcom/orbbec/obsensor/TimestampResetConfig;
 */
extern "C" JNIEXPORT jobject JNICALL
Java_com_orbbec_obsensor_Device_nGetTimestampResetConfig(JNIEnv *env,
                                                         jclass typeDevice,
                                                         jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto config = ob_device_get_timestamp_reset_config(device, &error);
  ob_handle_error(env, error);
  if (error) {
    LOGE("nGetTimestampResetConfig failed!");
    return NULL;
  }
  auto jobjConfig = obandroid::convert_j_TimestampResetConfig(env, config);
  return jobjConfig;
}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nTimestampReset
 * Signature: (J)V
 */
extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nTimestampReset(JNIEnv *env, jclass typeDevice,
                                                jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  ob_device_timestamp_reset(device, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nTimerSyncWithHost(JNIEnv *env,
                                                   jclass typeDevice,
                                                   jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  ob_device_timer_sync_with_host(device, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Device_nGetCalibrationCameraParamList(
    JNIEnv *env, jclass typeDevice, jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto cameraParamList =
      ob_device_get_calibration_camera_param_list(device, &error);
  ob_handle_error(env, error);
  return (jlong)cameraParamList;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_orbbec_obsensor_Device_nGetMultiDeviceSyncConfig(JNIEnv *env,
                                                          jclass typeDevice,
                                                          jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  OBMultiDeviceSyncConfig syncConfig =
      ob_device_get_multi_device_sync_config(device, &error);
  ob_handle_error(env, error);
  if (error) {
    LOGE("nGetMultiDeviceSyncConfig failed!");
    return NULL;
  }
  return obandroid::convert_j_MultiDeviceSyncConfig(env, syncConfig);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nSetMultiDeviceSyncConfig(
    JNIEnv *env, jclass typeDevice, jlong handle, jobject jdeviceSyncConfig) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  OBMultiDeviceSyncConfig syncConfig =
      obandroid::convert_c_MultiDeviceSyncConfig(env, jdeviceSyncConfig);
  ob_device_set_multi_device_sync_config(device, &syncConfig, &error);
  ob_handle_error(env, error);
}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nGetNetworkConfig
 * Signature: (J)Lcom/orbbec/obsensor/OBNetworkConfig;
 */
extern "C" JNIEXPORT jobject JNICALL
Java_com_orbbec_obsensor_Device_nGetNetworkConfig(JNIEnv *env,
                                                  jclass typeDevice,
                                                  jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  ob_net_ip_config config;
  uint32_t dataSize = sizeof(config);
  ob_device_get_structured_data(device, OB_STRUCT_DEVICE_IP_ADDR_CONFIG,
                                &config, &dataSize, &error);
  ob_handle_error(env, error);
  if (error) {
    LOGE("nGetNetworkConfig failed!");
    return NULL;
  }
  return obandroid::convert_j_NetworkConfig(env, config);
}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nSetNetworkConfig
 * Signature: (JLcom/orbbec/obsensor/OBNetworkConfig;)V
 */
extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nSetNetworkConfig(JNIEnv *env,
                                                  jclass typeDevice,
                                                  jlong handle,
                                                  jobject jobjConfig) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  ob_net_ip_config config = obandroid::convert_c_NetworkConfig(env, jobjConfig);
  ob_device_set_structured_data(device, OB_STRUCT_DEVICE_IP_ADDR_CONFIG,
                                &config, sizeof(config), &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_orbbec_obsensor_Device_nIsGlobalTimestampSupported(JNIEnv *env, jclass clazz,
                                                            jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  bool result = ob_device_is_global_timestamp_supported(device, &error);
  ob_handle_error(env, error);
  return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nResetDefaultDepthFilterConfig(JNIEnv *env, jclass clazz,
                                                               jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  ob_device_reset_default_depth_filter_config(device, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_Device_nGetCurrentPresetName(JNIEnv *env, jclass clazz,
                                                      jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  const char *name = ob_device_get_current_preset_name(device, &error);
  ob_handle_error(env, error);
  uint8_t ret = ensure_utf8(name);
  if (ret) {
    return env->NewStringUTF(name);
  }
  return env->NewStringUTF("null");
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nLoadPreset(JNIEnv *env, jclass clazz,
                                            jlong handle,
                                            jstring presetName) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  const char *name = env->GetStringUTFChars(presetName, JNI_FALSE);
  ob_device_load_preset(device, name, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nLoadPresetFromJsonFile(JNIEnv *env, jclass clazz,
                                                        jlong handle,
                                                        jstring jsonFilePath) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  const char *path = env->GetStringUTFChars(jsonFilePath, JNI_FALSE);
  ob_device_load_preset_from_json_file(device, path, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nExportCurrentSettingsAsPresetJsonFile(JNIEnv *env, jclass clazz,
                                                                       jlong handle,
                                                                       jstring jsonFilePath) {
    ob_error *error = NULL;
    auto device = reinterpret_cast<ob_device *>(handle);
    const char *path = env->GetStringUTFChars(jsonFilePath, JNI_FALSE);
    ob_device_export_current_settings_as_preset_json_file(device, path, &error);
    ob_handle_error(env, error);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Device_nGetAvailablePresetList(JNIEnv *env, jclass clazz,
                                                        jlong handle) {
    ob_error *error = NULL;
    auto device = reinterpret_cast<ob_device *>(handle);
    ob_device_preset_list *list = ob_device_get_available_preset_list(device, &error);
    ob_handle_error(env, error);

    return (jlong) list;
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Device_nReboot(
    JNIEnv *env, jclass typeDevice, jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  ob_device_reboot(device, &error);
  ob_handle_error(env, error);
}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nRebootDelayMode
 * Signature: (JI)V
 */
extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nRebootDelayMode(JNIEnv *env, jclass typeDevice,
                                                 jlong handle,
                                                 jint delayTimeMs) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  ob_device_reboot_delay_mode(device, static_cast<uint32_t>(delayTimeMs),
                              &error);
  ob_handle_error(env, error);
}

/**
 * Sensor
 */
extern "C" JNIEXPORT jint JNICALL Java_com_orbbec_obsensor_Sensor_nGetType(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_sensor *sensor = reinterpret_cast<ob_sensor *>(handle);
  auto type = ob_sensor_get_type(sensor, &error);
  ob_handle_error(env, error);
  return (jint)type;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Sensor_nGetStreamProfileList(JNIEnv *env,
                                                      jclass instance,
                                                      jlong handle) {
  ob_error *error = NULL;
  ob_sensor *sensor = reinterpret_cast<ob_sensor *>(handle);
  std::vector<jlong> streamProfileList;
  auto streamProfiles = ob_sensor_get_stream_profile_list(sensor, &error);
  ob_handle_error(env, error);
  return (jlong)streamProfiles;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Sensor_nGetRecommendedFilterList(JNIEnv *env,
                                                          jclass clazz,
                                                          jlong handle) {
  ob_error *error = NULL;
  ob_sensor *sensor = reinterpret_cast<ob_sensor *>(handle);
  auto recommendedFilterLists = ob_sensor_get_recommended_filter_list(sensor, &error);
  ob_handle_error(env, error);
  return (jlong) recommendedFilterLists;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Sensor_nSwitchProfile(JNIEnv *env, jclass instance,
                                               jlong handle,
                                               jlong streamProfileHandle) {
  ob_error *error = NULL;
  ob_sensor *sensor = reinterpret_cast<ob_sensor *>(handle);
  ob_stream_profile *profile =
      reinterpret_cast<ob_stream_profile *>(streamProfileHandle);
  ob_sensor_switch_profile(sensor, profile, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Sensor_nStart(
    JNIEnv *env, jclass instance, jlong handle, jlong streamProfileHandle,
    jobject callback) {
  LOGI("Sensor start");
  ob_error *error = NULL;
  ob_sensor *sensor = reinterpret_cast<ob_sensor *>(handle);
  void *cookie = NULL;
  std::lock_guard<std::mutex> lk(mutex_);
  if (callback) {
    jobject gCallback = env->NewGlobalRef(callback);
    cookie = gCallback;
    gListCallback_.push_back(std::pair<jlong, jobject>(handle, gCallback));
  }
  auto type = ob_sensor_get_type(sensor, &error);
  ob_stream_profile *streamProfile =
      reinterpret_cast<ob_stream_profile *>(streamProfileHandle);
  ob_sensor_start(sensor, streamProfile, onFrameCallback, cookie, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Sensor_nStop(
    JNIEnv *env, jclass instance, jlong handle) {
  LOGI("Sensor stop");
  ob_error *error = NULL;
  ob_sensor *sensor = reinterpret_cast<ob_sensor *>(handle);
  ob_sensor_stop(sensor, &error);

  std::vector<std::pair<jlong, jobject>>::iterator callbackIt;
  std::lock_guard<std::mutex> lk(mutex_);
  for (callbackIt = gListCallback_.begin();
       callbackIt != gListCallback_.end();) {
    if (handle == callbackIt->first) {
      env->DeleteGlobalRef(callbackIt->second);
      callbackIt = gListCallback_.erase(callbackIt);
    } else {
      callbackIt++;
    }
  }
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Sensor_nDelete(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_sensor *sensor = reinterpret_cast<ob_sensor *>(handle);
  ob_delete_sensor(sensor, &error);
  ob_handle_error(env, error);
}

/**
 * StreamProfile
 */
extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_VideoStreamProfile_nGetFps(JNIEnv *env,
                                                    jclass instance,
                                                    jlong handle) {
  ob_error *error = NULL;
  ob_stream_profile *streamProfile =
      reinterpret_cast<ob_stream_profile *>(handle);
  auto fps = ob_video_stream_profile_fps(streamProfile, &error);
  ob_handle_error(env, error);
  return fps;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_VideoStreamProfile_nGetWidth(JNIEnv *env,
                                                      jclass instance,
                                                      jlong handle) {
  ob_error *error = NULL;
  ob_stream_profile *streamProfile =
      reinterpret_cast<ob_stream_profile *>(handle);
  auto width = ob_video_stream_profile_width(streamProfile, &error);
  ob_handle_error(env, error);
  return width;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_VideoStreamProfile_nGetHeight(JNIEnv *env,
                                                       jclass instance,
                                                       jlong handle) {
  ob_error *error = NULL;
  ob_stream_profile *streamProfile =
      reinterpret_cast<ob_stream_profile *>(handle);
  auto height = ob_video_stream_profile_height(streamProfile, &error);
  ob_handle_error(env, error);
  return height;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_VideoStreamProfile_nGetIntrinsic(JNIEnv *env,
                                                          jclass clazz,
                                                          jlong handle,
                                                          jbyteArray intrinsic) {
  ob_error *error = NULL;
  ob_stream_profile *streamProfile =
          reinterpret_cast<ob_stream_profile *>(handle);
  ob_camera_intrinsic intrinsic_param = ob_video_stream_get_intrinsic(streamProfile, &error);
  ob_handle_error(env, error);

  jsize arrayLength = env->GetArrayLength(intrinsic);
  if (arrayLength < sizeof(ob_camera_intrinsic)) {
    return;
  }

  env->SetByteArrayRegion(intrinsic, 0, sizeof(ob_camera_intrinsic),
                          reinterpret_cast<const jbyte*>(&intrinsic_param));
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_VideoStreamProfile_nGetCameraDistortion(JNIEnv *env,
                                                                 jclass clazz,
                                                                 jlong handle,
                                                                 jbyteArray distortion) {
  ob_error *error = NULL;
  ob_stream_profile *streamProfile =
          reinterpret_cast<ob_stream_profile *>(handle);
  ob_camera_distortion distortion_param = ob_video_stream_get_distortion(streamProfile, &error);
  ob_handle_error(env, error);

  jsize arrarayLength = env->GetArrayLength(distortion);
  if (arrarayLength < sizeof(ob_camera_distortion)) {
    return;
  }

  env->SetByteArrayRegion(distortion, 0, sizeof(ob_camera_distortion),
                          reinterpret_cast<const jbyte*>(&distortion_param));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_StreamProfile_nGetFormat(JNIEnv *env, jclass instance,
                                                  jlong handle) {
  ob_error *error = NULL;
  ob_stream_profile *streamProfile =
      reinterpret_cast<ob_stream_profile *>(handle);
  auto format = ob_stream_profile_format(streamProfile, &error);
  ob_handle_error(env, error);
  return (jint)format;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_StreamProfile_nGetType(JNIEnv *env, jclass instance,
                                                jlong handle) {
  ob_error *error = NULL;
  ob_stream_profile *streamProfile =
      reinterpret_cast<ob_stream_profile *>(handle);
  auto type = ob_stream_profile_type(streamProfile, &error);
  ob_handle_error(env, error);
  return (jint)type;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_StreamProfile_nDelete(JNIEnv *env, jclass instance,
                                               jlong handle) {
  ob_error *error = NULL;
  ob_stream_profile *streamProfile =
      reinterpret_cast<ob_stream_profile *>(handle);
  ob_delete_stream_profile(streamProfile, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_StreamProfile_nGetExtrinsicTo(JNIEnv *env,
                                                       jclass clazz,
                                                       jlong sourceHandle, jlong targetHandle,
                                                       jbyteArray extrinsic_ptr) {
  ob_error *error = NULL;
  ob_stream_profile *source = reinterpret_cast<ob_stream_profile *>(sourceHandle);
  ob_stream_profile *target = reinterpret_cast<ob_stream_profile *>(targetHandle);
  ob_extrinsic extrinsic = ob_stream_profile_get_extrinsic_to(source, target, &error);
  ob_handle_error(env, error);

  jsize arrayLength = env->GetArrayLength(extrinsic_ptr);
  if (arrayLength < sizeof(ob_extrinsic)) {
    return;
  }

  env->SetByteArrayRegion(extrinsic_ptr, 0, sizeof(ob_extrinsic), reinterpret_cast<const jbyte*>(&extrinsic));
}

/**
 * StreamProfileList
 */
extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_StreamProfileList_nGetStreamProfileCount(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_stream_profile_list *streamProfileList =
      reinterpret_cast<ob_stream_profile_list *>(handle);
  auto count = ob_stream_profile_list_count(streamProfileList, &error);
  ob_handle_error(env, error);
  return count;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_StreamProfileList_nGetStreamProfile(JNIEnv *env,
                                                             jclass instance,
                                                             jlong handle,
                                                             jint index) {
  ob_error *error = NULL;
  ob_stream_profile_list *streamProfileList =
      reinterpret_cast<ob_stream_profile_list *>(handle);
  auto streamProfile =
      ob_stream_profile_list_get_profile(streamProfileList, index, &error);
  ob_handle_error(env, error);
  return (jlong)streamProfile;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_StreamProfileList_nGetVideoStreamProfile(
    JNIEnv *env, jclass instance, jlong handle, jint width, jint height,
    jint format, jint fps) {
  ob_error *error = NULL;
  ob_stream_profile_list *streamProfileList =
      reinterpret_cast<ob_stream_profile_list *>(handle);
  auto videoStreamProfile = ob_stream_profile_list_get_video_stream_profile(
      streamProfileList, width, height, static_cast<ob_format>(format), fps,
      &error);
  ob_handle_error(env, error);
  return (jlong)videoStreamProfile;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_StreamProfileList_nDelete(JNIEnv *env, jclass instance,
                                                   jlong handle) {
  ob_error *error = NULL;
  ob_stream_profile_list *streamProfileList =
      reinterpret_cast<ob_stream_profile_list *>(handle);
  ob_delete_stream_profile_list(streamProfileList, &error);
  ob_handle_error(env, error);
}

/**
 * PresetList
 */
extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_PresetList_nGetCount(JNIEnv *env, jobject thiz,
                                                    jlong handle) {
  ob_error *error = NULL;
  ob_device_preset_list *presetList =
          reinterpret_cast<ob_device_preset_list *>(handle);
  int count = ob_device_preset_list_count(presetList, &error);
  ob_handle_error(env, error);
  return count;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_PresetList_nGetName(JNIEnv *env, jobject thiz,
                                                   jlong handle,
                                                   jint index) {
  ob_error *error = NULL;
  ob_device_preset_list *presetList =
          reinterpret_cast<ob_device_preset_list *>(handle);
  const char *name = ob_device_preset_list_get_name(presetList, index, &error);
  ob_handle_error(env, error);
  uint8_t ret = ensure_utf8(name);
  if (ret) {
    return env->NewStringUTF(name);
  }
  return env->NewStringUTF("null");
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_orbbec_obsensor_PresetList_nHasPreset(JNIEnv *env, jobject thiz,
                                               jlong handle,
                                               jstring presetName) {
  ob_error *error = NULL;
  ob_device_preset_list *presetList =
          reinterpret_cast<ob_device_preset_list *>(handle);
  const char *name = env->GetStringUTFChars(presetName, JNI_FALSE);
  bool result = ob_device_preset_list_has_preset(presetList, name, &error);
  ob_handle_error(env, error);
  return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_PresetList_nDelete(JNIEnv *env, jobject thiz,
                                            jlong handle) {
  ob_error *error = NULL;
  ob_device_preset_list *presetList =
          reinterpret_cast<ob_device_preset_list *>(handle);
  ob_delete_preset_list(presetList, &error);
  ob_handle_error(env, error);
}

/**
 * Frame
 */
extern "C" JNIEXPORT jlong JNICALL Java_com_orbbec_obsensor_Frame_nGetIndex(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto index = ob_frame_index(frame, &error);
  ob_handle_error(env, error);
  return index;
}

extern "C" JNIEXPORT jint JNICALL Java_com_orbbec_obsensor_VideoFrame_nGetWidth(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto width = ob_video_frame_width(frame, &error);
  ob_handle_error(env, error);
  return width;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_VideoFrame_nGetHeight(JNIEnv *env, jclass instance,
                                               jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto height = ob_video_frame_height(frame, &error);
  ob_handle_error(env, error);
  return height;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_VideoFrame_nGetPixelAvailableBitSize(JNIEnv *env,
                                                              jclass instance,
                                                              jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto size = ob_video_frame_pixel_available_bit_size(frame, &error);
  return size;
}

/*
 * Class:     com_orbbec_obsensor_Frame
 * Method:    nGetFormat
 * Signature: (J)I
 */
extern "C" JNIEXPORT jint JNICALL Java_com_orbbec_obsensor_Frame_nGetFormat(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto format = ob_frame_format(frame, &error);
  ob_handle_error(env, error);
  return (jint)format;
}

/*
 * Class:     com_orbbec_obsensor_Frame
 * Method:    nGetType
 * Signature: (J)I
 */
extern "C" JNIEXPORT jint JNICALL Java_com_orbbec_obsensor_Frame_nGetType(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto type = ob_frame_get_type(frame, &error);
  ob_handle_error(env, error);
  return (jint)type;
}

/*
 * Class:     com_orbbec_obsensor_Frame
 * Method:    nGetTimeStamp
 * Signature: (J)J
 */
extern "C" JNIEXPORT jlong JNICALL Java_com_orbbec_obsensor_Frame_nGetTimeStamp(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto timeStamp = ob_frame_time_stamp(frame, &error);
  ob_handle_error(env, error);
  return timeStamp;
}

/*
 * Class:     com_orbbec_obsensor_Frame
 * Method:    nGetTimeStampUs
 * Signature: (J)J
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_Frame_nGetTimeStampUs(JNIEnv *env, jclass instance,
                                               jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  uint64_t timeStampUs = ob_frame_time_stamp_us(frame, &error);
  ob_handle_error(env, error);

  char buf[64] = {0};
  std::snprintf(buf, sizeof(buf), "%llu", timeStampUs);
  return env->NewStringUTF(buf);
}

/*
 * Class:     com_orbbec_obsensor_Frame
 * Method:    nGetSystemTimeStamp
 * Signature: (J)J
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Frame_nGetSystemTimeStamp(JNIEnv *env, jclass instance,
                                                   jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto systemTimeStamp = ob_frame_system_time_stamp(frame, &error);
  ob_handle_error(env, error);
  return systemTimeStamp;
}

/*
 * Class:     com_orbbec_obsensor_Frame
 * Method:    nGetData
 * Signature: (JLjava/nio/ByteBuffer;)I
 */
extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_Frame_nGetData__JLjava_nio_ByteBuffer_2(
    JNIEnv *env, jobject instance, jlong handle, jobject byteBuffer) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto frameData = reinterpret_cast<uint8_t *>(ob_frame_data(frame, &error));
  ob_handle_error(env, error);

  auto size = ob_frame_data_size(frame, &error);
  ob_handle_error(env, error);

  jlong capacity = env->GetDirectBufferCapacity(byteBuffer);
  if (capacity < size) {
    LOGW("getData failed. capacity < size");
    return -1;
  }

  auto address =
      reinterpret_cast<uint8_t *>(env->GetDirectBufferAddress(byteBuffer));
  if (nullptr == address) {
    LOGW("getData failed. DirectBufferAddress is null");
    return -1;
  }
  memset(address, 0, capacity);
  memcpy(address, frameData, size);

  return (jint)size;
}

/*
 * Class:     com_orbbec_obsensor_Frame
 * Method:    nGetData
 * Signature: (J[B)I
 */
extern "C" JNIEXPORT jint JNICALL Java_com_orbbec_obsensor_Frame_nGetData__J_3B(
    JNIEnv *env, jclass instance, jlong handle, jbyteArray jBuf) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto size = ob_frame_data_size(frame, &error);
  auto data = ob_frame_data(frame, &error);
  jsize length = env->GetArrayLength(jBuf);
  if (length < size) {
    LOGE("nGetData failed. buf length < size");
    return -1;
  }

  env->SetByteArrayRegion(jBuf, 0, size, reinterpret_cast<const jbyte *>(data));
  ob_handle_error(env, error);
  return size;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_orbbec_obsensor_Frame_nGetDirectBuffer(JNIEnv *env, jclass instance,
                                                jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto size = ob_frame_data_size(frame, &error);
  auto data = ob_frame_data(frame, &error);
  ob_handle_error(env, error);
  return env->NewDirectByteBuffer(data, size);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Frame_nGetSystemTimeStampUs(JNIEnv *env, jclass clazz,
                                                     jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  uint64_t timeStampUs = ob_frame_system_time_stamp_us(frame, &error);
  ob_handle_error(env, error);

  return timeStampUs;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Frame_nGetGlobalTimeStampUs(JNIEnv *env, jclass clazz,
                                                     jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  uint64_t timeStampUs = ob_frame_global_time_stamp_us(frame, &error);
  ob_handle_error(env, error);

  return timeStampUs;
}

/*
 * Class:     com_orbbec_obsensor_Frame
 * Method:    nGetDataSize
 * Signature: (J)I
 */
extern "C" JNIEXPORT jint JNICALL Java_com_orbbec_obsensor_Frame_nGetDataSize(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto size = ob_frame_data_size(frame, &error);
  ob_handle_error(env, error);
  return size;
}

/*
 * Class:     com_orbbec_obsensor_Frame
 * Method:    nGetMetadata
 * Signature: (J[B)V
 */
extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Frame_nGetMetadata(
    JNIEnv *env, jclass instance, jlong handle, jbyteArray jData) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto metadata = ob_frame_metadata(frame, &error);
  jsize length = env->GetArrayLength(jData);
  env->SetByteArrayRegion(jData, 0, length,
                          reinterpret_cast<const jbyte *>(metadata));
  ob_handle_error(env, error);
}

/*
 * Class:     com_orbbec_obsensor_Frame
 * Method:    nGetMetadataSize
 * Signature: (J)I
 */
extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_Frame_nGetMetadataSize(JNIEnv *env, jclass instance,
                                                jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto size = ob_frame_metadata_size(frame, &error);
  ob_handle_error(env, error);
  return size;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_orbbec_obsensor_Frame_nHasMetadata(JNIEnv *env, jclass clazz, jlong handle,
                                            jint frameMetadataType) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  ob_frame_metadata_type type = static_cast<ob_frame_metadata_type>(frameMetadataType);
  bool hasMetaData = ob_frame_has_metadata(frame, type, &error);
  ob_handle_error(env, error);
  return hasMetaData;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Frame_nGetMetaValue(JNIEnv *env, jclass clazz, jlong handle,
                                             jint frameMetadataType) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  ob_frame_metadata_type type = static_cast<ob_frame_metadata_type>(frameMetadataType);
  int64_t value = ob_frame_get_metadata_value(frame, type, &error);
  ob_handle_error(env, error);
  return (jlong) value;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Frame_nGetStreamProfile(JNIEnv *env, jclass clazz,
                                                 jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  ob_stream_profile *profile = ob_frame_get_stream_profile(frame, &error);
  ob_handle_error(env, error);
  return (jlong) profile;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Frame_nGetSensor(JNIEnv *env, jclass clazz,
                                          jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  ob_sensor *sensor = ob_frame_get_sensor(frame, &error);
  ob_handle_error(env, error);
  return (jlong) sensor;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Frame_nGetDevice(JNIEnv *env, jclass clazz,
                                          jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  ob_device *device = ob_frame_get_device(frame, &error);
  ob_handle_error(env, error);
  return (jlong) device;
}

/*
 * Class:     com_orbbec_obsensor_Frame
 * Method:    nDelete
 * Signature: (J)V
 */
extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Frame_nDelete(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  ob_delete_frame(frame, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_PointFrame_nGetPointCloudData(JNIEnv *env,
                                                       jclass clazz,
                                                       jlong handle,
                                                       jfloatArray jData) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto data = ob_frame_data(frame, &error);
  jsize length = env->GetArrayLength(jData);
  env->SetFloatArrayRegion(jData, 0, length, static_cast<const jfloat *>(data));
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_orbbec_obsensor_DepthFrame_nGetValueScale(JNIEnv *env, jclass instance,
                                                   jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto scale = ob_depth_frame_get_value_scale(frame, &error);
  ob_handle_error(env, error);
  return scale;
}

/**
 * FrameSet
 */
extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_FrameSet_nGetFrameCount(JNIEnv *env, jclass instance,
                                                 jlong handle) {
  ob_error *error = NULL;
  ob_frame *frameSet = reinterpret_cast<ob_frame *>(handle);
  auto size = ob_frameset_frame_count(frameSet, &error);
  ob_handle_error(env, error);
  return size;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_FrameSet_nGetDepthFrame(JNIEnv *env, jclass instance,
                                                 jlong handle) {
  ob_error *error = NULL;
  ob_frame *frameSet = reinterpret_cast<ob_frame *>(handle);
  auto frame = ob_frameset_depth_frame(frameSet, &error);
  ob_handle_error(env, error);
  return (jlong)frame;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_FrameSet_nGetColorFrame(JNIEnv *env, jclass instance,
                                                 jlong handle) {
  ob_error *error = NULL;
  ob_frame *frameSet = reinterpret_cast<ob_frame *>(handle);
  auto frame = ob_frameset_color_frame(frameSet, &error);
  ob_handle_error(env, error);
  return (jlong)frame;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_FrameSet_nGetInfraredFrame(JNIEnv *env,
                                                    jclass instance,
                                                    jlong handle) {
  ob_error *error = NULL;
  ob_frame *frameSet = reinterpret_cast<ob_frame *>(handle);
  auto frame = ob_frameset_ir_frame(frameSet, &error);
  ob_handle_error(env, error);
  return (jlong)frame;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_FrameSet_nGetPointFrame(JNIEnv *env, jclass instance,
                                                 jlong handle) {
  ob_error *error = NULL;
  ob_frame *frameSet = reinterpret_cast<ob_frame *>(handle);
  auto frame = ob_frameset_points_frame(frameSet, &error);
  ob_handle_error(env, error);
  return (jlong)frame;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_FrameSet_nGetFrameByIndex(JNIEnv *env, jclass clazz,
                                                jlong handle,
                                                jint index) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  ob_frame *frameByIndex = ob_frameset_get_frame_by_index(frame, index, &error);
  ob_handle_error(env, error);
  return (jlong) frameByIndex;
}

/*
 * Class:     com_orbbec_obsensor_FrameSet
 * Method:    nGetFrame
 * Signature: (JI)J
 */
extern "C" JNIEXPORT jlong JNICALL Java_com_orbbec_obsensor_FrameSet_nGetFrame(
    JNIEnv *env, jclass clazz, jlong handle, jint frameType) {
  ob_error *error = NULL;
  ob_frame *frameSet = reinterpret_cast<ob_frame *>(handle);
  auto frame =
      ob_frameset_get_frame(frameSet, (ob_frame_type)frameType, &error);
  ob_handle_error(env, error);
  return (long)frame;
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_FrameSet_nDelete(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_frame *frameSet = reinterpret_cast<ob_frame *>(handle);
  ob_delete_frame(frameSet, &error);
  ob_handle_error(env, error);
}

/**
 * Config
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Config_nCreate(JNIEnv *env, jclass instance) {
  ob_error *error = NULL;
  ob_config *config = ob_create_config(&error);
  ob_handle_error(env, error);
  return (jlong)config;
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Config_nDelete(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_config *config = reinterpret_cast<ob_config *>(handle);
  ob_delete_config(config, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Config_nEnableStream(
    JNIEnv *env, jclass instance, jlong handle, jlong streamProfileHandle) {
  ob_error *error = NULL;
  ob_config *config = reinterpret_cast<ob_config *>(handle);
  ob_stream_profile *streamProfile =
      reinterpret_cast<ob_stream_profile *>(streamProfileHandle);
  ob_config_enable_stream(config, streamProfile, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Config_nEnableAllStream(JNIEnv *env, jclass instance,
                                                 jlong handle) {
  ob_error *error = NULL;
  auto *config = reinterpret_cast<ob_config *>(handle);
  ob_config_enable_all_stream(config, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Config_nEnableVideoStream(JNIEnv *env, jclass clazz,
                                                   jlong configHandle,
                                                   jint streamType,
                                                   jint width, jint height,
                                                   jint fps,
                                                   jint format) {
  ob_error *error = NULL;
  auto *config = reinterpret_cast<ob_config *>(configHandle);
  auto type = static_cast<ob_stream_type>(streamType);
  auto f = static_cast<ob_format>(format);
  ob_config_enable_video_stream(config, type, width, height, fps, f, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Config_nEnableAccelStream(JNIEnv *env, jclass clazz,
                                                     jlong configHandle,
                                                     jint fullScaleRange,
                                                     jint sampleRate) {
  ob_error *error = NULL;
  auto *config = reinterpret_cast<ob_config *>(configHandle);
  auto range = static_cast<ob_accel_full_scale_range>(fullScaleRange);
  auto rate = static_cast<ob_accel_sample_rate>(sampleRate);
  ob_config_enable_accel_stream(config, range, rate, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Config_nEnableGyroStream(JNIEnv *env, jclass clazz,
                                                  jlong configHandle,
                                                  jint fullScaleRange,
                                                  jint sampleRate) {
  ob_error *error = NULL;
  auto *config = reinterpret_cast<ob_config *>(configHandle);
  auto range = static_cast<ob_gyro_full_scale_range>(fullScaleRange);
  auto rate = static_cast<ob_gyro_sample_rate>(sampleRate);
  ob_config_enable_gyro_stream(config, range, rate, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Config_nDisableStream(JNIEnv *env, jclass instance,
                                               jlong handle, jint streamType) {
  ob_error *error = NULL;
  auto *config = reinterpret_cast<ob_config *>(handle);
  ob_config_disable_stream(config, static_cast<ob_stream_type>(streamType),
                           &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Config_nGetEnabledStreamProfileList(JNIEnv *env, jclass clazz,
                                                               jlong configHandle) {
  ob_error *error = NULL;
  auto *config = reinterpret_cast<ob_config *>(configHandle);
  ob_stream_profile_list *list = ob_config_get_enabled_stream_profile_list(config, &error);
  ob_handle_error(env, error);
  return (jlong) list;
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Config_nSetAlignMode(
    JNIEnv *env, jclass instance, jlong handle, jint mode) {
  ob_error *error = NULL;
  auto config = reinterpret_cast<ob_config *>(handle);
  ob_config_set_align_mode(config, static_cast<ob_align_mode>(mode), &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Config_nSetDepthScaleRequire(JNIEnv *env,
                                                      jclass instance,
                                                      jlong handle,
                                                      jboolean enable) {
  ob_error *error = NULL;
  auto config = reinterpret_cast<ob_config *>(handle);
  ob_config_set_depth_scale_require(config, enable, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Config_nSetD2CTargetResolution(
    JNIEnv *env, jclass instance, jlong handle, jint width, jint height) {
  ob_error *error = NULL;
  auto config = reinterpret_cast<ob_config *>(handle);
  ob_config_set_d2c_target_resolution(config, width, height, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Config_nDisableAllStream(JNIEnv *env, jclass instance,
                                                  jlong handle) {
  ob_error *error = NULL;
  ob_config *config = reinterpret_cast<ob_config *>(handle);
  ob_config_disable_all_stream(config, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Pipeline_nCreateWithDevice(JNIEnv *env,
                                                    jclass instance,
                                                    jlong deviceHandle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(deviceHandle);
  ob_pipeline *pipeline = ob_create_pipeline_with_device(device, &error);
  ob_handle_error(env, error);
  return (jlong)pipeline;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Pipeline_nCreateWithPlaybackFile(JNIEnv *env,
                                                          jclass instance,
                                                          jstring filePath) {
  ob_error *error = NULL;
  std::string strPlaybackFile(getStdString(
      env, filePath, "Pipeline#nCreateWithPlaybackFile", "filePath"));
  auto pipeline =
      ob_create_pipeline_with_playback_file(strPlaybackFile.c_str(), &error);
  ob_handle_error(env, error);
  return (jlong)pipeline;
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Pipeline_nDelete(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_delete_pipeline(pipeline, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Pipeline_nStartWithConfig(JNIEnv *env, jclass instance,
                                                   jlong handle,
                                                   jlong configHandle) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_config *config = reinterpret_cast<ob_config *>(configHandle);
  ob_pipeline_start_with_config(pipeline, config, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Pipeline_nStartWithCallback(JNIEnv *env,
                                                     jclass instance,
                                                     jlong handle,
                                                     jlong configHandle,
                                                     jobject callback) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_config *config = reinterpret_cast<ob_config *>(configHandle);
  void *cookie = NULL;
  std::lock_guard<std::mutex> lk(mutex_);
  if (callback) {
    jobject gCallback = env->NewGlobalRef(callback);
    cookie = gCallback;
    gListCallback_.push_back(std::pair<jlong, jobject>(handle, gCallback));
  }
  ob_pipeline_start_with_callback(pipeline, config, onFrameSetCallback, cookie,
                                  &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Pipeline_nStop(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_pipeline_stop(pipeline, &error);

  std::vector<std::pair<jlong, jobject>>::iterator callbackIt;
  std::lock_guard<std::mutex> lk(mutex_);
  for (callbackIt = gListCallback_.begin();
       callbackIt != gListCallback_.end();) {
    if (handle == callbackIt->first) {
      env->DeleteGlobalRef(callbackIt->second);
      callbackIt = gListCallback_.erase(callbackIt);
    } else {
      callbackIt++;
    }
  }
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Pipeline_nWaitForFrameSet(JNIEnv *env, jclass instance,
                                                   jlong handle,
                                                   jlong timeout) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_frame *frameSet = ob_pipeline_wait_for_frameset(pipeline, timeout, &error);
  ob_handle_error(env, error);
  return (jlong)frameSet;
}

extern "C" JNIEXPORT jlong JNICALL Java_com_orbbec_obsensor_Pipeline_nGetConfig(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_config *config = ob_pipeline_get_config(pipeline, &error);
  ob_handle_error(env, error);
  return (jlong)config;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Pipeline_nGetStreamProfileList(JNIEnv *env,
                                                        jclass instance,
                                                        jlong handle,
                                                        jint sensorType) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  auto streamProfiles = ob_pipeline_get_stream_profile_list(
      pipeline, static_cast<ob_sensor_type>(sensorType), &error);
  ob_handle_error(env, error);
  return (jlong)streamProfiles;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Pipeline_nEnableFrameSync(JNIEnv *env, jclass instance,
                                                   jlong handle) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_pipeline_enable_frame_sync(pipeline, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Pipeline_nDisableFrameSync(JNIEnv *env,
                                                    jclass instance,
                                                    jlong handle) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_pipeline_disable_frame_sync(pipeline, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Pipeline_nSwitchConfig(JNIEnv *env, jclass instance,
                                                jlong handle,
                                                jlong configHandle) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_config *config = reinterpret_cast<ob_config *>(configHandle);
  ob_pipeline_switch_config(pipeline, config, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Pipeline_nGetD2CDepthProfileList(
    JNIEnv *env, jclass instance, jlong handle, jlong colorProfileHandle,
    jint mode) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_stream_profile *colorProfile =
      reinterpret_cast<ob_stream_profile *>(colorProfileHandle);
  auto profileList = ob_get_d2c_depth_profile_list(
      pipeline, colorProfile, static_cast<ob_align_mode>(mode), &error);
  ob_handle_error(env, error);
  return (jlong)profileList;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Pipeline_nGetD2CValidArea(JNIEnv *env, jclass instance,
                                                   jlong handle, jint distance,
                                                   jbyteArray rect) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  jbyte *bytes = env->GetByteArrayElements(rect, JNI_FALSE);
  ob_rect valid_area = ob_get_d2c_valid_area(pipeline, distance, &error);
  int size = sizeof(valid_area);
  memmove(bytes, &valid_area, size);
  env->SetByteArrayRegion(rect, 0, size, bytes);
  env->ReleaseByteArrayElements(rect, bytes, 0);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Pipeline_nGetD2CRangeValidArea(
    JNIEnv *env, jclass instance, jlong handle, jint minDistance,
    jint maxDistance, jbyteArray rect) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  jbyte *bytes = env->GetByteArrayElements(rect, JNI_FALSE);
  ob_rect valid_area =
      ob_get_d2c_range_valid_area(pipeline, minDistance, maxDistance, &error);
  int size = sizeof(valid_area);
  memmove(bytes, &valid_area, size);
  env->SetByteArrayRegion(rect, 0, size, bytes);
  env->ReleaseByteArrayElements(rect, bytes, 0);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Pipeline_nGetCameraParam(
    JNIEnv *env, jclass instance, jlong handle, jbyteArray depthIntr,
    jbyteArray colorIntr, jbyteArray depthDisto, jbyteArray colorDisto,
    jbyteArray trans, jobject cameraParam) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  jclass cameraParamCls = env->GetObjectClass(cameraParam);
  jfieldID jfIsMirrored = env->GetFieldID(cameraParamCls, "mIsMirrored", "Z");

  ob_camera_param params = ob_pipeline_get_camera_param(pipeline, &error);
  ob_handle_error(env, error);

  env->SetBooleanField(cameraParam, jfIsMirrored, params.isMirrored);

  jbyte *depth_intr = env->GetByteArrayElements(depthIntr, JNI_FALSE);
  jbyte *color_intr = env->GetByteArrayElements(colorIntr, JNI_FALSE);
  jbyte *depth_disto = env->GetByteArrayElements(depthDisto, JNI_FALSE);
  jbyte *color_disto = env->GetByteArrayElements(colorDisto, JNI_FALSE);
  jbyte *transform = env->GetByteArrayElements(trans, JNI_FALSE);

  memmove(depth_intr, &params.depthIntrinsic, sizeof(params.depthIntrinsic));
  memmove(color_intr, &params.rgbIntrinsic, sizeof(params.rgbIntrinsic));
  memmove(depth_disto, &params.depthDistortion, sizeof(params.depthDistortion));
  memmove(color_disto, &params.rgbDistortion, sizeof(params.rgbDistortion));
  memmove(transform, &params.transform, sizeof(params.transform));

  env->SetByteArrayRegion(depthIntr, 0, sizeof(params.depthIntrinsic),
                          depth_intr);
  env->ReleaseByteArrayElements(depthIntr, depth_intr, 0);

  env->SetByteArrayRegion(colorIntr, 0, sizeof(params.rgbIntrinsic),
                          color_intr);
  env->ReleaseByteArrayElements(colorIntr, color_intr, 0);

  env->SetByteArrayRegion(depthDisto, 0, sizeof(params.depthDistortion),
                          depth_disto);
  env->ReleaseByteArrayElements(depthDisto, depth_disto, 0);

  env->SetByteArrayRegion(colorDisto, 0, sizeof(params.rgbDistortion),
                          color_disto);
  env->ReleaseByteArrayElements(colorDisto, color_disto, 0);

  env->SetByteArrayRegion(trans, 0, sizeof(params.transform), transform);
  env->ReleaseByteArrayElements(trans, transform, 0);
}

/*
 * Class:     com_orbbec_obsensor_Pipeline
 * Method:    nGetCameraParamWithProfile
 * Signature: (JIIII[B[B[B[B[BLcom/orbbec/obsensor/CameraParam;)V
 */
extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Pipeline_nGetCameraParamWithProfile(
    JNIEnv *env, jclass typePipeline, jlong handle, jint colorWidth,
    jint colorHeight, jint depthWidth, jint depthHeight, jbyteArray depthIntr,
    jbyteArray colorIntr, jbyteArray depthDisto, jbyteArray colorDisto,
    jbyteArray trans, jobject cameraParam) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  jclass cameraParamCls = env->GetObjectClass(cameraParam);
  jfieldID jfIsMirrored = env->GetFieldID(cameraParamCls, "mIsMirrored", "Z");

  ob_camera_param params = ob_pipeline_get_camera_param_with_profile(
      pipeline, colorWidth, colorHeight, depthWidth, depthHeight, &error);
  ob_handle_error(env, error);

  env->SetBooleanField(cameraParam, jfIsMirrored, params.isMirrored);

  jbyte *depth_intr = env->GetByteArrayElements(depthIntr, JNI_FALSE);
  jbyte *color_intr = env->GetByteArrayElements(colorIntr, JNI_FALSE);
  jbyte *depth_disto = env->GetByteArrayElements(depthDisto, JNI_FALSE);
  jbyte *color_disto = env->GetByteArrayElements(colorDisto, JNI_FALSE);
  jbyte *transform = env->GetByteArrayElements(trans, JNI_FALSE);

  memmove(depth_intr, &params.depthIntrinsic, sizeof(params.depthIntrinsic));
  memmove(color_intr, &params.rgbIntrinsic, sizeof(params.rgbIntrinsic));
  memmove(depth_disto, &params.depthDistortion, sizeof(params.depthDistortion));
  memmove(color_disto, &params.rgbDistortion, sizeof(params.rgbDistortion));
  memmove(transform, &params.transform, sizeof(params.transform));

  env->SetByteArrayRegion(depthIntr, 0, sizeof(params.depthIntrinsic),
                          depth_intr);
  env->ReleaseByteArrayElements(depthIntr, depth_intr, 0);

  env->SetByteArrayRegion(colorIntr, 0, sizeof(params.rgbIntrinsic),
                          color_intr);
  env->ReleaseByteArrayElements(colorIntr, color_intr, 0);

  env->SetByteArrayRegion(depthDisto, 0, sizeof(params.depthDistortion),
                          depth_disto);
  env->ReleaseByteArrayElements(depthDisto, depth_disto, 0);

  env->SetByteArrayRegion(colorDisto, 0, sizeof(params.rgbDistortion),
                          color_disto);
  env->ReleaseByteArrayElements(colorDisto, color_disto, 0);

  env->SetByteArrayRegion(trans, 0, sizeof(params.transform), transform);
  env->ReleaseByteArrayElements(trans, transform, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Pipeline_nStartRecord(JNIEnv *env, jclass instance,
                                               jlong handle, jstring filePath) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  std::string strFilePath(
      getStdString(env, filePath, "Pipeline#nStartRecord", "filePath"));
  ob_pipeline_start_record(pipeline, strFilePath.c_str(), &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Pipeline_nStopRecord(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_pipeline_stop_record(pipeline, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Pipeline_nGetPlayback(JNIEnv *env, jclass instance,
                                               jlong handle) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  auto playback = ob_pipeline_get_playback(pipeline, &error);
  ob_handle_error(env, error);
  return (jlong)playback;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Pipeline_nGetCalibrationParam(JNIEnv *env, jclass clazz, jlong handle,
                                                       jlong configHandle,
                                                       jbyteArray intrinsicsBytes,
                                                       jbyteArray distortionBytes,
                                                       jbyteArray extrinsicBytes) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_config *config = reinterpret_cast<ob_config *>(configHandle);

  jsize ib_size = env->GetArrayLength(intrinsicsBytes);
  jbyte *ib_data = env->GetByteArrayElements(intrinsicsBytes, JNI_FALSE);
  if (ib_size != (sizeof(ob_camera_intrinsic) * OB_SENSOR_COUNT)) {
    LOGE("Intrinsics Size Error!");
    return;
  }
  jsize db_size = env->GetArrayLength(distortionBytes);
  jbyte *db_data = env->GetByteArrayElements(distortionBytes, JNI_FALSE);
  if (db_size != (sizeof(ob_camera_distortion) * OB_SENSOR_COUNT)) {
    LOGE("Distortion Size Error!");
    return;
  }
  jsize eb_size = env->GetArrayLength(extrinsicBytes);
  jbyte *eb_data = env->GetByteArrayElements(extrinsicBytes, JNI_FALSE);
  if (eb_size != (sizeof(ob_d2c_transform) * OB_SENSOR_COUNT * OB_SENSOR_COUNT)) {
    LOGE("Extrinsic Size Error!");
    return;
  }
  ob_calibration_param cp_data = ob_pipeline_get_calibration_param(pipeline, config, &error);

  memmove(ib_data, &cp_data.intrinsics, sizeof(ob_camera_intrinsic) * OB_SENSOR_COUNT);
  memmove(db_data, &cp_data.distortion, sizeof(ob_camera_distortion) * OB_SENSOR_COUNT);
  memmove(eb_data, &cp_data.extrinsics, sizeof(ob_d2c_transform) * OB_SENSOR_COUNT * OB_SENSOR_COUNT);

  env->SetByteArrayRegion(intrinsicsBytes, 0, sizeof(cp_data.intrinsics) * OB_SENSOR_COUNT,
                          ib_data);
  env->ReleaseByteArrayElements(intrinsicsBytes, ib_data, 0);
  env->SetByteArrayRegion(distortionBytes, 0, sizeof(cp_data.distortion) * OB_SENSOR_COUNT,
                          db_data);
  env->ReleaseByteArrayElements(distortionBytes, db_data, 0);
  env->SetByteArrayRegion(extrinsicBytes, 0, sizeof(cp_data.extrinsics) * OB_SENSOR_COUNT * OB_SENSOR_COUNT,
                          eb_data);
  env->ReleaseByteArrayElements(extrinsicBytes, eb_data, 0);

    for (auto & intrinsic : cp_data.intrinsics) {
        LOGD("测试 %f %f %f %f %d %d", intrinsic.fx,
             intrinsic.fy,
             intrinsic.cx,
             intrinsic.cy,
             intrinsic.width,
             intrinsic.height);
    }

    for (auto & i : cp_data.distortion) {
        LOGD("测试 %f %f %f %f %f %f %f %f", i.k1,
             i.k2,
             i.k3,
             i.k4,
             i.k5,
             i.k6,
             i.p1,
             i.p2);
    }

  ob_handle_error(env, error);
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  gJVM = vm;
  return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
  JNIEnv *env;
  gJVM->GetEnv((void **)&env, JNI_VERSION_1_6);
  gJVM = NULL;
}

/**
 * Filter
 */
extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Filter_nDelete(
    JNIEnv *env, jclass clazz, jlong handle) {
  ob_error *error = NULL;
  auto filter = reinterpret_cast<ob_filter *>(handle);
  std::vector<std::pair<jlong, jobject>>::iterator callbackIt;
  std::lock_guard<std::mutex> lk(mutex_);
  for (callbackIt = gListCallback_.begin();
       callbackIt != gListCallback_.end();) {
    if (handle == callbackIt->first) {
      env->DeleteGlobalRef(callbackIt->second);
      callbackIt = gListCallback_.erase(callbackIt);
    } else {
      callbackIt++;
    }
  }
  ob_delete_filter(filter, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT jlong JNICALL Java_com_orbbec_obsensor_Filter_nProcess(
    JNIEnv *env, jclass clazz, jlong handle, jlong frameHandle) {
  ob_error *error = NULL;
  auto filter = reinterpret_cast<ob_filter *>(handle);
  auto frame = reinterpret_cast<ob_frame *>(frameHandle);
  auto processFrame = ob_filter_process(filter, frame, &error);
  ob_handle_error(env, error);
  return (jlong)processFrame;
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Filter_nReset(
    JNIEnv *env, jclass clazz, jlong handle) {
  ob_error *error = NULL;
  auto filter = reinterpret_cast<ob_filter *>(handle);
  ob_filter_reset(filter, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Filter_nSetCallback(
    JNIEnv *env, jclass clazz, jlong handle, jobject callback) {
  ob_error *error = NULL;
  auto filter = reinterpret_cast<ob_filter *>(handle);
  void *cookie = NULL;
  std::lock_guard<std::mutex> lk(mutex_);
  if (callback) {
    jobject gCallback = env->NewGlobalRef(callback);
    cookie = gCallback;
    gListCallback_.push_back(std::pair<jlong, jobject>(handle, gCallback));
  }
  ob_filter_set_callback(filter, onFilterCallback, cookie, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Filter_nPushFrame(
    JNIEnv *env, jclass clazz, jlong handle, jlong frameHandle) {
  ob_error *error = NULL;
  auto filter = reinterpret_cast<ob_filter *>(handle);
  auto frame = reinterpret_cast<ob_frame *>(frameHandle);
  ob_filter_push_frame(filter, frame, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Filter_nEnable(JNIEnv *env, jclass clazz,
                                        jlong handle,
                                        jboolean isEnable) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_filter_enable(filter, isEnable, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_orbbec_obsensor_Filter_nIsEnable(JNIEnv *env, jclass clazz,
                                          jlong handle) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  bool result = ob_filter_is_enable(filter, &error);
  ob_handle_error(env, error);
  return result;
}

/**
 * PointCloudFilter
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_PointCloudFilter_nCreate(JNIEnv *env, jclass clazz) {
  ob_error *error = NULL;
  auto filter = ob_create_pointcloud_filter(&error);
  ob_handle_error(env, error);
  return (jlong)filter;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_PointCloudFilter_nSetPointFormat(JNIEnv *env,
                                                          jclass clazz,
                                                          jint format,
                                                          jlong filterPtr) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(filterPtr);
  ob_pointcloud_filter_set_point_format(filter, static_cast<ob_format>(format),
                                        &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_PointCloudFilter_nSetCameraParam(
    JNIEnv *env, jclass clazz, jlong handle, jbyteArray depthIntr,
    jbyteArray colorIntr, jbyteArray depthDistor, jbyteArray colorDistor,
    jbyteArray tran, jboolean isMirrored) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  jbyte *depthIntrinsic = env->GetByteArrayElements(depthIntr, JNI_FALSE);
  jbyte *colorIntrinsic = env->GetByteArrayElements(colorIntr, JNI_FALSE);
  jbyte *depthDistortion = env->GetByteArrayElements(depthDistor, JNI_FALSE);
  jbyte *colorDistortion = env->GetByteArrayElements(colorDistor, JNI_FALSE);
  jbyte *transform = env->GetByteArrayElements(tran, JNI_FALSE);

  ob_camera_param param;
  memmove(&param.depthIntrinsic, depthIntrinsic, sizeof(param.depthIntrinsic));
  memmove(&param.rgbIntrinsic, colorIntrinsic, sizeof(param.rgbIntrinsic));
  memmove(&param.depthDistortion, depthDistortion,
          sizeof(param.depthDistortion));
  memmove(&param.rgbDistortion, colorDistortion, sizeof(param.rgbDistortion));
  memmove(&param.transform, transform, sizeof(param.transform));
  param.isMirrored = isMirrored;

  ob_pointcloud_filter_set_camera_param(filter, param, &error);

  env->ReleaseByteArrayElements(depthIntr, depthIntrinsic, 0);
  env->ReleaseByteArrayElements(colorIntr, colorIntrinsic, 0);
  env->ReleaseByteArrayElements(depthDistor, depthDistortion, 0);
  env->ReleaseByteArrayElements(colorDistor, colorDistortion, 0);
  env->ReleaseByteArrayElements(tran, transform, 0);

  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_PointCloudFilter_nSetD2CAlignStatus(
    JNIEnv *env, jclass clazz, jboolean d2c_status, jlong filter_ptr) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(filter_ptr);

  ob_pointcloud_filter_set_frame_align_state(filter, d2c_status, &error);

  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_PointCloudFilter_nSetPositionDataScale(JNIEnv *env,
                                                                jclass clazz,
                                                                jlong handle,
                                                                jfloat scale) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);

  ob_pointcloud_filter_set_position_data_scale(filter, scale, &error);

  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_PointCloudFilter_nSetColorDataNormalization(
    JNIEnv *env, jclass clazz, jlong handle, jboolean state) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);

  ob_pointcloud_filter_set_color_data_normalization(filter, state, &error);

  ob_handle_error(env, error);
}

/**
 * FormatConvertFilter
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_FormatConvertFilter_nCreate(JNIEnv *env,
                                                     jclass instance) {
  ob_error *error = NULL;
  auto filter = ob_create_format_convert_filter(&error);
  ob_handle_error(env, error);
  return (jlong)filter;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_FormatConvertFilter_nSetFormatConvertType(
    JNIEnv *env, jclass clazz, jint type, jlong filter_ptr) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(filter_ptr);
  ob_format_convert_filter_set_format(
      filter, static_cast<ob_convert_format>(type), &error);
  ob_handle_error(env, error);
}

/**
 * HolefillingFilter
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_HoleFillingFilter_nCreate(JNIEnv *env, jclass clazz) {
  ob_error *error = NULL;
  auto filter = ob_create_holefilling_filter(&error);
  ob_handle_error(env, error);
  return (jlong) filter;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_HoleFillingFilter_nSetMode(JNIEnv *env, jclass clazz,
                                                    jlong handle,
                                                    jint mode) {
  ob_error *error = NULL;
  auto filter = reinterpret_cast<ob_filter *>(handle);
  ob_hole_filling_mode hf_mode = static_cast<ob_hole_filling_mode>(mode);
  ob_holefilling_filter_set_mode(filter, hf_mode, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_HoleFillingFilter_nGetMode(JNIEnv *env, jclass clazz,
                                                    jlong handle) {
  ob_error *error = NULL;
  auto filter = reinterpret_cast<ob_filter *>(handle);
  ob_hole_filling_mode mode = ob_holefilling_filter_get_mode(filter, &error);
  ob_handle_error(env, error);
  return (int) mode;
}

/**
 * TemporalFilter
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_TemporalFilter_nCreate(JNIEnv *env, jclass clazz) {
  ob_error *error = NULL;
  auto filter = ob_create_temporal_filter(&error);
  ob_handle_error(env, error);
  return (jlong) filter;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_TemporalFilter_nGetDiffscaleRange(JNIEnv *env, jclass clazz,
                                                           jlong handle,
                                                           jbyteArray diffscaleRange) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_float_property_range range = ob_temporal_filter_get_diffscale_range(filter, &error);
  ob_handle_error(env, error);

  jbyte *diffscaleRange_ = env->GetByteArrayElements(diffscaleRange, JNI_FALSE);

  memmove(diffscaleRange_, &range, sizeof(ob_float_property_range));
  env->SetByteArrayRegion(diffscaleRange,0, sizeof(ob_float_property_range),
                          diffscaleRange_);

  env->ReleaseByteArrayElements(diffscaleRange, diffscaleRange_, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_TemporalFilter_nSetDiffscaleValue(JNIEnv *env, jclass clazz,
                                                           jlong handle,
                                                           jfloat value) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_temporal_filter_set_diffscale_value(filter, value, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_TemporalFilter_nGetWeightRange(JNIEnv *env, jclass clazz,
                                                        jlong handle,
                                                        jbyteArray weightRange) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_float_property_range range = ob_temporal_filter_get_weight_range(filter, &error);
  ob_handle_error(env, error);

  jbyte *weightRange_ = env->GetByteArrayElements(weightRange, JNI_FALSE);

  memmove(weightRange_, &range, sizeof(ob_float_property_range));
  env->SetByteArrayRegion(weightRange, 0, sizeof(ob_float_property_range),
                          weightRange_);

  env->ReleaseByteArrayElements(weightRange, weightRange_, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_TemporalFilter_nSetWeightValue(JNIEnv *env, jclass clazz,
                                                        jlong handle,
                                                        jfloat value) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_temporal_filter_set_weight_value(filter, value, &error);


  ob_handle_error(env, error);
}

/**
 * SpatialAdvancedFilter
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_SpatialAdvancedFilter_nCreate(JNIEnv *env, jclass clazz) {
  ob_error *error = NULL;
  auto filter = ob_create_spatial_advanced_filter(&error);
  ob_handle_error(env, error);
  return (jlong) filter;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_SpatialAdvancedFilter_nGetAlphaRange(JNIEnv *env, jclass clazz,
                                                              jlong handle,
                                                              jbyteArray alphaRange) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_float_property_range range = ob_spatial_advanced_filter_get_alpha_range(filter, &error);
  ob_handle_error(env, error);

  jbyte *alphaRange_ = env->GetByteArrayElements(alphaRange, JNI_FALSE);

  memmove(alphaRange_, &range, sizeof(ob_float_property_range));
  env->SetByteArrayRegion(alphaRange, 0, sizeof(ob_float_property_range),
                          alphaRange_);

  env->ReleaseByteArrayElements(alphaRange, alphaRange_, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_SpatialAdvancedFilter_nGetDispDiffRange(JNIEnv *env, jclass clazz,
                                                                 jlong handle,
                                                                 jbyteArray dispDiffRange) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_uint16_property_range range = ob_spatial_advanced_filter_get_disp_diff_range(filter, &error);
  ob_handle_error(env, error);

  jbyte *dispDiffRange_ = env->GetByteArrayElements(dispDiffRange, JNI_FALSE);

  memmove(dispDiffRange_, &range, sizeof(ob_uint16_property_range));
  env->SetByteArrayRegion(dispDiffRange, 0, sizeof(ob_uint16_property_range),
                          dispDiffRange_);

  env->ReleaseByteArrayElements(dispDiffRange, dispDiffRange_, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_SpatialAdvancedFilter_nGetRadiusRange(JNIEnv *env, jclass clazz,
                                                               jlong handle,
                                                               jbyteArray radiusRange) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_uint16_property_range range = ob_spatial_advanced_filter_get_radius_range(filter, &error);
  ob_handle_error(env, error);

  jbyte *radiusRange_ = env->GetByteArrayElements(radiusRange, JNI_FALSE);

  memmove(radiusRange_, &range, sizeof(ob_uint16_property_range));
  env->SetByteArrayRegion(radiusRange, 0, sizeof(ob_uint16_property_range),
                          radiusRange_);

  env->ReleaseByteArrayElements(radiusRange, radiusRange_, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_SpatialAdvancedFilter_nGetMagnitudeRange(JNIEnv *env, jclass clazz,
                                                                  jlong handle,
                                                                  jbyteArray magnitudeRange) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_int_property_range range = ob_spatial_advanced_filter_get_magnitude_range(filter, &error);
  ob_handle_error(env, error);

  jbyte *magnitudeRange_ = env->GetByteArrayElements(magnitudeRange, JNI_FALSE);

  memmove(magnitudeRange_, &range, sizeof(ob_int_property_range));
  env->SetByteArrayRegion(magnitudeRange, 0, sizeof(ob_int_property_range),
                          magnitudeRange_);

  env->ReleaseByteArrayElements(magnitudeRange, magnitudeRange_, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_SpatialAdvancedFilter_nGetFilterParams(JNIEnv *env, jclass clazz,
                                                                jlong handle,
                                                                jbyteArray params) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_spatial_advanced_filter_params filter_params = ob_spatial_advanced_filter_get_filter_params(filter, &error);
  ob_handle_error(env, error);

  jbyte *params_ = env->GetByteArrayElements(params, JNI_FALSE);

  memmove(params_, &filter_params, sizeof(ob_spatial_advanced_filter_params));
  env->SetByteArrayRegion(params, 0, sizeof(ob_spatial_advanced_filter_params),
                          params_);

  env->ReleaseByteArrayElements(params, params_, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_SpatialAdvancedFilter_nSetFilterParams(JNIEnv *env, jclass clazz,
                                                                jlong handle,
                                                                jbyteArray params) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);

  jbyte *params_ = env->GetByteArrayElements(params, JNI_FALSE);
  ob_spatial_advanced_filter_params filter_params;

  memmove(&filter_params, params_, sizeof(ob_spatial_advanced_filter_params));
  env->ReleaseByteArrayElements(params, params_, 0);

  ob_spatial_advanced_filter_set_filter_params(filter, filter_params, &error);
  ob_handle_error(env ,error);
}

/**
 * NoiseRemovalFilter
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_NoiseRemovalFilter_nCreate(JNIEnv *env, jclass clazz) {
  ob_error *error = NULL;
  auto filter = ob_create_noise_removal_filter(&error);
  ob_handle_error(env, error);
  return (jlong) filter;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_NoiseRemovalFilter_nGetDispDiffRange(JNIEnv *env, jclass clazz,
                                                              jlong handle,
                                                              jbyteArray dispDiffRange) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_uint16_property_range range = ob_noise_removal_filter_get_disp_diff_range(filter, &error);
  ob_handle_error(env, error);

  jbyte *dispDiffRange_ = env->GetByteArrayElements(dispDiffRange, JNI_FALSE);

  memmove(dispDiffRange_, &range, sizeof(ob_uint16_property_range));
  env->SetByteArrayRegion(dispDiffRange, 0, sizeof(ob_uint16_property_range),
                          dispDiffRange_);

  env->ReleaseByteArrayElements(dispDiffRange, dispDiffRange_, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_NoiseRemovalFilter_nGetMaxSizeRange(JNIEnv *env, jclass clazz,
                                                             jlong handle,
                                                             jbyteArray maxSizeRange) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_uint16_property_range range = ob_noise_removal_filter_get_max_size_range(filter, &error);
  ob_handle_error(env, error);

  jbyte *maxSizeRange_ = env->GetByteArrayElements(maxSizeRange, JNI_FALSE);

  memmove(maxSizeRange_, &range, sizeof(ob_uint16_property_range));
  env->SetByteArrayRegion(maxSizeRange, 0, sizeof(ob_uint16_property_range),
                          maxSizeRange_);

  env->ReleaseByteArrayElements(maxSizeRange, maxSizeRange_, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_NoiseRemovalFilter_nSetFilterParams(JNIEnv *env, jclass clazz,
                                                             jlong handle,
                                                             jbyteArray params) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);

  jbyte *params_ = env->GetByteArrayElements(params, JNI_FALSE);
  ob_noise_removal_filter_params filter_params;

  memmove(&filter_params, params_, sizeof(ob_noise_removal_filter_params));
  env->ReleaseByteArrayElements(params, params_, 0);

  ob_noise_removal_filter_set_filter_params(filter, filter_params, &error);
  ob_handle_error(env ,error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_NoiseRemovalFilter_nGetFilterParams(JNIEnv *env, jclass clazz,
                                                             jlong handle,
                                                             jbyteArray params) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_noise_removal_filter_params filter_params = ob_noise_removal_filter_get_filter_params(filter, &error);
  ob_handle_error(env, error);

  jbyte *params_ = env->GetByteArrayElements(params, JNI_FALSE);

  memmove(params_, &filter_params, sizeof(ob_noise_removal_filter_params));
  env->SetByteArrayRegion(params, 0, sizeof(ob_noise_removal_filter_params),
                          params_);

  env->ReleaseByteArrayElements(params, params_, 0);
}

/**
 * EdgeNoiseRemovalFilter
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_EdgeNoiseRemovalFilter_nCreate(JNIEnv *env, jclass clazz) {
  ob_error *error = NULL;
  auto filter = ob_create_edge_noise_removal_filter(&error);
  ob_handle_error(env, error);
  return (jlong) filter;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_EdgeNoiseRemovalFilter_nSetFilterParams(JNIEnv *env, jclass clazz,
                                                                 jlong handle,
                                                                 jbyteArray params) {
    ob_error *error = NULL;
    ob_filter *filter = reinterpret_cast<ob_filter *>(handle);

    jbyte *params_ = env->GetByteArrayElements(params, JNI_FALSE);
    ob_edge_noise_removal_filter_params filter_params;

    memmove(&filter_params, params_, sizeof(ob_edge_noise_removal_filter_params));
    env->ReleaseByteArrayElements(params, params_, 0);

    ob_edge_noise_removal_filter_set_filter_params(filter, filter_params, &error);
    ob_handle_error(env ,error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_EdgeNoiseRemovalFilter_nGetFilterParams(JNIEnv *env, jclass clazz,
                                                                 jlong handle,
                                                                 jbyteArray params) {
    ob_error *error = NULL;
    ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
    ob_edge_noise_removal_filter_params filter_params = ob_edge_noise_removal_filter_get_filter_params(filter, &error);
    ob_handle_error(env, error);

    jbyte *params_ = env->GetByteArrayElements(params, JNI_FALSE);

    memmove(params_, &filter_params, sizeof(ob_edge_noise_removal_filter_params));
    env->SetByteArrayRegion(params, 0, sizeof(ob_edge_noise_removal_filter_params),
                            params_);

    env->ReleaseByteArrayElements(params, params_, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_EdgeNoiseRemovalFilter_nGetMarginLeftThRange(JNIEnv *env, jclass clazz,
                                                                      jlong handle,
                                                                      jbyteArray marginLeftThRange) {
    ob_error *error = NULL;
    ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
    ob_uint16_property_range range = ob_edge_noise_removal_filter_get_margin_left_th_range(filter, &error);
    ob_handle_error(env, error);

    jbyte *marginLeftThRange_ = env->GetByteArrayElements(marginLeftThRange, JNI_FALSE);

    memmove(marginLeftThRange_, &range, sizeof(ob_uint16_property_range));
    env->SetByteArrayRegion(marginLeftThRange, 0, sizeof(ob_uint16_property_range),
                            marginLeftThRange_);

    env->ReleaseByteArrayElements(marginLeftThRange, marginLeftThRange_, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_EdgeNoiseRemovalFilter_nGetMarginRightThRange(JNIEnv *env, jclass clazz,
                                                                       jlong handle,
                                                                       jbyteArray marginRightThRange) {
    ob_error *error = NULL;
    ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
    ob_uint16_property_range range = ob_edge_noise_removal_filter_get_margin_right_th_range(filter, &error);
    ob_handle_error(env, error);

    jbyte *marginRightThRange_ = env->GetByteArrayElements(marginRightThRange, JNI_FALSE);

    memmove(marginRightThRange_, &range, sizeof(ob_uint16_property_range));
    env->SetByteArrayRegion(marginRightThRange, 0, sizeof(ob_uint16_property_range),
                            marginRightThRange_);

    env->ReleaseByteArrayElements(marginRightThRange, marginRightThRange_, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_EdgeNoiseRemovalFilter_nGetMarginTopThRange(JNIEnv *env, jclass clazz,
                                                                     jlong handle,
                                                                     jbyteArray marginTopThRange) {
    ob_error *error = NULL;
    ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
    ob_uint16_property_range range = ob_edge_noise_removal_filter_get_margin_top_th_range(filter, &error);
    ob_handle_error(env, error);

    jbyte *marginTopThRange_ = env->GetByteArrayElements(marginTopThRange, JNI_FALSE);

    memmove(marginTopThRange_, &range, sizeof(ob_uint16_property_range));
    env->SetByteArrayRegion(marginTopThRange, 0, sizeof(ob_uint16_property_range),
                            marginTopThRange_);

    env->ReleaseByteArrayElements(marginTopThRange, marginTopThRange_, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_EdgeNoiseRemovalFilter_nGetMarginBottomThRange(JNIEnv *env, jclass clazz,
                                                                        jlong handle,
                                                                        jbyteArray marginBottomThRange) {
    ob_error *error = NULL;
    ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
    ob_uint16_property_range range = ob_edge_noise_removal_filter_get_margin_bottom_th_range(filter, &error);
    ob_handle_error(env, error);

    jbyte *marginBottomThRange_ = env->GetByteArrayElements(marginBottomThRange, JNI_FALSE);

    memmove(marginBottomThRange_, &range, sizeof(ob_uint16_property_range));
    env->SetByteArrayRegion(marginBottomThRange, 0, sizeof(ob_uint16_property_range),
                            marginBottomThRange_);

    env->ReleaseByteArrayElements(marginBottomThRange, marginBottomThRange_, 0);
}

/**
 * DecimationFilter
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_DecimationFilter_nCreate(JNIEnv *env, jclass clazz) {
    ob_error *error = NULL;
    auto filter = ob_create_decimation_filter(&error);
    ob_handle_error(env, error);
    return (jlong) filter;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_DecimationFilter_nGetScaleRange(JNIEnv *env, jclass clazz,
                                                         jlong handle,
                                                         jbyteArray scaleRange) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_uint8_property_range range = ob_decimation_filter_get_scale_range(filter, &error);
  ob_handle_error(env, error);

  jbyte *scaleRange_ = env->GetByteArrayElements(scaleRange, JNI_FALSE);

  memmove(scaleRange_, &range, sizeof(ob_uint8_property_range));
  env->SetByteArrayRegion(scaleRange, 0, sizeof(ob_uint8_property_range),
                          scaleRange_);

  env->ReleaseByteArrayElements(scaleRange, scaleRange_, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_DecimationFilter_nSetScaleValue(JNIEnv *env, jclass clazz,
                                                         jlong handle,
                                                         jshort value) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_decimation_filter_set_scale_value(filter, (uint8_t) value, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT jshort JNICALL
Java_com_orbbec_obsensor_DecimationFilter_nGetScaleValue(JNIEnv *env, jclass clazz,
                                                         jlong handle) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  uint8_t value = ob_decimation_filter_get_scale_value(filter, &error);
  ob_handle_error(env, error);
  return (jshort) value;
}

/**
 * ThresholdFilter
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_ThresholdFilter_nCreate(JNIEnv *env, jclass clazz) {
  ob_error *error = NULL;
  auto filter = ob_create_threshold_filter(&error);
  ob_handle_error(env, error);
  return (jlong) filter;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_ThresholdFilter_mGetMinRange(JNIEnv *env, jobject thiz,
                                                      jlong handle,
                                                      jbyteArray minRange) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_int_property_range range = ob_threshold_filter_get_min_range(filter, &error);
  ob_handle_error(env, error);

  jbyte *minRange_ = env->GetByteArrayElements(minRange, JNI_FALSE);

  memmove(minRange_, &range, sizeof(ob_int_property_range));
  env->SetByteArrayRegion(minRange, 0, sizeof(ob_int_property_range),
                          minRange_);

  env->ReleaseByteArrayElements(minRange, minRange_, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_ThresholdFilter_mGetMaxRange(JNIEnv *env, jobject thiz,
                                                      jlong handle,
                                                      jbyteArray maxRange) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_int_property_range range = ob_threshold_filter_get_max_range(filter, &error);
  ob_handle_error(env, error);

  jbyte *maxRange_ = env->GetByteArrayElements(maxRange, JNI_FALSE);

  memmove(maxRange_, &range, sizeof(ob_int_property_range));
  env->SetByteArrayRegion(maxRange, 0, sizeof(ob_int_property_range),
                          maxRange_);

  env->ReleaseByteArrayElements(maxRange, maxRange_, 0);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_orbbec_obsensor_ThresholdFilter_nSetScaleValue(JNIEnv *env, jobject thiz,
                                                        jlong handle,
                                                        jint min, jint max) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  bool value = ob_threshold_filter_set_scale_value(filter, min, max, &error);
  ob_handle_error(env, error);
  return value;
}

/**
 * SequenceIdFilter
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_SequenceIdFilter_nCreate(JNIEnv *env, jclass clazz) {
  ob_error *error = NULL;
  auto filter = ob_create_sequenceId_filter(&error);
  ob_handle_error(env, error);
  return (jlong) filter;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_SequenceIdFilter_nSelectSequenceId(JNIEnv *env, jclass clazz,
                                                            jlong handle,
                                                            jint sequenceId) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_sequence_id_filter_select_sequence_id(filter, sequenceId, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_SequenceIdFilter_nGetSequenceId(JNIEnv *env, jclass clazz,
                                                         jlong handle) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  int sequenceId = ob_sequence_id_filter_get_sequence_id(filter, &error);
  ob_handle_error(env, error);
  return sequenceId;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_SequenceIdFilter_nGetSequenceIdList(JNIEnv *env, jclass clazz,
                                                             jlong handle, jbyteArray idList) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_sequence_id_item *list = ob_sequence_id_filter_get_sequence_id_list(filter, &error);
  ob_handle_error(env, error);

  jbyte *idList_ = env->GetByteArrayElements(idList, JNI_FALSE);

  memmove(idList_, list, sizeof(ob_sequence_id_item));
  env->SetByteArrayRegion(idList, 0, sizeof(ob_sequence_id_item),
                          idList_);

  env->ReleaseByteArrayElements(idList, idList_, 0);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_SequenceIdFilter_nGetSequenceIdListSize(JNIEnv *env, jclass clazz,
                                                                 jlong handle) {
  ob_error *error = NULL;
  ob_filter * filter = reinterpret_cast<ob_filter *>(handle);
  int result = ob_sequence_id_filter_get_sequence_id_list_size(filter, &error);
  ob_handle_error(env, error);
  return result;
}

/**
 * HdrMergeFilter
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_HdrMergeFilter_nCreate(JNIEnv *env, jclass clazz) {
  ob_error  *error = NULL;
  auto filter = ob_create_hdr_merge(&error);
  ob_handle_error(env, error);
  return (jlong) filter;
}

/**
 * AlignFilter
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_AlignFilter_nCreate(JNIEnv *env,
                                                    jclass clazz,
                                                    jint streamType) {
  ob_error *error = NULL;
  auto filter = ob_create_align(&error, static_cast<ob_stream_type>(streamType));
  ob_handle_error(env, error);
  return (jlong) filter;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_AlignFilter_nGetAlignStreamType(JNIEnv *env,
                                                                jclass clazz,
                                                                jlong handle) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  ob_stream_type type = ob_align_get_to_stream_type(filter, &error);
  ob_handle_error(env, error);
  return (int) type;
}

/**
 * DisparityTransform
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_DisparityTransform_nCreate(JNIEnv *env, jclass clazz,
                                                    jboolean isDepthToDisparity) {
  ob_error *error = NULL;
  auto filter = ob_create_disparity_transform(&error, isDepthToDisparity);
  ob_handle_error(env, error);
  return (jlong) filter;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_Device_nGetSupportedPropertyCount(JNIEnv *env,
                                                           jclass instance,
                                                           jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto count = ob_device_get_supported_property_count(device, &error);
  ob_handle_error(env, error);
  return count;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_orbbec_obsensor_Device_nGetSupportedProperty(JNIEnv *env,
                                                      jclass instance,
                                                      jlong handle,
                                                      jint index) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  jclass devicePropertyItemClass =
      env->FindClass("com/orbbec/obsensor/DevicePropertyInfo");

  if (devicePropertyItemClass == NULL)
    return nullptr;

  ob_property_item unified_property =
      ob_device_get_supported_property(device, index, &error);

  jfieldID property_id =
      env->GetFieldID(devicePropertyItemClass, "propertyID", "I");
  jfieldID property_name = env->GetFieldID(
      devicePropertyItemClass, "propertyName", "Ljava/lang/String;");
  jfieldID property_type_id =
      env->GetFieldID(devicePropertyItemClass, "propertyTypeID", "I");
  jfieldID permission_id =
      env->GetFieldID(devicePropertyItemClass, "permissionID", "I");

  jobject result = env->AllocObject(devicePropertyItemClass);
  env->SetIntField(result, property_id, (jint)unified_property.id);
  env->SetObjectField(result, property_name,
                      env->NewStringUTF(unified_property.name));
  env->SetIntField(result, property_type_id, (jint)unified_property.type);
  env->SetIntField(result, permission_id, (jint)unified_property.permission);

  ob_handle_error(env, error);
  return result;
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_orbbec_obsensor_AccelFrame_nGetAccelData(JNIEnv *env, jclass clazz,
                                                  jlong handle,
                                                  jfloatArray jData) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  OBAccelValue accelValue;
  memset(&accelValue, 0, sizeof(accelValue));
  accelValue = ob_accel_frame_value(frame, &error);

  jsize length = env->GetArrayLength(jData);
  jfloatArray jarr = env->NewFloatArray(length);
  jfloat *p_rst = env->GetFloatArrayElements(jarr, NULL);
  p_rst[0] = accelValue.x;
  p_rst[1] = accelValue.y;
  p_rst[2] = accelValue.z;
  env->ReleaseFloatArrayElements(jarr, p_rst, 0);

  ob_handle_error(env, error);

  return jarr;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_orbbec_obsensor_AccelFrame_nGetAccelTemperature(JNIEnv *env,
                                                         jclass clazz,
                                                         jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  float temperature = ob_accel_frame_temperature(frame, &error);
  ob_handle_error(env, error);
  return temperature;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_AccelStreamProfile_nGetAccelFullScaleRange(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_stream_profile *accelStreamProfile =
      reinterpret_cast<ob_stream_profile *>(handle);
  auto type =
      ob_accel_stream_profile_full_scale_range(accelStreamProfile, &error);
  ob_handle_error(env, error);
  return (jint)type;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_AccelStreamProfile_nGetAccelSampleRate(JNIEnv *env,
                                                                jclass instance,
                                                                jlong handle) {
  ob_error *error = NULL;
  ob_stream_profile *accelStreamProfile =
      reinterpret_cast<ob_stream_profile *>(handle);
  auto type = ob_accel_stream_profile_sample_rate(accelStreamProfile, &error);
  ob_handle_error(env, error);
  return (jint)type;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_AccelStreamProfile_nGetIntrinsic(JNIEnv *env,
                                                          jclass clazz,
                                                          jlong handle,
                                                          jbyteArray intrinsic) {
  ob_error *error = NULL;
  ob_stream_profile *streamProfile =
          reinterpret_cast<ob_stream_profile *>(handle);
//  ob_accel_intrinsic intrinsic_param = ob_accel_stream_profile_get_intrinsic(streamProfile, &error);
  ob_handle_error(env, error);

  jsize arrayLength = env->GetArrayLength(intrinsic);
  if (arrayLength < sizeof(ob_accel_intrinsic)) {
    return;
  }

  jbyte *p_rst = env->GetByteArrayElements(intrinsic, NULL);
//  memmove(p_rst, &intrinsic_param, sizeof(ob_accel_intrinsic));
  env->ReleaseByteArrayElements(intrinsic, p_rst, 0);
//  env->SetByteArrayRegion(intrinsic, 0, sizeof(ob_accel_intrinsic),
//                          reinterpret_cast<const jbyte*>(&intrinsic_param));
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_orbbec_obsensor_GyroFrame_nGetGyroData(JNIEnv *env, jclass clazz,
                                                jlong handle,
                                                jfloatArray jData) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  OBGyroValue gyroValue;
  memset(&gyroValue, 0, sizeof(gyroValue));
  gyroValue = ob_gyro_frame_value(frame, &error);

  jsize length = env->GetArrayLength(jData);
  jfloatArray jarr = env->NewFloatArray(length);
  jfloat *p_rst = env->GetFloatArrayElements(jarr, NULL);
  p_rst[0] = gyroValue.x;
  p_rst[1] = gyroValue.y;
  p_rst[2] = gyroValue.z;
  env->ReleaseFloatArrayElements(jarr, p_rst, 0);

  ob_handle_error(env, error);

  return jarr;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_orbbec_obsensor_GyroFrame_nGetGyroTemperature(JNIEnv *env,
                                                       jclass clazz,
                                                       jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  float temperature = ob_gyro_frame_temperature(frame, &error);
  ob_handle_error(env, error);
  return temperature;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_GyroStreamProfile_nGetGyroFullScaleRange(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_stream_profile *gyroStreamProfile =
      reinterpret_cast<ob_stream_profile *>(handle);
  auto type =
      ob_gyro_stream_profile_full_scale_range(gyroStreamProfile, &error);
  ob_handle_error(env, error);
  return (jint)type;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_GyroStreamProfile_nGetGyroSampleRate(JNIEnv *env,
                                                              jclass instance,
                                                              jlong handle) {
  ob_error *error = NULL;
  ob_stream_profile *gyroStreamProfile =
      reinterpret_cast<ob_stream_profile *>(handle);
  auto type = ob_gyro_stream_profile_sample_rate(gyroStreamProfile, &error);
  ob_handle_error(env, error);
  return (jint)type;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_GyroStreamProfile_nGetIntrinsic(JNIEnv *env,
                                                         jclass clazz,
                                                         jlong handle,
                                                         jbyteArray intrinsic) {
  ob_error *error = NULL;
  ob_stream_profile *streamProfile =
          reinterpret_cast<ob_stream_profile *>(handle);
  ob_gyro_intrinsic intrinsic_param = ob_gyro_stream_get_intrinsic(streamProfile, &error);
  ob_handle_error(env, error);

  jsize arrayLength = env->GetArrayLength(intrinsic);
  if (arrayLength < sizeof(ob_gyro_intrinsic)) {
    return;
  }

  env->SetByteArrayRegion(intrinsic, 0, sizeof(ob_gyro_intrinsic),
                          reinterpret_cast<const jbyte*>(&intrinsic_param));
}

// Recorder
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Recorder_nCreateRecorder(JNIEnv *env, jclass clazz) {
  ob_error *error = NULL;
  auto recorder = ob_create_recorder(&error);
  ob_handle_error(env, error);
  return (jlong)recorder;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Recorder_nCreateRecorderWithDevice(
    JNIEnv *env, jclass clazz, jlong deviceHandle) {
  ob_error *error = NULL;
  ob_device *device = reinterpret_cast<ob_device *>(deviceHandle);
  auto recorder = ob_create_recorder_with_device(device, &error);
  ob_handle_error(env, error);
  return (jlong)recorder;
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Recorder_nStart(
    JNIEnv *env, jclass clazz, jlong handle, jstring fileName, jboolean async) {
  ob_error *error = NULL;
  ob_recorder *recorder = reinterpret_cast<ob_recorder *>(handle);
  std::string strFileName(
      getStdString(env, fileName, "Recorder#nStart", "fileName"));
  ob_recorder_start(recorder, strFileName.c_str(), async, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Recorder_nStop(
    JNIEnv *env, jclass clazz, jlong handle) {
  ob_error *error = NULL;
  ob_recorder *recorder = reinterpret_cast<ob_recorder *>(handle);
  ob_recorder_stop(recorder, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Recorder_nWriteFrame(
    JNIEnv *env, jclass clazz, jlong handle, jlong frameHandle) {
  ob_error *error = NULL;
  ob_recorder *recorder = reinterpret_cast<ob_recorder *>(handle);
  ob_frame *frame = reinterpret_cast<ob_frame *>(frameHandle);
  ob_recorder_write_frame(recorder, frame, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Recorder_nDelete(
    JNIEnv *env, jclass clazz, jlong handle) {
  ob_error *error = NULL;
  ob_recorder *recorder = reinterpret_cast<ob_recorder *>(handle);
  ob_delete_recorder(recorder, &error);
  ob_handle_error(env, error);
}

// Playback
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Playback_nCreatePlayback(JNIEnv *env, jclass clazz,
                                                  jstring fileName) {
  ob_error *error = NULL;
  std::string strFileName(
      getStdString(env, fileName, "Playback#nCreatePlayback", "fileName"));
  auto playback = ob_create_playback(strFileName.c_str(), &error);
  ob_handle_error(env, error);
  return (jlong)playback;
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Playback_nStart(
    JNIEnv *env, jclass clazz, jlong handle, jobject callback, jint mediaType) {
  ob_error *error = NULL;
  ob_playback *playback = reinterpret_cast<ob_playback *>(handle);
  void *cookie = NULL;
  std::lock_guard<std::mutex> lk(mutex_);
  if (callback) {
    jobject gCallback = env->NewGlobalRef(callback);
    cookie = gCallback;
    gListCallback_.push_back(std::pair<jlong, jobject>(handle, gCallback));
  }
  ob_playback_start(playback, onPlaybackCallback, cookie,
                    static_cast<ob_media_type>(mediaType), &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Playback_nStop(
    JNIEnv *env, jclass clazz, jlong handle) {
  ob_error *error = NULL;
  ob_playback *playback = reinterpret_cast<ob_playback *>(handle);
  ob_playback_stop(playback, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_orbbec_obsensor_Playback_nGetDeviceInfo(JNIEnv *env, jclass clazz,
                                                 jlong handle) {
  ob_error *error = NULL;
  ob_playback *playback = reinterpret_cast<ob_playback *>(handle);
  auto deviceInfo = ob_playback_get_device_info(playback, &error);
  ob_handle_error(env, error);
  if (error) {
    LOGE("Playback_nGetDeviceInfo failed!");
    return NULL;
  }
  jobject jobjDeviceInfo = obandroid::convert_j_DeviceInfo(env, deviceInfo);
  ob_delete_device_info(deviceInfo, &error);
  return jobjDeviceInfo;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Playback_nGetCameraParam(
    JNIEnv *env, jclass clazz, jlong handle, jbyteArray depthIntr,
    jbyteArray colorIntr, jbyteArray depthDisto, jbyteArray colorDisto,
    jbyteArray trans, jobject cameraParam) {
  ob_error *error = NULL;
  ob_playback *playback = reinterpret_cast<ob_playback *>(handle);

  jclass cameraParamCls = env->GetObjectClass(cameraParam);
  jfieldID jfIsMirrored = env->GetFieldID(cameraParamCls, "mIsMirrored", "Z");

  ob_camera_param params = ob_playback_get_camera_param(playback, &error);
  ob_handle_error(env, error);

  env->SetBooleanField(cameraParam, jfIsMirrored, params.isMirrored);

  jbyte *depth_intr = env->GetByteArrayElements(depthIntr, JNI_FALSE);
  jbyte *color_intr = env->GetByteArrayElements(colorIntr, JNI_FALSE);
  jbyte *depth_disto = env->GetByteArrayElements(depthDisto, JNI_FALSE);
  jbyte *color_disto = env->GetByteArrayElements(colorDisto, JNI_FALSE);
  jbyte *transform = env->GetByteArrayElements(trans, JNI_FALSE);

  memmove(depth_intr, &params.depthIntrinsic, sizeof(params.depthIntrinsic));
  memmove(color_intr, &params.rgbIntrinsic, sizeof(params.rgbIntrinsic));
  memmove(depth_disto, &params.depthDistortion, sizeof(params.depthDistortion));
  memmove(color_disto, &params.rgbDistortion, sizeof(params.rgbDistortion));
  memmove(transform, &params.transform, sizeof(params.transform));

  env->SetByteArrayRegion(depthIntr, 0, sizeof(params.depthIntrinsic),
                          depth_intr);
  env->ReleaseByteArrayElements(depthIntr, depth_intr, 0);

  env->SetByteArrayRegion(colorIntr, 0, sizeof(params.rgbIntrinsic),
                          color_intr);
  env->ReleaseByteArrayElements(colorIntr, color_intr, 0);

  env->SetByteArrayRegion(depthDisto, 0, sizeof(params.depthDistortion),
                          depth_disto);
  env->ReleaseByteArrayElements(depthDisto, depth_disto, 0);

  env->SetByteArrayRegion(colorDisto, 0, sizeof(params.rgbDistortion),
                          color_disto);
  env->ReleaseByteArrayElements(colorDisto, color_disto, 0);

  env->SetByteArrayRegion(trans, 0, sizeof(params.transform), transform);
  env->ReleaseByteArrayElements(trans, transform, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Playback_nSetMediaStateCallback(JNIEnv *env,
                                                         jclass clazz,
                                                         jlong handle,
                                                         jobject callback) {
  ob_error *error = NULL;
  ob_playback *playback = reinterpret_cast<ob_playback *>(handle);
  void *cookie = nullptr;
  std::lock_guard<std::mutex> lk(mutex_);
  if (callback) {
    jobject gCallback = env->NewGlobalRef(callback);
    cookie = gCallback;
    gListCallback_.push_back(std::pair<jlong, jobject>(handle, gCallback));
  }
  ob_set_playback_state_callback(playback, onMediaStateCallback, cookie,
                                 &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Playback_nDelete(
    JNIEnv *env, jclass clazz, jlong handle) {
  ob_error *error = NULL;
  ob_playback *playback = reinterpret_cast<ob_playback *>(handle);
  std::vector<std::pair<jlong, jobject>>::iterator callbackIt;
  std::lock_guard<std::mutex> lk(mutex_);
  for (callbackIt = gListCallback_.begin();
       callbackIt != gListCallback_.end();) {
    if (handle == callbackIt->first) {
      env->DeleteGlobalRef(callbackIt->second);
      callbackIt = gListCallback_.erase(callbackIt);
    } else {
      callbackIt++;
    }
  }
  ob_delete_playback(playback, &error);
  ob_handle_error(env, error);
}

/**
 * CameraParamList
 */
extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_CameraParamList_nGetCameraParamCount(JNIEnv *env,
                                                              jclass clazz,
                                                              jlong handle) {
  ob_error *error = NULL;
  auto cameraParamList = reinterpret_cast<ob_camera_param_list *>(handle);
  auto count = ob_camera_param_list_count(cameraParamList, &error);
  ob_handle_error(env, error);
  return count;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_CameraParamList_nGetCameraParam(
    JNIEnv *env, jclass clazz, jlong handle, jint index, jbyteArray depthIntr,
    jbyteArray colorIntr, jbyteArray depthDisto, jbyteArray colorDisto,
    jbyteArray trans, jobject cameraParam) {
  ob_error *error = NULL;
  auto cameraParamList = reinterpret_cast<ob_camera_param_list *>(handle);
  jclass cameraParamCls = env->GetObjectClass(cameraParam);
  jfieldID jfIsMirrored = env->GetFieldID(cameraParamCls, "mIsMirrored", "Z");

  OBCameraParam param =
      ob_camera_param_list_get_param(cameraParamList, index, &error);
  ob_handle_error(env, error);

  env->SetBooleanField(cameraParam, jfIsMirrored, param.isMirrored);

  jbyte *depth_intr = env->GetByteArrayElements(depthIntr, JNI_FALSE);
  jbyte *color_intr = env->GetByteArrayElements(colorIntr, JNI_FALSE);
  jbyte *depth_disto = env->GetByteArrayElements(depthDisto, JNI_FALSE);
  jbyte *color_disto = env->GetByteArrayElements(colorDisto, JNI_FALSE);
  jbyte *transform = env->GetByteArrayElements(trans, JNI_FALSE);

  memmove(depth_intr, &param.depthIntrinsic, sizeof(param.depthIntrinsic));
  memmove(color_intr, &param.rgbIntrinsic, sizeof(param.rgbIntrinsic));
  memmove(depth_disto, &param.depthDistortion, sizeof(param.depthDistortion));
  memmove(color_disto, &param.rgbDistortion, sizeof(param.rgbDistortion));
  memmove(transform, &param.transform, sizeof(param.transform));

  env->SetByteArrayRegion(depthIntr, 0, sizeof(param.depthIntrinsic),
                          depth_intr);
  env->ReleaseByteArrayElements(depthIntr, depth_intr, 0);

  env->SetByteArrayRegion(colorIntr, 0, sizeof(param.rgbIntrinsic), color_intr);
  env->ReleaseByteArrayElements(colorIntr, color_intr, 0);

  env->SetByteArrayRegion(depthDisto, 0, sizeof(param.depthDistortion),
                          depth_disto);
  env->ReleaseByteArrayElements(depthDisto, depth_disto, 0);

  env->SetByteArrayRegion(colorDisto, 0, sizeof(param.rgbDistortion),
                          color_disto);
  env->ReleaseByteArrayElements(colorDisto, color_disto, 0);

  env->SetByteArrayRegion(trans, 0, sizeof(param.transform), transform);
  env->ReleaseByteArrayElements(trans, transform, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_CameraParamList_nDelete(JNIEnv *env, jclass clazz,
                                                 jlong handle) {
  ob_error *error = NULL;
  auto cameraParamList = reinterpret_cast<ob_camera_param_list *>(handle);
  ob_delete_camera_param_list(cameraParamList, &error);
  ob_handle_error(env, error);
}

/**
 * RecommendedFilterList
 */
extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_RecommendedFilterList_nGetFilterListCount(JNIEnv *env, jclass clazz,
                                                                   jlong handle) {
  ob_error *error = NULL;
  ob_filter_list *filterList = reinterpret_cast<ob_filter_list *>(handle);
  uint32_t count = ob_filter_list_get_count(filterList, &error);
  ob_handle_error(env, error);
  return (int) count;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_RecommendedFilterList_nGetFilter(JNIEnv *env, jclass clazz, jlong handle,
                                                          jint index) {
  ob_error *error = NULL;
  ob_filter_list *filterList = reinterpret_cast<ob_filter_list *>(handle);
  ob_filter *filter = ob_get_filter(filterList, index, &error);
  ob_handle_error(env, error);
  return (jlong) filter;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_RecommendedFilterList_nGetFilterName(JNIEnv *env, jclass clazz,
                                                              jlong handle) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);
  const char *name = ob_get_filter_name(filter, &error);
  ob_handle_error(env, error);
  uint8_t ret = ensure_utf8(name);
  if (ret) {
      return env->NewStringUTF(name);
  }
  return env->NewStringUTF("null");
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_RecommendedFilterList_nDelete(JNIEnv *env, jclass clazz,
                                                       jlong handle) {
  ob_error *error = NULL;
  auto recommendedFilterList = reinterpret_cast<ob_filter_list *>(handle);
  ob_delete_filter_list(recommendedFilterList, &error);
  ob_handle_error(env, error);
}

/**
 * CompressionFilter
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_CompressionFilter_nCreate(JNIEnv *env, jclass clazz) {
  ob_error *error = NULL;
  auto compressionFilter = ob_create_compression_filter(&error);
  ob_handle_error(env, error);
  return reinterpret_cast<jlong>(compressionFilter);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_CompressionFilter_nSetCompressionParams(
    JNIEnv *env, jclass clazz, jlong handle, jint mode, jbyteArray paramBytes) {
  ob_error *error = NULL;
  auto compressionFilter = reinterpret_cast<ob_filter *>(handle);
  jbyte *compressionParams = env->GetByteArrayElements(paramBytes, JNI_FALSE);
  ob_compression_filter_set_compression_params(
      compressionFilter, static_cast<ob_compression_mode>(mode),
      compressionParams, &error);
  env->ReleaseByteArrayElements(paramBytes, compressionParams, 0);
  ob_handle_error(env, error);
}

/**
 * DeCompressionFilter
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_DeCompressionFilter_nCreate(JNIEnv *env,
                                                     jclass clazz) {
  ob_error *error = NULL;
  auto decompressionFilter = ob_create_decompression_filter(&error);
  ob_handle_error(env, error);
  return reinterpret_cast<jlong>(decompressionFilter);
}

/**
 * FrameHelper
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_FrameHelper_nCreateFrameFromBuffer(
    JNIEnv *env, jclass clazz, jint format, jint width, jint height,
    jbyteArray buffer) {
  ob_error *error = NULL;
  jbyte *pBuffer = env->GetByteArrayElements(buffer, JNI_FALSE);
  jint bufferSize = env->GetArrayLength(buffer);
  void *cookie = nullptr;
  if (NULL != buffer) {
    cookie = env->NewGlobalRef(buffer);
  }
  auto destroyCb = [](void *destroyBuffer, void *userData) {
    JNIEnv *env;
    int envStatus = gJVM->GetEnv((void **)&env, JNI_VERSION_1_6);
    bool needDetach = false;
    if (envStatus == JNI_EDETACHED) {
      needDetach = true;
      if (gJVM->AttachCurrentThread(&env, NULL) != 0) {
        LOGE("JNI error attach current thread");
        return;
      }
    }
    if (NULL != userData) {
      env->ReleaseByteArrayElements(static_cast<jbyteArray>(userData),
                                    static_cast<jbyte *>(destroyBuffer), 0);
      env->DeleteGlobalRef(static_cast<jobject>(userData));
    }
    if (needDetach) {
      gJVM->DetachCurrentThread();
    }
  };
  auto frame =
      ob_create_frame_from_buffer(static_cast<ob_format>(format), width, height,
                                  reinterpret_cast<uint8_t *>(pBuffer),
                                  bufferSize, destroyCb, cookie, &error);
  env->ReleaseByteArrayElements(buffer, pBuffer, 0);
  ob_handle_error(env, error);
  return reinterpret_cast<jlong>(frame);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_FrameHelper_nCreateFrameSet(JNIEnv *env,
                                                     jclass clazz) {
  ob_error *error = NULL;
  auto frameSet = ob_create_frameset(&error);
  ob_handle_error(env, error);
  return reinterpret_cast<jlong>(frameSet);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_FrameHelper_nPushFrame(JNIEnv *env, jclass clazz,
                                                jlong frameSet, jint frameType,
                                                jlong frame) {
  ob_error *error = NULL;
  auto pFrameSet = reinterpret_cast<ob_frame *>(frameSet);
  auto pFrame = reinterpret_cast<ob_frame *>(frame);
  ob_frameset_push_frame(pFrameSet, static_cast<ob_frame_type>(frameType),
                         pFrame, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_FrameHelper_nSetFrameSystemTimestamp(
    JNIEnv *env, jclass clazz, jlong frame, jlong systemTimestamp) {
  ob_error *error = NULL;
  auto pFrame = reinterpret_cast<ob_frame *>(frame);
  ob_frame_set_system_time_stamp(pFrame, systemTimestamp, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_FrameHelper_nSetFrameDeviceTimestamp(
    JNIEnv *env, jclass clazz, jlong frame, jlong deviceTimestamp) {
  ob_error *error = NULL;
  auto pFrame = reinterpret_cast<ob_frame *>(frame);
  ob_frame_set_device_time_stamp(pFrame, deviceTimestamp, &error);
  ob_handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_FrameHelper_nSetFrameDeviceTimestampUs(
    JNIEnv *env, jclass clazz, jlong frame, jlong deviceTimestampUs) {
  ob_error *error = NULL;
  auto pFrame = reinterpret_cast<ob_frame *>(frame);
  ob_frame_set_device_time_stamp(pFrame, deviceTimestampUs, &error);
  ob_handle_error(env, error);
}

/**
 * CoordinateUtil
 */
extern "C" JNIEXPORT jboolean JNICALL
Java_com_orbbec_obsensor_CoordinateUtil_nIs3dTo3d(JNIEnv *env, jclass clazz,
                                                  jbyteArray calibrationParamBytes,
                                                  jbyteArray sourcePoint3fBytes,
                                                  jint sourceType, jint targetType,
                                                  jbyteArray targetPoint3fBytes) {
  ob_error *error = NULL;
  jsize cpb_size = env->GetArrayLength(calibrationParamBytes);
  jbyte *cpb_data = env->GetByteArrayElements(calibrationParamBytes, JNI_FALSE);
  if (cpb_size != sizeof(ob_camera_param)) {
    env->ReleaseByteArrayElements(calibrationParamBytes, cpb_data, 0);
    LOGE("CalibrationParam Size Error!");
    return false;
  }
  ob_calibration_param calibrationParam;
  memmove(&calibrationParam, cpb_data, sizeof(ob_calibration_param));
  env->ReleaseByteArrayElements(calibrationParamBytes, cpb_data, 0);

  jsize sp3fb_size = env->GetArrayLength(sourcePoint3fBytes);
  jbyte *sp3fb_data = env->GetByteArrayElements(sourcePoint3fBytes, JNI_FALSE);
  if (sp3fb_size != sizeof(ob_point3f)) {
    env->ReleaseByteArrayElements(sourcePoint3fBytes, sp3fb_data, 0);
    LOGE("SourcePoint3fBytes Size Error!");
    return false;
  }
  ob_point3f sourcePoint3f;
  memmove(&sourcePoint3f, sp3fb_data, sizeof(ob_point3f));
  env->ReleaseByteArrayElements(sourcePoint3fBytes, sp3fb_data, 0);

  ob_sensor_type sType = static_cast<ob_sensor_type>(sourceType);
  ob_sensor_type tType = static_cast<ob_sensor_type>(targetType);

  jsize tp3fb_size = env->GetArrayLength(targetPoint3fBytes);
  if (tp3fb_size != sizeof(ob_point3f)) {
    return false;
  }
  ob_point3f targetPoint3f;

  bool result = ob_calibration_3d_to_3d(calibrationParam, sourcePoint3f,
                                        sType, tType, &targetPoint3f, &error);
  if (result) {
    env->SetByteArrayRegion(targetPoint3fBytes, 0, sizeof(ob_point3f), reinterpret_cast<const jbyte*>(&targetPoint3f));
  }

  ob_handle_error(env, error);
  return result;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_orbbec_obsensor_CoordinateUtil_nIs2dTo3d(JNIEnv *env, jclass clazz,
                                                  jbyteArray calibrationParamBytes,
                                                  jbyteArray sourcePoint2fBytes,
                                                  jfloat sourceDepthPixel,
                                                  jint sourceType, jint targetType,
                                                  jbyteArray targetPoint3fBytes) {
  ob_error *error = NULL;
  jsize cpb_size = env->GetArrayLength(calibrationParamBytes);
  jbyte *cpb_data = env->GetByteArrayElements(calibrationParamBytes, JNI_FALSE);
  if (cpb_size != sizeof(ob_camera_param)) {
    env->ReleaseByteArrayElements(calibrationParamBytes, cpb_data, 0);
    LOGE("CalibrationParam Size Error!");
    return false;
  }
  ob_calibration_param calibrationParam;
  memmove(&calibrationParam, cpb_data, sizeof(ob_calibration_param));
  env->ReleaseByteArrayElements(calibrationParamBytes, cpb_data, 0);

  jsize sp2fb_size = env->GetArrayLength(sourcePoint2fBytes);
  jbyte *sp2fb_data = env->GetByteArrayElements(sourcePoint2fBytes, JNI_FALSE);
  if (sp2fb_size != sizeof(ob_point2f)) {
    env->ReleaseByteArrayElements(sourcePoint2fBytes, sp2fb_data, 0);
    LOGE("SourcePoint2fBytes Size Error!");
    return false;
  }
  ob_point2f sourcePoint2f;
  memmove(&sourcePoint2f, sp2fb_data, sizeof(ob_point2f));
  env->ReleaseByteArrayElements(sourcePoint2fBytes, sp2fb_data, 0);

  ob_sensor_type sType = static_cast<ob_sensor_type>(sourceType);
  ob_sensor_type tType = static_cast<ob_sensor_type>(targetType);

  jsize tp3fb_size = env->GetArrayLength(targetPoint3fBytes);
  if (tp3fb_size != sizeof(ob_point3f)) {
    return false;
  }
  ob_point3f targetPoint3f;

  bool result = ob_calibration_2d_to_3d(calibrationParam, sourcePoint2f, sourceDepthPixel,
                                        sType, tType, &targetPoint3f, &error);
  if (result) {
    env->SetByteArrayRegion(targetPoint3fBytes, 0, sizeof(ob_point3f), reinterpret_cast<const jbyte*>(&targetPoint3f));
  }

  ob_handle_error(env, error);
  return result;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_orbbec_obsensor_CoordinateUtil_nIs2dTo3dUndistortion(JNIEnv *env, jclass clazz,
                                                              jbyteArray calibrationParamBytes,
                                                              jbyteArray sourcePoint2fBytes,
                                                              jfloat sourceDepthPixel,
                                                              jint sourceType, jint targetType,
                                                              jbyteArray targetPoint3fBytes) {
  ob_error *error = NULL;
  jsize cpb_size = env->GetArrayLength(calibrationParamBytes);
  jbyte *cpb_data = env->GetByteArrayElements(calibrationParamBytes, JNI_FALSE);
  if (cpb_size != sizeof(ob_camera_param)) {
    env->ReleaseByteArrayElements(calibrationParamBytes, cpb_data, 0);
    LOGE("CalibrationParam Size Error!");
    return false;
  }
  ob_calibration_param calibrationParam;
  memmove(&calibrationParam, cpb_data, sizeof(ob_calibration_param));
  env->ReleaseByteArrayElements(calibrationParamBytes, cpb_data, 0);

  jsize sp2fb_size = env->GetArrayLength(sourcePoint2fBytes);
  jbyte *sp2fb_data = env->GetByteArrayElements(sourcePoint2fBytes, JNI_FALSE);
  if (sp2fb_size != sizeof(ob_point2f)) {
    env->ReleaseByteArrayElements(sourcePoint2fBytes, sp2fb_data, 0);
    LOGE("SourcePoint2fBytes Size Error!");
    return false;
  }
  ob_point2f sourcePoint2f;
  memmove(&sourcePoint2f, sp2fb_data, sizeof(ob_point2f));
  env->ReleaseByteArrayElements(sourcePoint2fBytes, sp2fb_data, 0);

  ob_sensor_type sType = static_cast<ob_sensor_type>(sourceType);
  ob_sensor_type tType = static_cast<ob_sensor_type>(targetType);

  jsize tp3fb_size = env->GetArrayLength(targetPoint3fBytes);
  if (tp3fb_size != sizeof(ob_point3f)) {
    return false;
  }
  ob_point3f targetPoint3f;

//  bool result = ob_calibration_2d_to_3d_undistortion(calibrationParam, sourcePoint2f, sourceDepthPixel,
//                                                     sType, tType, &targetPoint3f, &error);
//  if (result) {
//    env->SetByteArrayRegion(targetPoint3fBytes, 0, sizeof(ob_point3f), reinterpret_cast<const jbyte*>(&targetPoint3f));
//  }

  ob_handle_error(env, error);
  return false;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_orbbec_obsensor_CoordinateUtil_nIs3dTo2d(JNIEnv *env, jclass clazz,
                                                  jbyteArray calibrationParamBytes,
                                                  jbyteArray sourcePoint3fBytes, jint sourceType,
                                                  jint targetType,
                                                  jbyteArray targetPoint2fBytes) {
  ob_error *error = NULL;
  jsize cpb_size = env->GetArrayLength(calibrationParamBytes);
  jbyte *cpb_data = env->GetByteArrayElements(calibrationParamBytes, JNI_FALSE);
  if (cpb_size != sizeof(ob_camera_param)) {
    env->ReleaseByteArrayElements(calibrationParamBytes, cpb_data, 0);
    LOGE("CalibrationParam Size Error!");
    return false;
  }
  ob_calibration_param calibrationParam;
  memmove(&calibrationParam, cpb_data, sizeof(ob_camera_param));
  env->ReleaseByteArrayElements(calibrationParamBytes, cpb_data, 0);

  jsize sp3fb_size = env->GetArrayLength(sourcePoint3fBytes);
  jbyte *sp3fb_data = env->GetByteArrayElements(sourcePoint3fBytes, JNI_FALSE);
  if (sp3fb_size != sizeof(ob_point3f)) {
    env->ReleaseByteArrayElements(sourcePoint3fBytes, sp3fb_data, 0);
    LOGE("SourcePoint3fBytes Size Error!");
    return false;
  }
  ob_point3f sourcePoint3f;
  memmove(&sourcePoint3f, sp3fb_data, sizeof(ob_point3f));
  env->ReleaseByteArrayElements(sourcePoint3fBytes, sp3fb_data, 0);

  ob_sensor_type sType = static_cast<ob_sensor_type>(sourceType);
  ob_sensor_type tType = static_cast<ob_sensor_type>(targetType);

  jsize tp2fb_size = env->GetArrayLength(targetPoint2fBytes);
  if (tp2fb_size != sizeof(ob_point2f)) {
    return false;
  }
  ob_point2f targetPoint2f;

  bool result = ob_calibration_3d_to_2d(calibrationParam, sourcePoint3f,
                                        sType, tType, &targetPoint2f, &error);
  if (result) {
    env->SetByteArrayRegion(targetPoint2fBytes, 0, sizeof(ob_point2f), reinterpret_cast<const jbyte*>(&targetPoint2f));
  }

  ob_handle_error(env, error);
  return result;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_orbbec_obsensor_CoordinateUtil_nIs2dTo2d(JNIEnv *env, jclass clazz,
                                                  jbyteArray calibrationParamBytes,
                                                  jbyteArray sourcePoint2fBytes,
                                                  jfloat sourceDepthPixel,
                                                  jint sourceType, jint targetType,
                                                  jbyteArray targetPoint2fBytes) {
  ob_error *error = NULL;
  jsize cpb_size = env->GetArrayLength(calibrationParamBytes);
  jbyte *cpb_data = env->GetByteArrayElements(calibrationParamBytes, JNI_FALSE);
  if (cpb_size != sizeof(ob_camera_param)) {
    env->ReleaseByteArrayElements(calibrationParamBytes, cpb_data, 0);
    LOGE("CalibrationParam Size Error!");
    return false;
  }
  ob_calibration_param calibrationParam;
  memmove(&calibrationParam, cpb_data, sizeof(ob_camera_param));
  env->ReleaseByteArrayElements(calibrationParamBytes, cpb_data, 0);

  jsize sp2fb_size = env->GetArrayLength(sourcePoint2fBytes);
  jbyte *sp2fb_data = env->GetByteArrayElements(sourcePoint2fBytes, JNI_FALSE);
  if (sp2fb_size != sizeof(ob_point2f)) {
    env->ReleaseByteArrayElements(sourcePoint2fBytes, sp2fb_data, 0);
    LOGE("SourcePoint2fBytes Size Error!");
    return false;
  }
  ob_point2f sourcePoint2f;
  memmove(&sourcePoint2f, sp2fb_data, sizeof(ob_point2f));
  env->ReleaseByteArrayElements(sourcePoint2fBytes, sp2fb_data, 0);

  ob_sensor_type sType = static_cast<ob_sensor_type>(sourceType);
  ob_sensor_type tType = static_cast<ob_sensor_type>(targetType);

  jsize tp2fb_size = env->GetArrayLength(targetPoint2fBytes);
  if (tp2fb_size != sizeof(ob_point2f)) {
    return false;
  }
  ob_point2f targetPoint2f;

  bool result = ob_calibration_2d_to_2d(calibrationParam, sourcePoint2f, sourceDepthPixel,
                                        sType, tType, &targetPoint2f, &error);
  if (result) {
    env->SetByteArrayRegion(targetPoint2fBytes, 0, sizeof(ob_point2f), reinterpret_cast<const jbyte*>(&targetPoint2f));
  }

  ob_handle_error(env, error);
  return result;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_CoordinateUtil_nDepthFrameToColorCamera(JNIEnv *env, jclass clazz,
                                                                 jlong deviceHandle,
                                                                 jlong depthFrameHandle,
                                                                 jint width, jint height) {
  ob_error *error = NULL;
  ob_device *device = reinterpret_cast<ob_device *>(deviceHandle);
  ob_frame *depthFrame = reinterpret_cast<ob_frame *>(depthFrameHandle);
  ob_frame *colorFrame = transformation_depth_frame_to_color_camera(device, depthFrame, width, height, &error);
  ob_handle_error(env, error);
  return (jlong) colorFrame;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_orbbec_obsensor_CoordinateUtil_nInitXYTables(JNIEnv *env, jclass clazz,
                                                      jbyteArray calibrationParamBytes,
                                                      jint sensorType, jfloatArray data, jlong size,
                                                      jlong handle) {
  ob_error *error = NULL;

  jsize cpb_size = env->GetArrayLength(calibrationParamBytes);
  jbyte *cpb_data = env->GetByteArrayElements(calibrationParamBytes, JNI_FALSE);
  if (cpb_size != sizeof(ob_camera_param)) {
    env->ReleaseByteArrayElements(calibrationParamBytes, cpb_data, 0);
    LOGE("CalibrationParam Size Error!");
    return false;
  }
  ob_calibration_param calibrationParam;
  memmove(&calibrationParam, cpb_data, sizeof(ob_camera_param));
  env->ReleaseByteArrayElements(calibrationParamBytes, cpb_data, 0);

  ob_sensor_type type = static_cast<ob_sensor_type>(sensorType);

  float *data_ptr = env->GetFloatArrayElements(data, JNI_FALSE);
  uint32_t data_size = static_cast<uint32_t>(size);

  ob_xy_tables *xyTables = reinterpret_cast<ob_xy_tables *>(handle);
  bool result = transformation_init_xy_tables(calibrationParam, type, data_ptr, &data_size, xyTables, &error);

  env->ReleaseFloatArrayElements(data, data_ptr, 0);

//  jclass xyTableClass = env->FindClass("com/orbbec/obsensor/datatype/XYTables");
//
//  jfieldID xTableField = env->GetFieldID(xyTableClass, "mXTable", "[F");
//  jfloatArray xTable = env->NewFloatArray(sizeof(xy_tables.xTable));
//  env->SetFloatArrayRegion(xTable, 0, sizeof(xy_tables.xTable),
//                           xy_tables.xTable);
//  env->SetObjectField(xyTables, xTableField, xTable);
//
//  jfieldID yTableField = env->GetFieldID(xyTableClass, "mYTable", "[F");
//  jfloatArray yTable = env->NewFloatArray(sizeof(xy_tables.yTable));
//  env->SetFloatArrayRegion(yTable, 0, sizeof(xy_tables.yTable),
//                           xy_tables.yTable);
//  env->SetObjectField(xyTables, yTableField, yTable);
//
//  jfieldID widthField = env->GetFieldID(xyTableClass, "mWidth", "I");
//  env->SetIntField(xyTables, widthField, xy_tables.width);
//
//  jfieldID heightField = env->GetFieldID(xyTableClass, "mHeight", "I");
//  env->SetIntField(xyTables, heightField, xy_tables.height);

  ob_handle_error(env, error);
  return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_CoordinateUtil_nDepthToPointcloud(JNIEnv *env, jclass clazz, jlong handle,
                                                           jbyteArray depthImageData,
                                                           jbyteArray pointCloudData) {
  ob_error *error = NULL;
  ob_xy_tables *xyTables = reinterpret_cast<ob_xy_tables *>(handle);

  jbyte *depthBytes = env->GetByteArrayElements(depthImageData, JNI_FALSE);
  jbyte *pointCloudBytes = env->GetByteArrayElements(pointCloudData, JNI_FALSE);

  transformation_depth_to_pointcloud(xyTables, (const void *) depthBytes, (void *) pointCloudBytes, &error);

  env->ReleaseByteArrayElements(depthImageData, depthBytes, 0);
  env->ReleaseByteArrayElements(pointCloudData, pointCloudBytes, 0);

  ob_handle_error(env, error);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_CoordinateUtil_nDepthToRgbdPointcloud(JNIEnv *env, jclass clazz,
                                                               jlong handle,
                                                               jbyteArray depthImageData,
                                                               jbyteArray colorImageData,
                                                               jbyteArray pointCloudData) {
  ob_error *error = NULL;
  ob_xy_tables *xyTables = reinterpret_cast<ob_xy_tables *>(handle);

  jbyte *depthBytes = env->GetByteArrayElements(depthImageData, JNI_FALSE);
  jbyte *colorBytes = env->GetByteArrayElements(colorImageData, JNI_FALSE);
  jbyte *pointCloudBytes = env->GetByteArrayElements(pointCloudData, JNI_FALSE);

  transformation_depth_to_rgbd_pointcloud(xyTables, (const void *) depthBytes, (const void *) colorBytes, (void *) pointCloudBytes, &error);

  env->ReleaseByteArrayElements(depthImageData, depthBytes, 0);
  env->ReleaseByteArrayElements(colorImageData, colorBytes, 0);
  env->ReleaseByteArrayElements(pointCloudData, pointCloudBytes, 0);

  ob_handle_error(env, error);
}