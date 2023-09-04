#ifndef OB_ANDROID_DEVICE_SYNC_CONFIG_H
#define OB_ANDROID_DEVICE_SYNC_CONFIG_H

#include <jni.h>

#include "libobsensor/h/ObTypes.h"

namespace obandroid {
jobject convert_j_DeviceSyncConfig(JNIEnv *env,
                                   const OBDeviceSyncConfig &syncConfig);
OBDeviceSyncConfig convert_c_DeviceSyncConfig(JNIEnv *env,
                                              jobject jDeviceConfig);
} // namespace obandroid

#endif
