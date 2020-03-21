package com.hbr.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * 视频截图，作为封面
 * @author huboren
 *
 */
public class FetchVideoCover {
	//ffmpeg命令路径
	private String ffmpegEXE;

	public void getCover(String videoInputPath, String coverOutputPath) throws IOException, InterruptedException {
		//ffmpeg.exe -ss 00:00:01 -i spring.mp4 -vframes 1 bb.jpg
		List<String> command = new java.util.ArrayList<String>();
		//ffmpeg执行命令
		command.add(ffmpegEXE);
		//将视频截成一秒
		command.add("-ss");
		command.add("00:00:01");
		//覆盖
		command.add("-y");
		//将一秒的视频覆盖下面那个视频
		command.add("-i");
		command.add(videoInputPath);
		//因为一秒视频可能有20多帧，所以截取1帧
		command.add("-vframes");
		command.add("1");
		//这是截取之后的截图
		command.add(coverOutputPath);
		//将这个数组进行遍历，然后输出
		for (String c : command) {
			System.out.print(c + " ");
		}
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

	public String getFfmpegEXE() {
		return ffmpegEXE;
	}

	public void setFfmpegEXE(String ffmpegEXE) {
		this.ffmpegEXE = ffmpegEXE;
	}

	public FetchVideoCover() {
		super();
	}

	public FetchVideoCover(String ffmpegEXE) {
		this.ffmpegEXE = ffmpegEXE;
	}
	
	
}