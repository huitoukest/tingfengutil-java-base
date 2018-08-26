package com.tingfeng.util.java.base.common.utils.verificationCode;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * 验证码工具类
 * 1.加入干扰线
 * 2.加入干扰框
 * 3.加入干扰点
 * #4.加入干扰字符
 * @author huitoukest
 *
 */
public class VerificationCodeUtil {
	private final static Random rand = new Random();
	private final static char[] baseCode = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'P',
			'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
	private final static int baseCodeCount = baseCode.length;

	private final static int MIN_ALPHA = 15;
	private final static int MAX_ALPHA = 35;
	
	public static void main(String[] args) {

		JFrame jFrame = new JFrame();
		jFrame.setBounds(400, 400, 250, 250);

		VerificationCodeInfo info = getSimpleVerificationCode(4, 40, 160);	
		System.out.println(info.getContent());
		ImageIcon img = new ImageIcon(info.getImage());
		JLabel background = new JLabel(img);
		jFrame.add(background);
		jFrame.setVisible(true);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * 获取一个验证码
	 * 
	 * @param length
	 *            内容长度
	 * @param height
	 *            高度
	 * @param width
	 *            宽度
	 */
	public static VerificationCodeInfo getSimpleVerificationCode(int length, int height, int width) {
		char[] content = getContent(length);
		BufferedImage image = getVerificationCode(content, height, width, 30,
				new Font("Atlantic Inline", Font.PLAIN, getIntRandom(22, 24)), 15, 4, 50);
		VerificationCodeInfo info = new VerificationCodeInfo(content, image);
		return info;
	}

	/**
	 * 获取一个验证码
	 * 
	 * @param content
	 *            内容
	 * @param height
	 *            高度
	 * @param width
	 *            宽度
	 * @param degree
	 *            随机旋转的最大角度(eg. 60°c)
	 * @param font
	 *            字体名称
	 * @param interferenceLineCount
	 *            干扰线数量
	 * @param interferenceBoxCount
	 *            干扰框数量
	 * @param interferencePointCount
	 *            干扰点数量
	 * @return
	 */
	public static BufferedImage getVerificationCode(char[] content, int height, int width, int degree, Font font,
			int interferenceLineCount, int interferenceBoxCount, int interferencePointCount) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		// 获取图形上下文
		Graphics g = image.getGraphics();
		// 设定背景色
		Color background = getRandomColor(180);
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		// 画边框
		g.setColor(background);
		g.drawRect(0, 0, width - 1, height - 1);
		int[] xs = getRadonWidths(width, content.length);
		int[] ys = getRadomHeights(height, content.length);
		for (int i = 0; i < content.length; i++) {
			int tmpDegree = getRandomDegree(degree);
			g.setColor(getRandomColor(background,255));
			g.setFont(font);
			String dContent = "" + content[i];
			RotateString(dContent, xs[i], ys[i], g, tmpDegree);
		}
		// 随机画几条线
		drawInterferenceLine(width, height, interferenceLineCount, g);
		// 画随机干扰框
		drawInterferenceBox(g, width, height, interferenceBoxCount);
		// 画干扰点
		drawInterferencePoint(width, height,interferencePointCount, g);
		// 释放图形上下文
		g.dispose();
		return image;
	}
	
	/**
	 * 得到一个随机旋转角度
	 * @param maxDegree
	 * @return
	 */
	private static int getRandomDegree(int maxDegree) {
		int type = rand.nextInt(2);
		if (type == 0)
		{
			return 360 -  rand.nextInt(maxDegree);
		}else
			{
			return rand.nextInt(maxDegree);
			}
	}

