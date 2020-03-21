package com.hbr;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * 相当于使用web.xml的形式去启动部署
 * @author huboren
 *
 */
public class WarStartApplication extends SpringBootServletInitializer{
	/**
	 * 重写基本配置
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		//使用web.xml运行应用程序，指向Application，最后启动springboot
		return builder.sources(Application.class);
	}

}
