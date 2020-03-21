package com.hbr.mapper;

import java.util.List;

import com.hbr.pojo.SearchRecords;
import com.hbr.utils.MyMapper;

public interface SearchRecordsMapper extends MyMapper<SearchRecords> {
	
	public List<String> getHotwords();
}