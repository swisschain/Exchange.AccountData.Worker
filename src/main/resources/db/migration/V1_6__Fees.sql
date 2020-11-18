CREATE SEQUENCE fee_instructions_id_seq start 1;

CREATE TABLE fee_instructions (
    id int8 not null default nextval('fee_instructions_id_seq'),
    message_id bigint not null,
    sequence_number bigint not null,
    broker_id varchar(255) not null,
    fee_type int2 not null,
    size varchar(255) null,
    size_type int2 null,
    maker_size varchar(255) null,
    maker_size_type int2 null,
    source_account_id bigint null,
    source_wallet_id bigint null,
    target_account_id bigint null,
    target_wallet_id bigint null,
    assets_ids varchar(255) null,
    maker_fee_modificator varchar(255) null,
    index int8 null,
    order_id int8 null,
    cash_in_id int8 null,
    cash_out_id int8 null,
    cash_transfer_id int8 null,
    primary key (id)
);

ALTER SEQUENCE fee_instructions_id_seq OWNED BY fee_instructions.id;

CREATE UNIQUE INDEX fee_instructions_order_id_idx
    ON fee_instructions USING btree (order_id);

CREATE UNIQUE INDEX fee_instructions_cash_in_id_idx
    ON fee_instructions USING btree (cash_in_id);

CREATE UNIQUE INDEX fee_instructions_cash_out_id_idx
    ON fee_instructions USING btree (cash_out_id);

CREATE UNIQUE INDEX fee_instructions_cash_transfer_id_idx
    ON fee_instructions USING btree (cash_transfer_id);


ALTER TABLE fee_instructions ADD CONSTRAINT FK_fee_instructions_order_id_orders foreign key (order_id) references orders (id);
ALTER TABLE fee_instructions ADD CONSTRAINT FK_fee_instructions_cash_in_id_cash_ins foreign key (cash_in_id) references cash_ins (id);
ALTER TABLE fee_instructions ADD CONSTRAINT FK_fee_instructions_cash_out_id_cash_outs foreign key (cash_out_id) references cash_outs (id);
ALTER TABLE fee_instructions ADD CONSTRAINT FK_fee_instructions_cash_transfer_id_cash_transfers foreign key (cash_transfer_id) references cash_transfers (id);

------------------------------------------------------------------------------------------------------------------------

CREATE SEQUENCE fee_transfers_id_seq start 1;

CREATE TABLE fee_transfers (
    id int8 not null default nextval('fee_transfers_id_seq'),
    message_id bigint not null,
    sequence_number bigint not null,
    broker_id varchar(255) not null,
    volume varchar(255) not null,
    source_account_id bigint null,
    source_wallet_id bigint not null,
    target_account_id bigint null,
    target_wallet_id bigint not null,
    asset_id varchar(255) not null,
    fee_coef varchar(255) null,
    index int8 null,
    order_id int8 null,
    trade_id int8 null,
    cash_in_id int8 null,
    cash_out_id int8 null,
    cash_transfer_id int8 null,
    primary key (id)
);

ALTER SEQUENCE fee_transfers_id_seq OWNED BY fee_transfers.id;

CREATE UNIQUE INDEX fee_transfers_order_id_idx
    ON fee_transfers USING btree (order_id);

CREATE UNIQUE INDEX fee_transfers_cash_in_id_idx
    ON fee_transfers USING btree (cash_in_id);

CREATE UNIQUE INDEX fee_transfers_cash_out_id_idx
    ON fee_transfers USING btree (cash_out_id);

CREATE UNIQUE INDEX fee_transfers_cash_transfer_id_idx
    ON fee_transfers USING btree (cash_transfer_id);

ALTER TABLE fee_transfers ADD CONSTRAINT FK_fee_transfers_order_id_orders foreign key (order_id) references orders (id);
ALTER TABLE fee_transfers ADD CONSTRAINT FK_fee_transfers_trade_id_trades foreign key (trade_id) references trades (id);
ALTER TABLE fee_transfers ADD CONSTRAINT FK_fee_transfers_cash_in_id_cash_ins foreign key (cash_in_id) references cash_ins (id);
ALTER TABLE fee_transfers ADD CONSTRAINT FK_fee_transfers_cash_out_id_cash_outs foreign key (cash_out_id) references cash_outs (id);
ALTER TABLE fee_transfers ADD CONSTRAINT FK_fee_transfers_cash_transfer_id_cash_transfers foreign key (cash_transfer_id) references cash_transfers (id);