package com.orbbec.obsensor;

/**
 * \if English
 * Device Property
 * \else
 * 设备属性
 * \endif
 */
public enum DeviceProperty {
    //0~999为设备端int, bool及float类型控制命令
    /**
	 * \if English
     * LDP switch
     * \else
     * LDP开关
	 * \endif
     */
    OB_PROP_LDP_BOOL(2),

    /**
     * \if English
     * laser switch
     * \else
     * 激光开光
	 * \endif
     */
    OB_PROP_LASER_BOOL(3),

    /**
     * \if English
     * @brief laser pulse width
     * \else
     * 读写激光Time值(脉宽)
     * \endif
     */
    OB_PROP_LASER_PULSE_WIDTH_INT(4),

    /**
	 * \if English
     * Laser current (unit: mA)
     * \else
     * 激光电流，单位：mA
	 * \endif
     */
    OB_PROP_LASER_CURRENT_FLOAT(5),

    /**
	 * \if English
     * IR flood switch
     * \else
     * 泛光灯开关
	 * \endif
     */
    OB_PROP_FLOOD_BOOL(6),

    /**
     * \if English
     * IR flood level
     * \else
     * 泛光灯等级
     * \endif
     */
    OB_PROP_FLOOD_LEVEL_INT(7),

    /**
	 * \if English
     * depth mirror
     * \else
     * 深度镜像
	 * \endif
     */
    OB_PROP_DEPTH_MIRROR_BOOL(14),

    /**
	 * \if English
     * depth flip
     * \else
     * 深度翻转
	 * \endif
     */
    OB_PROP_DEPTH_FLIP_BOOL(15),

    /**
	 * \if English
     * depth post filter
     * \else
     * 深度Post filter
	 * \endif
     */
    OB_PROP_DEPTH_POSTFILTER_BOOL(16),

    /**
	 * \if English
     * depth hole filter
     * \else
     * 深度Hole filter
	 * \endif
     */
    OB_PROP_DEPTH_HOLEFILTER_BOOL(17),

    /**
	 * \if English
     * IR mirror
     * \else
     * IR 镜像
     * \endif
     */
    OB_PROP_IR_MIRROR_BOOL(18),

    /**
	 * \if English
     * IR flip
     * \else
     * IR 翻转
     * \endif
     */
    OB_PROP_IR_FLIP_BOOL(19),

    /**
	 * \if English
     * minimum depth threshold
     * \else
     * 最小深度阈值
     * \endif
     */
    OB_PROP_MIN_DEPTH_INT(22),

    /**
	 * \if English
     * maximum depth threshold
     * \else
     * 最大深度阈值
     * \endif
     */
    OB_PROP_MAX_DEPTH_INT(23),

    /**
	 * \if English
     * Software filter switch
     * \else
     * 软件滤波开关
     * \endif
     */
    OB_PROP_DEPTH_SOFT_FILTER_BOOL(24),

    /**
	 * \if English
     * LDP status
     * \else
     * LDP状态
     * \endif
     */
    OB_PROP_LDP_STATUS_BOOL(32),

    /**
     * \if English
     * soft filter maxdiff param
     * \else
     * 软件滤波的maxdiff参数
     * \endif
     */
    OB_PROP_DEPTH_MAX_DIFF_INT(40),

    /**
     * \if English
     * soft filter maxSpeckleSize
     * \else
     * 软件滤波的maxSpeckleSize
     * \endif
     */
    OB_PROP_DEPTH_MAX_SPECKLE_SIZE_INT(41),

    /**
     * \if English
     * Hardware d2c is on
     * \else
     * 硬件d2c开
     * \endif
     */
    OB_PROP_DEPTH_ALIGN_HARDWARE_BOOL(42),

    /**
	 * \if English
     *	time stamp adjustment
     * \else
     * 时间戳调校
     * \endif
     */
    OB_PROP_TIMESTAMP_OFFSET_INT(43),

    /**
	 * \if English
     * 	Hardware de-distortion switch Rectify
     * \else
     * 硬件去畸变开关 Rectify
     * \endif
     */
    OB_PROP_HARDWARE_DISTORTION_SWITCH_BOOL(61),

