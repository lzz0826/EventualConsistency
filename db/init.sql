CREATE DATABASE e_order;

USE e_order;
-- order 訂單表
CREATE TABLE IF NOT EXISTS `t_order` (
  `id` BIGINT NOT NULL primary KEY auto_increment COMMENT '訂單id',
  `price` DECIMAL(10, 2) NOT NULL COMMENT '交易金額',
  `type` TINYINT(1) DEFAULT NULL COMMENT '訂單類型',
  `status` TINYINT(1) DEFAULT NULL COMMENT '定單狀態',
  `update_time` TIMESTAMP NOT NULL,
  `create_time` TIMESTAMP NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單表';

-- t_order_stoc 中間表
CREATE TABLE IF NOT EXISTS `t_order_stock` (
  `id` BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '關聯id',
  `order_id` BIGINT NOT NULL COMMENT '訂單id',
  `stock_id` BIGINT NOT NULL COMMENT '庫存id',
  `deducted_quantity` INT NOT NULL COMMENT '扣除的庫存數量',
  `status` TINYINT(1) DEFAULT NULL COMMENT '狀態',
  `create_time` TIMESTAMP NOT NULL,
  `update_time` TIMESTAMP NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單-庫存關聯表';


-- seata分布式事務紀錄表
CREATE TABLE IF NOT EXISTS `undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
    ) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';
   
ALTER TABLE `undo_log` ADD INDEX `ix_log_created` (`log_created`);



CREATE DATABASE e_stock;

USE e_stock;

-- t_stock 庫存表
CREATE TABLE IF NOT EXISTS `t_stock` (
    `id` BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '庫存id',
    `product_id` BIGINT COMMENT '商品id',
    `product_name` VARCHAR(60) COMMENT '商品名',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '商品金額',
    `type` TINYINT(1) DEFAULT NULL COMMENT '庫存類型',
    `status` TINYINT(1) DEFAULT NULL COMMENT '庫存狀態',
    `quantity` INT NOT NULL COMMENT '庫存數量',
    `update_time` TIMESTAMP NOT NULL,
    `create_time` TIMESTAMP NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='庫存表';


CREATE TABLE IF NOT EXISTS `t_stock_undo_log` (
    `id` BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '操作紀錄id',
    `stock_id` BIGINT NOT NULL COMMENT '庫存id',
    `order_id` BIGINT DEFAULT NULL COMMENT '相關訂單id，如果有',
    `operation_type` VARCHAR(20) NOT NULL COMMENT '操作類型：increase（增加庫存）、decrease（減少庫存)',
    `quantity` INT NOT NULL COMMENT '操作數量',
    `operation_time` TIMESTAMP NOT NULL COMMENT '操作時間',
    `description` TEXT COMMENT '操作描述',
    `status` INT DEFAULT 0 COMMENT '操作狀態：1-成功、-1-失敗、0-等待等',
    `rollback_status` TINYINT(1) DEFAULT 0 COMMENT '回滾狀態：0-未回滾、1-已回滾',
    `rollback_time` TIMESTAMP DEFAULT NULL COMMENT '回滾時間',
    `update_time` TIMESTAMP NOT NULL,
    `create_time` TIMESTAMP NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='庫存操作紀錄表';



-- seata分布式事務紀錄表
CREATE TABLE IF NOT EXISTS `undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
    ) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';

ALTER TABLE `undo_log` ADD INDEX `ix_log_created` (`log_created`);




