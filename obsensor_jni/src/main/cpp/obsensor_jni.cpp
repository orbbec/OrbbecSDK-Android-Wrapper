// License: Apache 2.0. See LICENSE file in root directory.
// Copyright(c) 2020  Orbbec Corporation. All Rights Reserved.

/**
 * \file obsensor_jni.cpp
 * \brief libobsensor jni
 * \author chaijingjing@orbbec.com
 */

#include "obsensor_jni.h"

#include <cinttypes>
#include <cstdio>
#include <cstring>
#include <string>
#include <memory>
#include <mutex>
#include <unistd.h>
#include <android/log.h>

#include "jdatatype/DataBundle.h"
#include "jdatatype/DepthWorkMode.h"
#include "jdatatype/DeviceSyncConfig.h"
#include "utils/LocalUtils.h"
#include "libobsensor/internal/Extension.h"

#define LOG_TAG "obsensor_jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

JavaVM *gJVM;
std::vector<std::pair<jlong, jobject>> gListCallback_;
std::mutex mutex_;

static inline void handle_error(JNIEnv *env, ob_error *error) {
  // No Error
  if (!error) {
    return;
  }

  // Handle Error
  std::string message = (char *)ob_error_message(error);
  std::string function = (char *)ob_error_function(error);
  std::string args = (char *)ob_error_args(error);
  std::string errorMsg = function + "(" + args + ")#" + message;
  ob_delete_error(error);
  env->ThrowNew(env->FindClass("com/orbbec/obsensor/OBException"),
                errorMsg.c_str());
}

static inline void throw_error(JNIEnv *env, const char *function_name, const char *message) {
  std::string strFunction = (function_name ? std::string(function_name) : "");
  std::string strMessage = (message ? std::string(message) : "");
  std::string errorMsg = strFunction + "(), " + strMessage;
  env->ThrowNew(env->FindClass("com/orbbec/obsensor/OBException"),
                errorMsg.c_str());
}

