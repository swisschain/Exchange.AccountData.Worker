CREATE SEQUENCE cash_ins_id_seq start 1;

CREATE TABLE cash_ins (
    id int8 not null default nextval('cash_ins_id_seq'),
    message_id bigint not null,
    sequence_number bigint not null,
    balance_update_id bigint not null,
    broker_id varchar(255) not null,
    account_id bigint not null,
    wallet_id bigint not null,
    asset_id varchar(255) not null,
    volume varchar(255) not null,
    description varchar null,
    primary key (id)
);

ALTER SEQUENCE cash_ins_id_seq OWNED BY cash_ins.id;

CREATE UNIQUE INDEX cash_ins_message_id_idx
    ON cash_ins USING btree (message_id);

ALTER TABLE cash_ins ADD CONSTRAINT FK_cash_ins_message_id_messages foreign key (message_id) references messages (id);
ALTER TABLE cash_ins ADD CONSTRAINT FK_cash_ins_sequence_number_messages foreign key (sequence_number) references messages (sequence_number);
ALTER TABLE cash_ins ADD CONSTRAINT FK_cash_ins_balance_update_id foreign key (balance_update_id) references balance_updates (id);

------------------------------------------------------------------------------------------------------------------------

CREATE SEQUENCE cash_outs_id_seq start 1;

CREATE TABLE cash_outs (
    id int8 not null default nextval('cash_outs_id_seq'),
    message_id bigint not null,
    sequence_number bigint not null,
    balance_update_id bigint not null,
    broker_id varchar(255) not null,
    account_id bigint not null,
    wallet_id bigint not null,
    asset_id varchar(255) not null,
    volume varchar(255) not null,
    description varchar null,
    primary key (id)
);

ALTER SEQUENCE cash_outs_id_seq OWNED BY cash_outs.id;

CREATE UNIQUE INDEX cash_outs_message_id_idx
    ON cash_outs USING btree (message_id);

ALTER TABLE cash_outs ADD CONSTRAINT FK_cash_outs_message_id_messages foreign key (message_id) references messages (id);
ALTER TABLE cash_outs ADD CONSTRAINT FK_cash_outs_sequence_number_messages foreign key (sequence_number) references messages (sequence_number);
ALTER TABLE cash_outs ADD CONSTRAINT FK_cash_outs_balance_update_id foreign key (balance_update_id) references balance_updates (id);

------------------------------------------------------------------------------------------------------------------------

CREATE SEQUENCE cash_transfers_id_seq start 1;

CREATE TABLE cash_transfers (
    id int8 not null default nextval('cash_transfers_id_seq'),
    message_id bigint not null,
    sequence_number bigint not null,
    balance_update_id bigint not null,
    broker_id varchar(255) not null,
    account_id bigint not null,
    from_wallet_id bigint not null,
    to_wallet_id bigint not null,
    asset_id varchar(255) not null,
    volume varchar(255) not null,
    overdraftLimit varchar(255) null,
    description varchar null,
    primary key (id)
);

ALTER SEQUENCE cash_transfers_id_seq OWNED BY cash_transfers.id;

CREATE UNIQUE INDEX cash_transfers_message_id_idx
    ON cash_transfers USING btree (message_id);

ALTER TABLE cash_transfers ADD CONSTRAINT FK_cash_transfers_message_id_messages foreign key (message_id) references messages (id);
ALTER TABLE cash_transfers ADD CONSTRAINT FK_cash_transfers_sequence_number_messages foreign key (sequence_number) references messages (sequence_number);
ALTER TABLE cash_transfers ADD CONSTRAINT FK_cash_transfers_balance_update_id foreign key (balance_update_id) references balance_updates (id);