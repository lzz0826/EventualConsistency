-- 選擇要使用的數據庫
USE e_stock;

CREATE TABLE IF NOT EXISTS `t_order_stock` (
                                               `id` BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '關聯id',
                                               `order_id` BIGINT NOT NULL COMMENT '訂單id',
                                               `stock_id` BIGINT NOT NULL COMMENT '庫存id',
                                               `deducted_quantity` INT NOT NULL COMMENT '扣除的庫存數量',
                                               `status` TINYINT(1) DEFAULT NULL COMMENT '狀態',
    `create_time` TIMESTAMP NOT NULL,
    `update_time` TIMESTAMP NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單-庫存關聯表';


INSERT INTO e_stock.t_stock
(id, product_id, product_name, price, `type`, status, quantity, update_time, create_time)
VALUES(1, 123, '寶可夢', 100.00, 1, 1, 100, '2024-03-12 13:24:10', '2024-03-12 13:24:10');
INSERT INTO e_stock.t_stock
(id, product_id, product_name, price, `type`, status, quantity, update_time, create_time)
VALUES(2, 1, '數碼暴龍', 234.12, 1, 1, 100, '2024-03-14 23:30:17', '2024-03-14 23:30:17');



-- Seata 分布式事務所需要的紀錄表
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