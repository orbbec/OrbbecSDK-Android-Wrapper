//
// Created by lumiaozi on 2022/12/22.
//

#ifndef ANDROID_LOCALUTILS_H
#define ANDROID_LOCALUTILS_H

#include <jni.h>

namespace obandroid {

    void throw_error(JNIEnv *env, char *function_name, char *message);

}

#endif //ANDROID_LOCALUTILS_H
