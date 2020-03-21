package com.hbr.controller;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.annotations.ApiImplicitParams;

import com.hbr.enums.VideoStatusEnum;
import com.hbr.pojo.Bgm;
import com.hbr.pojo.Comments;
import com.hbr.pojo.Videos;
import com.hbr.service.IBgmService;
import com.hbr.service.IVideoService;
import com.hbr.utils.BasicUtils;
import com.hbr.utils.FetchVideoCover;
import com.hbr.utils.HbrJSONResult;
import com.hbr.utils.MixVideoMp3;
import com.hbr.utils.PagesResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 视频相关的hander
 * 因为现在都是前后端分离，所以这里使用了Swagger2工具构建restful接口测试，进行测试后端接口是否成功
 * Swagger2：可以生成文档形式的api
 * @author huboren
 *
 */
@RestController
@Api(value="视频相关的业务",tags={"视频接口"})
@RequestMapping("/video")
public class VideoController extends BasicUtils{
	//注入bgm接口
	@Autowired
	private IBgmService bgmService;
	//注入视频接口
	@Autowired
	private IVideoService videoService;
	
	/**
	 * 上传视频，并且使用ffmpeg进行视频截图，用作封面
	 * @param userId
	 * @param bgmId
	 * @param videoSeconds
	 * @param videoWidth
	 * @param videoHeight
	 * @param desc
	 * @param file
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value="上传视频", notes="上传视频的接口")
	@ApiImplicitParams({
		@ApiImplicitParam(name="userId", value="用户id", required=true, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="bgmId", value="背景音乐id", required=false, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="videoSeconds", value="背景音乐播放长度", required=true, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="videoWidth", value="视频宽度", required=true, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="videoHeight", value="视频高度", required=true, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="desc", value="视频描述", required=false, 
				dataType="String", paramType="form")
	})
	@PostMapping(value="/uploadVideo", headers="content-type=multipart/form-data")
public HbrJSONResult uploadVideo(String userId,String bgmId,
		double videoSeconds,int videoWidth,int videoHeight,String desc,
		@ApiParam(value="短视频",required = true)
		MultipartFile file) throws Exception
	{
		//1.判断用户id是否存在
		if(StringUtils.isBlank(userId)) {
			return HbrJSONResult.errorMsg("不能获取到用户");
		}
		//2.定义两个流对象
		FileOutputStream fileOutputStream=null;
		InputStream inputStream=null;
		
				//保存到数据库的相对路径
				//1.上传视频到数据库的路径
				String uploadPathDB="/"+userId+"/video";
				//2.上传视频封面到数据库的相对路径
				String coverPathDB = "/" + userId + "/video";
				//3.视频的最终路径
				String finalVideoPath="";
		try {
		//4.判断视频文件是否为空
		if(file!=null) {
			//得到该文件的原始名字，因为等下要进行切割
			String fileName=file.getOriginalFilename();
			//将视频名字用.进行分割，因为.在java中不能直接显示，需要转义，返回一个数组
			String[] arrayFileName = fileName.split("\\.");
			//定义一个视频名字的前缀
			String fileNamePrefix="";
			//将数组进行循环
			for(int i=0;i<arrayFileName.length-1;i++) {
				fileNamePrefix+=arrayFileName[i];
			}
			if(StringUtils.isNotBlank(fileName)) {
				//视频文件上传的最终位置
				finalVideoPath=FILE_SPACE+uploadPathDB+"/"+fileName;
				//视频截图上传的最终位置
				coverPathDB = coverPathDB + "/" + fileNamePrefix + ".jpg";
				//设置数据库保存的路径
				uploadPathDB += ("/" + fileName);
				//创建一个这个路径下的文件
				File unloadFile=new File(finalVideoPath);
				//判断这个文件有没有文件夹不管这个文件有没有父文件夹，都要重新创建
				if(unloadFile.getParentFile()!=null||!unloadFile.getParentFile().isDirectory()) {
					unloadFile.getParentFile().mkdirs();
				}
				//将这个文件输出
				fileOutputStream=new FileOutputStream(unloadFile);
				//得到从前端得到的视频文件
				inputStream=file.getInputStream();
				//然后将从前端得到的文件内容复制到输出流文件上，完成拷贝
				IOUtils.copy(inputStream, fileOutputStream);
				
				
			}else {
				//如果文件为空，就抛出错误
				return HbrJSONResult.errorMsg("上传失败");
			}
			
		}
	} catch (Exception e) {
		e.printStackTrace();
	}finally {
		//判断输出流是否为空，如果不为空，要将流刷新一下，并进行关闭
		if(fileOutputStream!=null) {
			fileOutputStream.flush();
			fileOutputStream.close();
		}
	}
		//判断bgmId是否为空，如果不为空的话，就进行合并
		if(StringUtils.isNotBlank(bgmId)) {
			//如果背景音乐id不为空的话，那就调用背景音乐的接口根据id查询得到这个bgm
			Bgm bgm = bgmService.queryBgmById(bgmId);
			//背景音乐的绝对路径
			String mp3InputPath=FILE_SPACE+bgm.getPath();
			//把ffmpeg所在的位置通过构造方法设入进去
			MixVideoMp3 mixVideoMp3=new MixVideoMp3(FFMPEG_EXE);
			//原视频的最终地址
			String videoInputPath=finalVideoPath;
			//这是合并后的视频名字，通过uuid去生成，防止重合
			String videoOutputName=UUID.randomUUID().toString()+".mp4";
			//合并之后保存到数据库的视频路径
			uploadPathDB="/"+userId+"/video"+"/"+videoOutputName;
			//合并之后的最终路径
			finalVideoPath=FILE_SPACE+uploadPathDB;
			//调用工具类的合并方法
			mixVideoMp3.convertor(videoInputPath, mp3InputPath, videoSeconds, finalVideoPath);
			
		}
		//对视频进行截图
		FetchVideoCover videoInfo = new FetchVideoCover(FFMPEG_EXE);
		videoInfo.getCover(finalVideoPath, FILE_SPACE + coverPathDB);
		Videos videos=new Videos();
		videos.setAudioId(bgmId);
		videos.setUserId(userId);
		videos.setVideoSeconds((float)videoSeconds);
		videos.setVideoHeight(videoHeight);
		videos.setVideoWidth(videoWidth);
		videos.setVideoDesc(desc);
		videos.setVideoPath(uploadPathDB);
		videos.setStatus(VideoStatusEnum.SUCCESS.value);
		videos.setCreateTime(new Date());
		videos.setCoverPath(coverPathDB);
		String videoId = videoService.saveVideo(videos);
		return HbrJSONResult.ok(videoId);
	}
	
	
	//保存封面
	@ApiOperation(value="上传视频", notes="上传视频的接口")
	@ApiImplicitParams({
		@ApiImplicitParam(name="userId", value="用户id", required=true, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="videoId", value="背景音乐id", required=false, 
				dataType="String", paramType="form")
	})
	@PostMapping(value="/uploadCover", headers="content-type=multipart/form-data")
public HbrJSONResult uploadCover(String userId,String videoId,
		@ApiParam(value="视频封面",required = true)
		MultipartFile file) throws Exception
	{
		if(StringUtils.isBlank(videoId)||StringUtils.isBlank(userId)) {
			return HbrJSONResult.errorMsg("视频id或者用户id不能为空");
		}
		FileOutputStream fileOutputStream=null;
		InputStream inputStream=null;
		
		
				//保存到数据库的相对路径
				String uploadPathDB="/"+userId+"/video";
				String finalCoverPath="";
		try {
		
		if(file!=null) {
			
			//得到该文件的原始名字，因为等下要进行切割
			String fileName=file.getOriginalFilename();
			if(StringUtils.isNotBlank(fileName)) {
				//文件上传的最终位置
				finalCoverPath=FILE_SPACE+uploadPathDB+"/"+fileName;
				// 设置数据库保存的路径
				uploadPathDB += ("/" + fileName);
				File unloadFile=new File(finalCoverPath);
				//不管这个文件有没有父文件夹，都要重新创建
				if(unloadFile.getParentFile()!=null||unloadFile.getParentFile().isDirectory()) {
					unloadFile.getParentFile().mkdirs();
				}
				fileOutputStream=new FileOutputStream(unloadFile);
				inputStream=file.getInputStream();
				IOUtils.copy(inputStream, fileOutputStream);
				
			}else {
				return HbrJSONResult.errorMsg("上传失败");
			}
			
		}
	} catch (Exception e) {
		e.printStackTrace();
	}finally {
		if(fileOutputStream!=null) {
			fileOutputStream.flush();
			fileOutputStream.close();
		}
	}
		videoService.updateVideo(videoId, uploadPathDB);
		return HbrJSONResult.ok();
	} 
	
	/**
	 * 展示所有视频
	 * @param video
	 * @param isSaveRecord 0不需要保存，或者为空；1的话就要保存
	 * @param page 
	 * @return
	 */
	//分页展示视频
	@ApiOperation(value="上传视频", notes="展示视频的接口")
	@PostMapping("/showAllVideos")
	public HbrJSONResult showAllVideos(@RequestBody Videos video,Integer isSaveRecord,Integer page,Integer pageSize) {
		if(page==null) {
			page=1;
		}
		if (pageSize == null) {
			pageSize = PAGE_SIZE;
		}
		PagesResult pagesResult=videoService.getAllVideos(video,isSaveRecord,page, PAGE_SIZE);
		return HbrJSONResult.ok(pagesResult);
	}
	/**
	 * 热搜词，进行排序
	 * @return
	 */
	@PostMapping("/hotWords")
	public HbrJSONResult hotWords() {
		List<String> list = videoService.getHodWords();
		return HbrJSONResult.ok(list);
	}
	
