package com.orbbec.obsensor.types;

/**
 * \if English
 * Depth work mode
 * \else
 * 相机深度工作模式
 * \endif
 */
public class DepthWorkMode {
    /**
     * \if English
     * Checksum of work mode
     * \else
     * 工作模式的校验和
     * \endif
     */
    private byte[] checksum = new byte[16];
    /**
     * \if English
     * Name of work mode
     * \else
     * 工作模式的名称
     * \endif
     */
    private String name;

    /**
     * \if English
     * Preset tag
     * \else
     * 预置标签
     * \endif
     */
    int tag;

    public byte[] getChecksum() {
        return checksum;
    }

    public void setChecksum(byte[] checksum) {
        this.checksum = checksum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DepthWorkModeTag getTag() {
        return DepthWorkModeTag.get(tag);
    }

    public String getChecksumHex() {
        StringBuilder builder = new StringBuilder();
        char[] cArray = "0123456789abcdef".toCharArray();

        for (byte b : checksum) {
            int value = (int) b & 0xff;
            builder.append(cArray[value >>> 4]);
            builder.append(cArray[value & 0x0f]);
        }

        return builder.toString();
    }

    @Override
    public String toString() {
        return "name: " + name +
                ", checksum: " + getChecksumHex();
    }
}
