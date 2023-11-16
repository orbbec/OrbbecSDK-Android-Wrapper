#pragma once

#include <jni.h>
#include <string>

#include "obsensor_jni.h"

#define LOG_TAG "obsensor_jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

inline void ob_handle_error(JNIEnv *env, ob_error *error) {
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

inline void ob_throw_error(JNIEnv *env, const char *function_name, const char *message) {
    std::string strFunction = (function_name ? std::string(function_name) : "");
    std::string strMessage = (message ? std::string(message) : "");
    std::string errorMsg = strFunction + "(), " + strMessage;
    env->ThrowNew(env->FindClass("com/orbbec/obsensor/OBException"),
                  errorMsg.c_str());
}