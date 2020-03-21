package com.hbr.mapper;

import java.util.List;

import com.hbr.pojo.Comments;
import com.hbr.pojo.vo.CommentsVO;
import com.hbr.utils.MyMapper;

public interface CommentsMapperCustom extends MyMapper<Comments> {
	
	public List<CommentsVO> queryComments(String videoId);
}