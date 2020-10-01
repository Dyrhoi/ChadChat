DROP TABLE IF EXISTS channels;
create table channels
(
    id      int PRIMARY KEY AUTO_INCREMENT,
    name    varchar(25)      UNIQUE,
    _date   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO channels (id, name) VALUES (1, 'global');

ALTER TABLE messages ADD channel int NOT NULL DEFAULT 1;
ALTER TABLE messages ADD FOREIGN KEY (channel) REFERENCES channels(id);

UPDATE properties SET value = 2 WHERE name = 'version';