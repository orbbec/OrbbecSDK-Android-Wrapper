//
// Created by lumiaozi on 2022/12/22.
//

#include "DepthWorkMode.h"
#include "../utils/LocalUtils.h"

namespace obandroid {
jobject convert_j_DepthWorkMode(JNIEnv *env,
                                const ob_depth_work_mode &depthMode) {
  jclass clsDepthWorkMode = env->FindClass("com/orbbec/obsensor/DepthWorkMode");
  jmethodID constructMethod =
      env->GetMethodID(clsDepthWorkMode, "<init>", "()V");
  jobject jobjWorkMode = env->NewObject(clsDepthWorkMode, constructMethod);
  if (!jobjWorkMode) {
    obandroid::throw_error(env, "nGetCurrentDepthWorkMode",
                           "Create Java DepthWorkMode failed.");
    return NULL;
  }

  jstring jname = env->NewStringUTF(depthMode.name);
  jfieldID nameField =
      env->GetFieldID(clsDepthWorkMode, "name", "Ljava/lang/String;");
  env->SetObjectField(jobjWorkMode, nameField, jname);

  jfieldID checksumField = env->GetFieldID(clsDepthWorkMode, "checksum", "[B");
  jbyteArray checksumArr =
      (jbyteArray)env->GetObjectField(jobjWorkMode, checksumField);
  env->SetByteArrayRegion(checksumArr, 0, sizeof(depthMode.checksum),
                          (jbyte *)depthMode.checksum);
  return jobjWorkMode;
}

jobject convert_j_DepthWorkModeList(JNIEnv *env, ob_depth_work_mode *depthModes,
                                    size_t depthModeSize) {
  return nullptr;
}
} // namespace obandroid