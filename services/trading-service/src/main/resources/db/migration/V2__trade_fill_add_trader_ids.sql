alter table trade_fill
    add column if not exists buy_trader_id bigint;

alter table trade_fill
    add column if not exists sell_trader_id bigint;

update trade_fill tf
set buy_trader_id = lo.trader_id
from limit_order lo
where tf.buy_order_id = lo.id
  and tf.buy_trader_id is null;

update trade_fill tf
set sell_trader_id = lo.trader_id
from limit_order lo
where tf.sell_order_id = lo.id
  and tf.sell_trader_id is null;

alter table trade_fill
    alter column buy_trader_id set not null;

alter table trade_fill
    alter column sell_trader_id set not null;

alter table trade_fill
    add constraint fk_trade_fill_buy_order
        foreign key (buy_order_id) references limit_order(id);

alter table trade_fill
    add constraint fk_trade_fill_sell_order
        foreign key (sell_order_id) references limit_order(id);

create index if not exists idx_trade_fill_buy_trader_executed
    on trade_fill (buy_trader_id, executed_at desc);

create index if not exists idx_trade_fill_sell_trader_executed
    on trade_fill (sell_trader_id, executed_at desc);