package com.orangeforms.webadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 应用服务启动类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@EnableAsync
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan("com.orangeforms")
public class WebAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebAdminApplication.class, args);
	}
}
