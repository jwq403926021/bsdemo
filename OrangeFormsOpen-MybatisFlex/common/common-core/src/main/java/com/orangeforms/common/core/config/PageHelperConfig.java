package com.orangeforms.common.core.config;

import com.github.pagehelper.PageInterceptor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * pagehelper的配置对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "pagehelper")
public class PageHelperConfig {

    private String helperDialect;
    private String reasonable;
    private String supportMethodsArguments;
    private String params;

    @Bean
    public PageInterceptor pageInterceptor() {
        PageInterceptor interceptor = new PageInterceptor();
        Properties p = new Properties();
        p.setProperty("helperDialect", helperDialect);
        p.setProperty("reasonable", reasonable);
        p.setProperty("supportMethodsArguments", supportMethodsArguments);
        p.setProperty("params", params);
        interceptor.setProperties(p);
        return interceptor;
    }
}
