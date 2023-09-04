//
// Created by lumiaozi on 2022/12/22.
//

#include "LocalUtils.h"

#include <string>

namespace obandroid {
    static jclass cls_OBException = nullptr;

    void throw_error(JNIEnv *env, char *function_name, char *message) {
        if (cls_OBException == nullptr) {
            cls_OBException = env->FindClass("com/orbbec/obsensor/OBException");
        }

        std::string strFunction = (function_name ? std::string(function_name) : "");
        std::string strMessage  = (message ? std::string(message) : "");
        std::string errorMsg = strFunction + "(), " + strMessage;
        env->ThrowNew(cls_OBException, errorMsg.c_str());
    }
}