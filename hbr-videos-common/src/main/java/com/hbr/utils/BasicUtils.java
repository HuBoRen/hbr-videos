package com.hbr.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.hbr.utils.RedisOperator;

/**
 * 定义一些常量
 * @author huboren
 *
 */
@RestController
public class BasicUtils {
	
	@Autowired
	public RedisOperator redis;
	//redis名字
	public static final String USER_REDIS_SESSION = "user-redis-session";
	// 文件保存的命名空间
     public static final String FILE_SPACE = "C:/hbr_videos";
  // ffmpeg所在目录
 	public static final String FFMPEG_EXE = "C:\\ffmpeg\\bin\\ffmpeg.exe";
 	//定义每页五行数据
 	public static final Integer PAGE_SIZE=5;
	
}
