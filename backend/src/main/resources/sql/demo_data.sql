USE supply_chain;

-- ==========================================
-- 1. 更多用户（供应方 / 物流方 / 消费者）
-- ==========================================
INSERT IGNORE INTO
    user_info (
        user_id,
        username,
        password,
        role_type,
        status,
        delete_flag,
        create_time,
        update_time
    )
VALUES (
        101,
        'supplier02',
        '$2a$10$aLXdGT/O3D.2qH6SZP.tx.r8nyO8Eg8FF7Jjg7zCwTjoPtjfwbMlO',
        2,
        1,
        0,
        NOW(),
        NOW()
    ),
    (
        102,
        'supplier03',
        '$2a$10$aLXdGT/O3D.2qH6SZP.tx.r8nyO8Eg8FF7Jjg7zCwTjoPtjfwbMlO',
        2,
        1,
        0,
        NOW(),
        NOW()
    ),
    (
        103,
        'supplier04',
        '$2a$10$aLXdGT/O3D.2qH6SZP.tx.r8nyO8Eg8FF7Jjg7zCwTjoPtjfwbMlO',
        2,
        1,
        0,
        NOW(),
        NOW()
    ),
    (
        201,
        'logistics02',
        '$2a$10$aLXdGT/O3D.2qH6SZP.tx.r8nyO8Eg8FF7Jjg7zCwTjoPtjfwbMlO',
        3,
        1,
        0,
        NOW(),
        NOW()
    ),
    (
        202,
        'logistics03',
        '$2a$10$aLXdGT/O3D.2qH6SZP.tx.r8nyO8Eg8FF7Jjg7zCwTjoPtjfwbMlO',
        3,
        1,
        0,
        NOW(),
        NOW()
    ),
    (
        301,
        'consumer02',
        '$2a$10$aLXdGT/O3D.2qH6SZP.tx.r8nyO8Eg8FF7Jjg7zCwTjoPtjfwbMlO',
        4,
        1,
        0,
        NOW(),
        NOW()
    ),
    (
        302,
        'consumer03',
        '$2a$10$aLXdGT/O3D.2qH6SZP.tx.r8nyO8Eg8FF7Jjg7zCwTjoPtjfwbMlO',
        4,
        1,
        0,
        NOW(),
        NOW()
    ),
    (
        303,
        'consumer04',
        '$2a$10$aLXdGT/O3D.2qH6SZP.tx.r8nyO8Eg8FF7Jjg7zCwTjoPtjfwbMlO',
        4,
        1,
        0,
        NOW(),
        NOW()
    );

-- ==========================================
-- 2. 供应信息（与真实表字段完全对应）
-- supply_info 字段: supply_id,product_name,category,sub_category,
--   production_place,production_date,shelf_life,price,recommend_price,
--   stock,supplier_id,supplier_name,trace_code,audit_status,status,delete_flag
-- ==========================================
INSERT IGNORE INTO
    supply_info (
        supply_id,
        product_name,
        category,
        sub_category,
        production_place,
        production_date,
        shelf_life,
        price,
        recommend_price,
        stock,
        supplier_id,
        supplier_name,
        trace_code,
        audit_status,
        status,
        delete_flag
    )
