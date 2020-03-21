package com.hbr.service;

import java.util.List;

import com.hbr.pojo.Users;
import com.hbr.pojo.UsersReport;
import com.hbr.utils.HbrJSONResult;
/**
 * 用户相关的接口
 * @author huboren
 *
 */
public interface IUserService {
	
/**
 * 判断用户名是否存在接口
 * @param username
 * @return
 */
boolean queryUserIsExit(String username);

/**
 * 注册用户接口
 * @param user
 */
void saveUser(Users user);

/**
 * 登录的接口
 * @param username
 * @param password
 * @return
 */
public Users queryUserForLogin(String username, String password);

/**
 * 修改用户信息的接口
 * @param users
 */
public void updateUserInfo(Users users);
//查询用户信息
Users queryUserInfo(String userId);
/**
 * @Description: 查询用户是否喜欢点赞视频
 */
public boolean isUserLikeVideo(String userId, String videoId);

/**
 * @Description: 增加用户和粉丝的关系
 */
public void saveUserFanRelation(String userId, String fanId);

/**
 * @Description: 删除用户和粉丝的关系
 */
public void deleteUserFanRelation(String userId, String fanId);

/**
 * @Description: 查询用户是否关注
 */
public boolean queryIfFollow(String userId, String fanId);

/**
 * @Description: 举报用户
 */
public void reportUser(UsersReport userReport);
}
