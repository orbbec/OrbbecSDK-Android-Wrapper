#pragma once

#include <jni.h>

#include "libobsensor/h/ObTypes.h"

namespace obandroid {
jobject convert_j_MultiDeviceSyncConfig(JNIEnv *env,
                                   const OBMultiDeviceSyncConfig &syncConfig);
OBMultiDeviceSyncConfig convert_c_MultiDeviceSyncConfig(JNIEnv *env,
                                              jobject jDeviceConfig);
} // namespace obandroid