#pragma once

#include <jni.h>

#include "libobsensor/h/ObTypes.h"

namespace obandroid {
    jobject convert_j_NetworkConfig(JNIEnv *env, const OBNetIpConfig &config);
    OBNetIpConfig convert_c_NetworkConfig(JNIEnv *env, jobject jobjConfig);
} // namespace obandroid
