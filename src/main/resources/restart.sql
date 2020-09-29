drop database if exists chadchat;
drop user if exists 'chadchat'@'localhost';

create database chadchat;
create user 'chadchat'@'localhost';

grant all privileges on chadchat.* to 'chadchat'@'localhost';

create table users(
    id int PRIMARY KEY AUTO_INCREMENT,
    username string(25) NOT NULL UNIQUE,
    salt BINARY(16) NOT NULL,
    secret BINARY(32) NOT NULL,
    _date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
)