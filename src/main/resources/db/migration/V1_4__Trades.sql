CREATE SEQUENCE trades_id_seq start 1;

CREATE TABLE trades (
    id int8 not null default nextval('trades_id_seq'),
    order_id int8 not null,
    order_history_id int8 not null,
    message_id bigint not null,
    sequence_number bigint not null,
    external_order_id varchar(255) not null,
    trade_id varchar(255) not null,
    broker_id varchar(255) not null,
    account_id bigint not null,
    wallet_id bigint not null,
    base_asset_id varchar(255) not null,
    base_volume varchar(255) not null,
    price varchar(255) not null,
    timestamp timestamp not null,
    opposite_order_id varchar(255) not null,
    opposite_external_order_id varchar(255) not null,
    opposite_wallet_id varchar(255) not null,
    quoting_asset_id varchar(255) not null,
    quoting_volume varchar(255) not null,
    index int8 not null,
    absolute_spread varchar(255) not null,
    relative_spread varchar(255) not null,
    role int2 not null,
    primary key (id)
);

ALTER SEQUENCE trades_id_seq OWNED BY trades.id;

CREATE UNIQUE INDEX trades_id_idx
    ON trades USING btree (order_id, trade_id);

ALTER TABLE trades ADD CONSTRAINT FK_trades_message_id_messages foreign key (message_id) references messages (id);
ALTER TABLE trades ADD CONSTRAINT FK_trades_sequence_number_messages foreign key (sequence_number) references messages (sequence_number);
ALTER TABLE trades ADD CONSTRAINT FK_trades_order_id_orders foreign key (order_id) references orders (id);
ALTER TABLE trades ADD CONSTRAINT FK_trades_order_history_id_orders_history foreign key (order_history_id) references orders_history (id);
