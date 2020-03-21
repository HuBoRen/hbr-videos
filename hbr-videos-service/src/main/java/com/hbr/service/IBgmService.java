package com.hbr.service;

import java.util.List;
import com.hbr.pojo.Bgm;

/**
 * bgm相关的接口
 * @author huboren
 *
 */
public interface IBgmService {
/**
 *查询bgm的列表的接口
 * @return
 */
public List<Bgm> queryBgmList();
/**
 * 根据id去查询bgm的接口
 * @param bgmId
 * @return
 */
public Bgm queryBgmById(String bgmId);
}
