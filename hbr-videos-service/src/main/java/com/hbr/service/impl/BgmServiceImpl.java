package com.hbr.service.impl;

import java.util.List;

import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.hbr.mapper.BgmMapper;
import com.hbr.pojo.Bgm;
import com.hbr.service.IBgmService;

/**
 * 背景音乐相关的接口实现
 * @author huboren
 *
 */
@Service
public class BgmServiceImpl implements IBgmService {
@Autowired
private BgmMapper bgmMapper;
//注入一个全局唯一id
@Autowired
private Sid sid;

/**
 * 查询bgm的列表
 * 
 */
@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public List<Bgm> queryBgmList() {
		//查询所有的bgm
		List<Bgm> list = bgmMapper.selectAll();
		//将数据返回
		return list;
	}

/**
 * 通过id去查询bgm
 */
@Transactional(propagation = Propagation.REQUIRED)
@Override
public Bgm queryBgmById(String bgmId) {
	// TODO Auto-generated method stub
	Bgm bgm = bgmMapper.selectByPrimaryKey(bgmId);
	return bgm;
}

}
