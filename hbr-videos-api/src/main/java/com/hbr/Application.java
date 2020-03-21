package com.hbr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import tk.mybatis.spring.annotation.MapperScan;


/**
 * springboot的启动器
 * @author huboren
 *
 */
@SpringBootApplication
@MapperScan(basePackages="com.hbr.mapper")
@ComponentScan(basePackages= {"com.hbr", "org.n3r.idworker"})
public class Application {
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
}
