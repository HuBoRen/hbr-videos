package com.hbr.utils;

import java.security.MessageDigest;
import org.apache.commons.codec.binary.Base64;

/**
 * 这是一个md5加密的一个工具类
 * @author huboren
 *
 */
public class MD5Utils {

	/**
	 * @Description: 对字符串进行md5加密
	 */
	public static String getMD5Str(String strValue) throws Exception {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		String newstr = Base64.encodeBase64String(md5.digest(strValue.getBytes()));
		return newstr;
	}

}
