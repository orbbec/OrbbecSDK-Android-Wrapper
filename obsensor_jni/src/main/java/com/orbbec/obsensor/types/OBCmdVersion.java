package com.orbbec.obsensor.types;

/**
 * \if English
 *
 * @brief Command version associate with property id
 * \else
 * @brief 与属性ID关联的协议版本
 * \endif
 */
public enum OBCmdVersion {
    /**
     * \if English
     * version 1.0
     * \else
     * 版本1.0
     * \endif
     */
    CMD_VERSION_V0(0),
    /**
     * \if English
     * version 2.0
     * \else
     * 版本2.0
     * \endif
     */
    CMD_VERSION_V1(1),
    /**
     * \if English
     * version 3.0
     * \else
     * 版本3.0
     * \endif
     */
    CMD_VERSION_V2(2),
    /**
     * \if English
     * version 4.0
     * \else
     * 版本4.0
     * \endif
     */
    CMD_VERSION_V3(3),

    OB_CMD_VERSION_NOVERSION(0xfffe),
    /**
     * \if English
     * Invalid version
     * \else
     * 无效版本
     * \endif
     */
    OB_CMD_VERSION_INVALID(0xffff),
    ;

    private final int mValue;

    OBCmdVersion(int value) {
        this.mValue = value;
    }
}
