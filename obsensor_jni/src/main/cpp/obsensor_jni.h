//
// Created by colphin on 2020/8/20.
//

#ifndef ANDROID_OBSENSOR_JNI_H
#define ANDROID_OBSENSOR_JNI_H

#include <android/log.h>
#include <jni.h>
#include <map>
#include <string>
#include <vector>

#include "libobsensor/h/Advanced.h"
#include "libobsensor/h/Context.h"
#include "libobsensor/h/Device.h"
#include "libobsensor/h/Error.h"
#include "libobsensor/h/Export.h"
#include "libobsensor/h/Filter.h"
#include "libobsensor/h/Frame.h"
#include "libobsensor/h/MultipleDevices.h"
#include "libobsensor/h/ObTypes.h"
#include "libobsensor/h/Pipeline.h"
#include "libobsensor/h/Property.h"
#include "libobsensor/h/Sensor.h"
#include "libobsensor/h/StreamProfile.h"
#include "libobsensor/h/TypeHelper.h"
#include "libobsensor/h/Utils.h"
#include "libobsensor/h/Version.h"

#include "jdatatype/DepthWorkMode.h"
#include "jdatatype/DeviceInfo.h"
#include "jdatatype/FilterConfigSchemaItem.h"
#include "utils/LocalUtils.h"

#endif // ANDROID_OBSENSOR_JNI_H

