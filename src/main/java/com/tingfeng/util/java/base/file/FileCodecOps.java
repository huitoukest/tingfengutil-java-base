package com.tingfeng.util.java.base.file;

import java.io.IOException;
import java.io.OutputStream;

import com.tingfeng.util.java.base.lang.Base64Utils;
import com.tingfeng.util.java.base.lang.StringUtils;
import com.tingfeng.util.java.base.lang.exception.BaseException;
import com.tingfeng.util.java.base.lang.exception.StreamCloseException;

/**
 * Base64 图片编解码操作实现。
 * <p>包级私有，不对外暴露。通过 {@link FileUtils} 对外提供统一 API。</p>
 */
class FileCodecOps {

	private static final String BASE64_IMG_HEADER_START = "data:image/";
	private static final String BASE64_IMG_HEADER_END = ";base64";

	// ==================== Base64 图片编解码 ====================

	/**
	 * 通过base64的字符串来获取文件名data:image/png;
	 * @param imgStr 如果没有获取到或者内容是空则返回空串
	 * @return 文件扩展名称
	 */
	static String getExtensionNameByBase64Img(String imgStr) {
		if (StringUtils.isNotEmpty(imgStr)) {
			int start = imgStr.indexOf(BASE64_IMG_HEADER_START) + 11;
			int end = imgStr.indexOf(BASE64_IMG_HEADER_END);
			if (start <= end) {
				return imgStr.substring(start, end);
			}
		}
		return "";
	}

	/**
	 * 上传成功返回true，否则返回false;并且会自动关闭输出流
	 * @param fileStr base64编码字符串
	 * @param out 输出流
	 * @apiNote 注意：此方法会在 finally 块中关闭传入的 OutputStream
	 */
	static void saveBase64File(String fileStr, OutputStream out) {
		//对字节数组字符串进行Base64解码并生成图片
		if (fileStr == null) {
			//图像数据为空
			throw new BaseException("文件内容不能为空！");
		}
		try {
			fileStr = getBase64ImgFileContent(fileStr);
			//Base64解码
			byte[] content = Base64Utils.deCode(fileStr);
			for (int i = 0; i < content.length; ++i) {
				if (content[i] < 0) {
					content[i] += 256;//调整异常数据
				}
			}
			//生成jpeg图片
			out.write(content);
			out.flush();
		} catch (IOException e) {
			throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					throw new StreamCloseException(e);
				}
			}
		}
	}

	/**
	 * 获取base64的图片的内容信息
	 * @param fileStr base64编码字符串
	 * @return 去除base64文件名称和扩展名信息后的内容
	 */
	static String getBase64ImgFileContent(String fileStr) {
		int flag = fileStr.indexOf(",");
		if (flag <= 0) {
			throw new BaseException("不是base64的图片文件");
		}
		return fileStr.substring(flag + 1);
	}
}
