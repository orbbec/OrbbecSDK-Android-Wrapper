package com.orbbec.obsensor;

import android.text.TextUtils;
import android.util.Log;

/**
 * \if English
 * Network config of IP v4
 * \else
 * 网络配置参数，IP v4
 * \endif
 */
public class OBNetworkConfig {
    private static final String TAG = "OBNetworkConfig";

    private boolean dhcpEnable;
    private int ipValue[] = new int[4];
    private int maskValue[] = new int[4];
    private int gatewayValue[] = new int[4];

    /**
     * \if English
     * @brief DHCP status
     *
     * @note true: static IP; false: DHCP
     * \else
     * @brief DHCP 状态
     *
     * @note true: 静态IP； false：DHCP
     * \endif
     */
    public boolean isDhcpEnable() {
        return dhcpEnable;
    }

    /**
     * \if English
     * @brief DHCP status
     * @param dhcpEnable
     * \else
     * @brief DHCP 状态
     * \endif
     */
    public void setDhcpEnable(boolean dhcpEnable) {
        this.dhcpEnable = dhcpEnable;
    }

    /**
     * \if English
     * IP address, IP v4
     * \else
     * IP地址，IP v4
     * \endif
     */
    public String getIP() {
        return ipValueToText(ipValue);
    }

    /**
     * \if English
     * IP address, IP v4
     * \else
     * IP地址，IP v4
     * \endif
     */
    public void setIP(String ip) {
        int tmp[] = textToIpValue(ip);
        if (null != tmp) {
            ipValue = tmp;
        }
    }

    /**
     * \if English
     * Mask, IP v4
     * \else
     * 子网掩码, IP v4
     * \endif
     */
    public String getMask() {
        return ipValueToText(maskValue);
    }

    /**
     * \if English
     * Mask, IP v4
     * \else
     * 子网掩码, IP v4
     * \endif
     */
    public void setMask(String mask) {
        int tmp[] = textToIpValue(mask);
        if (null != tmp) {
            maskValue = tmp;
        }
    }

    /**
     * \if English
     * Gateway, IP v4
     * \else
     * 网关，IP v4
     * \endif
     */
    public String getGateway() {
        return ipValueToText(gatewayValue);
    }

    /**
     * \if English
     * Gateway, IP v4
     * \else
     * 网关，IP v4
     * \endif
     */
    public void setGateway(String gateway) {
        int tmp[] = textToIpValue(gateway);
        if (null != tmp) {
            gatewayValue = tmp;
        }
    }

    private String ipValueToText(int ipValue[]) {
        if (ipValue.length !=4) {
            Log.w(TAG, "ipValueToText. Invalid ipValue length=" + ipValue.length);
            return "";
        }

        boolean failed = false;
        StringBuilder builder = new StringBuilder();
        for (int  i = 0; i < ipValue.length; i++) {
            if (i > 0) {
                builder.append('.');
            }
            int value = ipValue[i];
            if (value < 0 || value > 255) {
                failed = true;
            } else {
                builder.append(String.valueOf(value));
            }
        }
        if (failed) {
            Log.w(TAG, "ipValueToText. Invalid ipValue out of range[0-255]");
            return "";
        }
        return builder.toString();
    }

    private int[] textToIpValue(String text) {
        if (!isValidIPv4(text)) {
            Log.w(TAG, "textToIpValue failed. invalid text: " + text);
            return null;
        }

        int retIpValue[] = new int[4];
        boolean failed = false;
        String splits[] = text.split("[.]");
        for (int i = 0; i < splits.length; i++) {
            int value = Integer.valueOf(splits[i]);
            if (value < 0 || value > 255) {
                failed = true;
                break;
            } else {
                retIpValue[i] = value;
            }
        }

        if (failed) {
            Log.w(TAG, "textToIpValue failed. out of range[0-255], text: " + text);
            return null;
        }
        return retIpValue;
    }

    private static boolean isValidIPv4(String ipAddress) {
        if (TextUtils.isEmpty(ipAddress)) {
            return false;
        }

        String pattern = "^((25[0-5]|2[0-4]\\d|[01]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[01]?\\d?\\d)){3})$";
        return ipAddress.matches(pattern);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("dhcpEnable: ").append(dhcpEnable)
                .append(", ip: ").append(ipValueToText(ipValue))
                .append(", mask: ").append(ipValueToText(maskValue))
                .append(", gateway: ").append(ipValueToText(gatewayValue));
        return builder.toString();
    }
}
