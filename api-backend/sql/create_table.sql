# 数据库初始化
# @author <a href="https://github.com/liyupi">程序员鱼皮</a>
# @from <a href="https://yupi.icu">编程导航知识星球</a>

-- 创建库
create database if not exists huang_api;

-- 切换库
use huang_api;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           null comment '账号',
    userPassword varchar(512)                           null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    `email`      varchar(256)                           NULL COMMENT '邮箱',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    `accessKey`  varchar(512) CHARACTER SET utf8        NOT NULL COMMENT 'ak',
    `secretKey`  varchar(512) CHARACTER SET utf8        NOT NULL COMMENT 'sk',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
) comment '用户' collate = utf8mb4_unicode_ci;

INSERT INTO `user`
VALUES (1, 'laohuang', 'b0dd3697a192885d7c055db46155b26a', NULL, NULL, 'laohuang', NULL, NULL, 'admin',
        '8817b9099f85326da4e633054ff19d93', '7132425bf5aac84e7174342df5dae754', '2023-07-05 20:04:29',
        '2023-08-02 16:29:09', 1);
INSERT INTO `user`
VALUES (2, 'huang', 'b0dd3697a192885d7c055db46155b26a', NULL, NULL, 'huang', NULL, NULL, 'user',
        '163c5da70c75caf832d3109e0753db93', 'f15a031ee34532019827b0283d725e1b', '2023-07-08 19:56:04',
        '2023-08-02 15:20:00', 1);

-- 接口信息
create table if not exists huang_api.`interface_info`
(
    `id`               bigint                             not null auto_increment comment '主键' primary key,
    `name`             varchar(256)                       not null comment '接口名字',
    `description`      varchar(256)                       null comment '描述',
    `url`              varchar(512)                       not null comment '接口地址',
    `requestHeader`    text                               null comment '请求头',
    `responseHeader`   text                               null comment '响应头',
    `userId`           bigint                             null comment '创建者',
    `status`           int      default 0                 not null comment '接口状态（0 - 关闭， 1 - 开启））',
    `requestParams`    text                               NULL COMMENT '请求参数',
    `method`           varchar(256)                       not null comment '请求类型',
    `sdk`              varchar(255)                       NULL DEFAULT NULL COMMENT '接口对应的SDK类路径',
    `parameterExample` varchar(255)                       NULL DEFAULT NULL COMMENT '参数示例',
    `createTime`       datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime`       datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDelete`         tinyint  default 0                 not null comment '是否删除(0-未删, 1-已删)'
) comment '接口信息' collate = utf8mb4_unicode_ci;

/*接口调用信息*/
DROP TABLE IF EXISTS huang_api.`user_interface_info`;
CREATE TABLE huang_api.`user_interface_info`
(
    `id`              bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `userId`          bigint(0) NULL DEFAULT NULL COMMENT '用户id',
    `interfaceInfoId` bigint(0) NULL DEFAULT NULL COMMENT '调用接口id',
    `totalNum`        int(0)    NULL DEFAULT 0 COMMENT '接口的总调用次数',
    `leftNum`         int(0)    NULL DEFAULT NULL COMMENT '接口剩余调用次数',
    `status`          int(0)    NULL DEFAULT 1 COMMENT '0 禁止调用 1 允许调用',
    `createTime`      datetime       default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime`      datetime       default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDelete`        int(0)    NULL DEFAULT 0 COMMENT '是否删除(0-未删, 1-已删)',
#     `version`         int(0)      NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`) USING BTREE
) comment '接口信息' collate = utf8mb4_unicode_ci;

use huang_api;
DROP TABLE IF EXISTS huang_api.`interface_charging`;
CREATE TABLE `interface_charging`
(
    `id`              bigint(0)                          NOT NULL AUTO_INCREMENT COMMENT '主键',
    `interfaceId`     bigint(0)                          NOT NULL COMMENT '接口id',
    `charging`        float(255, 2)                      NOT NULL COMMENT '计费规则（元/条）',
    `availablePieces` varchar(255)                       NOT NULL COMMENT '接口剩余可调用次数',
    `userId`          bigint(0)                          NOT NULL COMMENT '创建人',
    `createTime`      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime`      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDelete`        int(0)                             NULL DEFAULT 0 COMMENT '是否删除(0-未删, 1-已删)',
    PRIMARY KEY (`id`) USING BTREE
) comment '接口单价信息' ENGINE = InnoDB
                   AUTO_INCREMENT = 3
                   CHARACTER SET = utf8
                   COLLATE = utf8_general_ci
                   ROW_FORMAT = DYNAMIC;

