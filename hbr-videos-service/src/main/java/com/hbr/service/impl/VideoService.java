package com.hbr.service.impl;

import java.util.Date;
import java.util.List;

import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hbr.mapper.CommentsMapper;
import com.hbr.mapper.CommentsMapperCustom;
import com.hbr.mapper.SearchRecordsMapper;
import com.hbr.mapper.UsersLikeVideosMapper;
import com.hbr.mapper.UsersMapper;
import com.hbr.mapper.VideosMapper;
import com.hbr.mapper.VideosVOMapper;
import com.hbr.pojo.Comments;
import com.hbr.pojo.SearchRecords;
import com.hbr.pojo.UsersLikeVideos;
import com.hbr.pojo.Videos;
import com.hbr.pojo.vo.CommentsVO;
import com.hbr.pojo.vo.VideosVO;
import com.hbr.service.IVideoService;
import com.hbr.utils.PagesResult;
import com.hbr.utils.TimeAgoUtils;

import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;
@Service
public class VideoService implements IVideoService {
@Autowired
private VideosMapper videosMapper;
@Autowired
private Sid sid;
@Autowired
private VideosVOMapper videosVOMapper;
@Autowired
private SearchRecordsMapper searchRecordsMapper;
@Autowired
private UsersLikeVideosMapper usersLikeVideosMapper; 
@Autowired
private UsersMapper usersMapper;
@Autowired
private CommentsMapperCustom commentMapperCustom;
@Autowired
private CommentsMapper commentsMapper;

@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public String saveVideo(Videos videos) {
		// TODO Auto-generated method stub
	String id = sid.nextShort();
	videos.setId(id);
		videosMapper.insertSelective(videos);
		return id;
	}
@Transactional(propagation = Propagation.REQUIRED)
@Override
public void updateVideo(String videoId, String coverPath) {
	// TODO Auto-generated method stub
	Videos videos=new Videos();
	videos.setId(videoId);
	videos.setCoverPath(coverPath);
	videosMapper.updateByPrimaryKeySelective(videos);
}
@Transactional(propagation = Propagation.REQUIRED)
@Override
public PagesResult getAllVideos(Videos video,Integer isSaveRecord,Integer page,Integer pageSize) {
	// TODO Auto-generated method stub
	//保存热搜词
	String desc=video.getVideoDesc();
	String userId = video.getUserId();
	if(isSaveRecord!=null && isSaveRecord==1) {
		SearchRecords searchRecords=new SearchRecords();
		String id=sid.nextShort();
		searchRecords.setId(id);
		searchRecords.setContent(desc);
		searchRecordsMapper.insert(searchRecords);
	}
	PageHelper.startPage(page,pageSize);
	List<VideosVO> list = videosVOMapper.queryAllVideos(desc, userId);
	PageInfo<VideosVO> pageList=new PageInfo<VideosVO>(list);
	PagesResult pagesResult=new PagesResult();
	pagesResult.setPage(page);
	pagesResult.setTotal(pageList.getPages());
	pagesResult.setRows(list);
	pagesResult.setRecords(pageList.getTotal());
	return pagesResult;
}
@Override
public List<String> getHodWords() {
	// TODO Auto-generated method stub
	return searchRecordsMapper.getHotwords();
}


@Transactional(propagation = Propagation.SUPPORTS)
@Override
public PagesResult queryMyLikeVideos(String userId, Integer page, Integer pageSize) {
	PageHelper.startPage(page, pageSize);
	List<VideosVO> list = videosVOMapper.queryMyLikeVideos(userId);
			
	PageInfo<VideosVO> pageList = new PageInfo<>(list);
	
	PagesResult pagedResult = new PagesResult();
	pagedResult.setTotal(pageList.getPages());
	pagedResult.setRows(list);
	pagedResult.setPage(page);
	pagedResult.setRecords(pageList.getTotal());
	
	return pagedResult;
}

@Transactional(propagation = Propagation.SUPPORTS)
@Override
public PagesResult queryMyFollowVideos(String userId, Integer page, Integer pageSize) {
	PageHelper.startPage(page, pageSize);
	List<VideosVO> list = videosVOMapper.queryMyFollowVideos(userId);
			
	PageInfo<VideosVO> pageList = new PageInfo<>(list);
	
	PagesResult pagedResult = new PagesResult();
	pagedResult.setTotal(pageList.getPages());
	pagedResult.setRows(list);
	pagedResult.setPage(page);
	pagedResult.setRecords(pageList.getTotal());
	
	return pagedResult;
}


@Transactional(propagation = Propagation.REQUIRED)
@Override
public void userLikeVideo(String userId, String videoId, String videoCreaterId) {
	// 1. 保存用户和视频的喜欢点赞关联关系表
	String likeId = sid.nextShort();
	UsersLikeVideos ulv = new UsersLikeVideos();
	ulv.setId(likeId);
	ulv.setUserId(userId);
	ulv.setVideoId(videoId);
	usersLikeVideosMapper.insert(ulv);
	
	// 2. 视频喜欢数量累加
	videosVOMapper.addVideoLikeCount(videoId);
	
	// 3. 用户受喜欢数量的累加
	usersMapper.addReceiveLikeCount(videoCreaterId);
}

@Transactional(propagation = Propagation.REQUIRED)
@Override
public void userUnLikeVideo(String userId, String videoId, String videoCreaterId) {
	// 1. 删除用户和视频的喜欢点赞关联关系表
	
	Example example = new Example(UsersLikeVideos.class);
	Criteria criteria = example.createCriteria();
	
	criteria.andEqualTo("userId", userId);
	criteria.andEqualTo("videoId", videoId);
	
	usersLikeVideosMapper.deleteByExample(example);
	
	// 2. 视频喜欢数量累减
	videosVOMapper.reduceVideoLikeCount(videoId);
	
	// 3. 用户受喜欢数量的累减
	usersMapper.reduceReceiveLikeCount(videoCreaterId);
	
}
@Transactional(propagation = Propagation.REQUIRED)
@Override
public void saveComment(Comments comment) {
	String id = sid.nextShort();
	comment.setId(id);
	comment.setCreateTime(new Date());
	commentsMapper.insert(comment);
}

@Transactional(propagation = Propagation.SUPPORTS)
@Override
public PagesResult getAllComments(String videoId, Integer page, Integer pageSize) {
	
	PageHelper.startPage(page, pageSize);
	
	List<CommentsVO> list = commentMapperCustom.queryComments(videoId);
	
		for (CommentsVO c : list) {
			String timeAgo = TimeAgoUtils.format(c.getCreateTime());
			c.setTimeAgoStr(timeAgo);
		}
	
	PageInfo<CommentsVO> pageList = new PageInfo<>(list);
	
	PagesResult grid = new PagesResult();
	grid.setTotal(pageList.getPages());
	grid.setRows(list);
	grid.setPage(page);
	grid.setRecords(pageList.getTotal());
	
	return grid;
}
}