VALUES (
        2001,
        '山东红富士苹果',
        '农产品',
        '水果',
        '山东烟台',
        DATE_SUB(CURDATE(), INTERVAL 5 DAY),
        30,
        5.80,
        5.80,
        5000,
        2,
        'supplier01',
        'TRC_2001',
        1,
        1,
        0
    ),
    (
        2002,
        '赣南脐橙',
        '农产品',
        '水果',
        '江西赣州',
        DATE_SUB(CURDATE(), INTERVAL 3 DAY),
        20,
        8.50,
        8.50,
        200,
        2,
        'supplier01',
        'TRC_2002',
        1,
        1,
        0
    ),
    (
        2003,
        '东北大米（五常）',
        '农产品',
        '粮食',
        '黑龙江五常',
        DATE_SUB(CURDATE(), INTERVAL 10 DAY),
        180,
        7.20,
        7.20,
        8000,
        101,
        'supplier02',
        'TRC_2003',
        1,
        1,
        0
    ),
    (
        2004,
        '黄心土豆',
        '农产品',
        '蔬菜',
        '内蒙古乌兰察布',
        DATE_SUB(CURDATE(), INTERVAL 7 DAY),
        90,
        1.50,
        1.50,
        15000,
        101,
        'supplier02',
        'TRC_2004',
        1,
        1,
        0
    ),
    (
        2005,
        '走地老母鸡',
        '农产品',
        '肉禽',
        '安徽宣城',
        DATE_SUB(CURDATE(), INTERVAL 2 DAY),
        5,
        35.00,
        35.00,
        50,
        102,
        'supplier03',
        'TRC_2005',
        1,
        1,
        0
    ),
    (
        2006,
        '新鲜黑猪肉',
        '农产品',
        '肉禽',
        '浙江金华',
        DATE_SUB(CURDATE(), INTERVAL 1 DAY),
        3,
        42.00,
        42.00,
        80,
        102,
        'supplier03',
        'TRC_2006',
        1,
        1,
        0
    ),
    (
        2007,
        '大棚有机草莓',
        '农产品',
        '水果',
        '辽宁丹东',
        DATE_SUB(CURDATE(), INTERVAL 1 DAY),
        5,
        25.00,
        25.00,
        150,
        103,
        'supplier04',
        'TRC_2007',
        1,
        1,
        0
    ),
    (
        2008,
        '高山翠水白菜',
        '农产品',
        '蔬菜',
        '云南大理',
        DATE_SUB(CURDATE(), INTERVAL 2 DAY),
        7,
        2.10,
        2.10,
        3000,
        103,
        'supplier04',
        'TRC_2008',
        1,
        1,
        0
    ),
    (
        2009,
        '阳澄湖大闸蟹',
        '农产品',
        '水产',
        '江苏苏州',
        DATE_SUB(CURDATE(), INTERVAL 2 DAY),
        3,
        88.00,
        88.00,
        300,
        2,
        'supplier01',
        'TRC_2009',
        1,
        1,
        0
    ),
    (
        2010,
        '野生小黄鱼',
        '农产品',
        '水产',
        '浙江舟山',
        DATE_SUB(CURDATE(), INTERVAL 5 DAY),
        180,
        45.00,
        45.00,
        500,
        101,
        'supplier02',
        'TRC_2010',
        1,
        1,
        0
    );

-- ==========================================
-- 3. 公告（notice 表字段: notice_id,title,content,publisher_id,publisher_name,
--          is_top,status, create_time,update_time; 无 type/delete_flag）
-- ==========================================
INSERT IGNORE INTO
    notice (
        notice_id,
        title,
        content,
        publisher_id,
        publisher_name,
        is_top,
        status,
        create_time,
        update_time
    )
VALUES (
        3001,
        '关于系统升级的通知',
        '系统将于本周末凌晨2点进行维护升级，请各位用户提前保存数据。',
        1,
        'admin',
        1,
        1,
        DATE_SUB(NOW(), INTERVAL 1 DAY),
        NOW()
    ),
    (
        3002,
        '新一季农产品上架指导',
        '秋收季节已至，请各位供应商按照最新标准更新产品信息和溯源质检报告。',
        1,
        'admin',
        0,
        1,
        DATE_SUB(NOW(), INTERVAL 2 DAY),
        NOW()
    ),
    (
        3003,
        '防范虚假交易的风险提示',
        '近期发现部分账户存在异动，请大家警惕虚假订单，保护自身财产安全。',
        1,
        'admin',
        1,
        1,
        DATE_SUB(NOW(), INTERVAL 5 DAY),
        NOW()
    );

-- ==========================================
-- 4. 订单主表（order_main 无 trace_code 字段）
-- ==========================================
INSERT IGNORE INTO
    order_main (
        order_id,
        order_no,
        consumer_id,
        consumer_name,
        supplier_id,
        supplier_name,
        logistics_id,
        logistics_name,
        total_amount,
        product_amount,
        order_status,
        delivery_address,
        delete_flag,
        create_time,
        update_time
    )
