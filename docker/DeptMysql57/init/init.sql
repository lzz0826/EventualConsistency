CREATE DATABASE cloudDB01;

USE cloudDB01;

CREATE TABLE IF NOT EXISTS `dept` (
  `deptne` BIGINT NOT NULL primary KEY AUTO_INCREMENT,
  `dname` varchar(60),
  `db_source` varchar(60)
);

INSERT INTO dept (dname, db_source) VALUES ('開發部', DATABASE());
INSERT INTO dept (dname, db_source) VALUES ('人事部', DATABASE());
INSERT INTO dept (dname, db_source) VALUES ('財務部', DATABASE());
INSERT INTO dept (dname, db_source) VALUES ('市場部', DATABASE());
INSERT INTO dept (dname, db_source) VALUES ('運維部', DATABASE());

