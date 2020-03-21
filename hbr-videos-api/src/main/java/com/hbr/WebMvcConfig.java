package com.hbr;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.hbr.controller.interceptor.MiniInterceptor;

/**
 * 	资源映射 tomcat
 * @author huboren
 *@Configuration：表示这个是个配置文件
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {
	
	/**
	 * 映射方法
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**")//1.映射所有静态资源
				.addResourceLocations("classpath:/META-INF/resources/")//2.映射目录：为了让Swagger2成功显示
				.addResourceLocations("file:C:/hbr_videos/");//3.映射文件目录，为了让tomcat能访问文件
	}
	@Bean(initMethod="init")
	public ZKCuratorClient zkCuratorClient() {
		return new ZKCuratorClient();
	}
	
	@Bean
	public MiniInterceptor miniInterceptor() {
		return new MiniInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		
		registry.addInterceptor(miniInterceptor())
						.addPathPatterns("/user/**")
				       .addPathPatterns("/video/upload", "/video/uploadCover",
				    		   			"/video/userLike", "/video/userUnLike",
				    		   			"/video/saveComment")
												  .addPathPatterns("/bgm/**")
												  .excludePathPatterns("/users/queryPublisher");
		
		super.addInterceptors(registry);
	}
}
