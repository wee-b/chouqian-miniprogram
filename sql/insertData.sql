USE `quick_start`;

-- 菜单测试数据
INSERT INTO `qs_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `menu_type`, `visible`, `perms`, `icon`, `status`)
VALUES
    (1001, '系统管理', 0, 1, '/system', 'Layout', 1, 1, 0, NULL, 'system', 1),
    (1002, '用户管理', 1001, 1, '/system/user', 'system/user/index', 1, 2, 0, 'system:user:list', 'user', 1),
    (1003, '查询用户', 1002, 1, '', '', 1, 3, 0, 'system:user:query', '#', 1),
    (1004, '新增用户', 1002, 2, '', '', 1, 3, 0, 'system:user:add', '#', 1),
    (1005, '修改用户', 1002, 3, '', '', 1, 3, 0, 'system:user:edit', '#', 1),
    (1101, '菜单管理', 1001, 2, '/system/menu', 'system/menu/index', 1, 2, 0, 'system:menu:list', 'tree', 1),
    (1102, '查询菜单', 1101, 1, '', '', 1, 3, 0, 'system:menu:query', '#', 1),
    (1103, '新增菜单', 1101, 2, '', '', 1, 3, 0, 'system:menu:add', '#', 1),
    (1104, '修改菜单', 1101, 3, '', '', 1, 3, 0, 'system:menu:edit', '#', 1),
    (1201, '角色管理', 1001, 3, '/system/role', 'system/role/index', 1, 2, 0, 'system:role:list', 'peoples', 1),
    (1202, '查询角色', 1201, 1, '', '', 1, 3, 0, 'system:role:query', '#', 1),
    (1203, '新增角色', 1201, 2, '', '', 1, 3, 0, 'system:role:add', '#', 1),
    (1204, '修改角色', 1201, 3, '', '', 1, 3, 0, 'system:role:edit', '#', 1);

-- 角色测试数据
INSERT INTO `qs_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `remark`, `status`, `deleted_flag`)
VALUES
    (1, '超级管理员', 'admin', 1, '拥有所有菜单和权限', 1, 0),
    (2, '运营人员', 'operator', 2, '只能查看用户、菜单、角色', 1, 0);

-- 用户测试数据
INSERT INTO `qs_user` (`user_id`, `user_code`, `user_type`, `user_name`, `phone`, `email`, `password`, `gender`, `register_source`, `login_count`, `member_level_score`, `credit_score`, `status`, `deleted_flag`)
VALUES
    (1, 'QS000001', 1, '超级管理员', '13800000001', 'admin@quickstart.com', '$2a$10$oHh0XO3Y2PRt8rKbG/4yne7MYDgHVU9uWTw/3R7mGbIRYM7dhavvK', 1, 3, 0, 0.00, 100, 1, 0),
    (2, 'QS000002', 1, '运营主管', '13800000002', 'operator@quickstart.com', '$2a$10$0jVJHp.m6pq0ekIlZrBUsO8WnoifkC7JxtnNkwkq83JZTU1e2B4B2', 1, 3, 0, 0.00, 90, 1, 0),
    (3, 'QS000003', 2, '普通用户', '13800000003', 'client@quickstart.com', '$2a$10$0jVJHp.m6pq0ekIlZrBUsO8WnoifkC7JxtnNkwkq83JZTU1e2B4B2', 2, 1, 0, 0.00, 80, 1, 0);

-- 角色菜单关联
INSERT INTO `qs_role_menu` (`role_menu_id`, `role_id`, `menu_id`)
VALUES
    (1, 1, 1001),
    (2, 1, 1002),
    (3, 1, 1003),
    (4, 1, 1004),
    (5, 1, 1005),
    (6, 1, 1101),
    (7, 1, 1102),
    (8, 1, 1103),
    (9, 1, 1104),
    (10, 1, 1201),
    (11, 1, 1202),
    (12, 1, 1203),
    (13, 1, 1204),
    (14, 2, 1001),
    (15, 2, 1002),
    (16, 2, 1003),
    (17, 2, 1101),
    (18, 2, 1102),
    (19, 2, 1201),
    (20, 2, 1202);

