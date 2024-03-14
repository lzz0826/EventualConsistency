CREATE DATABASE e_stock;

USE e_stock;

CREATE TABLE IF NOT EXISTS `t_stock` (
                                         `id` BIGINT NOT NULL primary KEY auto_increment COMMENT '庫存id',
                                         `product_id` BIGINT COMMENT '商品id',
                                         `product_name` varchar(60) COMMENT '商品名',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '商品金額',
    `type` TINYINT(1) DEFAULT NULL COMMENT '庫存類型',
    `status` TINYINT(1) DEFAULT NULL COMMENT '庫存狀態',
    `update_time` TIMESTAMP NOT NULL,
    `create_time` TIMESTAMP NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='庫存表';


CREATE DATABASE e_order;

USE e_order;

CREATE TABLE IF NOT EXISTS `t_order` (
                                         `id` BIGINT NOT NULL primary KEY auto_increment COMMENT '訂單id',
                                         `stock_id` BIGINT COMMENT '庫存id',
                                         `price` DECIMAL(10, 2) NOT NULL COMMENT '交易金額',
    `type` TINYINT(1) DEFAULT NULL COMMENT '訂單類型',
    `status` TINYINT(1) DEFAULT NULL COMMENT '定單狀態',
    `update_time` TIMESTAMP NOT NULL,
    `create_time` TIMESTAMP NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單表';