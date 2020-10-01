drop table if exists users_channels;
create table users_channels(
    user int not null,
    channel int not null,
    primary key (user, channel),
    Foreign key (user) references users(id),
    Foreign key (channel) references channels(id)
);

UPDATE properties SET value = 3 WHERE name = 'version';