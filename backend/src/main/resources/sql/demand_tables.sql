-- =============================================
-- 需求对接子系统 - 建表 SQL
-- =============================================

-- 1. 需求主表
CREATE TABLE IF NOT EXISTS `demand_main` (
    `demand_id` BIGINT NOT NULL COMMENT '需求唯一ID',
    `demand_no` VARCHAR(32) NOT NULL COMMENT '需求编号',
    `title` VARCHAR(200) NOT NULL COMMENT '需求标题',
    `description` LONGTEXT NOT NULL COMMENT '需求详细描述',
    `category` VARCHAR(50) NOT NULL COMMENT '需求产品品类',
    `sub_category` VARCHAR(50) DEFAULT NULL COMMENT '子品类',
    `quantity` DECIMAL(10, 2) NOT NULL COMMENT '采购数量(kg)',
    `expect_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '期望单价(元/kg)',
    `expect_time` DATE NOT NULL COMMENT '期望交付时间',
    `address` VARCHAR(255) NOT NULL COMMENT '交付地址',
    `qualification_require` VARCHAR(500) DEFAULT NULL COMMENT '资质要求',
    `publisher_id` BIGINT NOT NULL COMMENT '发布方用户ID',
    `publisher_name` VARCHAR(50) NOT NULL COMMENT '发布方名称',
    `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态: 0待审核 1通过 2驳回',
    `demand_status` TINYINT NOT NULL DEFAULT 0 COMMENT '需求状态: 0待发布 1已发布 2承接中 3已对接 4已完成 99已关闭',
    `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶: 0否 1是',
    `audit_remark` VARCHAR(255) DEFAULT NULL COMMENT '审核备注',
    `close_reason` VARCHAR(255) DEFAULT NULL COMMENT '关闭原因',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `delete_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`demand_id`),
    UNIQUE KEY `uk_demand_no` (`demand_no`),
    KEY `idx_publisher_id` (`publisher_id`),
    KEY `idx_category` (`category`),
    KEY `idx_audit_status` (`audit_status`),
    KEY `idx_demand_status` (`demand_status`),
    KEY `idx_expect_time` (`expect_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '需求主表';

-- 2. 需求承接表
CREATE TABLE IF NOT EXISTS `demand_accept` (
    `accept_id` BIGINT NOT NULL COMMENT '承接申请ID',
    `demand_id` BIGINT NOT NULL COMMENT '关联需求ID',
    `accept_supplier_id` BIGINT NOT NULL COMMENT '承接供应方ID',
    `supplier_name` VARCHAR(50) NOT NULL COMMENT '供应方名称',
    `accept_scheme` LONGTEXT NOT NULL COMMENT '承接方案',
    `offer` DECIMAL(10, 2) NOT NULL COMMENT '报价(元/kg)',
    `delivery_time` DATE NOT NULL COMMENT '承诺交付时间',
    `qualification_info` VARCHAR(500) DEFAULT NULL COMMENT '资质说明',
    `accept_status` TINYINT NOT NULL DEFAULT 0 COMMENT '承接状态: 0待审核 1已确认 2已驳回',
    `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `delete_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`accept_id`),
    KEY `idx_demand_id` (`demand_id`),
    KEY `idx_accept_supplier_id` (`accept_supplier_id`),
    KEY `idx_accept_status` (`accept_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '需求承接表';