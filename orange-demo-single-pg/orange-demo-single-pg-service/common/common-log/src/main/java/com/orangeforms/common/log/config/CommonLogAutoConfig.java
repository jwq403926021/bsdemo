package com.orangeforms.common.log.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * common-log模块的自动配置引导类。
 *
 * @author Jerry
 * @date 2022-02-20
 */
@EnableConfigurationProperties({OperationLogProperties.class})
public class CommonLogAutoConfig {
}
