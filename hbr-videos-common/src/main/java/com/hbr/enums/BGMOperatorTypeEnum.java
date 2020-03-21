package com.hbr.enums;

/**
 * bgm操作枚举类
 * @author huboren
 *
 */
public enum BGMOperatorTypeEnum {
	
	ADD("1", "添加bgm"),				
	DELETE("2", "删除bgm");		
	
	public final String type;
	public final String value;
	
	BGMOperatorTypeEnum(String type, String value){
		this.type = type;
		this.value = value;
	}
	
	public String getUserType() {
		return type;
	}  
	
	public String getValue() {
		return value;
	} 
	

	
}
