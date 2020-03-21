package com.hbr.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hbr.pojo.Comments;
import com.hbr.pojo.Users;
import com.hbr.pojo.UsersReport;
import com.hbr.pojo.vo.PublisherVideo;
import com.hbr.pojo.vo.UsersVO;
import com.hbr.service.IUserService;
import com.hbr.service.IVideoService;
import com.hbr.service.impl.VideoService;
import com.hbr.utils.BasicUtils;
import com.hbr.utils.HbrJSONResult;
import com.hbr.utils.MD5Utils;
import com.hbr.utils.PagesResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 用户信息的controller
 * 因为现在都是前后端分离，所以这里使用了Swagger2工具构建restful接口测试，进行测试后端接口是否成功
 * Swagger2：可以生成文档形式的api
 * @author huboren
 *
 */
//因为后台和小程序的交互都是使用json传输，所以使用@restController，表明所有方法值都以json形式传输
@RestController
@Api(value="用户上传图片",tags={"上传图片的接口"})//Swagger2
@RequestMapping("/users")
public class UsersController extends BasicUtils{
	//注入用户接口
	@Autowired
	private IUserService userService;
	@Autowired
	private IVideoService videoService;
	
	/**
	 * 上传用户头像方法
	 * @param userId
	 * @param files
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/uploadFace")
public HbrJSONResult uploadFace(String userId,@RequestParam("file") MultipartFile[] files) throws Exception {
		//1.判断用户id是否为空，如果为空就不能让他上传
		if(StringUtils.isBlank(userId)) {
			return HbrJSONResult.errorMsg("不能获取到用户");
		}
		//2.定义两个流对象
		FileOutputStream fileOutputStream=null;
		InputStream inputStream=null;
		//3.定义一下文件的命名空间，到时候所有的文件都放在这里面
		String fileSpace="C:/hbr_videos";
		//4.定义一个 保存到数据库的相对路径
		String uploadPathDB="/"+userId+"/face";
		try {
		//5.判断文件是否为空，因为只有不能为空才能继续上传
		if(files!=null && files.length>0) {
			//6.得到文件的名字
			String fileName=files[0].getOriginalFilename();
			//7.如果文件名字不为空
			if(StringUtils.isNotBlank(fileName)) {
				//8.文件上传的最终位置(绝对路径)
				String finalPath=fileSpace+uploadPathDB+"/"+fileName;
				//9.设置数据库保存的路径
				uploadPathDB += ("/" + fileName);
				//10.new一个file对象，然后将路径放进去，创建一个这个路径的文件
				File unloadFile=new File(finalPath);
				//11.判断这个路径是否有父文件夹，或者这个文件的文件夹不是个目录
				if(unloadFile.getParentFile()!=null||!unloadFile.getParentFile().isDirectory()) {
					//12.创建父文件夹
					unloadFile.getParentFile().mkdirs();
				}
				//13.通过输出流将这个文件输出去
				fileOutputStream=new FileOutputStream(unloadFile);
				//14.通过输入流将微信小程序上传文件拿过来
				inputStream=files[0].getInputStream();
				//15.通过复制的方法，把输入流里面的头像文件复制到输出流里面
				IOUtils.copy(inputStream, fileOutputStream);
			}else {
				//16.否则的话就爆上传失败
				return HbrJSONResult.errorMsg("上传失败");
			}
			
		}
	} catch (Exception e) {
		e.printStackTrace();
	}finally {
		//17.如果文件输出流不为空，就先进行输出流的刷新，防止缓冲，然后将流关闭
		if(fileOutputStream!=null) {
			fileOutputStream.flush();
			fileOutputStream.close();
		}
	}
		//18.创建一个用户对象
		Users users=new Users();
		//19.将用户id设进去
		users.setId(userId);
		//20.将头像设置进去
		users.setFaceImage(uploadPathDB);
		//21.调用userservice的更新用户信息方法，通过对象修改
		userService.updateUserInfo(users);
		//22.将数据库相对路径返回给微信小程序，为了让小程序进行头像显示
		return HbrJSONResult.ok(uploadPathDB);
}
	@ApiOperation(value="查询用户信息", notes="查询用户信息的接口")
	@ApiImplicitParam(name="userId", value="用户id", required=true, 
						dataType="String", paramType="query")
	@PostMapping("/query")
	public HbrJSONResult query(String userId, String fanId) throws Exception {
		
		if (StringUtils.isBlank(userId)) {
			return HbrJSONResult.errorMsg("用户id不能为空...");
		}
		
		Users userInfo = userService.queryUserInfo(userId);
		UsersVO userVO = new UsersVO();
		BeanUtils.copyProperties(userInfo, userVO);
		
		userVO.setFollow(userService.queryIfFollow(userId, fanId));
		
		return HbrJSONResult.ok(userVO);
	}
	
	@PostMapping("/queryPublisher")
	public HbrJSONResult queryPublisher(String loginUserId, String videoId, 
			String publishUserId) throws Exception {
		
		if (StringUtils.isBlank(publishUserId)) {
			return HbrJSONResult.errorMsg("");
		}
		
		// 1. 查询视频发布者的信息
		Users userInfo = userService.queryUserInfo(publishUserId);
		UsersVO publisher = new UsersVO();
		BeanUtils.copyProperties(userInfo, publisher);
		
		// 2. 查询当前登录者和视频的点赞关系
		boolean userLikeVideo = userService.isUserLikeVideo(loginUserId, videoId);
		
		PublisherVideo bean = new PublisherVideo();
		bean.setPublisher(publisher);
		bean.setUserLikeVideo(userLikeVideo);
		
		return HbrJSONResult.ok(bean);
	}
	
	@PostMapping("/beyourfans")
	public HbrJSONResult beyourfans(String userId, String fanId) throws Exception {
		
		if (StringUtils.isBlank(userId) || StringUtils.isBlank(fanId)) {
			return HbrJSONResult.errorMsg("");
		}
		
		userService.saveUserFanRelation(userId, fanId);
		
		return HbrJSONResult.ok("关注成功...");
	}
	
	@PostMapping("/dontbeyourfans")
	public HbrJSONResult dontbeyourfans(String userId, String fanId) throws Exception {
		
		if (StringUtils.isBlank(userId) || StringUtils.isBlank(fanId)) {
			return HbrJSONResult.errorMsg("");
		}
		
		userService.deleteUserFanRelation(userId, fanId);
		
		return HbrJSONResult.ok("取消关注成功...");
	}
	
	@PostMapping("/reportUser")
	public HbrJSONResult reportUser(@RequestBody UsersReport usersReport) throws Exception {
		
		// 保存举报信息
		userService.reportUser(usersReport);
		
		return HbrJSONResult.errorMsg("举报成功...有你平台变得更美好...");
	}
	
	
	
	
}