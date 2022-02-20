-- ----------------------------
-- 用户权限管理数据执行脚本。
-- 数据库链接在橙单生成器中配置信息：
-- 应用名称：橙单微服务开源版 / OrangeMultiDemo
-- 链接名称：主数据源 / MAIN
-- 主机信息：localhost:3306
-- 数据库名：zzdemo-multi
-- 用户信息：root / 123456
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 部门管理表
-- ----------------------------
DROP TABLE IF EXISTS `zz_sys_dept`;
CREATE TABLE `zz_sys_dept` (
  `dept_id` bigint(20) NOT NULL COMMENT '部门Id',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父部门Id',
  `dept_name` varchar(50) COLLATE utf8mb4_bin NOT NULL COMMENT '部门名称',
  `show_order` int(11) NOT NULL COMMENT '兄弟部分之间的显示顺序，数字越小越靠前',
  `create_user_id` bigint(20) NOT NULL COMMENT '创建者Id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint(20) NOT NULL COMMENT '更新者Id',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  `deleted_flag` int(1) NOT NULL COMMENT '删除标记(1: 正常 -1: 已删除)',
  PRIMARY KEY (`dept_id`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE,
  KEY `idx_show_order` (`show_order`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=COMPACT COMMENT='部门管理表';

-- ----------------------------
-- 部门关联关系表
-- ----------------------------
DROP TABLE IF EXISTS `zz_sys_dept_relation`;
CREATE TABLE `zz_sys_dept_relation` (
  `parent_dept_id` bigint(20) NOT NULL COMMENT '父部门Id',
  `dept_id` bigint(20) NOT NULL COMMENT '部门Id',
  PRIMARY KEY (`parent_dept_id`,`dept_id`),
  KEY `idx_dept_id` (`dept_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=COMPACT COMMENT='部门关联关系表';

-- ----------------------------
-- 系统用户表
-- ----------------------------
DROP TABLE IF EXISTS `zz_sys_user`;
CREATE TABLE `zz_sys_user` (
  `user_id` bigint(20) NOT NULL COMMENT '主键Id',
  `login_name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '用户登录名称',
  `show_name` varchar(32) COLLATE utf8mb4_bin NOT NULL COMMENT '用户显示名称',
  `dept_id` bigint(20) NOT NULL COMMENT '用户所在部门Id',
  `head_image_url` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '用户头像的Url',
  `user_type` int(11) NOT NULL COMMENT '用户类型(0: 管理员 1: 系统管理用户 2: 系统业务用户)',
  `user_status` int(11) NOT NULL COMMENT '状态(0: 正常 1: 锁定)',
  `create_user_id` bigint(20) NOT NULL COMMENT '创建者Id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint(20) NOT NULL COMMENT '更新者Id',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  `deleted_flag` int(11) NOT NULL COMMENT '删除标记(1: 正常 -1: 已删除)',
  PRIMARY KEY (`user_id`) USING BTREE,
  KEY `idx_dept_id` (`dept_id`) USING BTREE,
  UNIQUE KEY `uk_login_name` (`login_name`) USING BTREE,
  KEY `idx_status` (`user_status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=COMPACT COMMENT='系统用户表';

-- ----------------------------
-- 系统角色表
-- ----------------------------
DROP TABLE IF EXISTS `zz_sys_role`;
CREATE TABLE `zz_sys_role` (
  `role_id` bigint(20) NOT NULL COMMENT '主键Id',
  `role_name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '角色名称',
  `create_user_id` bigint(20) NOT NULL COMMENT '创建者Id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint(20) NOT NULL COMMENT '更新者Id',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  `deleted_flag` int(11) NOT NULL COMMENT '逻辑删除标记(1: 正常 -1: 已删除)',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=COMPACT COMMENT='系统角色表';

-- ----------------------------
-- 用户与角色对应关系表
-- ----------------------------
DROP TABLE IF EXISTS `zz_sys_user_role`;
CREATE TABLE `zz_sys_user_role` (
  `user_id` bigint(20) NOT NULL COMMENT '用户Id',
  `role_id` bigint(20) NOT NULL COMMENT '角色Id',
  PRIMARY KEY (`user_id`,`role_id`) USING BTREE,
  KEY `idx_role_id` (`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=COMPACT COMMENT='用户与角色对应关系表';

-- ----------------------------
-- 菜单和操作权限管理表
-- ----------------------------
DROP TABLE IF EXISTS `zz_sys_menu`;
CREATE TABLE `zz_sys_menu` (
  `menu_id` bigint(20) NOT NULL COMMENT '主键Id',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父菜单Id，目录菜单的父菜单为null',
  `menu_name` varchar(50) COLLATE utf8mb4_bin NOT NULL COMMENT '菜单显示名称',
  `menu_type` int(11) NOT NULL COMMENT '(0: 目录 1: 菜单 2: 按钮 3: UI片段)',
  `form_router_name` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '前端表单路由名称，仅用于menu_type为1的菜单类型',
  `online_form_id` bigint(20) DEFAULT NULL COMMENT '在线表单主键Id',
  `online_menu_perm_type` int(11) DEFAULT NULL COMMENT '在线表单菜单的权限控制类型',
  `show_order` int(11) NOT NULL COMMENT '菜单显示顺序 (值越小，排序越靠前)',
  `icon` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '菜单图标',
  `create_user_id` bigint(20) NOT NULL COMMENT '创建者Id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint(20) NOT NULL COMMENT '更新者Id',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  `deleted_flag` int(11) NOT NULL COMMENT '逻辑删除标记(1: 正常 -1: 已删除)',
  PRIMARY KEY (`menu_id`) USING BTREE,
  KEY `idx_show_order` (`show_order`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=COMPACT COMMENT='菜单和操作权限管理表';

-- ----------------------------
-- 角色与菜单对应关系表
-- ----------------------------
DROP TABLE IF EXISTS `zz_sys_role_menu`;
CREATE TABLE `zz_sys_role_menu` (
  `role_id` bigint(20) NOT NULL COMMENT '角色Id',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单Id',
  PRIMARY KEY (`role_id`,`menu_id`) USING BTREE,
  KEY `idx_menu_id` (`menu_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=COMPACT COMMENT='角色与菜单对应关系表';

-- ----------------------------
-- 系统权限资源表
-- ----------------------------
DROP TABLE IF EXISTS `zz_sys_perm_code`;
CREATE TABLE `zz_sys_perm_code` (
  `perm_code_id` bigint(20) NOT NULL COMMENT '主键Id',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '上级权限字Id',
  `perm_code` varchar(128) COLLATE utf8_bin NOT NULL COMMENT '权限字标识(一般为有含义的英文字符串)',
  `perm_code_type` int(11) NOT NULL COMMENT '类型(0: 表单 1: UI片段 2: 操作)',
  `show_name` varchar(128) COLLATE utf8_bin NOT NULL COMMENT '显示名称',
  `show_order` int(11) NOT NULL COMMENT '显示顺序(数值越小，越靠前)',
  `create_user_id` bigint(20) NOT NULL COMMENT '创建者Id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint(20) NOT NULL COMMENT '更新者Id',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  `deleted_flag` int(11) NOT NULL COMMENT '逻辑删除标记(1: 正常 -1: 已删除)',
  PRIMARY KEY (`perm_code_id`),
  UNIQUE KEY `uk_perm_code` (`perm_code`,`deleted_flag`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE,
  KEY `idx_show_order` (`show_order`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=COMPACT COMMENT='系统权限资源表';

-- ----------------------------
-- 菜单和权限关系表
-- ----------------------------
DROP TABLE IF EXISTS `zz_sys_menu_perm_code`;
CREATE TABLE `zz_sys_menu_perm_code` (
  `menu_id` bigint(20) NOT NULL COMMENT '关联菜单Id',
  `perm_code_id` bigint(20) NOT NULL COMMENT '关联权限字Id',
  PRIMARY KEY (`menu_id`,`perm_code_id`) USING BTREE,
  KEY `idx_perm_code_id` (`perm_code_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='菜单和权限关系表';

-- ----------------------------
-- 系统权限模块表
-- ----------------------------
DROP TABLE IF EXISTS `zz_sys_perm_module`;
CREATE TABLE `zz_sys_perm_module` (
  `module_id` bigint(20) NOT NULL COMMENT '权限模块id',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '上级权限模块id',
  `module_name` varchar(64) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '权限模块名称',
  `module_type` int(11) NOT NULL COMMENT '模块类型(0: 普通模块 1: Controller模块)',
  `show_order` int(11) NOT NULL DEFAULT 0 COMMENT '权限模块在当前层级下的顺序，由小到大',
  `create_user_id` bigint(20) NOT NULL COMMENT '创建者Id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint(20) NOT NULL COMMENT '更新者Id',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  `deleted_flag` int(11) NOT NULL COMMENT '逻辑删除标记(1: 正常 -1: 已删除)',
  PRIMARY KEY (`module_id`) USING BTREE,
  KEY `idx_show_order` (`show_order`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE,
  KEY `idx_module_type` (`module_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=COMPACT COMMENT='系统权限模块表';

-- ----------------------------
-- 系统权限表
-- ----------------------------
DROP TABLE IF EXISTS `zz_sys_perm`;
CREATE TABLE `zz_sys_perm` (
  `perm_id` bigint(20) NOT NULL COMMENT '权限id',
  `module_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '权限所在的权限模块id',
  `perm_name` varchar(64) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '权限名称',
  `url` varchar(128) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '关联的url',
  `show_order` int(11) NOT NULL DEFAULT 0 COMMENT '权限在当前模块下的顺序，由小到大',
  `create_user_id` bigint(20) NOT NULL COMMENT '创建者Id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint(20) NOT NULL COMMENT '更新者Id',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  `deleted_flag` int(11) NOT NULL COMMENT '逻辑删除标记(1: 正常 -1: 已删除)',
  PRIMARY KEY (`perm_id`) USING BTREE,
  KEY `idx_show_order` (`show_order`) USING BTREE,
  KEY `idx_module_id` (`module_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=COMPACT COMMENT='系统权限表';

-- ----------------------------
-- 系统权限字和权限资源关联表
-- ----------------------------
DROP TABLE IF EXISTS `zz_sys_perm_code_perm`;
CREATE TABLE `zz_sys_perm_code_perm` (
  `perm_code_id` bigint(20) NOT NULL COMMENT '权限字Id',
  `perm_id` bigint(20) NOT NULL COMMENT '权限id',
  PRIMARY KEY (`perm_code_id`,`perm_id`),
  KEY `idx_perm_id` (`perm_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=COMPACT COMMENT='系统权限字和权限资源关联表';

-- ----------------------------
-- 权限资源白名单表
-- ----------------------------
DROP TABLE IF EXISTS `zz_sys_perm_whitelist`;
CREATE TABLE `zz_sys_perm_whitelist` (
  `perm_url` varchar(512) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '权限资源的url',
  `module_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '权限资源所属模块名字(通常是Controller的名字)',
  `perm_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '权限的名称',
  PRIMARY KEY (`perm_url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='权限资源白名单表(认证用户均可访问的url资源)';

-- ----------------------------
-- 数据权限表
-- ----------------------------
DROP TABLE IF EXISTS `zz_sys_data_perm`;
CREATE TABLE `zz_sys_data_perm` (
  `data_perm_id` bigint(20) NOT NULL COMMENT '主键',
  `data_perm_name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '显示名称',
  `rule_type` tinyint(2) NOT NULL COMMENT '数据权限规则类型(0: 全部可见 1: 只看自己 2: 只看本部门 3: 本部门及子部门 4: 多部门及子部门 5: 自定义部门列表)。',
  `create_user_id` bigint(20) NOT NULL COMMENT '创建者Id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint(20) NOT NULL COMMENT '更新者Id',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  `deleted_flag` int(11) NOT NULL COMMENT '逻辑删除标记(1: 正常 -1: 已删除)',
  PRIMARY KEY (`data_perm_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='数据权限表';

-- ----------------------------
-- 数据权限和用户关联表
-- ----------------------------
DROP TABLE IF EXISTS `zz_sys_data_perm_user`;
CREATE TABLE `zz_sys_data_perm_user` (
  `data_perm_id` bigint(20) NOT NULL COMMENT '数据权限Id',
  `user_id` bigint(20) NOT NULL COMMENT '用户Id',
  PRIMARY KEY (`data_perm_id`,`user_id`),
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='数据权限和用户关联表';

-- ----------------------------
-- 数据权限和部门关联表
-- ----------------------------
DROP TABLE IF EXISTS `zz_sys_data_perm_dept`;
CREATE TABLE `zz_sys_data_perm_dept` (
  `data_perm_id` bigint(20) NOT NULL COMMENT '数据权限Id',
  `dept_id` bigint(20) NOT NULL COMMENT '部门Id',
  PRIMARY KEY (`data_perm_id`,`dept_id`),
  KEY `idx_dept_id` (`dept_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='数据权限和部门关联表';

-- ----------------------------
-- 事务型消息的生产者发送数据流水表
-- ----------------------------
DROP TABLE IF EXISTS `zz_transaction_message_producer_trans`;
CREATE TABLE `zz_transaction_message_producer_trans` (
  `trans_id` varchar(128) COLLATE utf8mb4_bin NOT NULL COMMENT '消息事务Id',
  `message_type` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '消息类型',
  `command_type` varchar(64) COLLATE utf8mb4_bin COMMENT '命令类型',
  `message_topic` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '消息主题',
  `producer_trace_id` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '生产者traceId',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`trans_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='用于事务型消息的生产者发送数据流水表';

-- ----------------------------
-- 数据同步消费者消息流水表
-- ----------------------------
DROP TABLE IF EXISTS `zz_data_sync_consumer_trans`;
CREATE TABLE `zz_data_sync_consumer_trans` (
  `trans_id` char(80) COLLATE utf8mb4_bin NOT NULL COMMENT '同步事务Id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`trans_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='数据同步消费者消息流水表';

-- ----------------------------
-- 数据同步生产者消息流水表
-- ----------------------------
DROP TABLE IF EXISTS `zz_data_sync_producer_trans`;
CREATE TABLE `zz_data_sync_producer_trans` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `trans_id` char(80) COLLATE utf8mb4_bin NOT NULL COMMENT '同步事务Id',
  `message_type` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '消息类型',
  `message_command` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '消息的命令类型',
  `message_data` mediumtext COLLATE utf8mb4_bin NOT NULL COMMENT '消息数据',
  `message_topic` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '消息主题',
  `message_queue_selector_key` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '计算消息队列选择器的Key值',
  `producer_trace_id` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '生产者traceId',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sent_status` bit(1) NOT NULL DEFAULT b'0' COMMENT '发送状态',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_trans_id` (`trans_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='数据同步生产者消息流水表';

-- ----------------------------
-- 数据同步生产者消息流水表的高水标记表
-- ----------------------------
DROP TABLE IF EXISTS `zz_data_sync_producer_update_mark`;
CREATE TABLE `zz_data_sync_producer_update_mark` (
  `id` int(11) NOT NULL COMMENT '主键Id',
  `max_update_id` bigint(20) NOT NULL COMMENT '生产者读取的最大更新同步事务Id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='数据同步生产者消息流水表的高水标记表';

BEGIN;
INSERT INTO `zz_data_sync_producer_update_mark` VALUES (1,0,CURDATE());
COMMIT;


-- ----------------------------
-- 管理员账号数据
-- ----------------------------
BEGIN;
INSERT INTO `zz_sys_dept` VALUES(1293376634891538435,NULL,'公司总部',1,1293376634887344128,CURDATE(),1293376634887344128,CURDATE(),1);
INSERT INTO `zz_sys_user` VALUES(1293376634887344128,'admin','管理员',1293376634891538435,NULL,0,0,1293376634887344128,CURDATE(),1293376634887344128,CURDATE(),1);
INSERT INTO `zz_sys_dept_relation` VALUES(1293376634891538435,1293376634891538435);
COMMIT;
SET FOREIGN_KEY_CHECKS = 1;