-- 用户角色关联
INSERT INTO `qs_user_role` (`user_role_id`, `user_id`, `role_id`)
VALUES
    (1, 1, 1),
    (2, 2, 2);

-- 登录测试账号
-- 超级管理员: 13800000001 / admin123
-- 运营主管:   13800000002 / 123456
-- 客户端用户: 13800000003 / 123456




INSERT INTO `qs_draw` (
    `draw_id`,
    `publisher_user_id`,
    `title`,
    `draw_cover`,
    `description`,
    `has_prize`,
    `drawing_way`,
    `join_deadline`,
    `min_person`,
    `per_code_num`,
    `part_limit`,
    `draw_no`,
    `draw_time`,
    `participant_count`,
    `code_count`,
    `server_seed`,
    `seed_hash`,
    `codes_hash`,
    `verify_algorithm`,
    `xxl_job_id`,
    `status`,
    `deleted_flag`
) VALUES
      (1001, 0, '官方周末福利抽奖', 'cover/official1.jpg', '官方每周福利活动，参与即可抽奖', 1, 0, '2030-12-31 23:59:59', 50, 5, 1, 'DRAW20251001', '2031-01-01 10:00:00', 120, 150, NULL, NULL, NULL, NULL, NULL, 1, 0),
      (1002, 0, '节日限定幸运抽签', 'cover/official2.jpg', '节假日专属官方抽奖活动', 1, 0, '2030-12-30 23:59:59', 30, 5, 1, 'DRAW20251002', '2030-12-31 10:00:00', 85, 100, NULL, NULL, NULL, NULL, NULL, 1, 0),
      (1003, 0, '新人专属欢迎抽奖', 'cover/official3.jpg', '新用户首次参与必得奖励', 1, 0, '2030-12-29 23:59:59', 100, 5, 1, 'DRAW20251003', '2030-12-30 10:00:00', 210, 250, NULL, NULL, NULL, NULL, NULL, 1, 0),
      (1004, 0, '月度幸运用户抽签', 'cover/official4.jpg', '每月抽取幸运用户发放大奖', 1, 0, '2030-12-28 23:59:59', 200, 5, 1, 'DRAW20251004', '2030-12-29 10:00:00', 340, 400, NULL, NULL, NULL, NULL, NULL, 1, 0),
      (1005, 0, '官方日常福利抽签', 'cover/official5.jpg', '每日参与，每日开奖', 1, 0, '2030-12-27 23:59:59', 20, 5, 1, 'DRAW20251005', '2030-12-28 10:00:00', 95, 120, NULL, NULL, NULL, NULL, NULL, 1, 0);

-- 奖品测试数据
INSERT INTO `qs_prize` (`prize_id`, `draw_id`, `prize_name`, `prize_cover`, `prize_type`, `amount`, `giveaway`)
VALUES
    (2001, 1001, '一等奖-蓝牙耳机', 'cover/prize-earphone.jpg', 1, 1, 2),
    (2002, 1001, '二等奖-咖啡券', 'cover/prize-coffee.jpg', 2, 3, 2),
    (2003, 1002, '一等奖-电影票', 'cover/prize-movie.jpg', 1, 2, 3),
    (2004, 1003, '新人礼包', 'cover/prize-new-user.jpg', 1, 5, 4),
    (2005, 1004, '月度大奖-机械键盘', 'cover/prize-keyboard.jpg', 1, 1, 2),
    (2006, 1005, '日常福利-奶茶券', 'cover/prize-milk-tea.jpg', 1, 10, 3);

