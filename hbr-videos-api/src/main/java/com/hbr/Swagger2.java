package com.hbr;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @Configuration：注明它是个配置文件
 * @EnableSwagger2：开启Swagger2
 * @author huboren
 *
 */
@Configuration
@EnableSwagger2
public class Swagger2 {

	/**
	 * @Description:swagger2的配置文件，这里可以配置swagger2的一些基本的内容，比如扫描的包等等
	 */
	@Bean
	public Docket createRestApi() {
		
		return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select()
				.apis(RequestHandlerSelectors.basePackage("com.hbr.controller"))
				.paths(PathSelectors.any()).build(); 
				
	}

	/**
	 * @Description: 构建 api文档的信息
	 */
	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				// 设置页面标题
				.title("使用swagger2构建短视频后端api接口文档")
				// 设置联系人
				.contact(new Contact("胡波任", "http://www.brjy520.cn", "1592759763@qq.com"))
				// 描述
				.description("欢迎访问短视频接口文档，这里是描述信息")
				// 定义版本号
				.version("1.0").build();
	}

}
