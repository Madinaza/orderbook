create table if not exists order_audit_log (
    id bigserial primary key,
    order_id bigint not null,
    trader_id bigint not null,
    event_type varchar(40) not null,
    message varchar(255) not null,
    created_at timestamp with time zone not null,
    constraint fk_order_audit_log_order
        foreign key (order_id) references limit_order(id)
);

create index if not exists idx_order_audit_log_order_created
    on order_audit_log (order_id, created_at desc);