    /**
	 * \if English
     * Fan switch mode
     * \else
     * 风扇开关模式
     * \endif
     */
    OB_PROP_FAN_WORK_MODE_INT(62),

    /**
     * \if English
     * Multi-resolution D2C mode
     * \else
     * 多分辨率D2C模式
     * \endif
     */
    OB_PROP_DEPTH_ALIGN_HARDWARE_MODE_INT(63),

    /**
     * \if English
     * Anti_collusion activation status
     * \else
     * 防串货状态
     * \endif
     */
    OB_PROP_ANTI_COLLUSION_ACTIVATION_STATUS_BOOL(64),

    /**
	 * \if English
     * 	The depth precision level may change the depth frame data unit. You need to confirm the setting parameters through the ValueScale interface of DepthFrame.
	 * Set parameter reference {@link DepthPrecisionLevel#value()}
	 * The obtained value can be converted through {@link DepthPrecisionLevel#get(int)}
     * \else
     * 深度精度等级，可能会改变深度帧数据单位，需要通过DepthFrame的ValueScale接口确认
     * 设置参数参考{@link DepthPrecisionLevel#value()}
     * 获取的值可通过{@link DepthPrecisionLevel#get(int)}进行转换
	 * \endif
     */
    OB_PROP_DEPTH_PRECISION_LEVEL_INT(75),

    /**
	 * \if English
     * tof filter scene range configuration
     * \else
     * tof滤波场景范围配置
     * \endif
     */
    OB_PROP_TOF_FILTER_RANGE_INT(76),

    /**
	 * \if English
     * mx6600 laser lighting mode, the firmware only returns 1: IR Drive, 2: Torch at present
     * \else
     * mx6600 激光点亮模式，固件端目前只返回 1: IR Drive, 2: Torch
     * \endif
     */
    OB_PROP_LASER_MODE_INT(79),

    /**
	 * \if English
     * 	mx6600 brt2r-rectify function switch (brt2r is a special module on mx6600), false: Disable, true: Rectify Enable
     * \else
     * mx6600 brt2r-rectify功能开关(brt2r是mx6600上的一个特殊模块)，false: Disable, true: Rectify Enable
     * \endif
     */
    OB_PROP_RECTIFY2_BOOL(80),

    /**
	 * \if English
     * color mirror
     * \else
     * 彩色镜像
     * \endif
     */
    OB_PROP_COLOR_MIRROR_BOOL(81),

    /**
	 * \if English
     * color flip
     * \else
     * 彩色翻转
     * \endif
     */
    OB_PROP_COLOR_FLIP_BOOL(82),

    /**
	 * \if English
     * indicator light switch  false: Disable， true: Enable
     * \else
     * 指示灯开关 false: Disable， true: Enable
     * \endif
     */
    OB_PROP_INDICATOR_LIGHT_BOOL(83),

    /**
	 * \if English
	 * Hardware parallax to depth switch, 0: Turn off the hardware parallax to depth switch, turn on the software parallax to depth switch 1.Turn on the hardware parallax to depth switch, turn off the software parallax to depth switch
	 * \else
     * 硬件视差转深度开关， 0：关闭硬件视差转深度开关，打开软件视差转深度开关 1. 打开硬件视差转深度开关，关闭软件视差转深度开关
     * \endif
     */
    OB_PROP_DISPARITY_TO_DEPTH_BOOL(85),

    /**
	 * \if English
     * BRT function switch (anti-background interference), false: Disable, true: Enable
     * \else
     * BRT功能开关(抗背景干扰)，false: Disable， true: Enable
     * \endif
     */
    OB_PROP_BRT_BOOL(86),

    /**
	 * \if English
     * 	Watchdog function switch, false: Disable, true: Enable
     * \else
     * 看门狗功能开关，false: Disable， true: Enable
     * \endif
     */
    OB_PROP_WATCHDOG_BOOL(87),

    /**
	 * \if English
     * 	External signal triggers restart function switch, false: Disable, true: Enable
     * \else
     * 外部信号触发重启功能开关，false: Disable， true: Enable
     * \endif
     */
    OB_PROP_EXTERNAL_SIGNAL_RESET_BOOL(88),

