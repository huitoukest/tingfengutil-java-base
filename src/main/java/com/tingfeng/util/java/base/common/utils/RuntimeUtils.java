package com.tingfeng.util.java.base.common.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class RuntimeUtils {
	/**
	 * 获取当前操作系统名称. return 操作系统名称 例如:windows xp,linux 等.
	 */
	public static String getOSName() {
		return System.getProperty("os.name").toLowerCase();
	}

	public static List<InetAddress> getLocalAddress(){
		List<InetAddress> addressList = new ArrayList<>();
		try {
			Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = netInterfaces.nextElement();
				Enumeration<InetAddress> inetAddressIterator = ni.getInetAddresses();
				while(inetAddressIterator.hasMoreElements()) {
					addressList.add(inetAddressIterator.nextElement());
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return addressList;
	}

	/**
	 *
	 * @param exceptPhysicalAddress 是否排除物理地址
	 * @return
	 */
	public static List<String> getLocalIpV4Addresses(boolean exceptPhysicalAddress){
		List<InetAddress> addresses = getLocalAddress();
		return addresses.stream()
				.map(it -> it.getHostAddress())
				.filter(it -> !exceptPhysicalAddress || RegExpUtils.isMatch(RegExpUtils.PatternStr.ipV4,it,true)).distinct().collect(Collectors.toList());
	}

	/**
	 * 获取ipv4地址，不包含物理地址
	 * @return
	 */
	public static List<String> getLocalIpV4Addresses(){
		return getLocalIpV4Addresses(true);
	}

}
