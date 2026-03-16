create table if not exists order_book_row (
  id bigint primary key,                       -- order id from trading-service
  trader_id bigint not null,
  instrument varchar(16) not null,
  side varchar(8) not null,                    -- BUY/SELL
  order_type varchar(16) not null,             -- LIMIT/MARKET (market won’t rest but we store events)
  limit_price numeric(19,6),
  original_qty bigint not null,
  open_qty bigint not null,
  status varchar(24) not null,                 -- NEW/PARTIALLY_FILLED/FILLED/CANCELLED
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);

-- Query indices tuned for “top of book” lookups
create index if not exists idx_ob_bid
  on order_book_row (instrument, side, status, limit_price desc, created_at asc);

create index if not exists idx_ob_ask
  on order_book_row (instrument, side, status, limit_price asc, created_at asc);

create table if not exists trade_history (
  id bigserial primary key,
  event_id varchar(100) not null unique,       -- idempotency guard
  instrument varchar(16) not null,
  buy_order_id bigint not null,
  sell_order_id bigint not null,
  price numeric(19,6) not null,
  quantity bigint not null,
  executed_at timestamp with time zone not null
);

create index if not exists idx_trade_history
  on trade_history (instrument, executed_at desc);