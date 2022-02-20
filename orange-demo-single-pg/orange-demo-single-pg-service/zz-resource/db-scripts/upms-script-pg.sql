-- ----------------------------
-- 请仅在下面的数据库链接中执行该脚本。
-- 主数据源 [localhost:5432/zzdemo-single]
-- ----------------------------

-- ----------------------------
-- 部门管理表
-- ----------------------------
DROP TABLE IF EXISTS "zz_sys_dept";
CREATE TABLE "zz_sys_dept" (
  "dept_id" int8 NOT NULL,
  "parent_id" int8,
  "dept_name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "show_order" int4 NOT NULL,
  "create_user_id" int8 NOT NULL,
  "create_time" timestamp(6) NOT NULL,
  "update_user_id" int8 NOT NULL,
  "update_time" timestamp(6) NOT NULL,
  "deleted_flag" int4 NOT NULL
);
COMMENT ON COLUMN "zz_sys_dept"."dept_id" IS '部门Id';
COMMENT ON COLUMN "zz_sys_dept"."parent_id" IS '父部门Id';
COMMENT ON COLUMN "zz_sys_dept"."dept_name" IS '部门名称';
COMMENT ON COLUMN "zz_sys_dept"."show_order" IS '兄弟部分之间的显示顺序，数字越小越靠前';
COMMENT ON COLUMN "zz_sys_dept"."create_user_id" IS '创建者';
COMMENT ON COLUMN "zz_sys_dept"."create_time" IS '创建时间';
COMMENT ON COLUMN "zz_sys_dept"."update_user_id" IS '更新者Id';
COMMENT ON COLUMN "zz_sys_dept"."update_time" IS '最后更新时间';
COMMENT ON COLUMN "zz_sys_dept"."deleted_flag" IS '删除标记(1: 正常 -1: 已删除)';
COMMENT ON TABLE "zz_sys_dept" IS '部门管理表';

