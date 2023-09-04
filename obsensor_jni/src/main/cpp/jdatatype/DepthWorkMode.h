//
// Created by lumiaozi on 2022/12/22.
//

#ifndef ANDROID_DEPTHWORKMODE_H
#define ANDROID_DEPTHWORKMODE_H

#include <jni.h>

#include "libobsensor/h/ObTypes.h"

namespace obandroid {
    jobject convert_j_DepthWorkMode(JNIEnv *env, const ob_depth_work_mode &depthMode);
    jobject convert_j_DepthWorkModeList(JNIEnv *env, ob_depth_work_mode *depthModes, size_t depthModeSize);
}

#endif //ANDROID_DEPTHWORKMODE_H
