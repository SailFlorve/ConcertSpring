drop table if exists `member`;
create table `member`
(
    `id`     bigint not null comment 'id',
    `mobile` varchar(11) comment '手机号',
    primary key (`id`),
    unique key `mobile_unique` (`mobile`)
) engine = innodb
  default charset = utf8mb4 comment ='会员';

drop table if exists `audience`;
create table `audience`
(
    `id`          bigint      not null comment 'id',
    `member_id`   bigint      not null comment '会员id',
    `name`        varchar(20) not null comment '姓名',
    `id_card`     varchar(18) not null comment '身份证',
    `create_time` datetime(3) comment '新增时间',
    `update_time` datetime(3) comment '修改时间',
    primary key (`id`),
    index `member_id_index` (`member_id`)
) engine = innodb
  default charset = utf8mb4 comment ='听众';

drop table if exists `concert_ticket`;
create table `concert_ticket`
(
    `id`            bigint      not null comment 'id',
    `member_id`     bigint      not null comment '会员id',
    `audience_id`   bigint      not null comment '听众id',
    `audience_name` varchar(20) comment '听众姓名',
    `concert_id`    bigint      not null comment '演唱会ID',
    `concert_name`  varchar(30) not null comment '演唱会名称',
    `seat_number`   varchar(20) not null comment '座位号',
    `create_time`   datetime(3) comment '新增时间',
    `update_time`   datetime(3) comment '修改时间',
    primary key (`id`),
    index `member_id_index` (`member_id`)
) engine = innodb
  default charset = utf8mb4 comment ='演唱会门票';