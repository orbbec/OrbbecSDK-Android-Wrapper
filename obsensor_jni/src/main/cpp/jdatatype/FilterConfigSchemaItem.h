#pragma once

#include <jni.h>
#include "libobsensor/h/ObTypes.h"

namespace obandroid {

    jobject convert_j_FilterConfigSchemaItem(JNIEnv *env, ob_filter_config_schema_item *item);

} // namespace obandroid
