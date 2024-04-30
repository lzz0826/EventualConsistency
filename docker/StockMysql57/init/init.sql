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


