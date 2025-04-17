#include "FilterConfigSchemaItem.h"

#include "obsensor_jni.h"
#include "libobsensor/ObSensor.h"
#include "utils/LocalUtils.h"

namespace obandroid {

    jobject convert_j_FilterConfigSchemaItem(JNIEnv *env, ob_filter_config_schema_item *item)
    {
        if (item == nullptr) {
            LOGI("filter config schema item is nullptr");
            return nullptr;
        }

        jclass filterConfigSchemaItemClass = env->FindClass("com/orbbec/obsensor/types/FilterConfigSchemaItem");
        if (!filterConfigSchemaItemClass) {
            ob_throw_error(env, __func__, "filter config schema item class is nullptr");
        }
        jmethodID constructor = env->GetMethodID(filterConfigSchemaItemClass, "<init>", "()V");
        jobject item_ = env->NewObject(filterConfigSchemaItemClass, constructor);
        if (!item_) {
            ob_throw_error(env, __func__, "filter config schema item is nullptr");
        }

        jfieldID nameField = env->GetFieldID(filterConfigSchemaItemClass, "name", "Ljava/lang/String;");
        const char *name = item->name;
        if (name) {
            jobject jName = env->NewStringUTF(name);
            env->SetObjectField(item_, nameField, jName);
        }

        jfieldID typeField = env->GetFieldID(filterConfigSchemaItemClass, "type", "I");
        int type = item->type;
        if (type) {
            env->SetIntField(item_, typeField, type);
        }

        jfieldID minField = env->GetFieldID(filterConfigSchemaItemClass, "min", "D");
        double min = item->min;
        if (min >= 0) {
            env->SetDoubleField(item_, minField, min);
        }

        jfieldID maxField = env->GetFieldID(filterConfigSchemaItemClass, "max", "D");
        double max = item->max;
        if (max >= 0) {
            env->SetDoubleField(item_, maxField, max);
        }

        jfieldID stepField = env->GetFieldID(filterConfigSchemaItemClass, "step", "D");
        double step = item->step;
        if (step >= 0) {
            env->SetDoubleField(item_, stepField, step);
        }

        jfieldID defField = env->GetFieldID(filterConfigSchemaItemClass, "def", "D");
        double def = item->def;
        if (def >= 0) {
            env->SetDoubleField(item_, defField, def);
        }

        jfieldID descField = env->GetFieldID(filterConfigSchemaItemClass, "desc", "Ljava/lang/String;");
        const char *desc = item->desc;
        if (desc) {
            jobject jDesc = env->NewStringUTF(desc);
            env->SetObjectField(item_, descField, jDesc);
        }

        return item_;
    }
}