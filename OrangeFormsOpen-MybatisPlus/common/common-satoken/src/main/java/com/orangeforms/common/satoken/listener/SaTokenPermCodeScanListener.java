package com.orangeforms.common.satoken.listener;

import com.orangeforms.common.satoken.util.SaTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * 后台服务启动的时候扫描服务中标有权限字，并同步到Redis，以供接口查询所有使用到的权限字。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Component
public class SaTokenPermCodeScanListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private SaTokenUtil saTokenUtil;

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        saTokenUtil.collectPermCodes(event);
    }
}