	/**
	 * @Description: 我关注的人发的视频
	 */
	@PostMapping("/showMyFollow")
	public HbrJSONResult showMyFollow(String userId, Integer page) throws Exception {
		
		if (StringUtils.isBlank(userId)) {
			return HbrJSONResult.ok();
		}
		
		if (page == null) {
			page = 1;
		}

		int pageSize = 6;
		
		PagesResult videosList = videoService.queryMyFollowVideos(userId, page, pageSize);
		
		return HbrJSONResult.ok(videosList);
	}
	
	/**
	 * @Description: 我收藏(点赞)过的视频列表
	 */
	@PostMapping("/showMyLike")
	public HbrJSONResult showMyLike(String userId, Integer page, Integer pageSize) throws Exception {
		
		if (StringUtils.isBlank(userId)) {
			return HbrJSONResult.ok();
		}
		
		if (page == null) {
			page = 1;
		}

		if (pageSize == null) {
			pageSize = 6;
		}
		
		PagesResult videosList = videoService.queryMyLikeVideos(userId, page, pageSize);
		
		return HbrJSONResult.ok(videosList);
	}
	
	@PostMapping(value="/userLike")
	public HbrJSONResult userLike(String userId, String videoId, String videoCreaterId) 
			throws Exception {
		videoService.userLikeVideo(userId, videoId, videoCreaterId);
		return HbrJSONResult.ok();
	}
	
	@PostMapping(value="/userUnLike")
	public HbrJSONResult userUnLike(String userId, String videoId, String videoCreaterId) throws Exception {
		videoService.userUnLikeVideo(userId, videoId, videoCreaterId);
		return HbrJSONResult.ok();
	}
	@PostMapping("/saveComment")
	public HbrJSONResult saveComment(@RequestBody Comments comment, 
			String fatherCommentId, String toUserId) throws Exception {
		
		comment.setFatherCommentId(fatherCommentId);
		comment.setToUserId(toUserId);
		
		videoService.saveComment(comment);
		return HbrJSONResult.ok();
	}
	
	@PostMapping("/getVideoComments")
	public HbrJSONResult getVideoComments(String videoId, Integer page, Integer pageSize) throws Exception {
		
		if (StringUtils.isBlank(videoId)) {
			return HbrJSONResult.ok();
		}
		
		// 分页查询视频列表，时间顺序倒序排序
		if (page == null) {
			page = 1;
		}

		if (pageSize == null) {
			pageSize = 10;
		}
		
		PagesResult list = videoService.getAllComments(videoId, page, pageSize);
		
		return HbrJSONResult.ok(list);
	}
}