    /**
	 * \if English
     * Heartbeat monitoring function switch, false: Disable, true: Enable
     * \else
     * 心跳监测功能开关，false: Disable， true: Enable
     * \endif
     */
    OB_PROP_HEARTBEAT_BOOL(89),

    /**
	 * \if English
     * Depth cropping mode device: OB_DEPTH_CROPPING_MODE_EM (currently only suitable for Longquan Sword)
     * \else
     * 深度裁剪模式设备: OB_DEPTH_CROPPING_MODE_EM （当前仅适配龙泉剑）
     * \endif
     */
    OB_PROP_DEPTH_CROPPING_MODE_INT(90),

    /**
	 * \if English
     * D2C preprocessing switch (such as RGB cropping), false: off, true: on
     * \else
     * D2C前处理开关（如RGB裁剪），false：关，true：开
     * \endif
     */
    OB_PROP_D2C_PREPROCESS_BOOL(91),

    /**
	 * \if English
     * custom RGB cropping switch, 0 is off, 1 is on custom cropping, and the ROI cropping area is issued
     * \else
     * 自定义RGB裁剪开关，0为关闭，1为开启自定义裁剪，下发ROI裁剪区域
     * \endif
     */
    OB_PROP_RGB_CUSTOM_CROP_BOOL(94),

    /**
	 * \if English
     * Device operating mode (power consumption)
     * \else
     * 设备工作模式（功耗）
     * \endif
     */
    OB_PROP_DEVICE_WORK_MODE_INT(95),

    /**
     * \if English
     * Device communication type, 0: USB; 1: Ethernet(RTSP)
     * \else
     * 设备通信方式 0: USB; 1: Ethernet(RTSP)
     * \endif
     */
    OB_PROP_DEVICE_COMMUNICATION_TYPE_INT(97),

    /**
     *  \if English
     *  Switch IR mode, 0: IR active mode, 1: IR passive mode
     *  \else
     *  切换IR模式，0为主动IR模式,1为被动IR模式
     *  \endif
     */
    OB_PROP_SWITCH_IR_MODE_INT(98),

    /**
     * \if English
     * Laser energy level
     * \else
     * 激光能量层级
     * \endif
     */
    OB_PROP_LASER_ENERGY_LEVEL_INT(99),

    /**
     * \if English
     * LDP's measure distance, unit: mm
     * \else
     * 获取激光近距离保护的测量值，单位：mm
     * \endif
     */
    OB_PROP_LDP_MEASURE_DISTANCE_INT(100),

    /**
     * \if English
     * Reset device time to zero
     * \else
     * 触发设备时间归零
     * \endif
     */
    OB_PROP_TIMER_RESET_SIGNAL_BOOL(104),

    /**
     * \if English
     * Enable send reset device time signal to other device. true: enable, false: disable
     * \else
     * 向外发送时间归零信号开关, true:打开, false: 关闭; 默认为true
     * \endif
     */
    OB_PROP_TIMER_RESET_TRIGGLE_OUT_ENABLE_BOOL(105),

    /**
     * \if English
     * Delay to reset device time, unit: us
     * \else
     * 设置硬件时间归零延迟时间, 单位: 微妙
     * \endif
     */
    OB_PROP_TIMER_RESET_DELAY_US_INT(106),

    /**
     * \if English
     * Signal to capture image
     * \else
     * 软触发信号, 触发抓拍图片
     * \endif
     */
    OB_PROP_CAPTURE_IMAGE_SIGNAL_BOOL(107),

    /**
     * \if English
     * Right IR sensor mirror state
     * \else
     * 右IR的镜像
     * \endif
     */
    OB_PROP_IR_RIGHT_MIRROR_BOOL(112),

    /**
     * \if English
     * Number frame to capture once a 'OB_PROP_CAPTURE_IMAGE_SIGNAL_BOOL' effect. range: [1, 255]
     * \else
     * 单次软触发抓拍的帧数, 范围：[1, 255]
     * \endif
     */
    OB_PROP_CAPTURE_IMAGE_FRAME_NUMBER_INT(113),