	/**
	 * 旋转并且画出指定字符串
	 * 
	 * @param s
	 *            需要旋转的字符串
	 * @param x
	 *            字符串的x坐标
	 * @param y
	 *            字符串的Y坐标
	 * @param g
	 *            画笔g
	 * @param degree
	 *            旋转的角度
	 */
	private static void RotateString(String s, int x, int y, Graphics g, int degree) {
		Graphics2D g2d = (Graphics2D) g.create();
		// 平移原点到图形环境的中心 ,这个方法的作用实际上就是将字符串移动到某一个位置
		// g2d.translate(x-1, y+3);
		g2d.translate(getIntRandom(x, x + 2), getIntRandom(y, y + 2));
		// 旋转文本
		g2d.rotate(degree * Math.PI / 180);
		// 特别需要注意的是,这里的画笔已经具有了上次指定的一个位置,所以这里指定的其实是一个相对位置
		g2d.drawString(s, 0, 0);
	}

	/**
	 * 随机产生干扰点
	 * 
	 * @param width
	 * @param height
	 * @param many
	 * @param g
	 *            透明度0~255 0表示全透
	 */
	private static void drawInterferencePoint(int width, int height, int many, Graphics g) { // 随机产生干扰点
		for (int i = 0; i < many; i++) {
			int x = rand.nextInt(width);
			int y = rand.nextInt(height);
			g.setColor(getRandomColor(200));
			g.drawOval(x, y, rand.nextInt(4), rand.nextInt(4));
		}
	}

	/**
	 * 随机产生干扰线条
	 * 
	 * @param width
	 * @param height
	 * @param minMany
	 *            最少产生的数量
	 * @param g
	 *            透明度0~255 0表示全透
	 */
	private static void drawInterferenceLine(int width, int height, int minMany, Graphics g) { // 随机产生干扰线条
		for (int i = 0; i < minMany; i++) {
			int x1 = getIntRandom(0,width);
			int y1 = getIntRandom(0,height);
			int x2 = getIntRandom(0,width);
			int y2 = getIntRandom(0, height);
			g.setColor(getRandomColor(getIntRandom(MIN_ALPHA, MAX_ALPHA)));
			g.drawLine(x1, y1, x2, y2);
		}
	}

	/**
	 * 由随机产生的方块来作为干扰背景
	 */
	private static void drawInterferenceBox(Graphics g, int width, int height, int count) {
		// 随机产生干扰线条
		for (int i = 0; i < getIntRandom(count, count + 2); i++) {
			int x1 = getIntRandom(0, width);
			int y1 = getIntRandom(0, height);
			int x2 = getIntRandom(0, width);
			int y2 = getIntRandom(0, height);
			g.setColor(getRandomColor(100));
			int w = x2 - x1;
			int h = y2 - y1;
			if (w < 0) {
				w = -w;
			}				
			if (h < 0) {
				h = -h;
			}	
			g.drawRect(Math.min(x1,x2),Math.min(y1,y2), w, h);
			g.setColor(getRandomColor(getIntRandom(MIN_ALPHA, MAX_ALPHA)));
			g.fillRect(x1, y1, w, h);
		}
	}

	/***
	 * @return 随机返一个指定区间的数字
	 */
	private static int getIntRandom(int start, int end) {
		if (end < start) {
			int t = end;
			end = start;
			start = t;
		}
		int i = start + (int) (Math.random() * (end - start));
		return i;
	}

	@SuppressWarnings("unused")
	private int getIntRandom(double start, double end) {
		if (end < start) {
			double t = end;
			end = start;
			start = t;
		}
		double i = start + (int) (Math.random() * (end - start));
		return (int) i;
	}

	/***
	 * 随机返回一种颜色,透明度0~255 0表示全透
	 * 
	 * @return 随机返回一种颜色
	 * @param alpha
	 *            透明度0~255 0表示全透
	 */
	private static Color getRandomColor(int alpha) {
		int R = (int) (Math.random() * 255);
		int G = (int) (Math.random() * 255);
		int B = (int) (Math.random() * 255);
		return new Color(R, G, B, alpha);
	}

	/***
	 * @return 随机返回一种颜色,与给定颜色相类似
	 * @param alpha
	 *            透明度0~255 0表示全透
	 */
	private static Color getRandomColor(Color c, int alpha) {
		int r = getIntRandom(-160, 160);
		int g = getIntRandom(-160, 160);
		int b = getIntRandom(-160, 160);
		r = getCloserRandomColorValue(c.getRed(), r);
		b = getCloserRandomColorValue(c.getBlue(), b);
		g = getCloserRandomColorValue(c.getGreen(), g);
		return new Color(r, g, b, alpha);
	}

