package com.orangeforms.common.flow.util;

import com.orangeforms.common.flow.vo.FlowTaskVo;
import com.orangeforms.common.flow.vo.FlowUserInfoVo;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 流程通知扩展帮助实现类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
public class BaseFlowNotifyExtHelper {

    /**
     * 处理消息。
     *
     * @param notifyType   通知类型，具体值可参考FlowUserTaskExtData中NOTIFY_TYPE开头的常量。
     * @param userInfoList 待通知的用户信息列表。
     */
    public void doNotify(String notifyType, List<FlowUserInfoVo> userInfoList, FlowTaskVo taskInfo) {
        userInfoList.forEach(u -> log.info(
                "The user [{}] of Task [{}] is notified by [{}].", u.getLoginName(), taskInfo.getTaskKey(), notifyType));
    }
}
