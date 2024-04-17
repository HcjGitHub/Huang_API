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
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
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


