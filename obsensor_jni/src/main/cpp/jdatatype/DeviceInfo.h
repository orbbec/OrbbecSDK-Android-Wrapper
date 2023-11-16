#pragma once

#include <jni.h>

#include "libobsensor/h/ObTypes.h"

namespace obandroid {

jobject convert_j_DeviceInfo(JNIEnv *env, ob_device_info *device_info);

} // namespace obandroid