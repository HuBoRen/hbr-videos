package com.hbr.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 将视频和bgm进行合并，然后生成一个新的视频
 * @author huboren
 *
 */
public class MixVideoMp3 {
	//ffmpeg的命令路径，以构造方法的形式注入
	private String ffmpegEXE;
	
	public MixVideoMp3(String ffmpegEXE) {
		super();
		this.ffmpegEXE = ffmpegEXE;
	}
	
	public void convertor(String videoInputPath, String mp3InputPath,
			double seconds, String videoOutputPath) throws Exception {
//		ffmpeg.exe -i e083f698ed3ae0f99051368b54636b7c.mp4 -i hhhh.mp3 -t 3  -y 新的视频.mp4
		List<String> command = new ArrayList<>();
		//ffmpeg执行命令
		command.add(ffmpegEXE);
		//原视频路径
		command.add("-i");
		command.add(videoInputPath);
		//需要合并的bgm路径
		command.add("-i");
		command.add(mp3InputPath);
		//合并过后的秒数，这里是以视频秒数为主
		command.add("-t");
		command.add(String.valueOf(seconds));
		//覆盖命令
		command.add("-y");
		//得到的新的视频路径
		command.add(videoOutputPath);
		

		//执行这个命令
		ProcessBuilder builder = new ProcessBuilder(command);
		Process process = builder.start();
		//因为执行ffmpeg这个命令，这个过程中会存在很多流的对象，所以为了不让他堵塞，要将它进行关闭
		InputStream errorStream = process.getErrorStream();
		//将这些流放在读入流中逐个读出来
		InputStreamReader inputStreamReader = new InputStreamReader(errorStream);
		//这是一个缓冲流
		BufferedReader br = new BufferedReader(inputStreamReader);
		String line = "";
		//根据源码，当读取的这个字符串变成null的视频，证明已经全部读取完成
		while ( (line = br.readLine()) != null ) {
		}
		//最后判断这个缓冲流是否存在，如果不为空的话，证明存在，将它关闭即可
		if (br != null) {
			br.close();
		}
		if (inputStreamReader != null) {
			inputStreamReader.close();
		}
		if (errorStream != null) {
			errorStream.close();
		}
		
	}

	

}
