package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

/**
 * \if English
 * Temperature parameters of the device (unit: Celsius)
 * \else
 * 设备温度参数（单位：摄氏度）
 * \endif
 */
public class DeviceTemperature implements ByteConversion {
    /**
     * \if English
     * CPU temperature
     * \else
     * CPU 温度
     * \endif
     */
    @StructField(offset = 0, size = 4)
    private float cpuTemp;
    /**
     * \if English
     * IR temperature
     * \else
     * IR 温度
     * \endif
     */
    @StructField(offset = 4, size = 4)
    private float irTemp;
    /**
     * \if English
     * Laser temperature
     * \else
     * 激光器温度
     * \endif
     */
    @StructField(offset = 8, size = 4)
    private float ldmTemp;
    /**
     * \if English
     * Motherboard temperature
     * \else
     * 主板温度
     * \endif
     */
    @StructField(offset = 12, size = 4)
    private float mainBoardTemp;
    /**
     * \if English
     * TEC temperature
     * \else
     * TEC 温度
     */
    @StructField(offset = 16, size = 4)
    private float tecTemp;
    /**
     * \if English
     * IMU temperature
     * \else
     * IMU 温度
     * \endif
     */
    @StructField(offset = 20, size = 4)
    private float imuTemp;
    /**
     * \if English
     * RGB temperature
     * \else
     * RGB 温度
     * \endif
     */
    @StructField(offset = 24, size = 4)
    private float rgbTemp;
    /**
     * \if English
     * Left IR temperature
     * \else
     * 左IR温度
     * \endif
     */
    @StructField(offset = 28, size = 4)
    private float irLeftTemp;
    /**
     * \if English
     * Right IR temperature
     * \else
     * 右IR温度
     * \endif
     */
    @StructField(offset = 32, size = 4)
    private float irRightTemp;
    /**
     * \if English
     * MX6600 top temperature
     * \else
     * MX6600 芯片上温度
     * \endif
     */
    @StructField(offset = 36, size = 4)
    private float chipTopTemp;
    /**
     * \if English
     * MX6600 bottom temperature
     * \else
     * MX6600 芯片下温度
     * \endif
     */
    @StructField(offset = 40, size = 4)
    private float chipBottomTemp;

    private byte[] mBytes;

    public byte[] BYTES() {
        if (mBytes == null) {
            mBytes = new byte[44];
        }
        return mBytes;
    }

    @Override
    public boolean parseBytes() {
        return StructParser.parseBytes(this, mBytes);
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
