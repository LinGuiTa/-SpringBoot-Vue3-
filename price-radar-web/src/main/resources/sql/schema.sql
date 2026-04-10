CREATE DATABASE IF NOT EXISTS price_radar DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE price_radar;

DROP TABLE IF EXISTS t_admin_operation_log;
DROP TABLE IF EXISTS t_system_config;
DROP TABLE IF EXISTS t_notification;
DROP TABLE IF EXISTS t_price_alert;
DROP TABLE IF EXISTS t_user_favorite;
DROP TABLE IF EXISTS t_price_statistics;
DROP TABLE IF EXISTS t_price_latest;
DROP TABLE IF EXISTS t_price_record;
DROP TABLE IF EXISTS t_product_platform;
DROP TABLE IF EXISTS t_product;
DROP TABLE IF EXISTS t_platform;
DROP TABLE IF EXISTS t_category;
DROP TABLE IF EXISTS t_user_login_log;
DROP TABLE IF EXISTS t_user;

CREATE TABLE IF NOT EXISTS t_user (
    id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username   VARCHAR(50)   NOT NULL COMMENT '用户名',
    password   VARCHAR(255)  NOT NULL COMMENT '密码(BCrypt加密)',
    email      VARCHAR(100)  DEFAULT NULL COMMENT '邮箱',
    phone      VARCHAR(20)   DEFAULT NULL COMMENT '手机号',
    avatar     VARCHAR(500)  DEFAULT NULL COMMENT '头像URL',
    role       VARCHAR(20)   NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    status     TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=正常',
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS t_user_login_log (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id    BIGINT       NOT NULL COMMENT '用户ID',
    ip         VARCHAR(64)  DEFAULT NULL COMMENT 'IP地址',
    device     VARCHAR(200) DEFAULT NULL COMMENT '设备信息',
    login_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    status     TINYINT      DEFAULT NULL COMMENT '状态：1=成功，0=失败',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志';

CREATE TABLE IF NOT EXISTS t_category (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    parent_id  BIGINT       NOT NULL DEFAULT 0 COMMENT '父分类ID，0为顶级',
    name       VARCHAR(50)  NOT NULL COMMENT '分类名称',
    level      TINYINT      NOT NULL DEFAULT 1 COMMENT '层级：1/2/3',
    icon       VARCHAR(200) DEFAULT NULL COMMENT '图标URL',
    sort_order INT          NOT NULL DEFAULT 0 COMMENT '排序',
    status     TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=正常',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类';

CREATE TABLE IF NOT EXISTS t_platform (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name       VARCHAR(50)  NOT NULL COMMENT '平台名称',
    logo_url   VARCHAR(500) DEFAULT NULL COMMENT '平台Logo URL',
    base_url   VARCHAR(200) DEFAULT NULL COMMENT '平台首页URL',
    status     TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=正常',
    sort_order INT          NOT NULL DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电商平台';

CREATE TABLE IF NOT EXISTS t_product (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    barcode     VARCHAR(50)  NOT NULL COMMENT '商品条码(EAN-13/UPC-A)',
    name        VARCHAR(200) NOT NULL COMMENT '商品名称',
    brand       VARCHAR(100) DEFAULT NULL COMMENT '品牌',
    category_id BIGINT       NOT NULL DEFAULT 0 COMMENT '分类ID',
    image_url   VARCHAR(500) DEFAULT NULL COMMENT '主图URL',
    description TEXT         DEFAULT NULL COMMENT '商品描述',
    spec        VARCHAR(500) DEFAULT NULL COMMENT '规格参数',
    weight      VARCHAR(50)  DEFAULT NULL COMMENT '重量',
    unit        VARCHAR(20)  DEFAULT NULL COMMENT '单位',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0=下架，1=正常',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_barcode (barcode),
    KEY idx_category_id (category_id),
    KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

CREATE TABLE IF NOT EXISTS t_product_platform (
    id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    product_id          BIGINT       NOT NULL COMMENT '商品ID',
    platform_id         BIGINT       NOT NULL COMMENT '平台ID',
    platform_product_id VARCHAR(100) DEFAULT NULL COMMENT '平台内商品ID',
    platform_url        VARCHAR(500) DEFAULT NULL COMMENT '平台商品URL',
    status              TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0=失效，1=正常',
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_platform (product_id, platform_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品平台映射';

CREATE TABLE IF NOT EXISTS t_price_record (
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    product_id     BIGINT        NOT NULL COMMENT '商品ID',
    platform_id    BIGINT        NOT NULL COMMENT '平台ID',
    price          DECIMAL(10,2) NOT NULL COMMENT '价格',
    original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    discount_info  VARCHAR(200)  DEFAULT NULL COMMENT '折扣信息',
    stock_status   TINYINT       DEFAULT 1 COMMENT '库存状态：0=无货，1=有货',
    url            VARCHAR(500)  DEFAULT NULL COMMENT '购买链接',
    recorded_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (id),
    KEY idx_product_platform_time (product_id, platform_id, recorded_at),
    KEY idx_product_time (product_id, recorded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='价格历史记录';

CREATE TABLE IF NOT EXISTS t_price_latest (
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    product_id     BIGINT        NOT NULL COMMENT '商品ID',
    platform_id    BIGINT        NOT NULL COMMENT '平台ID',
    price          DECIMAL(10,2) NOT NULL COMMENT '当前价格',
    original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    discount_info  VARCHAR(200)  DEFAULT NULL COMMENT '折扣信息',
    stock_status   TINYINT       DEFAULT 1 COMMENT '库存状态：0=无货，1=有货',
    url            VARCHAR(500)  DEFAULT NULL COMMENT '购买链接',
    updated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_platform (product_id, platform_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='最新价格';

CREATE TABLE IF NOT EXISTS t_price_statistics (
    id               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    product_id       BIGINT        NOT NULL COMMENT '商品ID',
    platform_id      BIGINT        NOT NULL COMMENT '平台ID',
    stat_date        DATE          NOT NULL COMMENT '统计日期',
    min_price        DECIMAL(10,2) NOT NULL COMMENT '当日最低价',
    max_price        DECIMAL(10,2) NOT NULL COMMENT '当日最高价',
    avg_price        DECIMAL(10,2) NOT NULL COMMENT '当日均价',
    price_drop_count INT           DEFAULT 0 COMMENT '降价次数',
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_platform_date (product_id, platform_id, stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日价格统计汇总';

CREATE TABLE IF NOT EXISTS t_user_favorite (
    id         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id    BIGINT   NOT NULL COMMENT '用户ID',
    product_id BIGINT   NOT NULL COMMENT '商品ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_product (user_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏';

CREATE TABLE IF NOT EXISTS t_price_alert (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id      BIGINT        NOT NULL COMMENT '用户ID',
    product_id   BIGINT        NOT NULL COMMENT '商品ID',
    platform_id  BIGINT        DEFAULT NULL COMMENT '指定平台ID，NULL=任意平台',
    target_price DECIMAL(10,2) NOT NULL COMMENT '目标价格',
    alert_type   VARCHAR(20)   NOT NULL DEFAULT 'ONCE' COMMENT '提醒类型：ONCE/ALWAYS',
    status       VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/TRIGGERED/CANCELLED',
    triggered_at DATETIME      DEFAULT NULL COMMENT '触发时间',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='降价提醒';

CREATE TABLE IF NOT EXISTS t_notification (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id    BIGINT       NOT NULL COMMENT '用户ID',
    type       VARCHAR(30)  NOT NULL COMMENT '通知类型：PRICE_DROP/SYSTEM',
    title      VARCHAR(200) NOT NULL COMMENT '通知标题',
    content    TEXT         DEFAULT NULL COMMENT '通知内容',
    product_id BIGINT       DEFAULT NULL COMMENT '关联商品ID',
    is_read    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读：0=未读，1=已读',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_read (user_id, is_read),
    KEY idx_user_time (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知消息';

CREATE TABLE IF NOT EXISTS t_admin_operation_log (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    admin_id      BIGINT       NOT NULL COMMENT '管理员ID',
    action        VARCHAR(100) NOT NULL COMMENT '操作动作',
    resource_type VARCHAR(50)  DEFAULT NULL COMMENT '资源类型',
    resource_id   BIGINT       DEFAULT NULL COMMENT '资源ID',
    detail        TEXT         DEFAULT NULL COMMENT '操作详情',
    ip            VARCHAR(64)  DEFAULT NULL COMMENT '操作IP',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_admin_id (admin_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志';

CREATE TABLE IF NOT EXISTS t_system_config (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    config_key   VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT         DEFAULT NULL COMMENT '配置值',
    description  VARCHAR(200) DEFAULT NULL COMMENT '描述',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置';
