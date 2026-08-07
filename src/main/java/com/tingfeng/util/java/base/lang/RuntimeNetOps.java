package com.tingfeng.util.java.base.lang;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 网络信息相关操作实现。
 *
 * 包级私有，不对外暴露。通过 {@link RuntimeUtils} 对外提供统一 API。
 */
class RuntimeNetOps {

    /**
     * 获取本地所有IP地址
     */
    static List<InetAddress> getLocalAddress() {
        List<InetAddress> addressList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> inetAddressIterator = ni.getInetAddresses();
                while (inetAddressIterator.hasMoreElements()) {
                    addressList.add(inetAddressIterator.nextElement());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return addressList;
    }

    /**
     * 获取本地IPv4地址列表
     * 无论参数取值，始终只返回 IPv4 地址，IPv6 地址不会混入结果
     * @param excludePhysicalAddress 是否排除回环/虚拟网卡地址（如 127.0.0.1）
     *        为 true 时仅返回非回环 IPv4；为 false 时返回全部 IPv4（含回环地址）
     */
    static List<String> getLocalIpV4Addresses(boolean excludePhysicalAddress) {
        List<InetAddress> addresses = getLocalAddress();
        return addresses.stream()
                .map(InetAddress::getHostAddress)
                .filter(it -> excludePhysicalAddress ? isValidIPv4(it) : isIPv4(it))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 判断是否是有效的IPv4地址
     * 使用Inet4Address验证，避免正则匹配的性能开销
     * @param ip IP地址字符串
     * @return 是否有效IPv4
     */
    private static boolean isValidIPv4(String ip) {
        if (ip == null) {
            return false;
        }
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address instanceof Inet4Address && !address.isLoopbackAddress();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断指定字符串是否为 IPv4 地址（含回环地址）
     * 与 isValidIPv4 的区别：本方法不排除回环地址，用于 false 参数下保留全部 IPv4
     * @param ip IP地址字符串
     * @return 是否为 IPv4 地址
     */
    private static boolean isIPv4(String ip) {
        if (ip == null) {
            return false;
        }
        try {
            return InetAddress.getByName(ip) instanceof Inet4Address;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取本地IPv4地址列表（不包含物理地址）
     */
    static List<String> getLocalIpV4Addresses() {
        return getLocalIpV4Addresses(true);
    }

    // ==================== 网络信息增强 ====================

    /**
     * 获取主机名
     * @return 主机名
     */
    static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取本机MAC地址列表
     * @return MAC地址列表（格式如：00:1A:2B:3C:4D:5E）
     */
    static List<String> getMacAddresses() {
        List<String> macAddressList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                if (ni.isUp() && !ni.isLoopback()) {
                    byte[] hardwareAddress = ni.getHardwareAddress();
                    if (hardwareAddress != null && hardwareAddress.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < hardwareAddress.length; i++) {
                            sb.append(String.format("%02X%s", hardwareAddress[i],
                                    (i < hardwareAddress.length - 1) ? ":" : ""));
                        }
                        macAddressList.add(sb.toString());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return macAddressList;
    }

    /**
     * 获取本地IPv6地址列表
     * @return IPv6地址列表
     */
    static List<String> getLocalIPv6Addresses() {
        List<InetAddress> addresses = getLocalAddress();
        return addresses.stream()
                .map(InetAddress::getHostAddress)
                .filter(it -> it != null && it.contains(":"))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 判断指定网卡是否启用
     * @param interfaceName 网卡名称（如"eth0", "en0"）
     * @return 是否启用
     */
    static boolean isNetworkInterfaceUp(String interfaceName) {
        try {
            NetworkInterface ni = NetworkInterface.getByName(interfaceName);
            return ni != null && ni.isUp();
        } catch (Exception e) {
            return false;
        }
    }

}
