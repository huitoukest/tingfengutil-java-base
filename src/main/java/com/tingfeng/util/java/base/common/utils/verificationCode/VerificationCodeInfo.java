package com.tingfeng.util.java.base.common.utils.verificationCode;

import java.awt.image.BufferedImage;

/**
 * 验证码的配置信息
 * @author huitoukest
 *
 */
public class VerificationCodeInfo {
	/**
	 * 验证码的内容
	 */
	private String content;
	/**
	 * 验证码的图片对象
	 */
	private BufferedImage image;
	
	/**
	 * 
	 * @param content
	 * @param image
	 */
	public VerificationCodeInfo(String content, BufferedImage image) {
		super();
		this.content = content;
		this.image = image;
	}
	/**
	 * 
	 * @param content
	 * @param image
	 */
	public VerificationCodeInfo(char[] content, BufferedImage image) {
		this(new String(content),image);
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public BufferedImage getImage() {
		return image;
	}
	public void setImage(BufferedImage image) {
		this.image = image;
	}
	
}
