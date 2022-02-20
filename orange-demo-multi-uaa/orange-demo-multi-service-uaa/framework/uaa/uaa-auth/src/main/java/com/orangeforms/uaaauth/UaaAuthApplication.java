package com.orangeforms.uaaauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * UAA服务启动类。
 *
 * @author Jerry
 * @date 2020-08-08
 */
@ComponentScan("com.orangeforms")
@EnableDiscoveryClient
@SpringBootApplication
public class UaaAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(UaaAuthApplication.class, args);
	}
}
