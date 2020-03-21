package com.hbr.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;


import com.hbr.pojo.Bgm;

import com.hbr.service.IBgmService;

import com.hbr.utils.BasicUtils;
import com.hbr.utils.HbrJSONResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 背景音乐的handler方法
 *因为现在都是前后端分离，所以这里使用了Swagger2工具构建restful接口测试，进行测试后端接口是否成功
 * Swagger2：可以生成文档形式的api
 * @author huboren
 *
 */
@RestController
@Api(value="背景音乐",tags={"背景音乐接口"})
@RequestMapping("/bgm")
public class BgmController extends BasicUtils{
	//注入bgm接口
	@Autowired
	private IBgmService bgmService;
	
	/**
	 * 背景音乐列表方法
	 * @return
	 */
	@PostMapping("/list")
	@ApiOperation(value="背景音乐", notes="背景音乐的接口")
public HbrJSONResult regist()  {
		//调用service层查询bgm的方法
		List<Bgm> bgmList = bgmService.queryBgmList();
		//将数据返回
		return HbrJSONResult.ok(bgmList);
	}		
}