CREATE SEQUENCE orders_id_seq start 1;

CREATE TABLE orders (
    id int8 not null default nextval('orders_id_seq'),
    message_id bigint not null,
    sequence_number bigint not null,
    order_type int2 not null,
    me_id varchar(255) not null,
    external_id varchar(255) not null,
    asset_pair_id varchar(255) not null,
    broker_id varchar(255) not null,
    account_id bigint not null,
    wallet_id bigint not null,
    side int2 not null,
    volume varchar(255) not null,
    remaining_volume varchar(255) null,
    price varchar(255) null,
    status int2 not null,
    reject_reason varchar(255) null,
    status_date timestamp not null,
    created_at timestamp not null,
    registered_at timestamp not null,
    last_match_time timestamp null,
    lower_limit_price varchar(255) null,
    lower_price varchar(255) null,
    upper_limit_price varchar(255) null,
    upper_price varchar(255) null,
    time_in_force int2 not null,
    expiry_time timestamp null,
    parent_external_id varchar(255) null,
    child_external_id varchar(255) null,
    primary key (id)
);

ALTER SEQUENCE orders_id_seq OWNED BY orders.id;

CREATE UNIQUE INDEX orders_me_id_idx
    ON orders USING btree (me_id);

CREATE INDEX orders_status_broker_id_wallet_id_idx
    ON orders USING btree(status, broker_id, wallet_id);

ALTER TABLE orders ADD CONSTRAINT FK_orders_message_id_messages foreign key (message_id) references messages (id);
ALTER TABLE orders ADD CONSTRAINT FK_orders_sequence_number_messages foreign key (sequence_number) references messages (sequence_number);

------------------------------------------------------------------------------------------------------------------------

CREATE SEQUENCE orders_history_id_seq start 1;

CREATE TABLE orders_history (
    id int8 not null default nextval('orders_id_seq'),
    message_id bigint not null,
    sequence_number bigint not null,
    order_type int2 not null,
    me_id varchar(255) not null,
    external_id varchar(255) not null,
    asset_pair_id varchar(255) not null,
    broker_id varchar(255) not null,
    account_id bigint not null,
    wallet_id bigint not null,
    side int2 not null,
    volume varchar(255) not null,
    remaining_volume varchar(255) null,
    price varchar(255) null,
    status int2 not null,
    reject_reason varchar(255) null,
    status_date timestamp not null,
    created_at timestamp not null,
    registered_at timestamp not null,
    last_match_time timestamp null,
    lower_limit_price varchar(255) null,
    lower_price varchar(255) null,
    upper_limit_price varchar(255) null,
    upper_price varchar(255) null,
    time_in_force int2 not null,
    expiry_time timestamp null,
    parent_external_id varchar(255) null,
    child_external_id varchar(255) null,
    primary key (id)
);

ALTER SEQUENCE orders_history_id_seq OWNED BY orders_history.id;

CREATE UNIQUE INDEX orders_history_me_id_sequence_number_idx
    ON orders_history USING btree (me_id, sequence_number);

ALTER TABLE orders_history ADD CONSTRAINT FK_orders_history_message_id_messages foreign key (message_id) references messages (id);
ALTER TABLE orders_history ADD CONSTRAINT FK_orders_history_sequence_number_messages foreign key (sequence_number) references messages (sequence_number);