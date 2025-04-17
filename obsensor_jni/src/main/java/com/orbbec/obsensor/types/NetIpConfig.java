package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

import java.util.Arrays;

/**
 * \if English
 * IP address configuration for network devices (IPv4)
 * \else
 * 设备网络IP地址配置（IPv4）
 * \endif
 */
public class NetIpConfig implements ByteConversion {
    /**
     * \if English
     * DHCP status
     * \else
     * DHCP状态
     * \endif
     */
    @StructField(offset = 0, size = 2)
    private int mDhcp;
    /**
     * \if English
     * IP address (IPv4, big endian: 192.168.1.10, address[0] = 192, address[1] = 168, address[2] = 1, address[3] = 10)
     * \else
     * IP 地址（IPv4，大端序：192.168.1.10，address[0] = 192, address[1] = 168, address[2] = 1, address[3] = 10）
     * \endif
     */
    @StructField(offset = 2, size = 4, arraySize = 4)
    private short[] mAddress;
    /**
     * \if English
     * Subnet mask (big endian)
     * \else
     * 子网掩码（大端序）
     * \endif
     */
    @StructField(offset = 6, size = 4, arraySize = 4)
    private short[] mMask;
    /**
     * \if English
     * Gateway (big endian)
     * \else
     * 网关（大端序）
     */
    @StructField(offset = 10, size = 4, arraySize = 4)
    private short[] mGateway;

    private byte[] mBytes;

    public byte[] BYTES() {
        if (mBytes == null) {
            mBytes = new byte[14];
        }
        return mBytes;
    }

    @Override
    public boolean parseBytes() {
        return StructParser.parseBytes(this, mBytes);
    }

    @Override
    public boolean wrapBytes(byte[] bytes) {
        return StructParser.wrapBytes(this, bytes);
    }

    @Override
    public String toString() {
        return "NetIpConfig{" +
                "mGateway=" + Arrays.toString(mGateway) +
                ", mMask=" + Arrays.toString(mMask) +
                ", mAddress=" + Arrays.toString(mAddress) +
                ", mDhcp=" + mDhcp +
                '}';
    }
}
