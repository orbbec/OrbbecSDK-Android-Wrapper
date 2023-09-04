#pragma once

#include "libobsensor/h/ObTypes.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @brief set logger to rotating file
 *
 * @param[in] severity log level output to file
 * @param[in] directory The log file output path. If the path is empty, the existing settings will continue to be used (if the existing configuration is also
 * empty, the log will not be output to the file)
 * @param[in] max_file_size The maximum size of a single log file, Unit: MB. If the value is 0, the default value is 100MB
 * @param[in] max_file_num The maximum number of log files. If the value is 0, the default value is 3
 *
 * @param[out] error error messages
 */
void ob_set_logger_to_rotating_file(ob_log_severity severity, const char *directory, uint32_t max_file_size, uint32_t max_file_num, ob_error **error);

/**
 * @brief Device reboot delay mode
 * @attention The device will be disconnected and reconnected. After the device is disconnected, the interface access to the device handle may be abnormal.
 * Please use the ob_delete_device interface to delete the handle directly. After the device is reconnected, it can be obtained again.
 * Support devices: Gemini2 L
 *
 * @param[in] device Device object
 * @param[in] delay_ms Time unit：ms。delay_ms == 0：No delay；delay_ms > 0, Delay millisecond connect to host device after reboot
 * @param[out] error Log error messages
 */
void ob_device_reboot_delay_mode(ob_device *device, uint32_t delay_ms, ob_error **error);

#ifdef __cplusplus
}
#endif
