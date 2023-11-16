#pragma once

#include <jni.h>

#include "libobsensor/h/ObTypes.h"

namespace obandroid{

    jobject convert_j_DataBundle(JNIEnv *env, ob_data_bundle *data_bundle);
    ob_data_bundle *convert_c_DataBundle(JNIEnv *env, jobject jdata_bundle);
}

