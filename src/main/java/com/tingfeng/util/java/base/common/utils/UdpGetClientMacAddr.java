package com.tingfeng.util.java.base.common.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class UdpGetClientMacAddr {
	private String sRemoteAddr;
	private int iRemotePort = 137;
	private byte[] buffer = new byte[1024];
	private DatagramSocket ds = null;

	public UdpGetClientMacAddr(String strAddr) throws Exception {
		sRemoteAddr = strAddr;
		ds = new DatagramSocket();
	}

	public UdpGetClientMacAddr() {

	}

	protected final DatagramPacket send(final byte[] bytes) throws IOException {
		DatagramPacket dp = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(sRemoteAddr), iRemotePort);
		ds.send(dp);
		return dp;
	}

	protected final DatagramPacket receive() throws Exception {
		DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
		ds.receive(dp);
		return dp;
	}

	protected byte[] GetQueryCmd() throws Exception {
		byte[] t_ns = new byte[50];
		t_ns[0] = 0x00;
		t_ns[1] = 0x00;
		t_ns[2] = 0x00;
		t_ns[3] = 0x10;
		t_ns[4] = 0x00;
		t_ns[5] = 0x01;
		t_ns[6] = 0x00;
		t_ns[7] = 0x00;
		t_ns[8] = 0x00;
		t_ns[9] = 0x00;
		t_ns[10] = 0x00;
		t_ns[11] = 0x00;
		t_ns[12] = 0x20;
		t_ns[13] = 0x43;
		t_ns[14] = 0x4B;

		for (int i = 15; i < 45; i++) {
			t_ns[i] = 0x41;
		}

		t_ns[45] = 0x00;
		t_ns[46] = 0x00;
		t_ns[47] = 0x21;
		t_ns[48] = 0x00;
		t_ns[49] = 0x01;
		return t_ns;
	}

	protected final String GetMacAddr(byte[] brevdata) throws Exception {

		int i = brevdata[56] * 18 + 56;
		String sAddr = "";
		StringBuffer sb = new StringBuffer(17);

		for (int j = 1; j < 7; j++) {
			sAddr = Integer.toHexString(0xFF & brevdata[i + j]);
			if (sAddr.length() < 2) {
				sb.append(0);
			}
			sb.append(sAddr.toUpperCase());
			if (j < 6)
				sb.append(':');
		}
		return sb.toString();
	}

	public final void close() {
		try {
			ds.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String getLocalMacAddr() {
		String mac = "";
		try {
			 Process process = Runtime.getRuntime().exec("ipconfig /all");
			 InputStreamReader ir = new
			 InputStreamReader(process.getInputStream());
			 LineNumberReader input = new LineNumberReader(ir);
			 String line = null;
			 while ((line = input.readLine()) != null) {
			 if (line.indexOf("Physical Address") > 0) {
			 mac = line.substring(line.indexOf("-") - 2);
			 }
			 }
			// Process process =
			// Runtime.getRuntime().exec("nbtstat -a 127.0.0.1" );
			// InputStreamReader ir = new
			// InputStreamReader(process.getInputStream());
			// LineNumberReader input = new LineNumberReader(ir);
			// String line;
			// while ((line = input.readLine()) != null){
			// if (line.indexOf("MAC Address") > 0) {
			// mac = line.substring(line.indexOf("-") - 2);
			// }
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}
		return mac;
	}

	public String getLocalMac() throws UnknownHostException, SocketException {
		// 获取网卡，获取地址
		InetAddress ia = InetAddress.getLocalHost();
		byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
		System.out.println("mac数组长度：" + mac.length);
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < mac.length; i++) {
			if (i != 0) {
				sb.append("-");
			}
			// 字节转换为整数
			int temp = mac[i] & 0xff;
			String str = Integer.toHexString(temp);
			// System.out.println("每8位:"+str);
			if (str.length() == 1) {
				sb.append("0" + str);
			} else {
				sb.append(str);
			}
		}
		return sb.toString().toUpperCase();
		// System.out.println("本机MAC地址:"+sb.toString().toUpperCase());
	}

	public String getMacAddr() {
		String mac = "";
		String line = "";
		String os = System.getProperty("os.name");
		if (os != null && os.startsWith("Windows")) {
			try {
				String command = "cmd.exe /c ipconfig /all";
				Process p = Runtime.getRuntime().exec(command);
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while ((line = br.readLine()) != null) {
					if (line.indexOf("Physical Address") > 0 || line.indexOf("物理地址") >0 ) {
						int index = line.indexOf(":") + 2;
						mac = line.substring(index).toUpperCase();
						break;
					}
				}
				br.close();
			} catch (IOException e) {
				
			}
		}
		return mac;
	}

	public String getMacAddr(String host) {
		String mac = "";
		StringBuffer sb = new StringBuffer();
		try {
			NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(host));

			byte[] macs = ni.getHardwareAddress();

			for (int i = 0; i < macs.length; i++) {
				mac = Integer.toHexString(macs[i] & 0xFF);
				if (mac.length() == 1) {
					mac = '0' + mac;
				}
				sb.append(mac + "-");
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		mac = sb.toString();
		mac = mac.substring(0, mac.length() - 1);

		return mac;
	}

	public final String GetRemoteMacAddr() throws Exception {
		byte[] bqcmd = GetQueryCmd();
		send(bqcmd);
		DatagramPacket dp = receive();
		String smac = GetMacAddr(dp.getData());
		close();
		return smac;
	}

	/**
	 * 获取当前操作系统名称. return 操作系统名称 例如:windows xp,linux 等.
	 */
	public static String getOSName() {
		return System.getProperty("os.name").toLowerCase();
	}

	/**
	 * 获取unix网卡的mac地址. 非windows的系统默认调用本方法获取.如果有特殊系统请继续扩充新的取mac地址方法.
	 * 
	 * @return mac地址
	 */
	public static String getUnixMACAddress() {
		String mac = null;
		BufferedReader bufferedReader = null;
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("ifconfig eth0");// linux下的命令，一般取eth0作为本地主网卡
			// 显示信息中包含有mac地址信息
			bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			int index = -1;
			while ((line = bufferedReader.readLine()) != null) {
				index = line.toLowerCase().indexOf("hwaddr");// 寻找标示字符串[hwaddr]
				if (index >= 0) {// 找到了
					mac = line.substring(index + "hwaddr".length() + 1).trim();// 取出mac地址并去除2边空格
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			bufferedReader = null;
			process = null;
		}

		return mac;
	}

	/**
	 * 获取widnows网卡的mac地址.
	 * 
	 * @return mac地址
	 */
	public static String getWindowsMACAddress() {
		String mac = null;
		BufferedReader bufferedReader = null;
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("cmd.exe /c ipconfig /all");// windows下的命令，显示信息中包含有mac地址信息
			bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			int index = -1;
			while ((line = bufferedReader.readLine()) != null) {
				index = line.toLowerCase().indexOf("physical address");// 寻找标示字符串[physical
				if (index >= 0) {// 找到了
					index = line.indexOf(":");// 寻找":"的位置
					if (index >= 0) {
						mac = line.substring(index + 1).trim();// 取出mac地址并去除2边空格
					}
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			bufferedReader = null;
			process = null;
		}

		return mac;
	}

	public static void getAllMacAdress() {
		Enumeration<NetworkInterface> netInterfaces = null;

		try {
			// 获得所有网络接口
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				System.out.println("==============================================");
				String mac = "";
				StringBuffer sb = new StringBuffer();
				NetworkInterface ni = netInterfaces.nextElement();
				System.out.println("DisplayName: " + ni.getDisplayName());
				System.out.println("Name: " + ni.getName());

				byte[] macs = ni.getHardwareAddress();
				// 该interface不存在HardwareAddress，继续下一次循环
				if (macs == null) {
					continue;
				}

				for (int i = 0; i < macs.length; i++) {
					mac = Integer.toHexString(macs[i] & 0xFF);
					if (mac.length() == 1) {
						mac = '0' + mac;
					}
					sb.append(mac + "-");
				}
				mac = sb.toString();
				mac = mac.substring(0, mac.length() - 1);
				System.out.println(mac);

				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					System.out.println("IP: " + ips.nextElement().getHostAddress());
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		 /*String address = "";
		 String host = "*.*.*.*";
		 UdpGetClientMacAddr test = new UdpGetClientMacAddr();
		 address = test.getMacAddress();
		 System.out.println("Physical Address is : " + address);
		 address = test.getMacAddress(host);
		 System.out.println("Physical Address is : " + address);
		 String os = getOSName();
		 System.out.println(os);
		 if(os.startsWith("windows")){
		// 本地是windows
		 String mac = getWindowsMACAddress();
		 System.out.println(mac);
		 }else{
		 //本地是非windows系统 一般就是unix
		 String mac = getUnixMACAddress();
		 System.out.println(mac);
		 }
		UdpGetClientMacAddr.getAllMacAdress();*/
	}

}
