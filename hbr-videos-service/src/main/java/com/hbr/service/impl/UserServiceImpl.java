package com.hbr.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.hbr.mapper.UsersFansMapper;
import com.hbr.mapper.UsersLikeVideosMapper;
import com.hbr.mapper.UsersMapper;
import com.hbr.mapper.UsersReportMapper;
import com.hbr.pojo.Users;
import com.hbr.pojo.UsersFans;
import com.hbr.pojo.UsersLikeVideos;
import com.hbr.pojo.UsersReport;
import com.hbr.pojo.vo.UsersVO;
import com.hbr.service.IUserService;
import com.hbr.utils.BasicUtils;
import com.hbr.utils.HbrJSONResult;
import com.hbr.utils.MD5Utils;
import com.hbr.utils.RedisOperator;

import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

/**
 * 这是一个用户相关的service层接口实现类
 * 
 * @author huboren
 *
 */
@Service
public class UserServiceImpl extends BasicUtils implements IUserService {
	// 注入一个用户的mapper
	@Autowired
	private UsersMapper userMapper;

	@Autowired
	private UsersFansMapper usersFansMapper;

	@Autowired
	private UsersLikeVideosMapper usersLikeVideosMapper;

	@Autowired
	private UsersReportMapper usersReportMapper;

	// 这是定义一个全局唯一的id，不使用自增id，因为自增id可能会被注入性攻击，扩展性差，性能有上限
	@Autowired
	private Sid sid;

	/**
	 * 判断用户名是否存在的service层接口的实现方法
	 * 
	 * @Transactional(propagation = Propagation.SUPPORTS)表明如果当前这个方法可以有事务，也可以没有事务
	 */
	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public boolean queryUserIsExit(String username) {
		// 1.创建一个用户对象
		Users users = new Users();
		// 2.向这个用户对象设入用户名
		users.setUsername(username);
		// 3.调用userMapper里面的查询一个的方法，丢入这个对象
		Users users2 = userMapper.selectOne(users);
		// 4.如果users2能查到用戶名的话，返回true，否则返回false
		return users2 == null ? false : true;
	}

	/**
	 * 注册用户的service层接口的实现方法
	 * 
	 * @Transactional(propagation = Propagation.REQUIRED)这个代表这个方法一定要有事务，
	 *                            如果当前这个方法没有事务就重新创建一个
	 *
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void saveUser(Users user) {
		// 1.设入一个全局id
		String userId = sid.nextShort();
		// 2.将id设入进去
		user.setId(userId);
		// 3.插入用户
		userMapper.insert(user);

	}

	/**
	 * 登录的service层接口的实现方法
	 * 
	 * @Transactional(propagation = Propagation.SUPPORTS)表明如果当前这个方法可以有事务，也可以没有事务
	 */
	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public Users queryUserForLogin(String username, String password) {
		// 1.创建一个example对象
		Example userExample = new Example(Users.class);
		// 2.创建一个criteria对象
		Criteria criteria = userExample.createCriteria();
		// 3.设置查询条件，相当于select * from users where username=username and password=password
		criteria.andEqualTo("username", username);
		criteria.andEqualTo("password", password);
		// 4.调用usermapper根据查询条件进行查询，并把一条结果返回
		Users result = userMapper.selectOneByExample(userExample);
		// 5.返回结果
		return result;
	}
	
	/**
	 * 
	 * 修改用户的service方法
	 * @Transactional(propagation = Propagation.REQUIRED)这个代表这个方法一定要有事务
	 */
	@Override
	public void updateUserInfo(Users users) {
		//1.创建一个example对象
		Example userExample = new Example(Users.class);
		//2.创建一个criteria对象
		Criteria criteria = userExample.createCriteria();
		//3.设置查询条件，相当于select * from users where id=id
		criteria.andEqualTo("id", users.getId());
		//4.把修改条件设置进去，然后进行修改
		//updateByExampleSelective：条件对象要是为空的值不修改  updateByExample：全部都修改
		userMapper.updateByExampleSelective(users, userExample);
	}
	/**
	 * 根据用户id去查询，返回得到一个用户
	 */
	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public Users queryUserInfo(String userId) {
		//类似select * from users where id=?
		Example userExample = new Example(Users.class);
		Criteria criteria = userExample.createCriteria();
		criteria.andEqualTo("id", userId);
		//得到用户
		Users user = userMapper.selectOneByExample(userExample);
		//返回信息
		return user;
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public boolean isUserLikeVideo(String userId, String videoId) {

		if (StringUtils.isBlank(userId) || StringUtils.isBlank(videoId)) {
			return false;
		}

		Example example = new Example(UsersLikeVideos.class);
		Criteria criteria = example.createCriteria();

		criteria.andEqualTo("userId", userId);
		criteria.andEqualTo("videoId", videoId);

		List<UsersLikeVideos> list = usersLikeVideosMapper.selectByExample(example);

		if (list != null && list.size() > 0) {
			return true;
		}

		return false;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void saveUserFanRelation(String userId, String fanId) {

		String relId = sid.nextShort();

		UsersFans userFan = new UsersFans();
		userFan.setId(relId);
		userFan.setUserId(userId);
		userFan.setFanId(fanId);

		usersFansMapper.insert(userFan);

		userMapper.addFansCount(userId);
		userMapper.addFollersCount(fanId);

	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteUserFanRelation(String userId, String fanId) {

		Example example = new Example(UsersFans.class);
		Criteria criteria = example.createCriteria();

		criteria.andEqualTo("userId", userId);
		criteria.andEqualTo("fanId", fanId);

		usersFansMapper.deleteByExample(example);

		userMapper.reduceFansCount(userId);
		userMapper.reduceFollersCount(fanId);

	}

	@Override
	public boolean queryIfFollow(String userId, String fanId) {

		Example example = new Example(UsersFans.class);
		Criteria criteria = example.createCriteria();

		criteria.andEqualTo("userId", userId);
		criteria.andEqualTo("fanId", fanId);

		List<UsersFans> list = usersFansMapper.selectByExample(example);

		if (list != null && !list.isEmpty() && list.size() > 0) {
			return true;
		}

		return false;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void reportUser(UsersReport userReport) {

		String urId = sid.nextShort();
		userReport.setId(urId);
		userReport.setCreateDate(new Date());

		usersReportMapper.insert(userReport);
	}
	

}
