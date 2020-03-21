package com.hbr.controller;

import java.util.UUID;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import com.hbr.pojo.Users;
import com.hbr.pojo.vo.UsersVO;
import com.hbr.service.IUserService;
import com.hbr.utils.BasicUtils;
import com.hbr.utils.HbrJSONResult;
import com.hbr.utils.MD5Utils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

/**
 * 这是一个用户登录和注册的controller
 * 因为现在都是前后端分离，所以这里使用了Swagger2工具构建restful接口测试，进行测试后端接口是否成功
 * Swagger2：可以生成文档形式的api
 * @author huboren
 *
 */
//因为后台和小程序的交互都是使用json传输，所以使用@restController，表明所有方法值都以json形式传输
@RestController
@Api(value="用户注册",tags={"注册和登录的接口"})//Swagger2
public class RegistLoginController extends BasicUtils{
	
	//注入一个用户的service接口
	@Autowired
	private IUserService userService;
	
	/**
	 * 这是一个注册的方法
	 * @param users
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/regist")
	@ApiOperation(value="用户注册", notes="用户注册的接口")//因为它是一个json对象
public HbrJSONResult regist(@RequestBody Users users) throws Exception {
	//1.判断用户名或者密码是否为空，为空就返回一个错误信息
		if(StringUtils.isBlank(users.getUsername())|| StringUtils.isBlank(users.getPassword())) {
			return HbrJSONResult.errorMsg("用户名和密码不能为空");
		}
		
		//2.判断用户名是否存在，同样返回一个错误信息
		boolean usernameIsExit=userService.queryUserIsExit(users.getUsername());
		//3.如果用户不存在，返回一个false，所以就可以进行注册，将pojo补全
		if(!usernameIsExit) {
			users.setNickname(users.getNickname());
			users.setFollowCounts(0);
			users.setFansCounts(0);
			//因为密码的安全性，所有这里使用md5加密
			users.setPassword(MD5Utils.getMD5Str(users.getPassword()));
			users.setReceiveLikeCounts(0);
			//4.调用service层的插入用户的方法
			userService.saveUser(users);
		}else {
			//5.不为空的话，就向用户返回一个500，加错误信息
			return HbrJSONResult.errorMsg("用户名已经存在，请换一个重新注册");
		}
		//6.因为密码涉及到安全问题，所以在返回值的时候，把密码设为空
		users.setPassword("");
		//7.得到一个用户的token，使用uuid获取，userToken作为session的值，来标识
		String userToken=UUID.randomUUID().toString();
		//8.设置redis的有效期，还有名字
		//使用：连接，这样设置key是为了在redis工具里起到一个分类作用
		redis.set(USER_REDIS_SESSION+":"+users.getId(),userToken , 1800*1000);
		//9.因为users类没有token这个属性，并且一般不修改本来存在的po类，所以定义一个包装类，
		UsersVO userVo=new UsersVO();
		//10.然后将users类的信息复制到包装类里
		BeanUtils.copyProperties(users, userVo);
		//11.将token设置进包装类里
		userVo.setUserToken(userToken);
		//12.返回信息，为了让小程序那端有一个全局的对象信息，所以还要把userVo给返回过去 
		return HbrJSONResult.ok(userVo);
}
	
	/**
	 * 这是一个登录的controller的方法
	 * @param user json对象
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "用户登录",notes = "用户登录的接口")
	@PostMapping("/login")
	public HbrJSONResult login(@RequestBody Users user) throws Exception {
		//1.定义用户名和密码，并且进行赋值
		String username = user.getUsername();
		String password = user.getPassword();
		//2. 判断用户名和密码必须不为空
		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
			return HbrJSONResult.ok("用户名或密码不能为空...");
		}
		//3.调用userService查询用户名和密码是否存在，并且那个密码在查询前必须经过md5加密之后再去数据库查
		Users userResult = userService.queryUserForLogin(username, 
				MD5Utils.getMD5Str(user.getPassword()));
		//4.如果用户名和密码存在的话，那么userResult就不为空
		if (userResult != null) {
			//5.因为密码涉及到安全问题，所以在返回值的时候，把密码设为空
			userResult.setPassword("");
			//6.调用设置用户token进redis的一个方法
			UsersVO userVO = setUserRedisSessionToken(userResult);
			//7.把值返回去
			return HbrJSONResult.ok(userVO);
		} else {
			//8.如果用户名或者密码不正确，返回错误信息
			return HbrJSONResult.errorMsg("用户名或密码不正确, 请重试...");
		}
		
	}
	
	/**
	 * 这是一个用来设置用户token进redis的一个方法
	 * @param userModel
	 * @return
	 */
	public UsersVO setUserRedisSessionToken(Users userModel) {
		//1.得到一个token
		String uniqueToken = UUID.randomUUID().toString();
		//2.调用redis的设置方法，第一个参数是key，第二个是value，第三个是过期时间
		//使用：连接，这样设置key是为了在redis工具里起到一个分类作用
		redis.set(USER_REDIS_SESSION + ":" + userModel.getId(), uniqueToken, 1000 * 60 * 30);
		//3.创建一个包装的用户类，因为原来user类没有token这个属性
		UsersVO userVO = new UsersVO();
		//4.调用beanutils的复制方法，将原来的用户类的值复制进包装类里
		BeanUtils.copyProperties(userModel, userVO);
		//5.将token设置进包装类里面
		userVO.setUserToken(uniqueToken);
		//6.返回去值
		return userVO;
	}
	
	/**
	 * 用户注销的方法
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "用户注销",notes = "用户注销的接口")
	@PostMapping("/logout")
	@ApiImplicitParam(name="userId", value="用户id", required=true, 
	dataType="String", paramType="query")
	public HbrJSONResult logout(String userId) throws Exception {
		//1.调用redis的删除方法，将key删除
		redis.del(USER_REDIS_SESSION+":"+userId);
		//2.返回信息
			return HbrJSONResult.ok();
		}
		
	}

