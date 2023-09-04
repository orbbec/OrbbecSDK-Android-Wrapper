//
// Created by colphin on 2020/8/20.
//

#ifndef ANDROID_OBSENSOR_JNI_H
#define ANDROID_OBSENSOR_JNI_H

#include <android/log.h>
#include <jni.h>
#include <map>
#include <string>
#include <vector>

#include "libobsensor/h/Context.h"
#include "libobsensor/h/Device.h"
#include "libobsensor/h/Error.h"
#include "libobsensor/h/Filter.h"
#include "libobsensor/h/Frame.h"
#include "libobsensor/h/ObTypes.h"
#include "libobsensor/h/Pipeline.h"
#include "libobsensor/h/Property.h"
#include "libobsensor/h/RecordPlayback.h"
#include "libobsensor/h/Sensor.h"
#include "libobsensor/h/StreamProfile.h"
#include "libobsensor/h/Version.h"

#define LOG_TAG "obsensor_jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#endif // ANDROID_OBSENSOR_JNI_H