-- 抽签参与码测试数据
INSERT INTO `qs_draw_code` (`draw_code_id`, `user_id`, `draw_id`, `prize_id`, `code_value`, `create_time`)
VALUES
    (3001, 3, 1001, 2001, 'A8kP2xQm', '2030-12-20 09:15:21'),
    (3002, 2, 1001, 2002, '9FhL7Bcd', '2030-12-20 10:26:38'),
    (3003, 1, 1001, NULL, 'Z3nT5wYu', '2030-12-20 11:42:07'),
    (3004, 3, 1002, 2003, 'M6qR8sVa', '2030-12-21 14:05:11'),
    (3005, 2, 1002, NULL, 'H2jK9pWb', '2030-12-21 15:30:49'),
    (3006, 3, 1003, 2004, 'T7vC4nXe', '2030-12-22 08:18:33'),
    (3007, 1, 1004, NULL, 'P5mD2rLa', '2030-12-23 19:50:05'),
    (3008, 2, 1005, 2006, 'Q9xB6tNc', '2030-12-24 20:10:16');

-- 中奖记录测试数据
INSERT INTO `qs_winner` (`winner_id`, `user_id`, `draw_id`, `prize_id`, `winner_code_id`, `user_name`, `avatar`)
VALUES
    (4001, 3, 1001, 2001, 3001, '普通用户', NULL),
    (4002, 2, 1001, 2002, 3002, '运营主管', NULL),
    (4003, 3, 1002, 2003, 3004, '普通用户', NULL),
    (4004, 3, 1003, 2004, 3006, '普通用户', NULL),
    (4005, 2, 1005, 2006, 3008, '运营主管', NULL);

-- 抽签异步任务测试数据
INSERT INTO `qs_draw_task` (`task_id`, `draw_id`, `draw_participant_id`, `task_type`, `task_status`, `retry_count`, `message_body`, `last_error`, `next_retry_time`)
VALUES
    (5001, 1001, 3001, 'DRAW_JOIN', 2, 0, '{"drawId":1001,"userId":3}', NULL, NULL),
    (5002, 1002, 3005, 'DRAW_JOIN', 0, 0, '{"drawId":1002,"userId":2}', NULL, '2030-12-21 15:35:00'),
    (5003, 1005, 3008, 'DRAW_NOTIFY', 3, 2, '{"drawId":1005,"winnerCodeId":3008}', 'mock notify failed', '2030-12-24 20:20:00');














-- 手动开奖测试账号与参与用户
-- 发布者测试账号: 13800138000 / 123456
INSERT INTO `qs_user` (`user_id`, `user_code`, `user_type`, `user_name`, `phone`, `email`, `password`, `avatar`, `gender`, `register_source`, `login_count`, `member_level_score`, `credit_score`, `status`, `deleted_flag`)
VALUES
    (10, 'QS000010', 2, '开奖发布者', '13800138000', 'mock10@quickstart.com', '$2a$10$0jVJHp.m6pq0ekIlZrBUsO8WnoifkC7JxtnNkwkq83JZTU1e2B4B2', '/images/default-avatar.png', 1, 1, 0, 0.00, 85, 1, 0),
    (11, 'QS000011', 2, '测试用户A', '13800138011', 'mock11@quickstart.com', '$2a$10$0jVJHp.m6pq0ekIlZrBUsO8WnoifkC7JxtnNkwkq83JZTU1e2B4B2', '/images/default-avatar.png', 2, 1, 0, 0.00, 82, 1, 0),
    (12, 'QS000012', 2, '测试用户B', '13800138012', 'mock12@quickstart.com', '$2a$10$0jVJHp.m6pq0ekIlZrBUsO8WnoifkC7JxtnNkwkq83JZTU1e2B4B2', '/images/default-avatar.png', 1, 1, 0, 0.00, 79, 1, 0),
    (13, 'QS000013', 2, '测试用户C', '13800138013', 'mock13@quickstart.com', '$2a$10$0jVJHp.m6pq0ekIlZrBUsO8WnoifkC7JxtnNkwkq83JZTU1e2B4B2', '/images/default-avatar.png', 2, 1, 0, 0.00, 76, 1, 0),
    (14, 'QS000014', 2, '测试用户D', '13800138014', 'mock14@quickstart.com', '$2a$10$0jVJHp.m6pq0ekIlZrBUsO8WnoifkC7JxtnNkwkq83JZTU1e2B4B2', '/images/default-avatar.png', 1, 1, 0, 0.00, 73, 1, 0),
    (15, 'QS000015', 2, '测试用户E', '13800138015', 'mock15@quickstart.com', '$2a$10$0jVJHp.m6pq0ekIlZrBUsO8WnoifkC7JxtnNkwkq83JZTU1e2B4B2', '/images/default-avatar.png', 2, 1, 0, 0.00, 70, 1, 0);

