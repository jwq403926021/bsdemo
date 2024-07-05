package com.orangeforms.common.core.upload;

/**
 * 上传数据存储介质类型枚举。
 *
 * @author Jerry
 * @date 2024-07-02
 */
public enum UploadStoreTypeEnum {

    /**
     * 本地系统。
     */
    LOCAL_SYSTEM,
    /**
     * minio分布式存储。
     */
    MINIO_SYSTEM,
    /**
     * 阿里云OSS存储。
     */
    ALIYUN_OSS_SYTEM,
    /**
     * 腾讯云COS存储。
     */
    QCLOUD_COS_SYTEM,
    /**
     * 华为云OBS存储。
     */
    HUAWEI_OBS_SYSTEM
}
