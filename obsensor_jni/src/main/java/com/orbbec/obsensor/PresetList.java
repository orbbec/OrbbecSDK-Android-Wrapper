package com.orbbec.obsensor;

public class PresetList extends LobClass{

    PresetList(long handle) {
        mHandle = handle;
    }

    /**
     * \if English
     * @brief Get the number of preset in the preset list.
     **
     * @return The number of preset in the preset list.
     * \else
     * @brief 获取预置列表中的预置数量
     *
     * @return 预置列表中的预置数量
     * \endif
     */
    public int getCount() {
        throwInitializeException();
        return nGetCount(mHandle);
    }

    /**
     * \if English
     * @brief Get the name of the preset in the preset list.
     * @param index The index of the preset in the preset list.
     *
     * @return The name of the preset in the preset list.
     * \else
     * @brief 获取预置列表中的预置名称
     *
     * @param index 预置列表中的预置索引
     * @return 预置列表中的预置名称
     * \endif
     */
    public String getName(int index) {
        throwInitializeException();
        return nGetName(mHandle, index);
    }

    /**
     * \if English
     * @brief Check if the preset list has the preset.
     * @param presetName The name of the preset.
     *
     * @return Whether the preset list has the preset. If true, the preset list has the preset. If false, the preset list does not have the preset.
     * \else
     * @brief 检查预置列表中是否有该预置
     *
     * @param presetName 预置名称
     * @return 是否有预置
     * \endif
     */
    public boolean hasPreset(String presetName) {
        throwInitializeException();
        return nHasPreset(mHandle, presetName);
    }

    /**
     * \if English
     * resource release
     * \else
     * 资源释放
     * \endif
     */
    @Override
    public void close() {
        throwInitializeException();
        nDelete(mHandle);
        mHandle = 0;
    }

    private native int nGetCount(long handle);

    private native String nGetName(long handle, int index);

    private native boolean nHasPreset(long handle, String presetName);

    private native void nDelete(long handle);
}
