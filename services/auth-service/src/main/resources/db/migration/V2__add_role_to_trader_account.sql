alter table trader_account
    add column if not exists role varchar(32) not null default 'ROLE_TRADER';