create database huang_api_order;
use huang_api_order;

DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order`
(
    `id`          bigint(0)      NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `userId`      bigint(0)      NULL DEFAULT NULL COMMENT '用户id',
    `interfaceId` bigint(0)      NULL DEFAULT NULL COMMENT '接口id',
    `count`       int(0)         NULL DEFAULT NULL COMMENT '购买数量',
    `totalAmount` decimal(10, 2) NULL DEFAULT NULL COMMENT '订单应付价格',
    `status`      int(0)         NULL DEFAULT 0 COMMENT '订单状态 0-未支付 1 -已支付 2-超时支付',
    `orderSn`     varchar(255)   NOT NULL COMMENT '订单号',
    `charging`    float(255, 2)  NOT NULL COMMENT '单价',
    `createTime`  datetime            default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime`  datetime            default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDelete`    int(0)         NULL DEFAULT 0 COMMENT '是否删除(0-未删, 1-已删)',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 25
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = DYNAMIC;