VALUES
    -- 已完成 (6天前)
    (
        4001,
        'ORD20231010001',
        4,
        'consumer01',
        2,
        'supplier01',
        3,
        'logistics01',
        580.00,
        580.00,
        5,
        '北京市朝阳区高碑店',
        0,
        DATE_SUB(NOW(), INTERVAL 6 DAY),
        NOW()
    ),
    -- 已完成 (4天前)
    (
        4002,
        'ORD20231012002',
        301,
        'consumer02',
        101,
        'supplier02',
        201,
        'logistics02',
        3600.00,
        3600.00,
        5,
        '上海市浦东新区张江',
        0,
        DATE_SUB(NOW(), INTERVAL 4 DAY),
        NOW()
    ),
    -- 供应方已确认，待分配物流 (2天前)
    (
        4003,
        'ORD20231014003',
        302,
        'consumer03',
        102,
        'supplier03',
        NULL,
        NULL,
        700.00,
        700.00,
        1,
        '广东省广州市天河区',
        0,
        DATE_SUB(NOW(), INTERVAL 2 DAY),
        NOW()
    ),
    -- 配送中 (昨天)
    (
        4004,
        'ORD20231016004',
        303,
        'consumer04',
        103,
        'supplier04',
        202,
        'logistics03',
        250.00,
        250.00,
        3,
        '四川省成都市武侯区',
        0,
        DATE_SUB(NOW(), INTERVAL 1 DAY),
        NOW()
    ),
    -- 待供应方确认 (今天)
    (
        4005,
        'ORD20231017005',
        4,
        'consumer01',
        2,
        'supplier01',
        NULL,
        NULL,
        1760.00,
        1760.00,
        0,
        '北京市朝阳区高碑店',
        0,
        NOW(),
        NOW()
    ),
    -- 今天新下单
    (
        4006,
        'ORD20231017006',
        301,
        'consumer02',
        103,
        'supplier04',
        NULL,
        NULL,
        420.00,
        420.00,
        0,
        '湖北省武汉市洪山区',
        0,
        NOW(),
        NOW()
    );

-- ==========================================
-- 5. 订单明细（order_detail 有 trace_code NOT NULL，无 update_time）
-- ==========================================
INSERT IGNORE INTO
    order_detail (
        detail_id,
        order_id,
        supply_id,
        product_name,
        unit_price,
        quantity,
        total_price,
        trace_code,
        create_time
    )
VALUES (
        40011,
        4001,
        2001,
        '山东红富士苹果',
        5.80,
        100,
        580.00,
        'TRC_2001',
        DATE_SUB(NOW(), INTERVAL 6 DAY)
    ),
    (
        40021,
        4002,
        2003,
        '东北大米（五常）',
        7.20,
        500,
        3600.00,
        'TRC_2003',
        DATE_SUB(NOW(), INTERVAL 4 DAY)
    ),
    (
        40031,
        4003,
        2005,
        '走地老母鸡',
        35.00,
        20,
        700.00,
        'TRC_2005',
        DATE_SUB(NOW(), INTERVAL 2 DAY)
    ),
    (
        40041,
        4004,
        2007,
        '大棚有机草莓',
        25.00,
        10,
        250.00,
        'TRC_2007',
        DATE_SUB(NOW(), INTERVAL 1 DAY)
    ),
    (
        40051,
        4005,
        2009,
        '阳澄湖大闸蟹',
        88.00,
        20,
        1760.00,
        'TRC_2009',
        NOW()
    ),
    (
        40061,
        4006,
        2007,
        '大棚有机草莓',
        25.00,
        16,
        400.00,
        'TRC_2007',
        NOW()
    ),
    (
        40062,
        4006,
        2008,
        '高山翠水白菜',
        2.10,
        10,
        21.00,
        'TRC_2008',
        NOW()
    );

-- ==========================================
-- 6. 物流轨迹（order_track 无 update_time）
-- ==========================================
INSERT IGNORE INTO
    order_track (
        track_id,
        order_id,
        order_status,
        change_content,
        operator_id,
        operator_name,
        operator_role,
        create_time
    )