    /**
     * \if English
     * Right IR sensor flip state. true: flip image, false: origin, default: false
     * \else
     * 右IR的翻转, true：翻转,false：不翻转；默认为false
     * \endif
     */
    OB_PROP_IR_RIGHT_FLIP_BOOL(114),

    /**
     * \if English
     * Color sensor rotation, angle{0, 90, 180, 270}
     * \else
     * 彩色旋转, 翻转角度范围{0, 90, 180, 270}, 默认为0
     * \endif
     */
    OB_PROP_COLOR_ROTATE_INT(115),

    /**
     * \if English
     * IR/Left-IR sensor rotation, angle{0, 90, 180, 270}
     * \else
     * IR旋转, 翻转角度范围{0, 90, 180, 270}, 默认为0
     * \endif
     */
    OB_PROP_IR_ROTATE_INT(116),

    /**
     *  \if English
     *  Right IR sensor rotation, angle{0, 90, 180, 270}
     *  \else
     *  右IR旋转, 翻转角度范围{0, 90, 180, 270}, 默认为0
     *  \endif
     */
    OB_PROP_IR_RIGHT_ROTATE_INT(117),

    /**
     * \if English
     * Depth sensor rotation, angle{0, 90, 180, 270}
     * \else
     * 深度旋转, 翻转角度范围{0, 90, 180, 270}, 默认为0
     * \endif
     */
    OB_PROP_DEPTH_ROTATE_INT(118),

    /**
     * \if English
     * Get hardware laser energy level which real state of laser element. OB_PROP_LASER_ENERGY_LEVEL_INT（99）will effect
     * this command which it setting and changed the hardware laser energy level.
     * \else
     * 查询激光硬件的实际能量层级, OB_PROP_LASER_ENERGY_LEVEL_INT（99）指令用于设置能级,该指令用于查询设置后硬件实际能级 \
     * endif
     */
    OB_PROP_LASER_HW_ENERGY_LEVEL_INT(119),

    /**
     * \if English
     * USB's power state, enum type: OBUSBPowerState
     * \else
     * USB供电状态，状态值枚举: OBUSBPowerState
     * \endif
     */
    OB_PROP_USB_POWER_STATE_INT(121),

    /**
     * \if English
     * DC's power state, enum type: OBDCPowerState
     * \else
     * DC供电状态,状态值枚举: OBDCPowerState
     * \endif
     */
    OB_PROP_DC_POWER_STATE_INT(122),

    /**
     * \if English
     * Device development mode switch, optional modes can refer to the definition in @ref OBDeviceDevelopmentMode,the default mode is
     * @ref OB_USER_MODE
     * @attention The device takes effect after rebooting when switching modes.
     * \else
     * 设备开发模式切换，可选模式可参考 @ref OBDeviceDevelopmentMode 中的定义
     * \endif
     */
    OB_PROP_DEVICE_DEVELOPMENT_MODE_INT(129),

    /**
     * \if English
     * Multi-DeviceSync synchronized signal trigger out is enable state. true: enable, false: disable
     * \else
     * 多机同步触发信号外发使能，true：打开，false：关闭
     * \endif
     */
    OB_PROP_SYNC_SIGNAL_TRIGGER_OUT_BOOL(130),

    /**
     * \if English
     * Restore factory settings and factory parameters
     * @attention This command can only be written, and the parameter value must be true. The command takes effect after restarting the device.
     * \else
     * 恢复出厂设置和参数，只写，参数值必须为true，重启设备后生效
     * \endif
     */
    OB_PROP_RESTORE_FACTORY_SETTINGS_BOOL(131),

    /**
     * \if English
     * Enter recovery mode (flashing mode) when boot the device
     * @attention The device will take effect after rebooting with the enable option. After entering recovery mode, you can upgrade the device system. Upgrading
     * the system may cause system damage, please use it with caution.
     * \else
     * 启动设备时进入恢复模式（刷机模式）, 在该模式下可对设备系统进行升级
     * \endif
     */
    OB_PROP_BOOT_INTO_RECOVERY_MODE_BOOL(132),

