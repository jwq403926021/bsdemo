package com.orangeforms.common.core.config;

import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.extension.incrementer.OracleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 仅仅用于Oracle，基于Sequence计算自增字段值的生成器。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Configuration
public class MybatisPlusKeyGeneratorConfig {

    @Bean
    public IKeyGenerator keyGenerator() {
        return new OracleKeyGenerator();
    }
}
