DROP TABLE IF EXISTS messages;
create table messages
(
    id      int PRIMARY KEY AUTO_INCREMENT,
    message text      NOT NULL,
    user    int       not null,
    _date   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Foreign key (user) references users(id)
);

UPDATE properties SET value = 1 WHERE name = 'version';