    /**
     * \if English
     * Query whether the current device is running in recovery mode (read-only)
     * \else
     * 获取当前设备是否运行在恢复模式（刷机模式）
     * \endif
     */
    OB_PROP_DEVICE_IN_RECOVERY_MODE_BOOL(133),

    /**
     * \if English
     * Reboot device delay mode. Data type: uint32_t, Delay time unit: ms, range: [0, 8000).
     * \else
     * 控制设备重启，带延迟模式；类型：uint32_t，延迟时间单位：ms。delay为0：不延迟；delay大于0，延迟delay毫秒，范围: [0, 8000)
     * \endif
     */
    OB_PROP_DEVICE_REBOOT_DELAY_INT(142),

    //1000~1999为设备端结构体控制命令

    /**
     * \if English
     * Baseline calibration parameters
     * \else
     * 基线标定参数
     * \endif
     */
    OB_STRUCT_BASELINE_CALIBRATION_PARAM(1002),

    /**
	 * \if English
     * Device temperature
     * \else
     * 设备温度
     * \endif
     */
    OB_STRUCT_DEVICE_TEMPERATURE(1003),

    /**
	 * \if English
     * 	TOF exposure threshold range
     * \else
     * TOF曝光阈值范围
     * \endif
     */
    OB_STRUCT_TOF_EXPOSURE_THRESHOLD_CONTROL(1024),

    /**
     * \if English
     * get serial number
     * \else
     * get序列号
     * \endif
     */
    OB_STRUCT_DEVICE_SERIAL_NUMBER(1035),

    /**
     * \if English
     * get/set device time
     * \else
     * 获取/设置设备时间
     * \endif */
    OB_STRUCT_DEVICE_TIME(1037),

    /**
	 * \if English
     * 	Multi-device synchronization mode and parameter configuration
     * \else
     * 多设备同步模式和参数配置
     * \endif
     */
    OB_STRUCT_MULTI_DEVICE_SYNC_CONFIG(1038),

    /**
     * \if English
     * RGB cropping ROI
     * \else
     * RGB裁剪ROI
     * \endif
     */
    OB_STRUCT_RGB_CROP_ROI(1040),

    /**
     * \if English
     * Device IP address configuration
     * \else
     * 设备ip地址配置
     * \endif
     */
    OB_STRUCT_DEVICE_IP_ADDR_CONFIG(1041),

    /**
     * \if English
     * The current camera depth mode
     * \else
     * 当前的相机深度模式
     * \endif
     */
    OB_STRUCT_CURRENT_DEPTH_ALG_MODE(1043),

    /**
     * \if English
     * A list of depth accuracy levels, returning an array of uin16_t, corresponding to the enumeration definitions
     * of the accuracy classes
     * \else
     * 深度精度等级列表，返回uin16_t数组，对应精度等级的枚举定义
     * \endif
     */
    OB_STRUCT_DEPTH_PRECISION_SUPPORT_LIST(1045),

    /**
	 * \if English
     * Color camera auto exposure
     * \else
     * 彩色相机自动曝光
     * \endif
     */
    OB_PROP_COLOR_AUTO_EXPOSURE_BOOL(2000),

    /**
	 * \if English
     * 	Color camera exposure adjustment
     * \else
     * 彩色相机曝光调节
     * \endif
     */
    OB_PROP_COLOR_EXPOSURE_INT(2001),

    /**
	 * \if English
     * Color camera gain adjustment
     * \else
     * 彩色相机增益调节
     * \endif
     */
    OB_PROP_COLOR_GAIN_INT(2002),

    /**
	 * \if English
     * 	Color camera automatic white balance
     * \else
     * 彩色相机自动白平衡
     * \endif
     */
    OB_PROP_COLOR_AUTO_WHITE_BALANCE_BOOL(2003),

    /**
	 * \if English
     * Color camera white balance adjustment
     * \else
     * 彩色相机白平衡调节
     * \endif
     */
    OB_PROP_COLOR_WHITE_BALANCE_INT(2004),

