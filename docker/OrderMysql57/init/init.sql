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

