#pragma once

#include <jni.h>

#include "libobsensor/h/ObTypes.h"

namespace obandroid {
    jobject convert_j_DepthWorkMode(JNIEnv *env, const ob_depth_work_mode &depthMode);
}