-- 手动开奖测试抽签：一条有奖品，一条无奖品
INSERT INTO `qs_draw` (
    `draw_id`,
    `publisher_user_id`,
    `title`,
    `draw_cover`,
    `description`,
    `has_prize`,
    `drawing_way`,
    `join_deadline`,
    `min_person`,
    `per_code_num`,
    `part_limit`,
    `draw_no`,
    `draw_time`,
    `participant_count`,
    `code_count`,
    `server_seed`,
    `seed_hash`,
    `codes_hash`,
    `verify_algorithm`,
    `xxl_job_id`,
    `status`,
    `deleted_flag`
) VALUES
    (1010, 10, '手动开奖测试-有奖品', 'cover/manual-prize.jpg', '用于测试手动开奖，有奖品配置，登录 13800138000 后可直接开奖', 1, 2, '2030-12-31 23:59:59', 1, 1, 1, 'DRAW-MANUAL-PRIZE-1010', NULL, 5, 5, NULL, NULL, NULL, NULL, NULL, 1, 0),
    (1011, 10, '手动开奖测试-无奖品', 'cover/manual-no-prize.jpg', '用于测试手动开奖，无奖品时随机抽取一名幸运用户', 0, 2, '2030-12-31 23:59:59', 1, 1, 1, 'DRAW-MANUAL-NOPRIZE-1011', NULL, 5, 5, NULL, NULL, NULL, NULL, NULL, 1, 0);

-- 手动开奖测试奖品，仅 1010 有奖品
INSERT INTO `qs_prize` (`prize_id`, `draw_id`, `prize_name`, `prize_cover`, `prize_type`, `amount`, `giveaway`)
VALUES
    (2010, 1010, '一等奖-测试耳机', 'cover/test-earphone.jpg', 1, 1, 2),
    (2011, 1010, '二等奖-测试咖啡券', 'cover/test-coffee.jpg', 2, 2, 3);

-- 手动开奖测试参与码，开奖前 prize_id 保持 NULL
INSERT INTO `qs_draw_code` (`draw_code_id`, `user_id`, `draw_id`, `prize_id`, `code_value`, `create_time`)
VALUES
    (3010, 11, 1010, NULL, 'MTA1A2B3', '2030-12-25 09:00:01'),
    (3011, 12, 1010, NULL, 'MTA2C4D5', '2030-12-25 09:05:12'),
    (3012, 13, 1010, NULL, 'MTA3E6F7', '2030-12-25 09:10:23'),
    (3013, 14, 1010, NULL, 'MTA4G8H9', '2030-12-25 09:15:34'),
    (3014, 15, 1010, NULL, 'MTA5J1K2', '2030-12-25 09:20:45'),
    (3020, 11, 1011, NULL, 'MTB1L3M4', '2030-12-26 10:00:01'),
    (3021, 12, 1011, NULL, 'MTB2N5P6', '2030-12-26 10:05:12'),
    (3022, 13, 1011, NULL, 'MTB3Q7R8', '2030-12-26 10:10:23'),
    (3023, 14, 1011, NULL, 'MTB4S9T1', '2030-12-26 10:15:34'),
    (3024, 15, 1011, NULL, 'MTB5U2V3', '2030-12-26 10:20:45');
