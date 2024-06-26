create database api_third_party;
-- ----------------------------
-- Table structure for alipay_info
-- ----------------------------
use huang_api_order;
DROP TABLE IF EXISTS `alipay_info`;
CREATE TABLE `alipay_info`
(
    `orderNumber`    varchar(512) NOT NULL COMMENT '订单id',
    `subject`        varchar(255) NOT NULL COMMENT '交易名称',
    `totalAmount`    float(10, 2) NOT NULL COMMENT '交易金额',
    `buyerPayAmount` float(10, 2) NOT NULL COMMENT '买家付款金额',
    `buyerId`        text         NOT NULL COMMENT '买家在支付宝的唯一id',
    `tradeNo`        text         NOT NULL COMMENT '支付宝交易凭证号',
    `tradeStatus`    varchar(255) NOT NULL COMMENT '交易状态',
    `gmtPayment`     datetime(0)  NOT NULL COMMENT '买家付款时间',
    PRIMARY KEY (`orderNumber`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = DYNAMIC;
