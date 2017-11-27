package com.tingfeng.util.java.base.common.utils.verificationCode;

import java.awt.image.BufferedImage;

/**
 * 
 * @author huitoukest
 *
 */
public class VerificationCodeInfo {
	private String content;
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
