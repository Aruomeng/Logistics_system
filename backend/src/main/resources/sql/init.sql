-- ========================================
-- 农产品供应链管理系统 - 数据库初始化脚本
-- ========================================

CREATE DATABASE IF NOT EXISTS supply_chain DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE supply_chain;

-- ========================================
-- 1. 用户核心信息表
-- ========================================
CREATE TABLE IF NOT EXISTS `user_info` (
    `user_id` BIGINT NOT NULL COMMENT '用户唯一ID',
    `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
    `password` VARCHAR(100) NOT NULL COMMENT '登录密码(BCrypt)',
    `role_type` TINYINT NOT NULL COMMENT '角色:1=管理员,2=供应方,3=物流方,4=消费者',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号码',
    `email` VARCHAR(50) DEFAULT NULL COMMENT '邮箱地址',
    `qq` VARCHAR(20) DEFAULT NULL COMMENT 'QQ号码',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像路径',
    `gender` TINYINT DEFAULT 0 COMMENT '性别:0=未知,1=男,2=女',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0=禁用,1=启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `delete_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0=否,1=是',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户信息表';

-- ========================================
-- 2. 角色权限表（简化版，开发阶段通过 role_type 控制权限）
-- ========================================
CREATE TABLE IF NOT EXISTS `permission` (
    `perm_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `perm_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `perm_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `perm_type` TINYINT NOT NULL COMMENT '类型:1=接口,2=菜单,3=按钮',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父权限ID',
    `url` VARCHAR(255) DEFAULT NULL COMMENT '接口/菜单地址',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`perm_id`),
    UNIQUE KEY `uk_perm_code` (`perm_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限表';

CREATE TABLE IF NOT EXISTS `role_permission` (
    `rp_id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_id` TINYINT NOT NULL COMMENT '角色ID',
    `perm_id` BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (`rp_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色-权限关联表';

-- ========================================
-- 3. 公告表
-- ========================================
CREATE TABLE IF NOT EXISTS `notice` (
    `notice_id` BIGINT NOT NULL COMMENT '公告ID',
    `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content` LONGTEXT NOT NULL COMMENT '公告内容(富文本)',
    `publisher_id` BIGINT NOT NULL COMMENT '发布人ID',
    `publisher_name` VARCHAR(50) NOT NULL COMMENT '发布人名称',
    `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶:0=否,1=是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0=删除,1=正常',
    PRIMARY KEY (`notice_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告表';

CREATE TABLE IF NOT EXISTS `notice_attachment` (
    `na_id` BIGINT NOT NULL COMMENT '附件ID',
    `notice_id` BIGINT NOT NULL COMMENT '公告ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '原文件名',
    `file_path` VARCHAR(255) NOT NULL COMMENT '存储路径',
    `file_size` BIGINT NOT NULL COMMENT '文件大小(字节)',
    `file_type` VARCHAR(50) NOT NULL COMMENT '文件类型',
    `upload_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`na_id`),
    KEY `idx_notice_id` (`notice_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告附件表';

-- ========================================
-- 4. 供应信息表
-- ========================================
CREATE TABLE IF NOT EXISTS `supply_info` (
    `supply_id` BIGINT NOT NULL COMMENT '供应信息ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '产品名称',
    `category` VARCHAR(50) NOT NULL COMMENT '品类:农产品/生产物料',
    `sub_category` VARCHAR(50) NOT NULL COMMENT '子品类',
    `production_place` VARCHAR(200) NOT NULL COMMENT '生产地',
    `production_date` DATE NOT NULL COMMENT '生产/采收日期',
    `shelf_life` INT NOT NULL COMMENT '保质期(天)',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '单价(元/公斤)',
    `recommend_price` DECIMAL(10, 2) DEFAULT 0 COMMENT '推荐价格',
    `stock` DECIMAL(10, 2) NOT NULL COMMENT '库存(公斤)',
    `supplier_id` BIGINT NOT NULL COMMENT '供应方ID',
    `supplier_name` VARCHAR(50) NOT NULL COMMENT '供应方名称',
    `trace_code` VARCHAR(100) DEFAULT NULL COMMENT '溯源码',
    `trace_code_url` VARCHAR(255) DEFAULT NULL COMMENT '溯源码图片地址',
    `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核:0=待审,1=通过,2=驳回',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '上下架:0=下架,1=上架',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `delete_flag` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`supply_id`),
    UNIQUE KEY `uk_trace_code` (`trace_code`),
    KEY `idx_supplier_id` (`supplier_id`),
    KEY `idx_category` (`category`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '供应信息表';

-- ========================================
-- 5. 溯源信息表
-- ========================================
CREATE TABLE IF NOT EXISTS `trace_info` (
    `trace_id` BIGINT NOT NULL COMMENT '溯源信息ID',
    `supply_id` BIGINT NOT NULL COMMENT '供应信息ID',
    `seed_info` VARCHAR(500) DEFAULT NULL COMMENT '种子/种苗信息',
    `fertilizer_info` VARCHAR(500) DEFAULT NULL COMMENT '施肥信息',
    `pesticide_info` VARCHAR(500) DEFAULT NULL COMMENT '农药使用信息',
    `harvest_info` VARCHAR(500) DEFAULT NULL COMMENT '采收信息',
    `inspection_info` VARCHAR(500) DEFAULT NULL COMMENT '质检报告信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`trace_id`),
    KEY `idx_supply_id` (`supply_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '溯源信息表';

-- ========================================
-- 6. 订单主表
-- ========================================
CREATE TABLE IF NOT EXISTS `order_main` (
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
    `consumer_id` BIGINT NOT NULL COMMENT '消费者ID',
    `consumer_name` VARCHAR(50) NOT NULL COMMENT '消费者名称',
    `supplier_id` BIGINT NOT NULL COMMENT '供应方ID',
    `supplier_name` VARCHAR(50) NOT NULL COMMENT '供应方名称',
    `logistics_id` BIGINT DEFAULT NULL COMMENT '物流方ID',
    `logistics_name` VARCHAR(50) DEFAULT NULL COMMENT '物流方名称',
    `total_amount` DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    `product_amount` DECIMAL(10, 2) NOT NULL COMMENT '商品金额',
    `logistics_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '物流费用',
    `pay_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付:0=未付,1=已付,2=已退',
    `order_status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0=待付,1=待发货,2=待揽收,3=运输中,4=待收货,5=已完成,99=已关闭',
    `delivery_address` VARCHAR(255) NOT NULL COMMENT '收货地址',
    `delivery_no` VARCHAR(50) DEFAULT NULL COMMENT '物流运单号',
    `pay_time` DATETIME DEFAULT NULL,
    `delivery_time` DATETIME DEFAULT NULL,
    `receive_time` DATETIME DEFAULT NULL,
    `finish_time` DATETIME DEFAULT NULL,
    `close_time` DATETIME DEFAULT NULL,
    `close_reason` VARCHAR(255) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `delete_flag` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`order_id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_consumer_id` (`consumer_id`),
    KEY `idx_supplier_id` (`supplier_id`),
    KEY `idx_logistics_id` (`logistics_id`),
    KEY `idx_order_status` (`order_status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单主表';

-- ========================================
-- 7. 订单明细表
-- ========================================
CREATE TABLE IF NOT EXISTS `order_detail` (
    `detail_id` BIGINT NOT NULL COMMENT '明细ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `supply_id` BIGINT NOT NULL COMMENT '供应信息ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '产品名称',
    `unit_price` DECIMAL(10, 2) NOT NULL COMMENT '单价(元/公斤)',
    `quantity` DECIMAL(10, 2) NOT NULL COMMENT '数量(公斤)',
    `total_price` DECIMAL(10, 2) NOT NULL COMMENT '商品总价',
    `trace_code` VARCHAR(100) NOT NULL COMMENT '溯源码',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`detail_id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单明细表';

-- ========================================
-- 8. 订单轨迹表
-- ========================================
CREATE TABLE IF NOT EXISTS `order_track` (
    `track_id` BIGINT NOT NULL COMMENT '轨迹ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_status` TINYINT NOT NULL COMMENT '变更后状态',
    `change_content` VARCHAR(255) NOT NULL COMMENT '变更内容',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(50) NOT NULL COMMENT '操作人名称',
    `operator_role` TINYINT NOT NULL COMMENT '操作人角色',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`track_id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单轨迹表';

-- ========================================
-- 9. 操作日志表
-- ========================================
CREATE TABLE IF NOT EXISTS `operation_log` (
    `log_id` BIGINT NOT NULL COMMENT '日志ID',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人名称',
    `operator_role` TINYINT DEFAULT NULL COMMENT '操作人角色',
    `operation_type` VARCHAR(20) DEFAULT NULL COMMENT '操作类型',
    `module` VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
    `request_url` VARCHAR(255) DEFAULT NULL COMMENT '请求路径',
    `request_params` TEXT DEFAULT NULL COMMENT '请求参数',
    `response_result` TEXT DEFAULT NULL COMMENT '响应结果',
    `operator_ip` VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
    `cost_time` BIGINT DEFAULT NULL COMMENT '耗时(ms)',
    `status` TINYINT DEFAULT 1 COMMENT '状态:0=失败,1=成功',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`log_id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志表';

-- ========================================
-- 10. 库存分析表
-- ========================================
CREATE TABLE IF NOT EXISTS `inventory_analysis` (
    `ia_id` BIGINT NOT NULL AUTO_INCREMENT,
    `analysis_date` DATE NOT NULL COMMENT '统计日期',
    `product_id` BIGINT NOT NULL,
    `product_name` VARCHAR(100) NOT NULL,
    `category` VARCHAR(50) NOT NULL,
    `production_place` VARCHAR(200) NOT NULL,
    `current_inventory` DECIMAL(10, 2) NOT NULL,
    `max_inventory` DECIMAL(10, 2) NOT NULL,
    `min_inventory` DECIMAL(10, 2) NOT NULL,
    `inventory_turnover` DECIMAL(5, 2) NOT NULL DEFAULT 0,
    `safe_inventory` DECIMAL(10, 2) NOT NULL DEFAULT 0,
    `warning_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=正常,1=低库存,2=超库存',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`ia_id`),
    KEY `idx_analysis_date` (`analysis_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '库存分析表';

-- ========================================
-- 11. 供应计划分析表
-- ========================================
CREATE TABLE IF NOT EXISTS `supply_plan_analysis` (
    `spa_id` BIGINT NOT NULL AUTO_INCREMENT,
    `analysis_month` VARCHAR(7) NOT NULL COMMENT '统计月份yyyy-MM',
    `supplier_id` BIGINT NOT NULL,
    `supplier_name` VARCHAR(50) NOT NULL,
    `category` VARCHAR(50) NOT NULL,
    `plan_quantity` DECIMAL(10, 2) NOT NULL,
    `actual_quantity` DECIMAL(10, 2) NOT NULL,
    `completion_rate` DECIMAL(5, 2) NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`spa_id`),
    KEY `idx_analysis_month` (`analysis_month`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '供应计划分析表';

-- ========================================
-- 初始数据：管理员账号
-- 密码: admin123 (BCrypt 加密)
-- ========================================
INSERT INTO
    `user_info` (
        `user_id`,
        `username`,
        `password`,
        `role_type`,
        `status`,
        `gender`
    )
VALUES (
        1,
        'admin',
        '$2a$10$aLXdGT/O3D.2qH6SZP.tx.r8nyO8Eg8FF7Jjg7zCwTjoPtjfwbMlO',
        1,
        1,
        0
    );

-- 插入测试用户
INSERT INTO
    `user_info` (
        `user_id`,
        `username`,
        `password`,
        `role_type`,
        `status`,
        `gender`
    )
VALUES (
        2,
        'supplier01',
        '$2a$10$aLXdGT/O3D.2qH6SZP.tx.r8nyO8Eg8FF7Jjg7zCwTjoPtjfwbMlO',
        2,
        1,
        1
    ),
    (
        3,
        'logistics01',
        '$2a$10$aLXdGT/O3D.2qH6SZP.tx.r8nyO8Eg8FF7Jjg7zCwTjoPtjfwbMlO',
        3,
        1,
        1
    ),
    (
        4,
        'consumer01',
        '$2a$10$aLXdGT/O3D.2qH6SZP.tx.r8nyO8Eg8FF7Jjg7zCwTjoPtjfwbMlO',
        4,
        1,
        2
    );