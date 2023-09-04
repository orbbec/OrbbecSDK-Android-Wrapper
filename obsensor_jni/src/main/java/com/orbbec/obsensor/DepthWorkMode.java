package com.orbbec.obsensor;

/**
 * \if English
 * @brief Depth work mode
 * \else
 * 相机深度工作模式
 * \endif
 */
public class DepthWorkMode {
    // 工作模式的名称
    private String name;
    // 工作模式的checksum
    private byte checksum[] = new byte[16];

    /** \if English
     * Get name of work mode
     * \else
     * 获取深度工作模式名称名称
     * \endif
     */
    public String getName() {
        return name;
    }
    /** \if English
     * Set name of work mode
     * \else
     * 设置深度工作模式名称名称
     * \endif
     */
    public void setName(String name) {
        this.name = name;
    }

    /** \if English
     *  Get checksum of work mode
     *  \else
     *  获取相机深度模式对应哈希二进制数组
     *  \endif */
    public byte[] getChecksum() {
        return checksum;
    }

    /** \if English
     * Set checksum of work mode
     * \else 相
     * 设置机深度模式对应哈希二进制数组
     * \endif */
    public void setChecksum(byte[] checksum) {
        this.checksum = checksum;
    }

    /**
     * \if English
     * Get hex string which come from getChecksum()
     * \else
     * 获取基于getChecksum()生产的hex字符串
     * \endif
     */
    public String getChecksumHex() {
        StringBuilder builder = new StringBuilder();
        char cArray[] = "0123456789abcdef".toCharArray();

        for (byte b : checksum) {
            int value = (int)b & 0xff;
            builder.append(cArray[value >>> 4]);
            builder.append(cArray[value & 0x0f]);
        }

        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("name: ").append(name);
        builder.append(", checksum: ").append(getChecksumHex());
        return builder.toString();
    }
}