VALUES (
        5001,
        4001,
        0,
        '消费者 consumer01 创建了订单',
        4,
        'consumer01',
        4,
        DATE_SUB(NOW(), INTERVAL 6 DAY)
    ),
    (
        5002,
        4001,
        1,
        '供应方 supplier01 确认了订单，等待分配物流',
        2,
        'supplier01',
        2,
        DATE_SUB(NOW(), INTERVAL 5 DAY)
    ),
    (
        5003,
        4001,
        2,
        '管理员 admin 分配了物流：logistics01',
        1,
        'admin',
        1,
        DATE_SUB(NOW(), INTERVAL 5 DAY)
    ),
    (
        5004,
        4001,
        3,
        '包裹已从产地烟台装车发出',
        3,
        'logistics01',
        3,
        DATE_SUB(NOW(), INTERVAL 4 DAY)
    ),
    (
        5005,
        4001,
        4,
        '包裹已抵达北京分拨中心，正在派件',
        3,
        'logistics01',
        3,
        DATE_SUB(NOW(), INTERVAL 3 DAY)
    ),
    (
        5006,
        4001,
        5,
        '消费者 consumer01 确认收货，订单完成',
        4,
        'consumer01',
        4,
        DATE_SUB(NOW(), INTERVAL 2 DAY)
    ),
    (
        5011,
        4002,
        0,
        '消费者 consumer02 创建了订单',
        301,
        'consumer02',
        4,
        DATE_SUB(NOW(), INTERVAL 4 DAY)
    ),
    (
        5012,
        4002,
        1,
        '供应方 supplier02 确认了订单',
        101,
        'supplier02',
        2,
        DATE_SUB(NOW(), INTERVAL 3 DAY)
    ),
    (
        5013,
        4002,
        2,
        '管理员 admin 分配物流：logistics02',
        1,
        'admin',
        1,
        DATE_SUB(NOW(), INTERVAL 3 DAY)
    ),
    (
        5014,
        4002,
        3,
        '物流商 logistics02 已揽收，发往上海',
        201,
        'logistics02',
        3,
        DATE_SUB(NOW(), INTERVAL 2 DAY)
    ),
    (
        5015,
        4002,
        4,
        '包裹已到达上海浦东配送站',
        201,
        'logistics02',
        3,
        DATE_SUB(NOW(), INTERVAL 1 DAY)
    ),
    (
        5016,
        4002,
        5,
        'consumer02 确认收货',
        301,
        'consumer02',
        4,
        DATE_SUB(NOW(), INTERVAL 1 DAY)
    ),
    (
        5021,
        4004,
        0,
        '消费者 consumer04 创建了订单',
        303,
        'consumer04',
        4,
        DATE_SUB(NOW(), INTERVAL 2 DAY)
    ),
    (
        5022,
        4004,
        1,
        '供应方 supplier04 确认了订单',
        103,
        'supplier04',
        2,
        DATE_SUB(NOW(), INTERVAL 1 DAY)
    ),
    (
        5023,
        4004,
        2,
        '管理员 admin 分配物流：logistics03',
        1,
        'admin',
        1,
        DATE_SUB(NOW(), INTERVAL 1 DAY)
    ),
    (
        5024,
        4004,
        3,
        '物流商 logistics03 已揽收包裹，发往成都',
        202,
        'logistics03',
        3,
        NOW()
    );

-- ==========================================
-- 7. 操作日志
-- ==========================================
INSERT IGNORE INTO
    operation_log (
        log_id,
        operator_id,
        operator_name,
        operator_role,
        operation_type,
        module,
        description,
        method,
        request_url,
        request_params,
        response_result,
        operator_ip,
        cost_time,
        status,
        create_time
    )
VALUES (
        6001,
        1,
        'admin',
        1,
        'LOGIN',
        'auth',
        '用户登录',
        'UserService.login',
        '/api/v1/user/login',
        '{}',
        '成功',
        '127.0.0.1',
        45,
        1,
        DATE_SUB(NOW(), INTERVAL 3 DAY)
    ),
    (
        6002,
        2,
        'supplier01',
        2,
        'UPDATE',
        'supply',
        '更新供应信息',
        'SupplyService.update',
        '/api/v1/supply/update',
        '{"price":5.80}',
        '成功',
        '192.168.1.100',
        12,
        1,
        DATE_SUB(NOW(), INTERVAL 2 DAY)
    ),
    (
        6003,
        3,
        'logistics01',
        3,
        'UPDATE',
        'order',
        '更新物流轨迹',
        'OrderService.addTrack',
        '/api/v1/order/logistics/track',
        '{"orderId":4001}',
        '成功',
        '10.0.0.15',
        30,
        1,
        DATE_SUB(NOW(), INTERVAL 1 DAY)
    ),
    (
        6004,
        4,
        'consumer01',
        4,
        'CREATE',
        'order',
        '创建新订单',
        'OrderService.createOrder',
        '/api/v1/order/create',
        '{"supplyId":2009}',
        '成功',
        '114.254.12.8',
        85,
        1,
        NOW()
    );