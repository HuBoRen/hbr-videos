package com.hbr.service;

import java.util.List;

import com.hbr.pojo.Comments;
import com.hbr.pojo.Videos;
import com.hbr.utils.PagesResult;

public interface IVideoService {
	//保存视频
public String saveVideo(Videos videos);
//修改视频信息
public void updateVideo(String videoId,String coverPath);
//分页展现视频
public PagesResult getAllVideos(Videos video,Integer isSaveRecord,Integer page,Integer pageSize);
//热搜词列表，并进行排序
public List<String> getHodWords();
 //查询我喜欢的视频列表
public PagesResult queryMyLikeVideos(String userId, Integer page, Integer pageSize);

//查询我关注的人的视频列表
public PagesResult queryMyFollowVideos(String userId, Integer page, Integer pageSize);

//用户喜欢/点赞视频
public void userLikeVideo(String userId, String videoId, String videoCreaterId);

//用户不喜欢/取消点赞视频
public void userUnLikeVideo(String userId, String videoId, String videoCreaterId);
/**
 * @Description: 用户留言
 */
public void saveComment(Comments comment);

/**
 * @Description: 留言分页
 */
public PagesResult getAllComments(String videoId, Integer page, Integer pageSize);
}