	/**
	 * 在颜色值和给定的随机数之间返回一个随机颜色值0~255
	 * 
	 * @param colorValue
	 * @param randomValue
	 * @return
	 */
	private static int getCloserRandomColorValue(int colorValue, int randomValue) {
		if (colorValue + randomValue > 255) {
			return getCloserRandomColorValue(colorValue, randomValue - getIntRandom(1, randomValue + 20));
		} else if (colorValue + randomValue < 0) {
			return getCloserRandomColorValue(colorValue, randomValue + getIntRandom(1, randomValue + 20));
		} else if (Math.abs(randomValue) < 60) {
			return getCloserRandomColorValue(colorValue, getIntRandom(-255, 255));
		} else {
			return colorValue + randomValue;
		}
	}

	/**
	 * 对图片选择,这里保留以方便以后使用
	 * 
	 * @param bufferedimage
	 * @param degree
	 * @return 一张旋转后的图片
	 */
	public static BufferedImage rolateImage(BufferedImage bufferedimage, int degree, Color backGround) {
		BufferedImage img;
		int w = bufferedimage.getWidth();
		int h = bufferedimage.getHeight();
		int type = BufferedImage.TYPE_INT_RGB;
		Graphics2D graphics2d;
		graphics2d = (img = new BufferedImage(w, h, type)).createGraphics();
		graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		graphics2d.rotate(Math.toRadians(degree), w / 2, h / 2);
		graphics2d.drawImage(bufferedimage, null, null);
		return img;
	}

	
	
	private static int getRadomHeight(int fullHeight) {
		return getIntRandom((int) (fullHeight * 0.7), (int) (fullHeight * 0.95));
	}
	
	/**
	 * 
	 * @param many
	 * @return 画图的时候随机的高度的数组
	 */
	private static int[] getRadomHeights(int height, int many) {
		int[] temp = new int[many];
		for (int i = 0; i < many; i++) {
			temp[i] = getRadomHeight(height);
		}
		return temp;
	}

	/**
	 * 
	 * @param many
	 * @return 画图的时候起始x坐标的数组
	 */
	private static int[] getRadonWidths(int width, int many) {
		int[] temp = new int[many];
		for (int i = 0; i < many; i++) {
			if (i == 0) {
				temp[i] = getRadonWidth(many, 0, width);
			}else {
				temp[i] = getRadonWidth(many, temp[i - 1], width);
			}		
		}
		return temp;
	}
	
	private static int getRadonWidth(int many, int minWidth, int maxWidth) {
		int minJianju = maxWidth / (many + 2);
		int maxJianju = maxWidth / (many);
		int temp = maxJianju - minJianju;
		// 在的规定的范围内产生一个随机数
		return (int) (Math.random() * temp) + minWidth + minJianju;
	}

	/**
	 * 随机获取验证码的内容
	 * 
	 * @param length
	 *            指定验证码的长度
	 * @return
	 */
	private static char[] getContent(int length) {
		char[] content = new char[length];
		for (int i = 0; i < length; i++) {
			char ch = baseCode[rand.nextInt(baseCodeCount)];
			content[i] = ch;
		}
		return content;
	}
	/**
	 * 输出图片,返回验证码内容
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static String write(String path) throws IOException {
		OutputStream sos = new FileOutputStream(path);
			return write(sos);
	}
	/**
	 * 
	 * @param out
	 * @return
	 * @throws IOException
	 */
	public static String write(OutputStream out) throws IOException {
		VerificationCodeInfo info = getSimpleVerificationCode(4, 40, 160);			
		try {
		ImageIO.write(info.getImage(), "png", out);
		}finally {
			if(null != out) {
				out.close();
			}
		}
		return info.getContent();
	}
}
