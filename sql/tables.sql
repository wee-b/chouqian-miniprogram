

-- === 说明 ===
# 主键类型都为bigint,并且主键名称不能为id
# 每个表都有一下三个字段，部分表只有create_time字段表示其不可修改
# 含有status字段代表需要添加的时候需要审核
#     `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0-待审核 1-启用 2-下架 5-禁用',
#     `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
#     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
#     `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除状态：0-未删除 1-已删除',




DROP DATABASE IF EXISTS `quick_start`;
CREATE DATABASE IF NOT EXISTS `quick_start` ;
USE `quick_start`;




-- 一、系统相关表
-- 1.1 菜单权限表
DROP TABLE IF EXISTS `qs_menu`;
CREATE TABLE `qs_menu`
(
    `menu_id` int NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
    `parent_id` int DEFAULT 0 COMMENT '父菜单ID',
    `order_num` tinyint(4) DEFAULT 0 COMMENT '显示顺序',
    `path` varchar(200) DEFAULT '' COMMENT '路由地址',
    `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
    `is_frame` tinyint(1) DEFAULT 1 COMMENT '是否为外链（0是 1否）',
    `menu_type` tinyint(1) DEFAULT 1 COMMENT '菜单类型（1目录 2菜单 3按钮）',
    `visible` tinyint(1) DEFAULT 0 COMMENT '菜单状态（0显示 1隐藏）',
    `perms` varchar(100) DEFAULT NULL COMMENT '权限标识（如content:article:list）',
    `icon` varchar(100) DEFAULT '#' COMMENT '菜单图标',

    `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：1-启用 2-下架 ',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2034 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单权限表';



-- 1.2 角色表
DROP TABLE IF EXISTS `qs_role`;
CREATE TABLE `qs_role`
(
    `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name` varchar(30) NOT NULL COMMENT '角色名称',
    `role_key` varchar(100) NOT NULL COMMENT '角色权限字符串',
    `role_sort` int(4) NOT NULL COMMENT '显示顺序',

    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：1-启用 2-下架 ',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除状态：0-未删除 1-已删除',
    PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色信息表';




-- 1.3 角色菜单关联表
DROP TABLE IF EXISTS `qs_role_menu`;
CREATE TABLE `qs_role_menu`
(
    `role_menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_id` bigint NOT NULL COMMENT '角色ID',
    `menu_id` bigint NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`role_menu_id`),
    UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`) COMMENT '确保角色与菜单的关联唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色和菜单关联表';



-- 1.4 用户角色关联表
DROP TABLE IF EXISTS `qs_user_role`;
CREATE TABLE `qs_user_role`
(
    `user_role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `role_id` bigint NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_role_id`),
    UNIQUE KEY `uk_user_role` (`user_id`,`role_id`) COMMENT '确保用户与角色的关联唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户和角色关联表';




-- 二，业务相关表
-- qs_user
DROP TABLE IF EXISTS `qs_user`;
CREATE TABLE `qs_user`
(
    `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `user_code` varchar(50)  NOT NULL COMMENT '用户编码',
    `user_type` int(2)  NOT NULL COMMENT '用户类型',
    `user_name` varchar(50)  NOT NULL COMMENT '用户昵称',
    `phone` varchar(20)  NOT NULL COMMENT '手机号',
    `email` varchar(100)  NULL DEFAULT NULL COMMENT '邮箱',
    `password` varchar(100)  NOT NULL COMMENT '密码（加密）',
    `avatar` varchar(500)  NULL DEFAULT NULL COMMENT '头像',
    `gender` tinyint(1) NULL DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    `birthday` datetime NULL DEFAULT NULL COMMENT '生日',
    `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` varchar(50) NULL DEFAULT NULL COMMENT '最后登录IP',
    `register_source` int(2) NOT NULL DEFAULT 1 COMMENT '注册来源：1-小程序 2-APP 3-H5',
    `openid` varchar(100)  NULL DEFAULT NULL COMMENT '微信openid',
    `unionid` varchar(100) NULL DEFAULT NULL COMMENT '微信unionid',
    `login_count` int(11) NULL DEFAULT 0 COMMENT '登录次数',
    `member_level_score` decimal(10,2) NULL DEFAULT 0.00 COMMENT '会员等级分',
    `credit_score` int(11) NULL DEFAULT 0 COMMENT '信誉分数',

    `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '用户状态：0-待审核 1-启用 2-下架 5-禁用',
    `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除状态：0-未删除 1-已删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_user_code` (`user_code`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_openid` (`openid`),

    KEY `idx_deleted_status_createtime` (`deleted_flag`, `status`, `create_time`)
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;



-- 2.2 抽签主表
DROP TABLE IF EXISTS `qs_draw`;
CREATE TABLE `qs_draw`
(
    `draw_id` bigint NOT NULL AUTO_INCREMENT COMMENT '抽签ID',
    `publisher_user_id` bigint NOT NULL COMMENT '发布用户ID(id为0代表是官方抽签)',

    -- 手动设置部分
    `title` varchar(100) NOT NULL COMMENT '抽签标题',
    `draw_cover` varchar(100) DEFAULT NULL COMMENT '抽签封面',
    `description` varchar(500) DEFAULT NULL COMMENT '抽签说明',
    `has_prize` tinyint(1) NOT NULL DEFAULT 0 COMMENT '有无奖品：0-无奖品(随机选人) 1-有奖品(抽奖)',
    `drawing_way` tinyint(1) NOT NULL DEFAULT 0 COMMENT '开奖方式：0-按时间开奖 1-按人数开奖 2-',
    `join_deadline` datetime NOT NULL COMMENT '参与截止时间',
    `min_person` int default 0 comment '最少参与人数',
    `per_code_num` tinyint(1) default 5 comment '每人每次可获得多少抽签码',

    -- 自动生成部分
    `draw_no` varchar(64) NOT NULL COMMENT '唯一抽奖编号',
    `draw_time` datetime DEFAULT NULL COMMENT '实际开奖时间',
    `participant_count` int NOT NULL DEFAULT 0 COMMENT '累计参与人数',
    `code_count` int NOT NULL DEFAULT 0 COMMENT '参与码总数',

    `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：1-进行中 2-已开奖 3-流局',
    `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除状态：0-未删除 1-已删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`draw_id`),
    UNIQUE KEY `uk_draw_no` (`draw_no`),
    KEY `idx_publisher_status_deadline` (`publisher_user_id`, `status`,`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '抽签主表';


-- 2.3 奖品表
DROP TABLE IF EXISTS `qs_prize`;
CREATE TABLE `qs_prize`
(
    `prize_id` bigint NOT NULL AUTO_INCREMENT COMMENT '奖品ID',
    `draw_id` bigint NOT NULL COMMENT '关联抽签ID',
    `prize_name` varchar(100) NOT NULL COMMENT '奖品名称',
    `prize_cover` varchar(100) default NULL COMMENT '奖品封面',
    `prize_type` tinyint(1) default NULL COMMENT '1-一等奖，2-二等将，3-三等奖，最多9个',
    `amount` int default 1 COMMENT '奖品份数',
    `giveaway` tinyint(1) default NULL COMMENT '1-快递邮寄，2-联系发布者，3-中奖者填写信息，4-其他',


    PRIMARY KEY (`prize_id`),
    -- 核心业务索引：按抽签ID,prize_type查询奖品（最常用）
    UNIQUE KEY `uk_draw_prize_type` (`draw_id`,`prize_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '奖品表';


-- 2.4 抽签参与码表
DROP TABLE IF EXISTS `qs_draw_code`;
CREATE TABLE `qs_draw_code`
(
    `draw_code_id` bigint NOT NULL AUTO_INCREMENT COMMENT '参与码ID',
    `user_id` bigint NOT NULL COMMENT '关联用户',
    `draw_id` bigint NOT NULL COMMENT '关联抽签',
    `prize_id` bigint DEFAULT NULL COMMENT '关联奖品,null就是没中奖',

    `code_value` varchar(8) NOT NULL COMMENT '参与码',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`draw_code_id`),
    UNIQUE KEY `uk_code_value` (`code_value`),
    KEY `idx_select` (`user_id`,`draw_id`,`code_value`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '抽签参与码表';


-- 2.5 中奖记录表
DROP TABLE IF EXISTS `qs_winner`;
CREATE TABLE `qs_winner`
(
    `winner_id` bigint NOT NULL AUTO_INCREMENT COMMENT '中奖记录id',
    `user_id` bigint NOT NULL COMMENT '关联中奖用户',
    `draw_id` bigint NOT NULL COMMENT '关联抽签ID',
    `prize_id` bigint NOT NULL COMMENT '关联奖品',
    `winner_code_id` bigint DEFAULT NULL COMMENT '中奖参与码ID',

    -- 冗余存储，优化查询
    `user_name` varchar(50)  NOT NULL COMMENT '用户昵称',
    `avatar` varchar(500)  NULL DEFAULT NULL COMMENT '头像',

    PRIMARY KEY (`winner_id`),
    UNIQUE KEY `uk_winner_code_id` (`winner_code_id`),
    -- 查询索引：查询中奖用户
    KEY `select` (`prize_id`,`user_name`,`avatar`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '中奖记录表';







-- 2.6 抽签异步任务表
DROP TABLE IF EXISTS `qs_draw_task`;
CREATE TABLE `qs_draw_task`
(
    `task_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `draw_id` bigint NOT NULL COMMENT '抽签ID',
    `draw_participant_id` bigint DEFAULT NULL COMMENT '参与记录ID',
    `task_type` varchar(32) NOT NULL COMMENT '任务类型',
    `task_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '任务状态：0-待处理 1-处理中 2-成功 3-失败',
    `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
    `message_body` varchar(1000) DEFAULT NULL COMMENT '消息体',
    `last_error` varchar(500) DEFAULT NULL COMMENT '最后一次错误信息',
    `next_retry_time` datetime DEFAULT NULL COMMENT '下次重试时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`task_id`),
    UNIQUE KEY `uk_join_task` (`task_type`, `draw_participant_id`),
    KEY `idx_task_status_retry` (`task_status`, `next_retry_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '抽签异步任务表';
