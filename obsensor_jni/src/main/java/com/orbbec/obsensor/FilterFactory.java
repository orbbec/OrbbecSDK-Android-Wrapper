package com.orbbec.obsensor;

/**
 * \if English
 * A factory class for creating filters.
 * \else
 * 滤波工厂类
 * \endif
 */
public class FilterFactory {
    /**
     * \if English
     * @brief Create a filter by name.
     * \else
     * @brief 根据名称创建一个滤波
     * \endif
     */
    public static Filter createFilter(String name) {
        long handle = nCreateFilter(name);
        return handle != 0 ? new Filter(handle) : null;
    }

    /**
     * \if English
     * @brief Create a private filter by name and activation key.
     * @brief Some private filters require an activation key to be activated, its depends on the vendor of the filter.
     *
     * @param name The name of the filter.
     * @param activationKey The activation key of the filter.
     * \else
     * @brief 根据名称和激活码创建一个私有滤波
     * @brief 有些私有滤波需要激活码才能激活，具体激活码由滤波厂商提供
     *
     * @param name 滤波名称
     * @param activationKey 滤波激活码
     * \endif
     */
    public static Filter createPrivateFilter(String name, String activationKey) {
        long handle = nCreatePrivateFilter(name, activationKey);
        return handle != 0 ? new Filter(handle) : null;
    }

    /**
     * \if English
     * @brief Get the vendor specific code of a filter by filter name.
     * @brief A private filter can define its own vendor specific code for specific purposes.
     *
     * @param name The name of the filter.
     * @return The vendor specific code of the filter.
     * \else
     * @brief 根据滤波名称获取滤波厂商的私有码
     * @brief 有些私有滤波可以定义自己的私有码，用于特定用途
     *
     * @param name 滤波名称
     * @return 返回滤波厂商的私有码
     * \endif
     */
    public static String getFilterVendorSpecificCode(String name) {
        return nGetFilterVendorSpecificCode(name);
    }

    private static native long nCreateFilter(String name);

    private static native long nCreatePrivateFilter(String name, String activationKey);

    private static native String nGetFilterVendorSpecificCode(String name);
}
