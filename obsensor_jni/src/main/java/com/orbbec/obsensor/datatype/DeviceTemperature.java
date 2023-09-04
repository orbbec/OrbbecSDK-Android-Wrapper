package com.orbbec.obsensor.datatype;

import com.orbbec.obsensor.DataType;
import com.orbbec.internal.DataUtilities;

/**
 * \if English
 * Device temperature information
 * \else
 * 设备温度信息
 * \endif
 */
public class DeviceTemperature extends DataType {
    private float cpuTemp;
    private float irTemp;
    private float ldmTemp;
    private float mainBoardTemp;
    private float tecTemp;
    private float imuTemp;
    private float rgbTemp;
    private float irLeftTemp;
    private float irRightTemp;
    private float chipTopTemp;
    private float chipBottomTemp;

    /**
	 * \if English
	 * Get CPU temperature, unit: Celsius
     *
     * @return CPU temperature
	 * \else
     * 获取CPU温度，单位：摄氏度
     *
     * @return CPU温度
	 * \endif
     */
    public float getCpuTemp() {
        return cpuTemp;
    }

    /**
	 * \if English
	 * Get IR sensor temperature, unit: Celsius
     *
     * @return IR sensor temperature
	 * \else
     * 获取IR传感器温度，单位：摄氏度
     *
     * @return IR传感器温度
	 * \endif
     */
    public float getIrTemp() {
        return irTemp;
    }

    /**
	 * \if English
	 * Get the temperature of the laser module, unit: Celsius
     *
     * @return Laser module temperature
	 * \else
     * 获取激光模组温度，单位：摄氏度
     *
     * @return 激光模组温度
	 * \endif
     */
    public float getLdmTemp() {
        return ldmTemp;
    }

    /**
	 * \if English
	 * Get the motherboard temperature, unit: Celsius
     *
     * @return motherboard temperature
	 * \else
     * 获取主板温度，单位：摄氏度
     *
     * @return 主板温度
	 * \endif
     */
    public float getMainBoardTemp() {
        return mainBoardTemp;
    }

    /**
	 * \if English
	 * Get the TEC temperature in degrees Celsius
     *
     * @return TEC temperature
	 * \else
     * 获取TEC温度，单位：摄氏度
     *
     * @return TEC温度
	 * \endif
     */
    public float getTecTemp() {
        return tecTemp;
    }

    /**
	 * \if English
	 * Get IMU sensor temperature, unit: Celsius
     *
     * @return IMU sensor temperature
	 * \else
     * 获取IMU传感器温度，单位：摄氏度
     *
     * @return IMU传感器温度
	 * \endif
     */
    public float getImuTemp() {
        return imuTemp;
    }

    /**
	 * \if English
	 * Get RGB sensor temperature, unit: Celsius
     *
     * @return RGB sensor temperature
	 * \else
     * 获取RGB传感器温度，单位：摄氏度
     *
     * @return RGB传感器温度
	 * \endif
     */
    public float getRgbTemp() {
        return rgbTemp;
    }

    /**
     * \if English
     * Get temperature of Left IR, unit: degree Celsius
     * @return Left IR sensor temperature
     * \else
     * 获取左IR传感器温度，单位：摄氏度
     * @return 左IR传感器温度
     * \endif
     */
    public float getIrLeftTemp() {
        return irLeftTemp;
    }

    /**
     * \if English
     * Get temperature of Right IR, unit: degree Celsius
     * @return Right IR sensor temperature
     * \else
     * 获取右IR传感器温度，单位：摄氏度
     * @return 右IR传感器温度
     * \endif
     */
    public float getIrRightTemp() {
        return irRightTemp;
    }

    /**
     * \if English
     * Get top temperature of MX6600 chip. unit: degree Celsius
     * \else
     * 获取MX6600芯片Top的温度，单位：摄氏度
     * @return MX6600芯片Top的温度
     * \endif
     */
    public float getChipTopTemp() {
        return chipTopTemp;
    }

    /**
     * \if English
     * Get bottom temperature of MX6600 chip. unit: degree Celsius
     * \else
     * 获取MX6600芯片Bottom的温度，单位：摄氏度
     * @return MX6600芯片Bottom的温度
     * \endif
     */
    public float getChipBottomTemp() {
        return chipBottomTemp;
    }

    @Override
    public int BYTES() {
        return 44;
    }

    @Override
    protected boolean parseBytesImpl(byte[] bytes) {
        int offset = 0, length = Float.BYTES;
        cpuTemp = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        irTemp = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        ldmTemp = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mainBoardTemp = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        tecTemp = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        imuTemp = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        rgbTemp = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        irLeftTemp = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        irRightTemp = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        chipTopTemp = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        chipBottomTemp = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        return true;
    }

    @Override
    protected boolean wrapBytesImpl(byte[] bytes) {
        return true;
    }

    @Override
    public String toString() {
        return "DeviceTemperature{" +
                "cpuTemp=" + cpuTemp +
                ", irTemp=" + irTemp +
                ", ldmTemp=" + ldmTemp +
                ", mainBoardTemp=" + mainBoardTemp +
                ", tecTemp=" + tecTemp +
                ", imuTemp=" + imuTemp +
                ", rgbTemp=" + rgbTemp +
                ", irLeftTemp=" + irLeftTemp +
                ", irRightTemp=" + irRightTemp +
                ", chipTopTemp=" + chipTopTemp +
                ", chipBottomTemp=" + chipBottomTemp +
                '}';
    }
}
