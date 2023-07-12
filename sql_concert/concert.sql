drop table if exists `concert`;
create table `concert`
(
    `id`           bigint       not null comment '演唱会ID',
    `title`        varchar(100) not null comment '演唱会标题',
    `performer`    varchar(100) not null comment '表演者',
    `venue`        varchar(100) not null comment '演唱会场地',
    `concert_date` date         not null comment '演唱会日期',
    `start_time`   time         not null comment '开始时间',
    `end_time`     time         not null comment '结束时间',
    `ticket_left`  int          not null comment '剩余门票',
    `create_time`  datetime(3) comment '新增时间',
    `update_time`  datetime(3) comment '修改时间',
    primary key (`id`)
) engine = innodb
  default charset = utf8mb4 comment ='演唱会';

drop table if exists `order`;
drop table if exists `concert_order`;
create table `concert_order`
(
    `id`           bigint         not null comment '订单ID',
    `member_id`    bigint         not null comment '会员ID',
    `concert_id`   bigint         not null comment '演唱会ID',
    `ticket_id`    bigint         not null comment '门票ID',
    `total_price`  decimal(10, 2) not null comment '订单总价',
    `order_status` char(1)        not null comment '订单状态|枚举[OrderStatusEnum]',
    `create_time`  datetime(3) comment '新增时间',
    `update_time`  datetime(3) comment '修改时间',
    primary key (`id`),
    index `member_id_index` (`member_id`),
    index `concert_id_index` (`concert_id`),
    index `ticket_id_index` (`ticket_id`)
) engine = innodb
  default charset = utf8mb4 comment ='订单';