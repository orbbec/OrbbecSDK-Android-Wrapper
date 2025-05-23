package com.orbbec.obsensor.type;

public enum FrameMetadataType {

    /**
     * @brief Timestamp when the frame is captured.
     * @attention Different device models may have different units. It is recommended to use the timestamp related functions to get the timestamp in the
     * correct units.
     */
    TIMESTAMP(0),

    /**
     * @brief Timestamp in the middle of the capture.
     * @brief Usually is the middle of the exposure time.
     * @attention Different device models may have different units.
     */
    SENSOR_TIMESTAMP(1),

    /**
     * @brief The number of current frame.
     */
    FRAME_NUMBER(2),

    /**
     * @brief Auto exposure status
     * @brief If the value is 0, it means the auto exposure is disabled. Otherwise, it means the auto exposure is enabled.
     */
    AUTO_EXPOSURE(3),

    /**
     * @brief Exposure time
     * @attention Different sensor may have different units. Usually, it is 100us for color sensor and 1us for depth/infrared sensor.
     */
    EXPOSURE(4),

    /**
     * @brief Gain
     * @attention For some device models, the gain value represents the gain level, not the multiplier.
     */
    GAIN(5),

    /**
     * @brief Auto white balance status
     * @brief If the value is 0, it means the auto white balance is disabled. Otherwise, it means the auto white balance is enabled.
     */
    AUTO_WHITE_BALANCE(6),

    /**
     * @brief White balance
     */
    WHITE_BALANCE(7),

    /**
     * @brief Brightness
     */
    BRIGHTNESS(8),

    /**
     * @brief Contrast
     */
    CONTRAST(9),

    /**
     * @brief Saturation
     */
    SATURATION(10),

    /**
     * @brief Sharpness
     */
    SHARPNESS(11),

    /**
     * @brief Backlight compensation
     */
    BACKLIGHT_COMPENSATION(12),

    /**
     * @brief Hue
     */
    HUE(13),

    /**
     * @brief Gamma
     */
    GAMMA(14),

    /**
     * @brief Power line frequency
     * @brief For anti-flickering， 0：Close， 1： 50Hz， 2： 60Hz， 3： Auto
     */
    POWER_LINE_FREQUENCY(15),

    /**
     * @brief Low light compensation
     * @attention The low light compensation is a feature inside the device，and can not manually control it.
     */
    LOW_LIGHT_COMPENSATION(16),

    /**
     * @brief Manual white balance setting
     */
    MANUAL_WHITE_BALANCE(17),

    /**
     * @brief Actual frame rate
     * @brief The actual frame rate will be calculated according to the exposure time and other parameters.
     */
    ACTUAL_FRAME_RATE(18),

    /**
     * @brief Frame rate
     */
    FRAME_RATE(19),

    /**
     * @brief Left region of interest for the auto exposure Algorithm.
     */
    AE_ROI_LEFT(20),

    /**
     * @brief Top region of interest for the auto exposure Algorithm.
     */
    AE_ROI_TOP(21),

    /**
     * @brief Right region of interest for the auto exposure Algorithm.
     */
    AE_ROI_RIGHT(22),

    /**
     * @brief Bottom region of interest for the auto exposure Algorithm.
     */
    AE_ROI_BOTTOM(23),

    /**
     * @brief Exposure priority
     */
    EXPOSURE_PRIORITY(24),

    /**
     * @brief HDR sequence name
     */
    HDR_SEQUENCE_NAME(25),

    /**
     * @brief HDR sequence size
     */
    HDR_SEQUENCE_SIZE(26),

    /**
     * @brief HDR sequence index
     */
    HDR_SEQUENCE_INDEX(27),

    /**
     * @brief Laser power value in mW
     * @attention The laser power value is an approximate estimation.
     */
    LASER_POWER(28),

    /**
     * @brief Laser power level
     */
    LASER_POWER_LEVEL(29),

    /**
     * @brief Laser status
     * @brief 0: Laser off, 1: Laser on
     */
    LASER_STATUS(30),

    /**
     * @brief GPIO input data
     */
    GPIO_INPUT_DATA(31),

    /**
     * @brief The number of frame metadata types, using for types iterating
     * @attention It is not a valid frame metadata type
     */
    COUNT(32);

    private final int mValue;

    FrameMetadataType(int value) {
        mValue = value;
    }

    public int value() {
        return mValue;
    }

    public static FrameMetadataType get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
