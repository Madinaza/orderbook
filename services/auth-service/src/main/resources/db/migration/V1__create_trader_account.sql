create table if not exists trader_account (
    id bigserial primary key,
    username varchar(60) not null unique,
    password_hash varchar(255) not null,
    created_at timestamp with time zone not null
);