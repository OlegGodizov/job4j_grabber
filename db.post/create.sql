CREATE SCHEMA post
    CREATE TABLE IF NOT EXISTS posts(
        id serial primary key,
        name varchar(255),
        text text,
        link varchar(255) unique,
        created timestamp
    );