    /**
	 * \if English
     * Color camera brightness adjustment
     * \else
     * 彩色相机亮度调节
     * \endif
     */
    OB_PROP_COLOR_BRIGHTNESS_INT(2005),

    /**
	 * \if English
     * Color camera sharpness adjustment
     * \else
     * 彩色相机锐度调节
     * \endif
     */
    OB_PROP_COLOR_SHARPNESS_INT(2006),

    /**
	 * \if English
     * Color camera saturation adjustment
     * \else
     * 彩色相机饱和度调节
     * \endif
     */
    OB_PROP_COLOR_SATURATION_INT(2008),

    /**
	 * \if English
     * 	Color camera contrast adjustment
     * \else
     * 彩色相机对比度调节
     * \endif
     */
    OB_PROP_COLOR_CONTRAST_INT(2009),

    /**
	 * \if English
     * Color camera gamma adjustment
     * \else
     * 彩色相机伽马值调节
     * \endif
     */
    OB_PROP_COLOR_GAMMA_INT(2010),

    /**
	 * \if English
     * Color camera image rotation
     * \else
     * 彩色相机图像旋转
     * \endif
     */
    OB_PROP_COLOR_ROLL_INT(2011),

    /**
	 * \if English
     * Color camera auto exposure priority
     * \else
     * 彩色相机自动曝光优先
     * \endif
     */
    OB_PROP_COLOR_AUTO_EXPOSURE_PRIORITY_INT(2012),

    /**
	 * \if English
     * Color camera brightness compensation
     * \else
     * 彩色相机亮度补偿
     * \endif
     */
    OB_PROP_COLOR_BACKLIGHT_COMPENSATION_INT(2013),

    /**
	 * \if English
     * color camera color tint
     * \else
     * 彩色相机彩色色调
     * \endif
     */
    OB_PROP_COLOR_HUE_INT(2014),

    /**
	 * \if English
     * 	Color camera power line frequency
     * \else
     * 彩色相机电力线路频率
     * \endif
     */
    OB_PROP_COLOR_POWER_LINE_FREQUENCY_INT(2015),

    /**
	 * \if English
     * 	Automatic exposure of depth camera (infrared camera will be set synchronously under some models of devices)
     * \else
     * 深度相机自动曝光（某些型号设备下会同步设置红外相机）
     * \endif
     */
    OB_PROP_DEPTH_AUTO_EXPOSURE_BOOL(2016),

    /**
	 * \if English
     * 	Depth camera exposure adjustment (infrared cameras will be set synchronously under some models of devices)
     * \else
     * 深度相机曝光调节（某些型号设备下会同步设置红外相机）
     * \endif
     */
    OB_PROP_DEPTH_EXPOSURE_INT(2017),

    /**
	 * \if English
     * Depth camera gain adjustment (infrared cameras will be set synchronously under some models of devices)
     * \else
     * 深度相机增益调节（某些型号设备下会同步设置红外相机）
     * \endif
     */
    OB_PROP_DEPTH_GAIN_INT(2018),

    /**
	 * \if English
     * Infrared camera auto exposure (depth camera will be set synchronously under some models of devices)
     * \else
     * 红外相机自动曝光（某些型号设备下会同步设置深度相机）
     * \endif
     */
    OB_PROP_IR_AUTO_EXPOSURE_BOOL(2025),

    /**
	 * \if English
     * Infrared camera exposure adjustment (some models of devices will set the depth camera synchronously)
     * \else
     * 红外相机曝光调节（某些型号设备下会同步设置深度相机）
     * \endif
     */
    OB_PROP_IR_EXPOSURE_INT(2026),

    /**
	 * \if English
     * Infrared camera gain adjustment (the depth camera will be set synchronously under some models of devices)
     * \else
     * 红外相机增益调节（某些型号设备下会同步设置深度相机）
     * \endif
     */
    OB_PROP_IR_GAIN_INT(2027),

    /**
     * \if English
     * Select Infrared camera data source channel. If not support throw exception.
     * 0 : IR stream from IR Left sensor;
     * 1 : IR stream from IR Right sensor;
     * \else
     * 读写IR通道的输出目标sensor,不支持时返回错误。0: 左侧IR  sensor,1: 右侧IR sensor;
     * \endif
     */
    OB_PROP_IR_CHANNEL_DATA_SOURCE_INT(2028),