static inline std::string getStdString(JNIEnv *env, jstring jText, const char *functionName, const char *paramName) {
  if (!jText) {
    std::string strParamName = (paramName ? std::string(paramName) : "");
    std::string errMsg = "Invalid argument, " + strParamName + " is null";
    throw_error(env, functionName, errMsg.c_str());
  }
  const char* szText = env->GetStringUTFChars(jText, JNI_FALSE);
  if (!szText) {
    std::string strParamName = (paramName ? std::string(paramName) : "");
    std::string errMsg = "Invalid argument, " + strParamName + " GetStringUTFChars return null";
    throw_error(env, functionName, errMsg.c_str());
  }
  if (strlen(szText) <= 0) {
    env->ReleaseStringUTFChars(jText, szText);

    std::string strParamName = (paramName ? std::string(paramName) : "");
    std::string errMsg = "Invalid argument, " + strParamName + " string is empty";
    throw_error(env, functionName, errMsg.c_str());
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
      handle_error(env, error);
      return;
    }
    needDetach = true;
  }

  if (!pCallback) {
    LOGW("onPlaybackCallback JNI callback is null...");
    ob_delete_frame(frame, &error);
    handle_error(env, error);
    if (needDetach) {
      gJVM->DetachCurrentThread();
    }
    return;
  }

  if (!isInGListCallback(pCallback)) {
    LOGI("onPlaybackCallback Global ref pCallback had already been deleted !");
    ob_delete_frame(frame, &error);
    handle_error(env, error);
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
    handle_error(env, error);
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
    handle_error(env, error);
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
      handle_error(env, error);
      return;
    }
  }

  if (!pCallback) {
    LOGD("JNI callback is null...");
    ob_delete_frame(frame, &error);
    handle_error(env, error);
    if (needDetach) {
      gJVM->DetachCurrentThread();
    }
    return;
  }

  if (!isInGListCallback(pCallback)) {
    LOGD("onFrameCallback Global ref pCallback had already been deleted !");
    ob_delete_frame(frame, &error);
    handle_error(env, error);
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
    handle_error(env, error);
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
    handle_error(env, error);
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
      handle_error(env, error);
      return;
    }
  }

  if (!pCallback) {
    LOGD("JNI callback is null...");
    ob_delete_frame(frame, &error);
    handle_error(env, error);
    return;
  }

  if (!isInGListCallback(pCallback)) {
    LOGD("onFrameSetCallback Global ref pCallback had already been deleted !");
    ob_delete_frame(frame, &error);
    handle_error(env, error);
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
    handle_error(env, error);
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
    handle_error(env, error);
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
      handle_error(env, error);
      return;
    }
  }

  if (!pCallback) {
    LOGD("JNI callback is null...");
    ob_delete_frame(frame, &error);
    handle_error(env, error);
    return;
  }

  if (!isInGListCallback(pCallback)) {
    LOGD("onFilterCallback Global ref pCallback had already been deleted !");
    ob_delete_frame(frame, &error);
    handle_error(env, error);
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
    handle_error(env, error);
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
    handle_error(env, error);
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
    handle_error(env, error);
    return;
  }

  if (!isInGListCallback(pCallback)) {
    LOGD("onDeviceChangedCallback Global ref pCallback had already been "
         "deleted !");
    ob_delete_device_list(removed, &error);
    ob_delete_device_list(added, &error);
    handle_error(env, error);
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
      handle_error(env, error);
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
    handle_error(env, error);
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
    handle_error(env, error);
    return;
  }
  if (ob_device_list_device_count(added, &error) > 0) {
    env->CallVoidMethod(jCallback, methodCallback1, (jlong)added);
  } else {
    ob_delete_device_list(added, &error);
  }
  handle_error(env, error);
  if (ob_device_list_device_count(removed, &error) > 0) {
    env->CallVoidMethod(jCallback, methodCallback2, (jlong)removed);
  } else {
    ob_delete_device_list(removed, &error);
  }
  handle_error(env, error);
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
  handle_error(env, error);
  return (jlong)context;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_OBContext_nCreateWithConfig(JNIEnv *env,
                                                     jclass typeOBContext,
                                                     jstring configPath) {
  LOGI("JNI OBContext create with config");
  ob_error *error = NULL;
  std::string strConfigPath(getStdString(env, configPath, "OBContext#nCreateWithConfig", "configPath"));
  auto context = ob_create_context_with_config(strConfigPath.c_str(), &error);
  handle_error(env, error);
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
  handle_error(env, error);
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
Java_com_orbbec_obsensor_OBContext_nQueryDevices(JNIEnv *env, jclass typeOBContext,
                                                 jlong handle) {
  ob_error *error = NULL;
  auto context = reinterpret_cast<ob_context *>(handle);
  auto deviceInfoList = ob_query_device_list(context, &error);
  handle_error(env, error);
  return (jlong)deviceInfoList;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_OBContext_nSetDeviceChangedCallback(JNIEnv *env,
                                                             jclass typeOBContext,
                                                             jlong handle,
                                                             jobject callback) {
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
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_OBContext_nEnableMultiDeviceSync(
    JNIEnv *env, jclass typeOBContext, jlong handle, jlong repeatInterval) {
  ob_error *error = NULL;
  auto context = reinterpret_cast<ob_context *>(handle);
  ob_enable_multi_device_sync(context, repeatInterval, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_OBContext_nSetLoggerSeverity(JNIEnv *env,
                                                      jclass typeOBContext,
                                                      jint logSeverity) {
  ob_error *error = NULL;
  ob_set_logger_severity(static_cast<ob_log_severity>(logSeverity), &error);
  handle_error(env, error);
}

/*
 * Class:     com_orbbec_obsensor_OBContext
 * Method:    nSetLoggerToFile
 * Signature: (ILjava/lang/String;)V
 */
extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_OBContext_nSetLoggerToFile__ILjava_lang_String_2
                                                   (JNIEnv *env,
                                                    jclass typeOBContext,
                                                    jint logSeverity,
                                                    jstring directory) {
  ob_error *error = NULL;
  std::string strFilePath(getStdString(env, directory, "nSetLoggerToFile__ILjava_lang_String_2", "directory"));
  ob_set_logger_to_file(static_cast<ob_log_severity>(logSeverity), strFilePath.c_str(), &error);
  handle_error(env, error);
}

/*
 * Class:     com_orbbec_obsensor_OBContext
 * Method:    nSetLoggerToFile
 * Signature: (ILjava/lang/String;JJ)V
 */
extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_OBContext_nSetLoggerToFile__ILjava_lang_String_2JJ
        (JNIEnv *env, jclass typeOBContext, jint logSeverity, jstring directory, jlong maxFileSize, jlong maxFileNum) {

  ob_error *error = NULL;
  std::string strFilePath(getStdString(env, directory, "nSetLoggerToFile__ILjava_lang_String_2JJ", "directory"));
  LOGI("SetLoggerToFile with file size. directory: %s, maxFileSize: %lld, maxFileNum: %lld", strFilePath.c_str(), maxFileSize, maxFileNum);
  ob_set_logger_to_rotating_file(static_cast<ob_log_severity>(logSeverity), strFilePath.c_str(), (uint32_t)maxFileSize, (uint32_t)maxFileNum, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_OBContext_nSetLoggerToConsole(JNIEnv *env,
                                                       jclass typeOBContext,
                                                       jint logSeverity) {
  ob_error *error = NULL;
  ob_set_logger_to_console(static_cast<ob_log_severity>(logSeverity), &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_OBContext_nLoadLicense(JNIEnv *env,
                                                      jclass typeOBContext,
                                                      jstring licenseFilePath,
                                                      jstring key) {
  ob_error *error = NULL;
  std::string strFilePath(getStdString(env, licenseFilePath, "OBContext#nConfigLicensePath", "licenseFilePath"));
  if (key) {
    std::string strKey(getStdString(env, key, "OBContext#nConfigLicensePath", "key"));
    ob_load_license(strFilePath.c_str(), strKey.c_str(), &error);
  } else {
    ob_load_license(strFilePath.c_str(), nullptr, &error);
  }
  handle_error(env, error);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_OBContext_nCreateNetDevice(JNIEnv *env, jclass typeOBContext,
                                                    jlong handle,
                                                    jstring address,
                                                    jint port) {
  ob_error *error = NULL;
  auto context = reinterpret_cast<ob_context *>(handle);
  std::string szAddress(getStdString(env, address, "OBContext#nCreateNetDevice", "address"));
  auto device = ob_create_net_device(context, szAddress.c_str(), port, &error);
  handle_error(env, error);
  return reinterpret_cast<jlong>(device);
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
  handle_error(env, error);
  return count;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_DeviceList_nGetDevice(JNIEnv *env, jclass typeDeviceList,
                                               jlong handle, jint index) {
  ob_error *error = NULL;
  auto deviceList = reinterpret_cast<ob_device_list *>(handle);
  auto device = ob_device_list_get_device(deviceList, index, &error);
  handle_error(env, error);
  return (jlong)device;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_DeviceList_nGetDeviceBySerialNumber(
    JNIEnv *env, jclass typeDevicList, jlong handle, jstring serialNum) {
  ob_error *error = NULL;
  auto deviceList = reinterpret_cast<ob_device_list *>(handle);
  std::string strSerialNum(getStdString(env, serialNum, "DeviceList#nGetDeviceBySerialNumber", "serialNum"));
  auto device = ob_device_list_get_device_by_serial_number(deviceList,
                                                           strSerialNum.c_str(), &error);
  handle_error(env, error);
  return (jlong)device;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_DeviceList_nGetDeviceByUid(JNIEnv *env,
                                                    jclass typeDeviceList,
                                                    jlong handle, jstring uid) {
  ob_error *error = NULL;
  auto deviceList = reinterpret_cast<ob_device_list *>(handle);
  std::string strDeviceUid(getStdString(env, uid, "DeviceList#nGetDeviceByUid", "uid"));
  auto device = ob_device_list_get_device_by_uid(deviceList, strDeviceUid.c_str(), &error);
  handle_error(env, error);
  return (jlong)device;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_DeviceList_nGetName(JNIEnv *env, jclass typeDeviceList,
                                             jlong handle, jint index) {
  ob_error *error = NULL;
  auto deviceInfoList = reinterpret_cast<ob_device_list *>(handle);
  const char *name =
      ob_device_list_get_device_name(deviceInfoList, index, &error);
  handle_error(env, error);
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
  handle_error(env, error);
  return pid;
}

extern "C" JNIEXPORT jint JNICALL Java_com_orbbec_obsensor_DeviceList_nGetVid(
    JNIEnv *env, jclass typeDeviceList, jlong handle, jint index) {
  ob_error *error = NULL;
  auto deviceInfoList = reinterpret_cast<ob_device_list *>(handle);
  auto vid = ob_device_list_get_device_vid(deviceInfoList, index, &error);
  handle_error(env, error);
  return vid;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_DeviceList_nGetUid(JNIEnv *env, jclass typeDeviceList,
                                            jlong handle, jint index) {
  ob_error *error = NULL;
  auto deviceInfoList = reinterpret_cast<ob_device_list *>(handle);
  const char *uid =
      ob_device_list_get_device_uid(deviceInfoList, index, &error);
  handle_error(env, error);
  uint8_t ret = ensure_utf8(uid);
  if (ret) {
    return env->NewStringUTF(uid);
  }
  return env->NewStringUTF("null");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_DeviceList_nGetDeviceSerialNumber(JNIEnv *env,
                                                           jclass typeDeviceList,
                                                           jlong handle,
                                                           jint index) {
  ob_error *error = NULL;
  auto deviceList = reinterpret_cast<ob_device_list *>(handle);
  const char *serialNum =
      ob_device_list_get_device_serial_number(deviceList, index, &error);
  handle_error(env, error);
  uint8_t ret = ensure_utf8(serialNum);
  if (ret) {
    return env->NewStringUTF(serialNum);
  }
  return env->NewStringUTF("null");
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_DeviceList_nDelete(
    JNIEnv *env, jclass typeDeviceList, jlong handle) {
  ob_error *error = NULL;
  auto deviceInfoList = reinterpret_cast<ob_device_list *>(handle);
  ob_delete_device_list(deviceInfoList, &error);
  handle_error(env, error);
}

/**
 * DeviceInfo
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Device_nGetDeviceInfo(JNIEnv *env, jclass typeDeviceInfo,
                                               jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto deviceInfo = ob_device_get_device_info(device, &error);
  handle_error(env, error);
  return (jlong)deviceInfo;
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_DeviceInfo_nDelete(
    JNIEnv *env, jclass typeDeviceInfo, jlong handle) {
  ob_error *error = NULL;
  auto deviceInfo = reinterpret_cast<ob_device_info *>(handle);
  ob_delete_device_info(deviceInfo, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_DeviceInfo_nGetName(JNIEnv *env, jclass typeDeviceInfo,
                                             jlong handle) {
  ob_error *error = NULL;
  auto deviceInfo = reinterpret_cast<ob_device_info *>(handle);
  const char *name = ob_device_info_name(deviceInfo, &error);
  handle_error(env, error);
  uint8_t ret = ensure_utf8(name);
  if (ret) {
    return env->NewStringUTF(name);
  }
  return env->NewStringUTF("null");
}

extern "C" JNIEXPORT jint JNICALL Java_com_orbbec_obsensor_DeviceInfo_nGetPid(
    JNIEnv *env, jclass typeDeviceInfo, jlong handle) {
  ob_error *error = NULL;
  auto deviceInfo = reinterpret_cast<ob_device_info *>(handle);
  auto pid = ob_device_info_pid(deviceInfo, &error);
  handle_error(env, error);
  return pid;
}

extern "C" JNIEXPORT jint JNICALL Java_com_orbbec_obsensor_DeviceInfo_nGetVid(
    JNIEnv *env, jclass typeDeviceInfo, jlong handle) {
  ob_error *error = NULL;
  auto deviceInfo = reinterpret_cast<ob_device_info *>(handle);
  auto vid = ob_device_info_vid(deviceInfo, &error);
  handle_error(env, error);
  return vid;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_DeviceInfo_nGetUid(JNIEnv *env, jclass typeDeviceInfo,
                                            jlong handle) {
  ob_error *error = NULL;
  auto deviceInfo = reinterpret_cast<ob_device_info *>(handle);
  const char *uid = ob_device_info_uid(deviceInfo, &error);
  handle_error(env, error);
  uint8_t ret = ensure_utf8(uid);
  if (ret) {
    return env->NewStringUTF(uid);
  }
  return env->NewStringUTF("null");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_DeviceInfo_nGetSerialNumber(JNIEnv *env,
                                                     jclass typeDeviceInfo,
                                                     jlong handle) {
  ob_error *error = NULL;
  auto deviceInfo = reinterpret_cast<ob_device_info *>(handle);
  const char *serialNumber = ob_device_info_serial_number(deviceInfo, &error);
  handle_error(env, error);
  uint8_t ret = ensure_utf8(serialNumber);
  if (ret) {
    return env->NewStringUTF(serialNumber);
  }
  return env->NewStringUTF("null");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_DeviceInfo_nGetUsbType(JNIEnv *env, jclass typeDeviceInfo,
                                                jlong handle) {
  ob_error *error = NULL;
  auto deviceInfo = reinterpret_cast<ob_device_info *>(handle);
  const char *usbType = ob_device_info_usb_type(deviceInfo, &error);
  handle_error(env, error);
  uint8_t ret = ensure_utf8(usbType);
  if (ret) {
    return env->NewStringUTF(usbType);
  }
  return env->NewStringUTF("null");
}

/*
 * Class:     com_orbbec_obsensor_DeviceInfo
 * Method:    nGetConnectionType
 * Signature: (J)Ljava/lang/String;
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_DeviceInfo_nGetConnectionType(JNIEnv *env,
                                                       jclass typeDeviceInfo,
                                                       jlong handle) {
  ob_error *error = NULL;
  auto deviceInfo = reinterpret_cast<ob_device_info *>(handle);
  const char *connectionType =
      ob_device_info_connection_type(deviceInfo, &error);
  handle_error(env, error);
  uint8_t ret = ensure_utf8(connectionType);
  if (ret) {
    return env->NewStringUTF(connectionType);
  }
  return env->NewStringUTF("null");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_DeviceInfo_nGetFirmwareVersion(JNIEnv *env,
                                                        jclass typeDeviceInfo,
                                                        jlong handle) {
  ob_error *error = NULL;
  auto deviceInfo = reinterpret_cast<ob_device_info *>(handle);
  const char *firmwareVer = ob_device_info_firmware_version(deviceInfo, &error);
  handle_error(env, error);
  uint8_t ret = ensure_utf8(firmwareVer);
  if (ret) {
    return env->NewStringUTF(firmwareVer);
  }
  return env->NewStringUTF("null");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_DeviceInfo_nGetHardwareVersion(JNIEnv *env,
                                                        jclass typeDeviceInfo,
                                                        jlong handle) {
  ob_error *error = NULL;
  auto deviceInfo = reinterpret_cast<ob_device_info *>(handle);
  const char *hardwareVer = ob_device_info_hardware_version(deviceInfo, &error);
  handle_error(env, error);
  uint8_t ret = ensure_utf8(hardwareVer);
  if (ret) {
    return env->NewStringUTF(hardwareVer);
  }
  return env->NewStringUTF("null");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_DeviceInfo_nGetSupportedMinSdkVersion(JNIEnv *env,
                                                               jclass typeDeviceInfo,
                                                               jlong handle) {
  ob_error *error = NULL;
  auto deviceInfo = reinterpret_cast<ob_device_info *>(handle);
  const char *sdkVer =
      ob_device_info_supported_min_sdk_version(deviceInfo, &error);
  handle_error(env, error);
  uint8_t ret = ensure_utf8(sdkVer);
  if (ret) {
    return env->NewStringUTF(sdkVer);
  }
  return env->NewStringUTF("null");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orbbec_obsensor_DeviceInfo_nGetAsicName(JNIEnv *env, jclass typeDeviceInfo,
                                                 jlong handle) {
  ob_error *error = NULL;
  auto deviceInfo = reinterpret_cast<ob_device_info *>(handle);
  const char *asicName = ob_device_info_asicName(deviceInfo, &error);
  handle_error(env, error);
  uint8_t ret = ensure_utf8(asicName);
  if (ret) {
    return env->NewStringUTF(asicName);
  }
  return env->NewStringUTF("null");
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_DeviceInfo_nGetDeviceType(JNIEnv *env, jclass typeDevice,
                                                   jlong handle) {
  ob_error *error = NULL;
  auto deviceInfo = reinterpret_cast<ob_device_info *>(handle);
  auto deviceType = ob_device_info_device_type(deviceInfo, &error);
  handle_error(env, error);
  return deviceType;
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
  handle_error(env, error);
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
  handle_error(env, error);
  return obandroid::convert_j_DepthWorkMode(env, depthWorkMode);
}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nSwitchDepthWorkMode
 * Signature: (JLjava/lang/String;)V
 */
extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nSwitchDepthWorkMode(JNIEnv *env, jclass typeDevice,
                                                     jlong handle,
                                                     jstring modeName) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  std::string strModeName(getStdString(env, modeName, "Device#nSwitchDepthWorkMode", "modeName"));
  ob_device_switch_depth_work_mode_by_name(device, strModeName.c_str(), &error);
  handle_error(env, error);
}

/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nGetDepthWorkModeList
 * Signature: (J)Ljava/util/List;
 */
extern "C" JNIEXPORT jobject JNICALL
Java_com_orbbec_obsensor_Device_nGetDepthWorkModeList(JNIEnv *env, jclass typeDevice,
                                                      jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto workModeList = ob_device_get_depth_work_mode_list(device, &error);
  handle_error(env, error);

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
    handle_error(env, error);
    return NULL;
  }

  for (int i = 0; i < modeCount; i++) {
    auto workMode = ob_depth_work_mode_list_get_item(workModeList, i, &error);
    handle_error(env, error);
    auto jWorkMode = obandroid::convert_j_DepthWorkMode(env, workMode);
    if (jWorkMode) {
      env->CallBooleanMethod(modeArrayList, methodListAdd, jWorkMode);
    }
  }
  ob_delete_depth_work_mode_list(workModeList, &error);
  handle_error(env, error);

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
  handle_error(env, error);

  ob_data_bundle *dataBundle = ob_device_get_structured_data_ext(
      device, OB_STRUCT_DEPTH_PRECISION_SUPPORT_LIST, &error);
  handle_error(env, error);
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
  handle_error(env, error);
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
  handle_error(env, error);
  return isSupported;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nGetPropertyRangeB(JNIEnv *env, jclass typeDevice,
                                                   jlong handle,
                                                   jint propertyId,
                                                   jobject propertyRange) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto range = ob_device_get_bool_property_range(
      device, static_cast<ob_property_id>(propertyId), &error);
  handle_error(env, error);
  if (error) {
    return;
  }
  n2jPropertyRangeB(env, range, propertyRange);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nGetPropertyRangeI(JNIEnv *env, jclass typeDevice,
                                                   jlong handle,
                                                   jint propertyId,
                                                   jobject propertyRange) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto range = ob_device_get_int_property_range(
      device, static_cast<ob_property_id>(propertyId), &error);
  handle_error(env, error);
  if (error) {
    return;
  }
  n2jPropertyRangeI(env, range, propertyRange);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nGetPropertyRangeF(JNIEnv *env, jclass typeDevice,
                                                   jlong handle,
                                                   jint propertyId,
                                                   jobject propertyRange) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto range = ob_device_get_float_property_range(
      device, static_cast<ob_property_id>(propertyId), &error);
  handle_error(env, error);
  if (error) {
    return;
  }
  n2jPropertyRangeF(env, range, propertyRange);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nSetPropertyValueB(JNIEnv *env, jclass typeDevice,
                                                   jlong handle,
                                                   jint propertyId,
                                                   jboolean value) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  ob_device_set_bool_property(device, static_cast<ob_property_id>(propertyId),
                              value, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nSetPropertyValueI(JNIEnv *env, jclass typeDevice,
                                                   jlong handle,
                                                   jint propertyId,
                                                   jint value) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  ob_device_set_int_property(device, static_cast<ob_property_id>(propertyId),
                             value, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nSetPropertyValueF(JNIEnv *env, jclass typeDevice,
                                                   jlong handle,
                                                   jint propertyId,
                                                   jfloat value) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  ob_device_set_float_property(device, static_cast<ob_property_id>(propertyId),
                               value, &error);
  handle_error(env, error);
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
  handle_error(env, error);
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
Java_com_orbbec_obsensor_Device_nGetPropertyValueB(JNIEnv *env, jclass typeDevice,
                                                   jlong handle,
                                                   jint propertyId) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto value = ob_device_get_bool_property(
      device, static_cast<ob_property_id>(propertyId), &error);
  handle_error(env, error);
  return value;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_Device_nGetPropertyValueI(JNIEnv *env, jclass typeDevice,
                                                   jlong handle,
                                                   jint propertyId) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto value = ob_device_get_int_property(
      device, static_cast<ob_property_id>(propertyId), &error);
  handle_error(env, error);
  return value;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_orbbec_obsensor_Device_nGetPropertyValueF(JNIEnv *env, jclass typeDevice,
                                                   jlong handle,
                                                   jint propertyId) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto value = ob_device_get_float_property(
      device, static_cast<ob_property_id>(propertyId), &error);
  handle_error(env, error);
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
  handle_error(env, error);
}

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
  handle_error(env, error);
  if (cdata_bundle) {
    jobject ret = obandroid::convert_j_DataBundle(env, cdata_bundle);
    ob_delete_data_bundle(cdata_bundle, &error);
    handle_error(env, error);
    return ret;
  }
  throw_error(env, "nGetPropertyValueDataTypeExt",
              "Invoke get_structured_data_ext return NULL");
  return NULL;
}

extern "C" JNIEXPORT jlong JNICALL Java_com_orbbec_obsensor_Device_nGetSensor(
    JNIEnv *env, jclass typeDevice, jlong handle, jint sensorType) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto sensor = ob_device_get_sensor(
      device, static_cast<ob_sensor_type>(sensorType), &error);
  handle_error(env, error);
  return (jlong)sensor;
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Device_nUpgrade(
    JNIEnv *env, jclass typeDevice, jlong handle, jstring fileName,
    jobject callback) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  std::string strFileName(getStdString(env, fileName, "Device#nUpgrade", "fileName"));
  void *cookie = NULL;
  std::lock_guard<std::mutex> lk(mutex_);
  if (callback) {
    jobject gCallback = env->NewGlobalRef(callback);
    cookie = gCallback;
    gListCallback_.push_back(std::pair<jlong, jobject>(handle, gCallback));
  }
  ob_device_upgrade(device, strFileName.c_str(), onUpgradeCallback, true, cookie, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nSendFileToDestination(
    JNIEnv *env, jclass typeDevice, jlong handle, jstring filePath,
    jstring dstPath, jobject callback) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  std::string strSrcFile(getStdString(env, filePath, "Device#nSendFileToDestination", "filePath"));
  std::string strDstFile(getStdString(env, dstPath, "Device#nSendFileToDestination", "dstPath"));
  void *cookie = NULL;
  std::lock_guard<std::mutex> lk(mutex_);
  if (callback) {
    jobject gCallback = env->NewGlobalRef(callback);
    cookie = gCallback;
    gListCallback_.push_back(std::pair<jlong, jobject>(handle, gCallback));
  }
  ob_device_send_file_to_destination(device, strSrcFile.c_str(), strDstFile.c_str(),
                                     onFileSendCallback, true, cookie, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_orbbec_obsensor_Device_nActivateAuthorization(JNIEnv *env,
                                                       jclass typeDevice,
                                                       jlong handle,
                                                       jstring authCode) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  std::string strAuthCode(getStdString(env, authCode, "Device#nActivateAuthorization", "authCode"));
  auto activate = ob_device_activate_authorization(device, strAuthCode.c_str(), &error);
  handle_error(env, error);
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
  handle_error(env, error);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Device_nSyncDeviceTime(JNIEnv *env, jclass typeDevice,
                                                jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto rtt = ob_device_sync_device_time(device, &error);
  handle_error(env, error);
  return (jlong)rtt;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Device_nGetCalibrationCameraParamList(JNIEnv *env,
                                                               jclass typeDevice,
                                                               jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto cameraParamList =
      ob_device_get_calibration_camera_param_list(device, &error);
  handle_error(env, error);
  return (jlong)cameraParamList;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_orbbec_obsensor_Device_nGetMultiDeviceSyncConfig(JNIEnv *env,
                                                          jclass typeDevice,
                                                          jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  OBDeviceSyncConfig syncConfig = ob_device_get_sync_config(device, &error);
  handle_error(env, error);
  return obandroid::convert_j_DeviceSyncConfig(env, syncConfig);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Device_nSetMultiDeviceSyncConfig(
    JNIEnv *env, jclass typeDevice, jlong handle, jobject jdeviceSyncConfig) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  OBDeviceSyncConfig syncConfig =
      obandroid::convert_c_DeviceSyncConfig(env, jdeviceSyncConfig);
  ob_device_set_sync_config(device, syncConfig, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Device_nReboot(
    JNIEnv *env, jclass typeDevice, jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  ob_device_reboot(device, &error);
  handle_error(env, error);
}


/*
 * Class:     com_orbbec_obsensor_Device
 * Method:    nRebootDelayMode
 * Signature: (JI)V
 */
extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Device_nRebootDelayMode
        (JNIEnv *env, jclass typeDevice, jlong handle, jint delayTimeMs) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  ob_device_reboot_delay_mode(device, static_cast<uint32_t>(delayTimeMs), &error);
  handle_error(env, error);
}

/**
 * Sensor
 */
extern "C" JNIEXPORT jint JNICALL Java_com_orbbec_obsensor_Sensor_nGetType(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_sensor *sensor = reinterpret_cast<ob_sensor *>(handle);
  auto type = ob_sensor_get_type(sensor, &error);
  handle_error(env, error);
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
  handle_error(env, error);
  return (jlong)streamProfiles;
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
  handle_error(env, error);
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
  handle_error(env, error);
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
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Sensor_nDelete(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_sensor *sensor = reinterpret_cast<ob_sensor *>(handle);
  ob_delete_sensor(sensor, &error);
  handle_error(env, error);
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
  handle_error(env, error);
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
  handle_error(env, error);
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
  handle_error(env, error);
  return height;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_StreamProfile_nGetFormat(JNIEnv *env, jclass instance,
                                                  jlong handle) {
  ob_error *error = NULL;
  ob_stream_profile *streamProfile =
      reinterpret_cast<ob_stream_profile *>(handle);
  auto format = ob_stream_profile_format(streamProfile, &error);
  handle_error(env, error);
  return (jint)format;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_StreamProfile_nGetType(JNIEnv *env, jclass instance,
                                                jlong handle) {
  ob_error *error = NULL;
  ob_stream_profile *streamProfile =
      reinterpret_cast<ob_stream_profile *>(handle);
  auto type = ob_stream_profile_type(streamProfile, &error);
  handle_error(env, error);
  return (jint)type;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_StreamProfile_nDelete(JNIEnv *env, jclass instance,
                                               jlong handle) {
  ob_error *error = NULL;
  ob_stream_profile *streamProfile =
      reinterpret_cast<ob_stream_profile *>(handle);
  ob_delete_stream_profile(streamProfile, &error);
  handle_error(env, error);
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
  handle_error(env, error);
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
  handle_error(env, error);
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
  handle_error(env, error);
  return (jlong)videoStreamProfile;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_StreamProfileList_nDelete(JNIEnv *env, jclass instance,
                                                   jlong handle) {
  ob_error *error = NULL;
  ob_stream_profile_list *streamProfileList =
      reinterpret_cast<ob_stream_profile_list *>(handle);
  ob_delete_stream_profile_list(streamProfileList, &error);
  handle_error(env, error);
}

/**
 * Frame
 */
extern "C" JNIEXPORT jlong JNICALL Java_com_orbbec_obsensor_Frame_nGetIndex(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto index = ob_frame_index(frame, &error);
  handle_error(env, error);
  return index;
}

extern "C" JNIEXPORT jint JNICALL Java_com_orbbec_obsensor_VideoFrame_nGetWidth(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto width = ob_video_frame_width(frame, &error);
  handle_error(env, error);
  return width;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_VideoFrame_nGetHeight(JNIEnv *env, jclass instance,
                                               jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto height = ob_video_frame_height(frame, &error);
  handle_error(env, error);
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
  handle_error(env, error);
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
  handle_error(env, error);
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
  handle_error(env, error);
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
  handle_error(env, error);

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
  handle_error(env, error);
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
  handle_error(env, error);

  auto size = ob_frame_data_size(frame, &error);
  handle_error(env, error);

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
  handle_error(env, error);
  return size;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_orbbec_obsensor_Frame_nGetDirectBuffer(JNIEnv *env, jclass instance,
                                                jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto size = ob_frame_data_size(frame, &error);
  auto data = ob_frame_data(frame, &error);
  handle_error(env, error);
  return env->NewDirectByteBuffer(data, size);
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
  handle_error(env, error);
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
  auto metadata = ob_video_frame_metadata(frame, &error);
  jsize length = env->GetArrayLength(jData);
  env->SetByteArrayRegion(jData, 0, length,
                          reinterpret_cast<const jbyte *>(metadata));
  handle_error(env, error);
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
  auto size = ob_video_frame_metadata_size(frame, &error);
  handle_error(env, error);
  return size;
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
  handle_error(env, error);
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
  handle_error(env, error);
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_orbbec_obsensor_DepthFrame_nGetValueScale(JNIEnv *env, jclass instance,
                                                   jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  auto scale = ob_depth_frame_get_value_scale(frame, &error);
  handle_error(env, error);
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
  handle_error(env, error);
  return size;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_FrameSet_nGetDepthFrame(JNIEnv *env, jclass instance,
                                                 jlong handle) {
  ob_error *error = NULL;
  ob_frame *frameSet = reinterpret_cast<ob_frame *>(handle);
  auto frame = ob_frameset_depth_frame(frameSet, &error);
  handle_error(env, error);
  return (jlong)frame;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_FrameSet_nGetColorFrame(JNIEnv *env, jclass instance,
                                                 jlong handle) {
  ob_error *error = NULL;
  ob_frame *frameSet = reinterpret_cast<ob_frame *>(handle);
  auto frame = ob_frameset_color_frame(frameSet, &error);
  handle_error(env, error);
  return (jlong)frame;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_FrameSet_nGetInfraredFrame(JNIEnv *env,
                                                    jclass instance,
                                                    jlong handle) {
  ob_error *error = NULL;
  ob_frame *frameSet = reinterpret_cast<ob_frame *>(handle);
  auto frame = ob_frameset_ir_frame(frameSet, &error);
  handle_error(env, error);
  return (jlong)frame;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_FrameSet_nGetPointFrame(JNIEnv *env, jclass instance,
                                                 jlong handle) {
  ob_error *error = NULL;
  ob_frame *frameSet = reinterpret_cast<ob_frame *>(handle);
  auto frame = ob_frameset_points_frame(frameSet, &error);
  handle_error(env, error);
  return (jlong)frame;
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
  handle_error(env, error);
  return (long)frame;
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_FrameSet_nDelete(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_frame *frameSet = reinterpret_cast<ob_frame *>(handle);
  ob_delete_frame(frameSet, &error);
  handle_error(env, error);
}

/**
 * Config
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Config_nCreate(JNIEnv *env, jclass instance) {
  ob_error *error = NULL;
  ob_config *config = ob_create_config(&error);
  handle_error(env, error);
  return (jlong)config;
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Config_nDelete(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_config *config = reinterpret_cast<ob_config *>(handle);
  ob_delete_config(config, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Config_nEnableStream(
    JNIEnv *env, jclass instance, jlong handle, jlong streamProfileHandle) {
  ob_error *error = NULL;
  ob_config *config = reinterpret_cast<ob_config *>(handle);
  ob_stream_profile *streamProfile =
      reinterpret_cast<ob_stream_profile *>(streamProfileHandle);
  ob_config_enable_stream(config, streamProfile, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Config_nEnableAllStream(JNIEnv *env, jclass instance,
                                                 jlong handle) {
  ob_error *error = NULL;
  ob_config *config = reinterpret_cast<ob_config *>(handle);
  ob_config_enable_all_stream(config, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Config_nDisableStream(JNIEnv *env, jclass instance,
                                               jlong handle, jint streamType) {
  ob_error *error = NULL;
  ob_config *config = reinterpret_cast<ob_config *>(handle);
  ob_config_disable_stream(config, static_cast<ob_stream_type>(streamType),
                           &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Config_nSetAlignMode(
    JNIEnv *env, jclass instance, jlong handle, jint mode) {
  ob_error *error = NULL;
  auto config = reinterpret_cast<ob_config *>(handle);
  ob_config_set_align_mode(config, static_cast<ob_align_mode>(mode), &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Config_nSetDepthScaleRequire(JNIEnv *env,
                                                      jclass instance,
                                                      jlong handle,
                                                      jboolean enable) {
  ob_error *error = NULL;
  auto config = reinterpret_cast<ob_config *>(handle);
  ob_config_set_depth_scale_require(config, enable, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Config_nSetD2CTargetResolution(
    JNIEnv *env, jclass instance, jlong handle, jint width, jint height) {
  ob_error *error = NULL;
  auto config = reinterpret_cast<ob_config *>(handle);
  ob_config_set_d2c_target_resolution(config, width, height, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Config_nDisableAllStream(JNIEnv *env, jclass instance,
                                                  jlong handle) {
  ob_error *error = NULL;
  ob_config *config = reinterpret_cast<ob_config *>(handle);
  ob_config_disable_all_stream(config, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Pipeline_nCreateWithDevice(JNIEnv *env,
                                                    jclass instance,
                                                    jlong deviceHandle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(deviceHandle);
  ob_pipeline *pipeline = ob_create_pipeline_with_device(device, &error);
  handle_error(env, error);
  return (jlong)pipeline;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Pipeline_nCreateWithPlaybackFile(JNIEnv *env,
                                                          jclass instance,
                                                          jstring filePath) {
  ob_error *error = NULL;
  std::string strPlaybackFile(getStdString(env, filePath, "Pipeline#nCreateWithPlaybackFile", "filePath"));
  auto pipeline = ob_create_pipeline_with_playback_file(strPlaybackFile.c_str(), &error);
  handle_error(env, error);
  return (jlong)pipeline;
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Pipeline_nDelete(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_delete_pipeline(pipeline, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Pipeline_nStartWithConfig(JNIEnv *env, jclass instance,
                                                   jlong handle,
                                                   jlong configHandle) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_config *config = reinterpret_cast<ob_config *>(configHandle);
  ob_pipeline_start_with_config(pipeline, config, &error);
  handle_error(env, error);
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
  handle_error(env, error);
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
  handle_error(env, error);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Pipeline_nWaitForFrameSet(JNIEnv *env, jclass instance,
                                                   jlong handle,
                                                   jlong timeout) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_frame *frameSet = ob_pipeline_wait_for_frameset(pipeline, timeout, &error);
  handle_error(env, error);
  return (jlong)frameSet;
}

extern "C" JNIEXPORT jlong JNICALL Java_com_orbbec_obsensor_Pipeline_nGetConfig(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_config *config = ob_pipeline_get_config(pipeline, &error);
  handle_error(env, error);
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
  handle_error(env, error);
  return (jlong)streamProfiles;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Pipeline_nEnableFrameSync(JNIEnv *env, jclass instance,
                                                   jlong handle) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_pipeline_enable_frame_sync(pipeline, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Pipeline_nDisableFrameSync(JNIEnv *env,
                                                    jclass instance,
                                                    jlong handle) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_pipeline_disable_frame_sync(pipeline, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_Pipeline_nSwitchConfig(JNIEnv *env, jclass instance,
                                                jlong handle,
                                                jlong configHandle) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_config *config = reinterpret_cast<ob_config *>(configHandle);
  ob_pipeline_switch_config(pipeline, config, &error);
  handle_error(env, error);
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
  handle_error(env, error);
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
  handle_error(env, error);
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
  handle_error(env, error);
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
  handle_error(env, error);

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
  std::string strFilePath(getStdString(env, filePath, "Pipeline#nStartRecord", "filePath"));
  ob_pipeline_start_record(pipeline, strFilePath.c_str(), &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Pipeline_nStopRecord(
    JNIEnv *env, jclass instance, jlong handle) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  ob_pipeline_stop_record(pipeline, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Pipeline_nGetPlayback(JNIEnv *env, jclass instance,
                                               jlong handle) {
  ob_error *error = NULL;
  ob_pipeline *pipeline = reinterpret_cast<ob_pipeline *>(handle);
  auto playback = ob_pipeline_get_playback(pipeline, &error);
  handle_error(env, error);
  return (jlong)playback;
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
  handle_error(env, error);
}

extern "C" JNIEXPORT jlong JNICALL Java_com_orbbec_obsensor_Filter_nProcess(
    JNIEnv *env, jclass clazz, jlong handle, jlong frameHandle) {
  ob_error *error = NULL;
  auto filter = reinterpret_cast<ob_filter *>(handle);
  auto frame = reinterpret_cast<ob_frame *>(frameHandle);
  auto processFrame = ob_filter_process(filter, frame, &error);
  handle_error(env, error);
  return (jlong)processFrame;
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Filter_nReset(
    JNIEnv *env, jclass clazz, jlong handle) {
  ob_error *error = NULL;
  auto filter = reinterpret_cast<ob_filter *>(handle);
  ob_filter_reset(filter, &error);
  handle_error(env, error);
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
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Filter_nPushFrame(
    JNIEnv *env, jclass clazz, jlong handle, jlong frameHandle) {
  ob_error *error = NULL;
  auto filter = reinterpret_cast<ob_filter *>(handle);
  auto frame = reinterpret_cast<ob_frame *>(frameHandle);
  ob_filter_push_frame(filter, frame, &error);
  handle_error(env, error);
}

/**
 * PointCloudFilter
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_PointCloudFilter_nCreate(JNIEnv *env, jclass clazz) {
  ob_error *error = NULL;
  auto filter = ob_create_pointcloud_filter(&error);
  handle_error(env, error);
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
  handle_error(env, error);
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

  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_PointCloudFilter_nSetD2CAlignStatus(
    JNIEnv *env, jclass clazz, jboolean d2c_status, jlong filter_ptr) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(filter_ptr);

  ob_pointcloud_filter_set_frame_align_state(filter, d2c_status, &error);

  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_PointCloudFilter_nSetPositionDataScale(JNIEnv *env,
                                                                jclass clazz,
                                                                jlong handle,
                                                                jfloat scale) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);

  ob_pointcloud_filter_set_position_data_scale(filter, scale, &error);

  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_PointCloudFilter_nSetColorDataNormalization(
    JNIEnv *env, jclass clazz, jlong handle, jboolean state) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(handle);

  ob_pointcloud_filter_set_color_data_normalization(filter, state, &error);

  handle_error(env, error);
}

/**
 * FormatConvertFilter
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_FormatConvertFilter_nCreate(JNIEnv *env,
                                                     jclass instance) {
  ob_error *error = NULL;
  auto filter = ob_create_format_convert_filter(&error);
  handle_error(env, error);
  return (jlong)filter;
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_FormatConvertFilter_nSetFormatConvertType(
    JNIEnv *env, jclass clazz, jint type, jlong filter_ptr) {
  ob_error *error = NULL;
  ob_filter *filter = reinterpret_cast<ob_filter *>(filter_ptr);
  ob_format_convert_filter_set_format(
      filter, static_cast<ob_convert_format>(type), &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_Device_nGetSupportedPropertyCount(JNIEnv *env,
                                                           jclass instance,
                                                           jlong handle) {
  ob_error *error = NULL;
  auto device = reinterpret_cast<ob_device *>(handle);
  auto count = ob_device_get_supported_property_count(device, &error);
  handle_error(env, error);
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

  handle_error(env, error);
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

  handle_error(env, error);

  return jarr;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_orbbec_obsensor_AccelFrame_nGetAccelTemperature(JNIEnv *env,
                                                         jclass clazz,
                                                         jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  float temperature = ob_accel_frame_temperature(frame, &error);
  handle_error(env, error);
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
  handle_error(env, error);
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
  handle_error(env, error);
  return (jint)type;
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

  handle_error(env, error);

  return jarr;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_orbbec_obsensor_GyroFrame_nGetGyroTemperature(JNIEnv *env,
                                                       jclass clazz,
                                                       jlong handle) {
  ob_error *error = NULL;
  ob_frame *frame = reinterpret_cast<ob_frame *>(handle);
  float temperature = ob_gyro_frame_temperature(frame, &error);
  handle_error(env, error);
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
  handle_error(env, error);
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
  handle_error(env, error);
  return (jint)type;
}

// Recorder
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Recorder_nCreateRecorder(JNIEnv *env, jclass clazz) {
  ob_error *error = NULL;
  auto recorder = ob_create_recorder(&error);
  handle_error(env, error);
  return (jlong)recorder;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Recorder_nCreateRecorderWithDevice(
    JNIEnv *env, jclass clazz, jlong deviceHandle) {
  ob_error *error = NULL;
  ob_device *device = reinterpret_cast<ob_device *>(deviceHandle);
  auto recorder = ob_create_recorder_with_device(device, &error);
  handle_error(env, error);
  return (jlong)recorder;
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Recorder_nStart(
    JNIEnv *env, jclass clazz, jlong handle, jstring fileName, jboolean async) {
  ob_error *error = NULL;
  ob_recorder *recorder = reinterpret_cast<ob_recorder *>(handle);
  std::string strFileName(getStdString(env, fileName, "Recorder#nStart", "fileName"));
  ob_recorder_start(recorder, strFileName.c_str(), async, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Recorder_nStop(
    JNIEnv *env, jclass clazz, jlong handle) {
  ob_error *error = NULL;
  ob_recorder *recorder = reinterpret_cast<ob_recorder *>(handle);
  ob_recorder_stop(recorder, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Recorder_nWriteFrame(
    JNIEnv *env, jclass clazz, jlong handle, jlong frameHandle) {
  ob_error *error = NULL;
  ob_recorder *recorder = reinterpret_cast<ob_recorder *>(handle);
  ob_frame *frame = reinterpret_cast<ob_frame *>(frameHandle);
  ob_recorder_write_frame(recorder, frame, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Recorder_nDelete(
    JNIEnv *env, jclass clazz, jlong handle) {
  ob_error *error = NULL;
  ob_recorder *recorder = reinterpret_cast<ob_recorder *>(handle);
  ob_delete_recorder(recorder, &error);
  handle_error(env, error);
}

// Playback
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Playback_nCreatePlayback(JNIEnv *env, jclass clazz,
                                                  jstring fileName) {
  ob_error *error = NULL;
  std::string strFileName(getStdString(env, fileName, "Playback#nCreatePlayback", "fileName"));
  auto playback = ob_create_playback(strFileName.c_str(), &error);
  handle_error(env, error);
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
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL Java_com_orbbec_obsensor_Playback_nStop(
    JNIEnv *env, jclass clazz, jlong handle) {
  ob_error *error = NULL;
  ob_playback *playback = reinterpret_cast<ob_playback *>(handle);
  ob_playback_stop(playback, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_Playback_nGetDeviceInfo(JNIEnv *env, jclass clazz,
                                                 jlong handle) {
  ob_error *error = NULL;
  ob_playback *playback = reinterpret_cast<ob_playback *>(handle);
  auto deviceInfo = ob_playback_get_device_info(playback, &error);
  handle_error(env, error);
  return (jlong)deviceInfo;
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
  handle_error(env, error);

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
  handle_error(env, error);
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
  handle_error(env, error);
}

// CameraParamList
extern "C" JNIEXPORT jint JNICALL
Java_com_orbbec_obsensor_CameraParamList_nGetCameraParamCount(JNIEnv *env,
                                                              jclass clazz,
                                                              jlong handle) {
  ob_error *error = NULL;
  auto cameraParamList = reinterpret_cast<ob_camera_param_list *>(handle);
  auto count = ob_camera_param_list_count(cameraParamList, &error);
  handle_error(env, error);
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
  handle_error(env, error);

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
  handle_error(env, error);
}

/**
 * CompressionFilter
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_CompressionFilter_nCreate(JNIEnv *env, jclass clazz) {
  ob_error *error = NULL;
  auto compressionFilter = ob_create_compression_filter(&error);
  handle_error(env, error);
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
  handle_error(env, error);
}

/**
 * DeCompressionFilter
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_DeCompressionFilter_nCreate(JNIEnv *env,
                                                     jclass clazz) {
  ob_error *error = NULL;
  auto decompressionFilter = ob_create_decompression_filter(&error);
  handle_error(env, error);
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
  handle_error(env, error);
  return reinterpret_cast<jlong>(frame);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_orbbec_obsensor_FrameHelper_nCreateFrameSet(JNIEnv *env,
                                                     jclass clazz) {
  ob_error *error = NULL;
  auto frameSet = ob_create_frameset(&error);
  handle_error(env, error);
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
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_FrameHelper_nSetFrameSystemTimestamp(
    JNIEnv *env, jclass clazz, jlong frame, jlong systemTimestamp) {
  ob_error *error = NULL;
  auto pFrame = reinterpret_cast<ob_frame *>(frame);
  ob_frame_set_system_time_stamp(pFrame, systemTimestamp, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_FrameHelper_nSetFrameDeviceTimestamp(
    JNIEnv *env, jclass clazz, jlong frame, jlong deviceTimestamp) {
  ob_error *error = NULL;
  auto pFrame = reinterpret_cast<ob_frame *>(frame);
  ob_frame_set_device_time_stamp(pFrame, deviceTimestamp, &error);
  handle_error(env, error);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_obsensor_FrameHelper_nSetFrameDeviceTimestampUs(
    JNIEnv *env, jclass clazz, jlong frame, jlong deviceTimestampUs) {
  ob_error *error = NULL;
  auto pFrame = reinterpret_cast<ob_frame *>(frame);
  ob_frame_set_device_time_stamp(pFrame, deviceTimestampUs, &error);
  handle_error(env, error);
}