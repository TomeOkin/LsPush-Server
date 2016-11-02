drop table if exists pin;
drop table if exists collection;
drop table if exists link;
drop table if exists user;

create table user (
    uid varchar(24), -- ([a-zA-Z0-9]){3,}
    nickname varchar(36),
    email varchar(48) unique, -- 邮箱可以为空，但不能重复绑定
    phone varchar(24), -- 允许多个账号绑定到相同的手机号
    region varchar(2), -- 地区码都为两个字符
    password varchar(24), -- ([a-zA-Z0-9\\.,;]){6,}
    validate tinyint, -- 00：未验证，01：phone 已验证，02：email 已验证，03：phone 和 email 都已验证
    image varchar(128), -- file unique code in service
    primary key(`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table link (
	  id bigint auto_increment not null,
	  url text(1024) not null, -- 由于超过 767 字节，采用新的字段保证唯一性
    url_unique varchar(48) not null unique,
    title varchar(200),
    primary key(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table collection (
	  id bigint auto_increment not null,
    link_id bigint not null,
    user_id varchar(24) not null,
    description varchar(512),
    image text(1024),
    create_date timestamp default CURRENT_TIMESTAMP,
    update_date timestamp default CURRENT_TIMESTAMP,
    primary key(`id`),
    KEY `link_id` (`link_id`),
	  CONSTRAINT `collection_link_id_constraint` FOREIGN KEY (`link_id`) REFERENCES `link` (`id`),
    KEY `user_id` (`user_id`),
	  CONSTRAINT `collection_user_id_constraint` FOREIGN KEY (`user_id`) REFERENCES `user` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

alter table collection add constraint `uid_link_id_constraint` UNIQUE(`link_id`, `user_id`);

CREATE TABLE pin (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id VARCHAR(24) NOT NULL,
    pins VARCHAR(512),
    pin_date TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`),
    CONSTRAINT `pin_user_id_constraint` FOREIGN KEY (`user_id`)
        REFERENCES `user` (`uid`)
)  ENGINE=INNODB DEFAULT CHARSET=UTF8;