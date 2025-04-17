#include "DeviceInfo.h"

#include "obsensor_jni.h"
#include "libobsensor/ObSensor.h"
#include "utils/LocalUtils.h"

namespace obandroid {

jobject convert_j_DeviceInfo(JNIEnv *env, ob_device_info *device_info) {
    if (nullptr == device_info) {
        LOGI("device_info is nullptr");
        return nullptr;
    }

    jclass clsDeviceInfo = env->FindClass("com/orbbec/obsensor/types/DeviceInfo");
    if (!clsDeviceInfo) {
        ob_throw_error(env, __func__, "find class failed");
    }
    jmethodID constructMethod = env->GetMethodID(clsDeviceInfo, "<init>", "()V");
    jobject jobjDeviceInfo = env->NewObject(clsDeviceInfo, constructMethod);
    if (!jobjDeviceInfo) {
        ob_throw_error(env, __func__, "Create Java DeviceInfo failed.");
    }

    ob_error *error = nullptr;
    jfieldID nameField = env->GetFieldID(clsDeviceInfo, "name", "Ljava/lang/String;");
    const char *name = ob_device_info_get_name(device_info, &error);
    ob_handle_error(env, error);
    if (name) {
        jobject jName = env->NewStringUTF(name);
        env->SetObjectField(jobjDeviceInfo, nameField, jName);
    }

    jfieldID vidField = env->GetFieldID(clsDeviceInfo, "vid", "I");
    int vid = ob_device_info_get_vid(device_info, &error);
    ob_handle_error(env, error);
    env->SetIntField(jobjDeviceInfo, vidField, vid);

    jfieldID pidField = env->GetFieldID(clsDeviceInfo, "pid", "I");
    int pid = ob_device_info_get_pid(device_info, &error);
    ob_handle_error(env, error);
    env->SetIntField(jobjDeviceInfo, pidField, pid);

    jfieldID uidField = env->GetFieldID(clsDeviceInfo, "uid", "Ljava/lang/String;");
    const char* uid = ob_device_info_get_uid(device_info, &error);
    ob_handle_error(env, error);
    if (uid) {
        jobject jText = env->NewStringUTF(uid);
        env->SetObjectField(jobjDeviceInfo, uidField, jText);
    }

    jfieldID serialNumberField = env->GetFieldID(clsDeviceInfo, "serialNumber", "Ljava/lang/String;");
    const char* serialNumber = ob_device_info_get_serial_number(device_info, &error);
    ob_handle_error(env, error);
    if (serialNumber) {
        jobject jText = env->NewStringUTF(serialNumber);
        env->SetObjectField(jobjDeviceInfo, serialNumberField, jText);
    }

    jfieldID connectionTypeField = env->GetFieldID(clsDeviceInfo, "connectionType", "Ljava/lang/String;");
    const char* connectionType = ob_device_info_get_connection_type(device_info, &error);
    ob_handle_error(env, error);
    if (connectionType) {
        jobject jText = env->NewStringUTF(connectionType);
        env->SetObjectField(jobjDeviceInfo, connectionTypeField, jText);
    }

    jfieldID ipAddressField = env->GetFieldID(clsDeviceInfo, "ipAddress", "Ljava/lang/String;");
    if (strcmp("ethernet", connectionType) == 0 || strcmp("Ethernet", connectionType) == 0) {
        const char *ipAddress = ob_device_info_get_ip_address(device_info, &error);
        ob_handle_error(env, error);
        if (ipAddress) {
            jobject jText = env->NewStringUTF(ipAddress);
            env->SetObjectField(jobjDeviceInfo, ipAddressField, jText);
        }
    }

    jfieldID firmwareVersionField = env->GetFieldID(clsDeviceInfo, "firmwareVersion", "Ljava/lang/String;");
    const char* firmwareVersion = ob_device_info_get_firmware_version(device_info, &error);
    ob_handle_error(env, error);
    if (firmwareVersion) {
        jobject jText = env->NewStringUTF(firmwareVersion);
        env->SetObjectField(jobjDeviceInfo, firmwareVersionField, jText);
    }

    jfieldID hardwareVersionField = env->GetFieldID(clsDeviceInfo, "hardwareVersion", "Ljava/lang/String;");
    const char* hardwareVersion = ob_device_info_get_hardware_version(device_info, &error);
    ob_handle_error(env, error);
    if (hardwareVersion) {
        jobject jText = env->NewStringUTF(hardwareVersion);
        env->SetObjectField(jobjDeviceInfo, hardwareVersionField, jText);
    }

    jfieldID supportedMinSdkVersionField = env->GetFieldID(clsDeviceInfo, "supportedMinSdkVersion", "Ljava/lang/String;");
    const char* supportedMinSdkVersion = ob_device_info_get_supported_min_sdk_version(device_info, &error);
    ob_handle_error(env, error);
    if (supportedMinSdkVersion) {
        jobject jText = env->NewStringUTF(supportedMinSdkVersion);
        env->SetObjectField(jobjDeviceInfo, supportedMinSdkVersionField, jText);
    }

    jfieldID asicNameField = env->GetFieldID(clsDeviceInfo, "asicName", "Ljava/lang/String;");
    const char* asicName = ob_device_info_get_asicName(device_info, &error);
    ob_handle_error(env, error);
    if (asicName) {
        jobject jText = env->NewStringUTF(asicName);
        env->SetObjectField(jobjDeviceInfo, asicNameField, jText);
    }

    jfieldID deviceTypeField = env->GetFieldID(clsDeviceInfo, "deviceTypeValue", "I");
    int deviceType = ob_device_info_get_device_type(device_info, &error);
    ob_handle_error(env, error);
    env->SetIntField(jobjDeviceInfo, deviceTypeField, deviceType);

    return jobjDeviceInfo;
}

} // namespace obandroid