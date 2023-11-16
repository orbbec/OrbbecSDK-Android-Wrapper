#pragma once

#include <jni.h>

#include "libobsensor/h/ObTypes.h"

namespace obandroid {

jobject convert_j_TimestampResetConfig(JNIEnv *env, const OBDeviceTimestampResetConfig &config);
OBDeviceTimestampResetConfig convert_c_TimestampResetConfig(JNIEnv *env, jobject jobjConfig);

} // namespace obandroid