CREATE INDEX "idx_zz_sys_dept_parent_id" ON "zz_sys_dept" USING btree (
  "parent_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_zz_sys_dept_show_order" ON "zz_sys_dept" USING btree (
  "show_order" "pg_catalog"."int4_ops" ASC NULLS LAST
);
ALTER TABLE "zz_sys_dept" ADD CONSTRAINT "zz_sys_dept_pkey" PRIMARY KEY ("dept_id");

-- ----------------------------
-- 部门关联关系表
-- ----------------------------
DROP TABLE IF EXISTS "zz_sys_dept_relation";
CREATE TABLE "zz_sys_dept_relation" (
  "parent_dept_id" int8 NOT NULL,
  "dept_id" int8 NOT NULL
);
COMMENT ON COLUMN "zz_sys_dept_relation"."parent_dept_id" IS '父部门Id';
COMMENT ON COLUMN "zz_sys_dept_relation"."dept_id" IS '部门Id';
COMMENT ON TABLE "zz_sys_dept_relation" IS '部门关联关系表';

CREATE INDEX "idx_zz_sys_dept_relation_dept_id" ON "zz_sys_dept_relation" USING btree (
  "dept_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
ALTER TABLE "zz_sys_dept_relation" ADD CONSTRAINT "zz_sys_dept_relation_pkey" PRIMARY KEY ("parent_dept_id", "dept_id");

-- ----------------------------
-- 系统用户表
-- ----------------------------
DROP TABLE IF EXISTS "zz_sys_user";
CREATE TABLE "zz_sys_user" (
  "user_id" int8 NOT NULL,
  "login_name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "password" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "show_name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "dept_id" int8 NOT NULL,
  "user_type" int4 NOT NULL,
  "head_image_url" varchar(255) COLLATE "pg_catalog"."default",
  "user_status" int4 NOT NULL,
  "create_user_id" int8 NOT NULL,
  "create_time" timestamp(6) NOT NULL,
  "update_user_id" int8 NOT NULL,
  "update_time" timestamp(6) NOT NULL,
  "deleted_flag" int4 NOT NULL
);
COMMENT ON COLUMN "zz_sys_user"."user_id" IS '主键Id';
COMMENT ON COLUMN "zz_sys_user"."login_name" IS '用户登录名称';
COMMENT ON COLUMN "zz_sys_user"."password" IS '用户密码';
COMMENT ON COLUMN "zz_sys_user"."show_name" IS '用户显示名称';
COMMENT ON COLUMN "zz_sys_user"."dept_id" IS '用户所在部门Id';
COMMENT ON COLUMN "zz_sys_user"."user_type" IS '用户类型(0: 管理员 1: 系统管理用户 2: 系统业务用户)';
COMMENT ON COLUMN "zz_sys_user"."head_image_url" IS '用户头像的Url';
COMMENT ON COLUMN "zz_sys_user"."user_status" IS '状态(0: 正常 1: 锁定)';
COMMENT ON COLUMN "zz_sys_user"."create_user_id" IS '创建者';
COMMENT ON COLUMN "zz_sys_user"."create_time" IS '创建时间';
COMMENT ON COLUMN "zz_sys_user"."update_user_id" IS '更新者Id';
COMMENT ON COLUMN "zz_sys_user"."update_time" IS '最后更新时间';
COMMENT ON COLUMN "zz_sys_user"."deleted_flag" IS '删除标记(1: 正常 -1: 已删除)';
COMMENT ON TABLE "zz_sys_user" IS '系统用户表';

CREATE INDEX "idx_zz_sys_user_dept_id" ON "zz_sys_user" USING btree (
  "dept_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_zz_sys_user_status" ON "zz_sys_user" USING btree (
  "user_status" "pg_catalog"."int4_ops" ASC NULLS LAST
);
CREATE UNIQUE INDEX "uk_zz_sys_user_login_name" ON "zz_sys_user" USING btree (
  "login_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
ALTER TABLE "zz_sys_user" ADD CONSTRAINT "zz_sys_user_pkey" PRIMARY KEY ("user_id");

-- ----------------------------
-- 系统角色表
-- ----------------------------
DROP TABLE IF EXISTS "zz_sys_role";
CREATE TABLE "zz_sys_role" (
  "role_id" int8 NOT NULL,
  "role_name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "create_user_id" int8 NOT NULL,
  "create_time" timestamp(6) NOT NULL,
  "update_user_id" int8 NOT NULL,
  "update_time" timestamp(6) NOT NULL
);
COMMENT ON COLUMN "zz_sys_role"."role_id" IS '主键Id';
COMMENT ON COLUMN "zz_sys_role"."role_name" IS '角色名称';
COMMENT ON COLUMN "zz_sys_role"."create_user_id" IS '创建者';
COMMENT ON COLUMN "zz_sys_role"."create_time" IS '创建时间';
COMMENT ON COLUMN "zz_sys_role"."update_user_id" IS '更新者Id';
COMMENT ON COLUMN "zz_sys_role"."update_time" IS '更新时间';
COMMENT ON TABLE "zz_sys_role" IS '系统角色表';

ALTER TABLE "zz_sys_role" ADD CONSTRAINT "zz_sys_role_pkey" PRIMARY KEY ("role_id");

-- ----------------------------
-- 用户与角色对应关系表
-- ----------------------------
DROP TABLE IF EXISTS "zz_sys_user_role";
CREATE TABLE "zz_sys_user_role" (
  "user_id" int8 NOT NULL,
  "role_id" int8 NOT NULL
);
COMMENT ON COLUMN "zz_sys_user_role"."user_id" IS '用户Id';
COMMENT ON COLUMN "zz_sys_user_role"."role_id" IS '角色Id';
COMMENT ON TABLE "zz_sys_user_role" IS '用户与角色对应关系表';

CREATE INDEX "idx_zz_sys_user_role_role_id" ON "zz_sys_user_role" USING btree (
  "role_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
ALTER TABLE "zz_sys_user_role" ADD CONSTRAINT "zz_sys_user_role_pkey" PRIMARY KEY ("user_id", "role_id");

-- ----------------------------
-- 菜单和操作权限管理表
-- ----------------------------
DROP TABLE IF EXISTS "zz_sys_menu";
CREATE TABLE "zz_sys_menu" (
  "menu_id" int8 NOT NULL,
  "parent_id" int8,
  "menu_name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "menu_type" int4 NOT NULL,
  "form_router_name" varchar(64) COLLATE "pg_catalog"."default",
  "online_form_id" int8,
  "online_menu_perm_type" int4,
  "show_order" int4 NOT NULL,
  "icon" varchar(50) COLLATE "pg_catalog"."default",
  "create_user_id" int8 NOT NULL,
  "create_time" timestamp(6) NOT NULL,
  "update_user_id" int8 NOT NULL,
  "update_time" timestamp(6) NOT NULL
);
COMMENT ON COLUMN "zz_sys_menu"."menu_id" IS '主键Id';
COMMENT ON COLUMN "zz_sys_menu"."parent_id" IS '父菜单Id，目录菜单的父菜单为null';
COMMENT ON COLUMN "zz_sys_menu"."menu_name" IS '菜单显示名称';
COMMENT ON COLUMN "zz_sys_menu"."menu_type" IS '(0: 目录 1: 菜单 2: 按钮 3: UI片段)';
COMMENT ON COLUMN "zz_sys_menu"."form_router_name" IS '前端表单路由名称，仅用于menu_type为1的菜单类型';
COMMENT ON COLUMN "zz_sys_menu"."online_form_id" IS '在线表单主键Id';
COMMENT ON COLUMN "zz_sys_menu"."online_menu_perm_type" IS '在线表单菜单的权限控制类型';
COMMENT ON COLUMN "zz_sys_menu"."show_order" IS '菜单显示顺序 (值越小，排序越靠前)';
COMMENT ON COLUMN "zz_sys_menu"."icon" IS '菜单图标';
COMMENT ON COLUMN "zz_sys_menu"."create_user_id" IS '创建者';
COMMENT ON COLUMN "zz_sys_menu"."create_time" IS '创建时间';
COMMENT ON COLUMN "zz_sys_menu"."update_user_id" IS '更新者Id';
COMMENT ON COLUMN "zz_sys_menu"."update_time" IS '更新时间';
COMMENT ON TABLE "zz_sys_menu" IS '菜单和操作权限管理表';

CREATE INDEX "idx_zz_sys_menu_parent_id" ON "zz_sys_menu" USING btree (
  "parent_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_zz_sys_menu_show_order" ON "zz_sys_menu" USING btree (
  "show_order" "pg_catalog"."int4_ops" ASC NULLS LAST
);
ALTER TABLE "zz_sys_menu" ADD CONSTRAINT "zz_sys_menu_pkey" PRIMARY KEY ("menu_id");

-- ----------------------------
-- 角色与菜单对应关系表
-- ----------------------------
DROP TABLE IF EXISTS "zz_sys_role_menu";
CREATE TABLE "zz_sys_role_menu" (
  "role_id" int8 NOT NULL,
  "menu_id" int8 NOT NULL
);
COMMENT ON COLUMN "zz_sys_role_menu"."role_id" IS '角色Id';
COMMENT ON COLUMN "zz_sys_role_menu"."menu_id" IS '菜单Id';
COMMENT ON TABLE "zz_sys_role_menu" IS '角色与菜单对应关系表';

CREATE INDEX "idx_zz_sys_role_menu_menu_id" ON "zz_sys_role_menu" USING btree (
  "menu_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
ALTER TABLE "zz_sys_role_menu" ADD CONSTRAINT "zz_sys_role_menu_pkey" PRIMARY KEY ("role_id", "menu_id");

-- ----------------------------
-- 系统权限资源表
-- ----------------------------
DROP TABLE IF EXISTS "zz_sys_perm_code";
CREATE TABLE "zz_sys_perm_code" (
  "perm_code_id" int8 NOT NULL,
  "parent_id" int8,
  "perm_code" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "perm_code_type" int4 NOT NULL,
  "show_name" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "show_order" int4 NOT NULL,
  "create_user_id" int8 NOT NULL,
  "create_time" timestamp(6) NOT NULL,
  "update_user_id" int8 NOT NULL,
  "update_time" timestamp(6) NOT NULL
);
COMMENT ON COLUMN "zz_sys_perm_code"."perm_code_id" IS '主键Id';
COMMENT ON COLUMN "zz_sys_perm_code"."parent_id" IS '上级权限字Id';
COMMENT ON COLUMN "zz_sys_perm_code"."perm_code" IS '权限字标识(一般为有含义的英文字符串)';
COMMENT ON COLUMN "zz_sys_perm_code"."perm_code_type" IS '类型(0: 表单 1: UI片段 2: 操作)';
COMMENT ON COLUMN "zz_sys_perm_code"."show_name" IS '显示名称';
COMMENT ON COLUMN "zz_sys_perm_code"."show_order" IS '显示顺序(数值越小，越靠前)';
COMMENT ON COLUMN "zz_sys_perm_code"."create_user_id" IS '创建者';
COMMENT ON COLUMN "zz_sys_perm_code"."create_time" IS '创建时间';
COMMENT ON COLUMN "zz_sys_perm_code"."update_user_id" IS '更新者Id';
COMMENT ON COLUMN "zz_sys_perm_code"."update_time" IS '更新时间';
COMMENT ON TABLE "zz_sys_perm_code" IS '系统权限资源表';

CREATE INDEX "idx_zz_sys_perm_code_parent_id" ON "zz_sys_perm_code" USING btree (
  "parent_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE UNIQUE INDEX "uk_zz_sys_perm_code_perm_code" ON "zz_sys_perm_code" USING btree (
  "perm_code" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_zz_sys_perm_code_show_order" ON "zz_sys_perm_code" USING btree (
  "show_order" "pg_catalog"."int4_ops" ASC NULLS LAST
);
ALTER TABLE "zz_sys_perm_code" ADD CONSTRAINT "zz_sys_perm_code_pkey" PRIMARY KEY ("perm_code_id");

-- ----------------------------
-- 菜单和权限关系表
-- ----------------------------
DROP TABLE IF EXISTS "zz_sys_menu_perm_code";
CREATE TABLE "zz_sys_menu_perm_code" (
  "menu_id" int8 NOT NULL,
  "perm_code_id" int8 NOT NULL
);
COMMENT ON COLUMN "zz_sys_menu_perm_code"."menu_id" IS '关联菜单Id';
COMMENT ON COLUMN "zz_sys_menu_perm_code"."perm_code_id" IS '关联权限字Id';
COMMENT ON TABLE "zz_sys_menu_perm_code" IS '菜单和权限关系表';

CREATE INDEX "idx_zz_sys_menu_perm_code_perm_code_id" ON "zz_sys_menu_perm_code" USING btree (
  "perm_code_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
ALTER TABLE "zz_sys_menu_perm_code" ADD CONSTRAINT "zz_sys_menu_perm_code_pkey" PRIMARY KEY ("menu_id", "perm_code_id");

-- ----------------------------
-- 系统权限模块表
-- ----------------------------
DROP TABLE IF EXISTS "zz_sys_perm_module";
CREATE TABLE "zz_sys_perm_module" (
  "module_id" int8 NOT NULL,
  "parent_id" int8 DEFAULT 0,
  "module_name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "module_type" int4 NOT NULL,
  "show_order" int4 NOT NULL DEFAULT 0,
  "create_user_id" int8 NOT NULL,
  "create_time" timestamp(6) NOT NULL,
  "update_user_id" int8 NOT NULL,
  "update_time" timestamp(6) NOT NULL
);
COMMENT ON COLUMN "zz_sys_perm_module"."module_id" IS '权限模块id';
COMMENT ON COLUMN "zz_sys_perm_module"."parent_id" IS '上级权限模块id';
COMMENT ON COLUMN "zz_sys_perm_module"."module_name" IS '权限模块名称';
COMMENT ON COLUMN "zz_sys_perm_module"."module_type" IS '模块类型(0: 普通模块 1: Controller模块)';
COMMENT ON COLUMN "zz_sys_perm_module"."show_order" IS '权限模块在当前层级下的顺序，由小到大';
COMMENT ON COLUMN "zz_sys_perm_module"."create_user_id" IS '创建者';
COMMENT ON COLUMN "zz_sys_perm_module"."create_time" IS '创建时间';
COMMENT ON COLUMN "zz_sys_perm_module"."update_user_id" IS '更新者Id';
COMMENT ON COLUMN "zz_sys_perm_module"."update_time" IS '更新时间';
COMMENT ON TABLE "zz_sys_perm_module" IS '系统权限模块表';

CREATE INDEX "idx_zz_sys_perm_module_module_type" ON "zz_sys_perm_module" USING btree (
  "module_type" "pg_catalog"."int4_ops" ASC NULLS LAST
);
CREATE INDEX "idx_zz_sys_perm_module_parent_id" ON "zz_sys_perm_module" USING btree (
  "parent_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_zz_sys_perm_module_show_order" ON "zz_sys_perm_module" USING btree (
  "show_order" "pg_catalog"."int4_ops" ASC NULLS LAST
);
ALTER TABLE "zz_sys_perm_module" ADD CONSTRAINT "zz_sys_perm_module_pkey" PRIMARY KEY ("module_id");

-- ----------------------------
-- 系统权限表
-- ----------------------------
DROP TABLE IF EXISTS "zz_sys_perm";
CREATE TABLE "zz_sys_perm" (
  "perm_id" int8 NOT NULL,
  "module_id" int8 NOT NULL DEFAULT 0,
  "perm_name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "url" varchar(128) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "show_order" int4 NOT NULL DEFAULT 0,
  "create_user_id" int8 NOT NULL,
  "create_time" timestamp(6) NOT NULL,
  "update_user_id" int8 NOT NULL,
  "update_time" timestamp(6) NOT NULL
);
COMMENT ON COLUMN "zz_sys_perm"."perm_id" IS '权限id';
COMMENT ON COLUMN "zz_sys_perm"."module_id" IS '权限所在的权限模块id';
COMMENT ON COLUMN "zz_sys_perm"."perm_name" IS '权限名称';
COMMENT ON COLUMN "zz_sys_perm"."url" IS '关联的url';
COMMENT ON COLUMN "zz_sys_perm"."show_order" IS '权限在当前模块下的顺序，由小到大';
COMMENT ON COLUMN "zz_sys_perm"."create_user_id" IS '创建者';
COMMENT ON COLUMN "zz_sys_perm"."create_time" IS '创建时间';
COMMENT ON COLUMN "zz_sys_perm"."update_user_id" IS '更新者Id';
COMMENT ON COLUMN "zz_sys_perm"."update_time" IS '更新时间';
COMMENT ON TABLE "zz_sys_perm" IS '系统权限表';

CREATE INDEX "idx_zz_sys_perm_module_id" ON "zz_sys_perm" USING btree (
  "module_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_zz_sys_perm_show_order" ON "zz_sys_perm" USING btree (
  "show_order" "pg_catalog"."int4_ops" ASC NULLS LAST
);
ALTER TABLE "zz_sys_perm" ADD CONSTRAINT "zz_sys_perm_pkey" PRIMARY KEY ("perm_id");

-- ----------------------------
-- 系统权限字和权限资源关联表
-- ----------------------------
DROP TABLE IF EXISTS "zz_sys_perm_code_perm";
CREATE TABLE "zz_sys_perm_code_perm" (
  "perm_code_id" int8 NOT NULL,
  "perm_id" int8 NOT NULL
);
COMMENT ON COLUMN "zz_sys_perm_code_perm"."perm_code_id" IS '权限字Id';
COMMENT ON COLUMN "zz_sys_perm_code_perm"."perm_id" IS '权限id';
COMMENT ON TABLE "zz_sys_perm_code_perm" IS '系统权限字和权限资源关联表';

CREATE INDEX "idx_zz_sys_perm_code_perm_perm_id" ON "zz_sys_perm_code_perm" USING btree (
  "perm_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
ALTER TABLE "zz_sys_perm_code_perm" ADD CONSTRAINT "zz_sys_perm_code_perm_pkey" PRIMARY KEY ("perm_code_id", "perm_id");

-- ----------------------------
-- 权限资源白名单表
-- ----------------------------
DROP TABLE IF EXISTS "zz_sys_perm_whitelist";
CREATE TABLE "zz_sys_perm_whitelist" (
  "perm_url" varchar(512) COLLATE "pg_catalog"."default" NOT NULL,
  "module_name" varchar(64) COLLATE "pg_catalog"."default",
  "perm_name" varchar(64) COLLATE "pg_catalog"."default"
);
COMMENT ON COLUMN "zz_sys_perm_whitelist"."perm_url" IS '权限资源的url';
COMMENT ON COLUMN "zz_sys_perm_whitelist"."module_name" IS '权限资源所属模块名字(通常是Controller的名字)';
COMMENT ON COLUMN "zz_sys_perm_whitelist"."perm_name" IS '权限的名称';
COMMENT ON TABLE "zz_sys_perm_whitelist" IS '权限资源白名单表(认证用户均可访问的url资源)';

ALTER TABLE "zz_sys_perm_whitelist" ADD CONSTRAINT "zz_sys_perm_whitelist_pkey" PRIMARY KEY ("perm_url");

-- ----------------------------
-- 数据权限表
-- ----------------------------
DROP TABLE IF EXISTS "zz_sys_data_perm";
CREATE TABLE "zz_sys_data_perm" (
  "data_perm_id" int8 NOT NULL,
  "data_perm_name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rule_type" int2 NOT NULL,
  "create_user_id" int8 NOT NULL,
  "create_time" timestamp(6) NOT NULL,
  "update_user_id" int8 NOT NULL,
  "update_time" timestamp(6) NOT NULL
);
COMMENT ON COLUMN "zz_sys_data_perm"."data_perm_id" IS '主键';
COMMENT ON COLUMN "zz_sys_data_perm"."data_perm_name" IS '显示名称';
COMMENT ON COLUMN "zz_sys_data_perm"."rule_type" IS '数据权限规则类型(0: 全部可见 1: 只看自己 2: 只看本部门 3: 本部门及子部门 4: 多部门及子部门 5: 自定义部门列表)。';
COMMENT ON COLUMN "zz_sys_data_perm"."create_user_id" IS '创建者';
COMMENT ON COLUMN "zz_sys_data_perm"."create_time" IS '创建时间';
COMMENT ON COLUMN "zz_sys_data_perm"."update_user_id" IS '更新者Id';
COMMENT ON COLUMN "zz_sys_data_perm"."update_time" IS '更新时间';
COMMENT ON TABLE "zz_sys_data_perm" IS '数据权限表';

CREATE INDEX "idx_zz_sys_data_perm_create_time" ON "zz_sys_data_perm" USING btree (
  "create_time" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
ALTER TABLE "zz_sys_data_perm" ADD CONSTRAINT "zz_sys_data_perm_pkey" PRIMARY KEY ("data_perm_id");

-- ----------------------------
-- 数据权限和用户关联表
-- ----------------------------
DROP TABLE IF EXISTS "zz_sys_data_perm_user";
CREATE TABLE "zz_sys_data_perm_user" (
  "data_perm_id" int8 NOT NULL,
  "user_id" int8 NOT NULL
);
COMMENT ON COLUMN "zz_sys_data_perm_user"."data_perm_id" IS '数据权限Id';
COMMENT ON COLUMN "zz_sys_data_perm_user"."user_id" IS '用户Id';
COMMENT ON TABLE "zz_sys_data_perm_user" IS '数据权限和用户关联表';

CREATE INDEX "idx_sys_data_perm_user_user_id" ON "zz_sys_data_perm_user" USING btree (
  "user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
ALTER TABLE "zz_sys_data_perm_user" ADD CONSTRAINT "zz_sys_data_perm_user_pkey" PRIMARY KEY ("data_perm_id", "user_id");

-- ----------------------------
-- 数据权限和部门关联表
-- ----------------------------
DROP TABLE IF EXISTS "zz_sys_data_perm_dept";
CREATE TABLE "zz_sys_data_perm_dept" (
  "data_perm_id" int8 NOT NULL,
  "dept_id" int8 NOT NULL
);
COMMENT ON COLUMN "zz_sys_data_perm_dept"."data_perm_id" IS '数据权限Id';
COMMENT ON COLUMN "zz_sys_data_perm_dept"."dept_id" IS '部门Id';
COMMENT ON TABLE "zz_sys_data_perm_dept" IS '数据权限和部门关联表';

CREATE INDEX "idx_zz_sys_data_perm_dept_dept_id" ON "zz_sys_data_perm_dept" USING btree (
  "dept_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
ALTER TABLE "zz_sys_data_perm_dept" ADD CONSTRAINT "zz_sys_data_perm_dept_pkey" PRIMARY KEY ("data_perm_id", "dept_id");

-- ----------------------------
-- 系统操作日志表
-- ----------------------------
DROP TABLE IF EXISTS "zz_sys_operation_log";
CREATE TABLE "zz_sys_operation_log" (
  "log_id" int8 NOT NULL,
  "description" varchar(255) COLLATE "pg_catalog"."default",
  "operation_type" int4,
  "service_name" varchar(128) COLLATE "pg_catalog"."default",
  "api_class" varchar(255) COLLATE "pg_catalog"."default",
  "api_method" varchar(255) COLLATE "pg_catalog"."default",
  "session_id" varchar(255) COLLATE "pg_catalog"."default",
  "trace_id" char(32) COLLATE "pg_catalog"."default",
  "elapse" int4,
  "request_method" varchar(32) COLLATE "pg_catalog"."default",
  "request_url" varchar(255) COLLATE "pg_catalog"."default",
  "request_arguments" varchar(2000) COLLATE "pg_catalog"."default",
  "response_result" varchar(2000) COLLATE "pg_catalog"."default",
  "request_ip" varchar(32) COLLATE "pg_catalog"."default",
  "success" bool DEFAULT false,
  "error_msg" varchar(2000) COLLATE "pg_catalog"."default",
  "tenant_id" int8,
  "operator_id" int8,
  "operator_name" varchar(255) COLLATE "pg_catalog"."default",
  "operation_time" timestamp(6)
);
COMMENT ON COLUMN "zz_sys_operation_log"."log_id" IS '主键Id';
COMMENT ON COLUMN "zz_sys_operation_log"."description" IS '日志描述';
COMMENT ON COLUMN "zz_sys_operation_log"."operation_type" IS '操作类型';
COMMENT ON COLUMN "zz_sys_operation_log"."service_name" IS '接口所在服务名称';
COMMENT ON COLUMN "zz_sys_operation_log"."api_class" IS '调用的controller全类名';
COMMENT ON COLUMN "zz_sys_operation_log"."api_method" IS '调用的controller中的方法';
COMMENT ON COLUMN "zz_sys_operation_log"."session_id" IS '用户会话sessionId';
COMMENT ON COLUMN "zz_sys_operation_log"."trace_id" IS '每次请求的Id';
COMMENT ON COLUMN "zz_sys_operation_log"."elapse" IS '调用时长';
COMMENT ON COLUMN "zz_sys_operation_log"."request_method" IS 'HTTP 请求方法，如GET';
COMMENT ON COLUMN "zz_sys_operation_log"."request_url" IS 'HTTP 请求地址';
COMMENT ON COLUMN "zz_sys_operation_log"."request_arguments" IS 'controller接口参数';
COMMENT ON COLUMN "zz_sys_operation_log"."response_result" IS 'controller应答结果';
COMMENT ON COLUMN "zz_sys_operation_log"."request_ip" IS '请求IP';
COMMENT ON COLUMN "zz_sys_operation_log"."success" IS '应答状态';
COMMENT ON COLUMN "zz_sys_operation_log"."error_msg" IS '错误信息';
COMMENT ON COLUMN "zz_sys_operation_log"."tenant_id" IS '租户Id';
COMMENT ON COLUMN "zz_sys_operation_log"."operator_id" IS '操作员Id';
COMMENT ON COLUMN "zz_sys_operation_log"."operator_name" IS '操作员名称';
COMMENT ON COLUMN "zz_sys_operation_log"."operation_time" IS '操作时间';
COMMENT ON TABLE "zz_sys_operation_log" IS '系统操作日志表';

CREATE INDEX "idx_zz_sys_operation_log_elapse" ON "zz_sys_operation_log" USING btree (
  "elapse" "pg_catalog"."int4_ops" ASC NULLS LAST
);
CREATE INDEX "idx_zz_sys_operation_log_operation_time" ON "zz_sys_operation_log" USING btree (
  "operation_time" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_zz_sys_operation_log_operation_type" ON "zz_sys_operation_log" USING btree (
  "operation_type" "pg_catalog"."int4_ops" ASC NULLS LAST
);
CREATE INDEX "idx_zz_sys_operation_log_trace_id" ON "zz_sys_operation_log" USING btree (
  "trace_id" COLLATE "pg_catalog"."default" "pg_catalog"."bpchar_ops" ASC NULLS LAST
);
ALTER TABLE "zz_sys_operation_log" ADD CONSTRAINT "zz_sys_operation_log_pkey" PRIMARY KEY ("log_id");

-- ----------------------------
-- 管理员账号数据
-- ----------------------------
BEGIN;
INSERT INTO "zz_sys_dept" VALUES(1495250233481760770,NULL,'公司总部',1,1495250233473372160,NOW(),1495250233473372160,NOW(),1);
INSERT INTO "zz_sys_user" VALUES(1495250233473372160,'admin','$2a$10$yGxCD/evW6n2Uc5MWS7mHuJ.goP./8VtznZ7ezpkfaFg280pppkW6','管理员',1495250233481760770,0,NULL,0,1495250233473372160,NOW(),1495250233473372160,NOW(),1);
INSERT INTO "zz_sys_dept_relation" VALUES(1495250233481760770,1495250233481760770);
COMMIT;