    /**
     * \if English
     * Depth effect dedistortion, true: on, false: off. mutually exclusive with D2C function, RM_Filter disable When hardware or software D2C is enabled.
     * \else
     * RM是Remark Filter的缩写, 深度mask对齐参数开关, true：打开，false：关闭, 与D2C功能互斥。软硬件D2C开启时，不能使用mask功能
     * \endif
     */
    OB_PROP_DEPTH_RM_FILTER_BOOL(2029),

    /**
     * \if English
     * Color camera maximal gain
     * \else
     * 彩色相机最大增益
     * \endif
     */
    OB_PROP_COLOR_MAXIMAL_GAIN_INT(2030),

    /**
     * \if English
     * Color camera shutter gain
     * \else
     * 彩色相机最大快门
     * \endif
     */
    OB_PROP_COLOR_MAXIMAL_SHUTTER_INT(2031),

    /**
	 * \if English
     * Depth data unpacking function switch (each open stream will be turned on by default, support RLE/Y10/Y11/Y12/Y14 format)
     * \else
     * SDK端Depth数据解包功能开关(每次开流都会默认打开，支持RLE/Y10/Y11/Y12/Y14格式)
     * \endif
     */
    OB_PROP_SDK_DEPTH_FRAME_UNPACK_BOOL(3007),

    /**
     * \if English
     * IR data unpacking function switch (each current will be turned on by default, support RLE/Y10/Y11/Y12/Y14 format)
     * \else
     * Depth数据解包功能开关(每次开流都会默认打开,支持RLE/Y10/Y11/Y12/Y14格式)
     * \endif
     */
    OB_PROP_SDK_IR_FRAME_UNPACK_BOOL(3008),

    /**
     * \if English
     * Accel data conversion function switch (on by default)
     * \else
     * Accel数据转换功能开关(默认打开)
     * \endif
     */
    OB_PROP_SDK_ACCEL_FRAME_TRANSFORMED_BOOL(3009),

    /**
     * \if English
     * Gyro data conversion function switch (on by default)
     * \else
     * Gyro数据转换功能开关(默认打开)
     * \endif
     */
    OB_PROP_SDK_GYRO_FRAME_TRANSFORMED_BOOL(3010),

    /**
     * \if English
     * Left IR frame data unpacking function switch (each current will be turned on by default, support RLE/Y10/Y11/Y12/Y14 format)
     * \else
     * [左]Ir数据解包功能开关(每次开流都会默认打开，支持RLE/Y10/Y11/Y12/Y14格式)
     * \endif
     */
    OB_PROP_SDK_IR_LEFT_FRAME_UNPACK_BOOL(3011),

    /**
     * \if English
     * Right IR frame data unpacking function switch (each current will be turned on by default, support RLE/Y10/Y11/Y12/Y14 format)
     * \else
     * [右]Ir数据解包功能开关(每次开流都会默认打开，支持RLE/Y10/Y11/Y12/Y14格式)
     * \endif
     */
    OB_PROP_SDK_IR_RIGHT_FRAME_UNPACK_BOOL(3012),

    /**
     * \if English
     * Calibration JSON file read from device (Femto Mega, read only)
     * \else
     * 从设备端读取的标定Json文件(Femto Mega, read only)
     * \endif
     */
    OB_RAW_DATA_CAMERA_CALIB_JSON_FILE(4029);

    private final int mValue;

    DeviceProperty(int value) {
        mValue = value;
    }

    /**
	 * \if English
	 * Get the index corresponding to the device property
     *
     * @return index value
	 * \else
     * 获取设备属性对应的索引
     *
     * @return 索引值
	 * \endif
     */
    public int value() {
        return mValue;
    }

    /**
	 * \if English
	 * Get the device properties corresponding to the specified index
     *
     * @param value index value
     * @return device properties
	 * \else
     * 获取指定索引对应的设备属性
     *
     * @param value 索引值
     * @return 设备属性
	 * \endif
     */
    public static DeviceProperty get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
