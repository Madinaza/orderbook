create table if not exists limit_order (
    id bigserial primary key,
    client_order_id varchar(64) not null unique,
    trader_id bigint not null,
    instrument varchar(16) not null,
    side varchar(8) not null,
    order_type varchar(16) not null,
    limit_price numeric(19,6),
    original_qty bigint not null,
    open_qty bigint not null,
    status varchar(24) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    version bigint not null default 0
);

create table if not exists trade_fill (
    id bigserial primary key,
    instrument varchar(16) not null,
    buy_order_id bigint not null,
    sell_order_id bigint not null,
    price numeric(19,6) not null,
    quantity bigint not null,
    executed_at timestamp with time zone not null
);

create index if not exists idx_limit_order_trader_created
    on limit_order (trader_id, created_at desc);

create index if not exists idx_limit_order_instrument_status
    on limit_order (instrument, status);

create index if not exists idx_trade_fill_instrument_executed
    on trade_fill (instrument, executed_at desc);