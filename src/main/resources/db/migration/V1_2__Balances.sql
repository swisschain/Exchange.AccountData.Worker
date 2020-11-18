CREATE SEQUENCE balances_id_seq start 1;

CREATE TABLE balances (
    id int8 not null default nextval('balances_id_seq'),
    message_id bigint not null,
    sequence_number bigint not null,
    broker_id varchar(255) not null,
    account_id bigint not null,
    wallet_id bigint not null,
    asset_id varchar(255) not null,
    balance varchar(255) not null,
    reserved varchar(255) not null,
    timestamp timestamp not null,
    primary key (id)
);

ALTER SEQUENCE balances_id_seq OWNED BY balances.id;

CREATE UNIQUE INDEX balances_broker_id_wallet_id_asset_id_idx
    ON balances USING btree (broker_id, wallet_id, asset_id);

ALTER TABLE balances ADD CONSTRAINT FK_balances_message_id_messages foreign key (message_id) references messages (id);
ALTER TABLE balances ADD CONSTRAINT FK_balances_sequence_number_messages foreign key (sequence_number) references messages (sequence_number);

------------------------------------------------------------------------------------------------------------------------

CREATE SEQUENCE balances_updates_id_seq start 1;

CREATE TABLE balance_updates (
    id int8 not null default nextval('balances_updates_id_seq'),
    message_id bigint not null,
    sequence_number bigint not null,
    broker_id varchar(255) not null,
    account_id bigint not null,
    wallet_id bigint not null,
    asset_id varchar(255) not null,
    event_type int2 not null,
    balance varchar(255) not null,
    old_balance varchar(255) not null,
    reserved varchar(255) not null,
    old_reserved varchar(255) not null,
    timestamp timestamp not null,
    primary key (id)
);

ALTER SEQUENCE balances_updates_id_seq OWNED BY balance_updates.id;

CREATE UNIQUE INDEX balance_updates_broker_id_wallet_id_asset_id_idx
    ON balance_updates USING btree (sequence_number, broker_id, wallet_id, asset_id);

ALTER TABLE balance_updates ADD CONSTRAINT FK_balance_updates_message_id_messages foreign key (message_id) references messages (id);
ALTER TABLE balance_updates ADD CONSTRAINT FK_balance_updates_sequence_number_messages foreign key (sequence_number) references messages (sequence_number);