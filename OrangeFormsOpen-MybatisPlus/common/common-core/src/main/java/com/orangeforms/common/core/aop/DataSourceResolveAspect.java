package com.orangeforms.common.core.aop;

import com.orangeforms.common.core.annotation.MyDataSourceResolver;
import com.orangeforms.common.core.util.DataSourceResolver;
import com.orangeforms.common.core.config.DataSourceContextHolder;
import com.orangeforms.common.core.util.ApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于自定义解析规则的多数据源AOP切面处理类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Aspect
@Component
@Order(1)
@Slf4j
public class DataSourceResolveAspect {

    private final Map<Class<? extends DataSourceResolver>, DataSourceResolver> resolverMap = new ConcurrentHashMap<>();

    /**
     * 所有配置MyDataSourceResovler注解的Service实现类。
     */
    @Pointcut("execution(public * com.orangeforms..service..*(..)) " +
            "&& @target(com.orangeforms.common.core.annotation.MyDataSourceResolver)")
    public void datasourceResolverPointCut() {
        // 空注释，避免sonar警告
    }

    @Around("datasourceResolverPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Class<?> clazz = point.getTarget().getClass();
        MyDataSourceResolver dsr = clazz.getAnnotation(MyDataSourceResolver.class);
        Class<? extends DataSourceResolver> resolverClass = dsr.resolver();
        DataSourceResolver resolver =
                resolverMap.computeIfAbsent(resolverClass, ApplicationContextHolder::getBean);
        Integer type = resolver.resolve(dsr.arg(), dsr.intArg(), this.getMethodName(point), point.getArgs());
        Integer originalType = null;
        if (type != null) {
            // 通过判断 DataSource 中的值来判断当前方法应用哪个数据源
            originalType = DataSourceContextHolder.setDataSourceType(type);
            log.debug("set datasource is " + type);
        }
        try {
            return point.proceed();
        } finally {
            if (type != null) {
                DataSourceContextHolder.unset(originalType);
                log.debug("unset datasource is " + originalType);
            }
        }
    }

    private String getMethodName(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return methodSignature.getMethod().getName();
    }
}
